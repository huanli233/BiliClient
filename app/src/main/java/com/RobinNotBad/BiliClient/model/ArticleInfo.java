package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;

public class ArticleInfo implements Serializable {
    public long id;
    public String title;
    public String summary; //摘要
    public String banner; //头图
    public UserInfo upInfo;
    public long ctime;
    public Stats stats;
    public int wordCount; //字数
    public String keywords;
    public String content; //文章内容

    public ArticleInfo() {
    }

}
