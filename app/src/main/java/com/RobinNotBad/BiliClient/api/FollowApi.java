package com.RobinNotBad.BiliClient.api;

//关注api
//2023-08-27

import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Response;

public class FollowApi {
    public static int getFollowList(long mid, int page, ArrayList<UserInfo> userList) throws IOException, JSONException {
        String url = "https://api.bilibili.com/x/relation/followings?vmid=" + mid + "&pn=" + page + "&ps=20&order=desc&order_type=attention";

        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject callback = new JSONObject(Objects.requireNonNull(response.body()).string());
        JSONObject data = callback.getJSONObject("data");
        JSONArray list = data.getJSONArray("list");
        if(list.length()==0) return 1;
        else{
            for (int i = 0; i < list.length(); i++) {
                JSONObject userInfo = list.getJSONObject(i);
                String name = userInfo.getString("uname");
                long uid = userInfo.getLong("mid");
                String avatar = userInfo.getString("face");
                String sign = userInfo.getString("sign");
                userList.add(new UserInfo(uid,name,avatar,sign,0,0,true,"",0,""));
            }
            return 0;
        }
    }
}
