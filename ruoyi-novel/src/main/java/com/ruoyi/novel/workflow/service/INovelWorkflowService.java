package com.ruoyi.novel.workflow.service;

import com.ruoyi.novel.workflow.domain.NovelWorkflowConfirmRequest;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRunDetail;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStartRequest;

public interface INovelWorkflowService
{
    NovelWorkflowRun startWorkflow(NovelWorkflowStartRequest request, Long userId, String username);

    NovelWorkflowRunDetail getRunDetail(Long runId);

    NovelWorkflowRunDetail getActiveRunByProject(Long projectId);

    void confirmStep(Long runId, NovelWorkflowConfirmRequest request, String username);

    void pauseRun(Long runId);

    void resumeRun(Long runId);

    void retryCurrentStep(Long runId);
}
