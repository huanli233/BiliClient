package com.RobinNotBad.BiliClient.api;


import android.util.Base64;
import android.util.Log;

import com.RobinNotBad.BiliClient.util.NetWorkUtil;

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
        写不下去了tmd按照
        https://socialsisteryi.github.io/bilibili-API-collect/docs/login/cookie_refresh.html#%E7%94%9F%E6%88%90correspondpath%E7%AE%97%E6%B3%95
        写，算出来的结果不对
        研究好久了
        放弃了
        你们自己看吧
        我也不知道怎么弄了
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
            byte[] result = cipher.doFinal(data.getBytes("UTF-8"));
            return ""+result;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
}
