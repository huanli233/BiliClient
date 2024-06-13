package com.RobinNotBad.BiliClient.activity.settings;

import android.os.Bundle;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.AnnouncementAdapter;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.model.Announcement;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import com.RobinNotBad.BiliClient.util.MsgUtil;
import java.util.ArrayList;

//公告列表
//2024-02-23

public class AnnouncementsActivity extends RefreshListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("公告列表");

        CenterThreadPool.run(()->{
            try {
                ArrayList<Announcement> announcements = AppInfoApi.getAnnouncementList();
                setRefreshing(false);

                AnnouncementAdapter adapter = new AnnouncementAdapter(this,announcements);

                setAdapter(adapter);

            } catch (Exception e) {
                report(e);
                runOnUiThread(() -> MsgUtil.toast("连接到哔哩终端接口时发生错误",this));
            }
        });
    }

}