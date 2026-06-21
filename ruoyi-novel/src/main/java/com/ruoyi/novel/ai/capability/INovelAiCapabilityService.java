package com.ruoyi.novel.ai.capability;

import com.ruoyi.novel.ai.context.ContextOptions;
import com.ruoyi.novel.ai.domain.dto.ExtractResult;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;
import com.ruoyi.novel.ai.domain.dto.SettingDraftResult;
import com.ruoyi.novel.ai.domain.dto.SyncSettingMetaResult;

public interface INovelAiCapabilityService
{
    ReviewResult reviewChapter(ContextOptions options);

    ReviewResult reviewProject(ContextOptions options);

    ExtractResult extractMetaPreview(ContextOptions options);

    SettingDraftResult extractSettingFromChapters(ContextOptions options);

    SettingDraftResult generateSetting(ContextOptions options);

    SyncSettingMetaResult syncSettingAndMeta(ContextOptions options);
}
