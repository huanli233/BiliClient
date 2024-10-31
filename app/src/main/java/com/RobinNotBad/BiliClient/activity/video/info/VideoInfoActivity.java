package com.RobinNotBad.BiliClient.activity.video.info;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.reply.ReplyFragment;
import com.RobinNotBad.BiliClient.adapter.viewpager.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.event.ReplyEvent;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.util.*;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//视频详情页，但这只是个壳，瓤是VideoInfoFragment、VideoReplyFragment、VideoRcmdFragment

public class VideoInfoActivity extends BaseActivity {

    private long aid;
    private String bvid;

    private List<Fragment> fragmentList;
    ReplyFragment replyFragment;
    private long seek_reply;

    //private MediaViewPager2Adapter mediaViewPager2Adapter;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if (type == null) type = "video";
        this.aid = intent.getLongExtra("aid", 114514);
        this.bvid = intent.getStringExtra("bvid");
        this.seek_reply = intent.getLongExtra("seekReply", -1);
        setContentView(R.layout.activity_loading);

        String finalType = type;
        asyncInflate(R.layout.activity_simple_viewpager, (layoutView, resId) -> {
            setContentView(layoutView);
            setTopbarExit();

            if (finalType.equals("media")) initMediaInfoView();
            else initVideoInfoView();
        });
    }


    public void initMediaInfoView() {
        ViewPager viewPager = findViewById(R.id.viewPager);
        ImageView loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        setPageName("视频详情");

        fragmentList = new ArrayList<>(2);
        BangumiInfoFragment bangumiInfoFragment = BangumiInfoFragment.newInstance(aid);
        fragmentList.add(bangumiInfoFragment);
        replyFragment = ReplyFragment.newInstance(aid, 1, seek_reply == -1, seek_reply);
        fragmentList.add(replyFragment);

        viewPager.setOffscreenPageLimit(fragmentList.size());
        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(vpfAdapter);
        if (seek_reply != -1) viewPager.setCurrentItem(1);
        bangumiInfoFragment.setOnFinishLoad(() -> AnimationUtils.crossFade(loading, bangumiInfoFragment.getView()));
        if (SharedPreferencesUtil.getBoolean("first_videoinfo", true)) {
            MsgUtil.showMsgLong("提示：本页面可以左右滑动", this);
            SharedPreferencesUtil.putBoolean("first_videoinfo", false);
        }
    }

    protected void initVideoInfoView() {
        TutorialHelper.showTutorialList(this, R.array.tutorial_video, 1);
        TutorialHelper.showPagerTutorial(this, 3);

        ViewPager viewPager = findViewById(R.id.viewPager);
        TextView pageName = findViewById(R.id.pageName);
        ImageView loading = findViewById(R.id.loading);
        findViewById(R.id.top).setOnClickListener(view -> finish());
        loading.setVisibility(View.VISIBLE);
        pageName.setText("视频详情");
        CenterThreadPool.supplyAsyncWithLiveData(() -> {
           JSONObject data;
            if (TextUtils.isEmpty(bvid)) data = VideoInfoApi.getJsonByAid(aid);
            else data = VideoInfoApi.getJsonByBvid(bvid);
            if (data == null) {
                return null;
            }
            return VideoInfoApi.getInfoByJson(data);
        }).observe(this, (result) -> {
            result.onSuccess((videoInfo) -> {
                TerminalContext.getInstance().enterVideoDetailPage(videoInfo);
                fragmentList = new ArrayList<>(3);
                fragmentList.add(VideoInfoFragment.newInstance());
                replyFragment = ReplyFragment.newInstance(videoInfo.aid, 1, seek_reply, videoInfo.staff.get(0).mid);
                fragmentList.add(replyFragment);
                if (SharedPreferencesUtil.getBoolean("related_enable", true)) {
                    VideoRcmdFragment vrFragment = VideoRcmdFragment.newInstance(videoInfo.aid);
                    fragmentList.add(vrFragment);
                }
                viewPager.setOffscreenPageLimit(fragmentList.size());
                ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
                viewPager.setAdapter(vpfAdapter);
                View view;
                if ((view = fragmentList.get(0).getView()) != null)
                    view.setVisibility(View.GONE);
                if (seek_reply != -1) viewPager.setCurrentItem(1);
                AnimationUtils.crossFade(loading, fragmentList.get(0).getView());
            }).onFailure((error) -> {
                loading.setImageResource(R.mipmap.loading_2233_error);
                MsgUtil.showMsg("获取信息失败！\n可能是视频不存在？", BiliTerminal.context);
                CenterThreadPool.runOnUIThreadAfter(5L, TimeUnit.SECONDS, ()-> {
                    MsgUtil.err(error, BiliTerminal.context);
                });
            });
        });
    }

    public void setCurrentAid(long aid) {
        if (replyFragment != null) runOnUiThread(() -> replyFragment.refresh(aid));
    }

    @Override
    protected boolean eventBusEnabled() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true, priority = 1)
    public void onEvent(ReplyEvent event) {
        replyFragment.notifyReplyInserted(event);
    }

    @Override
    protected void onDestroy() {
        TerminalContext.getInstance().leaveDetailPage();
        super.onDestroy();
    }
}