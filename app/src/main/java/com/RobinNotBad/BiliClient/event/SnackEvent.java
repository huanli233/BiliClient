package com.RobinNotBad.BiliClient.event;

public class SnackEvent {
    private String message;

    public SnackEvent() {}

    public SnackEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
