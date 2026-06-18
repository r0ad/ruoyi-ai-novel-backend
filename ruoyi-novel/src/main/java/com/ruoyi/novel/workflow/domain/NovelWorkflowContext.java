package com.ruoyi.novel.workflow.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作流运行上下文（序列化为 context_json）
 */
public class NovelWorkflowContext
{
    private int currentChapterIndex;
    private List<Long> chapterIds = new ArrayList<Long>();
    private int targetChapterCount = 3;
    private Long lastReviewChapterId;

    public int getCurrentChapterIndex()
    {
        return currentChapterIndex;
    }

    public void setCurrentChapterIndex(int currentChapterIndex)
    {
        this.currentChapterIndex = currentChapterIndex;
    }

    public List<Long> getChapterIds()
    {
        return chapterIds;
    }

    public void setChapterIds(List<Long> chapterIds)
    {
        this.chapterIds = chapterIds;
    }

    public int getTargetChapterCount()
    {
        return targetChapterCount;
    }

    public void setTargetChapterCount(int targetChapterCount)
    {
        this.targetChapterCount = targetChapterCount;
    }

    public Long getLastReviewChapterId()
    {
        return lastReviewChapterId;
    }

    public void setLastReviewChapterId(Long lastReviewChapterId)
    {
        this.lastReviewChapterId = lastReviewChapterId;
    }
}
