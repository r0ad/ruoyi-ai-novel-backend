package com.ruoyi.novel.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelChapterDiff;
import com.ruoyi.novel.domain.NovelChapterVersion;
import com.ruoyi.novel.mapper.NovelChapterVersionMapper;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.service.INovelChapterVersionService;
import com.ruoyi.novel.utils.NovelTextDiffUtils;

@Service
public class NovelChapterVersionServiceImpl implements INovelChapterVersionService
{
    @Autowired
    private NovelChapterVersionMapper novelChapterVersionMapper;

    @Autowired
    private INovelChapterService novelChapterService;

    @Override
    public List<NovelChapterVersion> selectVersionsByChapterId(Long chapterId)
    {
        return novelChapterVersionMapper.selectVersionsByChapterId(chapterId);
    }

    @Override
    public NovelChapterVersion selectVersion(Long chapterId, Integer versionNo)
    {
        NovelChapterVersion query = new NovelChapterVersion();
        query.setChapterId(chapterId);
        query.setVersionNo(versionNo);
        return novelChapterVersionMapper.selectVersionByChapterAndNo(query);
    }

    @Override
    public NovelChapterDiff compareVersions(Long chapterId, Integer fromVersionNo, Integer toVersionNo)
    {
        NovelChapterVersion from = selectVersion(chapterId, fromVersionNo);
        NovelChapterVersion to = selectVersion(chapterId, toVersionNo);
        if (from == null || to == null)
        {
            throw new ServiceException("版本不存在");
        }
        NovelChapterDiff diff = new NovelChapterDiff();
        diff.setFromVersionNo(fromVersionNo);
        diff.setToVersionNo(toVersionNo);
        diff.setFromTitle(from.getTitle());
        diff.setToTitle(to.getTitle());
        diff.setFromContent(from.getContent());
        diff.setToContent(to.getContent());
        diff.setLines(NovelTextDiffUtils.diffLines(from.getContent(), to.getContent()));
        return diff;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revertToVersion(Long chapterId, Integer versionNo, String updateBy)
    {
        NovelChapterVersion version = selectVersion(chapterId, versionNo);
        if (version == null)
        {
            throw new ServiceException("版本不存在");
        }
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
        if (chapter == null)
        {
            throw new ServiceException("章节不存在");
        }
        chapter.setTitle(version.getTitle());
        chapter.setSummary(version.getSummary());
        chapter.setContent(version.getContent());
        chapter.setUpdateBy(updateBy);
        return novelChapterService.updateNovelChapter(chapter);
    }
}