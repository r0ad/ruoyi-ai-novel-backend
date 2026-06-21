package com.ruoyi.novel.ai.apply.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.apply.INovelAiApplyService;
import com.ruoyi.novel.ai.domain.dto.AiTaskApplyRequest;
import com.ruoyi.novel.ai.domain.dto.ApplyResult;
import com.ruoyi.novel.ai.domain.dto.ExtractEntityItem;
import com.ruoyi.novel.ai.domain.dto.ExtractRelationItem;
import com.ruoyi.novel.ai.domain.dto.ExtractResult;
import com.ruoyi.novel.ai.domain.dto.ReviewIssue;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;
import com.ruoyi.novel.ai.domain.dto.SettingDraftResult;
import com.ruoyi.novel.ai.domain.dto.SyncSettingMetaResult;
import com.ruoyi.novel.ai.task.domain.NovelAiTask;
import com.ruoyi.novel.ai.task.enums.NovelAiTaskStatus;
import com.ruoyi.novel.ai.task.enums.NovelAiTaskType;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.domain.NovelMetaRelation;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.service.INovelMetaService;
import com.ruoyi.novel.service.INovelSettingService;

@Service
public class NovelAiApplyServiceImpl implements INovelAiApplyService
{
    @Autowired
    private INovelMetaService novelMetaService;

    @Autowired
    private INovelSettingService novelSettingService;

    @Override
    public ApplyResult apply(NovelAiTask task, AiTaskApplyRequest request, String operator)
    {
        if (task == null)
        {
            throw new ServiceException("任务不存在");
        }
        if (!NovelAiTaskStatus.COMPLETED.equals(task.getStatus()))
        {
            throw new ServiceException("仅已完成任务可应用");
        }
        if (NovelAiTaskType.EXTRACT_META.equals(task.getTaskType()))
        {
            return applyExtractMeta(task, request);
        }
        if (NovelAiTaskType.EXTRACT_SETTING.equals(task.getTaskType())
            || NovelAiTaskType.GENERATE_SETTING.equals(task.getTaskType()))
        {
            return applySettingDraft(task, request);
        }
        if (NovelAiTaskType.SYNC_SETTING_META.equals(task.getTaskType()))
        {
            return applySyncSettingMeta(task, request);
        }
        if (NovelAiTaskType.REVIEW_CHAPTER.equals(task.getTaskType())
            || NovelAiTaskType.REVIEW_PROJECT.equals(task.getTaskType()))
        {
            return applyReviewFixes(task, request);
        }
        throw new ServiceException("当前任务类型暂不支持应用：" + task.getTaskType());
    }

    private ApplyResult applySettingDraft(NovelAiTask task, AiTaskApplyRequest request)
    {
        SettingDraftResult draft = JSON.parseObject(task.getResultJson(), SettingDraftResult.class);
        if (draft == null || StringUtils.isEmpty(draft.getContent()))
        {
            throw new ServiceException("设定草案内容为空");
        }
        String settingType = StringUtils.isNotEmpty(draft.getSettingType())
            ? draft.getSettingType() : task.getTargetId();
        if (StringUtils.isEmpty(settingType))
        {
            settingType = "characters";
        }
        ApplyResult result = new ApplyResult();
        NovelSetting setting = new NovelSetting();
        setting.setProjectId(task.getProjectId());
        setting.setSettingType(settingType);
        setting.setTitle(StringUtils.isNotEmpty(draft.getTitle()) ? draft.getTitle() : settingType);
        if ("replace".equals(request.getOverwriteMode()))
        {
            setting.setContent(draft.getContent());
        }
        else
        {
            NovelSetting existing = novelSettingService.selectNovelSettingByProjectAndType(
                task.getProjectId(), settingType);
            String base = existing != null && StringUtils.isNotEmpty(existing.getContent())
                ? existing.getContent() : "";
            setting.setContent(base.isEmpty() ? draft.getContent() : base + "\n\n" + draft.getContent());
        }
        novelSettingService.saveNovelSetting(setting);
        result.setSettingsUpdated(1);
        result.setMessage("已保存设定：" + settingType);
        return result;
    }

    private ApplyResult applySyncSettingMeta(NovelAiTask task, AiTaskApplyRequest request)
    {
        SyncSettingMetaResult sync = JSON.parseObject(task.getResultJson(), SyncSettingMetaResult.class);
        if (sync == null || sync.getSyncActions() == null)
        {
            throw new ServiceException("同步建议为空");
        }
        ApplyResult result = new ApplyResult();
        Map<String, Long> nameToId = loadEntityNameMap(task.getProjectId());
        for (int i = 0; i < sync.getSyncActions().size(); i++)
        {
            if (!shouldApplyIndex(request.getSelectedEntityIndexes(), i))
            {
                continue;
            }
            Map<String, Object> action = sync.getSyncActions().get(i);
            executeAction(task.getProjectId(), action, result, nameToId, request.getOverwriteMode());
        }
        result.setMessage(String.format("已更新设定 %d 条、Meta 重命名 %d 条、新增实体 %d 条",
            result.getSettingsUpdated(), result.getMetaRenamed(), result.getEntitiesInserted()));
        return result;
    }

    private ApplyResult applyReviewFixes(NovelAiTask task, AiTaskApplyRequest request)
    {
        ReviewResult review = JSON.parseObject(task.getResultJson(), ReviewResult.class);
        if (review == null || review.getIssues() == null)
        {
            throw new ServiceException("审查结果为空");
        }
        ApplyResult result = new ApplyResult();
        Map<String, Long> nameToId = loadEntityNameMap(task.getProjectId());
        for (ReviewIssue issue : review.getIssues())
        {
            if (!shouldApplyIssue(request.getSelectedIssueIds(), issue.getId()))
            {
                continue;
            }
            if (issue.getSuggestedActions() == null)
            {
                continue;
            }
            for (Map<String, Object> action : issue.getSuggestedActions())
            {
                executeAction(task.getProjectId(), action, result, nameToId, request.getOverwriteMode());
            }
        }
        result.setMessage(String.format("已修复：设定 %d 条、Meta 重命名 %d 条、新增实体 %d 条",
            result.getSettingsUpdated(), result.getMetaRenamed(), result.getEntitiesInserted()));
        return result;
    }

    private void executeAction(Long projectId, Map<String, Object> action, ApplyResult result,
        Map<String, Long> nameToId, String overwriteMode)
    {
        if (action == null)
        {
            return;
        }
        String type = String.valueOf(action.get("type"));
        if ("update_setting".equals(type) || "append_setting".equals(type))
        {
            applySettingAction(projectId, action, result, overwriteMode);
        }
        else if ("update_meta_entity".equals(type) || "insert_meta_entity".equals(type))
        {
            applyMetaAction(projectId, action, result, nameToId);
        }
    }

    private void applySettingAction(Long projectId, Map<String, Object> action, ApplyResult result, String overwriteMode)
    {
        String settingType = action.get("settingType") != null
            ? String.valueOf(action.get("settingType")) : "characters";
        NovelSetting existing = novelSettingService.selectNovelSettingByProjectAndType(projectId, settingType);
        String content = existing != null ? existing.getContent() : "";
        String oldName = action.get("oldName") != null ? String.valueOf(action.get("oldName")) : null;
        String newName = action.get("newName") != null ? String.valueOf(action.get("newName")) : null;
        if (StringUtils.isNotEmpty(oldName) && StringUtils.isNotEmpty(newName))
        {
            content = StringUtils.isNotEmpty(content)
                ? content.replace(oldName, newName) : newName;
        }
        else if (action.get("content") != null)
        {
            String patch = String.valueOf(action.get("content"));
            content = "replace".equals(overwriteMode) ? patch
                : (StringUtils.isEmpty(content) ? patch : content + "\n\n" + patch);
        }
        else if (action.get("hint") != null)
        {
            String hint = String.valueOf(action.get("hint"));
            content = StringUtils.isEmpty(content) ? hint : content + "\n\n<!-- AI -->\n" + hint;
        }
        NovelSetting setting = new NovelSetting();
        setting.setProjectId(projectId);
        setting.setSettingType(settingType);
        setting.setTitle(existing != null && StringUtils.isNotEmpty(existing.getTitle())
            ? existing.getTitle() : settingType);
        setting.setContent(content);
        novelSettingService.saveNovelSetting(setting);
        result.setSettingsUpdated(result.getSettingsUpdated() + 1);
    }

    private void applyMetaAction(Long projectId, Map<String, Object> action, ApplyResult result,
        Map<String, Long> nameToId)
    {
        String entityName = action.get("entityName") != null ? String.valueOf(action.get("entityName")) : null;
        String newName = action.get("newName") != null ? String.valueOf(action.get("newName")) : null;
        if (StringUtils.isNotEmpty(entityName) && StringUtils.isNotEmpty(newName))
        {
            Long entityId = nameToId.get(entityName);
            if (entityId != null)
            {
                NovelMetaEntity entity = novelMetaService.selectEntityById(entityId);
                if (entity != null)
                {
                    entity.setName(newName);
                    if (action.get("description") != null)
                    {
                        entity.setDescription(String.valueOf(action.get("description")));
                    }
                    novelMetaService.updateEntity(entity);
                    nameToId.remove(entityName);
                    nameToId.put(newName, entityId);
                    result.setMetaRenamed(result.getMetaRenamed() + 1);
                }
            }
        }
        else if ("insert_meta_entity".equals(String.valueOf(action.get("type"))))
        {
            String name = action.get("name") != null ? String.valueOf(action.get("name")) : entityName;
            if (StringUtils.isEmpty(name) || nameToId.containsKey(name))
            {
                return;
            }
            NovelMetaEntity entity = new NovelMetaEntity();
            entity.setProjectId(projectId);
            entity.setName(name);
            entity.setEntityType(action.get("entityType") != null
                ? String.valueOf(action.get("entityType")) : "character");
            if (action.get("description") != null)
            {
                entity.setDescription(String.valueOf(action.get("description")));
            }
            novelMetaService.insertEntity(entity);
            nameToId.put(name, entity.getEntityId());
            result.setEntitiesInserted(result.getEntitiesInserted() + 1);
        }
    }

    private Map<String, Long> loadEntityNameMap(Long projectId)
    {
        Map<String, Long> nameToId = new HashMap<String, Long>();
        NovelMetaEntity query = new NovelMetaEntity();
        query.setProjectId(projectId);
        for (NovelMetaEntity entity : novelMetaService.selectEntityList(query))
        {
            nameToId.put(entity.getName(), entity.getEntityId());
        }
        return nameToId;
    }

    private ApplyResult applyExtractMeta(NovelAiTask task, AiTaskApplyRequest request)
    {
        ExtractResult extract = JSON.parseObject(task.getResultJson(), ExtractResult.class);
        if (extract == null)
        {
            throw new ServiceException("任务结果为空");
        }
        Long chapterId = task.resolveChapterId();
        ApplyResult result = new ApplyResult();
        Map<String, Long> nameToId = loadEntityNameMap(task.getProjectId());

        List<ExtractEntityItem> entities = extract.getEntities() != null
            ? extract.getEntities() : java.util.Collections.emptyList();
        for (int i = 0; i < entities.size(); i++)
        {
            if (!shouldApplyIndex(request.getSelectedEntityIndexes(), i))
            {
                continue;
            }
            ExtractEntityItem item = entities.get(i);
            if (StringUtils.isEmpty(item.getName()) || StringUtils.isEmpty(item.getEntityType()))
            {
                continue;
            }
            Long existingId = nameToId.get(item.getName());
            if (existingId != null && "merge".equals(request.getOverwriteMode()))
            {
                NovelMetaEntity update = novelMetaService.selectEntityById(existingId);
                if (update != null)
                {
                    if (StringUtils.isNotEmpty(item.getDescription()))
                    {
                        update.setDescription(item.getDescription());
                    }
                    update.setLastChapterId(chapterId);
                    novelMetaService.updateEntity(update);
                    result.setEntitiesUpdated(result.getEntitiesUpdated() + 1);
                }
            }
            else if (existingId == null)
            {
                NovelMetaEntity entity = new NovelMetaEntity();
                entity.setProjectId(task.getProjectId());
                entity.setEntityType(item.getEntityType());
                entity.setName(item.getName());
                entity.setDescription(item.getDescription());
                entity.setFirstChapterId(chapterId);
                entity.setLastChapterId(chapterId);
                novelMetaService.insertEntity(entity);
                nameToId.put(item.getName(), entity.getEntityId());
                result.setEntitiesInserted(result.getEntitiesInserted() + 1);
            }
        }

        List<ExtractRelationItem> relations = extract.getRelations() != null
            ? extract.getRelations() : java.util.Collections.emptyList();
        for (int i = 0; i < relations.size(); i++)
        {
            if (!shouldApplyIndex(request.getSelectedRelationIndexes(), i))
            {
                continue;
            }
            ExtractRelationItem rel = relations.get(i);
            Long sourceId = nameToId.get(rel.getFrom());
            Long targetId = nameToId.get(rel.getTo());
            if (sourceId == null || targetId == null)
            {
                continue;
            }
            NovelMetaRelation relation = new NovelMetaRelation();
            relation.setProjectId(task.getProjectId());
            relation.setSourceId(sourceId);
            relation.setTargetId(targetId);
            relation.setRelationType(StringUtils.isNotEmpty(rel.getRelationType()) ? rel.getRelationType() : "related");
            relation.setChapterId(chapterId);
            relation.setSourceName(rel.getFrom());
            relation.setTargetName(rel.getTo());
            novelMetaService.insertRelation(relation);
            result.setRelationsInserted(result.getRelationsInserted() + 1);
        }

        result.setMessage(String.format("已写入实体 %d 条、更新 %d 条、关系 %d 条",
            result.getEntitiesInserted(), result.getEntitiesUpdated(), result.getRelationsInserted()));
        return result;
    }

    private boolean shouldApplyIndex(List<Integer> selected, int index)
    {
        return selected == null || selected.isEmpty() || selected.contains(index);
    }

    private boolean shouldApplyIssue(List<String> selectedIssueIds, String issueId)
    {
        return selectedIssueIds == null || selectedIssueIds.isEmpty()
            || selectedIssueIds.contains(issueId);
    }
}
