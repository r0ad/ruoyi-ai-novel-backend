package com.ruoyi.novel.ai.service;

import java.util.List;
import com.ruoyi.novel.ai.domain.NovelAiModel;

/**
 * AI ƒ£–Õ≈‰÷√ Service
 */
public interface INovelAiModelService
{
    public NovelAiModel selectNovelAiModelByModelId(Long modelId);

    public NovelAiModel selectActiveNovelAiModel();

    public List<NovelAiModel> selectNovelAiModelList(NovelAiModel novelAiModel);

    public int insertNovelAiModel(NovelAiModel novelAiModel);

    public int updateNovelAiModel(NovelAiModel novelAiModel);

    public int deleteNovelAiModelByModelIds(Long[] modelIds);

    public int activateModel(Long modelId);

    public String testConnection(Long modelId);

    public String testConnection(NovelAiModel novelAiModel);
}
