package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingLaboratoryActivity extends BaseActivity {

    //private SwitchMaterial refresh_cookie;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_laboratory);
        Log.e("debug","进入实验性设置");

        //refresh_cookie = findViewById(R.id.refresh_cookie);
        //refresh_cookie.setChecked(SharedPreferencesUtil.getBoolean("dev_refresh_cookie",true));
    }

    private void save() {
        //SharedPreferencesUtil.putBoolean("dev_refresh_cookie", refresh_cookie.isChecked());
    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }
}