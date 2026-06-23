package com.ruoyi.novel.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 小说项目 novel_project
 *
 * @author novel
 */
public class NovelProject extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 项目ID */
    private Long projectId;

    /** 对外UUID */
    @Excel(name = "项目UUID")
    private String projectUuid;

    /** 归属用户ID */
    private Long userId;

    /** 书名 */
    @Excel(name = "书名")
    private String title;

    /** 类型 */
    @Excel(name = "类型")
    private String genre;

    /** 状态（0草稿 1连载 2完结） */
    @Excel(name = "状态", readConverterExp = "0=草稿,1=连载,2=完结")
    private String status;

    /** 总字数 */
    @Excel(name = "总字数")
    private Long wordCount;

    /** 章节数 */
    @Excel(name = "章节数")
    private Integer chapterCount;

    /** 封面地址 */
    private String coverUrl;

    /** 简介 */
    private String summary;

    /** 风格指南 */
    private String styleGuide;

    /** 删除标志 */
    private String delFlag;

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public String getProjectUuid()
    {
        return projectUuid;
    }

    public void setProjectUuid(String projectUuid)
    {
        this.projectUuid = projectUuid;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名不能超过200个字符")
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getGenre()
    {
        return genre;
    }

    public void setGenre(String genre)
    {
        this.genre = genre;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getWordCount()
    {
        return wordCount;
    }

    public void setWordCount(Long wordCount)
    {
        this.wordCount = wordCount;
    }

    public Integer getChapterCount()
    {
        return chapterCount;
    }

    public void setChapterCount(Integer chapterCount)
    {
        this.chapterCount = chapterCount;
    }

    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getStyleGuide()
    {
        return styleGuide;
    }

    public void setStyleGuide(String styleGuide)
    {
        this.styleGuide = styleGuide;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("projectId", getProjectId())
            .append("projectUuid", getProjectUuid())
            .append("userId", getUserId())
            .append("title", getTitle())
            .append("genre", getGenre())
            .append("status", getStatus())
            .append("wordCount", getWordCount())
            .append("chapterCount", getChapterCount())
            .append("coverUrl", getCoverUrl())
            .append("summary", getSummary())
            .append("styleGuide", getStyleGuide())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
