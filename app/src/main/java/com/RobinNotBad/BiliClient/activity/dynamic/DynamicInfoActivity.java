package com.RobinNotBad.BiliClient.activity.dynamic;

import static com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity.getRelayDynamicLauncher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

//动态信息页面
//2023-10-03

public class DynamicInfoActivity extends BaseActivity {


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        long id = intent.getLongExtra("id",0);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("动态详情");

        CenterThreadPool.run(()->{
            try {
                Dynamic dynamic = DynamicApi.getDynamic(id);

                List<Fragment> fragmentList = new ArrayList<>();
                DynamicInfoFragment diFragment = DynamicInfoFragment.newInstance(dynamic);
                fragmentList.add(diFragment);
                VideoReplyFragment rFragment = VideoReplyFragment.newInstance(dynamic.comment_id, dynamic.comment_type);
                rFragment.isDynamic = true;
                fragmentList.add(rFragment);

                ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);

                runOnUiThread(()->{
                    ViewPager viewPager = findViewById(R.id.viewPager);
                    viewPager.setAdapter(vpfAdapter);  //没啥好说的，教科书式的ViewPager使用方法

                    if(SharedPreferencesUtil.getBoolean("first_dynamicinfo",true)){
                        MsgUtil.toast("下载完成",this);
                        SharedPreferencesUtil.putBoolean("first_dynamicinfo",false);
                    }
                });

            } catch (Exception e) {
                MsgUtil.err(e,this);
            }
        });

    }
}