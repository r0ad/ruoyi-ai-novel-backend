package com.ruoyi.novel.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.ai.service.INovelAiService;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class ReviewTools
{
    @Autowired
    private INovelAiService novelAiService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Tool(description = "对指定章节进行一致性审查，返回结构化问题列表")
    public String reviewChapterConsistency(@ToolParam(description = "章节ID") long chapterId)
    {
        Long projectId = NovelToolContext.getProjectId();
        String report = novelAiService.reviewChapter(projectId, chapterId);
        publishTool("reviewChapterConsistency", chapterId);
        return report;
    }

    @Tool(description = "全书一致性审查")
    public String reviewFullNovel()
    {
        Long projectId = NovelToolContext.getProjectId();
        String report = novelAiService.reviewProject(projectId);
        publishTool("reviewFullNovel", projectId);
        return report;
    }

    @Tool(description = "从章节正文抽取 Meta 实体与关系")
    public String extractMetaFromChapter(@ToolParam(description = "章节ID") long chapterId)
    {
        NovelToolContext.requireWriteAllowed();
        Long projectId = NovelToolContext.getProjectId();
        String result = novelAiService.extractMeta(projectId, chapterId);
        publishTool("extractMetaFromChapter", chapterId);
        return result;
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
