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
import com.ruoyi.novel.ai.session.domain.NovelAiSession;
import com.ruoyi.novel.ai.session.service.INovelAiSessionService;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStep;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;
import com.ruoyi.novel.workflow.enums.NovelWorkflowRunStatus;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepStatus;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowRunMapper;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowStepMapper;
import reactor.core.publisher.Flux;

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
        NovelToolContext.set(runId, run.getProjectId(), stepId, true, run.getCreateBy());
        NovelToolContext.Context toolContext = NovelToolContext.get();
        try
        {
            NovelAiSession session = novelAiSessionService.createSession(
                run.getProjectId(), run.getUserId(), "workflow_" + stepCode.getCode(),
                stepCode.getLabel(), null);
            step.setAgentSessionId(session.getSessionId());
            novelWorkflowStepMapper.updateNovelWorkflowStep(step);

            ChatClient client = novelAgentFactory.createForStep(stepCode, toolContext);
            String system = novelPromptTemplateService.buildSystemPrompt(run, stepCode);
            String user = buildStepUserPrompt(run, stepCode);
            novelAiSessionService.appendMessage(session.getSessionId(), "user", user);

            String response = invokeAgent(runId, stepId, client, system, user);
            if (StringUtils.isEmpty(response))
            {
                response = "（步骤已完成，无文本输出）";
            }
            novelAiSessionService.appendMessage(session.getSessionId(), "assistant", response);

            step.setOutputSnapshot(response);
            step.setStatus(NovelWorkflowStepStatus.WAITING_CONFIRM.getCode());
            step.setFinishedAt(new Date());
            novelWorkflowStepMapper.updateNovelWorkflowStep(step);

            run.setStatus(NovelWorkflowRunStatus.WAITING_CONFIRM.getCode());
            novelWorkflowRunMapper.updateNovelWorkflowRun(run);

            java.util.Map<String, Object> completedPayload = new java.util.HashMap<String, Object>();
            completedPayload.put("stepCode", stepCode.getCode());
            completedPayload.put("output", response);
            workflowEventPublisher.publish(runId, stepId, NovelWorkflowEventType.STEP_COMPLETED.getCode(),
                completedPayload);
            workflowEventPublisher.publish(runId, stepId, NovelWorkflowEventType.RUN_STATUS.getCode(),
                java.util.Collections.singletonMap("status", NovelWorkflowRunStatus.WAITING_CONFIRM.getCode()));
        }
        catch (Exception ex)
        {
            log.error("Workflow step failed runId={} step={}", runId, stepCode, ex);
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
        finally
        {
            NovelToolContext.clear();
        }
    }

    private String invokeAgent(Long runId, Long stepId, ChatClient client, String system, String user)
    {
        StringBuilder responseBuilder = new StringBuilder();
        try
        {
            Flux<String> flux = client.prompt().system(system).user(user).stream().content();
            flux.doOnNext(chunk -> appendToken(runId, stepId, responseBuilder, chunk)).blockLast();
            if (responseBuilder.length() > 0)
            {
                return responseBuilder.toString();
            }
        }
        catch (Exception streamEx)
        {
            log.debug("Agent stream unavailable runId={} stepId={}", runId, stepId, streamEx);
        }
        String response = client.prompt().system(system).user(user).call().content();
        if (StringUtils.isNotEmpty(response) && responseBuilder.length() == 0)
        {
            appendToken(runId, stepId, responseBuilder, response);
        }
        return StringUtils.isNotEmpty(response) ? response : responseBuilder.toString();
    }

    private void appendToken(Long runId, Long stepId, StringBuilder responseBuilder, String chunk)
    {
        if (StringUtils.isEmpty(chunk))
        {
            return;
        }
        responseBuilder.append(chunk);
        workflowEventPublisher.publish(runId, stepId, NovelWorkflowEventType.TOKEN.getCode(),
            java.util.Collections.singletonMap("text", chunk));
    }

    private String buildStepUserPrompt(NovelWorkflowRun run, NovelWorkflowStepCode stepCode)
    {
        String base = novelPromptTemplateService.buildUserPrompt(run, stepCode);
        com.alibaba.fastjson2.JSONObject ctx = JSON.parseObject(run.getContextJson());
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