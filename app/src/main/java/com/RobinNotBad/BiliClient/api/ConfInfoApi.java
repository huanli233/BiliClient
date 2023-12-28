package com.RobinNotBad.BiliClient.api;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
    public static final String USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";
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


    /*
    这里是WBI签名校验
    https://socialsisteryi.github.io/bilibili-API-collect/docs/misc/sign/wbi.html#wbi-%E7%AD%BE%E5%90%8D%E7%AE%97%E6%B3%95
     */
    private static final int[] MIXIN_KEY_ENC_TAB = {46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
        33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
        61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
        36, 20, 34, 44, 52};

    public static String getWBIRawKey() throws IOException, JSONException {
        JSONObject getJson = new JSONObject(Objects.requireNonNull(NetWorkUtil.get("https://api.bilibili.com/x/web-interface/nav", defHeaders).body()).string());
        JSONObject wbi_img = getJson.getJSONObject("data").getJSONObject("wbi_img");  //不要被名称骗了，这玩意是签名用的
        String img_key = LittleToolsUtil.getFileFirstName(LittleToolsUtil.getFileNameFromLink(wbi_img.getString("img_url")));  //得到文件名
        String sub_key = LittleToolsUtil.getFileFirstName(LittleToolsUtil.getFileNameFromLink(wbi_img.getString("sub_url")));

        return img_key + sub_key;  //相连
    }

    public static String getWBIMixinKey(String raw_key){
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            key.append(raw_key.charAt(MIXIN_KEY_ENC_TAB[i]));
        }

        return key.toString();
    }

    //计算时需要按字母顺序排列
    //使用时记得切换web的请求头
    public static String signWBI(String url_query_before_wts,String url_query_after_wts ,String mixin_key) {
        String wts = String.valueOf(System.currentTimeMillis() / 1000);
        String calc_str = sortUrlParams(Uri.encode(url_query_before_wts, "@#&=*+-_.,:!?()/~'%") + "&wts=" + wts + Uri.encode(url_query_after_wts, "@#&=*+-_.,:!?()/~'%") + mixin_key);
        Log.e("calc_str",calc_str);

        String w_rid = md5(calc_str);

        return url_query_before_wts + url_query_after_wts + "&w_rid=" + w_rid + "&wts=" + wts;
    }
    public static String sortUrlParams(String url) {
        // 解析URL参数
        Map<String, String> paramMap = new HashMap<>();
        String[] params = url.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                paramMap.put(keyValue[0], keyValue[1]);
            }else if (keyValue.length == 1) {
                paramMap.put(keyValue[0], "");
            }
        }

        // 使用TreeMap对参数进行排序
        Map<String, String> sortedMap = new TreeMap<>(paramMap);

        // 构建排序后的URL
        StringBuilder sortedUrl = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            if (!isFirst) {
                sortedUrl.append("&");
            } else {
                isFirst = false;
            }
            sortedUrl.append(entry.getKey()).append("=").append(entry.getValue());
        }

        return sortedUrl.toString();
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

            int curr = getDateCurr();

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

    public static int getDateCurr(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) * 10000 + calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DATE);
    }
}
