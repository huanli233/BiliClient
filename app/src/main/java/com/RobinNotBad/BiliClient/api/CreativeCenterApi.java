package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Response;

public class CreativeCenterApi {
    public static JSONObject getVideoStat() throws IOException, JSONException {
        String url = "https://member.bilibili.com/x/web/index/stat";
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        if(result.has("data")) return result.getJSONObject("data");
        else return new JSONObject();
    }
}
