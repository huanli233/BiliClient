package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.settings.SetupUIActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

//启动页面
//一切的一切的开始

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends Activity {

    private TextView splashText;
    private int splashAnim = 0;
    private final String[] splashTextA = {
            "_",
            "欢_",
            "欢迎_",
            "欢迎使_",
            "欢迎使用_",
            "欢迎使用"
    };
    private final String[] splashTextB = {    //我知道这是个笨方法，但是我懒得写啥算法了，这也不占多少空间
            "欢迎使用\n_",
            "欢迎使用\n哔_",
            "欢迎使用\n哔哩_",
            "欢迎使用\n哔哩终_",
            "欢迎使用\n哔哩终端_"
    };

    @Override
    protected void attachBaseContext(Context newBase) {

        SharedPreferencesUtil.initSharedPrefs(newBase);

        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.0F);
        if(dpiTimes != 1.0F && Build.VERSION.SDK_INT >= 25) {    //实测发现，低版本并不是不能调整大小，而是不能放在启动Activity里，所以判断一下就行了，反正启动页无所谓
            Resources res = newBase.getResources();
            Configuration configuration = res.getConfiguration();
            WindowManager windowManager = (WindowManager) newBase.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            int dpi = metrics.densityDpi;
            Log.e("debug-系统dpi", String.valueOf(dpi));
            configuration.densityDpi = (int) (dpi * dpiTimes);
            Log.e("debug-应用dpi", String.valueOf((int) (dpi * dpiTimes)));
            Context confBase =  newBase.createConfigurationContext(configuration);

            super.attachBaseContext(confBase);
        }
        else super.attachBaseContext(newBase);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.e("debug","进入应用");

        splashText = findViewById(R.id.splashText);



        new Thread(()->{

            //FileUtil.clearCache(this);  //先清个缓存（为了防止占用过大）
            //不需要了，我把大部分图片的硬盘缓存都关闭了，只有表情包保留，这样既可以缩减缓存占用又能在一定程度上减少流量消耗

            if(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.setup,false)) {//判断是否设置完成
                try {

                    if(SharedPreferencesUtil.getLong("mid",0)!=0) {
                        UserInfo userInfo = UserInfoApi.getCurrentUserInfo();
                        Log.e("mid", String.valueOf(userInfo.mid));
                        Log.e("accesskey", SharedPreferencesUtil.getString(SharedPreferencesUtil.access_key, ""));
                    }

                    splashAnim = (SharedPreferencesUtil.getBoolean("start_anim",false) ? 0 : 20);

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (splashAnim == 20) {
                                Intent intent = new Intent();
                                intent.setClass(SplashActivity.this, RecommendActivity.class);   //已登录且联网，去首页
                                startActivity(intent);

                                new Thread(()->ConfInfoApi.check(SplashActivity.this)).start();

                                finish();
                                this.cancel();
                            } else {
                                if (splashAnim < 5) {
                                    runOnUiThread(() -> splashText.setText(splashTextA[splashAnim]));
                                } else if (splashAnim > 7 && splashAnim < 12) {
                                    runOnUiThread(() -> splashText.setText(splashTextB[splashAnim - 8]));
                                } else if (splashAnim >= 12) {
                                    runOnUiThread(() -> splashText.setText(splashTextB[4]));
                                }
                            }
                            splashAnim++;
                        }
                    }, 120, 120);
                } catch (IOException e) {
                    runOnUiThread(()-> {
                        MsgUtil.quickErr(MsgUtil.err_net,this);
                        splashText.setText("网络错误");
                        if(SharedPreferencesUtil.getBoolean("setup",false)){
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent();
                                    intent.setClass(SplashActivity.this, LocalListActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            },200);
                        }
                    });
                    e.printStackTrace();
                } catch (JSONException e) {
                    runOnUiThread(()-> {
                        MsgUtil.toast("获取用户信息失败",this);
                        splashText.setText("获取用户信息失败");
                    });
                    e.printStackTrace();
                }
            }
            else {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, SetupUIActivity.class);   //没登录，去初次设置
                startActivity(intent);
                finish();
            }

        }).start();
    }
}