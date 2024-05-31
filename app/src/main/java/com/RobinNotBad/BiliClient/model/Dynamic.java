package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dynamic implements Serializable {
    public final static String DYNAMIC_TYPE_UGC_SEASON = "DYNAMIC_TYPE_UGC_SEASON";
    public long dynamicId;
    public String type;
    public long comment_id;
    public int comment_type;

    public UserInfo userInfo;
    public String content;
    public ArrayList<Emote> emotes;
    public String pubTime;

    public Stats stats;

    public String major_type;
    public Object major_object;
    public Dynamic dynamic_forward;
    public List<At> ats;
    public boolean canDelete;

    public Dynamic(){}
}
