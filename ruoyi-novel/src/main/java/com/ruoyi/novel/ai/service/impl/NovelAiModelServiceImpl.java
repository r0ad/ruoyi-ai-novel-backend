package com.ruoyi.novel.ai.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.config.NovelAiKeyCrypto;
import com.ruoyi.novel.ai.config.NovelAiModelFactory;
import com.ruoyi.novel.ai.domain.NovelAiModel;
import com.ruoyi.novel.ai.mapper.NovelAiModelMapper;
import com.ruoyi.novel.ai.service.INovelAiModelService;
import com.ruoyi.novel.ai.utils.NovelAiKeyUtils;

@Service
public class NovelAiModelServiceImpl implements INovelAiModelService
{
    @Autowired
    private NovelAiModelMapper novelAiModelMapper;

    @Autowired
    private NovelAiKeyCrypto novelAiKeyCrypto;

    @Autowired
    private NovelAiModelFactory novelAiModelFactory;

    @Override
    public NovelAiModel selectNovelAiModelByModelId(Long modelId)
    {
        NovelAiModel model = novelAiModelMapper.selectNovelAiModelByModelId(modelId);
        return maskModel(model);
    }

    @Override
    public NovelAiModel selectActiveNovelAiModel()
    {
        NovelAiModel model = novelAiModelMapper.selectActiveNovelAiModel();
        return maskModel(model);
    }

    @Override
    public List<NovelAiModel> selectNovelAiModelList(NovelAiModel novelAiModel)
    {
        List<NovelAiModel> list = novelAiModelMapper.selectNovelAiModelList(novelAiModel);
        for (NovelAiModel item : list)
        {
            maskModel(item);
        }
        return list;
    }

    @Override
    public int insertNovelAiModel(NovelAiModel novelAiModel)
    {
        validateModel(novelAiModel, true);
        applyDefaults(novelAiModel);
        novelAiModel.setApiKey(novelAiKeyCrypto.encrypt(novelAiModel.getApiKey()));
        novelAiModel.setIsActive(StringUtils.defaultIfEmpty(novelAiModel.getIsActive(), "0"));
        novelAiModel.setStatus(StringUtils.defaultIfEmpty(novelAiModel.getStatus(), "0"));
        int rows = novelAiModelMapper.insertNovelAiModel(novelAiModel);
        if ("1".equals(novelAiModel.getIsActive()))
        {
            activateModel(novelAiModel.getModelId());
        }
        return rows;
    }

    @Override
    public int updateNovelAiModel(NovelAiModel novelAiModel)
    {
        if (novelAiModel.getModelId() == null)
        {
            throw new ServiceException("???ID???????");
        }
        NovelAiModel existing = novelAiModelMapper.selectNovelAiModelByModelId(novelAiModel.getModelId());
        if (existing == null)
        {
            throw new ServiceException("??????¨°?????");
        }
        validateModel(novelAiModel, false);
        if (NovelAiKeyUtils.isMaskedValue(novelAiModel.getApiKey()))
        {
            novelAiModel.setApiKey(null);
        }
        else
        {
            novelAiModel.setApiKey(novelAiKeyCrypto.encrypt(novelAiModel.getApiKey()));
        }
        int rows = novelAiModelMapper.updateNovelAiModel(novelAiModel);
        NovelAiModel latest = novelAiModelMapper.selectNovelAiModelByModelId(novelAiModel.getModelId());
        if (latest != null && "1".equals(latest.getIsActive()))
        {
            novelAiModelFactory.reload();
        }
        return rows;
    }

    @Override
    public int deleteNovelAiModelByModelIds(Long[] modelIds)
    {
        for (Long modelId : modelIds)
        {
            NovelAiModel model = novelAiModelMapper.selectNovelAiModelByModelId(modelId);
            if (model != null && "1".equals(model.getIsActive()))
            {
                throw new ServiceException("???????????????????" + model.getModelName());
            }
        }
        return novelAiModelMapper.deleteNovelAiModelByModelIds(modelIds);
    }

    @Override
    @Transactional
    public int activateModel(Long modelId)
    {
        NovelAiModel model = novelAiModelMapper.selectNovelAiModelByModelId(modelId);
        if (model == null)
        {
            throw new ServiceException("??????¨°?????");
        }
        if ("1".equals(model.getStatus()))
        {
            throw new ServiceException("???????????????");
        }
        novelAiModelMapper.deactivateAllModels();
        int rows = novelAiModelMapper.activateModel(modelId);
        novelAiModelFactory.reload();
        return rows;
    }

    @Override
    public String testConnection(Long modelId)
    {
        NovelAiModel model = novelAiModelMapper.selectNovelAiModelByModelId(modelId);
        if (model == null)
        {
            throw new ServiceException("??????¨°?????");
        }
        String plainKey = novelAiKeyCrypto.decrypt(model.getApiKey());
        return novelAiModelFactory.testModel(model, plainKey);
    }

    @Override
    public String testConnection(NovelAiModel novelAiModel)
    {
        validateModel(novelAiModel, true);
        applyDefaults(novelAiModel);
        return novelAiModelFactory.testModel(novelAiModel, novelAiModel.getApiKey());
    }

    private void validateModel(NovelAiModel model, boolean requireApiKey)
    {
        if (model == null)
        {
            throw new ServiceException("??????¨°??????");
        }
        if (StringUtils.isEmpty(model.getModelName()))
        {
            throw new ServiceException("?????????????");
        }
        if (StringUtils.isEmpty(model.getProviderType()))
        {
            throw new ServiceException("§¿????????????");
        }
        if (StringUtils.isEmpty(model.getBaseUrl()))
        {
            throw new ServiceException("Base URL ???????");
        }
        if (StringUtils.isEmpty(model.getModelCode()))
        {
            throw new ServiceException("????????????");
        }
        if (requireApiKey && StringUtils.isEmpty(model.getApiKey()))
        {
            throw new ServiceException("API Key ???????");
        }
        String provider = model.getProviderType().toLowerCase();
        if (!NovelAiModel.PROVIDER_OPENAI.equals(provider) && !NovelAiModel.PROVIDER_ANTHROPIC.equals(provider))
        {
            throw new ServiceException("??????§¿???????" + model.getProviderType());
        }
    }

    private void applyDefaults(NovelAiModel model)
    {
        if (model.getTemperature() == null)
        {
            model.setTemperature(new BigDecimal("0.70"));
        }
        if (model.getMaxTokens() == null)
        {
            model.setMaxTokens(4096);
        }
        if (model.getTimeoutMs() == null)
        {
            model.setTimeoutMs(60000);
        }
        if (model.getSortOrder() == null)
        {
            model.setSortOrder(0);
        }
    }

    private NovelAiModel maskModel(NovelAiModel model)
    {
        if (model == null)
        {
            return null;
        }
        if (StringUtils.isNotEmpty(model.getApiKey()))
        {
            try
            {
                model.setApiKey(novelAiKeyCrypto.mask(novelAiKeyCrypto.decrypt(model.getApiKey())));
            }
            catch (Exception ex)
            {
                model.setApiKey(NovelAiKeyUtils.mask(model.getApiKey()));
            }
        }
        return model;
    }
}
