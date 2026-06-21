package com.ruoyi.novel.ai.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelMetaGraph;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.service.INovelMetaService;
import com.ruoyi.novel.service.INovelProjectService;
import com.ruoyi.novel.service.INovelSettingService;

@Component
public class ProjectContextBuilder
{
    private static final Map<String, String> SETTING_LABELS = new HashMap<String, String>();

    static
    {
        SETTING_LABELS.put("characters", "角色设定");
        SETTING_LABELS.put("world", "世界观");
        SETTING_LABELS.put("outline", "故事大纲");
        SETTING_LABELS.put("style", "风格指南");
        SETTING_LABELS.put("scene", "场景设定");
        SETTING_LABELS.put("metrics", "创作指标");
    }

    @Autowired
    private INovelProjectService novelProjectService;

    @Autowired
    private INovelSettingService novelSettingService;

    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private INovelMetaService novelMetaService;

    public String buildProjectSystemContext(Long projectId)
    {
        StringBuilder sb = new StringBuilder();
        appendProjectContext(sb, projectId);
        return sb.toString();
    }

    public void appendSettingContext(StringBuilder sb, Long projectId, List<String> types)
    {
        if (projectId == null || types == null)
        {
            return;
        }
        for (String type : types)
        {
            appendSettingIfPresent(sb, projectId, type, SETTING_LABELS.getOrDefault(type, type));
        }
    }

    public ProjectAiContext buildReviewChapterContext(ContextOptions options)
    {
        Long projectId = options.getProjectId();
        Long chapterId = options.getChapterId();
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
        StringBuilder system = new StringBuilder(
            "你是小说一致性审查专家。仅输出 JSON，不要 Markdown 代码块，不要额外解释。");
        system.append("\nJSON 格式：{\"taskType\":\"review_chapter\",\"target\":{\"chapterId\":")
            .append(chapterId).append(",\"chapterTitle\":\"...\"},\"passed\":true/false,")
            .append("\"issues\":[{\"id\":\"iss-1\",\"severity\":\"critical|major|minor\",")
            .append("\"category\":\"...\",\"summary\":\"...\",\"suggestedActions\":[{\"type\":\"...\",\"hint\":\"...\"}],")
            .append("\"autoFixable\":true/false}]}");
        appendProjectContext(system, projectId);
        appendSettingContext(system, projectId, options.getIncludeSettings());

        StringBuilder user = new StringBuilder("请审查以下章节与设定、Meta 图谱的一致性：\n");
        if (chapter != null)
        {
            user.append("【章节】").append(chapter.getTitle()).append("\n");
            if (StringUtils.isNotEmpty(chapter.getContent()))
            {
                user.append(chapter.getContent());
            }
        }
        if (options.isIncludeMetaGraph())
        {
            NovelMetaGraph graph = novelMetaService.selectGraphByProjectId(projectId);
            if (graph != null)
            {
                user.append("\n【Meta图谱】").append(JSON.toJSONString(graph));
            }
        }
        if (StringUtils.isNotEmpty(options.getUserMessage()))
        {
            user.append("\n【附加说明】").append(options.getUserMessage());
        }
        return new ProjectAiContext(system.toString(), user.toString());
    }

    public ProjectAiContext buildReviewProjectContext(ContextOptions options)
    {
        Long projectId = options.getProjectId();
        StringBuilder system = new StringBuilder(
            "你是小说终审编辑。仅输出 JSON，格式与 review_chapter 相同，taskType 为 review_project。");
        appendProjectContext(system, projectId);

        StringBuilder user = new StringBuilder("请对全书进行一致性审查：\n");
        appendSettingContext(user, projectId, options.getIncludeSettings());
        if (options.isIncludeMetaGraph())
        {
            NovelMetaGraph graph = novelMetaService.selectGraphByProjectId(projectId);
            if (graph != null)
            {
                user.append("\n【Meta图谱】").append(JSON.toJSONString(graph));
            }
        }
        NovelChapter query = new NovelChapter();
        query.setProjectId(projectId);
        for (NovelChapter ch : novelChapterService.selectNovelChapterList(query))
        {
            user.append("\n【第").append(ch.getChapterNumber()).append("章 ").append(ch.getTitle()).append("】");
            if (StringUtils.isNotEmpty(ch.getSummary()))
            {
                user.append("\n摘要：").append(ch.getSummary());
            }
        }
        return new ProjectAiContext(system.toString(), user.toString());
    }

    public ProjectAiContext buildExtractMetaContext(ContextOptions options)
    {
        Long projectId = options.getProjectId();
        Long chapterId = options.getChapterId();
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
        StringBuilder system = new StringBuilder(
            "你是信息抽取专家。仅输出 JSON，不要 Markdown 代码块。");
        system.append("\n格式：{\"taskType\":\"extract_meta\",\"target\":{\"chapterId\":")
            .append(chapterId).append("},\"entities\":[{\"entityType\":\"character|location|item|event\",")
            .append("\"name\":\"...\",\"description\":\"...\"}],\"relations\":[{\"from\":\"...\",")
            .append("\"to\":\"...\",\"relationType\":\"...\"}],\"conflicts\":[]}");

        StringBuilder user = new StringBuilder("从以下章节正文抽取实体与关系：\n");
        if (chapter != null && StringUtils.isNotEmpty(chapter.getContent()))
        {
            user.append(chapter.getContent());
        }
        if (options.isIncludeMetaGraph())
        {
            NovelMetaGraph graph = novelMetaService.selectGraphByProjectId(projectId);
            if (graph != null)
            {
                user.append("\n【已有Meta图谱，请标记与已有实体同名项到 conflicts】")
                    .append(JSON.toJSONString(graph));
            }
        }
        return new ProjectAiContext(system.toString(), user.toString());
    }

    public ProjectAiContext buildExtractSettingContext(ContextOptions options)
    {
        Long projectId = options.getProjectId();
        String settingType = StringUtils.isNotEmpty(options.getSettingType()) ? options.getSettingType() : "characters";
        StringBuilder system = new StringBuilder(
            "你是小说设定编辑。仅输出 JSON，不要 Markdown 代码块。");
        system.append("\n格式：{\"taskType\":\"extract_setting\",\"settingType\":\"")
            .append(settingType).append("\",\"title\":\"...\",\"content\":\"Markdown 设定正文\"}");
        appendProjectContext(system, projectId);

        StringBuilder user = new StringBuilder("请从以下章节正文反推并整理「")
            .append(SETTING_LABELS.getOrDefault(settingType, settingType))
            .append("」设定草案：\n");
        appendChaptersContent(user, options);
        if (StringUtils.isNotEmpty(options.getUserMessage()))
        {
            user.append("\n【附加要求】").append(options.getUserMessage());
        }
        Map<String, Object> target = new HashMap<String, Object>();
        target.put("settingType", settingType);
        return new ProjectAiContext(system.toString(), user.toString());
    }

    public ProjectAiContext buildGenerateSettingContext(ContextOptions options)
    {
        Long projectId = options.getProjectId();
        String settingType = StringUtils.isNotEmpty(options.getSettingType()) ? options.getSettingType() : "characters";
        StringBuilder system = new StringBuilder(
            "你是网络小说策划。仅输出 JSON，不要 Markdown 代码块。");
        system.append("\n格式：{\"taskType\":\"generate_setting\",\"settingType\":\"")
            .append(settingType).append("\",\"title\":\"...\",\"content\":\"Markdown 设定正文\"}");
        appendProjectContext(system, projectId);
        appendSettingContext(system, projectId, options.getIncludeSettings());

        StringBuilder user = new StringBuilder("请为当前项目生成「")
            .append(SETTING_LABELS.getOrDefault(settingType, settingType))
            .append("」设定草案。");
        if (StringUtils.isNotEmpty(options.getUserMessage()))
        {
            user.append("\n【创作要求】").append(options.getUserMessage());
        }
        return new ProjectAiContext(system.toString(), user.toString());
    }

    public ProjectAiContext buildSyncSettingMetaContext(ContextOptions options)
    {
        Long projectId = options.getProjectId();
        StringBuilder system = new StringBuilder(
            "你是设定与知识图谱对齐专家。仅输出 JSON，不要 Markdown 代码块。");
        system.append("\n格式：{\"taskType\":\"sync_setting_meta\",\"syncActions\":[")
            .append("{\"type\":\"update_meta_entity|insert_meta_entity|update_setting\",")
            .append("\"entityName\":\"...\",\"newName\":\"...\",\"settingType\":\"characters\",")
            .append("\"hint\":\"...\"}]}");
        appendProjectContext(system, projectId);
        appendSettingContext(system, projectId, java.util.Arrays.asList("characters", "world", "outline"));

        StringBuilder user = new StringBuilder("请对比设定与 Meta 图谱，给出需要对齐的 syncActions 列表。\n");
        if (options.isIncludeMetaGraph())
        {
            NovelMetaGraph graph = novelMetaService.selectGraphByProjectId(projectId);
            if (graph != null)
            {
                user.append("【Meta图谱】").append(JSON.toJSONString(graph));
            }
        }
        return new ProjectAiContext(system.toString(), user.toString());
    }

    private void appendChaptersContent(StringBuilder user, ContextOptions options)
    {
        java.util.List<Long> ids = new java.util.ArrayList<Long>();
        if (options.getChapterIds() != null && !options.getChapterIds().isEmpty())
        {
            ids.addAll(options.getChapterIds());
        }
        else if (options.getChapterId() != null)
        {
            ids.add(options.getChapterId());
        }
        for (Long chapterId : ids)
        {
            NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
            if (chapter != null)
            {
                user.append("\n【").append(chapter.getTitle()).append("】\n");
                if (StringUtils.isNotEmpty(chapter.getContent()))
                {
                    user.append(chapter.getContent());
                }
            }
        }
    }

    private void appendProjectContext(StringBuilder sb, Long projectId)
    {
        if (projectId == null)
        {
            return;
        }
        NovelProject project = novelProjectService.selectNovelProjectByProjectId(projectId);
        if (project == null)
        {
            return;
        }
        sb.append("\n书名：").append(project.getTitle());
        if (StringUtils.isNotEmpty(project.getGenre()))
        {
            sb.append("\n类型：").append(project.getGenre());
        }
        if (StringUtils.isNotEmpty(project.getSummary()))
        {
            sb.append("\n简介：").append(project.getSummary());
        }
        if (StringUtils.isNotEmpty(project.getStyleGuide()))
        {
            sb.append("\n风格：").append(project.getStyleGuide());
        }
    }

    private void appendSettingIfPresent(StringBuilder sb, Long projectId, String type, String label)
    {
        NovelSetting setting = novelSettingService.selectNovelSettingByProjectAndType(projectId, type);
        if (setting != null && StringUtils.isNotEmpty(setting.getContent()))
        {
            sb.append("\n【").append(label).append("】\n").append(setting.getContent());
        }
    }
}
