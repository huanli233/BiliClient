package com.RobinNotBad.BiliClient.activity.video.info;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.reply.ReplyFragment;
import com.RobinNotBad.BiliClient.adapter.viewpager.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.event.ReplyEvent;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.util.AnimationUtils;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private ImageView loading;
    private ViewPager viewPager;

    //private MediaViewPager2Adapter mediaViewPager2Adapter;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);    //这里async是否反而会减慢速度，仅有一个viewpager的页面加载已经足够快了吧

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if (type == null) type = "video";
        this.aid = intent.getLongExtra("aid", 114514);
        this.bvid = intent.getStringExtra("bvid");
        this.seek_reply = intent.getLongExtra("seekReply", -1);

        viewPager = findViewById(R.id.viewPager);
        loading = findViewById(R.id.loading);
        if (type.equals("media")) initMediaInfoView();
        else initVideoInfoView();
    }


    public void initMediaInfoView() {
        setPageName("番剧详情");

        fragmentList = new ArrayList<>(2);
        BangumiInfoFragment bangumiInfoFragment = BangumiInfoFragment.newInstance(aid);
        fragmentList.add(bangumiInfoFragment);
        replyFragment = ReplyFragment.newInstance(aid, 1, seek_reply == -1, seek_reply);
        TerminalContext.getInstance().getVideoInfoByAidOrBvId(aid, bvid).observe(this, (videoInfo) -> replyFragment.setSource(videoInfo));
        fragmentList.add(replyFragment);

        viewPager.setOffscreenPageLimit(fragmentList.size());
        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
        viewPager.setAdapter(vpfAdapter);
        if (seek_reply != -1) viewPager.setCurrentItem(1);
        bangumiInfoFragment.setOnFinishLoad(() -> AnimationUtils.crossFade(loading, bangumiInfoFragment.getView()));
        if (SharedPreferencesUtil.getBoolean("first_videoinfo", true)) {
            MsgUtil.showMsgLong("提示：本页面可以左右滑动");
            SharedPreferencesUtil.putBoolean("first_videoinfo", false);
        }
    }

    protected void initVideoInfoView() {
        TutorialHelper.showTutorialList(this, R.array.tutorial_video, 1);
        TutorialHelper.showPagerTutorial(this, 3);

        setPageName("视频详情");
        TerminalContext.getInstance().getVideoInfoByAidOrBvId(aid, bvid).observe(this, (result) -> result.onSuccess((videoInfo) -> {
            fragmentList = new ArrayList<>(3);
            VideoInfoFragment videoInfoFragment = VideoInfoFragment.newInstance(aid, bvid);
            fragmentList.add(videoInfoFragment);
            replyFragment = ReplyFragment.newInstance(videoInfo.aid, 1, seek_reply, videoInfo.staff.get(0).mid);
            replyFragment.setSource(videoInfo);
            fragmentList.add(replyFragment);
            if (SharedPreferencesUtil.getBoolean("related_enable", true)) {
                VideoRcmdFragment vrFragment = VideoRcmdFragment.newInstance(videoInfo.aid);
                fragmentList.add(vrFragment);
            }
            viewPager.setOffscreenPageLimit(fragmentList.size());
            ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
            viewPager.setAdapter(vpfAdapter);
            if (seek_reply != -1) viewPager.setCurrentItem(1);
            videoInfoFragment.setOnFinishLoad(()-> AnimationUtils.crossFade(loading, videoInfoFragment.getView()));
        }).onFailure((error) -> {
            loading.setImageResource(R.mipmap.loading_2233_error);
            MsgUtil.showMsg("获取信息失败！\n可能是视频不存在？");
            CenterThreadPool.runOnUIThreadAfter(5L, TimeUnit.SECONDS, ()->
                    MsgUtil.err(error));
        }));
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