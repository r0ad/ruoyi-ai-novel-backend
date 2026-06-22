package com.ruoyi.novel.workflow;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.apply.FixPlanGenerator;
import com.ruoyi.novel.ai.capability.INovelAiCapabilityService;
import com.ruoyi.novel.ai.context.ContextOptions;
import com.ruoyi.novel.ai.domain.dto.ReviewResult;
import com.ruoyi.novel.domain.NovelChapter;
import com.ruoyi.novel.service.INovelChapterService;
import com.ruoyi.novel.workflow.domain.NovelWorkflowContext;
import com.ruoyi.novel.workflow.domain.NovelWorkflowRun;
import com.ruoyi.novel.workflow.domain.NovelWorkflowStep;
import com.ruoyi.novel.workflow.enums.NovelWorkflowRunStatus;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepCode;
import com.ruoyi.novel.workflow.enums.NovelWorkflowStepStatus;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowRunMapper;
import com.ruoyi.novel.workflow.mapper.NovelWorkflowStepMapper;

/**
 * 工作流状态机引擎
 */
@Component
public class NovelWorkflowEngine
{
    @Autowired
    private NovelWorkflowRunMapper novelWorkflowRunMapper;

    @Autowired
    private NovelWorkflowStepMapper novelWorkflowStepMapper;

    @Autowired
    private NovelWorkflowStepRunner novelWorkflowStepRunner;

    @Autowired
    private INovelChapterService novelChapterService;

    @Autowired
    private INovelAiCapabilityService novelAiCapabilityService;

    @Autowired
    private FixPlanGenerator fixPlanGenerator;

    private static final int MAX_REVIEW_FIX_ROUNDS = 3;

    public NovelWorkflowStep startStep(NovelWorkflowRun run, NovelWorkflowStepCode stepCode)
    {
        NovelWorkflowStep step = new NovelWorkflowStep();
        step.setRunId(run.getRunId());
        step.setStepCode(stepCode.getCode());
        step.setStatus(NovelWorkflowStepStatus.RUNNING.getCode());
        step.setStartedAt(new Date());
        novelWorkflowStepMapper.insertNovelWorkflowStep(step);

        run.setCurrentStep(stepCode.getCode());
        run.setStatus(NovelWorkflowRunStatus.RUNNING.getCode());
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);

        novelWorkflowStepRunner.executeAsync(run.getRunId(), step.getStepId(), stepCode);
        return step;
    }

    public NovelWorkflowStepCode resolveNextStep(NovelWorkflowRun run, NovelWorkflowStepCode current)
    {
        if (current == NovelWorkflowStepCode.WRITE_CHAPTER)
        {
            NovelWorkflowContext ctx = parseContext(run);
            refreshChapterIds(ctx, run.getProjectId());
            if (ctx.getCurrentChapterIndex() < ctx.getChapterIds().size())
            {
                saveContext(run, ctx);
                return NovelWorkflowStepCode.REVIEW_CHAPTER;
            }
            return NovelWorkflowStepCode.FINAL_REVIEW;
        }
        if (current == NovelWorkflowStepCode.REVIEW_CHAPTER)
        {
            NovelWorkflowContext ctx = parseContext(run);
            refreshChapterIds(ctx, run.getProjectId());
            Long chapterId = resolveChapterIdAtIndex(ctx, ctx.getCurrentChapterIndex());
            if (chapterId != null)
            {
                ContextOptions options = new ContextOptions();
                options.setProjectId(run.getProjectId());
                options.setChapterId(chapterId);
                ReviewResult review = novelAiCapabilityService.reviewChapter(options);
                ctx.setLastReviewResultJson(JSON.toJSONString(review));
                ctx.setReviewPassed(review.isPassed());
                ctx.setLastReviewChapterId(chapterId);
                if (!review.isPassed() && fixPlanGenerator.hasCriticalOrMajor(review)
                    && ctx.getReviewFixRound() < MAX_REVIEW_FIX_ROUNDS)
                {
                    ctx.setReviewFixRound(ctx.getReviewFixRound() + 1);
                    saveContext(run, ctx);
                    return NovelWorkflowStepCode.REVIEW_CHAPTER;
                }
            }
            ctx.setReviewFixRound(0);
            ctx.setReviewPassed(true);
            ctx.setCurrentChapterIndex(ctx.getCurrentChapterIndex() + 1);
            saveContext(run, ctx);
            if (ctx.getCurrentChapterIndex() < ctx.getChapterIds().size())
            {
                return NovelWorkflowStepCode.WRITE_CHAPTER;
            }
            return NovelWorkflowStepCode.FINAL_REVIEW;
        }
        if (current == NovelWorkflowStepCode.CHAPTER_PLANNING)
        {
            return null;
        }
        return current.next();
    }

    public void afterChapterPlanning(NovelWorkflowRun run)
    {
        NovelWorkflowContext ctx = parseContext(run);
        refreshChapterIds(ctx, run.getProjectId());
        ctx.setCurrentChapterIndex(0);
        saveContext(run, ctx);
    }

    private void refreshChapterIds(NovelWorkflowContext ctx, Long projectId)
    {
        NovelChapter query = new NovelChapter();
        query.setProjectId(projectId);
        java.util.List<NovelChapter> chapters = novelChapterService.selectNovelChapterList(query);
        chapters.sort(java.util.Comparator.comparingInt(c -> c.getChapterNumber() != null ? c.getChapterNumber() : 0));
        java.util.List<Long> ids = new java.util.ArrayList<Long>();
        for (NovelChapter ch : chapters)
        {
            ids.add(ch.getChapterId());
        }
        ctx.setChapterIds(ids);
    }

    public NovelWorkflowContext parseContext(NovelWorkflowRun run)
    {
        if (StringUtils.isEmpty(run.getContextJson()))
        {
            return new NovelWorkflowContext();
        }
        return JSON.parseObject(run.getContextJson(), NovelWorkflowContext.class);
    }

    public void saveContext(NovelWorkflowRun run, NovelWorkflowContext ctx)
    {
        run.setContextJson(JSON.toJSONString(ctx));
        novelWorkflowRunMapper.updateNovelWorkflowRun(run);
    }

    public Long resolveChapterIdAtIndex(NovelWorkflowContext ctx, int index)
    {
        if (ctx.getChapterIds() == null || index < 0 || index >= ctx.getChapterIds().size())
        {
            return null;
        }
        return ctx.getChapterIds().get(index);
    }

    public void ensureWaitingConfirm(NovelWorkflowRun run)
    {
        if (!NovelWorkflowRunStatus.WAITING_CONFIRM.getCode().equals(run.getStatus()))
        {
            throw new ServiceException("当前运行不在待确认状态");
        }
    }
}
