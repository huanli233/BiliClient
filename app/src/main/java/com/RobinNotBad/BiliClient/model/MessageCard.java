package com.RobinNotBad.BiliClient.model;

import java.util.List;

public class MessageCard {

    public static final int GET_TYPE_REPLY = 0;
    public static final int GET_TYPE_AT = 1;
    public static final int GET_TYPE_LIKE = 2;

    public long id;
    public List<UserInfo> user;
    public long timeStamp = 0;
    public String timeDesc = "";
    public String content;
    public VideoCard videoCard = null;
    public Reply replyInfo = null;
    public Reply dynamicInfo = null;
    public long subjectId;
    public int businessId;
    public String itemType;
    public int getType;
    public long sourceId;
    public long rootId;
    public long targetId;

    public static class Cursor {
        public final boolean is_end;
        public final long id;
        public final long time;

        public Cursor(boolean is_end, long id, long time) {
            this.is_end = is_end;
            this.id = id;
            this.time = time;
        }
    }
}
