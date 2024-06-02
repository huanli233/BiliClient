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
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//菜单页面
//2023-07-14

public class MenuActivity extends BaseActivity {

    private int from;
    private static final Map<Integer, Class<? extends InstanceActivity>> activityClasses = new HashMap<>() {{
        put(R.id.menu_recommend, RecommendActivity.class);
        put(R.id.menu_popular, PopularActivity.class);
        put(R.id.menu_precious, PreciousActivity.class);
        put(R.id.menu_search, DynamicActivity.class);
        put(R.id.menu_dynamic, DynamicActivity.class);
        put(R.id.menu_myspace, MySpaceActivity.class);
        put(R.id.menu_message, MessageActivity.class);
        put(R.id.menu_local, LocalListActivity.class);
        put(R.id.menu_settings, SettingMainActivity.class);
    }};

    public static final Map<String, Pair<String, Integer>> btnNames = new LinkedHashMap<>() {{
        put("recommend", new Pair<>("推荐", R.id.menu_recommend));
        put("popular", new Pair<>("热门", R.id.menu_popular));
        put("precious", new Pair<>("入站必刷", R.id.menu_precious));
        put("search", new Pair<>("搜索", R.id.menu_search));
        put("dynamic", new Pair<>("动态", R.id.menu_dynamic));
        put("myspace", new Pair<>("我的", R.id.menu_myspace));
        put("local", new Pair<>("缓存", R.id.menu_local));
        put("settings", new Pair<>("设置", R.id.menu_settings));
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

        List<MaterialButton> cardList;

        String sortConf = SharedPreferencesUtil.getString(SharedPreferencesUtil.MENU_SORT, "");
        if (!TextUtils.isEmpty(sortConf)) {
            cardList = new ArrayList<>();
            String[] splitName = sortConf.split(";");
            for (String name : splitName) {
                if (!btnNames.containsKey(name)) {
                    cardList = getDefaultSortList();
                    break;
                } else {
                    int id = Objects.requireNonNull(btnNames.get(name)).second;
                    cardList.add(findViewById(id));
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
            layout.addView(button);
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
        return new ArrayList<>() {{
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
    }

}