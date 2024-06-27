package com.RobinNotBad.BiliClient.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingMenuActivity extends BaseActivity {

    private SwitchMaterial menu_popular, menu_precious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_setting_menu, null, (layoutView, resId, parent) -> {
            setContentView(R.layout.activity_setting_menu);
            setTopbarExit();
            Log.e("debug", "进入菜单设置");

            menu_popular = findViewById(R.id.menu_popular);
            menu_popular.setChecked(SharedPreferencesUtil.getBoolean("menu_popular", true));

            menu_precious = findViewById(R.id.menu_precious);
            menu_precious.setChecked(SharedPreferencesUtil.getBoolean("menu_precious", false));

            MaterialButton sort_btn = findViewById(R.id.sort);
            sort_btn.setOnClickListener(view -> {
                Intent intent = new Intent(SettingMenuActivity.this, SortSettingActivity.class);
                startActivity(intent);
            });
        });
    }

    private void save() {
        SharedPreferencesUtil.putBoolean("menu_popular", menu_popular.isChecked());
        SharedPreferencesUtil.putBoolean("menu_precious", menu_precious.isChecked());
    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }
}
