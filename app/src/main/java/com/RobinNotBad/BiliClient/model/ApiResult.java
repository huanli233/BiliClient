package com.RobinNotBad.BiliClient.model;

import org.json.JSONObject;

public class ApiResult {
    public int code;
    public String message;
    public long offset;
    public long timestamp;
    public String business = "";
    public boolean isBottom;
    public Object result;

    public ApiResult() {}
    public ApiResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResult(JSONObject fullResult) {
        this.code = fullResult.optInt("code",-1);
        this.message = fullResult.optString("message","json_format_error");
    }

    public ApiResult fromJson(JSONObject fullResult){
        this.code = fullResult.optInt("code",-1);
        this.message = fullResult.optString("message","json_format_error");
        return this;
    }

    public ApiResult setOffset(long timestamp, long offset, String business){
        if(timestamp != 0) this.timestamp = timestamp;
        this.offset = offset;
        this.business = business;
        return this;
    }

    public ApiResult setBottom(boolean isBottom){
        this.isBottom = isBottom;
        return this;
    }

    public ApiResult setResult(Object result){
        this.result = result;
        return this;
    }
}
