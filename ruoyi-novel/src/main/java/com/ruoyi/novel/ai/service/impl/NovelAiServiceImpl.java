package com.ruoyi.novel.ai.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.domain.NovelAiChatRequest;
import com.ruoyi.novel.ai.service.INovelAiService;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.service.INovelProjectService;
import com.ruoyi.novel.service.INovelSettingService;
import reactor.core.publisher.Flux;

@Service
public class NovelAiServiceImpl implements INovelAiService
{
    @Autowired(required = false)
    private ChatClient novelChatClient;

    @Autowired(required = false)
    private OpenAiChatModel openAiChatModel;

    @Autowired
    private INovelProjectService novelProjectService;

    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private INovelSettingService novelSettingService;

    @Override
    public String chat(NovelAiChatRequest request)
    {
        ensureChatClient();
        if (request == null || StringUtils.isEmpty(request.getMessage()))
        {
            throw new ServiceException("消息内容不能为空");
        }
        return resolveChatClient().prompt()
            .system(buildSystemPrompt(request))
            .user(request.getMessage())
            .call()
            .content();
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
            throw new ServiceException("Spring AI 未配置，请设置 spring.ai.openai.api-key 或环境变量 OPENAI_API_KEY，并重启后端");
        }
    }

    private ChatClient resolveChatClient()
    {
        if (novelChatClient != null)
        {
            return novelChatClient;
        }
        if (openAiChatModel != null)
        {
            return ChatClient.builder(openAiChatModel)
                .defaultSystem("你是一位专业网络小说创作助手，请用中文回答。")
                .build();
        }
        return null;
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
