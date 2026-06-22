package com.ruoyi.novel.agent.prompts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.domain.NovelProject;
import com.ruoyi.novel.service.INovelProjectService;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;

/**
 * Agent 步骤 Prompt 模板
 */
@Service
public class NovelPromptTemplateService
{
    private static final String SKILL_BASE = "classpath:novel/skills/";

    @Autowired
    private INovelProjectService novelProjectService;

    public String buildSystemPrompt(NovelWorkflowRun run, NovelWorkflowStepCode stepCode)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业网络小说创作 Agent，请用中文工作。");
        sb.append("\n当前步骤：").append(stepCode.getLabel());
        sb.append("\n【交互模式】");
        sb.append("\n- 以对话方式与用户协作，每轮回复简洁清晰，可提问、可迭代修改。");
        sb.append("\n- 用户满意并确认前，步骤视为未完成；收到修改意见时继续调用工具更新。");
        sb.append("\n- 必须在适当时机调用工具写入数据库，不要只描述「将要做什么」。");
        sb.append("\n- 需要查库时，**同一轮内立刻**调用 getProjectInfo / getSetting，禁止只写「让我先确认数据库状态」而不调用工具。");
        sb.append("\n- 工具调用期间用户界面可能暂无文字输出，属正常现象；请继续完成全部工具调用后再给出总结。");
        appendSkill(sb, stepCode);
        if (run.getProjectId() != null)
        {
            NovelProject project = novelProjectService.selectNovelProjectByProjectId(run.getProjectId());
            if (project != null)
            {
                sb.append("\n【当前项目】").append(project.getTitle());
                if (StringUtils.isNotEmpty(project.getGenre()))
                {
                    sb.append("，类型：").append(project.getGenre());
                }
            }
        }
        return sb.toString();
    }

    public String buildUserPrompt(NovelWorkflowRun run, NovelWorkflowStepCode stepCode)
    {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(run.getUserIdea()))
        {
            sb.append("【用户创意】\n").append(run.getUserIdea()).append("\n\n");
        }
        switch (stepCode)
        {
            case INIT_PROJECT:
                sb.append("本环节：完善项目信息。先简要说明理解，与用户协作完善书名、类型、简介、风格指南，调用 updateProject 保存。可多次修改直到用户满意。");
                break;
            case WORLD_BUILDING:
                sb.append("本环节：世界观构建。**首轮必须先调用 getProjectInfo 和 getSetting(world)**，再输出完整 Markdown 并调用 saveSetting(world) 保存。禁止只回复开场白或口头说将要查库。");
                break;
            case CHARACTER_DESIGN:
                sb.append("本环节：角色设计。**首轮必须先调用 getSetting(world) 和 getSetting(characters)**，再设计角色并 saveSetting(characters) 保存，主要角色 saveMetaEntity。禁止只回复开场白。");
                break;
            case PLOT_OUTLINE:
                sb.append("本环节：故事大纲。**可先 getSetting(world/characters)** 再编写大纲，必须调用 saveSetting(outline) 保存。禁止只描述计划不调用工具。");
                break;
            case CHAPTER_PLANNING:
                sb.append("本环节：章节规划。根据大纲规划章节（至少3章），为每章调用 createChapter。可根据用户意见增删章节。");
                break;
            case WRITE_CHAPTER:
                sb.append("请为当前待写章节：先 getWritingContext，再撰写正文并 saveChapter，最后 extractMetaFromChapter。");
                break;
            case REVIEW_CHAPTER:
                sb.append("请对【待审查章节ID】调用 reviewChapterConsistency，对 Critical/Major 问题必须调用 saveSetting/saveMetaEntity 修复，修复后再次审查。最多 3 轮。");
                break;
            case FINAL_REVIEW:
                sb.append("请调用 reviewFullNovel 进行全书一致性审查，输出最终报告。");
                break;
            default:
                sb.append("请完成当前步骤任务。");
        }
        return sb.toString();
    }

    public String loadSkillContent(String skillDir)
    {
        try
        {
            Resource resource = new PathMatchingResourcePatternResolver()
                .getResource(SKILL_BASE + skillDir + "/SKILL.md");
            if (resource.exists())
            {
                return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            }
        }
        catch (IOException ignored)
        {
        }
        return "";
    }

    private void appendSkill(StringBuilder sb, NovelWorkflowStepCode stepCode)
    {
        String skillDir = mapSkillDir(stepCode);
        if (StringUtils.isEmpty(skillDir))
        {
            return;
        }
        String skill = loadSkillContent(skillDir);
        if (StringUtils.isNotEmpty(skill))
        {
            sb.append("\n\n【技能指南】\n").append(skill);
        }
    }

    private String mapSkillDir(NovelWorkflowStepCode stepCode)
    {
        switch (stepCode)
        {
            case WORLD_BUILDING:
                return "world-building";
            case CHARACTER_DESIGN:
                return "character-design";
            case PLOT_OUTLINE:
                return "plot-outline";
            case WRITE_CHAPTER:
                return "write-chapter";
            case REVIEW_CHAPTER:
                return "review-chapter";
            default:
                return null;
        }
    }
}
