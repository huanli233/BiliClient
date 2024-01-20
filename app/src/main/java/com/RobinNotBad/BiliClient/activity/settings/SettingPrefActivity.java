package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.SwitchCompat;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

public class SettingPrefActivity extends BaseActivity {

    private SwitchCompat back_disable,fav_single,fav_notice, video_tags, video_related, myspace_creativecenter,menu_popular,menu_precious,old_search_enable,copy_enable;

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

        video_tags = findViewById(R.id.tags_enable);
        video_tags.setChecked(SharedPreferencesUtil.getBoolean("tags_enable",true));

        video_related = findViewById(R.id.related_enable);
        video_related.setChecked(SharedPreferencesUtil.getBoolean("related_enable",true));

        myspace_creativecenter = findViewById(R.id.creative_enable);
        myspace_creativecenter.setChecked(SharedPreferencesUtil.getBoolean("creative_enable",true));

        menu_popular = findViewById(R.id.menu_popular);
        menu_popular.setChecked(SharedPreferencesUtil.getBoolean("menu_popular",true));

        menu_precious = findViewById(R.id.menu_precious);
        menu_precious.setChecked(SharedPreferencesUtil.getBoolean("menu_precious",true));
        
        old_search_enable = findViewById(R.id.old_search_enable);
        old_search_enable.setChecked(SharedPreferencesUtil.getBoolean("old_search_enable",false));
                
        copy_enable = findViewById(R.id.copy_enable);
        copy_enable.setChecked(SharedPreferencesUtil.getBoolean("copy_enable",true));
    }

    private void save() {
        SharedPreferencesUtil.putBoolean("back_disable", back_disable.isChecked());
        SharedPreferencesUtil.putBoolean("fav_single", fav_single.isChecked());
        SharedPreferencesUtil.putBoolean("fav_notice", fav_notice.isChecked());
        SharedPreferencesUtil.putBoolean("tags_enable", video_tags.isChecked());
        SharedPreferencesUtil.putBoolean("related_enable", video_related.isChecked());
        SharedPreferencesUtil.putBoolean("creative_enable", myspace_creativecenter.isChecked());
        SharedPreferencesUtil.putBoolean("menu_popular", menu_popular.isChecked());
        SharedPreferencesUtil.putBoolean("menu_precious", menu_precious.isChecked());
        SharedPreferencesUtil.putBoolean("old_search_enable", old_search_enable.isChecked());
        SharedPreferencesUtil.putBoolean("copy_enable", copy_enable.isChecked());
    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }
}