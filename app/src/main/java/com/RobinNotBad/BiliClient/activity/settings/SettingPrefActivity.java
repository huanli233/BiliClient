package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.SettingsAdapter;
import com.RobinNotBad.BiliClient.model.SettingSection;

import java.util.ArrayList;
import java.util.List;

public class SettingPrefActivity extends RefreshListActivity {

    /*
    private SwitchMaterial back_disable,fav_single,fav_notice, video_tags, video_related,
            myspace_creativecenter,
            copy_enable, cover_play_enable,image_no_load_onscroll,
            text_link_enable, disable_network_check;

     */

    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("偏好设置");
        Log.e("debug", "进入偏好设置");

        List<SettingSection> sectionList = new ArrayList<>(){{
            add(new SettingSection("switch","禁用返回键","back_disable",getString(R.string.desc_back_disable),"false"));
            add(new SettingSection("switch","收藏夹单选","fav_single",getString(R.string.desc_fav_single),"false"));
            add(new SettingSection("switch","收藏成功提示","fav_notice",getString(R.string.desc_fav_notice),"false"));
            add(new SettingSection("switch","显示视频标签","tags_enable",getString(R.string.desc_tags_enable),"true"));
            add(new SettingSection("switch","视频相关推荐","related_enable",getString(R.string.desc_related_enable),"true"));
            add(new SettingSection("switch","创作中心","creative_enable",getString(R.string.desc_creative_enable),"true"));
            add(new SettingSection("switch","长按复制","copy_enable",getString(R.string.desc_copy_enable),"true"));
            add(new SettingSection("switch","点击封面播放","cover_play_enable",getString(R.string.desc_cover_play_enable),"false"));
            add(new SettingSection("switch","翻动时不加载图片","image_no_load_onscroll",getString(R.string.desc_img_no_load_onscroll),"true"));
            add(new SettingSection("switch","识别链接","link_enable",getString(R.string.desc_link_enable),"true"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this,sectionList);
        setAdapter(adapter);

        setRefreshing(false);

        /*
        back_disable = findViewById(R.id.back_disable);
        back_disable.setChecked(SharedPreferencesUtil.getBoolean("back_disable", false));

        fav_single = findViewById(R.id.fav_single);
        fav_single.setChecked(SharedPreferencesUtil.getBoolean("fav_single", false));

        fav_notice = findViewById(R.id.fav_notice);
        fav_notice.setChecked(SharedPreferencesUtil.getBoolean("fav_notice", false));

        video_tags = findViewById(R.id.tags_enable);
        video_tags.setChecked(SharedPreferencesUtil.getBoolean("tags_enable", true));

        video_related = findViewById(R.id.related_enable);
        video_related.setChecked(SharedPreferencesUtil.getBoolean("related_enable", true));

        myspace_creativecenter = findViewById(R.id.creative_enable);
        myspace_creativecenter.setChecked(SharedPreferencesUtil.getBoolean("creative_enable", true));

        copy_enable = findViewById(R.id.copy_enable);
        copy_enable.setChecked(SharedPreferencesUtil.getBoolean("copy_enable", true));

        cover_play_enable = findViewById(R.id.cover_play_enable);
        cover_play_enable.setChecked(SharedPreferencesUtil.getBoolean("cover_play_enable", false));

        image_no_load_onscroll = findViewById(R.id.image_no_load_onscroll);
        image_no_load_onscroll.setChecked(SharedPreferencesUtil.getBoolean("image_no_load_onscroll", true));

        text_link_enable = findViewById(R.id.text_link_enable);
        text_link_enable.setChecked(SharedPreferencesUtil.getBoolean("link_enable", true));

        disable_network_check = findViewById(R.id.disable_network_check);
        disable_network_check.setChecked(SharedPreferencesUtil.getBoolean("network_check_disable", false));


         */
    }

    /*
    private void save() {
        SharedPreferencesUtil.putBoolean("back_disable", back_disable.isChecked());
        SharedPreferencesUtil.putBoolean("fav_single", fav_single.isChecked());
        SharedPreferencesUtil.putBoolean("fav_notice", fav_notice.isChecked());
        SharedPreferencesUtil.putBoolean("tags_enable", video_tags.isChecked());
        SharedPreferencesUtil.putBoolean("related_enable", video_related.isChecked());
        SharedPreferencesUtil.putBoolean("creative_enable", myspace_creativecenter.isChecked());
        SharedPreferencesUtil.putBoolean("copy_enable", copy_enable.isChecked());
        SharedPreferencesUtil.putBoolean("cover_play_enable", cover_play_enable.isChecked());
        SharedPreferencesUtil.putBoolean("image_no_load_onscroll", image_no_load_onscroll.isChecked());
        SharedPreferencesUtil.putBoolean("link_enable", text_link_enable.isChecked());
        SharedPreferencesUtil.putBoolean("network_check_disable", disable_network_check.isChecked());
    }

    @Override
    protected void onDestroy() {
        //save();
        super.onDestroy();
    }

     */
}