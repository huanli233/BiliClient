package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;

public class ArticleCard implements Serializable {
    public String title;
    public long id;
    public String cover;
    public String upName;
    public String view;

    public ArticleCard() {
    }

    public ArticleCard(String title, long id, String cover, String upName, String view) {
        this.title = title;
        this.id = id;
        this.cover = cover;
        this.upName = upName;
        this.view = view;
    }
}
