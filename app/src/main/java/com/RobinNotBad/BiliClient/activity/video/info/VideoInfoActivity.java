package com.RobinNotBad.BiliClient.activity.video.info;

import android.content.Intent;
import android.os.Bundle;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.video.info.factory.DetailInfoFactory;
import com.RobinNotBad.BiliClient.activity.video.info.factory.DetailPage;

//视频详情页，但这只是个壳，瓤是VideoInfoFragment、VideoReplyFragment、VideoRcmdFragment

public class VideoInfoActivity extends BaseActivity {

    private DetailPage info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String bvid = intent.getStringExtra("bvid");
        long aid = intent.getLongExtra("aid",114514);
        String type = intent.getStringExtra("type");
        DetailInfoFactory.Data data = new DetailInfoFactory.Data();
        data.setAid(aid);
        data.setBvid(bvid);
        data.setType(type == null ? "video" : type);
        data.setMediaId(aid);
        info = DetailInfoFactory.get(this, data);
        getLifecycle().addObserver(info);
        setContentView(info.getRootView());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}