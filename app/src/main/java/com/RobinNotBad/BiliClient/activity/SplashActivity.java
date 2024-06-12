package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.settings.SetupUIActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.api.CookieRefreshApi;
import com.RobinNotBad.BiliClient.api.CookiesApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

//启动页面
//一切的一切的开始

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends Activity {

    private TextView splashTextView;
    private int splashFrame;
    private Timer splashTimer;
    private final String splashText = "欢迎使用\n哔哩终端";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(BiliTerminal.getFitDisplayContext(newBase));
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_BiliClient);
        setContentView(R.layout.activity_splash);
        Log.e("debug","进入应用");

        splashTextView = findViewById(R.id.splashText);

        splashTimer = new Timer();
        splashTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(()->showSplashText(splashFrame));
                splashFrame++;
                if(splashFrame>splashText.length()) this.cancel();
            }
        },100,100);

        CenterThreadPool.run(()->{

            //FileUtil.clearCache(this);  //先清个缓存（为了防止占用过大）
            //不需要了，我把大部分图片的硬盘缓存都关闭了，只有表情包保留，这样既可以缩减缓存占用又能在一定程度上减少流量消耗

            if(SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.setup,false)) {//判断是否设置完成
                try {
                    // 未登录时请求bilibili.com
                    if (SharedPreferencesUtil.getLong("mid", 0) != 0) {
                        checkCookie();
                    } else {
                        // [开发者]RobinNotBad: 如果提前不请求bilibili.com，未登录时的推荐有概率一直返回同样的内容
                        NetWorkUtil.get("https://www.bilibili.com", NetWorkUtil.webHeaders);
                    }

                    CookiesApi.checkCookies();

                    String firstActivity = null;
                    String sortConf = SharedPreferencesUtil.getString(SharedPreferencesUtil.MENU_SORT, "");
                    if (!TextUtils.isEmpty(sortConf)) {
                        String[] splitName = sortConf.split(";");
                        for (String name : splitName) {
                            if (!MenuActivity.btnNames.containsKey(name)) {
                                for (Map.Entry<String, Pair<String, Class<? extends InstanceActivity>>> entry : MenuActivity.btnNames.entrySet()) {
                                    firstActivity = entry.getKey();
                                    break;
                                }
                            } else {
                                firstActivity = name;
                            }
                            break;
                        }
                    } else {
                        for (Map.Entry<String, Pair<String, Class<? extends InstanceActivity>>> entry : MenuActivity.btnNames.entrySet()) {
                            firstActivity = entry.getKey();
                            break;
                        }
                    }

                    Class<? extends InstanceActivity> activityClass = Objects.requireNonNull(MenuActivity.btnNames.get(firstActivity)).second;

                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, (activityClass != null ? activityClass : RecommendActivity.class));
                    intent.putExtra("from", firstActivity);
                    startActivity(intent);

                    CenterThreadPool.run(() -> AppInfoApi.check(SplashActivity.this));

                    interruptSplash();
                    finish();
                } catch (IOException e) {
                    runOnUiThread(()-> {
                        MsgUtil.err(e,this);
                        splashTextView.setText("网络错误");
                        if(SharedPreferencesUtil.getBoolean("setup",false)){
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent();
                                    intent.setClass(SplashActivity.this, LocalListActivity.class);
                                    startActivity(intent);
                                    interruptSplash();
                                    finish();
                                }
                            },200);
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(()-> MsgUtil.err(e,this));
                    Intent intent = new Intent();
                    intent.setClass(SplashActivity.this, LocalListActivity.class);
                    startActivity(intent);
                    interruptSplash();
                    finish();
                }
            }
            else {
                Intent intent = new Intent();
                intent.setClass(SplashActivity.this, SetupUIActivity.class);   //没登录，去初次设置
                startActivity(intent);
                interruptSplash();
                finish();
            }

        });
    }

    private void checkCookie() {
        try{
            JSONObject cookieInfo = CookieRefreshApi.cookieInfo();
            if(cookieInfo.getBoolean("refresh")){
                Log.e("Cookie","需要刷新");
                if(Objects.equals(SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token, ""), "")) runOnUiThread(()-> MsgUtil.toastLong("无法刷新Cookie，请重新登录！",this));
                else{
                    String correspondPath = CookieRefreshApi.getCorrespondPath(cookieInfo.getLong("timestamp"));
                    Log.e("CorrespondPath",correspondPath);
                    String refreshCsrf = CookieRefreshApi.getRefreshCsrf(correspondPath);
                    Log.e("RefreshCsrf",refreshCsrf);
                    if(CookieRefreshApi.refreshCookie(refreshCsrf)){
                        NetWorkUtil.refreshHeaders();
                        runOnUiThread(()-> MsgUtil.toast("Cookie已刷新",this));
                    }
                    else {
                        runOnUiThread(()->MsgUtil.toastLong("登录信息过期，请重新登录！",this));
                        resetLogin();
                    }
                }
            }
        }catch (JSONException e){
            runOnUiThread(()->MsgUtil.toastLong("登录信息过期，请重新登录！",this));
            resetLogin();
        }catch (IOException e){
            runOnUiThread(()->MsgUtil.err(e,this));
        }
    }

    private void resetLogin(){
        SharedPreferencesUtil.putLong(SharedPreferencesUtil.mid, 0L);
        SharedPreferencesUtil.putString(SharedPreferencesUtil.csrf, "");
        SharedPreferencesUtil.putString(SharedPreferencesUtil.cookies, "");
        SharedPreferencesUtil.putString(SharedPreferencesUtil.refresh_token, "");
        NetWorkUtil.refreshHeaders();
    }

    @SuppressLint("SetTextI18n")
    private void showSplashText(int i){
        if(i> splashText.length()) splashTextView.setText(splashText);
        else splashTextView.setText(splashText.substring(0,i) + "_");
    }

    private void interruptSplash(){
        if(splashTimer!=null) splashTimer.cancel();
        runOnUiThread(()->splashTextView.setText(splashText));
    }
}