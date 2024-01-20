package com.RobinNotBad.BiliClient.activity.video.info;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.media.MediaInfoFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//视频详情页，但这只是个壳，瓤是VideoInfoFragment、VideoReplyFragment、VideoRcmdFragment

public class VideoInfoActivity extends BaseActivity {

    private String type;
    private long aid;
    private String bvid;
    private long mediaId;

    private List<Fragment> fragmentList;
    VideoReplyFragment replyFragment;

    //private MediaViewPager2Adapter mediaViewPager2Adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        this.type = intent.getStringExtra("type");
        if(type == null) type = "video";
        this.aid = intent.getLongExtra("aid",114514);
        this.bvid = intent.getStringExtra("bvid");
        this.mediaId = this.aid;
        //int layoutId;
        //if(type == "media") layoutId = R.layout.activity_simple_viewpager2;
        //else layoutId = R.layout.activity_simple_viewpager;
        //setContentView(layoutId);
        setContentView(R.layout.activity_simple_viewpager);

        if(type.equals("media")) {
            initMediaInfoView();
            if(!SharedPreferencesUtil.getBoolean("tutorial_media",false)){
                MsgUtil.showDialog(this,"使用教程","此页面从左向有或从右向左滑动可以切换页面，第一页为视频详情，第二页为评论区",R.mipmap.tutorial_media,true,5);
                SharedPreferencesUtil.putBoolean("tutorial_media",true);
            }
        } else {
            initVideoInfoView();
            if(!SharedPreferencesUtil.getBoolean("tutorial_video",false)){
                MsgUtil.showDialog(this,"使用教程","此页面从左向有或从右向左滑动可以切换页面，第一页为视频详情，第二页为评论区，第三页为推荐（如果开启了的话）",R.mipmap.tutorial_video,true,5);
                SharedPreferencesUtil.putBoolean("tutorial_video",true);
            }
        }
    }

    /*
    @Override
    protected void onStart() {
        super.onStart();
        if(type.equals("media")) initMediaInfoView();
        else initVideoInfoView();
    }
     */

    public void initMediaInfoView() {
        ViewPager viewPager = findViewById(R.id.viewPager);
        findViewById(R.id.top).setOnClickListener(view -> finish());
        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("视频详情");
        fragmentList = createFragmentList(null);
        viewPager.setOffscreenPageLimit(fragmentList.size());
        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(),fragmentList);
        viewPager.setAdapter(vpfAdapter);
        if (SharedPreferencesUtil.getBoolean("first_videoinfo", true)) {
            Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
            SharedPreferencesUtil.putBoolean("first_videoinfo", false);
        }
    }
    protected void initVideoInfoView() {
        ViewPager viewPager = findViewById(R.id.viewPager);
        TextView pageName = findViewById(R.id.pageName);
        ImageView loading = findViewById(R.id.loading);
        findViewById(R.id.top).setOnClickListener(view -> finish());
        loading.setVisibility(View.VISIBLE);
        pageName.setText("视频详情");
        Log.e("VideoInfoActivity",SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));

        CenterThreadPool.run(() -> {
            JSONObject data;
            try {
                VideoInfo videoInfo;
                if (bvid == null || TextUtils.isEmpty(bvid)) data = VideoInfoApi.getJsonByAid(aid);
                else data = VideoInfoApi.getJsonByBvid(bvid);
                JSONArray tagList;
                if (bvid == null || bvid.isEmpty()) tagList = VideoInfoApi.getTagsByAid(aid);
                else tagList = VideoInfoApi.getTagsByBvid(bvid);
                videoInfo = VideoInfoApi.getInfoByJson(data, tagList);
                fragmentList = createFragmentList(videoInfo);
                viewPager.setOffscreenPageLimit(fragmentList.size());
                ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    viewPager.setAdapter(vpfAdapter);
                    if (SharedPreferencesUtil.getBoolean("first_videoinfo", true)) {
                        Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
                        SharedPreferencesUtil.putBoolean("first_videoinfo", false);
                    }
                });
                //没啥好说的，教科书式的ViewPager使用方法
            } catch (JSONException e) {
                runOnUiThread(() -> {
                    loading.setImageResource(R.mipmap.loading_2233_error);
                    MsgUtil.jsonErr(e, this);
                });
                e.printStackTrace();
            } catch (IOException e) {
                runOnUiThread(() -> {
                    loading.setImageResource(R.mipmap.loading_2233_error);
                    MsgUtil.quickErr(MsgUtil.err_net, this);
                });
                e.printStackTrace();
            }
        });
    }

    public void setCurrentEpisodeInfo(MediaSectionInfo.EpisodeInfo currentEpisodeInfo) {
        if(replyFragment!=null) runOnUiThread(()->replyFragment.refresh(currentEpisodeInfo.aid));
        Log.e("","REF");
        //fragmentList.set(1, VideoReplyFragment.newInstance(currentEpisodeInfo.aid, 1));
        //if(mediaViewPager2Adapter != null) mediaViewPager2Adapter.notifyItemChanged(1);
    }

    private List<Fragment> createFragmentList(VideoInfo videoInfo) {
        List<Fragment> list;
        if(type.equals("media")) {
            list = new ArrayList<>(2);
            list.add(MediaInfoFragment.newInstance(mediaId));
            replyFragment = VideoReplyFragment.newInstance(mediaId, 1,true);
            list.add(replyFragment);
        } else {
            list = new ArrayList<>(3);
            list.add(VideoInfoFragment.newInstance(videoInfo));
            list.add(VideoReplyFragment.newInstance(videoInfo.aid, 1));
            if (SharedPreferencesUtil.getBoolean("related_enable", true)) {
                VideoRcmdFragment vrFragment = VideoRcmdFragment.newInstance(videoInfo.aid);
                list.add(vrFragment);
            }
        }
        return list;
    }

}