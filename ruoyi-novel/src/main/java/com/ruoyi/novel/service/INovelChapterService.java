package com.ruoyi.novel.service;

import java.util.List;
import com.ruoyi.novel.domain.NovelChapter;

/**
 * 章节 服务层
 *
 * @author novel
 */
public interface INovelChapterService
{
    public NovelChapter selectNovelChapterByChapterId(Long chapterId);

    public List<NovelChapter> selectNovelChapterList(NovelChapter novelChapter);

    public List<NovelChapter> selectNovelChapterTreeByProjectId(Long projectId);

    public int insertNovelChapter(NovelChapter novelChapter);

    public int updateNovelChapter(NovelChapter novelChapter);

    public int deleteNovelChapterByChapterIds(Long[] chapterIds);

    public int deleteNovelChapterByChapterId(Long chapterId);
}
