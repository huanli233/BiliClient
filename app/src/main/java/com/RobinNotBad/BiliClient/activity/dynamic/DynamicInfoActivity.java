package com.RobinNotBad.BiliClient.activity.dynamic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
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
        long rid = intent.getLongExtra("rid",0);
        int type = intent.getIntExtra("type",1);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("动态详情");

        ViewPager viewPager = findViewById(R.id.viewPager);

        List<Fragment> fragmentList = new ArrayList<>();
        DynamicInfoFragment diFragment = DynamicInfoFragment.newInstance(id);
        fragmentList.add(diFragment);
        VideoReplyFragment rFragment = VideoReplyFragment.newInstance(rid, type);
        fragmentList.add(rFragment);

        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);

        viewPager.setAdapter(vpfAdapter);  //没啥好说的，教科书式的ViewPager使用方法

        if(SharedPreferencesUtil.getBoolean("first_dynamicinfo",true)){
            Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
            SharedPreferencesUtil.putBoolean("first_dynamicinfo",false);
        }
    }
}