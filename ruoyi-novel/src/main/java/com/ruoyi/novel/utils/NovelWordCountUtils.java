package com.ruoyi.novel.utils;

import com.ruoyi.common.utils.StringUtils;

/**
 * 论文字数统计（中文汉字 + 英文单词）
 *
 * @author novel
 */
public final class NovelWordCountUtils
{
    private static final java.util.regex.Pattern CHINESE = java.util.regex.Pattern.compile(
        "[\u4e00-\u9fff\u3400-\u4dbf\uff00-\uffef]");
    private static final java.util.regex.Pattern ENGLISH = java.util.regex.Pattern.compile("\\b[a-zA-Z0-9]+\\b");

    private NovelWordCountUtils()
    {
    }

    public static int countWords(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return 0;
        }
        String clean = text
            .replaceAll("^#+\\s*", "")
            .replaceAll("(?m)^\\s*[-*+]\\s*", "")
            .replaceAll("(?m)^\\s*\\d+\\.\\s*", "")
            .trim();
        int chinese = 0;
        java.util.regex.Matcher cm = CHINESE.matcher(clean);
        while (cm.find())
        {
            chinese++;
        }
        int english = 0;
        java.util.regex.Matcher em = ENGLISH.matcher(clean);
        while (em.find())
        {
            english++;
        }
        return chinese + english;
    }
}
