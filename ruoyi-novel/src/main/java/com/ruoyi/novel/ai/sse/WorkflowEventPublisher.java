package com.ruoyi.novel.ai.sse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.novel.workflow.domain.NovelWorkflowEvent;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowEventMapper;

/**
 * 工作流 SSE 事件发布
 */
@Component
public class WorkflowEventPublisher
{
    private static final Logger log = LoggerFactory.getLogger(WorkflowEventPublisher.class);

    private static final long SSE_TIMEOUT = 30L * 60L * 1000L;

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<Long, List<SseEmitter>>();

    @Autowired
    private NovelWorkflowEventMapper novelWorkflowEventMapper;

    public SseEmitter subscribe(Long runId)
    {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.computeIfAbsent(runId, k -> new CopyOnWriteArrayList<SseEmitter>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(runId, emitter));
        emitter.onTimeout(() -> removeEmitter(runId, emitter));
        emitter.onError(e -> removeEmitter(runId, emitter));
        CompletableFuture.runAsync(() -> sendInitialEvents(runId, emitter));
        return emitter;
    }

    private void sendInitialEvents(Long runId, SseEmitter emitter)
    {
        try
        {
            emitter.send(SseEmitter.event().comment("connected"));
            List<NovelWorkflowEvent> history = novelWorkflowEventMapper.selectEventsByRunIdAfterId(runId, null);
            for (NovelWorkflowEvent event : history)
            {
                emitter.send(SseEmitter.event().name("workflow").data(JSON.toJSONString(event)));
            }
        }
        catch (Exception ex)
        {
            if (isClientDisconnect(ex))
            {
                log.debug("SSE initial send failed, client disconnected for run {}", runId);
            }
            else
            {
                log.debug("SSE initial send failed for run {}", runId, ex);
            }
            removeEmitter(runId, emitter);
            completeQuietly(emitter);
        }
    }

    public NovelWorkflowEvent publish(Long runId, Long stepId, String eventType, Object payload)
    {
        NovelWorkflowEvent event = new NovelWorkflowEvent();
        event.setRunId(runId);
        event.setStepId(stepId);
        event.setEventType(eventType);
        event.setPayloadJson(payload == null ? null : JSON.toJSONString(payload));
        novelWorkflowEventMapper.insertNovelWorkflowEvent(event);
        broadcast(runId, event);
        return event;
    }

    private void broadcast(Long runId, NovelWorkflowEvent event)
    {
        List<SseEmitter> list = emitters.get(runId);
        if (list == null || list.isEmpty())
        {
            return;
        }
        String payload = JSON.toJSONString(event);
        for (SseEmitter emitter : list)
        {
            safeSend(runId, emitter, payload);
        }
    }

    private void safeSend(Long runId, SseEmitter emitter, String payload)
    {
        try
        {
            emitter.send(SseEmitter.event().name("workflow").data(payload));
        }
        catch (Exception ex)
        {
            if (isClientDisconnect(ex))
            {
                log.debug("SSE client disconnected for run {}", runId);
            }
            else
            {
                log.warn("SSE send failed for run {}", runId, ex);
            }
            removeEmitter(runId, emitter);
            completeQuietly(emitter);
        }
    }

    private void completeQuietly(SseEmitter emitter)
    {
        try
        {
            emitter.complete();
        }
        catch (Exception ignored)
        {
        }
    }

    private boolean isClientDisconnect(Throwable ex)
    {
        while (ex != null)
        {
            String className = ex.getClass().getName();
            if (className.contains("ClientAbortException")
                || className.contains("AsyncRequestNotUsableException")
                || className.contains("ClosedChannelException"))
            {
                return true;
            }
            String message = ex.getMessage();
            if (message != null && (message.contains("中止了一个已建立的连接")
                || message.contains("Connection reset")
                || message.contains("Broken pipe")
                || message.contains("disconnected client")))
            {
                return true;
            }
            ex = ex.getCause();
        }
        return false;
    }

    private void removeEmitter(Long runId, SseEmitter emitter)
    {
        List<SseEmitter> list = emitters.get(runId);
        if (list != null)
        {
            list.remove(emitter);
            if (list.isEmpty())
            {
                emitters.remove(runId);
            }
        }
    }
}
