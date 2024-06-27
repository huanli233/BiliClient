package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.BuildConfig;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.update.UpdateInfoActivity;
import com.RobinNotBad.BiliClient.model.Announcement;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class AppInfoApi {
    public static void check(Context context) {
        try {
            int version = BiliTerminal.getVersion();
            int curr = ConfInfoApi.getDateCurr();

            checkAnnouncement(context);

            if (SharedPreferencesUtil.getInt("app_version_last", 0) < version) {
                int last_ver = SharedPreferencesUtil.getInt("app_version_last", 0);
                if (last_ver != -1) {
                    if (last_ver < 20240606)
                        MsgUtil.showDialog(context, "提醒", "当前的新版本实现了对抗部分类型的风控，建议您重新登录账号以确保成功使用");
                    if (last_ver < 20240615)
                        MsgUtil.showDialog(context, "提醒", "当前版本开始，内置播放器退出播放时会上传视频播放进度");
                }
                MsgUtil.showText(context, "更新公告", context.getResources().getString(R.string.update_tip) + "\n\n更新日志：\n" + ToolsUtil.getUpdateLog(context));
                if (ToolsUtil.isDebugBuild())
                    MsgUtil.showDialog(context, "警告", "这个版本是测试版，仅在测试群中发布，禁止外传到其他平台（如：奇妙应用等应用商店）");
                SharedPreferencesUtil.putInt("app_version_last", version);
            }

            if (SharedPreferencesUtil.getInt("app_version_check", 0) < curr) {    //限制一天一次
                Log.e("debug", "检查更新");
                SharedPreferencesUtil.putInt("app_version_check", curr);

                checkUpdate(context, false, false);
            }
        } catch (Exception e) {
            Log.e("BiliClient", e.toString());
            CenterThreadPool.runOnUiThread(() -> MsgUtil.toast("连接到哔哩终端接口时发生错误", context));
        }
    }

    private static final ArrayList<String> customHeaders = new ArrayList<>() {{
        add("User-Agent");
        add(NetWorkUtil.USER_AGENT_WEB);    //防止携带b站cookies导致可能存在的开发者盗号问题（
        add("App-Info");
        try {
            add(new JSONObject()
                    .put("versionName", BuildConfig.VERSION_NAME)
                    .put("versionCode", BuildConfig.VERSION_CODE)
                    .put("isBeta", BuildConfig.BETA)
                    .put("applicationId", BuildConfig.APPLICATION_ID)
                    .put("buildType", BuildConfig.BUILD_TYPE)
                    .put("debugEnabled", BuildConfig.DEBUG)
                    .toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        add("Device-Info");
        try {
            add(new JSONObject()
                    .put("sdk", Build.VERSION.SDK_INT)
                    .put("release", Build.VERSION.RELEASE)
                    .put("product", Build.PRODUCT)
                    .put("brand", Build.BRAND)
                    .put("device", Build.DEVICE)
                    .put("type", Build.TYPE)
                    .put("id", Build.ID)
                    .toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }};

    public static void checkUpdate(Context context, boolean need_toast, boolean debug_ver) throws Exception {
        debug_ver = ToolsUtil.isDebugBuild();
        String url = "http://api.biliterminal.cn/terminal/version/get_last";
        if (debug_ver) url += "?debug";
        JSONObject result = NetWorkUtil.getJson(url, customHeaders);

        if (result.getInt("code") != 0) throw new Exception(result.getString("msg"));
        JSONObject data = result.getJSONObject("data");

        String version_name = data.getString("version_name");
        String update_log = data.getString("update_log");
        int latest = data.getInt("version_code");
        long ctime = data.optLong("ctime", -1);
        int can_download = data.optInt("can_download", 0);
        int is_release = data.optInt("is_release", 0);

        int version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        if (latest > version) {
            if (debug_ver)
                CenterThreadPool.runOnUiThread(() -> MsgUtil.toast("发现新的测试版！", context));
            else CenterThreadPool.runOnUiThread(() -> MsgUtil.toast("发现新版本！", context));
            context.startActivity(new Intent(context, UpdateInfoActivity.class)
                    .putExtra("versionName", version_name)
                    .putExtra("versionCode", latest)
                    .putExtra("updateLog", update_log)
                    .putExtra("ctime", ctime)
                    .putExtra("isRelease", is_release)
                    .putExtra("canDownload", can_download));
        } else if (need_toast) {
            if (debug_ver)
                CenterThreadPool.runOnUiThread(() -> MsgUtil.toast("没有新的测试版了！", context));
            else CenterThreadPool.runOnUiThread(() -> MsgUtil.toast("当前是最新版本！", context));
        }
    }

    public static String getDownloadUrl(int versionCode) throws Exception {
        String url = "https://api.biliterminal.cn/terminal/version/get_download_url" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("version_code", versionCode);
        JSONObject result = NetWorkUtil.getJson(url, customHeaders);

        if (result.getInt("code") != 0) throw new Exception("错误：" + result.getString("msg"));

        return result.optString("data");
    }

    public static void checkAnnouncement(Context context) throws Exception {
        String url = "http://api.biliterminal.cn/terminal/announcement/get_list?from=" + SharedPreferencesUtil.getInt("app_announcement_last", -1);
        JSONObject result = NetWorkUtil.getJson(url, customHeaders);

        if (result.getInt("code") != 0) throw new Exception("错误：" + result.getString("msg"));
        JSONArray data = result.getJSONArray("data");
        for (int i = 0; i < data.length(); i++) {
            JSONObject item = data.getJSONObject(i);

            int id = item.getInt("id");

            if (SharedPreferencesUtil.getInt("app_announcement_last", 0) < id)
                SharedPreferencesUtil.putInt("app_announcement_last", id);
            String title = item.getString("title");
            String content = item.getString("content");
            MsgUtil.showText(context, title, content);
        }
    }

    public static ArrayList<Announcement> getAnnouncementList() throws Exception {
        String url = "http://api.biliterminal.cn/terminal/announcement/get_list";
        JSONObject result = NetWorkUtil.getJson(url, customHeaders);

        if (result.getInt("code") != 0) throw new Exception("错误：" + result.getString("msg"));
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

    public static String uploadStack(String stack, Context context) {
        //上传崩溃堆栈
        try {
            String url = "http://api.biliterminal.cn/terminal/upload/stack";

            JSONObject post_data = new JSONObject();
            post_data.put("stack", stack);
            post_data.put("client_version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
            post_data.put("device_sdk", Build.VERSION.SDK_INT);
            post_data.put("device_product", Build.PRODUCT);
            post_data.put("device_brand", Build.BRAND);

            JSONObject res = new JSONObject(Objects.requireNonNull(NetWorkUtil.postJson(url, post_data.toString(), customHeaders).body()).string());
            if (res.getInt("code") == 200) return "上传成功";
            else return res.getString("msg");
        } catch (Throwable e) {
            e.printStackTrace();
            return "上传失败";
        }
    }

    public static ArrayList<UserInfo> getSponsors(int page) throws Exception {
        String url = "http://api.biliterminal.cn/terminal/afdian/get_sponsor?page=" + page;
        JSONObject result = NetWorkUtil.getJson(url, customHeaders);

        if (result.getInt("code") != 200) throw new Exception("获取失败");
        JSONArray data = result.getJSONArray("data");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");

        ArrayList<UserInfo> list = new ArrayList<>();
        if (page == 1) {
            list.add(new UserInfo(-1, "温馨提醒", "", "若有捐赠意向请访问https://afdian.net/a/bili_terminal\n捐赠为纯自愿行为，收到的捐款仅用于维护服务器，未成年人请征求父母意见\n你可以尽管放心终端不会设置任何付费功能，并且无论是否捐赠，你所提出的意见和建议都会被同等考虑（难以实现或没有必要的功能我们也有权拒绝）\n捐赠者信息来自于爱发电，我们不对列表内的信息内容负责", -1, 6, true, "", 0, ""));
        }
        for (int i = 0; i < data.length(); i++) {
            JSONObject sponsor = data.getJSONObject(i);

            UserInfo user = new UserInfo();

            user.name = sponsor.getString("name");
            user.avatar = sponsor.getString("avatar");
            user.sign = "总金额：" + sponsor.getInt("sum_amount") + "r | 捐赠时间：" + sdf.format(sponsor.getLong("last_time") * 1000);
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
