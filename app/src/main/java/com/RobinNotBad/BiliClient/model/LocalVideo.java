package com.RobinNotBad.BiliClient.model;

import java.util.ArrayList;

public class LocalVideo {
    public String cover;
    public String title;
    public ArrayList<String> pageList;
    public ArrayList<String> videoFileList;
    public ArrayList<String> danmakuFileList;
    public ArrayList<Long> sizeList;
    public long size;

    public LocalVideo() {}

    public void calcTotalSize(){
        size = 0;
        for (long pageSize: sizeList) {
            size += pageSize;
        }
    }
}
