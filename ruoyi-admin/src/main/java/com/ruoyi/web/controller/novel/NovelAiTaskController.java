package com.ruoyi.web.controller.novel;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.novel.ai.domain.dto.AiTaskApplyRequest;
import com.ruoyi.novel.ai.domain.dto.AiTaskCreateRequest;
import com.ruoyi.novel.ai.task.domain.NovelAiTask;
import com.ruoyi.novel.ai.task.service.INovelAiTaskService;
import com.ruoyi.novel.security.NovelProjectSecurity;

@RestController
@RequestMapping("/novel/ai/task")
public class NovelAiTaskController extends BaseController
{
    @Autowired
    private INovelAiTaskService novelAiTaskService;

    @Autowired
    private NovelProjectSecurity novelProjectSecurity;

    @PreAuthorize("@ss.hasPermi('novel:ai:task')")
    @Log(title = "创建AI任务", businessType = BusinessType.OTHER)
    @PostMapping
    public AjaxResult create(@RequestBody AiTaskCreateRequest request)
    {
        novelProjectSecurity.checkProject(request.getProjectId());
        NovelAiTask task = novelAiTaskService.createAndRun(request, getUsername());
        return success(task);
    }

    @PreAuthorize("@ss.hasPermi('novel:ai:task')")
    @GetMapping("/{taskId}")
    public AjaxResult get(@PathVariable Long taskId)
    {
        novelProjectSecurity.checkTask(taskId);
        return success(novelAiTaskService.getTask(taskId));
    }

    @PreAuthorize("@ss.hasPermi('novel:ai:task')")
    @GetMapping("/list")
    public AjaxResult list(NovelAiTask query)
    {
        if (query.getProjectId() != null)
        {
            novelProjectSecurity.checkProject(query.getProjectId());
        }
        // 非管理员仅能查看自己创建的任务
        if (!novelProjectSecurity.isAdmin())
        {
            query.setCreateBy(getUsername());
        }
        List<NovelAiTask> list = novelAiTaskService.listTasks(query);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('novel:ai:apply')")
    @Log(title = "应用AI任务结果", businessType = BusinessType.UPDATE)
    @PostMapping("/{taskId}/apply")
    public AjaxResult apply(@PathVariable Long taskId, @RequestBody(required = false) AiTaskApplyRequest request)
    {
        novelProjectSecurity.checkTask(taskId);
        if (request == null)
        {
            request = new AiTaskApplyRequest();
        }
        return success(novelAiTaskService.applyTask(taskId, request, getUsername()));
    }
}
