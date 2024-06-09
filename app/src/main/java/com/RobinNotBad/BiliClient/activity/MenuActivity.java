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

    /**
     * 在排序设置和Splash中使用到的，
     * 需要使用排序，故用了LinkedHashMap
     * 请不要让它的顺序被打乱（
     */
    public static final Map<String, Pair<String, Integer>> btnNames = new LinkedHashMap<>() {{
        put("recommend", new Pair<>("推荐", R.id.menu_recommend));
        put("popular", new Pair<>("热门", R.id.menu_popular));
        put("precious", new Pair<>("入站必刷", R.id.menu_precious));
        put("search", new Pair<>("搜索", R.id.menu_search));
        put("dynamic", new Pair<>("动态", R.id.menu_dynamic));
        put("myspace", new Pair<>("我的", R.id.menu_myspace));
        put("message", new Pair<>("消息", R.id.menu_message));
        put("local", new Pair<>("缓存", R.id.menu_local));
        put("settings", new Pair<>("设置", R.id.menu_settings));
    }};

    @SuppressLint({"MissingInflatedId", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_menu, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            long time = System.currentTimeMillis();
            Log.e("debug","MenuActivity onCreate: " + time);

            Intent intent = getIntent();
            from = intent.getIntExtra("from",0);

            findViewById(R.id.top).setOnClickListener(view -> finish());
            
            MenuAdapter adapter = new MenuAdapter();

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
            List<Integer> needRemoveIndexes = new ArrayList<>();
            for (int i = 0; i < cardList.size(); i++) {
                int id = cardList.get(i).getId();
                if (id == R.id.menu_myspace || id == R.id.menu_dynamic || id == R.id.menu_message) {
                    if (SharedPreferencesUtil.getLong("mid", 0) == 0) {
                        needRemoveIndexes.add(i);
                    }
                } else if (id == R.id.menu_login) {
                    if (SharedPreferencesUtil.getLong("mid", 0) != 0) needRemoveIndexes.add(i);
                } else if (id == R.id.menu_popular) {
                    if (!SharedPreferencesUtil.getBoolean("menu_popular",true)) needRemoveIndexes.add(i);
                } else if (id == R.id.menu_precious) {
                    if (!SharedPreferencesUtil.getBoolean("menu_precious",false)) needRemoveIndexes.add(i);
                }
                adapter.setOnClickListener(id, view -> killAndJump(view.getId()));
            }

            int shift = 0;
            for (int i : needRemoveIndexes) {
                cardList.remove(i - shift);
                shift++;
            }

            adapter.setOnClickListener(R.id.menu_login, view -> {
                Intent intent1 = new Intent();
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT) intent1.setClass(this, LoginActivity.class);
                else{
                    intent1.setClass(this, SpecialLoginActivity.class);
                    intent1.putExtra("login",true);
                }
                startActivity(intent1);
            });

            //我求求你了退出按钮能用吧....
            adapter.setOnClickListener(R.id.menu_exit, view -> {
                InstanceActivity instance = BiliTerminal.instance;
                if(instance != null && !instance.isDestroyed()) instance.finish();
                finish();
            });

            adapter.setBtns(cardList);
            ((RecyclerView) findViewById(R.id.recyclerView)).setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            ((RecyclerView) findViewById(R.id.recyclerView)).setAdapter(adapter);

            Log.e("debug", "MenuActivity onCreate used time: " + (System.currentTimeMillis() - time));
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

class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MaterialButton> btns = new ArrayList<>();
    private final Map<Integer, View.OnClickListener> listeners = new HashMap<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = new LinearLayout(parent.getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return new MenuHolder(linearLayout);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((MenuHolder) holder).setBtn(btns.get(position));
        btns.get(position).setOnClickListener((view) -> {
            View.OnClickListener onClickListener;
            if ((onClickListener = listeners.get(btns.get(position).getId())) != null) {
                onClickListener.onClick(view);
            }
        });
    }

    @Override
    public int getItemCount() {
        return btns.size();
    }

    public void setOnClickListener(int id, @NonNull View.OnClickListener onClickListener) {
        listeners.put(id, onClickListener);
    }

    public void setBtns(List<MaterialButton> btns) {
        this.btns = btns;
    }

    static class MenuHolder extends RecyclerView.ViewHolder {
        public MaterialButton btn;
        private final View itemView;
        public MenuHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }

        public void setBtn(MaterialButton btn) {
            this.btn = btn;
            if (btn.getParent() != null) {
                ((ViewGroup) btn.getParent()).removeView(btn);
            }
            ((LinearLayout) itemView).removeAllViews();
            ((LinearLayout) itemView).addView(btn, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
    }
}