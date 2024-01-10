package com.RobinNotBad.BiliClient.api;

import android.util.Base64;
import android.util.Log;

import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import okhttp3.Response;

/*
这里是Cookie刷新逻辑
https://socialsisteryi.github.io/bilibili-API-collect/docs/login/cookie_refresh.html#%E8%8E%B7%E5%8F%96refresh-csrf
 */
public class CookieRefreshApi {
    public static JSONObject cookieInfo() throws IOException, JSONException {
        String url = "https://passport.bilibili.com/x/passport-login/web/cookie/info";
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        return result.getJSONObject("data");
    }

    public static String getCorrespondPath(long timestamp) { //时间戳要精准到毫秒！
        /*
        https://socialsisteryi.github.io/bilibili-API-collect/docs/login/cookie_refresh.html#%E7%94%9F%E6%88%90correspondpath%E7%AE%97%E6%B3%95
         */
        try {
            String publicKeyPEM = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLgd2OAkcGVtoE3ThUREbio0EgUc/prcajMKXvkCKFCWhJYJcLkcM2DKKcSeFpD/j6Boy538YXnR6VhcuUJOhH2x71nzPjfdTcqMz7djHum0qSZA0AyCBDABUqCrfNgCiJ00Ra7GmRj+YCK1NJEuewlb40JNrRuoEUXpabUzGB8QIDAQAB";
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(publicKeyPEM,Base64.DEFAULT));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec("SHA-256","MGF1",MGF1ParameterSpec.SHA256,PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParameterSpec);
            String data = "refresh_" + timestamp;
            return base16Encode(cipher.doFinal(data.getBytes()));
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    
    public static String getRefreshCsrf(String CorrespondPath) throws IOException {
        if(Objects.equals(CorrespondPath, "")) return "";
        String url = "https://www.bilibili.com/correspond/1/" + CorrespondPath;
        Response response = NetWorkUtil.get(url,ConfInfoApi.webHeaders);
        if (response.body() != null) {
            Document document = Jsoup.parse(response.body().string());
            if(document.select("#1-name").size() > 0) return document.select("#1-name").get(0).text();
            else return "";
        }else return "";
    }
    
    public static boolean refreshCookie(String RefreshCsrf) throws JSONException, IOException{
        //必须的参数不能为空
        if(Objects.equals(RefreshCsrf, "")) return false;
        if(Objects.equals(SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token, ""), "")) return false;

        //请求一个新的cookie
        String url = "https://passport.bilibili.com/x/passport-login/web/cookie/refresh";
        String args= "csrf=" + LittleToolsUtil.getInfoFromCookie("bili_jct",SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,"")) + "&refresh_csrf=" + RefreshCsrf + "&source=main_web&refresh_token=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token,"");
        Response response = NetWorkUtil.post(url,args,ConfInfoApi.webHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        if (result.getInt("code") == 0) {
            String cookies_new = "buvid3=" + LittleToolsUtil.getInfoFromCookie("buvid3",SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,"")) + "; " + UserLoginApi.getCookies(response);
            Log.e("新的Cookie",cookies_new);
            String refreshToken_new = result.getJSONObject("data").getString("refresh_token");
            Log.e("新的RefreshToken",refreshToken_new);


            ArrayList<String> requestHeaders = new ArrayList<String>() {{
                add("Cookie");
                add(cookies_new);
                add("Referer");
                add("https://www.bilibili.com/");
                add("User-Agent");
                add(ConfInfoApi.USER_AGENT_WEB);
            }};

            //使老的Cookie失效
            int confirmCode = new JSONObject(Objects.requireNonNull(NetWorkUtil.post("https://passport.bilibili.com/x/passport-login/web/confirm/refresh", "csrf=" + LittleToolsUtil.getInfoFromCookie("bili_jct",cookies_new) + "&refresh_token=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token, ""), requestHeaders).body()).string()).getInt("code");
            if(confirmCode != 0){ //必须要等确认更新Cookie成功，不然就无法完成Cookie的刷新
                Log.e("Cookie刷新失败","确认刷新时返回:" + confirmCode);
                return false;
            }
            SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies,cookies_new);
            SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token,refreshToken_new);
            SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, Long.parseLong(LittleToolsUtil.getInfoFromCookie("DedeUserID", cookies_new)));
            SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf, LittleToolsUtil.getInfoFromCookie("bili_jct", cookies_new));
            Log.e("Cookie刷新成功","Success");
            return true;
        }else{
            Log.e("Cookie刷新失败","刷新时返回:"+ result.getInt("code"));
            return false;
        }
    }
    
    public static String base16Encode(byte[] src) {
        StringBuilder strbuf = new StringBuilder(src.length * 2);
        int i;

        for (i = 0; i < src.length; i++) {
            if (((int) src[i] & 0xff) < 0x10)
                strbuf.append("0");

            strbuf.append(Long.toString((int) src[i] & 0xff, 16));
        }

        return strbuf.toString();
    }
}
