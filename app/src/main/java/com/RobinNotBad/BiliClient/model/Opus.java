package com.RobinNotBad.BiliClient.model;

import java.util.ArrayList;

public class Opus {
    public static final int TYPE_DYNAMIC = 1;
    public static final int TYPE_ARTICLE = 2;
    public static final int TYPE_DYNAMIC_OLD_STYLE = 3;

    public long id;
    public int type;
    public long commentId;
    public int commentType;
    public String title;
    public String cover;
    public String content;
    public String pubTime;
    public UserInfo upInfo;
    public Stats stats;
    public ArrayList<String> topImages;
    public OpusParagraph[] paragraphs;
    public long listId;


    public long parsedId;

    public Opus(int type, long id) {
        this.type = type;
        this.parsedId = id;
    }

    public Opus() {

    }
}
