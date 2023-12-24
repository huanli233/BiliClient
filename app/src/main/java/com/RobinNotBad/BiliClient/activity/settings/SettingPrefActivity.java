package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

public class SettingPrefActivity extends BaseActivity {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch back_disable,fav_single,fav_notice,tags_enable,related_enable;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_pref);
        Log.e("debug","进入偏好设置");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        back_disable = findViewById(R.id.back_disable);
        back_disable.setChecked(SharedPreferencesUtil.getBoolean("back_disable",false));

        fav_single = findViewById(R.id.fav_single);
        fav_single.setChecked(SharedPreferencesUtil.getBoolean("fav_single",false));

        fav_notice = findViewById(R.id.fav_notice);
        fav_notice.setChecked(SharedPreferencesUtil.getBoolean("fav_notice",false));

        tags_enable = findViewById(R.id.tags_enable);
        tags_enable.setChecked(SharedPreferencesUtil.getBoolean("tags_enable",true));

        related_enable = findViewById(R.id.related_enable);
        related_enable.setChecked(SharedPreferencesUtil.getBoolean("related_enable",true));
    }

    private void save() {
        SharedPreferencesUtil.putBoolean("back_disable", back_disable.isChecked());
        SharedPreferencesUtil.putBoolean("fav_single", fav_single.isChecked());
        SharedPreferencesUtil.putBoolean("fav_notice", fav_notice.isChecked());
        SharedPreferencesUtil.putBoolean("tags_enable", tags_enable.isChecked());
        SharedPreferencesUtil.putBoolean("related_enable", related_enable.isChecked());
    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }
}