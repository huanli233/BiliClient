package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.SettingsAdapter;
import com.RobinNotBad.BiliClient.model.SettingSection;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class SettingInfoActivity extends RefreshListActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("详情页设置");
        Log.e("debug", "进入详情页设置");

        final List<SettingSection> sectionList = new ArrayList<>() {{
            add(new SettingSection("switch", "收藏夹单选", "fav_single", getString(R.string.desc_fav_single), "false"));
            add(new SettingSection("switch", "收藏成功提示", "fav_notice", getString(R.string.desc_fav_notice), "false"));
            add(new SettingSection("switch", "显示视频标签", "tags_enable", getString(R.string.desc_tags_enable), "true"));
            add(new SettingSection("switch", "视频相关推荐", "related_enable", getString(R.string.desc_related_enable), "true"));
            add(new SettingSection("switch", "点击封面播放", "cover_play_enable", getString(R.string.desc_cover_play_enable), "false"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);

    }

}