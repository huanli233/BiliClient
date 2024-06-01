package com.RobinNotBad.BiliClient.util;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.Inflater;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RetryAndFollowUpInterceptor;

/**
 * 被 luern0313 创建于 2019/10/13.
 * #以下代码来源于腕上哔哩的开源项目，感谢开源者做出的贡献！
 */

public class NetWorkUtil
{
    private static final AtomicReference<OkHttpClient> INSTANCE = new AtomicReference<>();

    private static OkHttpClient getOkHttpInstance() {
        while (INSTANCE.get() == null) {
            INSTANCE.compareAndSet(null, new OkHttpClient.Builder()
                    .followRedirects(false)
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        Response response = chain.proceed(request);
                        RedirectHandler handler;
                        String location = response.header("Location");
                        boolean isSslRedirect = false;
                        try {
                            isSslRedirect = location != null && !request.isHttps() && new URI(location).getScheme().equalsIgnoreCase("https") && request.url().host().equalsIgnoreCase(new URI(location).getHost());
                        } catch (URISyntaxException ignored) {}
                        if ((response.code() == 301 || response.code() == 302) && location != null && request.url().host().equals("b23.tv") && !isSslRedirect && (handler = request.tag(RedirectHandler.class)) != null) {
                            handler.handleRedirect(location);
                        } else if (response.isRedirect() && location != null) {
                            Request newRequest = request.newBuilder()
                                    .url(location)
                                    .build();

                            response = chain.proceed(newRequest);
                        }

                        return response;
                    })
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS).build());
        }
        return INSTANCE.get();
    }

    public static JSONObject getJson(String url) throws IOException, JSONException{
        ResponseBody body = get(url).body();
        if(body!=null) return new JSONObject(body.string());
        else throw new JSONException("在访问" + url + "时返回数据为空");
    }

    public static JSONObject getJson(String url, ArrayList<String> headers) throws IOException, JSONException{
        ResponseBody body = get(url,headers).body();
        if(body!=null) return new JSONObject(body.string());
        else throw new JSONException("在访问" + url + "时返回数据为空");
    }

    public static Response get(String url) throws IOException
    {
        return get(url, webHeaders);
    }

    public static Response get(String url, ArrayList<String> headers) throws IOException {
        return get(url, headers, null);
    }

    public static Response get(String url, ArrayList<String> headers, RedirectHandler redirectHandler) throws IOException {
        Log.e("debug-get","----------------");
        Log.e("debug-get-url",url);
        Log.e("debug-get","----------------");
        OkHttpClient client = getOkHttpInstance();
        Request.Builder requestBuilder = new Request.Builder().url(url).get();
        for(int i = 0; i < headers.size(); i+=2)
            requestBuilder = requestBuilder.addHeader(headers.get(i), headers.get(i+1));
        if (redirectHandler != null) requestBuilder.tag(RedirectHandler.class, redirectHandler);
        Request request = requestBuilder.build();
        Response response =  client.newCall(request).execute();
        saveCookiesFromResponse(response);
        return response;
    }

    public static Response post(String url, String data, ArrayList<String> headers, String contentType) throws IOException
    {
        Log.e("debug-post","----------------");
        Log.e("debug-post-url",url);
        Log.e("debug-post-data",data);
        Log.e("debug-post","----------------");
        OkHttpClient client = getOkHttpInstance();
        RequestBody body = RequestBody.create(MediaType.parse(contentType + "; charset=utf-8"), data);
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        for (int i = 0; i < headers.size(); i+=2) {
            String key = headers.get(i);
            String val = headers.get(i + 1);
            if (key.equalsIgnoreCase("Content-Type")) val = contentType;
            requestBuilder = requestBuilder.addHeader(key, val);
        }
        Request request = requestBuilder.build();
        Response response =  client.newCall(request).execute();
        saveCookiesFromResponse(response);
        return response;
    }

    public static Response post(String url, String data, ArrayList<String> headers) throws IOException {
        return post(url, data, headers, "application/x-www-form-urlencoded");
    }

    public static Response postJson(String url, String data, ArrayList<String> headers) throws IOException {
        return post(url, data, headers, "application/json");
    }

    public static Response postJson(String url, String data) throws IOException {
        return post(url, data, webHeaders, "application/json");
    }

    public static Response post(String url, String data) throws IOException {
        return post(url, data, webHeaders);
    }


    public static byte[] readStream(InputStream inStream) throws IOException
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1)
        {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public static byte[] uncompress(byte[] inputByte) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputByte.length);
        try
        {
            Inflater inflater = new Inflater(true);
            inflater.setInput(inputByte);
            byte[] buffer = new byte[4 * 1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        byte[] output = outputStream.toByteArray();
        outputStream.close();
        return output;
    }

    public static String getInfoFromCookie(String name, String cookie)
    {
        String[] cookies = cookie.split("; ");
        for(String i : cookies)
        {
            if(i.contains(name + "="))
                return i.substring(name.length() + 1);
        }
        return "";
    }

    private static void saveCookiesFromResponse(Response response) {
        List<String> newCookies = response.headers("Set-Cookie");

        //如果没有新cookies，直接返回
        if (newCookies.isEmpty()) return;
        String cookiesStr = SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, "");
        ArrayList<String> oldCookies = (cookiesStr.equals("") ? new ArrayList<>() : new ArrayList<>(Arrays.asList(cookiesStr.split("; "))));  //转list

        for (String newCookie : newCookies) {  //对每一条新cookie遍历

            int index = newCookie.indexOf("; ");
            if (index != -1) newCookie = newCookie.substring(0, index);  //如果没有分号不做处理

            index = newCookie.indexOf("=") + 1;
            if(index == 0) continue;   //如果没有等号，跳过

            String key = newCookie.substring(0, index);    //key=
            Log.e("debug-newCookie", newCookie);

            boolean added = false;
            for (int i = 0; i < oldCookies.size(); i++) {  //查找旧cookie表有没有
                String oldCookie = oldCookies.get(i);
                if (oldCookie.contains(key)) {
                    oldCookies.set(i, newCookie);    //有的话直接换掉
                    added = true;
                    break;
                }
            }
            if (!added) {
                oldCookies.add(newCookie);  //没有就加项
            }
        }

        StringBuilder setCookies = new StringBuilder();
        for (String setCookie : oldCookies) {
            setCookies.append(setCookie).append("; ");
        }
        //如果一次setCookies都没有，就不要存了， 因为是个空字符串
        if(setCookies.length() >= 2) {
            Log.e("debug-save-result", setCookies.substring(0, setCookies.length() - 2));
            SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies, setCookies.substring(0, setCookies.length() - 2));
            refreshHeaders();
        }
    }

    public static final String USER_AGENT_WEB = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.6045.160 Safari/537.36";
    public static ArrayList<String> webHeaders = new ArrayList<>() {{
        add("Cookie");
        add(SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies, ""));
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
    public static class FormData {
        private final Map<String, String> data;
        private boolean isUrlParam;

        public FormData() {
            data = new HashMap<>();
        }

        public FormData remove(String key) {
            data.remove(key);
            return this;
        }

        public FormData put(String key, Object value) {
            data.put(key, String.valueOf(value));
            return this;
        }

        public FormData setUrlParam(boolean isUrlParam) {
            this.isUrlParam = isUrlParam;
            return this;
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (isUrlParam) sb.append("?");

            try {
                for (String key : data.keySet()) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(URLEncoder.encode(key, "UTF-8"));
                    sb.append("=");
                    sb.append(URLEncoder.encode(data.get(key), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            return sb.toString();
        }
    }

    public interface RedirectHandler {
        void handleRedirect(String location);
    }

}
