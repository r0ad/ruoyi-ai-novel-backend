package com.ruoyi.web.controller.novel;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.novel.domain.NovelSetting;
import com.ruoyi.novel.security.NovelProjectSecurity;
import com.ruoyi.novel.service.INovelSettingService;

/**
 * 设定文档 Controller
 *
 * @author novel
 */
@RestController
@RequestMapping("/novel/setting")
public class NovelSettingController extends BaseController
{
    @Autowired
    private INovelSettingService novelSettingService;

    @Autowired
    private NovelProjectSecurity novelProjectSecurity;

    /**
     * 查询项目全部设定
     */
    @PreAuthorize("@ss.hasPermi('novel:setting:list')")
    @GetMapping("/list/{projectId}")
    public AjaxResult list(@PathVariable Long projectId)
    {
        novelProjectSecurity.checkProject(projectId);
        List<NovelSetting> list = novelSettingService.selectNovelSettingListByProjectId(projectId);
        return success(list);
    }

    /**
     * 按类型获取设定
     */
    @PreAuthorize("@ss.hasPermi('novel:setting:query')")
    @GetMapping("/{projectId}/{settingType}")
    public AjaxResult getByType(@PathVariable Long projectId, @PathVariable String settingType)
    {
        novelProjectSecurity.checkProject(projectId);
        NovelSetting setting = novelSettingService.selectNovelSettingByProjectAndType(projectId, settingType);
        return success(setting);
    }

    /**
     * 获取设定模板
     */
    @PreAuthorize("@ss.hasPermi('novel:setting:query')")
    @GetMapping("/template/{settingType}")
    public AjaxResult template(@PathVariable String settingType)
    {
        return success(novelSettingService.selectSettingTemplateContent(settingType));
    }

    /**
     * 保存设定（新增或更新）
     */
    @PreAuthorize("@ss.hasPermi('novel:setting:edit')")
    @Log(title = "小说设定", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult save(@Validated @RequestBody NovelSetting novelSetting)
    {
        novelProjectSecurity.checkProject(novelSetting.getProjectId());
        novelSetting.setUpdateBy(getUsername());
        if (novelSetting.getSettingId() == null)
        {
            novelSetting.setCreateBy(getUsername());
        }
        return toAjax(novelSettingService.saveNovelSetting(novelSetting));
    }
}