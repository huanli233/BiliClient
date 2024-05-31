package com.RobinNotBad.BiliClient.event;

import com.RobinNotBad.BiliClient.model.Reply;

public class ReplyEvent {
    private int type;
    private Reply message;

    public ReplyEvent(int type, Reply message) {
        this.type = type;
        this.message = message;
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
}
