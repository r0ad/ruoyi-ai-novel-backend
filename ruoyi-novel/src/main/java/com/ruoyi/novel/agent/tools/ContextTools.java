package com.ruoyi.novel.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.rag.service.INovelContextService;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class ContextTools
{
    @Autowired
    private INovelContextService novelContextService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Tool(description = "获取写作上下文，包含相关设定、前文摘要与 RAG 检索结果")
    public String getWritingContext(
        @ToolParam(description = "章节序号") int chapterNumber,
        @ToolParam(description = "向前获取章节数，默认1") Integer rangeBefore)
    {
        Long projectId = NovelToolContext.getProjectId();
        int before = rangeBefore != null ? rangeBefore : 1;
        String context = novelContextService.buildWritingContext(projectId, chapterNumber, before);
        publishTool("getWritingContext", chapterNumber);
        return context;
    }

    private void publishTool(String tool, Object detail)
    {
        NovelToolContext.Context ctx = NovelToolContext.get();
        if (ctx != null)
        {
            workflowEventPublisher.publish(ctx.runId, ctx.stepId,
                NovelWorkflowEventType.TOOL_CALL.getCode(),
                JSON.parseObject("{\"tool\":\"" + tool + "\",\"detail\":" + JSON.toJSONString(detail) + "}"));
        }
    }
}
