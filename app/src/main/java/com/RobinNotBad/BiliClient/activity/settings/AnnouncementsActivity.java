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
                if(e.getMessage().contains("<!DOCTYPE")) runOnUiThread(() -> MsgUtil.toast("哔哩终端域名疑似失效或被屏蔽",this));
                report(e);
            }
        });
    }

}