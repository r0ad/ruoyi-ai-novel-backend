package com.ruoyi.novel.workflow.domain;

import java.util.Date;

/**
 * Standardized workflow step output exposed to the workbench.
 */
public class NovelWorkflowStepArtifact
{
    private String artifactId;
    private Long runId;
    private Long stepId;
    private String stepCode;
    private String artifactType;
    private String title;
    private String contentMd;
    private String contentJson;
    private Integer version;
    private Long sourceRunId;
    private Date updateTime;

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    public Long getRunId()
    {
        return runId;
    }

    public void setRunId(Long runId)
    {
        this.runId = runId;
    }

    public Long getStepId()
    {
        return stepId;
    }

    public void setStepId(Long stepId)
    {
        this.stepId = stepId;
    }

    public String getStepCode()
    {
        return stepCode;
    }

    public void setStepCode(String stepCode)
    {
        this.stepCode = stepCode;
    }

    public String getArtifactType()
    {
        return artifactType;
    }

    public void setArtifactType(String artifactType)
    {
        this.artifactType = artifactType;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContentMd()
    {
        return contentMd;
    }

    public void setContentMd(String contentMd)
    {
        this.contentMd = contentMd;
    }

    public String getContentJson()
    {
        return contentJson;
    }

    public void setContentJson(String contentJson)
    {
        this.contentJson = contentJson;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public Long getSourceRunId()
    {
        return sourceRunId;
    }

    public void setSourceRunId(Long sourceRunId)
    {
        this.sourceRunId = sourceRunId;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
}
