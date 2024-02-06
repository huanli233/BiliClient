package com.RobinNotBad.BiliClient.api;

import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

//新的动态api，旧的那个实在太蛋疼而且说不定随时会被弃用（

public class DynamicApiNew {
    public static void getDynamic() throws IOException, JSONException {
        Response response = NetWorkUtil.get("https://api.bilibili.com/x/polymer/web-dynamic/v1/feed/all");
        if(response.body()==null) throw new JSONException("动态返回数据为空TAT");
        JSONObject all = new JSONObject(response.body().string());
        if(all.getInt("code")!=0) throw new JSONException(all.getString("message"));
        //以后API也按照这个写吧，提供的报错信息清楚些也不会爆黄

        JSONObject data = all.getJSONObject("data");

        String offset = data.getString("offset");
        boolean has_more = data.getBoolean("has_more");
        JSONArray items = data.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            analyzeDynamic(items.getJSONObject(i));
        }
    }

    public static void analyzeDynamic(JSONObject dynamic_json) throws JSONException {
        Dynamic dynamic = new Dynamic();
        dynamic.dynamicId = dynamic_json.getString("id_str");
        dynamic.type = dynamic_json.getString("type");

        JSONObject basic = dynamic_json.getJSONObject("basic");
        dynamic.comment_id = basic.getString("comment_id_str");
        dynamic.comment_type = basic.getInt("comment_type");


        JSONObject modules = dynamic_json.getJSONObject("modules");

        if(modules.has("module_author") && !modules.isNull("module_author")) {
            JSONObject module_author = modules.getJSONObject("module_author");
            UserInfo userInfo = new UserInfo();
            userInfo.mid = module_author.getLong("mid");
            userInfo.followed = module_author.getBoolean("following");
            userInfo.avatar = module_author.getString("face");
            dynamic.userInfo = userInfo;
        }
        else dynamic.userInfo = new UserInfo();

        if(modules.has("module_dynamic") && !modules.isNull("module_dynamic")){

            JSONObject module_dynamic = modules.getJSONObject("module_dynamic");
            if(module_dynamic.has("desc") && !module_dynamic.isNull("module_dynamic")) {
                JSONObject desc = module_dynamic.getJSONObject("desc");
                JSONArray rich_text_nodes = module_dynamic.getJSONArray("rich_text_nodes");
                for (int i = 0; i < rich_text_nodes.length(); i++) {

                }
            }


        }

    }
}
