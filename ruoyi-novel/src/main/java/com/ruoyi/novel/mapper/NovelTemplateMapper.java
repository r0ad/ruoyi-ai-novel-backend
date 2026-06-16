package com.ruoyi.novel.mapper;

import com.ruoyi.novel.domain.NovelTemplate;

/**
 * 创作模板 数据层
 *
 * @author novel
 */
public interface NovelTemplateMapper
{
    public NovelTemplate selectNovelTemplateByCode(String templateCode);
}