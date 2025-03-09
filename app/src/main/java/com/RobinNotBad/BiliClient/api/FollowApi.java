package com.RobinNotBad.BiliClient.api;

//关注api
//2023-08-27

import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;


public class FollowApi {
    public static int getFollowingList(long mid, int page, List<UserInfo> userList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/relation/followings?vmid=" + mid + "&pn=" + page + "&ps=20&order=desc&order_type=attention";
        JSONObject callback = NetWorkUtil.getJson(url);
        if (callback.optInt("code", -1) != 0)
            throw new JSONException(callback.optInt("code", -1) + "：" + callback.optString("message", "未知API错误"));
        JSONObject data = callback.getJSONObject("data");
        JSONArray list = data.getJSONArray("list");
        if (list.length() == 0) return 1;
        else {
            for (int i = 0; i < list.length(); i++) {
                JSONObject userInfo = list.getJSONObject(i);
                String name = userInfo.getString("uname");
                long uid = userInfo.getLong("mid");
                String avatar = userInfo.getString("face");
                String sign = userInfo.getString("sign");
                userList.add(new UserInfo(uid, name, avatar, sign, 0, 0, 0, true, "", 0, "", userInfo.optLong("mtime", 0), 0));
            }
            return 0;
        }
    }

    public static int getFollowerList(long mid, int page, List<UserInfo> userList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/relation/followers?vmid=" + mid + "&pn=" + page + "&ps=20&order=desc&order_type=attention";
        JSONObject callback = NetWorkUtil.getJson(url);
        if (callback.optInt("code", -1) != 0)
            throw new JSONException(callback.optInt("code", -1) + "：" + callback.optString("message", "未知API错误"));
        JSONObject data = callback.getJSONObject("data");
        JSONArray list = data.getJSONArray("list");
        if (list.length() == 0) return 1;
        else {
            for (int i = 0; i < list.length(); i++) {
                JSONObject userInfo = list.getJSONObject(i);
                String name = userInfo.getString("uname");
                long uid = userInfo.getLong("mid");
                String avatar = userInfo.getString("face");
                String sign = userInfo.getString("sign");
                userList.add(new UserInfo(uid, name, avatar, sign, 0, 0, 0, true, "", 0, "", userInfo.optLong("mtime", 0), 0));
            }
            return 0;
        }
    }
}
