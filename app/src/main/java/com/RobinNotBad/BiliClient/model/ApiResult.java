package com.RobinNotBad.BiliClient.model;

public class ApiResult {
    public int code;
    public String message;
    public long offset;
    public long timestamp;
    public String business = "";
    public boolean isBottom;

    public ApiResult() {}
    public ApiResult(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
