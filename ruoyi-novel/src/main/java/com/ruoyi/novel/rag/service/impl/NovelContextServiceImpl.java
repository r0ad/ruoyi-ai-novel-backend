package com.ruoyi.novel.rag.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.rag.domain.NovelRagChunk;
import com.ruoyi.novel.rag.mapper.NovelRagChunkMapper;
import com.ruoyi.novel.rag.service.INovelContextService;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.service.INovelProjectService;
import com.ruoyi.novel.service.INovelSettingService;

@Service
public class NovelContextServiceImpl implements INovelContextService
{
    private static final int CHUNK_SIZE = 800;

    @Autowired
    private INovelProjectService novelProjectService;

    @Autowired
    private INovelSettingService novelSettingService;

    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private NovelRagChunkMapper novelRagChunkMapper;

    @Override
    public String buildWritingContext(Long projectId, int chapterNumber, int rangeBefore)
    {
        StringBuilder sb = new StringBuilder();
        NovelProject project = novelProjectService.selectNovelProjectByProjectId(projectId);
        if (project != null)
        {
            sb.append("【项目】").append(project.getTitle());
            if (StringUtils.isNotEmpty(project.getStyleGuide()))
            {
                sb.append("\n【风格】").append(truncate(project.getStyleGuide(), 500));
            }
        }
        appendSetting(sb, projectId, "characters");
        appendSetting(sb, projectId, "world");
        appendSetting(sb, projectId, "outline");
        appendPreviousChapters(sb, projectId, chapterNumber, rangeBefore);
        appendRagHints(sb, projectId, chapterNumber);
        return sb.toString();
    }

    @Override
    public void indexProjectContent(Long projectId)
    {
        NovelChapter query = new NovelChapter();
        query.setProjectId(projectId);
        List<NovelChapter> chapters = novelChapterService.selectNovelChapterList(query);
        for (NovelChapter chapter : chapters)
        {
            if (StringUtils.isNotEmpty(chapter.getContent()))
            {
                indexText(projectId, "chapter", chapter.getChapterId(), chapter.getContent());
            }
        }
        for (String type : new String[] { "characters", "world", "outline" })
        {
            NovelSetting setting = novelSettingService.selectNovelSettingByProjectAndType(projectId, type);
            if (setting != null && StringUtils.isNotEmpty(setting.getContent()))
            {
                indexText(projectId, "setting", setting.getSettingId(), setting.getContent());
            }
        }
    }

    private void appendSetting(StringBuilder sb, Long projectId, String type)
    {
        NovelSetting setting = novelSettingService.selectNovelSettingByProjectAndType(projectId, type);
        if (setting != null && StringUtils.isNotEmpty(setting.getContent()))
        {
            sb.append("\n【").append(type).append("】\n").append(truncate(setting.getContent(), 2000));
        }
    }

    private void appendPreviousChapters(StringBuilder sb, Long projectId, int chapterNumber, int rangeBefore)
    {
        NovelChapter query = new NovelChapter();
        query.setProjectId(projectId);
        List<NovelChapter> chapters = new ArrayList<NovelChapter>(novelChapterService.selectNovelChapterList(query));
        chapters.sort(Comparator.comparingInt(c -> c.getChapterNumber() != null ? c.getChapterNumber() : 0));
        int start = Math.max(1, chapterNumber - rangeBefore);
        for (NovelChapter chapter : chapters)
        {
            int num = chapter.getChapterNumber() != null ? chapter.getChapterNumber() : 0;
            if (num >= start && num < chapterNumber)
            {
                sb.append("\n【第").append(num).append("章 ").append(chapter.getTitle()).append("】\n");
                if (StringUtils.isNotEmpty(chapter.getSummary()))
                {
                    sb.append("摘要：").append(chapter.getSummary()).append("\n");
                }
                if (StringUtils.isNotEmpty(chapter.getContent()))
                {
                    sb.append(truncate(chapter.getContent(), 1500)).append("\n");
                }
            }
        }
    }

    private void appendRagHints(StringBuilder sb, Long projectId, int chapterNumber)
    {
        List<NovelRagChunk> chunks = novelRagChunkMapper.searchByKeyword(
            projectId, "第" + chapterNumber, 3);
        if (chunks.isEmpty())
        {
            return;
        }
        sb.append("\n【RAG 相关片段】\n");
        for (NovelRagChunk chunk : chunks)
        {
            sb.append(truncate(chunk.getContent(), 400)).append("\n---\n");
        }
    }

    private void indexText(Long projectId, String sourceType, Long sourceId, String text)
    {
        novelRagChunkMapper.deleteByProjectAndSource(projectId, sourceType, sourceId);
        int index = 0;
        for (int i = 0; i < text.length(); i += CHUNK_SIZE)
        {
            String part = text.substring(i, Math.min(text.length(), i + CHUNK_SIZE));
            NovelRagChunk chunk = new NovelRagChunk();
            chunk.setProjectId(projectId);
            chunk.setSourceType(sourceType);
            chunk.setSourceId(sourceId);
            chunk.setChunkIndex(index++);
            chunk.setContent(part);
            novelRagChunkMapper.insertNovelRagChunk(chunk);
        }
    }

    private String truncate(String text, int max)
    {
        if (text == null)
        {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
