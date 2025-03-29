package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.BuildConfig;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.update.UpdateInfoActivity;
import com.RobinNotBad.BiliClient.model.Announcement;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class AppInfoApi {
    public static void check(Context context) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour >= 23 || hour <= 3){
            MsgUtil.showDialog("温馨提醒", "夜深了，要注意休息呐~", 3);    //早茶光提供的彩蛋（实际上并不能算作彩蛋，太容易触发了？
        }

        try {
            int version = BiliTerminal.getVersion();
            int curr = ConfInfoApi.getDateCurr();

            checkAnnouncement();

            int last_ver = SharedPreferencesUtil.getInt("app_version_last", 0);
            if (last_ver < version) {
                String update_apk = SharedPreferencesUtil.getString("terminal_update_pkg", "");
                if(!TextUtils.isEmpty(update_apk)){
                    File file = new File(update_apk);
                    if(file.exists()) {
                        if(file.delete()) {
                            SharedPreferencesUtil.putString("terminal_update_pkg", "");
                            MsgUtil.showMsg("更新包已删除");
                        }
                        else MsgUtil.showMsg("更新包删除失败");
                    }
                    else SharedPreferencesUtil.putString("terminal_update_pkg", "");
                }

                MsgUtil.showDialog("提醒", "终端的每个版本都可能会新增一些兼容性相关的设置项\n如果你遇到了某些奇葩问题，请先在设置里找一找相关的兼容性选项，视频放不了、无法加载图片的问题都有可能解决哦~",5);

                if (last_ver != 0) {
                    if (last_ver < 20240606)
                        MsgUtil.showDialog("部分风控问题已解决", "当前的新版本实现了对抗部分类型的风控，建议您重新登录账号以确保成功使用");

                    if (/*last_ver < 20250329 && */SharedPreferencesUtil.getBoolean("player_ui_round",false)){
                        SharedPreferencesUtil.putInt("paddingV_percent", 3);
                        SharedPreferencesUtil.putInt("paddingH_percent", 7);
                    }

                    if (!SharedPreferencesUtil.getString("player", "null").equals("terminalPlayer"))
                        MsgUtil.showDialog("内置播放器", "现在的内置播放器已支持以下特色功能：\n·视频实时观看人数\n·显示直播弹幕\n·强制滚动显示弹幕\n\n欢迎在需要时切换到内置播放器使用哦");
                }
                MsgUtil.showText("更新公告", context.getResources().getString(R.string.update_tip) + "\n\n更新细节：\n" + ToolsUtil.getUpdateLog(context));
                if (ToolsUtil.isDebugBuild())
                    MsgUtil.showDialog("警告", "这个版本是测试版，仅在测试群中发布，禁止外传到如奇妙应用、小趣空间等平台或其他QQ群");
                SharedPreferencesUtil.putInt("app_version_last", version);
            }

            if (SharedPreferencesUtil.getInt("app_version_check", 0) < curr) {    //限制一天一次
                Log.e("debug", "检查更新");
                SharedPreferencesUtil.putInt("app_version_check", curr);

                checkUpdate(context, false);
            }
        } catch (IOException e){
            MsgUtil.showMsg("无法连接到终端公告接口\n也许是服务器宕机了？\n（对软件内容无影响）");
        } catch (Exception e) {
            Log.e("debug-terminal", e.toString());
            MsgUtil.err("终端接口出现问题（不影响软件内容）", e);
        }
    }

    public static final ArrayList<String> customHeaders = new ArrayList<>() {{
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
            MsgUtil.showMsg("版本信息json生成出错\n无影响，正常情况下你应该不会遇到");
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
            MsgUtil.showMsg("设备信息json生成出错\n无影响，正常情况下你应该不会遇到");
        }
    }};

    private static void checkUpdate(Context context, boolean need_toast, boolean debug_ver) {
        try {
            boolean realIsDebug = ToolsUtil.isDebugBuild();
            String url = "http://api.biliterminal.sineworld.cn/terminal/version/get_last";
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
                MsgUtil.showMsg(debug_ver ? "发现新的测试版！" : "发现新版本！");
                context.startActivity(new Intent(context, UpdateInfoActivity.class)
                        .putExtra("versionName", version_name)
                        .putExtra("versionCode", latest)
                        .putExtra("updateLog", update_log)
                        .putExtra("ctime", ctime)
                        .putExtra("isRelease", is_release)
                        .putExtra("canDownload", can_download));
                return;
            } else if (need_toast && !(realIsDebug && !debug_ver)) {
                MsgUtil.showMsg(debug_ver ? "没有新的测试版了！" : "当前是最新版本！");
            }
            if (realIsDebug && !debug_ver) {
                checkUpdate(context, need_toast, true);
            }
        } catch (IOException | JSONException e){
            MsgUtil.err("检查更新：",e);
        } catch (Exception e){
            MsgUtil.showMsg(e.getMessage());
        }
    }

    public static void checkUpdate(Context context, boolean need_toast) {
        checkUpdate(context, need_toast, false);
    }

    public static String getDownloadUrl(int versionCode) throws Exception {
        String url = "https://api.biliterminal.sineworld.cn/terminal/version/get_download_url" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("version_code", versionCode);
        JSONObject result = NetWorkUtil.getJson(url, customHeaders);

        if (result.getInt("code") != 0) throw new Exception("错误：" + result.getString("msg"));

        return result.optString("data");
    }

    public static void checkAnnouncement() throws Exception {
        String url = "http://api.biliterminal.sineworld.cn/terminal/announcement/get_list?from=" + SharedPreferencesUtil.getInt("app_announcement_last", -1);
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
            MsgUtil.showText(title, content);
        }
    }

    public static ArrayList<Announcement> getAnnouncementList() throws Exception {
        String url = "http://api.biliterminal.sineworld.cn/terminal/announcement/get_list";
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

    public static Pair<Integer, String> uploadStack(String stack, Context context) {
        //上传崩溃堆栈
        try {
            String url = "http://api.biliterminal.sineworld.cn/terminal/upload/stack";

            JSONObject post_data = new JSONObject();
            post_data.put("stack", stack);
            post_data.put("client_version", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
            post_data.put("device_sdk", Build.VERSION.SDK_INT);
            post_data.put("device_product", Build.PRODUCT);
            post_data.put("device_brand", Build.BRAND);

            JSONObject res = new JSONObject(Objects.requireNonNull(NetWorkUtil.postJson(url, post_data.toString(), customHeaders).body()).string());
            String msg = (res.getInt("code") == 200) ? "上传成功" : res.getString("msg");
            int id = res.optInt("id",-114);
            return new Pair<>(id, msg);
        } catch (Throwable e) {
            e.printStackTrace();
            return new Pair<>(-1,"上传失败");
        }
    }

    public static int getSponsors(ArrayList<UserInfo> list, int page) throws Exception {
        String url = "http://api.biliterminal.sineworld.cn/terminal/afdian/get_sponsor?page=" + page;
        JSONObject result = NetWorkUtil.getJson(url, customHeaders);

        if (result.getInt("code") != 200) throw new Exception("获取失败");
        JSONArray data = result.getJSONArray("data");

        if(data.length() == 0) return 1;

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");

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
        return 0;
    }
}
