package com.RobinNotBad.BiliClient.activity.user.info;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.DynamicHolder;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

//用户信息页面
//2023-08-07

public class UserInfoActivity extends BaseActivity {

    UserDynamicFragment udFragment;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        long mid = intent.getLongExtra("mid",114514);

        setPageName("用户信息");

        ViewPager viewPager = findViewById(R.id.viewPager);

        List<Fragment> fragmentList = new ArrayList<>();
        udFragment = UserDynamicFragment.newInstance(mid);
        fragmentList.add(udFragment);
        UserVideoFragment uvFragment = UserVideoFragment.newInstance(mid);
        fragmentList.add(uvFragment);
        UserArticleFragment acFragment = UserArticleFragment.newInstance(mid);
        fragmentList.add(acFragment);
        viewPager.setOffscreenPageLimit(fragmentList.size());

        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);

        viewPager.setAdapter(vpfAdapter);  //没啥好说的，教科书式的ViewPager使用方法

        if(!SharedPreferencesUtil.getBoolean("tutorial_user",false)){
            MsgUtil.showTutorial(this,"使用教程","此页面从左向右或从右向左滑动可以切换页面，第一页为动态列表，第二页为视频列表，第三页为专栏列表\n登录后可以关注用户，关注过的用户会显示私信按钮\n点击动态的文字部分可以查看动态详情",R.mipmap.tutorial_user);
            SharedPreferencesUtil.putBoolean("tutorial_user",true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DynamicHolder.GO_TO_INFO_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                udFragment.onDynamicRemove(data.getIntExtra("position", 0) - 1);
            }
        }
    }
}