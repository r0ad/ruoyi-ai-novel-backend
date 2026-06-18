package com.ruoyi.novel.ai.session.mapper;

import com.ruoyi.novel.ai.session.domain.NovelAiSession;

public interface NovelAiSessionMapper
{
    NovelAiSession selectNovelAiSessionBySessionId(Long sessionId);

    int insertNovelAiSession(NovelAiSession session);

    int updateNovelAiSession(NovelAiSession session);
}
