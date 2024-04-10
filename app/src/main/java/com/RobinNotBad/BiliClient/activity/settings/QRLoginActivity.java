package com.RobinNotBad.BiliClient.activity.settings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.Guideline;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.SplashActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.LoginApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;

//登录页面，参考了腕上哔哩和WearBili的代码

public class QRLoginActivity extends BaseActivity {
    private ImageView qrImageView;
    private TextView scanStat;
    private int clickCount = 0;
    Bitmap QRImage;
    Timer timer;
    boolean need_refresh = false;
    int qrScale = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.e("debug","进入登录页面");

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
            if(need_refresh) {
                qrImageView.setImageResource(R.mipmap.loading);
                qrImageView.setEnabled(false);
                refreshQrCode();
            }
            else {
                Guideline guideline_left = findViewById(R.id.guideline33);
                Guideline guideline_right = findViewById(R.id.guideline34);
                switch (qrScale){
                    case 0:
                        guideline_left.setGuidelinePercent(0.00f);
                        guideline_right.setGuidelinePercent(1.00f);
                        MsgUtil.toast("切换为大二维码",this);
                        qrScale = 1;
                        break;
                    case 1:
                        guideline_left.setGuidelinePercent(0.30f);
                        guideline_right.setGuidelinePercent(0.70f);
                        MsgUtil.toast("切换为小二维码",this);
                        qrScale = 2;
                        break;
                    case 2:
                        guideline_left.setGuidelinePercent(0.15f);
                        guideline_right.setGuidelinePercent(0.85f);
                        MsgUtil.toast("切换为默认大小",this);
                        qrScale = 0;
                        break;
                }
            }
        });


        refreshQrCode();
    }

    public void refreshQrCode() {

        CenterThreadPool.run(() ->{
            try{
                runOnUiThread(() -> scanStat.setText("正在获取二维码"));
                QRImage = LoginApi.getLoginQR();

                runOnUiThread(() -> {
                    Log.e("debug-image", QRImage.getWidth() + "," + QRImage.getHeight());
                    qrImageView.setImageBitmap(QRImage);
                    startLoginDetect();
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    qrImageView.setEnabled(true);
                    scanStat.setText("获取二维码失败，网络错误");
                });
                e.printStackTrace();
            } catch (JSONException e){
                runOnUiThread(() -> {
                    qrImageView.setEnabled(true);
                    scanStat.setText("登录接口可能失效，请找开发者");
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

    public void startLoginDetect(){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Response response = LoginApi.getLoginState();
                    assert response.body() != null;

                    JSONObject loginJson = new JSONObject(response.body().string());

                    int code = loginJson.getJSONObject("data").getInt("code");
                    switch (code){
                        case 86090:
                            runOnUiThread(() -> scanStat.setText("已扫描，请在手机上点击登录"));
                            break;
                        case 86101:
                            runOnUiThread(() -> scanStat.setText("请使用手机端哔哩哔哩扫码登录\n点击二维码可以进行放大和缩小"));
                            break;
                        case 86038:
                            runOnUiThread(() -> {
                                scanStat.setText("二维码已失效，点击上方重新获取");
                                qrImageView.setEnabled(true);
                            });
                            this.cancel();
                            break;
                        case 0:
                            String cookies = SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,"");

                            SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, Long.parseLong(NetWorkUtil.getInfoFromCookie("DedeUserID", cookies)));
                            SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf, NetWorkUtil.getInfoFromCookie("bili_jct", cookies));
                            SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token,loginJson.getJSONObject("data").getString("refresh_token"));

                            SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.cookie_refresh,true);

                            Log.e("refresh_token",SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token,""));

                            if(SharedPreferencesUtil.getBoolean("setup",false)) {
                                InstanceActivity instance = BiliTerminal.instance;
                                if(instance != null && !instance.isDestroyed()) instance.finish();
                            }
                            else SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.setup,true);

                            ConfInfoApi.refreshHeaders();

                            Intent intent = new Intent();
                            intent.setClass(QRLoginActivity.this, SplashActivity.class);
                            startActivity(intent);

                            finish();
                            this.cancel();
                            break;
                        default:
                            runOnUiThread(()->scanStat.setText("二维码登录API可能变动，\n但你仍然可以尝试扫码登录。\n建议反馈给开发者"));
                            break;
                    }

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        qrImageView.setEnabled(true);
                        scanStat.setText("无法获取二维码信息，点击上方重试");
                        MsgUtil.err(e, QRLoginActivity.this);
                    });
                    this.cancel();
                }
            }
        }, 2000, 500);
    }
}