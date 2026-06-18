package com.ruoyi.web.controller.novel;

import java.util.Collections;
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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.novel.ai.domain.NovelAiChatRequest;
import com.ruoyi.novel.ai.session.domain.NovelAiSession;
import com.ruoyi.novel.ai.session.service.INovelAiSessionService;
import com.ruoyi.novel.ai.service.INovelAiService;
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
}
