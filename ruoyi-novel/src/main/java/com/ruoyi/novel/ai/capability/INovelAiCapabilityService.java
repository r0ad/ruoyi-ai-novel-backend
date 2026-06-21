package com.ruoyi.novel.ai.capability;

import com.ruoyi.novel.ai.context.ContextOptions;
import com.ruoyi.novel.ai.domain.dto.ExtractResult;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;

public interface INovelAiCapabilityService
{
    ReviewResult reviewChapter(ContextOptions options);

    ReviewResult reviewProject(ContextOptions options);

    ExtractResult extractMetaPreview(ContextOptions options);
}
