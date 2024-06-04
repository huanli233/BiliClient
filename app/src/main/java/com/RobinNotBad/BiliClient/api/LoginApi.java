package com.RobinNotBad.BiliClient.api;

import android.graphics.Bitmap;

import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.QRCodeUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Response;

/**
 * Created by liupe on 2018/10/6.
 * 各位大佬好
 * #以下代码修改自腕上哔哩的开源项目，感谢开源者做出的贡献！
 */

public class LoginApi
{
    private static String oauthKey;
    private static String sid;
    private static ArrayList<String> headers = new ArrayList<>();

    public static Bitmap getLoginQR() throws JSONException,IOException
    {
        sid = String.valueOf(Math.round(Math.random() * 100000000));

        headers = new ArrayList<String>()
        {{
            add("Cookie"); add("sid=" + sid);
            add("User-Agent"); add(NetWorkUtil.USER_AGENT_WEB);
            add("Referer"); add("https://www.bilibili.com/");
        }};

        String url = "https://passport.bilibili.com/x/passport-login/web/qrcode/generate?source=main-fe-header";
        JSONObject loginUrlJson = NetWorkUtil.getJson(url,headers).getJSONObject("data");
        oauthKey = (String) loginUrlJson.get("qrcode_key");
        return QRCodeUtil.createQRCodeBitmap((String) loginUrlJson.get("url"), 320, 320);
    }

    public static Response getLoginState() throws IOException {
        return NetWorkUtil.get("https://passport.bilibili.com/x/passport-login/web/qrcode/poll?source=main-fe-header?qrcode_key=" + oauthKey, headers);
    }

    public static void requestSSOs() throws JSONException, IOException {
        String listUrl = "https://passport.bilibili.com/x/passport-login/web/sso/list";
        JSONObject listResult = new JSONObject(NetWorkUtil.post(listUrl, new NetWorkUtil.FormData().put("csrf", SharedPreferencesUtil.getString(SharedPreferencesUtil.csrf, "")).toString()).body().string());
        if (listResult.has("data") && !listResult.isNull("data")) {
            JSONArray sso = listResult.getJSONObject("data").getJSONArray("sso");
            for (int i = 0; i < sso.length(); i++) {
                NetWorkUtil.post(sso.getString(i), "");
            }
        }
    }

}