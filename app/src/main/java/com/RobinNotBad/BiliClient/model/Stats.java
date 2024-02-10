package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;

public class Stats implements Serializable {
    public int view;
    public int like;
    public int reply;
    public int coin;
    public int forward;

    public boolean liked;
    public int coined;

    public int like_disabled;
    public int coin_disabled;
    public int reply_disabled;
    public int forward_disabled;
}
