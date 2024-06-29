package com.RobinNotBad.BiliClient.event;

import com.google.android.material.snackbar.Snackbar;

public class SnackEvent {
    private String message;
    private long startTime;
    private int duration;

    public SnackEvent() {}

    public SnackEvent(String message) {
        this.message = message;
        this.startTime = System.currentTimeMillis();
        this.duration = Snackbar.LENGTH_SHORT;
    }

    public SnackEvent(String message, long startTime) {
        this.message = message;
        this.startTime = startTime;
        this.duration = Snackbar.LENGTH_SHORT;
    }

    public SnackEvent(String message, long startTime, int duration) {
        this.message = message;
        this.startTime = startTime;
        this.duration = duration;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
