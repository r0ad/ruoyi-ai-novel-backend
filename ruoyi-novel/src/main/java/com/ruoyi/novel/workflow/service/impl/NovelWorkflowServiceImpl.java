package com.ruoyi.novel.workflow.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ruoyi.novel.ai.session.domain.NovelAiMessage;
import com.ruoyi.novel.workflow.NovelWorkflowEngine;
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
import com.ruoyi.novel.ai.session.service.INovelAiSessionService;
import com.ruoyi.novel.workflow.NovelWorkflowStepRunner;
import com.ruoyi.novel.workflow.NovelWorkflowStepValidator;
import com.ruoyi.novel.workflow.domain.NovelWorkflowChatRequest;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStepReadiness;
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

    @Autowired
    private INovelAiSessionService novelAiSessionService;

    @Autowired
    private NovelWorkflowStepRunner novelWorkflowStepRunner;

    @Autowired
    private NovelWorkflowStepValidator novelWorkflowStepValidator;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NovelWorkflowRun startWorkflow(NovelWorkflowStartRequest request, Long userId, String username)
    {
        if (request.getProjectId() == null)
        {
            throw new ServiceException("项目ID不能为空");
        }
        NovelProject project = novelProjectService.selectNovelProjectByProjectId(request.getProjectId());
        if (project == null)
        {
            throw new ServiceException("项目不存在");
        }
        NovelWorkflowRun existing = novelWorkflowRunMapper.selectActiveRunByProjectId(request.getProjectId());
        if (existing != null)
        {
            throw new ServiceException("该项目已有进行中的工作流，请先完成或暂停");
        }
        NovelWorkflowRun run = new NovelWorkflowRun();
        run.setProjectId(request.getProjectId());
        run.setUserId(userId);
        run.setWorkflowType(StringUtils.isNotEmpty(request.getWorkflowType()) ? request.getWorkflowType() : "planning_only");
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
        return getRunDetail(runId, null);
    }

    @Override
    public NovelWorkflowRunDetail getRunDetail(Long runId, String messageStepCode)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("工作流运行不存在");
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
        enrichStepContext(detail, run, messageStepCode);
        return detail;
    }

    @Override
    public List<NovelAiMessage> getStepMessages(Long runId, String stepCode)
    {
        if (runId == null || StringUtils.isEmpty(stepCode))
        {
            return Collections.emptyList();
        }
        NovelWorkflowStep step = novelWorkflowStepMapper.selectLatestStepByRunIdAndCode(runId, stepCode);
        if (step == null || step.getAgentSessionId() == null)
        {
            return Collections.emptyList();
        }
        return novelAiSessionService.listMessages(step.getAgentSessionId());
    }

    private void enrichStepContext(NovelWorkflowRunDetail detail, NovelWorkflowRun run, String messageStepCode)
    {
        String loadCode = StringUtils.isNotEmpty(messageStepCode) ? messageStepCode : run.getCurrentStep();
        if (StringUtils.isNotEmpty(loadCode))
        {
            detail.setStepMessages(getStepMessages(run.getRunId(), loadCode));
        }
        else
        {
            detail.setStepMessages(Collections.emptyList());
        }
        NovelWorkflowStepCode stepCode = NovelWorkflowStepCode.fromCode(run.getCurrentStep());
        if (stepCode != null)
        {
            detail.setStepReadiness(novelWorkflowStepValidator.evaluate(run.getProjectId(), stepCode));
        }
    }

    @Override
    public NovelWorkflowRunDetail getActiveRunByProject(Long projectId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectActiveRunByProjectId(projectId);
        if (run == null)
        {
            run = novelWorkflowRunMapper.selectLatestRunByProjectId(projectId);
        }
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
            throw new ServiceException("当前步骤无效");
        }
        NovelWorkflowStepReadiness readiness = novelWorkflowStepValidator.evaluate(run.getProjectId(), current);
        if (!readiness.isReady())
        {
            throw new ServiceException(readiness.getHint());
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
            throw new ServiceException("工作流不存在");
        }
        run.setStatus(NovelWorkflowRunStatus.PAUSED.getCode());
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);
        workflowEventPublisher.publish(runId, null, NovelWorkflowEventType.RUN_STATUS.getCode(),
            java.util.Collections.singletonMap("status", NovelWorkflowRunStatus.PAUSED.getCode()));
    }

    @Override
    public void resumeRun(Long runId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("工作流不存在");
        }
        if (NovelWorkflowRunStatus.PAUSED.getCode().equals(run.getStatus()))
        {
            run.setStatus(NovelWorkflowRunStatus.WAITING_CONFIRM.getCode());
            novelWorkflowRunMapper.updateNovelWorkflowRun(run);
            workflowEventPublisher.publish(runId, null, NovelWorkflowEventType.RUN_STATUS.getCode(),
                java.util.Collections.singletonMap("status", NovelWorkflowRunStatus.WAITING_CONFIRM.getCode()));
        }
    }

    @Override
    public void retryCurrentStep(Long runId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("工作流不存在");
        }
        NovelWorkflowStepCode stepCode = NovelWorkflowStepCode.fromCode(run.getCurrentStep());
        if (stepCode == null)
        {
            throw new ServiceException("当前步骤无效");
        }
        run.setStatus(NovelWorkflowRunStatus.RUNNING.getCode());
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);
        novelWorkflowEngine.startStep(run, stepCode);
    }

    @Override
    public void chatInCurrentStep(Long runId, NovelWorkflowChatRequest request)
    {
        if (request == null || StringUtils.isEmpty(request.getMessage()))
        {
            throw new ServiceException("消息不能为空");
        }
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        if (run == null)
        {
            throw new ServiceException("工作流不存在");
        }
        if (!NovelWorkflowRunStatus.WAITING_CONFIRM.getCode().equals(run.getStatus())
            && !NovelWorkflowRunStatus.PAUSED.getCode().equals(run.getStatus()))
        {
            throw new ServiceException("当前状态不可对话，请等待 Agent 完成或恢复工作流");
        }
        NovelWorkflowStepCode stepCode = NovelWorkflowStepCode.fromCode(run.getCurrentStep());
        if (stepCode == null || !isInteractiveStep(stepCode))
        {
            throw new ServiceException("当前步骤不支持对话");
        }
        NovelWorkflowStep step = novelWorkflowStepMapper.selectLatestStepByRunIdAndCode(runId, stepCode.getCode());
        if (step == null || step.getAgentSessionId() == null)
        {
            throw new ServiceException("当前步骤会话不存在，请重跑步骤");
        }
        run.setStatus(NovelWorkflowRunStatus.RUNNING.getCode());
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);
        workflowEventPublisher.publish(runId, step.getStepId(), NovelWorkflowEventType.RUN_STATUS.getCode(),
            java.util.Collections.singletonMap("status", NovelWorkflowRunStatus.RUNNING.getCode()));
        novelAiSessionService.appendMessage(step.getAgentSessionId(), "user", request.getMessage().trim());
        novelWorkflowStepRunner.chatAsync(runId, step.getStepId());
    }

    private boolean isInteractiveStep(NovelWorkflowStepCode stepCode)
    {
        return stepCode == NovelWorkflowStepCode.INIT_PROJECT
            || stepCode == NovelWorkflowStepCode.WORLD_BUILDING
            || stepCode == NovelWorkflowStepCode.CHARACTER_DESIGN
            || stepCode == NovelWorkflowStepCode.PLOT_OUTLINE
            || stepCode == NovelWorkflowStepCode.CHAPTER_PLANNING;
    }
}
