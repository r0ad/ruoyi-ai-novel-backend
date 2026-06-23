package com.ruoyi.web.controller.novel;

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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.novel.domain.NovelMetaEntity;
import com.ruoyi.novel.domain.NovelMetaRelation;
import com.ruoyi.novel.security.NovelProjectSecurity;
import com.ruoyi.novel.service.INovelMetaService;

@RestController
@RequestMapping("/novel/meta")
public class NovelMetaController extends BaseController
{
    @Autowired
    private INovelMetaService novelMetaService;

    @Autowired
    private NovelProjectSecurity novelProjectSecurity;

    @PreAuthorize("@ss.hasPermi('novel:meta:list')")
    @GetMapping("/graph/{projectId}")
    public AjaxResult graph(@PathVariable Long projectId)
    {
        novelProjectSecurity.checkProject(projectId);
        return success(novelMetaService.selectGraphByProjectId(projectId));
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:list')")
    @GetMapping("/entity/list/{projectId}")
    public AjaxResult entityList(@PathVariable Long projectId, NovelMetaEntity query)
    {
        novelProjectSecurity.checkProject(projectId);
        query.setProjectId(projectId);
        List<NovelMetaEntity> list = novelMetaService.selectEntityList(query);
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:query')")
    @GetMapping("/entity/{entityId}")
    public AjaxResult getEntity(@PathVariable Long entityId)
    {
        novelProjectSecurity.checkMetaEntity(entityId);
        return success(novelMetaService.selectEntityById(entityId));
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:add')")
    @Log(title = "Meta实体", businessType = BusinessType.INSERT)
    @PostMapping("/entity")
    public AjaxResult addEntity(@Validated @RequestBody NovelMetaEntity entity)
    {
        novelProjectSecurity.checkProject(entity.getProjectId());
        entity.setCreateBy(getUsername());
        return toAjax(novelMetaService.insertEntity(entity));
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:edit')")
    @Log(title = "Meta实体", businessType = BusinessType.UPDATE)
    @PutMapping("/entity")
    public AjaxResult editEntity(@Validated @RequestBody NovelMetaEntity entity)
    {
        novelProjectSecurity.checkMetaEntity(entity.getEntityId());
        entity.setUpdateBy(getUsername());
        return toAjax(novelMetaService.updateEntity(entity));
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:remove')")
    @Log(title = "Meta实体", businessType = BusinessType.DELETE)
    @DeleteMapping("/entity/{entityIds}")
    public AjaxResult removeEntity(@PathVariable Long[] entityIds)
    {
        novelProjectSecurity.checkMetaEntities(entityIds);
        return toAjax(novelMetaService.deleteEntityByIds(entityIds));
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:list')")
    @GetMapping("/relation/list/{projectId}")
    public AjaxResult relationList(@PathVariable Long projectId, NovelMetaRelation query)
    {
        novelProjectSecurity.checkProject(projectId);
        query.setProjectId(projectId);
        return success(novelMetaService.selectRelationList(query));
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:add')")
    @Log(title = "Meta关系", businessType = BusinessType.INSERT)
    @PostMapping("/relation")
    public AjaxResult addRelation(@Validated @RequestBody NovelMetaRelation relation)
    {
        novelProjectSecurity.checkProject(relation.getProjectId());
        relation.setCreateBy(getUsername());
        return toAjax(novelMetaService.insertRelation(relation));
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:edit')")
    @Log(title = "Meta关系", businessType = BusinessType.UPDATE)
    @PutMapping("/relation")
    public AjaxResult editRelation(@Validated @RequestBody NovelMetaRelation relation)
    {
        novelProjectSecurity.checkMetaRelations(new Long[]{relation.getRelationId()});
        return toAjax(novelMetaService.updateRelation(relation));
    }

    @PreAuthorize("@ss.hasPermi('novel:meta:remove')")
    @Log(title = "Meta关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/relation/{relationIds}")
    public AjaxResult removeRelation(@PathVariable Long[] relationIds)
    {
        novelProjectSecurity.checkMetaRelations(relationIds);
        return toAjax(novelMetaService.deleteRelationByIds(relationIds));
    }
}