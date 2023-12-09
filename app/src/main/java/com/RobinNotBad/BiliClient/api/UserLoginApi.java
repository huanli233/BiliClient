package com.RobinNotBad.BiliClient.api;

import android.graphics.Bitmap;

import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.QRCodeUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
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

    public static Response getLoginState() throws IOException
    {
        return NetWorkUtil.get("https://passport.bilibili.com/x/passport-login/web/qrcode/poll?qrcode_key=" + oauthKey , defaultHeaders);
    }

    public static String getAccessKey(final String cookie) throws IOException
    {
        try
        {
            ArrayList<String> headers1 = new ArrayList<String>()
            {{
                add("Cookie"); add(cookie);
                add("Host"); add("passport.bilibili.com");
                add("Referer"); add("http://www.bilibili.com/");
                add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
            }};
            String url = "https://passport.bilibili.com/login/app/third";
            String temp_per = "api=http://link.acg.tv/forum.php&appkey=27eb53fc9058f8c3&sign=67ec798004373253d60114caaad89a8c";
            Response response = NetWorkUtil.get(url + "?" + temp_per, headers1);

            ArrayList<String> headers2 = new ArrayList<String>()
            {{
                add("Cookie"); add(cookie);
                add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
            }};

            url = new JSONObject(Objects.requireNonNull(response.body()).string()).getJSONObject("data").getString("confirm_uri");

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .followRedirects(false)
                    .followSslRedirects(false).build();
            Request.Builder requestBuilder = new Request.Builder().url(url);
            for (int i = 0; i < headers2.size(); i += 2)
                requestBuilder = requestBuilder.addHeader(headers2.get(i), headers2.get(i + 1));
            Request request = requestBuilder.build();
            response = client.newCall(request).execute();
            String url_location = response.header("location");
            assert url_location != null;
            return url_location.substring(url_location.indexOf("access_key=")+11, url_location.indexOf("&", url_location.indexOf("access_key=")));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "";
    }



}