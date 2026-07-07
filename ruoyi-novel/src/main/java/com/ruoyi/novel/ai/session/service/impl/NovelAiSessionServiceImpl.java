package com.ruoyi.novel.ai.session.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.session.domain.NovelAiMessage;
import com.ruoyi.novel.ai.session.domain.NovelAiSession;
import com.ruoyi.novel.ai.session.mapper.NovelAiMessageMapper;
import com.ruoyi.novel.ai.session.mapper.NovelAiSessionMapper;
import com.ruoyi.novel.ai.session.service.INovelAiSessionService;

@Service
public class NovelAiSessionServiceImpl implements INovelAiSessionService
{
    @Autowired
    private NovelAiSessionMapper novelAiSessionMapper;

    @Autowired
    private NovelAiMessageMapper novelAiMessageMapper;

    @Override
    public NovelAiSession createSession(Long projectId, Long userId, String sessionType, String title, Long chapterId)
    {
        NovelAiSession session = new NovelAiSession();
        session.setProjectId(projectId);
        session.setUserId(userId);
        session.setSessionType(sessionType);
        session.setTitle(title);
        session.setChapterId(chapterId);
        novelAiSessionMapper.insertNovelAiSession(session);
        return session;
    }

    @Override
    public void appendMessage(Long sessionId, String role, String content)
    {
        NovelAiMessage message = new NovelAiMessage();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setTokenUsage(0);
        novelAiMessageMapper.insertNovelAiMessage(message);
        NovelAiSession session = new NovelAiSession();
        session.setSessionId(sessionId);
        novelAiSessionMapper.updateNovelAiSession(session);
    }

    @Override
    public boolean appendMessageIfNotDuplicate(Long sessionId, String role, String content)
    {
        List<NovelAiMessage> messages = listMessages(sessionId);
        if (messages != null && !messages.isEmpty())
        {
            NovelAiMessage last = messages.get(messages.size() - 1);
            if (last != null
                && StringUtils.equals(trimToEmpty(last.getRole()), trimToEmpty(role))
                && StringUtils.equals(trimToEmpty(last.getContent()), trimToEmpty(content)))
            {
                return false;
            }
        }
        appendMessage(sessionId, role, content);
        return true;
    }

    private String trimToEmpty(String value)
    {
        return value == null ? "" : value.trim();
    }

    @Override
    public List<NovelAiMessage> listMessages(Long sessionId)
    {
        return novelAiMessageMapper.selectMessagesBySessionId(sessionId);
    }

    @Override
    public NovelAiSession getSession(Long sessionId)
    {
        return novelAiSessionMapper.selectNovelAiSessionBySessionId(sessionId);
    }
}
