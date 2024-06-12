package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

public class SettingMainActivity extends InstanceActivity {

    private int eggClick = 0;
    private boolean tutorialReset_clicked = false;

    @SuppressLint({"MissingInflatedId", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_setting_main, null, (layoutView, layoutId, parent) -> {
            setContentView(R.layout.activity_setting_main);
            setMenuClick();
            Log.e("debug","进入设置页");
 
            //查看登录信息
            MaterialCardView login_cookie = findViewById(R.id.login_cookie);
            login_cookie.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SpecialLoginActivity.class);
                intent.putExtra("login",false);
                startActivity(intent);
            });
                
            //登录
            MaterialCardView login = findViewById(R.id.login);
            if(SharedPreferencesUtil.getLong("mid",0)==0) {
                login_cookie.setVisibility(View.GONE);
                login.setVisibility(View.VISIBLE);
                login.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    if(Build.VERSION.SDK_INT>=19)
                        intent.setClass(this, LoginActivity.class);   //去扫码登录页面
                    else{
                        intent.setClass(this, SpecialLoginActivity.class);   //4.4以下系统去特殊登录页面
                        intent.putExtra("login",true);
                    }
                    startActivity(intent);
                });
            }

            //播放器选择
            MaterialCardView playerSetting = findViewById(R.id.playerSetting);
            playerSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingPlayerChooseActivity.class);
                startActivity(intent);
            });


            //内置播放器设置
            MaterialCardView clientPlayerSetting = findViewById(R.id.clientPlayerSetting);
            clientPlayerSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingPlayerInsideActivity.class);
                startActivity(intent);
            });

            //界面设置
            MaterialCardView uiSetting = findViewById(R.id.uiSetting);
            uiSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingUIActivity.class);
                startActivity(intent);
            });

            // 菜单设置
            MaterialCardView menuSetting = findViewById(R.id.menuSetting);
            menuSetting.setOnClickListener(view -> startActivity(new Intent(this, SettingMenuActivity.class)));

            //偏好设置
            MaterialCardView prefSetting = findViewById(R.id.prefSetting);
            prefSetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingPrefActivity.class);
                startActivity(intent);
            });

            //实验性设置
            MaterialCardView laboratorySetting = findViewById(R.id.laboratory);
            laboratorySetting.setVisibility(View.GONE);
            laboratorySetting.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, SettingLaboratoryActivity.class);
                startActivity(intent);
            });

            //关于
            MaterialCardView about = findViewById(R.id.about);
            about.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, AboutActivity.class);
                startActivity(intent);
            });

            //彩蛋
            String[] eggList = getResources().getStringArray(R.array.eggs);
            about.setOnLongClickListener(view -> {
                MsgUtil.showText(this,"彩蛋",eggList[eggClick]);
                if(eggClick<eggList.length-1) eggClick++;
                return true;
            });

            //检查更新
            MaterialCardView checkUpdate = findViewById(R.id.checkupdate);
            checkUpdate.setOnClickListener(view -> {
                MsgUtil.toast("正在获取...",this);
                CenterThreadPool.run(() -> {
                    try {
                        AppInfoApi.checkUpdate(this,true,false);
                    } catch (Exception e) {
                        runOnUiThread(()->MsgUtil.err(e,this));
                    }
                });
            });

            //公告列表
            MaterialCardView announcement = findViewById(R.id.announcement);
            announcement.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this,AnnouncementsActivity.class);
                startActivity(intent);
            });

            MaterialCardView test = findViewById(R.id.test);    //用于测试
            test.setVisibility(View.GONE);
            test.setOnClickListener(view -> {
            });
        });
    }
}