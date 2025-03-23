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

public class SettingRepliesActivity extends RefreshListActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("评论区设置");
        Log.e("debug", "进入评论区设置");

        final List<SettingSection> sectionList = new ArrayList<>() {{
            add(new SettingSection("switch", "众生平等", SharedPreferencesUtil.NO_VIP_COLOR, getString(R.string.desc_no_vip_color), "false"));
            add(new SettingSection("switch", "粉丝铭牌消失术", SharedPreferencesUtil.NO_MEDAL, getString(R.string.desc_no_medal), "false"));
            add(new SettingSection("switch", "昵称不换行显示", SharedPreferencesUtil.REPLY_MARQUEE_NAME, getString(R.string.desc_reply_marquee_name), "true"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);

    }

}