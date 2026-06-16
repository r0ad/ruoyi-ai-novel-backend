package com.ruoyi.novel.utils;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.novel.domain.NovelChapterDiff;

/**
 * 简单行级文本 diff
 */
public final class NovelTextDiffUtils
{
    private NovelTextDiffUtils() {}

    public static List<NovelChapterDiff.DiffLine> diffLines(String left, String right)
    {
        String[] leftLines = splitLines(left);
        String[] rightLines = splitLines(right);
        List<NovelChapterDiff.DiffLine> result = new ArrayList<NovelChapterDiff.DiffLine>();
        int i = 0;
        int j = 0;
        while (i < leftLines.length || j < rightLines.length)
        {
            if (i < leftLines.length && j < rightLines.length && leftLines[i].equals(rightLines[j]))
            {
                result.add(new NovelChapterDiff.DiffLine("equal", leftLines[i]));
                i++;
                j++;
            }
            else if (j < rightLines.length && (i >= leftLines.length || indexOf(rightLines[j], leftLines, i) == -1))
            {
                result.add(new NovelChapterDiff.DiffLine("added", rightLines[j]));
                j++;
            }
            else if (i < leftLines.length)
            {
                result.add(new NovelChapterDiff.DiffLine("removed", leftLines[i]));
                i++;
            }
            else
            {
                result.add(new NovelChapterDiff.DiffLine("added", rightLines[j]));
                j++;
            }
        }
        return result;
    }

    private static String[] splitLines(String text)
    {
        if (text == null || text.isEmpty())
        {
            return new String[0];
        }
        return text.split("\\r?\\n", -1);
    }

    private static int indexOf(String target, String[] lines, int start)
    {
        for (int k = start; k < lines.length; k++)
        {
            if (target.equals(lines[k]))
            {
                return k;
            }
        }
        return -1;
    }
}