package com.ruoyi.novel.ai.service;

import com.ruoyi.novel.ai.domain.NovelAiChatRequest;
import reactor.core.publisher.Flux;

/**
 * 小说 AI 服务
 *
 * @author novel
 */
public interface INovelAiService
{
    public String chat(NovelAiChatRequest request);

    public Flux<String> continueStream(NovelAiChatRequest request);
}
