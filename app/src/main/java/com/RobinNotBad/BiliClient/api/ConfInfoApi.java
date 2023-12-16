package com.RobinNotBad.BiliClient.api;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.SplashActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.Response;

/**
 * 被 luern0313 创建于 2019/8/25.
 * (人尽皆知的)绝 · 密 · 档 · 案
 * #以下代码修改自腕上哔哩的开源项目，感谢开源者做出的贡献！
 */

public class ConfInfoApi
{
    public static File getDownloadPath(Context context){
        return new File(Environment.getExternalStorageDirectory() + "/Android/media/" + context.getPackageName() + "/");
    }

    public static final String USER_AGENT_DEF = "Mozilla/5.0 BiliDroid/4.34.0 (bbcallen@gmail.com)";
    public static final String USER_AGENT_OWN = "BiliClient/2.2 (robin_0229@qq.com; bilibili@RobinNotBad;)";
    public static final String USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36";
    private static final HashMap<String, String> conf = new HashMap<String, String>(){{
        put("appkey", "1d8b6e7d45233436");  //27eb53fc9058f8c3
        put("actionKey", "appkey");
        put("build", "520001");  //70000100
        put("device", "android");
        put("mobi_app", "android");
        put("platform", "android");
        put("app_secret", "560c52ccd288fed045859ed18bffd973");
    }};

    private static final HashMap<String, String> tvConf = new HashMap<String, String>(){{
        put("appkey", "4409e2ce8ffd12b8");
        put("actionKey", "appkey");
        put("build", "520001");
        put("device", "android_tv");
        put("mobi_app", "android");
        put("platform", "android");
        put("app_secret", "59b43e04ad6965f34319062b478f83dd");
    }};

    private static final HashMap<String, String> bConf = new HashMap<String, String>(){{
        put("appkey", "07da50c9a0bf829f");
        put("actionKey", "appkey");
        put("build", "520001");
        put("device", "android_b");
        put("mobi_app", "android");
        put("platform", "android");
        put("app_secret", "25bdede4e1581c836cab73a48790ca6e");
    }};

    public static String getConf(String key)
    {
        return conf.get(key);
    }

    public static String getTVConf(String key)
    {
        return tvConf.get(key);
    }

    public static String getBConf(String key)
    {
        return bConf.get(key);
    }

    public static String calc_sign(String str, String salt)
    {
        str += salt;
        return md5(str);
    }

    private static String md5(String plainText) {
        byte[] secretBytes;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            secretBytes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有md5这个算法！");
        }
        StringBuilder md5code = new StringBuilder(new BigInteger(1, secretBytes).toString(16));
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code.insert(0, "0");
        }
        return md5code.toString();
    }

    public static ArrayList<String> defHeaders = new ArrayList<String>() {{
        add("Cookie");
        add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));
        add("Referer");
        add("https://www.bilibili.com/");
        add("User-Agent");
        add(USER_AGENT_DEF);
    }};

    public static ArrayList<String> webHeaders = new ArrayList<String>() {{
        add("Cookie");
        add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));
        add("Referer");
        add("https://www.bilibili.com/");
        add("User-Agent");
        add(USER_AGENT_WEB);
    }};


    public static void check(Context context){
        try {
            int version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            Calendar calendar = Calendar.getInstance();
            int curr = calendar.get(Calendar.YEAR) * 10000 + calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DATE);

            if(SharedPreferencesUtil.getInt("last_check",0) < curr) {    //限制一天一次
                Log.e("debug","检查更新");
                SharedPreferencesUtil.putInt("last_check", curr);

                String url = "https://biliclient.rth1.link/check.json";
                Response response = NetWorkUtil.get(url, webHeaders);
                JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
                if (result.getInt("code") == 0) {
                    int latest = result.getInt("latest_version");
                    int announcement = result.getInt("announcement");
                    if (latest > SharedPreferencesUtil.getInt("latest",version)) {
                        SharedPreferencesUtil.putInt("latest",latest);
                        getUpdate(context);
                    } else if (SharedPreferencesUtil.getInt("last_version", 0) < version) {
                        MsgUtil.showText(context, "更新公告", context.getString(R.string.update_log));
                        SharedPreferencesUtil.putInt("last_version", version);
                    }
                    if (announcement > SharedPreferencesUtil.getInt("announcement", 0)) {
                        SharedPreferencesUtil.putInt("announcement", announcement);
                        getAnnouncement(context);
                    }
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getUpdate(Context context) throws IOException, JSONException, PackageManager.NameNotFoundException {
        String url = "https://biliclient.rth1.link/update.json";
        Response response = NetWorkUtil.get(url,webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        String title = result.getString("title");
        String content = result.getString("content");
        int latest = result.getInt("pubdate");
        int version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        if(latest>version) MsgUtil.showText(context,title,content);
        else MsgUtil.showText(context,"检查更新","您现在是最新版本！");
    }

    public static void getAnnouncement(Context context) throws IOException, JSONException {
        String url = "https://biliclient.rth1.link/announcement.json";
        Response response = NetWorkUtil.get(url,webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        String title = result.getString("title");
        String content = result.getString("content");
        MsgUtil.showText(context,title,content);
    }
}
