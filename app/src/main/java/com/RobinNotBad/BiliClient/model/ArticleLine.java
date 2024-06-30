package com.RobinNotBad.BiliClient.model;

public class ArticleLine {
    public int type;
    public String content;
    public String extra;

    public ArticleLine() {
    }

    public ArticleLine(int type, String content, String extra) {
        this.type = type;
        this.content = content;
        this.extra = extra;
    }
}
