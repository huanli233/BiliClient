package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;

public class Stats implements Serializable {
    public int view;
    public int like;
    public int reply;
    public int coin;
    public int forward;
    public int danmaku;
    public int favorite;

    public boolean liked;
    public boolean favoured;
    public int coined;

    public boolean like_disabled;
    public boolean coin_disabled;
    public boolean reply_disabled;
    public boolean forward_disabled;
    public int allow_coin;
}
