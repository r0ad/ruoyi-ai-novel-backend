package com.ruoyi.novel.workflow.domain;

/**
 * 工作流步骤完成度（是否已写入业务数据）
 */
public class NovelWorkflowStepReadiness
{
    private boolean ready;

    private String hint;

    private String savedPreview;

    public boolean isReady()
    {
        return ready;
    }

    public void setReady(boolean ready)
    {
        this.ready = ready;
    }

    public String getHint()
    {
        return hint;
    }

    public void setHint(String hint)
    {
        this.hint = hint;
    }

    public String getSavedPreview()
    {
        return savedPreview;
    }

    public void setSavedPreview(String savedPreview)
    {
        this.savedPreview = savedPreview;
    }
}
