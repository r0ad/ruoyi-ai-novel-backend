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
import com.ruoyi.novel.ai.task.domain.NovelAiTask;
import com.ruoyi.novel.ai.task.enums.NovelAiTaskStatus;
import com.ruoyi.novel.ai.task.enums.NovelAiTaskType;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.domain.NovelMetaRelation;
import com.ruoyi.novel.service.INovelMetaService;

@Service
public class NovelAiApplyServiceImpl implements INovelAiApplyService
{
    @Autowired
    private INovelMetaService novelMetaService;

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
        throw new ServiceException("当前任务类型暂不支持应用：" + task.getTaskType());
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
        Map<String, Long> nameToId = new HashMap<String, Long>();

        NovelMetaEntity query = new NovelMetaEntity();
        query.setProjectId(task.getProjectId());
        List<NovelMetaEntity> existing = novelMetaService.selectEntityList(query);
        for (NovelMetaEntity entity : existing)
        {
            nameToId.put(entity.getName(), entity.getEntityId());
        }

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
}
