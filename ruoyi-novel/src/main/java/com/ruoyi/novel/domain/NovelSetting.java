package com.ruoyi.novel.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 设定文档 novel_setting
 *
 * @author novel
 */
public class NovelSetting extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long settingId;
    private Long projectId;
    private String settingType;
    private String title;
    private String content;
    private String contentHtml;
    private Integer versionNo;

    public Long getSettingId()
    {
        return settingId;
    }

    public void setSettingId(Long settingId)
    {
        this.settingId = settingId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public String getSettingType()
    {
        return settingType;
    }

    public void setSettingType(String settingType)
    {
        this.settingType = settingType;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getContentHtml()
    {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml)
    {
        this.contentHtml = contentHtml;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
    }
}
