package com.ruoyi.novel.ai.task.service.impl;

import java.util.Date;
import java.util.List;
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
import com.ruoyi.novel.ai.domain.dto.ExtractResult;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;
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
        novelAiTaskMapper.updateNovelAiTask(task);
        return task;
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
        if (request.getOptions() != null && request.getOptions().get("includeMeta") != null)
        {
            options.setIncludeMetaGraph(Boolean.TRUE.equals(request.getOptions().get("includeMeta"))
                || "true".equals(String.valueOf(request.getOptions().get("includeMeta"))));
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
        throw new ServiceException("不支持的任务类型：" + taskType);
    }
}
