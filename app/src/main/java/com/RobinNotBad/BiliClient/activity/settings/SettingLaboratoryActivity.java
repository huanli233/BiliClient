package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.SettingsAdapter;
import com.RobinNotBad.BiliClient.model.SettingSection;
import com.RobinNotBad.BiliClient.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class SettingLaboratoryActivity extends RefreshListActivity {


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("实验室");

        final List<SettingSection> sectionList = new ArrayList<>() {{
            add(new SettingSection("switch", "使用旧版下载器", "dev_download_old", getString(R.string.setting_lab_download_old), "false"));
            add(new SettingSection("switch", "横屏模式", "ui_landscape", getString(R.string.setting_lab_ui_landscape), "false"));
            add(new SettingSection("input_string","开屏文字","ui_splashtext", getString(R.string.setting_lab_splashtext),"欢迎使用\n哔哩终端"));
            add(new SettingSection("input_string","缓存路径","save_path_video",getString(R.string.setting_lab_path_video), FileUtil.getDownloadPath().toString()));
            add(new SettingSection("input_string","缓存路径","save_path_pictures",getString(R.string.setting_lab_path_pictures), FileUtil.getDownloadPicturePath().toString()));
            add(new SettingSection("input_float","表冠适配灵敏度（Recycler）","ui_rotatory_recycler", getString(R.string.setting_lab_ui_rotatory),"0"));
            add(new SettingSection("input_float","表冠适配灵敏度（Scroll）","ui_rotatory_scroll", getString(R.string.setting_lab_ui_rotatory),"0"));
            add(new SettingSection("input_float","表冠适配灵敏度（List）","ui_rotatory_scroll", getString(R.string.setting_lab_ui_rotatory),"0"));
            add(new SettingSection("switch","详细显示数据解析报错","dev_jsonerr_detailed",getString(R.string.setting_lab_jsonerr_detailed),"false"));
            add(new SettingSection("switch","详细显示列表报错","dev_recyclererr_detailed",getString(R.string.setting_lab_recyclererr_detailed),"false"));
            //add(new SettingSection("input_int","test_int","test_int", "这是描述描述描述","123"));
            //add(new SettingSection("input_float","test_float","test_float_2", "这是描述描述描述","1.1"));
            //add(new SettingSection("switch", "圆屏模式", "ui_round", getString(R.string.setting_lab_ui_round), "false"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);
    }

}