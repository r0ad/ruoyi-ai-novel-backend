package com.ruoyi.novel.workflow.service;

import java.util.List;
import com.ruoyi.novel.ai.session.domain.NovelAiMessage;
import com.ruoyi.novel.workflow.domain.NovelWorkflowChatRequest;
import com.ruoyi.novel.workflow.domain.NovelWorkflowConfirmRequest;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRunDetail;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStartRequest;

public interface INovelWorkflowService
{
    NovelWorkflowRun startWorkflow(NovelWorkflowStartRequest request, Long userId, String username);

    NovelWorkflowRunDetail getRunDetail(Long runId);

    NovelWorkflowRunDetail getRunDetail(Long runId, String messageStepCode);

    List<NovelAiMessage> getStepMessages(Long runId, String stepCode);

    NovelWorkflowRunDetail getActiveRunByProject(Long projectId);

    void confirmStep(Long runId, NovelWorkflowConfirmRequest request, String username);

    void pauseRun(Long runId);

    void resumeRun(Long runId);

    void retryCurrentStep(Long runId);

    void chatInCurrentStep(Long runId, NovelWorkflowChatRequest request);
}
