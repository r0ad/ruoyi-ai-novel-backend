package com.ruoyi.novel.ai.sse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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

    private static final long HEARTBEAT_INTERVAL_SECONDS = 15L;

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<Long, List<SseEmitter>>();

    private ScheduledExecutorService heartbeatExecutor;

    @Autowired
    private NovelWorkflowEventMapper novelWorkflowEventMapper;

    @PostConstruct
    public void init()
    {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "workflow-sse-heartbeat");
            t.setDaemon(true);
            return t;
        });
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeats,
            HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy()
    {
        if (heartbeatExecutor != null)
        {
            heartbeatExecutor.shutdownNow();
        }
    }

    public SseEmitter subscribe(Long runId, Long lastEventId)
    {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.computeIfAbsent(runId, k -> new CopyOnWriteArrayList<SseEmitter>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(runId, emitter));
        emitter.onTimeout(() -> removeEmitter(runId, emitter));
        emitter.onError(e -> removeEmitter(runId, emitter));
        CompletableFuture.runAsync(() -> sendInitialEvents(runId, emitter, lastEventId));
        return emitter;
    }

    private void sendInitialEvents(Long runId, SseEmitter emitter, Long lastEventId)
    {
        try
        {
            emitter.send(SseEmitter.event().comment("connected"));
            // 仅重放 lastEventId 之后的结构化事件（token 为瞬时事件，不参与重放）
            List<NovelWorkflowEvent> history =
                novelWorkflowEventMapper.selectReplayEventsByRunIdAfterId(runId, lastEventId);
            for (NovelWorkflowEvent event : history)
            {
                emitter.send(toSseEvent(event));
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

    /**
     * 持久化并广播结构化事件
     */
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

    /**
     * 仅广播、不落库的瞬时事件（如流式 token），避免高频写库与重连全量重放
     */
    public void publishTransient(Long runId, Long stepId, String eventType, Object payload)
    {
        NovelWorkflowEvent event = new NovelWorkflowEvent();
        event.setRunId(runId);
        event.setStepId(stepId);
        event.setEventType(eventType);
        event.setPayloadJson(payload == null ? null : JSON.toJSONString(payload));
        broadcast(runId, event);
    }

    private void broadcast(Long runId, NovelWorkflowEvent event)
    {
        List<SseEmitter> list = emitters.get(runId);
        if (list == null || list.isEmpty())
        {
            return;
        }
        SseEmitter.SseEventBuilder builder = toSseEvent(event);
        for (SseEmitter emitter : list)
        {
            safeSend(runId, emitter, builder);
        }
    }

    private SseEmitter.SseEventBuilder toSseEvent(NovelWorkflowEvent event)
    {
        SseEmitter.SseEventBuilder builder = SseEmitter.event()
            .name("workflow")
            .data(JSON.toJSONString(event));
        // 仅持久化事件带 id，供前端记录 Last-Event-ID 做增量重连
        if (event.getEventId() != null)
        {
            builder.id(String.valueOf(event.getEventId()));
        }
        return builder;
    }

    private void sendHeartbeats()
    {
        if (emitters.isEmpty())
        {
            return;
        }
        for (Map.Entry<Long, List<SseEmitter>> entry : emitters.entrySet())
        {
            Long runId = entry.getKey();
            for (SseEmitter emitter : entry.getValue())
            {
                try
                {
                    emitter.send(SseEmitter.event().comment("ping"));
                }
                catch (Exception ex)
                {
                    removeEmitter(runId, emitter);
                    completeQuietly(emitter);
                }
            }
        }
    }

    private void safeSend(Long runId, SseEmitter emitter, SseEmitter.SseEventBuilder builder)
    {
        try
        {
            emitter.send(builder);
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
