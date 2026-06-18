package com.ruoyi.novel.ai.mapper;

import java.util.List;
import com.ruoyi.novel.ai.domain.NovelAiModel;

/**
 * AI 模型配置 Mapper
 */
public interface NovelAiModelMapper
{
    public NovelAiModel selectNovelAiModelByModelId(Long modelId);

    public NovelAiModel selectActiveNovelAiModel();

    public List<NovelAiModel> selectNovelAiModelList(NovelAiModel novelAiModel);

    public int insertNovelAiModel(NovelAiModel novelAiModel);

    public int updateNovelAiModel(NovelAiModel novelAiModel);

    public int deleteNovelAiModelByModelId(Long modelId);

    public int deleteNovelAiModelByModelIds(Long[] modelIds);

    public int deactivateAllModels();

    public int activateModel(Long modelId);
}
