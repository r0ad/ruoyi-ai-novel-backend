package com.ruoyi.novel.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.service.INovelProjectService;
import com.ruoyi.novel.service.INovelSettingService;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStepReadiness;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;

@Component
public class NovelWorkflowStepValidator
{
    private static final int MIN_SETTING_CHARS = 200;

    private static final int MIN_CHAPTER_COUNT = 3;

    public static boolean isInteractiveStep(NovelWorkflowStepCode stepCode)
    {
        if (stepCode == null)
        {
            return false;
        }
        return stepCode == NovelWorkflowStepCode.INIT_PROJECT
            || stepCode == NovelWorkflowStepCode.WORLD_BUILDING
            || stepCode == NovelWorkflowStepCode.CHARACTER_DESIGN
            || stepCode == NovelWorkflowStepCode.PLOT_OUTLINE
            || stepCode == NovelWorkflowStepCode.CHAPTER_PLANNING;
    }

    @Autowired
    private INovelSettingService novelSettingService;

    @Autowired
    private INovelProjectService novelProjectService;

    @Autowired
    private INovelChapterService novelChapterService;

    public NovelWorkflowStepReadiness evaluate(Long projectId, NovelWorkflowStepCode stepCode)
    {
        NovelWorkflowStepReadiness readiness = new NovelWorkflowStepReadiness();
        if (projectId == null || stepCode == null)
        {
            readiness.setReady(false);
            readiness.setHint("项目或步骤无效");
            return readiness;
        }
        switch (stepCode)
        {
            case INIT_PROJECT:
                return evaluateProject(projectId);
            case WORLD_BUILDING:
                return evaluateSetting(projectId, "world", "世界观");
            case CHARACTER_DESIGN:
                return evaluateSetting(projectId, "characters", "角色档案");
            case PLOT_OUTLINE:
                return evaluateSetting(projectId, "outline", "故事大纲");
            case CHAPTER_PLANNING:
                return evaluateChapters(projectId);
            default:
                readiness.setReady(true);
                readiness.setHint("");
                return readiness;
        }
    }

    private NovelWorkflowStepReadiness evaluateProject(Long projectId)
    {
        NovelWorkflowStepReadiness readiness = new NovelWorkflowStepReadiness();
        NovelProject project = novelProjectService.selectNovelProjectByProjectId(projectId);
        if (project == null)
        {
            readiness.setReady(false);
            readiness.setHint("项目不存在，请让 Agent 调用 updateProject 保存书名、类型与简介");
            return readiness;
        }
        boolean ok = StringUtils.isNotEmpty(project.getTitle())
            && StringUtils.isNotEmpty(project.getSummary())
            && project.getSummary().length() >= 20;
        readiness.setReady(ok);
        readiness.setHint(ok ? "" : "项目信息不完整，请通过对话让 Agent 调用 updateProject 完善并保存");
        if (ok)
        {
            readiness.setSavedPreview(project.getTitle() + "\n\n" + project.getSummary());
        }
        return readiness;
    }

    private NovelWorkflowStepReadiness evaluateSetting(Long projectId, String type, String label)
    {
        NovelWorkflowStepReadiness readiness = new NovelWorkflowStepReadiness();
        NovelSetting setting = novelSettingService.selectNovelSettingByProjectAndType(projectId, type);
        String content = setting != null ? setting.getContent() : null;
        boolean ok = StringUtils.isNotEmpty(content) && content.trim().length() >= MIN_SETTING_CHARS;
        readiness.setReady(ok);
        readiness.setHint(ok ? ""
            : label + "尚未写入数据库。请继续与 Agent 对话，要求其调用 saveSetting(" + type + ") 保存完整 Markdown，不要只输出计划");
        if (ok)
        {
            readiness.setSavedPreview(truncate(content, 1200));
        }
        return readiness;
    }

    private NovelWorkflowStepReadiness evaluateChapters(Long projectId)
    {
        NovelWorkflowStepReadiness readiness = new NovelWorkflowStepReadiness();
        NovelChapter query = new NovelChapter();
        query.setProjectId(projectId);
        int count = novelChapterService.selectNovelChapterList(query).size();
        boolean ok = count >= MIN_CHAPTER_COUNT;
        readiness.setReady(ok);
        readiness.setHint(ok ? ""
            : "章节规划不足（当前 " + count + " 章，至少需要 " + MIN_CHAPTER_COUNT
                + " 章）。请继续对话，让 Agent 调用 createChapter 创建章节骨架");
        if (ok)
        {
            readiness.setSavedPreview("已规划 " + count + " 个章节");
        }
        return readiness;
    }

    private String truncate(String text, int max)
    {
        if (text == null)
        {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max) + "\n…";
    }
}
