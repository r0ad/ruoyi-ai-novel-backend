package com.ruoyi.novel.ai.service.impl;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.domain.NovelAiChatRequest;
import com.ruoyi.novel.ai.service.INovelAiService;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.service.INovelProjectService;

@Service
public class NovelAiServiceImpl implements INovelAiService
{
    @Autowired(required = false)
    private ChatClient novelChatClient;

    @Autowired
    private INovelProjectService novelProjectService;

    @Override
    public String chat(NovelAiChatRequest request)
    {
        if (novelChatClient == null)
        {
            throw new ServiceException("Spring AI 未配置，请设置 spring.ai.openai.api-key 或环境变量 OPENAI_API_KEY");
        }
        if (request == null || StringUtils.isEmpty(request.getMessage()))
        {
            throw new ServiceException("消息内容不能为空");
        }
        StringBuilder systemPrompt = new StringBuilder("你是一位专业网络小说创作助手，请用中文回答。");
        if (request.getProjectId() != null)
        {
            NovelProject project = novelProjectService.selectNovelProjectByProjectId(request.getProjectId());
            if (project != null)
            {
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
        }
        return novelChatClient.prompt()
            .system(systemPrompt.toString())
            .user(request.getMessage())
            .call()
            .content();
    }
}
