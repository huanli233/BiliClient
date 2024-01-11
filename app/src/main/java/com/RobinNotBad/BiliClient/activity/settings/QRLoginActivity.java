package com.RobinNotBad.BiliClient.activity.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.SplashActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.UserLoginApi;
import com.RobinNotBad.BiliClient.util.*;
import com.google.android.material.card.MaterialCardView;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

//登录页面，参考了腕上哔哩和WearBili的代码

public class QRLoginActivity extends InstanceActivity {
    private ImageView qrImageView;
    private TextView scanStat;
    private int clickCount = 0;
    Bitmap QRImage;
    Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.e("debug","进入登录页面");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        qrImageView = findViewById(R.id.qrImage);
        scanStat = findViewById(R.id.scanStat);


        MaterialCardView jump = findViewById(R.id.jump);        //跳过
        jump.setOnClickListener(view -> {
            if(!SharedPreferencesUtil.getBoolean("setup", false)) {
                SharedPreferencesUtil.putBoolean("setup",true);
                Intent intent = new Intent();
                intent.setClass(QRLoginActivity.this, SplashActivity.class);
                startActivity(intent);
            }
            if(timer!=null) timer.cancel();
            finish();
        });



        scanStat.setOnClickListener(view -> {
            clickCount++;
            Log.e("debug-登录","点");
        });
        scanStat.setOnLongClickListener(view -> {
            Log.e("debug-登录","长按");
            if(clickCount==7){
                Intent intent = new Intent();
                intent.setClass(QRLoginActivity.this, SpecialLoginActivity.class);
                intent.putExtra("login",true);
                startActivity(intent);
                if(timer!=null)timer.cancel();
                finish();
            }else clickCount=0;
            return true;
        });


        qrImageView.setOnClickListener(view -> {
            try {
                refreshQrCode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        try {
            refreshQrCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshQrCode() throws IOException {
        runOnUiThread(()->qrImageView.setEnabled(false));

        qrImageView.setImageResource(R.drawable.loading);
        CenterThreadPool.run(() ->{
            try{
                runOnUiThread(() -> scanStat.setText("正在获取二维码"));
                QRImage = UserLoginApi.getLoginQR();

                runOnUiThread(() -> {
                    Log.e("debug-image", QRImage.getWidth() + "," + QRImage.getHeight());
                    qrImageView.setImageBitmap(QRImage);
                    detectLogin();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    qrImageView.setEnabled(true);
                    scanStat.setText("获取二维码失败，点击上方重试");
                });
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(timer!=null) timer.cancel();
        super.onDestroy();
    }

    public void detectLogin(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Response response = UserLoginApi.getLoginState();
                    assert response.body() != null;

                    JSONObject loginJson = new JSONObject(response.body().string());

                    int code = loginJson.getJSONObject("data").getInt("code");
                    switch (code){
                        case 86090:
                            runOnUiThread(() -> scanStat.setText("已扫描，请在手机上点击登录"));
                            break;
                        case 86101:
                            runOnUiThread(() -> scanStat.setText("请使用手机端哔哩哔哩扫码登录"));
                            break;
                        case 86038:
                            runOnUiThread(() -> {
                                scanStat.setText("二维码已失效，点击上方重新获取");
                                qrImageView.setEnabled(true);
                            });
                            this.cancel();
                            break;
                        case 0:
                            String cookies = UserLoginApi.getCookies(response);


                            List<String> responseCookies = Objects.requireNonNull(Objects.requireNonNull(NetWorkUtil.get("https://www.bilibili.com/")).networkResponse()).headers("Set-Cookie");
                            String buvid3 = "";
                            for (String responseCookie : responseCookies) {
                                if (responseCookie.startsWith("buvid3")) {
                                    buvid3 = responseCookie;
                                }
                            }
                            buvid3=buvid3.substring(buvid3.indexOf("buvid3"),buvid3.indexOf(";",buvid3.indexOf("buvid3"))+1);
                            Log.e("buvid3",buvid3);//获取buvid3

                            SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, Long.parseLong(LittleToolsUtil.getInfoFromCookie("DedeUserID", cookies)));
                            SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf, LittleToolsUtil.getInfoFromCookie("bili_jct", cookies));
                            SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies, buvid3 + cookies);
                            SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token,loginJson.getJSONObject("data").getString("refresh_token"));

                            Log.e("refresh_token",SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token,""));

                            ConfInfoApi.refreshHeaders();

                            if(SharedPreferencesUtil.getBoolean("setup",false)) {
                                Activity instance = InstanceActivity.getInstance(SettingMainActivity.class);
                                if(instance != null) instance.finish();
                            }
                            else {
                                SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.setup,true);
                                Intent intent = new Intent();
                                intent.setClass(QRLoginActivity.this, SplashActivity.class);
                                startActivity(intent);
                            }

                            finish();
                            this.cancel();
                            break;
                        default:
                            runOnUiThread(()->scanStat.setText("二维码登录API可能变动，\n但你仍然可以尝试扫码登录。\n建议反馈给开发者"));
                            break;
                    }

                } catch (IOException e) {
                    runOnUiThread(() -> {
                        qrImageView.setEnabled(true);
                        scanStat.setText("无法获取二维码信息，点击上方重试");
                        MsgUtil.quickErr(MsgUtil.err_net, QRLoginActivity.this);
                    });
                    this.cancel();
                    e.printStackTrace();
                } catch (JSONException e) {
                    runOnUiThread(()-> scanStat.setText(e.toString()));
                    this.cancel();
                    e.printStackTrace();
                }
            }
        }, 2000, 500);
    }
}