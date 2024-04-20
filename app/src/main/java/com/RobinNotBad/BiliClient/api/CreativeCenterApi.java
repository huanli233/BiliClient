package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/*
创作中心
 */

public class CreativeCenterApi {
    public static JSONObject getVideoStat() throws IOException, JSONException {
        String url = "https://member.bilibili.com/x/web/index/stat";
        JSONObject result = NetWorkUtil.getJson(url);
        return result.optJSONObject("data");
    }
}
