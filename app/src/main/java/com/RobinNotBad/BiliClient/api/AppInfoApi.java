package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Announcement;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AppInfoApi {
    public static void check(Context context){
        try {
            int version = BiliTerminal.getVersion();
            int curr = ConfInfoApi.getDateCurr();

            checkAnnouncement(context);

            if (SharedPreferencesUtil.getInt("app_version_last", 0) < version) {
                MsgUtil.showText(context, "更新公告", context.getString(R.string.update_log));
                SharedPreferencesUtil.putInt("app_version_last", version);
            }

            if(SharedPreferencesUtil.getInt("app_version_check",0) < curr) {    //限制一天一次
                Log.e("debug", "检查更新");
                SharedPreferencesUtil.putInt("app_version_check", curr);

                checkUpdate(context,false);
            }
        }catch (Exception e){
            CenterThreadPool.runOnUiThread(()->MsgUtil.toast(e.getMessage(),context));
        }
    }

    private static final ArrayList<String> customHeaders = new ArrayList<String>(){{
        add("User-Agent");
        add(NetWorkUtil.USER_AGENT_WEB);    //防止携带b站cookies导致可能存在的开发者盗号问题（
    }};

    public static void checkUpdate(Context context, boolean need_toast) throws Exception {
        String url = "http://api.biliterminal.cn/terminal/version/get_last";
        JSONObject result = NetWorkUtil.getJson(url,customHeaders);

        if(result.getInt("code")!=0) throw new Exception(result.getString("msg"));
        JSONObject data = result.getJSONObject("data");

        String version_name = data.getString("version_name");
        String update_log = data.getString("update_log");
        int latest = data.getInt("version_code");

        int version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        if(latest>version) MsgUtil.showText(context,version_name,update_log);
        else if(need_toast) CenterThreadPool.runOnUiThread(()->MsgUtil.toast("当前是最新版本！",context));
    }

    public static void checkAnnouncement(Context context) throws Exception {
        String url = "http://api.biliterminal.cn/terminal/announcement/get_last";
        JSONObject result = NetWorkUtil.getJson(url,customHeaders);

        if(result.getInt("code")!=0) throw new Exception("错误："+result.getString("msg"));
        JSONObject data = result.getJSONObject("data");

        int id = data.getInt("id");

        if(SharedPreferencesUtil.getInt("app_announcement_last",0) < id) {
            SharedPreferencesUtil.putInt("app_announcement_last", id);
            String title = data.getString("title");
            String content = data.getString("content");
            MsgUtil.showText(context,title,content);
        }
    }

    public static ArrayList<Announcement> getAnnouncementList() throws Exception {
        String url = "http://api.biliterminal.cn/terminal/announcement/get_list";
        JSONObject result = NetWorkUtil.getJson(url,customHeaders);

        if(result.getInt("code")!=0) throw new Exception("错误："+result.getString("msg"));
        JSONArray data = result.getJSONArray("data");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        ArrayList<Announcement> list = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject section = data.getJSONObject(i);
            Announcement announcement = new Announcement();
            announcement.id = section.getInt("id");
            announcement.ctime = sdf.format(section.getLong("ctime") * 1000);
            announcement.title = section.getString("title");
            announcement.content = section.getString("content");
            list.add(announcement);
        }
        return list;
    }
}
