package com.ruoyi.novel.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.domain.NovelTemplate;
import com.ruoyi.novel.mapper.NovelSettingMapper;
import com.ruoyi.novel.mapper.NovelTemplateMapper;
import com.ruoyi.novel.service.INovelSettingService;

/**
 * 设定 服务层实现
 *
 * @author novel
 */
@Service
public class NovelSettingServiceImpl implements INovelSettingService
{
    private static final Map<String, String> DEFAULT_TITLES = new HashMap<String, String>();

    static
    {
        DEFAULT_TITLES.put("characters", "角色设定");
        DEFAULT_TITLES.put("world", "世界观");
        DEFAULT_TITLES.put("outline", "故事大纲");
        DEFAULT_TITLES.put("metrics", "创作指标");
        DEFAULT_TITLES.put("style", "风格指南");
        DEFAULT_TITLES.put("scene", "场景设定");
    }

    @Autowired
    private NovelSettingMapper novelSettingMapper;

    @Autowired
    private NovelTemplateMapper novelTemplateMapper;

    @Override
    public NovelSetting selectNovelSettingByProjectAndType(Long projectId, String settingType)
    {
        NovelSetting query = new NovelSetting();
        query.setProjectId(projectId);
        query.setSettingType(settingType);
        return novelSettingMapper.selectNovelSettingByProjectAndType(query);
    }

    @Override
    public List<NovelSetting> selectNovelSettingListByProjectId(Long projectId)
    {
        return novelSettingMapper.selectNovelSettingListByProjectId(projectId);
    }

    @Override
    public String selectSettingTemplateContent(String settingType)
    {
        NovelTemplate template = novelTemplateMapper.selectNovelTemplateByCode(settingType);
        if (template != null && StringUtils.isNotEmpty(template.getContent()))
        {
            return template.getContent();
        }
        return DEFAULT_TITLES.containsKey(settingType)
            ? "# " + DEFAULT_TITLES.get(settingType) + "\n\n"
            : "";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveNovelSetting(NovelSetting novelSetting)
    {
        if (novelSetting.getProjectId() == null || StringUtils.isEmpty(novelSetting.getSettingType()))
        {
            return 0;
        }
        if (StringUtils.isEmpty(novelSetting.getTitle()))
        {
            novelSetting.setTitle(DEFAULT_TITLES.getOrDefault(novelSetting.getSettingType(), novelSetting.getSettingType()));
        }
        if (novelSetting.getContent() == null)
        {
            novelSetting.setContent("");
        }
        NovelSetting existing = selectNovelSettingByProjectAndType(
            novelSetting.getProjectId(), novelSetting.getSettingType());
        if (existing == null)
        {
            novelSetting.setVersionNo(1);
            return novelSettingMapper.insertNovelSetting(novelSetting);
        }
        novelSetting.setSettingId(existing.getSettingId());
        int nextVersion = existing.getVersionNo() == null ? 1 : existing.getVersionNo() + 1;
        novelSetting.setVersionNo(nextVersion);
        return novelSettingMapper.updateNovelSetting(novelSetting);
    }
}