package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.SwitchCompat;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import java.util.Objects;
import java.util.prefs.Preferences;

public class SettingLaboratoryActivity extends BaseActivity {

    private SwitchCompat refresh_cookie,article_picture_load;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_laboratory);
        Log.e("debug","进入实验性设置");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        refresh_cookie = findViewById(R.id.refresh_cookie);
        refresh_cookie.setChecked(SharedPreferencesUtil.getBoolean("dev_refresh_cookie",false));

        article_picture_load = findViewById(R.id.article_picture_load);
        article_picture_load.setChecked(SharedPreferencesUtil.getBoolean("dev_article_pic_load",true));

        if(Objects.equals(SharedPreferencesUtil.getString(SharedPreferencesUtil.refresh_token, ""), "")) refresh_cookie.setEnabled(false);
    }

    private void save() {
        SharedPreferencesUtil.putBoolean("dev_refresh_cookie", refresh_cookie.isChecked());
        SharedPreferencesUtil.putBoolean("dev_article_pic_load", article_picture_load.isChecked());

    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }
}