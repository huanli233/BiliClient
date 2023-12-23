package com.RobinNotBad.BiliClient.activity.video.info;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//视频详情页，但这只是个壳，瓤是VideoInfoFragment、VideoReplyFragment、VideoRcmdFragment

public class VideoInfoActivity extends BaseActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        String bvid = intent.getStringExtra("bvid");
        long aid = intent.getLongExtra("aid",114514);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        ImageView loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("视频详情");

        ViewPager viewPager = findViewById(R.id.viewPager);

        new Thread(()->{
            JSONObject data;
            try {
                if (bvid==null || bvid.equals("")) data = VideoInfoApi.getJsonByAid(aid);
                else data = VideoInfoApi.getJsonByBvid(bvid);

                //ErrorUtil.showText(requireContext(),"调试",data.toString());

                JSONArray tagList;
                if (bvid==null || bvid.equals("")) tagList = VideoInfoApi.getTagJsonByAid(aid);
                else tagList = VideoInfoApi.getTagJsonByBvid(bvid);
                VideoInfo videoInfo = VideoInfoApi.getInfoByJson(data,tagList);

                viewPager.setOffscreenPageLimit(3);

                List<Fragment> fragmentList = new ArrayList<>();
                VideoInfoFragment viFragment = VideoInfoFragment.newInstance(videoInfo);
                fragmentList.add(viFragment);
                VideoReplyFragment vpFragment = VideoReplyFragment.newInstance(videoInfo.aid,1);
                fragmentList.add(vpFragment);
                VideoRcmdFragment vrFragment = VideoRcmdFragment.newInstance(videoInfo.aid);
                fragmentList.add(vrFragment);

                ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);

                runOnUiThread(()->{
                    loading.setVisibility(View.GONE);
                    viewPager.setAdapter(vpfAdapter);

                    if(SharedPreferencesUtil.getBoolean("first_videoinfo",true)){
                        Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
                        SharedPreferencesUtil.putBoolean("first_videoinfo",false);
                    }
                });
                //没啥好说的，教科书式的ViewPager使用方法
            }catch (JSONException e){
                runOnUiThread(() -> {
                    loading.setImageResource(R.drawable.loading_2233_error);
                    MsgUtil.jsonErr(e, this);
                });
                e.printStackTrace();
            }catch (IOException e){
                runOnUiThread(() -> {
                    loading.setImageResource(R.drawable.loading_2233_error);
                    MsgUtil.quickErr(MsgUtil.err_net,this);
                });
                e.printStackTrace();
            }

        }).start();

    }
}