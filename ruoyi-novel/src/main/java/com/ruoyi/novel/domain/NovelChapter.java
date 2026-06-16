package com.ruoyi.novel.domain;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 章节元数据 novel_chapter
 *
 * @author novel
 */
public class NovelChapter extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long chapterId;

    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    private Long parentId;
    private Integer chapterNumber;

    @NotBlank(message = "章节标题不能为空")
    @Size(max = 200, message = "章节标题不能超过200个字符")
    private String title;

    private String summary;
    private Integer wordCount;
    private String status;
    private Integer sortOrder;
    private Integer versionNo;
    private String delFlag;

    /** 章节正文（关联 novel_chapter_content） */
    private String content;

    /** 子章节 */
    private List<NovelChapter> children;

    public Long getChapterId()
    {
        return chapterId;
    }

    public void setChapterId(Long chapterId)
    {
        this.chapterId = chapterId;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(Long projectId)
    {
        this.projectId = projectId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public Integer getChapterNumber()
    {
        return chapterNumber;
    }

    public void setChapterNumber(Integer chapterNumber)
    {
        this.chapterNumber = chapterNumber;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Integer getWordCount()
    {
        return wordCount;
    }

    public void setWordCount(Integer wordCount)
    {
        this.wordCount = wordCount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public List<NovelChapter> getChildren()
    {
        return children;
    }

    public void setChildren(List<NovelChapter> children)
    {
        this.children = children;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("chapterId", getChapterId())
            .append("projectId", getProjectId())
            .append("parentId", getParentId())
            .append("chapterNumber", getChapterNumber())
            .append("title", getTitle())
            .append("summary", getSummary())
            .append("wordCount", getWordCount())
            .append("status", getStatus())
            .append("sortOrder", getSortOrder())
            .append("versionNo", getVersionNo())
            .append("delFlag", getDelFlag())
            .append("children", getChildren())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
