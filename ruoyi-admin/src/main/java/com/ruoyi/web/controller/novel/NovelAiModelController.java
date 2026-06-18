package com.ruoyi.web.controller.novel;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.novel.ai.domain.NovelAiModel;
import com.ruoyi.novel.ai.service.INovelAiModelService;

/**
 * AI 模型配置 Controller
 */
@RestController
@RequestMapping("/novel/ai-model")
public class NovelAiModelController extends BaseController
{
    @Autowired
    private INovelAiModelService novelAiModelService;

    @PreAuthorize("@ss.hasPermi('novel:aimodel:list')")
    @GetMapping("/list")
    public TableDataInfo list(NovelAiModel novelAiModel)
    {
        startPage();
        List<NovelAiModel> list = novelAiModelService.selectNovelAiModelList(novelAiModel);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('novel:aimodel:query')")
    @GetMapping("/{modelId}")
    public AjaxResult getInfo(@PathVariable Long modelId)
    {
        return success(novelAiModelService.selectNovelAiModelByModelId(modelId));
    }

    @GetMapping("/active")
    public AjaxResult active()
    {
        NovelAiModel model = novelAiModelService.selectActiveNovelAiModel();
        return success(model != null ? model : Collections.emptyMap());
    }

    @PreAuthorize("@ss.hasPermi('novel:aimodel:add')")
    @Log(title = "AI模型配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody NovelAiModel novelAiModel)
    {
        novelAiModel.setCreateBy(getUsername());
        return toAjax(novelAiModelService.insertNovelAiModel(novelAiModel));
    }

    @PreAuthorize("@ss.hasPermi('novel:aimodel:edit')")
    @Log(title = "AI模型配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody NovelAiModel novelAiModel)
    {
        novelAiModel.setUpdateBy(getUsername());
        return toAjax(novelAiModelService.updateNovelAiModel(novelAiModel));
    }

    @PreAuthorize("@ss.hasPermi('novel:aimodel:remove')")
    @Log(title = "AI模型配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{modelIds}")
    public AjaxResult remove(@PathVariable Long[] modelIds)
    {
        return toAjax(novelAiModelService.deleteNovelAiModelByModelIds(modelIds));
    }

    @PreAuthorize("@ss.hasPermi('novel:aimodel:activate')")
    @Log(title = "AI模型激活", businessType = BusinessType.UPDATE)
    @PutMapping("/{modelId}/activate")
    public AjaxResult activate(@PathVariable Long modelId)
    {
        return toAjax(novelAiModelService.activateModel(modelId));
    }

    @PreAuthorize("@ss.hasPermi('novel:aimodel:test')")
    @Log(title = "AI模型连通性测试", businessType = BusinessType.OTHER)
    @PostMapping("/{modelId}/test")
    public AjaxResult testById(@PathVariable Long modelId)
    {
        String reply = novelAiModelService.testConnection(modelId);
        return success(Collections.singletonMap("reply", reply));
    }

    @PreAuthorize("@ss.hasPermi('novel:aimodel:test')")
    @Log(title = "AI模型连通性测试", businessType = BusinessType.OTHER)
    @PostMapping("/test")
    public AjaxResult test(@RequestBody NovelAiModel novelAiModel)
    {
        String reply = novelAiModelService.testConnection(novelAiModel);
        return success(Collections.singletonMap("reply", reply));
    }
}
