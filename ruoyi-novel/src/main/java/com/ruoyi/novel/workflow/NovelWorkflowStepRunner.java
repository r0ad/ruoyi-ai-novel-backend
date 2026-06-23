package com.ruoyi.novel.workflow;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.agent.NovelAgentFactory;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.agent.prompts.NovelPromptTemplateService;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.mapper.NovelProjectMapper;
import com.ruoyi.novel.ai.session.domain.NovelAiSession;
import com.ruoyi.novel.ai.session.service.INovelAiSessionService;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStep;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStepReadiness;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;
import com.ruoyi.novel.workflow.enums.NovelWorkflowRunStatus;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepStatus;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowRunMapper;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowStepMapper;

@Component
public class NovelWorkflowStepRunner
{
    private static final Logger log = LoggerFactory.getLogger(NovelWorkflowStepRunner.class);

    @Autowired
    private NovelAgentFactory novelAgentFactory;

    @Autowired
    private NovelPromptTemplateService novelPromptTemplateService;

    @Autowired
    private NovelWorkflowRunMapper novelWorkflowRunMapper;

    @Autowired
    private NovelWorkflowStepMapper novelWorkflowStepMapper;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Autowired
    private INovelAiSessionService novelAiSessionService;

    @Autowired
    private NovelWorkflowAgentInvoker novelWorkflowAgentInvoker;

    @Autowired
    private NovelWorkflowStepValidator novelWorkflowStepValidator;

    @Autowired
    private NovelProjectMapper novelProjectMapper;

    public void executeAsync(Long runId, Long stepId, NovelWorkflowStepCode stepCode)
    {
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override
                public void afterCommit()
                {
                    runStepAsync(runId, stepId, stepCode);
                }
            });
        }
        else
        {
            runStepAsync(runId, stepId, stepCode);
        }
    }

    public void chatAsync(Long runId, Long stepId)
    {
        CompletableFuture.runAsync(() -> executeChat(runId, stepId));
    }

    private void runStepAsync(Long runId, Long stepId, NovelWorkflowStepCode stepCode)
    {
        CompletableFuture.runAsync(() -> executeStep(runId, stepId, stepCode));
    }

    private void executeStep(Long runId, Long stepId, NovelWorkflowStepCode stepCode)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        NovelWorkflowStep step = novelWorkflowStepMapper.selectNovelWorkflowStepByStepId(stepId);
        if (run == null || step == null)
        {
            log.warn("Workflow step skipped, run or step not found runId={} stepId={}", runId, stepId);
            return;
        }
        workflowEventPublisher.publish(runId, stepId, NovelWorkflowEventType.STEP_STARTED.getCode(),
            java.util.Collections.singletonMap("stepCode", stepCode.getCode()));
        NovelToolContext.set(runId, run.getProjectId(), stepId, true, run.getCreateBy(), resolveRunUserId(run));
        NovelToolContext.Context toolContext = NovelToolContext.get();
        try
        {
            NovelAiSession session = novelAiSessionService.createSession(
                run.getProjectId(), run.getUserId(), "workflow_" + stepCode.getCode(),
                stepCode.getLabel(), null);
            step.setAgentSessionId(session.getSessionId());
            novelWorkflowStepMapper.updateNovelWorkflowStep(step);

            String user = buildStepUserPrompt(run, stepCode);
            novelAiSessionService.appendMessage(session.getSessionId(), "user", user);

            ChatClient client = novelAgentFactory.createForStep(stepCode, toolContext);
            String response = novelWorkflowAgentInvoker.invoke(runId, stepId, run, stepCode, client,
                session.getSessionId());
            finishStepInteraction(run, step, stepCode, session.getSessionId(), response);
        }
        catch (Exception ex)
        {
            failStep(run, step, runId, stepId, ex);
        }
        finally
        {
            NovelToolContext.clear();
        }
    }

    private void executeChat(Long runId, Long stepId)
    {
        NovelWorkflowRun run = novelWorkflowRunMapper.selectNovelWorkflowRunByRunId(runId);
        NovelWorkflowStep step = novelWorkflowStepMapper.selectNovelWorkflowStepByStepId(stepId);
        if (run == null || step == null || step.getAgentSessionId() == null)
        {
            log.warn("Workflow chat skipped runId={} stepId={}", runId, stepId);
            return;
        }
        NovelWorkflowStepCode stepCode = NovelWorkflowStepCode.fromCode(run.getCurrentStep());
        if (stepCode == null)
        {
            return;
        }
        NovelToolContext.set(runId, run.getProjectId(), stepId, true, run.getCreateBy(), resolveRunUserId(run));
        NovelToolContext.Context toolContext = NovelToolContext.get();
        try
        {
            run.setStatus(NovelWorkflowRunStatus.RUNNING.getCode());
            novelWorkflowRunMapper.updateNovelWorkflowRun(run);
            workflowEventPublisher.publish(runId, stepId, NovelWorkflowEventType.RUN_STATUS.getCode(),
                java.util.Collections.singletonMap("status", NovelWorkflowRunStatus.RUNNING.getCode()));

            ChatClient client = novelAgentFactory.createForStep(stepCode, toolContext);
            String response = novelWorkflowAgentInvoker.invoke(runId, stepId, run, stepCode, client,
                step.getAgentSessionId());
            finishStepInteraction(run, step, stepCode, step.getAgentSessionId(), response);
        }
        catch (Exception ex)
        {
            failStep(run, step, runId, stepId, ex);
        }
        finally
        {
            NovelToolContext.clear();
        }
    }

    private void finishStepInteraction(NovelWorkflowRun run, NovelWorkflowStep step, NovelWorkflowStepCode stepCode,
        Long sessionId, String response)
    {
        if (StringUtils.isEmpty(response))
        {
            response = "（Agent 未返回文本，请继续对话或重试）";
        }
        novelAiSessionService.appendMessage(sessionId, "assistant", response);

        NovelWorkflowStepReadiness readiness = novelWorkflowStepValidator.evaluate(run.getProjectId(), stepCode);
        String snapshot = response;
        if (!readiness.isReady() && StringUtils.isNotEmpty(readiness.getHint())
            && !NovelWorkflowStepValidator.isInteractiveStep(stepCode))
        {
            snapshot = response + "\n\n---\n**待完成**：" + readiness.getHint();
        }

        step.setOutputSnapshot(snapshot);
        step.setStatus(NovelWorkflowStepStatus.WAITING_CONFIRM.getCode());
        step.setFinishedAt(new Date());
        step.setErrorMessage(null);
        novelWorkflowStepMapper.updateNovelWorkflowStep(step);

        run.setStatus(NovelWorkflowRunStatus.WAITING_CONFIRM.getCode());
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);

        java.util.Map<String, Object> completedPayload = new java.util.HashMap<String, Object>();
        completedPayload.put("stepCode", stepCode.getCode());
        completedPayload.put("output", snapshot);
        completedPayload.put("ready", readiness.isReady());
        workflowEventPublisher.publish(run.getRunId(), step.getStepId(), NovelWorkflowEventType.STEP_COMPLETED.getCode(),
            completedPayload);
        workflowEventPublisher.publish(run.getRunId(), step.getStepId(), NovelWorkflowEventType.RUN_STATUS.getCode(),
            java.util.Collections.singletonMap("status", NovelWorkflowRunStatus.WAITING_CONFIRM.getCode()));
    }

    private void failStep(NovelWorkflowRun run, NovelWorkflowStep step, Long runId, Long stepId, Exception ex)
    {
        log.error("Workflow step failed runId={} step={}", runId, run.getCurrentStep(), ex);
        step.setStatus(NovelWorkflowStepStatus.FAILED.getCode());
        step.setErrorMessage(ex.getMessage());
        step.setFinishedAt(new Date());
        novelWorkflowStepMapper.updateNovelWorkflowStep(step);
        run.setStatus(NovelWorkflowRunStatus.FAILED.getCode());
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);
        workflowEventPublisher.publish(runId, stepId, NovelWorkflowEventType.STEP_FAILED.getCode(),
            java.util.Collections.singletonMap("error", ex.getMessage()));
        workflowEventPublisher.publish(runId, stepId, NovelWorkflowEventType.RUN_STATUS.getCode(),
            java.util.Collections.singletonMap("status", NovelWorkflowRunStatus.FAILED.getCode()));
    }

    /**
     * 解析工作流归属用户ID。异步线程无法读取安全上下文，
     * 优先用 run 上记录的 userId，缺失时回退到项目归属用户，确保历史 run 也能正常执行。
     */
    private Long resolveRunUserId(NovelWorkflowRun run)
    {
        if (run.getUserId() != null)
        {
            return run.getUserId();
        }
        if (run.getProjectId() == null)
        {
            return null;
        }
        NovelProject project = novelProjectMapper.selectNovelProjectByProjectId(run.getProjectId());
        return project != null ? project.getUserId() : null;
    }

    private String buildStepUserPrompt(NovelWorkflowRun run, NovelWorkflowStepCode stepCode)
    {
        String base = novelPromptTemplateService.buildUserPrompt(run, stepCode);
        JSONObject ctx = JSON.parseObject(run.getContextJson());
        if (ctx == null)
        {
            return base;
        }
        if (stepCode == NovelWorkflowStepCode.WRITE_CHAPTER || stepCode == NovelWorkflowStepCode.REVIEW_CHAPTER)
        {
            int idx = ctx.getIntValue("currentChapterIndex");
            base += "\n当前章节索引：" + idx;
            com.alibaba.fastjson2.JSONArray ids = ctx.getJSONArray("chapterIds");
            if (ids != null && idx >= 0 && idx < ids.size())
            {
                base += "\n【待审查章节ID】" + ids.getLong(idx);
            }
        }
        if (stepCode == NovelWorkflowStepCode.REVIEW_CHAPTER && ctx.getIntValue("reviewFixRound") > 0)
        {
            base += "\n【修复轮次】" + ctx.getIntValue("reviewFixRound") + "/" + 3;
            if (StringUtils.isNotEmpty(ctx.getString("lastReviewResultJson")))
            {
                base += "\n【上一审查结果】\n" + ctx.getString("lastReviewResultJson");
            }
            base += "\n请根据 Critical/Major 问题调用工具修复后再次 reviewChapterConsistency 验证。";
        }
        return base;
    }
}
