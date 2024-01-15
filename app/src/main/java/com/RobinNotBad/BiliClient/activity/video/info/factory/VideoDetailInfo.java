package com.RobinNotBad.BiliClient.activity.video.info.factory;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoFragment;
import com.RobinNotBad.BiliClient.activity.video.info.VideoRcmdFragment;
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
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

class VideoDetailInfo extends DetailInfo {
    private final String bvid;
    private final long aid;
    public VideoDetailInfo(AppCompatActivity activity, String bvid, long aid) {
        super(activity);
        this.bvid = bvid;
        this.aid = aid;
    }

    @Override
    protected View createView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.activity_simple_viewpager, null, true);
    }

    @Override
    protected void initView() {
        View rootView = getRootView();
        ViewPager viewPager = rootView.findViewById(R.id.viewPager);
        TextView pageName = rootView.findViewById(R.id.pageName);
        ImageView loading = rootView.findViewById(R.id.loading);
        rootView.findViewById(R.id.top).setOnClickListener(view -> activity.finish());
        loading.setVisibility(View.VISIBLE);
        pageName.setText("视频详情");
        Log.e("VideoInfoActivity",SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""));

        CenterThreadPool.run(() -> {
            JSONObject data;
            try {
                VideoInfo videoInfo = null;
                if (bvid == null || TextUtils.isEmpty(bvid)) data = VideoInfoApi.getJsonByAid(aid);
                else data = VideoInfoApi.getJsonByBvid(bvid);
                JSONArray tagList;
                if (bvid == null || bvid.isEmpty()) tagList = VideoInfoApi.getTagsByAid(aid);
                else tagList = VideoInfoApi.getTagsByBvid(bvid);
                videoInfo = VideoInfoApi.getInfoByJson(data, tagList);
                List<Fragment> fragmentList = createFragmentsFromVideoType(videoInfo);
                viewPager.setOffscreenPageLimit(fragmentList.size());
                ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(activity.getSupportFragmentManager(), fragmentList);
                activity.runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    viewPager.setAdapter(vpfAdapter);
                    if (SharedPreferencesUtil.getBoolean("first_videoinfo", true)) {
                        Toast.makeText(activity, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
                        SharedPreferencesUtil.putBoolean("first_videoinfo", false);
                    }
                });
                //没啥好说的，教科书式的ViewPager使用方法
            } catch (JSONException e) {
                activity.runOnUiThread(() -> {
                    loading.setImageResource(R.drawable.loading_2233_error);
                    MsgUtil.jsonErr(e, activity);
                });
                e.printStackTrace();
            } catch (IOException e) {
                activity.runOnUiThread(() -> {
                    loading.setImageResource(R.drawable.loading_2233_error);
                    MsgUtil.quickErr(MsgUtil.err_net, activity);
                });
                e.printStackTrace();
            }
        });
    }

    private List<Fragment> createFragmentsFromVideoType(VideoInfo videoInfo) {
        //放侧滑页, 容量不多, 设置了初始值, 内存能省点, 为避免触发扩容, 以后在这里添加fragment的记得让初始容量等于塞进去的fragment数量.
        List<Fragment> fragmentList = new ArrayList<>(3);
        VideoInfoFragment viFragment = VideoInfoFragment.newInstance(videoInfo);
        fragmentList.add(viFragment);
        VideoReplyFragment vpFragment = VideoReplyFragment.newInstance(videoInfo.aid, 1);
        fragmentList.add(vpFragment);
        if (SharedPreferencesUtil.getBoolean("related_enable", true)) {
            VideoRcmdFragment vrFragment = VideoRcmdFragment.newInstance(videoInfo.aid);
            fragmentList.add(vrFragment);
        }
        return fragmentList;
    }

}
