package com.RobinNotBad.BiliClient.api;

import android.util.Base64;
import android.util.Log;

import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
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
        Response response = NetWorkUtil.get(url,ConfInfoApi.defHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        return result.getJSONObject("data");
    }

    public static String getCorrespondPath(long timestamp) {
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
        if(CorrespondPath.equals("")) return "";
        String url = "https://www.bilibili.com/correspond/1/" + CorrespondPath;
        Response response = NetWorkUtil.get(url,ConfInfoApi.defHeaders);
        String body = response.body().string();
        if (body.contains("<div id=\"1-name\">")){ //手动截断提取html
            body = body.substring(body.indexOf("<div id=\"1-name\">") + 17);
            body = body.substring(0,body.indexOf("</div>"));
            return body;
        }else return "";
    }
    
    public static boolean refreshCookie(String RefreshCsrf) throws JSONException, IOException{
        if(RefreshCsrf.equals("")) return false;
        String url = "https://passport.bilibili.com/x/passport-login/web/cookie/refresh";
        String args= "csrf=" + LittleToolsUtil.getInfoFromCookie("bili_jct",SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,"")) + "&refresh_csrf=" + RefreshCsrf + "&source=main_web&refresh_token=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token,"");
        Response response = NetWorkUtil.post(url,args,ConfInfoApi.defHeaders);
        JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
        if (result.getInt("code") == 0) {
            String cookies = "buvid3=" + LittleToolsUtil.getInfoFromCookie("buvid3",SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,"")) + "; " + UserLoginApi.getCookies(response);
            Log.e("新的Cookie",cookies);
            Log.e("新的RefreshToken",result.getJSONObject("data").getString("refresh_token"));
            int confirmCode = new JSONObject(NetWorkUtil.post("https://passport.bilibili.com/x/passport-login/web/confirm/refresh","csrf=" + LittleToolsUtil.getInfoFromCookie("bili_jct",cookies) + "&refresh_token=" + SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token,""),ConfInfoApi.defHeaders).body().string()).getInt("code");
            if(confirmCode != 0){
                Log.e("Cookie刷新失败","Confirm: " + String.valueOf(confirmCode));
                return false;
            }
            SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies,cookies);
            SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token,result.getJSONObject("data").getString("refresh_token")); 
            return true;
        }else{
            Log.e("Cookie刷新失败","Refresh: "+String.valueOf(result.getInt("code")));
            return false;
        }
    }
    
    public static String base16Encode(byte src[])throws Exception {
        StringBuffer strbuf = new StringBuffer(src.length * 2);
        int i;

        for (i = 0; i < src.length; i++) {
            if (((int) src[i] & 0xff) < 0x10)
                strbuf.append("0");

            strbuf.append(Long.toString((int) src[i] & 0xff, 16));
        }

        return strbuf.toString();
    }
}
