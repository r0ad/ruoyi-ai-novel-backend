package com.ruoyi.novel.agent;

import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.novel.agent.capability.NovelAgentCapabilityRegistry;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class NovelAgentUtilsBridge
{
    @Autowired
    private NovelAgentCapabilityRegistry capabilityRegistry;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    public ToolCallback[] getExtraToolCallbacks()
    {
        List<String> names = new ArrayList<String>();
        names.add(NovelAgentCapabilityRegistry.TODO_WRITE);
        names.add(NovelAgentCapabilityRegistry.ASK_USER);
        names.add(NovelAgentCapabilityRegistry.SKILLS);
        List<ToolCallback> callbacks = capabilityRegistry.listAvailableToolCallbacks(names);
        return callbacks.toArray(new ToolCallback[callbacks.size()]);
    }

    public void publishTodoUpdate(String todosJson)
    {
        NovelToolContext.Context ctx = NovelToolContext.get();
        if (ctx != null)
        {
            workflowEventPublisher.publish(ctx.runId, ctx.stepId,
                NovelWorkflowEventType.TODO_UPDATE.getCode(),
                com.alibaba.fastjson2.JSON.parseObject("{\"todos\":" + todosJson + "}"));
        }
    }
}
