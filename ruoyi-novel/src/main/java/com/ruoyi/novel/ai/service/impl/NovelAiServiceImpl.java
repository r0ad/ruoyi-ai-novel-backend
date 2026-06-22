package com.ruoyi.novel.ai.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.capability.INovelAiCapabilityService;
import com.ruoyi.novel.ai.config.NovelAiModelFactory;
import com.ruoyi.novel.ai.context.ContextOptions;
import com.ruoyi.novel.ai.context.ProjectContextBuilder;
import com.ruoyi.novel.ai.domain.NovelAiChatRequest;
import com.ruoyi.novel.ai.domain.dto.ExtractResult;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;
import com.ruoyi.novel.ai.service.INovelAiService;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.rag.service.INovelContextService;
import com.ruoyi.novel.service.INovelChapterService;
import reactor.core.publisher.Flux;

@Service
public class NovelAiServiceImpl implements INovelAiService
{
    private static final Logger log = LoggerFactory.getLogger(NovelAiServiceImpl.class);

    private static final String STREAM_ERROR_PREFIX = "__AI_ERROR__:";

    @Autowired
    private NovelAiModelFactory novelAiModelFactory;

    @Autowired
    private INovelAiCapabilityService novelAiCapabilityService;

    @Autowired
    private ProjectContextBuilder projectContextBuilder;

    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private INovelContextService novelContextService;

    @Override
    public String chat(NovelAiChatRequest request)
    {
        ensureChatClient();
        if (request == null || StringUtils.isEmpty(request.getMessage()))
        {
            throw new ServiceException("消息内容不能为空");
        }
        StringBuilder system = new StringBuilder("你是一位专业网络小说创作助手，请用中文回答。");
        system.append(projectContextBuilder.buildProjectSystemContext(request.getProjectId()));
        return extractAssistantText(resolveChatClient().prompt()
            .system(system.toString())
            .user(request.getMessage())
            .call());
    }

    @Override
    public String reviewChapter(Long projectId, Long chapterId)
    {
        ContextOptions options = new ContextOptions();
        options.setProjectId(projectId);
        options.setChapterId(chapterId);
        ReviewResult result = novelAiCapabilityService.reviewChapter(options);
        return JSON.toJSONString(result);
    }

    @Override
    public String reviewProject(Long projectId)
    {
        ContextOptions options = new ContextOptions();
        options.setProjectId(projectId);
        ReviewResult result = novelAiCapabilityService.reviewProject(options);
        return JSON.toJSONString(result);
    }

    @Override
    public String extractMeta(Long projectId, Long chapterId)
    {
        ContextOptions options = new ContextOptions();
        options.setProjectId(projectId);
        options.setChapterId(chapterId);
        ExtractResult result = novelAiCapabilityService.extractMetaPreview(options);
        return JSON.toJSONString(result);
    }

    @Override
    public Flux<String> continueStream(NovelAiChatRequest request)
    {
        if (request == null)
        {
            return Flux.just(streamError("请求不能为空"));
        }
        if (resolveChatClient() == null)
        {
            return Flux.just(streamError("未配置激活的 AI 模型，请先在「AI模型管理」中添加并激活模型"));
        }
        return resolveChatClient().prompt()
            .system(buildContinueSystemPrompt(request))
            .user(buildContinueUserPrompt(request))
            .stream()
            .content()
            .onErrorResume(this::handleStreamError);
    }

    private Flux<String> handleStreamError(Throwable ex)
    {
        log.error("Continue stream failed", ex);
        return Flux.just(streamError(formatStreamError(ex)));
    }

    private String streamError(String message)
    {
        return STREAM_ERROR_PREFIX + message;
    }

    private String formatStreamError(Throwable ex)
    {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root)
        {
            root = root.getCause();
        }
        String rootMessage = root.getMessage();
        if (root instanceof java.io.InterruptedIOException
            || (rootMessage != null && rootMessage.toLowerCase().contains("timeout")))
        {
            return "AI 响应超时，请在「AI模型管理」将超时设为 300000ms（5 分钟）或更大后重新激活模型";
        }
        if (StringUtils.isNotEmpty(ex.getMessage()))
        {
            return "续写失败：" + ex.getMessage();
        }
        return "续写失败，请稍后重试";
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

    private String buildContinueSystemPrompt(NovelAiChatRequest request)
    {
        StringBuilder systemPrompt = new StringBuilder(
            "你是一位专业网络小说作家，请根据提供的设定与上文续写章节正文。");
        systemPrompt.append("要求：保持叙事风格一致，直接输出续写内容，不要解释；段落之间保留一个空行。");
        systemPrompt.append(projectContextBuilder.buildProjectSystemContext(request.getProjectId()));
        StringBuilder settingCtx = new StringBuilder();
        projectContextBuilder.appendSettingContext(settingCtx, request.getProjectId(),
            java.util.Arrays.asList("characters", "world", "outline"));
        systemPrompt.append(settingCtx);
        return systemPrompt.toString();
    }

    private String buildContinueUserPrompt(NovelAiChatRequest request)
    {
        boolean isWrite = "write".equals(request.getSessionType());
        StringBuilder userPrompt = new StringBuilder();
        if (request.getProjectId() != null && isWrite)
        {
            int chapterNum = resolveTargetChapterNumber(request);
            userPrompt.append("【全书写作上下文】\n")
                .append(novelContextService.buildWritingContext(request.getProjectId(), chapterNum, 2))
                .append("\n");
        }
        if (request.getChapterId() != null)
        {
            NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(request.getChapterId());
            if (chapter != null)
            {
                if (StringUtils.isNotEmpty(chapter.getTitle()))
                {
                    userPrompt.append("【当前章节标题】").append(chapter.getTitle()).append("\n");
                }
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
            userPrompt.append(isWrite ? "【创作要求】" : "【续写要求】").append(request.getMessage()).append("\n");
        }
        else if (isWrite)
        {
            userPrompt.append("【创作要求】请根据全书上下文自然开创新章节，撰写完整正文约 2000-3000 字。\n");
        }
        else
        {
            userPrompt.append("【续写要求】请自然延续上文情节，续写约 500-800 字。\n");
        }
        userPrompt.append(isWrite ? "请开始撰写正文：" : "请开始续写：");
        return userPrompt.toString();
    }

    private int resolveTargetChapterNumber(NovelAiChatRequest request)
    {
        if (request.getChapterId() != null)
        {
            NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(request.getChapterId());
            if (chapter != null && chapter.getChapterNumber() != null)
            {
                return chapter.getChapterNumber();
            }
        }
        NovelChapter query = new NovelChapter();
        query.setProjectId(request.getProjectId());
        int max = 0;
        for (NovelChapter ch : novelChapterService.selectNovelChapterList(query))
        {
            if (ch.getChapterNumber() != null && ch.getChapterNumber() > max)
            {
                max = ch.getChapterNumber();
            }
        }
        return max + 1;
    }
}
