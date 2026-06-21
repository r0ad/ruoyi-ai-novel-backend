package com.ruoyi.novel.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.ai.capability.INovelAiCapabilityService;
import com.ruoyi.novel.ai.context.ContextOptions;
import com.ruoyi.novel.ai.domain.dto.ExtractResult;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class ReviewTools
{
    @Autowired
    private INovelAiCapabilityService novelAiCapabilityService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Tool(description = "对指定章节进行一致性审查，返回结构化 JSON 问题列表")
    public String reviewChapterConsistency(@ToolParam(description = "章节ID") long chapterId)
    {
        Long projectId = NovelToolContext.getProjectId();
        ContextOptions options = new ContextOptions();
        options.setProjectId(projectId);
        options.setChapterId(chapterId);
        ReviewResult report = novelAiCapabilityService.reviewChapter(options);
        publishTool("reviewChapterConsistency", chapterId);
        return JSON.toJSONString(report);
    }

    @Tool(description = "全书一致性审查，返回结构化 JSON")
    public String reviewFullNovel()
    {
        Long projectId = NovelToolContext.getProjectId();
        ContextOptions options = new ContextOptions();
        options.setProjectId(projectId);
        ReviewResult report = novelAiCapabilityService.reviewProject(options);
        publishTool("reviewFullNovel", projectId);
        return JSON.toJSONString(report);
    }

    @Tool(description = "从章节正文抽取 Meta 实体与关系（预览 JSON，不直接写库）")
    public String extractMetaFromChapter(@ToolParam(description = "章节ID") long chapterId)
    {
        Long projectId = NovelToolContext.getProjectId();
        ContextOptions options = new ContextOptions();
        options.setProjectId(projectId);
        options.setChapterId(chapterId);
        ExtractResult result = novelAiCapabilityService.extractMetaPreview(options);
        publishTool("extractMetaFromChapter", chapterId);
        return JSON.toJSONString(result);
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
