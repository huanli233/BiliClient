package com.RobinNotBad.BiliClient.api;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.RobinNotBad.BiliClient.util.ToolsUtil;
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

    public static final String USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.160 Safari/537.36";


    /*
    这里是WBI签名校验
    https://socialsisteryi.github.io/bilibili-API-collect/docs/misc/sign/wbi.html#wbi-%E7%AD%BE%E5%90%8D%E7%AE%97%E6%B3%95
     */
    private static final int[] MIXIN_KEY_ENC_TAB = {46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
        33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
        61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
        36, 20, 34, 44, 52};

    public static String getWBIRawKey() throws IOException, JSONException {
        JSONObject getJson = new JSONObject(Objects.requireNonNull(NetWorkUtil.get("https://api.bilibili.com/x/web-interface/nav", webHeaders).body()).string());
        JSONObject wbi_img = getJson.getJSONObject("data").getJSONObject("wbi_img");  //不要被名称骗了，这玩意是签名用的
        String img_key = ToolsUtil.getFileFirstName(ToolsUtil.getFileNameFromLink(wbi_img.getString("img_url")));  //得到文件名
        String sub_key = ToolsUtil.getFileFirstName(ToolsUtil.getFileNameFromLink(wbi_img.getString("sub_url")));

        return img_key + sub_key;  //相连
    }

    public static String getWBIMixinKey(String raw_key){
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            key.append(raw_key.charAt(MIXIN_KEY_ENC_TAB[i]));
        }

        return key.toString();
    }

    public static String signWBI(String url_query) throws JSONException, IOException {
        String mixin_key;
        int curr = getDateCurr();
        if (SharedPreferencesUtil.getInt("last_wbi", 0) < curr) {    //限制一天一次
            Log.e("debug", "检查WBI");
            SharedPreferencesUtil.putInt("last_wbi", curr);

            mixin_key = ConfInfoApi.getWBIMixinKey(ConfInfoApi.getWBIRawKey());
            SharedPreferencesUtil.putString("wbi_mixin_key",mixin_key);
        }
        else mixin_key = SharedPreferencesUtil.getString("wbi_mixin_key","");

        String wts = String.valueOf(System.currentTimeMillis() / 1000);
        String calc_str = sortUrlParams(Uri.encode(url_query, "@#&=*+-_.,:!?()/~'%") + "&wts=" + wts) + mixin_key;
        Log.e("calc_str",calc_str);

        String w_rid = md5(calc_str);

        return url_query + "&w_rid=" + w_rid + "&wts=" + wts;
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

    public static ArrayList<String> webHeaders = new ArrayList<String>() {{
        add("Cookie");
        add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));
        add("Referer");
        add("https://www.bilibili.com/");
        add("User-Agent");
        add(USER_AGENT_WEB);
        add("Content-Type");
        add("application/x-www-form-urlencoded");
    }};


    public static void refreshHeaders(){
        webHeaders.set(1,SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));
    }


    public static int getDateCurr(){
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) * 10000 + calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DATE);
    }
}
