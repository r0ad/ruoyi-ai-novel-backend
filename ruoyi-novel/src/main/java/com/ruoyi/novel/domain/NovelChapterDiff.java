package com.ruoyi.novel.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 章节版本对比结果
 */
public class NovelChapterDiff
{
    private Integer fromVersionNo;
    private Integer toVersionNo;
    private String fromTitle;
    private String toTitle;
    private String fromContent;
    private String toContent;
    private List<DiffLine> lines = new ArrayList<DiffLine>();

    public static class DiffLine
    {
        private String type;
        private String text;

        public DiffLine() {}

        public DiffLine(String type, String text)
        {
            this.type = type;
            this.text = text;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public Integer getFromVersionNo() { return fromVersionNo; }
    public void setFromVersionNo(Integer fromVersionNo) { this.fromVersionNo = fromVersionNo; }
    public Integer getToVersionNo() { return toVersionNo; }
    public void setToVersionNo(Integer toVersionNo) { this.toVersionNo = toVersionNo; }
    public String getFromTitle() { return fromTitle; }
    public void setFromTitle(String fromTitle) { this.fromTitle = fromTitle; }
    public String getToTitle() { return toTitle; }
    public void setToTitle(String toTitle) { this.toTitle = toTitle; }
    public String getFromContent() { return fromContent; }
    public void setFromContent(String fromContent) { this.fromContent = fromContent; }
    public String getToContent() { return toContent; }
    public void setToContent(String toContent) { this.toContent = toContent; }
    public List<DiffLine> getLines() { return lines; }
    public void setLines(List<DiffLine> lines) { this.lines = lines; }
}