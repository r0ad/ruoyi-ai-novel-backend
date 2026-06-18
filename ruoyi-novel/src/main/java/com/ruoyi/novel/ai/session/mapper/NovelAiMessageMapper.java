package com.ruoyi.novel.ai.session.mapper;

import java.util.List;
import com.ruoyi.novel.ai.session.domain.NovelAiMessage;

public interface NovelAiMessageMapper
{
    List<NovelAiMessage> selectMessagesBySessionId(Long sessionId);

    int insertNovelAiMessage(NovelAiMessage message);
}
