package com.ruoyi.novel.ai.task.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.apply.INovelAiApplyService;
import com.ruoyi.novel.ai.capability.INovelAiCapabilityService;
import com.ruoyi.novel.ai.context.ContextOptions;
import com.ruoyi.novel.ai.domain.dto.AiTaskApplyRequest;
import com.ruoyi.novel.ai.domain.dto.AiTaskCreateRequest;
import com.ruoyi.novel.ai.domain.dto.ApplyResult;
import com.ruoyi.novel.ai.invocation.INovelAiInvocationService;
import com.ruoyi.novel.ai.invocation.domain.AiInvocationRecord;
import com.ruoyi.novel.ai.task.domain.NovelAiTask;
import com.ruoyi.novel.ai.task.enums.NovelAiTaskStatus;
import com.ruoyi.novel.ai.task.enums.NovelAiTaskType;
import com.ruoyi.novel.ai.task.mapper.NovelAiTaskMapper;
import com.ruoyi.novel.ai.task.service.INovelAiTaskService;

@Service
public class NovelAiTaskServiceImpl implements INovelAiTaskService
{
    @Autowired
    private NovelAiTaskMapper novelAiTaskMapper;

    @Autowired
    private INovelAiCapabilityService novelAiCapabilityService;

    @Autowired
    private INovelAiApplyService novelAiApplyService;

    @Autowired
    private INovelAiInvocationService novelAiInvocationService;

    @Override
    public NovelAiTask createAndRun(AiTaskCreateRequest request, String operator)
    {
        validateCreateRequest(request);
        NovelAiTask task = new NovelAiTask();
        task.setProjectId(request.getProjectId());
        task.setTaskType(request.getTaskType());
        task.setTargetType(request.getTargetType());
        task.setTargetId(request.getTargetId());
        task.setStatus(NovelAiTaskStatus.RUNNING);
        task.setInputJson(JSON.toJSONString(request));
        task.setCreateBy(operator);
        novelAiTaskMapper.insertNovelAiTask(task);
        try
        {
            ContextOptions options = buildContextOptions(request);
            Object result = executeCapability(request.getTaskType(), options);
            task.setResultJson(JSON.toJSONString(result));
            task.setStatus(NovelAiTaskStatus.COMPLETED);
            task.setErrorMessage(null);
        }
        catch (Exception ex)
        {
            task.setStatus(NovelAiTaskStatus.FAILED);
            task.setErrorMessage(ex.getMessage());
        }
        finally
        {
            attachPromptJson(task);
            novelAiInvocationService.clearLastInvocation();
            novelAiTaskMapper.updateNovelAiTask(task);
        }
        return task;
    }

    private void attachPromptJson(NovelAiTask task)
    {
        AiInvocationRecord invocation = novelAiInvocationService.getLastInvocation();
        if (invocation != null)
        {
            task.setPromptJson(JSON.toJSONString(invocation));
        }
    }

    @Override
    public NovelAiTask getTask(Long taskId)
    {
        NovelAiTask task = novelAiTaskMapper.selectNovelAiTaskByTaskId(taskId);
        if (task == null)
        {
            throw new ServiceException("AI 任务不存在");
        }
        return task;
    }

    @Override
    public List<NovelAiTask> listTasks(NovelAiTask query)
    {
        return novelAiTaskMapper.selectNovelAiTaskList(query);
    }

    @Override
    public ApplyResult applyTask(Long taskId, AiTaskApplyRequest request, String operator)
    {
        NovelAiTask task = getTask(taskId);
        ApplyResult result = novelAiApplyService.apply(task, request, operator);
        task.setStatus(NovelAiTaskStatus.APPLIED);
        task.setAppliedAt(new Date());
        task.setAppliedBy(operator);
        novelAiTaskMapper.updateNovelAiTask(task);
        return result;
    }

    private void validateCreateRequest(AiTaskCreateRequest request)
    {
        if (request == null || request.getProjectId() == null)
        {
            throw new ServiceException("项目ID不能为空");
        }
        if (StringUtils.isEmpty(request.getTaskType()))
        {
            throw new ServiceException("任务类型不能为空");
        }
    }

    private ContextOptions buildContextOptions(AiTaskCreateRequest request)
    {
        ContextOptions options = new ContextOptions();
        options.setProjectId(request.getProjectId());
        options.setChapterId(request.resolveChapterId());
        options.setIncludeMetaGraph(true);
        Map<String, Object> opts = request.getOptions();
        if (opts != null)
        {
            if (opts.get("includeMeta") != null)
            {
                options.setIncludeMetaGraph(Boolean.TRUE.equals(opts.get("includeMeta"))
                    || "true".equals(String.valueOf(opts.get("includeMeta"))));
            }
            if (opts.get("userMessage") != null)
            {
                options.setUserMessage(String.valueOf(opts.get("userMessage")));
            }
            if (opts.get("settingType") != null)
            {
                options.setSettingType(String.valueOf(opts.get("settingType")));
            }
            else if ("setting".equals(request.getTargetType()) && StringUtils.isNotEmpty(request.getTargetId()))
            {
                options.setSettingType(request.getTargetId());
            }
            Object chapterIds = opts.get("chapterIds");
            if (chapterIds instanceof List)
            {
                List<Long> ids = new ArrayList<Long>();
                for (Object item : (List<?>) chapterIds)
                {
                    if (item != null)
                    {
                        ids.add(Long.valueOf(String.valueOf(item)));
                    }
                }
                options.setChapterIds(ids);
            }
        }
        return options;
    }

    private Object executeCapability(String taskType, ContextOptions options)
    {
        if (NovelAiTaskType.REVIEW_CHAPTER.equals(taskType))
        {
            return novelAiCapabilityService.reviewChapter(options);
        }
        if (NovelAiTaskType.REVIEW_PROJECT.equals(taskType))
        {
            return novelAiCapabilityService.reviewProject(options);
        }
        if (NovelAiTaskType.EXTRACT_META.equals(taskType))
        {
            return novelAiCapabilityService.extractMetaPreview(options);
        }
        if (NovelAiTaskType.EXTRACT_SETTING.equals(taskType))
        {
            return novelAiCapabilityService.extractSettingFromChapters(options);
        }
        if (NovelAiTaskType.GENERATE_SETTING.equals(taskType))
        {
            return novelAiCapabilityService.generateSetting(options);
        }
        if (NovelAiTaskType.SYNC_SETTING_META.equals(taskType))
        {
            return novelAiCapabilityService.syncSettingAndMeta(options);
        }
        throw new ServiceException("不支持的任务类型：" + taskType);
    }
}
