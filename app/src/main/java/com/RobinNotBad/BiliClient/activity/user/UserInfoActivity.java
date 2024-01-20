package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

//用户信息页面
//2023-08-07

public class UserInfoActivity extends BaseActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        long mid = intent.getLongExtra("mid",114514);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("用户信息");

        ViewPager viewPager = findViewById(R.id.viewPager);

        List<Fragment> fragmentList = new ArrayList<>();
        UserDynamicFragment udFragment = UserDynamicFragment.newInstance(mid);
        fragmentList.add(udFragment);
        UserVideoFragment uvFragment = UserVideoFragment.newInstance(mid);
        fragmentList.add(uvFragment);
        UserArticleFragment acFragment = UserArticleFragment.newInstance(mid);
        fragmentList.add(acFragment);
        viewPager.setOffscreenPageLimit(fragmentList.size());

        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);

        viewPager.setAdapter(vpfAdapter);  //没啥好说的，教科书式的ViewPager使用方法

        if(!SharedPreferencesUtil.getBoolean("tutorial_user",false)){
            MsgUtil.showDialog(this,"使用教程","此页面从左向右或从右向左滑动可以切换页面，第一页为动态列表，第二页为视频列表，第三页为专栏列表\n登录后可以关注用户，关注过的用户会显示私信按钮\n点击动态的文字部分可以查看动态详情",R.mipmap.tutorial_user,true,5);
            SharedPreferencesUtil.putBoolean("tutorial_user",true);
        }
    }
}