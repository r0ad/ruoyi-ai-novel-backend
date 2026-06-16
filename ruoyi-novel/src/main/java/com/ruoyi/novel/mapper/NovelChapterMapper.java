package com.ruoyi.novel.mapper;

import java.util.List;
import com.ruoyi.novel.domain.NovelChapter;

/**
 * 章节 数据层
 *
 * @author novel
 */
public interface NovelChapterMapper
{
    public NovelChapter selectNovelChapterByChapterId(Long chapterId);

    public List<NovelChapter> selectNovelChapterList(NovelChapter novelChapter);

    public Integer selectMaxChapterNumber(Long projectId);

    public int insertNovelChapter(NovelChapter novelChapter);

    public int updateNovelChapter(NovelChapter novelChapter);

    public int deleteNovelChapterByChapterId(Long chapterId);

    public int deleteNovelChapterByChapterIds(Long[] chapterIds);

    public int insertNovelChapterContent(NovelChapter novelChapter);

    public int updateNovelChapterContent(NovelChapter novelChapter);

    public int deleteNovelChapterContentByChapterIds(Long[] chapterIds);

    public int insertNovelChapterVersion(NovelChapter novelChapter);

    public int countChaptersByProjectId(Long projectId);

    public Long sumWordCountByProjectId(Long projectId);
}
