package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.SettingsAdapter;
import com.RobinNotBad.BiliClient.model.SettingSection;

import java.util.ArrayList;
import java.util.List;

public class SettingLaboratoryActivity extends RefreshListActivity {


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("实验室");

        final List<SettingSection> sectionList = new ArrayList<>() {{
            add(new SettingSection("switch", "横屏模式", "ui_landscape", getString(R.string.setting_lab_ui_landscape), "false"));
            //add(new SettingSection("switch", "圆屏模式", "ui_round", getString(R.string.setting_lab_ui_round), "false"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);
    }

}