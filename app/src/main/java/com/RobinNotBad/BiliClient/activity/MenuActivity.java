package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity;
import com.RobinNotBad.BiliClient.activity.message.MessageActivity;
import com.RobinNotBad.BiliClient.activity.search.SearchActivity;
import com.RobinNotBad.BiliClient.activity.settings.LoginActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingMainActivity;
import com.RobinNotBad.BiliClient.activity.settings.SpecialLoginActivity;
import com.RobinNotBad.BiliClient.activity.user.MySpaceActivity;
import com.RobinNotBad.BiliClient.activity.video.PopularActivity;
import com.RobinNotBad.BiliClient.activity.video.PreciousActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

import java.util.*;

//菜单页面
//2023-07-14

public class MenuActivity extends BaseActivity {

    private int from;
    public static final Map<Integer, Class<? extends InstanceActivity>> activityClasses = Map.of(
        R.id.menu_recommend, RecommendActivity.class,
        R.id.menu_popular, PopularActivity.class,
        R.id.menu_precious, PreciousActivity.class,
        R.id.menu_search, SearchActivity.class,
        R.id.menu_dynamic, DynamicActivity.class,
        R.id.menu_myspace, MySpaceActivity.class,
        R.id.menu_message, MessageActivity.class,
        R.id.menu_local, LocalListActivity.class,
        R.id.menu_settings, SettingMainActivity.class
    );

    public static final Map<String, Pair<String, Integer>> btnNames = Map.of(
            "recommend", new Pair<>("推荐", R.id.menu_recommend),
            "popular", new Pair<>("热门", R.id.menu_popular),
            "precious", new Pair<>("入站必刷", R.id.menu_precious),
            "search", new Pair<>("搜索", R.id.menu_search),
            "dynamic", new Pair<>("动态", R.id.menu_dynamic),
            "myspace", new Pair<>("我的", R.id.menu_myspace),
            "message", new Pair<>("消息", R.id.menu_message),
            "local", new Pair<>("缓存", R.id.menu_local),
            "settings", new Pair<>("设置", R.id.menu_settings)
    );

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Log.e("debug","进入菜单页");

        Intent intent = getIntent();
        from = intent.getIntExtra("from",0);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        List<MaterialButton> cardList;

        String sortConf = SharedPreferencesUtil.getString(SharedPreferencesUtil.MENU_SORT, "");
        if (!TextUtils.isEmpty(sortConf)) {
            cardList = new ArrayList<>();
            String[] splitName = sortConf.split(";");
            if (splitName.length != btnNames.size()) {
                cardList = getDefaultSortList();
            } else {
                for (String name : splitName) {
                    if (!btnNames.containsKey(name)) {
                        cardList = getDefaultSortList();
                        break;
                    } else {
                        int id = Objects.requireNonNull(btnNames.get(name)).second;
                        cardList.add(findViewById(id));
                    }
                }
            }
        } else {
            cardList = getDefaultSortList();
        }

        LinearLayout layout = findViewById(R.id.menu_layout);
        cardList.add(0, findViewById(R.id.menu_login));
        cardList.add(findViewById(R.id.menu_exit));
        layout.removeAllViews();
        for (int i = 0; i < cardList.size(); i++) {
            MaterialButton button = cardList.get(i);
            try {
                layout.addView(button);
            } catch (IllegalStateException e) {
                layout.removeView(button);
                layout.addView(button);
            }
            button.setOnClickListener(view -> killAndJump(view.getId()));
        }

        if(SharedPreferencesUtil.getLong("mid",0) == 0) {
            findViewById(R.id.menu_myspace).setVisibility(View.GONE);
            findViewById(R.id.menu_dynamic).setVisibility(View.GONE);
            findViewById(R.id.menu_message).setVisibility(View.GONE);
            findViewById(R.id.menu_login).setOnClickListener(view -> {
                Intent intent1 = new Intent();
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) intent1.setClass(this, LoginActivity.class);
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

        //我求求你了退出按钮能用吧....
        findViewById(R.id.menu_exit).setOnClickListener(view -> {
            InstanceActivity instance = BiliTerminal.instance;
            if(instance != null && !instance.isDestroyed()) instance.finish();
            finish();
        });
    }

    private void killAndJump(int i){
        if (activityClasses.containsKey(i) && i != from) {
            InstanceActivity instance = BiliTerminal.instance;
            if(instance != null && !instance.isDestroyed()) instance.finish();

            Intent intent = new Intent();
            intent.setClass(MenuActivity.this, activityClasses.get(i));
            intent.putExtra("from", i);
            startActivity(intent);
        }
        finish();
    }

    private List<MaterialButton> getDefaultSortList() {
        return new ArrayList<>(Arrays.asList(
            findViewById(R.id.menu_recommend),
            findViewById(R.id.menu_popular),
            findViewById(R.id.menu_precious),
            findViewById(R.id.menu_search),
            findViewById(R.id.menu_dynamic),
            findViewById(R.id.menu_myspace),
            findViewById(R.id.menu_message),
            findViewById(R.id.menu_local),
            findViewById(R.id.menu_settings),
            findViewById(R.id.menu_exit)
        ));
    }

}