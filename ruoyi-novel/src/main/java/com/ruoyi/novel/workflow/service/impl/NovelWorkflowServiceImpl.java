package com.ruoyi.novel.workflow.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.rag.service.INovelContextService;
import com.ruoyi.novel.service.INovelProjectService;
import com.ruoyi.novel.workflow.NovelWorkflowEngine;
import com.ruoyi.novel.workflow.domain.NovelWorkflowConfirmRequest;
import com.ruoyi.novel.workflow.domain.NovelWorkflowContext;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRunDetail;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStartRequest;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStep;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;
import com.ruoyi.novel.workflow.enums.NovelWorkflowRunStatus;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepStatus;
import com.ruoyi.novel.workflow.domain.NovelWorkflowEvent;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowEventMapper;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowRunMapper;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowStepMapper;
import com.ruoyi.novel.workflow.service.INovelWorkflowService;

@Service
public class NovelWorkflowServiceImpl implements INovelWorkflowService
{
    @Autowired
    private NovelWorkflowEventMapper novelWorkflowEventMapper;

    @Autowired
    private NovelWorkflowRunMapper novelWorkflowRunMapper;

    @Autowired
    private NovelWorkflowStepMapper novelWorkflowStepMapper;

    @Autowired
    private NovelWorkflowEngine novelWorkflowEngine;

    @Autowired
    private INovelProjectService novelProjectService;

    @Autowired
    private INovelContextService novelContextService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NovelWorkflowRun startWorkflow(NovelWorkflowStartRequest request, Long userId, String username)
    {
        if (request.getProjectId() == null)
        {
            throw new ServiceException("???ID???????");
        }
        NovelProject project = novelProjectService.selectNovelProjectByProjectId(request.getProjectId());
        if (project == null)
        {
            throw new ServiceException("?????????");
        }
        NovelWorkflowRun existing = novelWorkflowRunMapper.selectActiveRunByProjectId(request.getProjectId());
        if (existing != null)
        {
            throw new ServiceException("???????????????????????????????????");
        }
        NovelWorkflowRun run = new NovelWorkflowRun();
        run.setProjectId(request.getProjectId());
        run.setUserId(userId);
        run.setWorkflowType(StringUtils.isNotEmpty(request.getWorkflowType()) ? request.getWorkflowType() : "full_creation");
        run.setStatus(NovelWorkflowRunStatus.RUNNING.getCode());
        run.setCurrentStep(NovelWorkflowStepCode.INIT_PROJECT.getCode());
        run.setUserIdea(request.getUserIdea());
        run.setContextJson(JSON.toJSONString(new NovelWorkflowContext()));
        run.setCreateBy(username);
        novelWorkflowRunMapper.insertNovelWorkflowRun(run);

        novelWorkflowEngine.startStep(run, NovelWorkflowStepCode.INIT_PROJECT);
        workflowEventPublisher.publish(run.getRunId(), null, NovelWorkflowEventType.RUN_STATUS.getCode(),
            java.util.Collections.singletonMap("status", run.getStatus()));
        return run;
    }

    @Override
    public NovelWorkflowRunDetail getRunDetail(Long runId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("????????????????");
        }
        NovelWorkflowRunDetail detail = new NovelWorkflowRunDetail();
        detail.setRun(run);
        detail.setSteps(novelWorkflowStepMapper.selectStepsByRunId(runId));
        if (StringUtils.isNotEmpty(run.getCurrentStep()))
        {
            detail.setCurrentStepRecord(
                novelWorkflowStepMapper.selectLatestStepByRunIdAndCode(runId, run.getCurrentStep()));
        }
        detail.setEvents(novelWorkflowEventMapper.selectEventsByRunIdAfterId(runId, null));
        return detail;
    }

    @Override
    public NovelWorkflowRunDetail getActiveRunByProject(Long projectId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectActiveRunByProjectId(projectId);
        if (run == null)
        {
            return null;
        }
        return getRunDetail(run.getRunId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStep(Long runId, NovelWorkflowConfirmRequest request, String username)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        novelWorkflowEngine.ensureWaitingConfirm(run);
        NovelWorkflowStepCode current = NovelWorkflowStepCode.fromCode(run.getCurrentStep());
        if (current == null)
        {
            throw new ServiceException("???????????");
        }
        NovelWorkflowStep step = novelWorkflowStepMapper.selectLatestStepByRunIdAndCode(runId, current.getCode());
        if (step != null)
        {
            if (request != null && StringUtils.isNotEmpty(request.getOutputSnapshot()))
            {
                step.setOutputSnapshot(request.getOutputSnapshot());
            }
            step.setStatus(NovelWorkflowStepStatus.COMPLETED.getCode());
            novelWorkflowStepMapper.updateNovelWorkflowStep(step);
        }
        workflowEventPublisher.publish(runId, step != null ? step.getStepId() : null,
            NovelWorkflowEventType.USER_CONFIRM.getCode(), null);

        if (current == NovelWorkflowStepCode.CHAPTER_PLANNING)
        {
            novelWorkflowEngine.afterChapterPlanning(run);
            novelContextService.indexProjectContent(run.getProjectId());
        }

        NovelWorkflowStepCode next = novelWorkflowEngine.resolveNextStep(run, current);
        if (next == null)
        {
            run.setStatus(NovelWorkflowRunStatus.COMPLETED.getCode());
            run.setUpdateBy(username);
            novelWorkflowRunMapper.updateNovelWorkflowRun(run);
            workflowEventPublisher.publish(runId, null, NovelWorkflowEventType.RUN_STATUS.getCode(),
                java.util.Collections.singletonMap("status", NovelWorkflowRunStatus.COMPLETED.getCode()));
            return;
        }
        novelWorkflowEngine.startStep(run, next);
    }

    @Override
    public void pauseRun(Long runId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("????????????");
        }
        run.setStatus(NovelWorkflowRunStatus.PAUSED.getCode());
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);
    }

    @Override
    public void resumeRun(Long runId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("????????????");
        }
        if (NovelWorkflowRunStatus.PAUSED.getCode().equals(run.getStatus()))
        {
            run.setStatus(NovelWorkflowRunStatus.WAITING_CONFIRM.getCode());
            novelWorkflowRunMapper.updateNovelWorkflowRun(run);
        }
    }

    @Override
    public void retryCurrentStep(Long runId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("????????????");
        }
        NovelWorkflowStepCode stepCode = NovelWorkflowStepCode.fromCode(run.getCurrentStep());
        if (stepCode == null)
        {
            throw new ServiceException("???????????");
        }
        run.setStatus(NovelWorkflowRunStatus.RUNNING.getCode());
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);
        novelWorkflowEngine.startStep(run, stepCode);
    }
}
