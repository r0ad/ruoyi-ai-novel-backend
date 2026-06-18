package com.ruoyi.novel.rag.service;

/**
 * 写作上下文服务
 */
public interface INovelContextService
{
    String buildWritingContext(Long projectId, int chapterNumber, int rangeBefore);

    void indexProjectContent(Long projectId);
}
