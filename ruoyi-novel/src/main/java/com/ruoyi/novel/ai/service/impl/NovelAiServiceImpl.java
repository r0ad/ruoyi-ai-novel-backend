package com.ruoyi.novel.ai.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.config.NovelAiModelFactory;
import com.ruoyi.novel.ai.domain.NovelAiChatRequest;
import com.ruoyi.novel.ai.service.INovelAiService;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.service.INovelProjectService;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.domain.NovelMetaGraph;
import com.ruoyi.novel.service.INovelMetaService;
import com.ruoyi.novel.service.INovelSettingService;
import reactor.core.publisher.Flux;

@Service
public class NovelAiServiceImpl implements INovelAiService
{
    @Autowired
    private NovelAiModelFactory novelAiModelFactory;

    @Autowired
    private INovelProjectService novelProjectService;

    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private INovelSettingService novelSettingService;

    @Autowired
    private INovelMetaService novelMetaService;

    @Override
    public String chat(NovelAiChatRequest request)
    {
        ensureChatClient();
        if (request == null || StringUtils.isEmpty(request.getMessage()))
        {
            throw new ServiceException("消息内容不能为空");
        }
        return extractAssistantText(resolveChatClient().prompt()
            .system(buildSystemPrompt(request))
            .user(request.getMessage())
            .call());
    }

    @Override
    public String reviewChapter(Long projectId, Long chapterId)
    {
        ensureChatClient();
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
        if (chapter == null || !chapter.getProjectId().equals(projectId))
        {
            throw new ServiceException("章节不存在");
        }
        StringBuilder user = new StringBuilder("请审查以下章节与设定、Meta 图谱的一致性，输出 JSON 数组格式问题列表：\n");
        user.append("【章节】").append(chapter.getTitle()).append("\n");
        if (StringUtils.isNotEmpty(chapter.getContent()))
        {
            user.append(chapter.getContent());
        }
        StringBuilder system = new StringBuilder("你是小说一致性审查专家，请用中文输出结构化审查报告。");
        appendProjectContext(system, projectId);
        appendSettingContext(system, projectId);
        NovelMetaGraph graph = novelMetaService.selectGraphByProjectId(projectId);
        if (graph != null)
        {
            user.append("\n【Meta图谱】").append(com.alibaba.fastjson2.JSON.toJSONString(graph));
        }
        return extractAssistantText(resolveChatClient().prompt().system(system.toString()).user(user.toString()).call());
    }

    @Override
    public String reviewProject(Long projectId)
    {
        ensureChatClient();
        StringBuilder user = new StringBuilder("请对全书进行一致性审查，涵盖角色、世界观、情节连贯性：\n");
        appendSettingContext(user, projectId);
        NovelMetaGraph graph = novelMetaService.selectGraphByProjectId(projectId);
        if (graph != null)
        {
            user.append("\n【Meta图谱】").append(com.alibaba.fastjson2.JSON.toJSONString(graph));
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
        StringBuilder system = new StringBuilder("你是小说终审编辑，请输出完整审查报告与修改建议。");
        appendProjectContext(system, projectId);
        return extractAssistantText(resolveChatClient().prompt().system(system.toString()).user(user.toString()).call());
    }

    @Override
    public String extractMeta(Long projectId, Long chapterId)
    {
        ensureChatClient();
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(chapterId);
        if (chapter == null || !chapter.getProjectId().equals(projectId))
        {
            throw new ServiceException("章节不存在");
        }
        String prompt = "从以下章节正文抽取角色、地点、物品实体及关系，以 JSON 返回 entities 和 relations 数组：\n"
            + (chapter.getContent() != null ? chapter.getContent() : "");
        String json = extractAssistantText(resolveChatClient().prompt()
            .system("你是信息抽取专家，仅输出 JSON。")
            .user(prompt)
            .call());
        persistExtractedMeta(projectId, chapterId, json);
        return json;
    }

    private void persistExtractedMeta(Long projectId, Long chapterId, String json)
    {
        try
        {
            com.alibaba.fastjson2.JSONObject root = com.alibaba.fastjson2.JSON.parseObject(json);
            if (root == null)
            {
                return;
            }
            com.alibaba.fastjson2.JSONArray entities = root.getJSONArray("entities");
            if (entities != null)
            {
                for (int i = 0; i < entities.size(); i++)
                {
                    com.alibaba.fastjson2.JSONObject item = entities.getJSONObject(i);
                    NovelMetaEntity entity = new NovelMetaEntity();
                    entity.setProjectId(projectId);
                    entity.setEntityType(item.getString("entityType"));
                    entity.setName(item.getString("name"));
                    entity.setDescription(item.getString("description"));
                    entity.setLastChapterId(chapterId);
                    if (StringUtils.isNotEmpty(entity.getName()) && StringUtils.isNotEmpty(entity.getEntityType()))
                    {
                        novelMetaService.insertEntity(entity);
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    @Override
    public Flux<String> continueStream(NovelAiChatRequest request)
    {
        ensureChatClient();
        if (request == null)
        {
            return Flux.error(new ServiceException("请求不能为空"));
        }
        return resolveChatClient().prompt()
            .system(buildContinueSystemPrompt(request))
            .user(buildContinueUserPrompt(request))
            .stream()
            .content();
    }

    private void ensureChatClient()
    {
        if (resolveChatClient() == null)
        {
            throw new ServiceException("未配置激活的 AI 模型，请先在「AI模型管理」中添加并激活模型");
        }
    }

    private ChatClient resolveChatClient()
    {
        return novelAiModelFactory.getChatClient();
    }

    private String extractAssistantText(ChatClient.CallResponseSpec callResponse)
    {
        String content = callResponse.content();
        if (StringUtils.isNotEmpty(content))
        {
            return content;
        }
        throw new ServiceException("AI 返回内容为空");
    }

    private String buildSystemPrompt(NovelAiChatRequest request)
    {
        StringBuilder systemPrompt = new StringBuilder("你是一位专业网络小说创作助手，请用中文回答。");
        appendProjectContext(systemPrompt, request != null ? request.getProjectId() : null);
        return systemPrompt.toString();
    }

    private String buildContinueSystemPrompt(NovelAiChatRequest request)
    {
        StringBuilder systemPrompt = new StringBuilder(
            "你是一位专业网络小说作家，请根据提供的设定与上文续写章节正文。");
        systemPrompt.append("要求：保持叙事风格一致，直接输出续写内容，不要解释。");
        appendProjectContext(systemPrompt, request.getProjectId());
        appendSettingContext(systemPrompt, request.getProjectId());
        return systemPrompt.toString();
    }

    private String buildContinueUserPrompt(NovelAiChatRequest request)
    {
        StringBuilder userPrompt = new StringBuilder();
        if (request.getChapterId() != null)
        {
            NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(request.getChapterId());
            if (chapter != null)
            {
                userPrompt.append("【章节标题】").append(chapter.getTitle()).append("\n");
                if (StringUtils.isNotEmpty(chapter.getSummary()))
                {
                    userPrompt.append("【章节摘要】").append(chapter.getSummary()).append("\n");
                }
                if (StringUtils.isNotEmpty(chapter.getContent()))
                {
                    userPrompt.append("【已有正文】\n").append(chapter.getContent()).append("\n\n");
                }
            }
        }
        if (StringUtils.isNotEmpty(request.getMessage()))
        {
            userPrompt.append("【续写要求】").append(request.getMessage()).append("\n");
        }
        else
        {
            userPrompt.append("【续写要求】请自然延续上文情节，续写约 500-800 字。\n");
        }
        userPrompt.append("请开始续写：");
        return userPrompt.toString();
    }

    private void appendProjectContext(StringBuilder systemPrompt, Long projectId)
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
        systemPrompt.append("\n书名：").append(project.getTitle());
        if (StringUtils.isNotEmpty(project.getGenre()))
        {
            systemPrompt.append("\n类型：").append(project.getGenre());
        }
        if (StringUtils.isNotEmpty(project.getSummary()))
        {
            systemPrompt.append("\n简介：").append(project.getSummary());
        }
        if (StringUtils.isNotEmpty(project.getStyleGuide()))
        {
            systemPrompt.append("\n风格：").append(project.getStyleGuide());
        }
    }

    private void appendSettingContext(StringBuilder systemPrompt, Long projectId)
    {
        if (projectId == null)
        {
            return;
        }
        appendSettingIfPresent(systemPrompt, projectId, "characters", "角色设定");
        appendSettingIfPresent(systemPrompt, projectId, "world", "世界观");
        appendSettingIfPresent(systemPrompt, projectId, "outline", "大纲");
    }

    private void appendSettingIfPresent(StringBuilder systemPrompt, Long projectId, String type, String label)
    {
        NovelSetting setting = novelSettingService.selectNovelSettingByProjectAndType(projectId, type);
        if (setting != null && StringUtils.isNotEmpty(setting.getContent()))
        {
            systemPrompt.append("\n【").append(label).append("】\n").append(setting.getContent());
        }
    }
}
