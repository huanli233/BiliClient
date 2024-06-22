package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;
import java.util.List;

public class EmotePackage implements Serializable {
    public int id;
    public String text;
    public String url;
    public int type;
    public int attr;
    public int size;
    public int item_id;
    public List<Emote> emotes;
    public boolean permanent;
}
