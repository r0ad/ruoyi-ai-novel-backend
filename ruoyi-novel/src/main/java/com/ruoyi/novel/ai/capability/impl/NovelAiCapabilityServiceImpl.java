package com.ruoyi.novel.ai.capability.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.capability.INovelAiCapabilityService;
import com.ruoyi.novel.ai.capability.NovelAiJsonParser;
import com.ruoyi.novel.ai.config.NovelAiModelFactory;
import com.ruoyi.novel.ai.context.ContextOptions;
import com.ruoyi.novel.ai.context.ProjectAiContext;
import com.ruoyi.novel.ai.context.ProjectContextBuilder;
import com.ruoyi.novel.ai.domain.dto.ExtractConflictItem;
import com.ruoyi.novel.ai.domain.dto.ExtractEntityItem;
import com.ruoyi.novel.ai.domain.dto.ExtractRelationItem;
import com.ruoyi.novel.ai.domain.dto.ExtractResult;
import com.ruoyi.novel.ai.domain.dto.ReviewIssue;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.service.INovelMetaService;

@Service
public class NovelAiCapabilityServiceImpl implements INovelAiCapabilityService
{
    @Autowired
    private NovelAiModelFactory novelAiModelFactory;

    @Autowired
    private ProjectContextBuilder projectContextBuilder;

    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private INovelMetaService novelMetaService;

    @Override
    public ReviewResult reviewChapter(ContextOptions options)
    {
        validateChapter(options);
        ProjectAiContext ctx = projectContextBuilder.buildReviewChapterContext(options);
        String raw = callAi(ctx.getSystemPrompt(), ctx.getUserPrompt());
        return parseReviewResult(raw, "review_chapter", options.getChapterId());
    }

    @Override
    public ReviewResult reviewProject(ContextOptions options)
    {
        if (options.getProjectId() == null)
        {
            throw new ServiceException("项目ID不能为空");
        }
        ProjectAiContext ctx = projectContextBuilder.buildReviewProjectContext(options);
        String raw = callAi(ctx.getSystemPrompt(), ctx.getUserPrompt());
        return parseReviewResult(raw, "review_project", null);
    }

    @Override
    public ExtractResult extractMetaPreview(ContextOptions options)
    {
        validateChapter(options);
        ProjectAiContext ctx = projectContextBuilder.buildExtractMetaContext(options);
        String raw = callAi(ctx.getSystemPrompt(), ctx.getUserPrompt());
        ExtractResult result = parseExtractResult(raw, options.getChapterId());
        markNewEntities(result, options.getProjectId());
        return result;
    }

    private void validateChapter(ContextOptions options)
    {
        if (options.getProjectId() == null || options.getChapterId() == null)
        {
            throw new ServiceException("项目ID与章节ID不能为空");
        }
        NovelChapter chapter = novelChapterService.selectNovelChapterByChapterId(options.getChapterId());
        if (chapter == null || !chapter.getProjectId().equals(options.getProjectId()))
        {
            throw new ServiceException("章节不存在");
        }
    }

    private String callAi(String system, String user)
    {
        ChatClient client = novelAiModelFactory.getChatClient();
        if (client == null)
        {
            throw new ServiceException("未配置激活的 AI 模型，请先在「AI模型管理」中添加并激活模型");
        }
        String content = client.prompt().system(system).user(user).call().content();
        if (StringUtils.isEmpty(content))
        {
            throw new ServiceException("AI 返回内容为空");
        }
        return content;
    }

    private ReviewResult parseReviewResult(String raw, String taskType, Long chapterId)
    {
        ReviewResult result = new ReviewResult();
        result.setTaskType(taskType);
        result.setRawText(raw);
        JSONObject json = NovelAiJsonParser.parseObject(raw);
        if (json == null)
        {
            result.setPassed(false);
            ReviewIssue issue = new ReviewIssue();
            issue.setId("iss-parse");
            issue.setSeverity("major");
            issue.setCategory("parse_error");
            issue.setSummary("AI 返回非结构化内容，请重试");
            issue.setAutoFixable(false);
            result.getIssues().add(issue);
            if (chapterId != null)
            {
                Map<String, Object> target = new HashMap<String, Object>();
                target.put("chapterId", chapterId);
                result.setTarget(target);
            }
            return result;
        }
        ReviewResult parsed = json.toJavaObject(ReviewResult.class);
        if (parsed.getIssues() != null)
        {
            result.setIssues(parsed.getIssues());
        }
        result.setPassed(result.getIssues() == null || result.getIssues().isEmpty());
        if (parsed.getTarget() != null)
        {
            result.setTarget(parsed.getTarget());
        }
        else if (chapterId != null)
        {
            Map<String, Object> target = new HashMap<String, Object>();
            target.put("chapterId", chapterId);
            result.setTarget(target);
        }
        assignIssueIds(result.getIssues());
        return result;
    }

    private ExtractResult parseExtractResult(String raw, Long chapterId)
    {
        ExtractResult result = new ExtractResult();
        result.setRawText(raw);
        Map<String, Object> target = new HashMap<String, Object>();
        target.put("chapterId", chapterId);
        result.setTarget(target);
        JSONObject json = NovelAiJsonParser.parseObject(raw);
        if (json == null)
        {
            return result;
        }
        ExtractResult parsed = json.toJavaObject(ExtractResult.class);
        if (parsed.getEntities() != null)
        {
            result.setEntities(parsed.getEntities());
        }
        if (parsed.getRelations() != null)
        {
            result.setRelations(parsed.getRelations());
        }
        if (parsed.getConflicts() != null)
        {
            result.setConflicts(parsed.getConflicts());
        }
        return result;
    }

    private void markNewEntities(ExtractResult result, Long projectId)
    {
        NovelMetaEntity query = new NovelMetaEntity();
        query.setProjectId(projectId);
        List<NovelMetaEntity> existing = novelMetaService.selectEntityList(query);
        for (ExtractEntityItem item : result.getEntities())
        {
            boolean found = false;
            for (NovelMetaEntity entity : existing)
            {
                if (item.getName() != null && item.getName().equals(entity.getName()))
                {
                    found = true;
                    break;
                }
            }
            item.setNew(!found);
        }
    }

    private void assignIssueIds(List<ReviewIssue> issues)
    {
        if (issues == null)
        {
            return;
        }
        for (int i = 0; i < issues.size(); i++)
        {
            ReviewIssue issue = issues.get(i);
            if (StringUtils.isEmpty(issue.getId()))
            {
                issue.setId("iss-" + (i + 1));
            }
        }
    }
}
