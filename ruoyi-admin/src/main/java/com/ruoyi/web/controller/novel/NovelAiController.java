package com.ruoyi.web.controller.novel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.novel.ai.context.ProjectContextBuilder;
import com.ruoyi.novel.ai.domain.NovelAiChatRequest;
import com.ruoyi.novel.ai.invocation.INovelAiInvocationService;
import com.ruoyi.novel.ai.session.domain.NovelAiSession;
import com.ruoyi.novel.ai.session.service.INovelAiSessionService;
import com.ruoyi.novel.ai.service.INovelAiService;
import com.ruoyi.novel.service.INovelChapterService;
import reactor.core.publisher.Flux;

/**
 * 小说 AI 助手 Controller
 */
@RestController
@RequestMapping("/novel/ai")
public class NovelAiController extends BaseController
{
    @Autowired
    private INovelAiService novelAiService;

    @Autowired
    private INovelAiSessionService novelAiSessionService;

    @Autowired
    private INovelAiInvocationService novelAiInvocationService;

    @Autowired
    private ProjectContextBuilder projectContextBuilder;

    @Autowired
    private INovelChapterService novelChapterService;

    @PreAuthorize("@ss.hasPermi('novel:ai:chat')")
    @Log(title = "小说AI对话", businessType = BusinessType.OTHER)
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody NovelAiChatRequest request)
    {
        String content = novelAiService.chat(request);
        return success(Collections.singletonMap("content", content));
    }

    @PreAuthorize("@ss.hasPermi('novel:ai:continue')")
    @Log(title = "小说AI续写", businessType = BusinessType.OTHER)
    @PostMapping(value = "/continue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> continueWriting(@RequestBody NovelAiChatRequest request)
    {
        return novelAiService.continueStream(request);
    }

    @PreAuthorize("@ss.hasPermi('novel:ai:review')")
    @Log(title = "小说AI审查", businessType = BusinessType.OTHER)
    @PostMapping("/review")
    public AjaxResult review(@RequestBody Map<String, Long> body)
    {
        Long projectId = body.get("projectId");
        Long chapterId = body.get("chapterId");
        String content = chapterId != null
            ? novelAiService.reviewChapter(projectId, chapterId)
            : novelAiService.reviewProject(projectId);
        return success(Collections.singletonMap("content", content));
    }

    @PreAuthorize("@ss.hasPermi('novel:ai:review')")
    @Log(title = "Meta抽取", businessType = BusinessType.OTHER)
    @PostMapping("/extract-meta")
    public AjaxResult extractMeta(@RequestBody Map<String, Long> body)
    {
        String content = novelAiService.extractMeta(body.get("projectId"), body.get("chapterId"));
        return success(Collections.singletonMap("content", content));
    }

    @PreAuthorize("@ss.hasPermi('novel:ai:chat')")
    @PostMapping("/session")
    public AjaxResult createSession(@RequestBody Map<String, Object> body)
    {
        Long projectId = Long.valueOf(body.get("projectId").toString());
        String sessionType = body.get("sessionType") != null ? body.get("sessionType").toString() : "qa";
        String title = body.get("title") != null ? body.get("title").toString() : "AI会话";
        Long chapterId = body.get("chapterId") != null ? Long.valueOf(body.get("chapterId").toString()) : null;
        NovelAiSession session = novelAiSessionService.createSession(
            projectId, com.ruoyi.common.utils.SecurityUtils.getUserId(), sessionType, title, chapterId);
        return success(session);
    }

    @PreAuthorize("@ss.hasPermi('novel:ai:chat')")
    @GetMapping("/session/{sessionId}/messages")
    public AjaxResult listMessages(@PathVariable Long sessionId)
    {
        return success(novelAiSessionService.listMessages(sessionId));
    }

    /**
     * AI 根据已写正文建议章节标题和摘要（单章节写完后调用）
     */
    @PreAuthorize("@ss.hasPermi('novel:ai:chat')")
    @Log(title = "AI建议章节标题摘要", businessType = BusinessType.OTHER)
    @PostMapping("/suggest-chapter-meta")
    public AjaxResult suggestChapterMeta(@RequestBody Map<String, Object> body)
    {
        Long projectId = body.containsKey("projectId") ? Long.valueOf(body.get("projectId").toString()) : null;
        String content = body.containsKey("content") ? (String) body.get("content") : "";
        if (StringUtils.isEmpty(content))
        {
            return error("章节内容不能为空");
        }
        String excerpt = content.length() > 1200 ? content.substring(0, 1200) + "..." : content;
        String projectCtx = projectId != null ? projectContextBuilder.buildProjectSystemContext(projectId) : "";

        String systemPrompt = "你是一位专业网络小说编辑，请根据章节正文内容，提炼一个精炼的章节标题和一段30字以内的摘要。"
            + "输出严格为 JSON 对象，格式：{\"title\":\"章节标题\",\"summary\":\"一两句摘要\"}。"
            + "不要输出任何解释文字，只输出 JSON 对象本身。"
            + projectCtx;
        String userPrompt = "以下是章节正文：\n" + excerpt + "\n\n请提炼章节标题和摘要：";

        String raw = novelAiInvocationService.invoke("suggest-chapter-meta", systemPrompt, userPrompt);
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start)
        {
            return error("AI 返回格式异常，请重试");
        }
        Map<String, Object> result = JSON.parseObject(raw.substring(start, end + 1),
            new TypeReference<Map<String, Object>>() {});
        return success(result);
    }

    /**
     * AI 章节规划建议：根据项目设定和已有章节，生成 count 个章节标题+摘要建议
     */
    @PreAuthorize("@ss.hasPermi('novel:ai:chat')")
    @Log(title = "AI章节规划建议", businessType = BusinessType.OTHER)
    @PostMapping("/plan-chapters")
    public AjaxResult planChapters(@RequestBody Map<String, Object> body)
    {
        Long projectId = Long.valueOf(body.get("projectId").toString());
        int count = body.containsKey("count") ? Integer.parseInt(body.get("count").toString()) : 5;
        if (count < 1) count = 1;
        if (count > 50) count = 50;
        String hint = body.containsKey("hint") ? (String) body.get("hint") : "";

        String projectCtx = projectContextBuilder.buildProjectSystemContext(projectId);

        // 获取已有章节概览，避免重复
        com.ruoyi.novel.domain.NovelChapter query = new com.ruoyi.novel.domain.NovelChapter();
        query.setProjectId(projectId);
        java.util.List<com.ruoyi.novel.domain.NovelChapter> existing = novelChapterService.selectNovelChapterList(query);
        StringBuilder existingInfo = new StringBuilder();
        if (!existing.isEmpty())
        {
            existingInfo.append("\n【已有章节（避免重复）】\n");
            for (com.ruoyi.novel.domain.NovelChapter ch : existing)
            {
                existingInfo.append("第").append(ch.getChapterNumber()).append("章：").append(ch.getTitle());
                if (StringUtils.isNotEmpty(ch.getSummary()))
                {
                    existingInfo.append(" — ").append(ch.getSummary());
                }
                existingInfo.append("\n");
            }
        }

        String systemPrompt = "你是一位专业网络小说策划，请根据项目信息为作者规划后续章节。"
            + "输出严格为 JSON 数组，每个元素必须包含三个字段：chapterNumber（整数）、title（字符串）、summary（字符串）。"
            + "示例：[{\"chapterNumber\":1,\"title\":\"章节标题\",\"summary\":\"30字以内的情节摘要\"}]。"
            + "字段名必须使用英文（chapterNumber、title、summary），不要使用中文字段名。"
            + "不要输出任何解释文字，只输出 JSON 数组本身，不要用 markdown 代码块包裹。"
            + projectCtx + existingInfo;

        int startNum = existing.isEmpty() ? 1 : existing.stream()
            .mapToInt(ch -> ch.getChapterNumber() == null ? 0 : ch.getChapterNumber())
            .max().orElse(0) + 1;

        String userPrompt = "请从第" + startNum + "章开始，规划接下来 " + count + " 个章节。"
            + (StringUtils.isNotEmpty(hint) ? "创作方向提示：" + hint : "")
            + "保持与项目大纲和风格一致，章节标题要有文学性，每章 summary 字段简洁描述核心情节（30字以内）。"
            + "注意：每个对象必须有 summary 字段，不能省略。";

        String raw = novelAiInvocationService.invoke("plan-chapters", systemPrompt, userPrompt);

        // 提取 JSON 数组部分（AI 可能在数组前后附带文字）
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start < 0 || end <= start)
        {
            return error("AI 返回格式异常，请重试");
        }
        String json = raw.substring(start, end + 1);
        List<Map<String, Object>> chapters = JSON.parseObject(json, new TypeReference<List<Map<String, Object>>>() {});
        return success(chapters);
    }
}
