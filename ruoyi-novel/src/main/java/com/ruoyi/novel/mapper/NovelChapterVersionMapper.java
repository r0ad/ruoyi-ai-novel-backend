package com.ruoyi.novel.mapper;

import java.util.List;
import com.ruoyi.novel.domain.NovelChapterVersion;

public interface NovelChapterVersionMapper
{
    public List<NovelChapterVersion> selectVersionsByChapterId(Long chapterId);

    public NovelChapterVersion selectVersionByChapterAndNo(NovelChapterVersion version);
}