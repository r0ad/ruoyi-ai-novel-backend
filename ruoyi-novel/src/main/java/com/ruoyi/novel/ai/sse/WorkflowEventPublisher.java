package com.ruoyi.novel.ai.sse;

import java.io.IOException;
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
 * ?????? SSE ???????
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
        catch (IOException ex)
        {
            log.debug("SSE initial send failed for run {}", runId, ex);
            removeEmitter(runId, emitter);
            emitter.completeWithError(ex);
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
        for (SseEmitter emitter : list)
        {
            try
            {
                emitter.send(SseEmitter.event().name("workflow").data(JSON.toJSONString(event)));
            }
            catch (IOException ex)
            {
                log.debug("SSE send failed for run {}", runId, ex);
                removeEmitter(runId, emitter);
            }
        }
    }

    private void removeEmitter(Long runId, SseEmitter emitter)
    {
        List<SseEmitter> list = emitters.get(runId);
        if (list != null)
        {
            list.remove(emitter);
        }
    }
}
