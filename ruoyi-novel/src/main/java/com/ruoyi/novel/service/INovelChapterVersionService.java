package com.ruoyi.novel.service;

import java.util.List;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelChapterDiff;
import com.ruoyi.novel.domain.NovelChapterVersion;

public interface INovelChapterVersionService
{
    public List<NovelChapterVersion> selectVersionsByChapterId(Long chapterId);

    public NovelChapterVersion selectVersion(Long chapterId, Integer versionNo);

    public NovelChapterDiff compareVersions(Long chapterId, Integer fromVersionNo, Integer toVersionNo);

    public int revertToVersion(Long chapterId, Integer versionNo, String updateBy);
}