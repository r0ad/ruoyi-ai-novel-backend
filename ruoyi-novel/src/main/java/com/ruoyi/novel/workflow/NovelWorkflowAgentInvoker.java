package com.ruoyi.novel.workflow;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.agent.prompts.NovelPromptTemplateService;
import com.ruoyi.novel.ai.session.domain.NovelAiMessage;
import com.ruoyi.novel.ai.session.service.INovelAiSessionService;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;

/**
 * 工作流 Agent 调用：优先 SSE 流式输出 token；流式失败或为空时回退 sync call 完成 tool loop。
 */
@Component
public class NovelWorkflowAgentInvoker
{
    private static final Logger log = LoggerFactory.getLogger(NovelWorkflowAgentInvoker.class);

    @Autowired
    private NovelPromptTemplateService novelPromptTemplateService;

    @Autowired
    private INovelAiSessionService novelAiSessionService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    public String invoke(Long runId, Long stepId, NovelWorkflowRun run, NovelWorkflowStepCode stepCode,
        ChatClient client, Long sessionId)
    {
        String system = novelPromptTemplateService.buildSystemPrompt(run, stepCode);
        List<Message> messages = buildConversationMessages(system, sessionId);
        log.info("Agent invoke start runId={} stepId={} step={} messageCount={}", runId, stepId,
            stepCode.getCode(), messages.size());
        String response = invokeWithStreaming(runId, stepId, client, messages);
        log.info("Agent invoke done runId={} stepId={} responseLen={}", runId, stepId,
            response != null ? response.length() : 0);
        return response;
    }

    private List<Message> buildConversationMessages(String system, Long sessionId)
    {
        List<Message> messages = new ArrayList<Message>();
        messages.add(new SystemMessage(system));
        if (sessionId != null)
        {
            for (NovelAiMessage item : novelAiSessionService.listMessages(sessionId))
            {
                if (StringUtils.isEmpty(item.getContent()))
                {
                    continue;
                }
                if ("assistant".equalsIgnoreCase(item.getRole()))
                {
                    messages.add(new AssistantMessage(item.getContent()));
                }
                else
                {
                    messages.add(new UserMessage(item.getContent()));
                }
            }
        }
        return messages;
    }

    private String invokeWithStreaming(Long runId, Long stepId, ChatClient client, List<Message> messages)
    {
        StringBuilder responseBuilder = new StringBuilder();
        Exception streamError = null;
        try
        {
            streamToBuilder(runId, stepId, client, messages, responseBuilder);
        }
        catch (Exception ex)
        {
            streamError = ex;
            log.warn("Agent stream failed runId={} stepId={} partialLen={}", runId, stepId,
                responseBuilder.length(), ex);
        }

        String streamResult = responseBuilder.toString();
        if (StringUtils.isNotEmpty(streamResult) && streamError == null)
        {
            return streamResult;
        }

        return invokeSyncFallback(runId, stepId, client, messages, streamResult, streamError);
    }

    private void streamToBuilder(Long runId, Long stepId, ChatClient client, List<Message> messages,
        StringBuilder responseBuilder)
    {
        client.prompt().messages(messages).stream().content()
            .doOnNext(chunk -> appendToken(runId, stepId, responseBuilder, chunk))
            .blockLast();
    }

    private String invokeSyncFallback(Long runId, Long stepId, ChatClient client, List<Message> messages,
        String streamResult, Exception streamError)
    {
        try
        {
            String syncResponse = client.prompt().messages(messages).call().content();
            if (StringUtils.isNotEmpty(syncResponse))
            {
                if (syncResponse.length() > streamResult.length())
                {
                    publishDelta(runId, stepId, streamResult, syncResponse);
                }
                else if (StringUtils.isEmpty(streamResult))
                {
                    appendToken(runId, stepId, null, syncResponse);
                }
                return syncResponse;
            }
        }
        catch (Exception callEx)
        {
            log.warn("Agent sync fallback failed runId={} stepId={}", runId, stepId, callEx);
            if (StringUtils.isNotEmpty(streamResult))
            {
                return streamResult;
            }
            throw new ServiceException("Agent 执行失败：" + rootMessage(callEx));
        }

        if (StringUtils.isNotEmpty(streamResult))
        {
            return streamResult;
        }
        if (streamError != null)
        {
            throw new ServiceException("Agent 执行失败：" + rootMessage(streamError));
        }
        throw new ServiceException("Agent 未返回有效内容，请重试或继续对话");
    }

    private void publishDelta(Long runId, Long stepId, String alreadySent, String fullText)
    {
        if (fullText.length() <= alreadySent.length())
        {
            return;
        }
        appendToken(runId, stepId, null, fullText.substring(alreadySent.length()));
    }

    private void appendToken(Long runId, Long stepId, StringBuilder responseBuilder, String chunk)
    {
        if (StringUtils.isEmpty(chunk))
        {
            return;
        }
        if (responseBuilder != null)
        {
            responseBuilder.append(chunk);
        }
        workflowEventPublisher.publishTransient(runId, stepId, NovelWorkflowEventType.TOKEN.getCode(),
            java.util.Collections.singletonMap("text", chunk));
    }

    private String rootMessage(Throwable ex)
    {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root)
        {
            root = root.getCause();
        }
        return StringUtils.isNotEmpty(root.getMessage()) ? root.getMessage() : ex.getMessage();
    }
}
