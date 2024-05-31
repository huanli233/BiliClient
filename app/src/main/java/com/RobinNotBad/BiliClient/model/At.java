package com.RobinNotBad.BiliClient.model;

import java.io.Serializable;

public class At implements Serializable {
    public long rid;
    public int textStartIndex;
    public int textEndIndex;
    public String name;

    public At(long rid, int startIndex, int endIndex) {
        this.rid = rid;
        this.textStartIndex = startIndex;
        this.textEndIndex = endIndex;
    }

    public At(long rid, String name) {
        this.rid = rid;
        this.name = name;
    }
}
