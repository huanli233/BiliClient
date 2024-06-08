package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Announcement;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import com.RobinNotBad.BiliClient.util.ToolsUtil;
import okhttp3.Response;
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
                MsgUtil.showText(context, "更新公告", context.getResources().getString(R.string.update_tip) + "\n\n更新日志：\n" + ToolsUtil.getUpdateLog(context));
                if(SharedPreferencesUtil.getInt("app_version_last", 0) < 20240606) MsgUtil.showDialog(context,"提醒","当前的新版本实现了对抗部分类型的风控，建议您重新登录账号以确保成功使用");
                SharedPreferencesUtil.putInt("app_version_last", version);
            }

            if(SharedPreferencesUtil.getInt("app_version_check",0) < curr) {    //限制一天一次
                Log.e("debug", "检查更新");
                SharedPreferencesUtil.putInt("app_version_check", curr);

                checkUpdate(context,false,false);
            }
        }catch (Exception e){
            CenterThreadPool.runOnUiThread(()->MsgUtil.toast(e.getMessage(),context));
        }
    }

    private static final ArrayList<String> customHeaders = new ArrayList<String>(){{
        add("User-Agent");
        add(NetWorkUtil.USER_AGENT_WEB);    //防止携带b站cookies导致可能存在的开发者盗号问题（
    }};

    public static void checkUpdate(Context context, boolean need_toast,boolean debug_ver) throws Exception {
        String url = "http://api.biliterminal.cn/terminal/version/get_last";
        if(debug_ver) url += "?debug";
        JSONObject result = NetWorkUtil.getJson(url,customHeaders);

        if(result.getInt("code")!=0) throw new Exception(result.getString("msg"));
        JSONObject data = result.getJSONObject("data");

        String version_name = data.getString("version_name");
        String update_log = data.getString("update_log");
        int latest = data.getInt("version_code");

        int version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        if(latest > version) {
            if(debug_ver) CenterThreadPool.runOnUiThread(()->MsgUtil.toast("发现新的测试版！",context));
            else CenterThreadPool.runOnUiThread(()->MsgUtil.toast("发现新版本！",context));
            MsgUtil.showText(context,version_name,update_log);
        }
        else if(need_toast) {
            if(debug_ver) CenterThreadPool.runOnUiThread(()->MsgUtil.toast("没有新的测试版了！",context));
            else CenterThreadPool.runOnUiThread(()->MsgUtil.toast("当前是最新版本！",context));
        }

        if(ToolsUtil.isDebugBuild() && !debug_ver) checkUpdate(context,need_toast,true);
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

    public static String uploadStack(String stack,Context context){
        //上传崩溃堆栈
        try {
            String url = "http://api.biliterminal.cn/terminal/upload/stack";

            JSONObject post_data = new JSONObject();
            post_data.put("stack",stack);
            post_data.put("client_version", context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode);
            post_data.put("device_sdk", Build.VERSION.SDK_INT);
            post_data.put("device_product",Build.PRODUCT);
            post_data.put("device_brand",Build.BRAND);
            
            JSONObject res = new JSONObject(NetWorkUtil.postJson(url,post_data.toString(),customHeaders).body().string());
            if(res.getInt("code") == 200) return "上传成功";
            else return res.getString("msg");
        }catch (Throwable e){
            e.printStackTrace();
            return "上传失败";
        }
    }
    
    public static ArrayList<UserInfo> getSponsors(int page) throws Exception{
        String url = "http://api.biliterminal.cn/terminal/afdian/get_sponsor?page=" + page;
        JSONObject result = NetWorkUtil.getJson(url,customHeaders);

        if(result.getInt("code")!=200) throw new Exception("获取失败");
        JSONArray data = result.getJSONArray("data");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        ArrayList<UserInfo> list = new ArrayList<>();
        if(page == 1){
            UserInfo tip_user = new UserInfo(-1,"温馨提醒","","收到的捐款仅用于维护服务器 | 捐赠者信息来自于爱发电，不对信息内容负责",-1,6,true,"",0,"");
            list.add(tip_user);
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject sponsor = data.getJSONObject(i);
            
            UserInfo user = new UserInfo();
            
            user.name = sponsor.getString("name");
            user.avatar = sponsor.getString("avatar");
            user.sign = "总金额：" + sponsor.getInt("sum_amount") + "r | 捐赠时间：" + sdf.format(sponsor.getLong("last_time")*1000);
            user.mid = -1;
            user.fans = 0;
            user.followed = true;
            user.level = 6;
            user.notice = "";
            user.official = 0;
            user.officialDesc = "";
            
            list.add(user);
        }
        return list;
    }
}
