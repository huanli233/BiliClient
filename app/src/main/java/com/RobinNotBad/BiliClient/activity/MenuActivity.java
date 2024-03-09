package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity;
import com.RobinNotBad.BiliClient.activity.message.MessageActivity;
import com.RobinNotBad.BiliClient.activity.search.SearchActivity;
import com.RobinNotBad.BiliClient.activity.search.SearchOldActivity;
import com.RobinNotBad.BiliClient.activity.settings.QRLoginActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingMainActivity;
import com.RobinNotBad.BiliClient.activity.settings.SpecialLoginActivity;
import com.RobinNotBad.BiliClient.activity.user.MySpaceActivity;
import com.RobinNotBad.BiliClient.activity.video.PopularActivity;
import com.RobinNotBad.BiliClient.activity.video.PreciousActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

//菜单页面
//2023-07-14

public class MenuActivity extends BaseActivity {

    private int from;

    private final List<Class<? extends InstanceActivity>> classList = new ArrayList<Class<? extends InstanceActivity>>(){{
        add(RecommendActivity.class);
        add(PopularActivity.class);
        add(PreciousActivity.class);
        add(SearchActivity.class);
        add(DynamicActivity.class);
        add(MySpaceActivity.class);
        add(MessageActivity.class);
        add(LocalListActivity.class);
        add(SettingMainActivity.class);
    }};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Log.e("debug","进入菜单页");

        Intent intent = getIntent();
        from = intent.getIntExtra("from",0);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        List<MaterialButton> cardList = new ArrayList<MaterialButton>() {{
            add(findViewById(R.id.menu_recommend));
            add(findViewById(R.id.menu_popular));
            add(findViewById(R.id.menu_precious));
            add(findViewById(R.id.menu_search));
            add(findViewById(R.id.menu_dynamic));
            add(findViewById(R.id.menu_myspace));
            add(findViewById(R.id.menu_message));
            add(findViewById(R.id.menu_local));
            add(findViewById(R.id.menu_settings));
            add(findViewById(R.id.menu_exit));
        }};

        if(SharedPreferencesUtil.getLong("mid",0) == 0) {
            findViewById(R.id.menu_myspace).setVisibility(View.GONE);
            findViewById(R.id.menu_dynamic).setVisibility(View.GONE);
            findViewById(R.id.menu_message).setVisibility(View.GONE);
            findViewById(R.id.menu_login).setOnClickListener(view -> {
                Intent intent1 = new Intent();
                if(Build.VERSION.SDK_INT>=19) intent1.setClass(this,QRLoginActivity.class);
                else{
                    intent1.setClass(this, SpecialLoginActivity.class);
                    intent1.putExtra("login",true);
                }
                startActivity(intent1);
            });
        }
        else findViewById(R.id.menu_login).setVisibility(View.GONE);

        if(!SharedPreferencesUtil.getBoolean("menu_popular",true))
            findViewById(R.id.menu_popular).setVisibility(View.GONE);

        if(!SharedPreferencesUtil.getBoolean("menu_precious",false))
            findViewById(R.id.menu_precious).setVisibility(View.GONE);

        for (int i = 0; i < cardList.size(); i++) {
            int finalI = i;
            cardList.get(i).setOnClickListener(view -> killAndJump(finalI));
        }
        
        //我求求你了退出按钮能用吧....
        findViewById(R.id.menu_exit).setOnClickListener(view -> {
            classList.add(SearchOldActivity.class);
            for(int j = 0;j<classList.size();j++){
                InstanceActivity instance = BiliTerminal.instance;
                if(instance != null && !instance.isDestroyed()) instance.finish();
            }
            finish();
        });

        if(!SharedPreferencesUtil.getBoolean("tutorial_menu",false)){
            MsgUtil.showTutorial(this,"使用教程","点击上方标题栏可以返回上一个页面",R.mipmap.tutorial_menu);
            SharedPreferencesUtil.putBoolean("tutorial_menu",true);
        }
    }

    private void killAndJump(int i){
        if (i <= classList.size() && i != from){
            InstanceActivity instance = BiliTerminal.instance;
            if(instance != null && !instance.isDestroyed()) instance.finish();
            if(i != classList.size()) {
                Intent intent = new Intent();

                if(i==3 && SharedPreferencesUtil.getBoolean("old_search_enable",false)) intent.setClass(this,SearchOldActivity.class);
                else intent.setClass(MenuActivity.this, classList.get(i));

                startActivity(intent);
            }
        }
        finish();
    }

}