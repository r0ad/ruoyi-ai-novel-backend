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
        sb.append("\n你必须优先调用提供的工具完成数据写入，然后再输出步骤总结。");
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
                sb.append("请根据用户创意完善项目信息（书名、类型、简介、风格指南），调用 updateProject 工具保存。");
                break;
            case WORLD_BUILDING:
                sb.append("请构建完整世界观设定，调用 saveSetting(settingType=world) 保存 Markdown。");
                break;
            case CHARACTER_DESIGN:
                sb.append("请设计主要角色，调用 saveSetting(characters) 保存角色档案，并为每个主要角色调用 saveMetaEntity。");
                break;
            case PLOT_OUTLINE:
                sb.append("请编写全书故事大纲，调用 saveSetting(outline) 保存。");
                break;
            case CHAPTER_PLANNING:
                sb.append("请根据大纲规划前若干章节（至少3章），为每章调用 createChapter，包含标题与摘要。");
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
