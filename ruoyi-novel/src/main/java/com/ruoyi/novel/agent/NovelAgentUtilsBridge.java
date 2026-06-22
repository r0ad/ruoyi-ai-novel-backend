package com.ruoyi.novel.agent;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

/**
 * 集成 spring-ai-agent-utils 的 TodoWrite 等扩展工具
 */
@Component
public class NovelAgentUtilsBridge
{
    @Autowired(required = false)
    @Qualifier("novelAgentUtilToolCallbacks")
    private ToolCallback[] agentUtilToolCallbacks;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    public ToolCallback[] getExtraToolCallbacks()
    {
        return agentUtilToolCallbacks != null ? agentUtilToolCallbacks : new ToolCallback[0];
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
