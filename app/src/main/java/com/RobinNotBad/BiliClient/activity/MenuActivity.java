package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity;
import com.RobinNotBad.BiliClient.activity.live.RecommendLiveActivity;
import com.RobinNotBad.BiliClient.activity.message.MessageActivity;
import com.RobinNotBad.BiliClient.activity.search.SearchActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingMainActivity;
import com.RobinNotBad.BiliClient.activity.settings.login.LoginActivity;
import com.RobinNotBad.BiliClient.activity.user.MySpaceActivity;
import com.RobinNotBad.BiliClient.activity.video.PopularActivity;
import com.RobinNotBad.BiliClient.activity.video.PreciousActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
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
    private CountDownTimer countDownTimer;
    private MaterialButton timedExitButton;

    /**
     * 在排序设置和Splash中使用到的，
     * 需要使用排序，故用了LinkedHashMap
     * 请不要让它的顺序被打乱（
     */
    public static final Map<String, Pair<String, Class<? extends InstanceActivity>>> btnNames = new LinkedHashMap<>() {{
        put("recommend", new Pair<>("推荐", RecommendActivity.class));
        put("popular", new Pair<>("热门", PopularActivity.class));
        put("precious", new Pair<>("入站必刷", PreciousActivity.class));
        put("live", new Pair<>("直播", RecommendLiveActivity.class));
        put("search", new Pair<>("搜索", SearchActivity.class));
        put("dynamic", new Pair<>("动态", DynamicActivity.class));
        put("myspace", new Pair<>("我的", MySpaceActivity.class));
        put("message", new Pair<>("消息", MessageActivity.class));
        put("local", new Pair<>("缓存", LocalListActivity.class));
        put("settings", new Pair<>("设置", SettingMainActivity.class));
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
        if(from!=null){
            Log.d("debug-menu",from);
            if(btnNames.containsKey(from)) setPageName(Objects.requireNonNull(btnNames.get(from)).first);
        }

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

        if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
            btnList.add(0, "login");
            btnList.remove("dynamic");
            btnList.remove("message");
            btnList.remove("myspace");
        }

        if (!SharedPreferencesUtil.getBoolean("menu_popular", true)) btnList.remove("popular");
        if (!SharedPreferencesUtil.getBoolean("menu_precious", false)) btnList.remove("precious");
        if (!SharedPreferencesUtil.getBoolean("menu_live", false)) btnList.remove("live");

        btnList.add("timed_exit");
        btnList.add("exit"); //如果你希望用户手动把退出按钮排到第一个（

        LinearLayout layout = findViewById(R.id.menu_layout);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (String btn : btnList) {
            MaterialButton materialButton = new MaterialButton(this);
            switch (btn) {
                case "exit":
                    materialButton.setText("退出");
                    break;
                case "login":
                    materialButton.setText("登录");
                    break;
                case "timed_exit":
                    timedExitButton = materialButton;
                    materialButton.setText("定时关闭");
                    break;
                default:
                    materialButton.setText(Objects.requireNonNull(btnNames.get(btn)).first);
                    break;
            }
            if (btn.equals("timed_exit")) {
                materialButton.setOnClickListener(view -> showTimedExitDialog());
            } else {
                materialButton.setOnClickListener(view -> killAndJump(btn));
            }
            layout.addView(materialButton, params);
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

    private void killAndJump(String name) {
        if (btnNames.containsKey(name) && !Objects.equals(name, from)) {
            InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
            if (instance != null && instance.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) instance.finish();

            Intent intent = new Intent();
            intent.setClass(MenuActivity.this, Objects.requireNonNull(btnNames.get(name)).second);
            intent.putExtra("from", name);
            startActivity(intent);
            Glide.get(BiliTerminal.context).clearMemory();
        } else {
            switch (name) {
                case "exit": //退出按钮
                    InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
                    if (instance != null && !instance.isDestroyed()) instance.finish();
                    Process.killProcess(Process.myPid());
                    break;
                case "login": //登录按钮
                    Intent intent = new Intent();
                    intent.setClass(MenuActivity.this, LoginActivity.class);
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
            add("live");
            add("search");
            add("dynamic");
            add("myspace");
            add("message");
            add("local");
            add("settings");
        }};
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU) finish();
        return super.onKeyDown(keyCode, event);
    }

    private void showTimedExitDialog() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
            timedExitButton.setText("定时关闭");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_timed_exit, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
        }

        dialogView.findViewById(R.id.btn_15_min).setOnClickListener(v -> {
            startTimer(15 * 60 * 1000);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_30_min).setOnClickListener(v -> {
            startTimer(30 * 60 * 1000);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_45_min).setOnClickListener(v -> {
            startTimer(45 * 60 * 1000);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btn_60_min).setOnClickListener(v -> {
            startTimer(60 * 60 * 1000);
            dialog.dismiss();
        });

        EditText customTimeEditText = dialogView.findViewById(R.id.edit_text_custom_time);
        dialogView.findViewById(R.id.btn_custom_time).setOnClickListener(v -> {
            String customTimeStr = customTimeEditText.getText().toString();
            if (!TextUtils.isEmpty(customTimeStr)) {
                try {
                    long minutes = Long.parseLong(customTimeStr);
                    if (minutes > 0) {
                        startTimer(minutes * 60 * 1000);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "请输入大于0的分钟数", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "请输入有效的分钟数", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "请输入分钟数", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        //动态设置弹窗大小
        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            lp.width = (int) (dm.widthPixels * 0.8);
            dialog.getWindow().setAttributes(lp);
        }
    }

    private void startTimer(long timeInMillis) {
        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                timedExitButton.setText("剩余 " + minutes + "分" + seconds + "秒");
            }

            @Override
            public void onFinish() {
                timedExitButton.setText("定时关闭");
                InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
                if (instance != null && !instance.isDestroyed()) instance.finish();
                Process.killProcess(Process.myPid());
            }
        }.start();
    }
}
