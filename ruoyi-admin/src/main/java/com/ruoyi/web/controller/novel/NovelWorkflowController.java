package com.ruoyi.web.controller.novel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.novel.ai.sse.WorkflowEventPublisher;
import com.ruoyi.novel.workflow.domain.NovelWorkflowConfirmRequest;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStartRequest;
import com.ruoyi.novel.workflow.service.INovelWorkflowService;

@RestController
@RequestMapping("/novel/workflow")
public class NovelWorkflowController extends BaseController
{
    @Autowired
    private INovelWorkflowService novelWorkflowService;

    @Autowired
    private WorkflowEventPublisher workflowEventPublisher;

    @PreAuthorize("@ss.hasPermi('novel:workflow:start')")
    @Log(title = "启动创作工作流", businessType = BusinessType.INSERT)
    @PostMapping("/run")
    public AjaxResult start(@RequestBody NovelWorkflowStartRequest request)
    {
        return success(novelWorkflowService.startWorkflow(request, SecurityUtils.getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('novel:workflow:view')")
    @GetMapping("/run/{runId}")
    public AjaxResult getRun(@PathVariable Long runId)
    {
        return success(novelWorkflowService.getRunDetail(runId));
    }

    @PreAuthorize("@ss.hasPermi('novel:workflow:view')")
    @GetMapping("/run/project/{projectId}")
    public AjaxResult getActiveByProject(@PathVariable Long projectId)
    {
        return success(novelWorkflowService.getActiveRunByProject(projectId));
    }

    @PreAuthorize("@ss.hasPermi('novel:workflow:confirm')")
    @Log(title = "确认工作流步骤", businessType = BusinessType.UPDATE)
    @PostMapping("/run/{runId}/confirm")
    public AjaxResult confirm(@PathVariable Long runId, @RequestBody(required = false) NovelWorkflowConfirmRequest request)
    {
        novelWorkflowService.confirmStep(runId, request, getUsername());
        return success(novelWorkflowService.getRunDetail(runId));
    }

    @PreAuthorize("@ss.hasPermi('novel:workflow:start')")
    @PostMapping("/run/{runId}/pause")
    public AjaxResult pause(@PathVariable Long runId)
    {
        novelWorkflowService.pauseRun(runId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('novel:workflow:start')")
    @PostMapping("/run/{runId}/resume")
    public AjaxResult resume(@PathVariable Long runId)
    {
        novelWorkflowService.resumeRun(runId);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('novel:workflow:start')")
    @PostMapping("/run/{runId}/retry-step")
    public AjaxResult retry(@PathVariable Long runId)
    {
        novelWorkflowService.retryCurrentStep(runId);
        return success(novelWorkflowService.getRunDetail(runId));
    }

    @PreAuthorize("@ss.hasPermi('novel:workflow:view')")
    @GetMapping(value = "/run/{runId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable Long runId)
    {
        return workflowEventPublisher.subscribe(runId);
    }
}
