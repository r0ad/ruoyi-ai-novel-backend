package com.ruoyi.novel.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.mapper.NovelChapterMapper;
import com.ruoyi.novel.mapper.NovelProjectMapper;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.utils.NovelWordCountUtils;

/**
 * 章节 服务层实现
 *
 * @author novel
 */
@Service
public class NovelChapterServiceImpl implements INovelChapterService
{
    @Autowired
    private NovelChapterMapper novelChapterMapper;

    @Autowired
    private NovelProjectMapper novelProjectMapper;

    @Override
    public NovelChapter selectNovelChapterByChapterId(Long chapterId)
    {
        return novelChapterMapper.selectNovelChapterByChapterId(chapterId);
    }

    @Override
    public List<NovelChapter> selectNovelChapterList(NovelChapter novelChapter)
    {
        return novelChapterMapper.selectNovelChapterList(novelChapter);
    }

    @Override
    public List<NovelChapter> selectNovelChapterTreeByProjectId(Long projectId)
    {
        NovelChapter query = new NovelChapter();
        query.setProjectId(projectId);
        List<NovelChapter> chapters = novelChapterMapper.selectNovelChapterList(query);
        Map<Long, NovelChapter> chapterMap = new LinkedHashMap<Long, NovelChapter>();
        List<NovelChapter> roots = new ArrayList<NovelChapter>();
        for (NovelChapter chapter : chapters)
        {
            chapter.setChildren(new ArrayList<NovelChapter>());
            chapterMap.put(chapter.getChapterId(), chapter);
        }
        for (NovelChapter chapter : chapters)
        {
            Long parentId = chapter.getParentId();
            if (parentId == null || parentId == 0L || !chapterMap.containsKey(parentId))
            {
                roots.add(chapter);
            }
            else
            {
                chapterMap.get(parentId).getChildren().add(chapter);
            }
        }
        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertNovelChapter(NovelChapter novelChapter)
    {
        prepareDefaults(novelChapter, true);
        novelChapter.setWordCount(NovelWordCountUtils.countWords(novelChapter.getContent()));
        int rows = novelChapterMapper.insertNovelChapter(novelChapter);
        if (rows > 0)
        {
            if (novelChapter.getContent() == null)
            {
                novelChapter.setContent("");
            }
            novelChapterMapper.insertNovelChapterContent(novelChapter);
            novelChapterMapper.insertNovelChapterVersion(novelChapter);
            refreshProjectStats(novelChapter.getProjectId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNovelChapter(NovelChapter novelChapter)
    {
        NovelChapter existing = novelChapterMapper.selectNovelChapterByChapterId(novelChapter.getChapterId());
        if (existing == null)
        {
            return 0;
        }
        Long originalProjectId = existing.getProjectId();
        if (novelChapter.getProjectId() == null)
        {
            novelChapter.setProjectId(existing.getProjectId());
        }
        if (novelChapter.getParentId() == null)
        {
            novelChapter.setParentId(existing.getParentId() == null ? 0L : existing.getParentId());
        }
        if (novelChapter.getChapterNumber() == null)
        {
            novelChapter.setChapterNumber(existing.getChapterNumber());
        }
        if (StringUtils.isEmpty(novelChapter.getTitle()))
        {
            novelChapter.setTitle(existing.getTitle());
        }
        if (novelChapter.getSummary() == null)
        {
            novelChapter.setSummary(existing.getSummary());
        }
        if (StringUtils.isEmpty(novelChapter.getStatus()))
        {
            novelChapter.setStatus(existing.getStatus());
        }
        if (novelChapter.getSortOrder() == null)
        {
            novelChapter.setSortOrder(existing.getSortOrder());
        }
        if (novelChapter.getContent() != null)
        {
            novelChapter.setWordCount(NovelWordCountUtils.countWords(novelChapter.getContent()));
        }
        else
        {
            novelChapter.setWordCount(existing.getWordCount());
            novelChapter.setContent(existing.getContent());
        }
        int nextVersion = existing.getVersionNo() == null ? 1 : existing.getVersionNo() + 1;
        novelChapter.setVersionNo(nextVersion);
        int rows = novelChapterMapper.updateNovelChapter(novelChapter);
        if (rows > 0)
        {
            if (novelChapterMapper.updateNovelChapterContent(novelChapter) == 0)
            {
                novelChapterMapper.insertNovelChapterContent(novelChapter);
            }
            novelChapterMapper.insertNovelChapterVersion(novelChapter);
            refreshProjectStats(originalProjectId);
            if (!originalProjectId.equals(novelChapter.getProjectId()))
            {
                refreshProjectStats(novelChapter.getProjectId());
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteNovelChapterByChapterIds(Long[] chapterIds)
    {
        if (chapterIds == null || chapterIds.length == 0)
        {
            return 0;
        }
        NovelChapter first = novelChapterMapper.selectNovelChapterByChapterId(chapterIds[0]);
        Long projectId = first != null ? first.getProjectId() : null;
        int rows = novelChapterMapper.deleteNovelChapterByChapterIds(chapterIds);
        if (rows > 0 && projectId != null)
        {
            refreshProjectStats(projectId);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteNovelChapterByChapterId(Long chapterId)
    {
        return deleteNovelChapterByChapterIds(new Long[] { chapterId });
    }

    private void prepareDefaults(NovelChapter novelChapter, boolean assignNumber)
    {
        if (novelChapter.getParentId() == null)
        {
            novelChapter.setParentId(0L);
        }
        if (StringUtils.isEmpty(novelChapter.getStatus()))
        {
            novelChapter.setStatus("0");
        }
        if (novelChapter.getSortOrder() == null)
        {
            novelChapter.setSortOrder(0);
        }
        if (novelChapter.getVersionNo() == null)
        {
            novelChapter.setVersionNo(1);
        }
        if (assignNumber && novelChapter.getChapterNumber() == null)
        {
            Integer max = novelChapterMapper.selectMaxChapterNumber(novelChapter.getProjectId());
            novelChapter.setChapterNumber(max == null ? 1 : max + 1);
        }
    }

    private void refreshProjectStats(Long projectId)
    {
        if (projectId == null)
        {
            return;
        }
        NovelProject project = new NovelProject();
        project.setProjectId(projectId);
        project.setChapterCount(novelChapterMapper.countChaptersByProjectId(projectId));
        Long wordCount = novelChapterMapper.sumWordCountByProjectId(projectId);
        project.setWordCount(wordCount == null ? 0L : wordCount);
        novelProjectMapper.updateNovelProject(project);
    }
}
