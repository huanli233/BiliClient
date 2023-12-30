package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity;
import com.RobinNotBad.BiliClient.activity.message.MessageActivity;
import com.RobinNotBad.BiliClient.activity.settings.QRLoginActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingMainActivity;
import com.RobinNotBad.BiliClient.activity.user.MySpaceActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.activity.video.SearchActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

//菜单页面
//2023-07-14

public class MenuActivity extends BaseActivity {

    private int from;
    private final List<Activity> activityList = new ArrayList<Activity>(){{
        add(RecommendActivity.instance);
        add(SearchActivity.instance);
        add(DynamicActivity.instance);
        add(QRLoginActivity.instance);
        add(MySpaceActivity.instance);
        add(MessageActivity.instance);
        add(LocalListActivity.instance);
        add(SettingMainActivity.instance);
    }};
    private final List<Class<?>> classList = new ArrayList<Class<?>>(){{
        add(RecommendActivity.class);
        add(SearchActivity.class);
        add(DynamicActivity.class);
        add(QRLoginActivity.class);
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

        List<MaterialCardView> cardList = new ArrayList<MaterialCardView>() {{
            add(findViewById(R.id.main));
            add(findViewById(R.id.search));
            add(findViewById(R.id.dynamic));
            add(findViewById(R.id.login));
            add(findViewById(R.id.profile));
            add(findViewById(R.id.message));
            add(findViewById(R.id.local));
            add(findViewById(R.id.settings));
            add(findViewById(R.id.exit));
        }};

        if(SharedPreferencesUtil.getLong("mid",0)==0) {
            findViewById(R.id.profile).setVisibility(View.GONE);
            findViewById(R.id.dynamic).setVisibility(View.GONE);
            findViewById(R.id.message).setVisibility(View.GONE);
        }
        else findViewById(R.id.login).setVisibility(View.GONE);

        for (int i = 0; i < cardList.size(); i++) {
            int finalI = i;
            cardList.get(i).setOnClickListener(view -> {
                if(from == finalI) finish();
                else killAndJump(finalI);
            });
        }
/*
        main.setOnClickListener(view -> {
            if(from==0) finish();
            else killAndJump(RecommendActivity.class);
        });

        search.setOnClickListener(view -> {
            if(from==1) finish();
            else killAndJump(SearchActivity.class);
        });

        dynamic.setOnClickListener(view -> {
            if(from==2) finish();
            else killAndJump(DynamicActivity.class);
        });

        profile.setOnClickListener(view -> {
            if(from==3) finish();
            else killAndJump(MySpaceActivity.class);
        });

        local.setOnClickListener(view -> {
            if(from==4) finish();
            else killAndJump(LocalListActivity.class);
        });

        settings.setOnClickListener(view -> {
            if(from==5) finish();
            else killAndJump(SettingMainActivity.class);
        });

        exit.setOnClickListener(view -> {
            activityList.get(from).finish();
        });

 */
    }

    private void killAndJump(int i){
        if(activityList.get(from)!=null) activityList.get(from).finish();

        if(i != activityList.size()) {
            Intent intent = new Intent();
            intent.setClass(MenuActivity.this, classList.get(i));
            startActivity(intent);
        }


        /*
        switch (from){
            case 0:
                RecommendActivity.instance.finish();
                break;
            case 1:
                SearchActivity.instance.finish();
                break;
            case 2:
                DynamicActivity.instance.finish();
                break;
            case 3:
                MySpaceActivity.instance.finish();
                break;
            case 4:
                LocalListActivity.instance.finish();
                break;
            case 5:
                SettingMainActivity.instance.finish();
                break;
        }
         */

        finish();
    }
}