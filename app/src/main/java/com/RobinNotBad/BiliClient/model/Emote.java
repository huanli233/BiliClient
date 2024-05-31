package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;

public class Emote implements Serializable {
    public int id;
    public int packageId;
    public String name;
    public String url;
    public int size;

    public Emote(){}

    public Emote(String name, String url, int size) {
        this.name = name;
        this.url = url;
        this.size = size;
    }
}
