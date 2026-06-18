package com.ruoyi.novel.agent.tools;

import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.agent.NovelToolContext;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.workflow.enums.NovelWorkflowEventType;

@Component
public class ChapterTools
{
    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @Tool(description = "创建新章节")
    public String createChapter(
        @ToolParam(description = "章节序号") int chapterNumber,
        @ToolParam(description = "章节标题") String title,
        @ToolParam(description = "章节摘要") String summary)
    {
        NovelToolContext.requireWriteAllowed();
        Long projectId = NovelToolContext.getProjectId();
        NovelChapter chapter = new NovelChapter();
        chapter.setProjectId(projectId);
        chapter.setChapterNumber(chapterNumber);
        chapter.setTitle(title);
        chapter.setSummary(summary);
        chapter.setContent("");
        chapter.setParentId(0L);
        novelChapterService.insertNovelChapter(chapter);
        publishTool("createChapter", title);
        return "章节已创建，ID=" + chapter.getChapterId() + "，标题=" + title;
    }

    @Tool(description = "保存章节正文与摘要")
    public String saveChapter(
        @ToolParam(description = "章节ID") long chapterId,
        @ToolParam(description = "正文 Markdown") String content,
        @ToolParam(description = "摘要，可选") String summary)
    {
        NovelToolContext.requireWriteAllowed();
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
        if (chapter == null || !chapter.getProjectId().equals(NovelToolContext.getProjectId()))
        {
            return "章节不存在或无权访问";
        }
        if (StringUtils.isNotEmpty(content))
        {
            chapter.setContent(content);
        }
        if (summary != null)
        {
            chapter.setSummary(summary);
        }
        novelChapterService.updateNovelChapter(chapter);
        publishTool("saveChapter", chapterId);
        return "章节已保存，ID=" + chapterId;
    }

    @Tool(description = "列出项目所有章节")
    public String listChapters()
    {
        Long projectId = NovelToolContext.getProjectId();
        NovelChapter query = new NovelChapter();
        query.setProjectId(projectId);
        List<NovelChapter> chapters = novelChapterService.selectNovelChapterList(query);
        publishTool("listChapters", chapters.size());
        return JSON.toJSONString(chapters);
    }

    @Tool(description = "获取章节详情含正文")
    public String getChapter(@ToolParam(description = "章节ID") long chapterId)
    {
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
        if (chapter == null || !chapter.getProjectId().equals(NovelToolContext.getProjectId()))
        {
            return "章节不存在";
        }
        publishTool("getChapter", chapterId);
        return JSON.toJSONString(chapter);
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
