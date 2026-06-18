package com.ruoyi.novel.ai.session.service;

import java.util.List;
import com.ruoyi.novel.ai.session.domain.NovelAiMessage;
import com.ruoyi.novel.ai.session.domain.NovelAiSession;

public interface INovelAiSessionService
{
    NovelAiSession createSession(Long projectId, Long userId, String sessionType, String title, Long chapterId);

    void appendMessage(Long sessionId, String role, String content);

    List<NovelAiMessage> listMessages(Long sessionId);

    NovelAiSession getSession(Long sessionId);
}
