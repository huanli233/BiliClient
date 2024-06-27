package com.RobinNotBad.BiliClient.event;

import com.RobinNotBad.BiliClient.model.Reply;

public class ReplyEvent {
    private int type;
    private Reply message;
    private int pos;
    private long oid;

    public ReplyEvent(int type, Reply message, long oid) {
        this.type = type;
        this.message = message;
        this.oid = oid;
    }

    public ReplyEvent(int type, Reply message, int pos, long oid) {
        this.type = type;
        this.message = message;
        this.pos = pos;
        this.oid = oid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Reply getMessage() {
        return message;
    }

    public void setMessage(Reply message) {
        this.message = message;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }
}
