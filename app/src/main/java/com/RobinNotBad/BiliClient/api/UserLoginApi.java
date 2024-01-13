package com.RobinNotBad.BiliClient.api;

import android.graphics.Bitmap;

import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.QRCodeUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Response;

/**
 * Created by liupe on 2018/10/6.
 * 各位大佬好
 * #以下代码修改自腕上哔哩的开源项目，感谢开源者做出的贡献！
 */

public class UserLoginApi
{
    private static String oauthKey;
    private static String sid;
    private static ArrayList<String> defaultHeaders = new ArrayList<>();

    public UserLoginApi()
    {
        sid = String.valueOf(Math.round(Math.random() * 100000000));
        defaultHeaders = new ArrayList<String>()
        {{
            add("User-Agent");
            add(ConfInfoApi.USER_AGENT_OWN);
        }};
    }

    public static Bitmap getLoginQR() throws Exception
    {
        ArrayList<String> headers = new ArrayList<String>()
        {{
            add("Cookie"); add("sid=" + sid);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};

        String url = "https://passport.bilibili.com/x/passport-login/web/qrcode/generate";
        JSONObject loginUrlJson = new JSONObject(Objects.requireNonNull(NetWorkUtil.get(url, headers).body()).string()).getJSONObject("data");
        oauthKey = (String) loginUrlJson.get("qrcode_key");
        return QRCodeUtil.createQRCodeBitmap((String) loginUrlJson.get("url"), 320, 320);
    }

    public static Response getLoginState() throws IOException {
        return NetWorkUtil.get("https://passport.bilibili.com/x/passport-login/web/qrcode/poll?qrcode_key=" + oauthKey, defaultHeaders);
    }

    public static String getCookies(Response response){
        List<String> cookiesList = response.headers("Set-Cookie");
        if(cookiesList.isEmpty())return "";

        StringBuilder cookies = new StringBuilder();
        for (String s : cookiesList) cookies.append(s.split("; ")[0]).append("; ");

        return cookies.substring(0, cookies.length() - 2);
    }
}