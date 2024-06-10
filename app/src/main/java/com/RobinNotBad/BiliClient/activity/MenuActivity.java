package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;

import java.util.*;

//菜单页面
//2023-07-14

public class MenuActivity extends BaseActivity {

    private String from;

    /**
     * 在排序设置和Splash中使用到的，
     * 需要使用排序，故用了LinkedHashMap
     * 请不要让它的顺序被打乱（
     */
    public static final Map<String, Pair<String,Class<? extends InstanceActivity>>> btnNames = new HashMap<>() {{
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

        LinearLayout layout = findViewById(R.id.menu_layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (String btn:btnList) {
            MaterialButton materialButton = new MaterialButton(this);
            materialButton.setText(Objects.requireNonNull(btnNames.get(btn)).first);
            materialButton.setOnClickListener(view->{killAndJump(btn);});
            layout.addView(materialButton,params);
        }


        /*
        btnList.add(0, findViewById(R.id.menu_login));
        btnList.add(findViewById(R.id.menu_exit));
        layout.removeAllViews();
        List<Integer> needRemoveIndexes = new ArrayList<>();
        for (int i = 0; i < btnList.size(); i++) {
            int id = btnList.get(i).getId();
            if (id == R.id.menu_myspace || id == R.id.menu_dynamic || id == R.id.menu_message) {
                if (SharedPreferencesUtil.getLong("mid", 0) == 0) {
                    needRemoveIndexes.add(i);
                }
            } else if (id == R.id.menu_login) {
                if (SharedPreferencesUtil.getLong("mid", 0) != 0) needRemoveIndexes.add(i);
            } else if (id == R.id.menu_popular) {
                if (!SharedPreferencesUtil.getBoolean("menu_popular", true))
                    needRemoveIndexes.add(i);
            } else if (id == R.id.menu_precious) {
                if (!SharedPreferencesUtil.getBoolean("menu_precious", false))
                    needRemoveIndexes.add(i);
            }
            adapter.setOnClickListener(id, view -> killAndJump(view.getId()));
        }

        int shift = 0;
        for (int i : needRemoveIndexes) {
            btnList.remove(i - shift);
            shift++;
        }

        adapter.setOnClickListener(R.id.menu_login, view -> {
            Intent intent1 = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                intent1.setClass(this, LoginActivity.class);
            else {
                intent1.setClass(this, SpecialLoginActivity.class);
                intent1.putExtra("login", true);
            }
            startActivity(intent1);
        });

        //我求求你了退出按钮能用吧....
        adapter.setOnClickListener(R.id.menu_exit, view -> {
            InstanceActivity instance = BiliTerminal.instance;
            if (instance != null && !instance.isDestroyed()) instance.finish();
            finish();
        });

        adapter.setBtns(btnList);
        ((RecyclerView) findViewById(R.id.recyclerView)).setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        ((RecyclerView) findViewById(R.id.recyclerView)).setAdapter(adapter);


         */
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

