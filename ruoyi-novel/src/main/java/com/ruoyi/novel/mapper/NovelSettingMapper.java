package com.ruoyi.novel.mapper;

import java.util.List;
import com.ruoyi.novel.domain.NovelSetting;

/**
 * 设定文档 数据层
 *
 * @author novel
 */
public interface NovelSettingMapper
{
    public NovelSetting selectNovelSettingBySettingId(Long settingId);

    public NovelSetting selectNovelSettingByProjectAndType(NovelSetting novelSetting);

    public List<NovelSetting> selectNovelSettingListByProjectId(Long projectId);

    public int insertNovelSetting(NovelSetting novelSetting);

    public int updateNovelSetting(NovelSetting novelSetting);
}