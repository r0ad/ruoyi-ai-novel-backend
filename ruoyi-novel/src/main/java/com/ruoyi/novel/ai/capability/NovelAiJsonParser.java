package com.ruoyi.novel.ai.capability;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;

public final class NovelAiJsonParser
{
    private NovelAiJsonParser()
    {
    }

    public static JSONObject parseObject(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        String trimmed = text.trim();
        try
        {
            return JSON.parseObject(trimmed);
        }
        catch (Exception ignored)
        {
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start)
        {
            try
            {
                return JSON.parseObject(trimmed.substring(start, end + 1));
            }
            catch (Exception ignored)
            {
            }
        }
        start = trimmed.indexOf('[');
        end = trimmed.lastIndexOf(']');
        if (start >= 0 && end > start)
        {
            JSONArray array = JSON.parseArray(trimmed.substring(start, end + 1));
            JSONObject wrapper = new JSONObject();
            wrapper.put("issues", array);
            wrapper.put("passed", array == null || array.isEmpty());
            return wrapper;
        }
        return null;
    }
}
