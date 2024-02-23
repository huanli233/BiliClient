package com.RobinNotBad.BiliClient.activity.settings;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.AnnouncementAdapter;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.model.Announcement;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

//公告列表
//2024-02-23

public class AnnouncementsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        setPageName("公告列表");

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        CenterThreadPool.run(()->{
            try {
                ArrayList<Announcement> announcements = AppInfoApi.getAnnouncementList();

                AnnouncementAdapter adapter = new AnnouncementAdapter(this,announcements);

                runOnUiThread(()->{
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                });
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.err(e, this));
            }
        });
    }

}