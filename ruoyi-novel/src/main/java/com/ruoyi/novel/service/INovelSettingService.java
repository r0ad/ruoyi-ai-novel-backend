package com.ruoyi.novel.service;

import java.util.List;
import com.ruoyi.novel.domain.NovelSetting;

/**
 * 设定 服务层
 *
 * @author novel
 */
public interface INovelSettingService
{
    public NovelSetting selectNovelSettingByProjectAndType(Long projectId, String settingType);

    public List<NovelSetting> selectNovelSettingListByProjectId(Long projectId);

    public String selectSettingTemplateContent(String settingType);

    public int saveNovelSetting(NovelSetting novelSetting);
}
