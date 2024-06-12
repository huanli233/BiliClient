package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.ViewGroup;
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
import com.RobinNotBad.BiliClient.activity.user.MySpaceActivity;
import com.RobinNotBad.BiliClient.activity.video.PopularActivity;
import com.RobinNotBad.BiliClient.activity.video.PreciousActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//菜单页面
//2023-07-14

public class MenuActivity extends BaseActivity {

    private String from;

    /**
     * 在排序设置和Splash中使用到的，
     * 需要使用排序，故用了LinkedHashMap
     * 请不要让它的顺序被打乱（
     */
    public static final Map<String, Pair<String,Class<? extends InstanceActivity>>> btnNames = new LinkedHashMap<>() {{
        put("recommend", new Pair<>("推荐"   , RecommendActivity.class));
        put("popular",   new Pair<>("热门"   , PopularActivity.class));
        put("precious",  new Pair<>("入站必刷", PreciousActivity.class));
        put("search",    new Pair<>("搜索"   , SearchActivity.class));
        put("dynamic",   new Pair<>("动态"   , DynamicActivity.class));
        put("myspace",   new Pair<>("我的"   , MySpaceActivity.class));
        put("message",   new Pair<>("消息"   , MessageActivity.class));
        put("local",     new Pair<>("缓存"   , LocalListActivity.class));
        put("settings",  new Pair<>("设置"   , SettingMainActivity.class));
    }};

    long time;

    @SuppressLint({"MissingInflatedId", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        time = System.currentTimeMillis();
        Log.e("debug", "MenuActivity onCreate: " + time);

        Intent intent = getIntent();
        from = intent.getStringExtra("from");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        List<String> btnList;

        String sortConf = SharedPreferencesUtil.getString(SharedPreferencesUtil.MENU_SORT, "");
        Log.e("debug_sort", sortConf);

        if (!TextUtils.isEmpty(sortConf)) {
            String[] splitName = sortConf.split(";");
            if (splitName.length != btnNames.size()) {
                btnList = getDefaultSortList();
            } else {
                btnList = new ArrayList<>();
                for (String name : splitName) {
                    if (!btnNames.containsKey(name)) {
                        btnList = getDefaultSortList();
                        break;
                    } else {
                        btnList.add(name);
                    }
                }
            }
        } else {
            btnList = getDefaultSortList();
        }
        
        if(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0) == 0) {
            btnList.add(0,"login");
            btnList.remove("dynamic");
            btnList.remove("message");
            btnList.remove("myspace");
        }
        
        if(!SharedPreferencesUtil.getBoolean("menu_popular",true)) btnList.remove("popular");
        if(!SharedPreferencesUtil.getBoolean("menu_precious",false)) btnList.remove("precious");
        
        btnList.add("exit"); //如果你希望用户手动把退出按钮排到第一个（

        LinearLayout layout = findViewById(R.id.menu_layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (String btn:btnList) {
            MaterialButton materialButton = new MaterialButton(this);
            switch(btn){
                case "exit":
                    materialButton.setText("退出");
                    break;
                case "login":
                    materialButton.setText("登录"); 
                    break;
                default:
                    materialButton.setText(Objects.requireNonNull(btnNames.get(btn)).first);
                    break;
            }
            materialButton.setOnClickListener(view-> killAndJump(btn));
            layout.addView(materialButton,params);
        }

        Log.e("debug", "MenuActivity onCreate in: " + (System.currentTimeMillis() - time));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("debug", "MenuActivity onStart in: " + (System.currentTimeMillis() - time));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("debug", "MenuActivity onResume in: " + (System.currentTimeMillis() - time));
    }

    private void killAndJump(String name){
        if (btnNames.containsKey(name) && !Objects.equals(name, from)) {
            InstanceActivity instance = BiliTerminal.instance;
            if(instance != null && !instance.isDestroyed()) instance.finish();

            Intent intent = new Intent();
            intent.setClass(MenuActivity.this, Objects.requireNonNull(btnNames.get(name)).second);
            intent.putExtra("from", name);
            startActivity(intent);
        }else{
            switch(name){
                case "exit": //退出按钮
                    InstanceActivity instance = BiliTerminal.instance;
                    if (instance != null && !instance.isDestroyed()) instance.finish();
                    break;
                case "login": //登录按钮
                    Intent intent = new Intent();
                    intent.setClass(MenuActivity.this,LoginActivity.class);
                    startActivity(intent);
                    break;
            }
        }
        finish();
    }

    private List<String> getDefaultSortList() {
        return new ArrayList<>() {{
            add("recommend");
            add("popular");
            add("precious");
            add("search");
            add("dynamic");
            add("myspace");
            add("message");
            add("local");
            add("settings");
        }};
    }
}

