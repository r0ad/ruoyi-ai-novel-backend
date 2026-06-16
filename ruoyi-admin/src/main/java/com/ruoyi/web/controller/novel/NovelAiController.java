package com.ruoyi.web.controller.novel;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.novel.ai.domain.NovelAiChatRequest;
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
}
