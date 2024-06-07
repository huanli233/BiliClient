package com.RobinNotBad.BiliClient.activity.user.info;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.dynamic.DynamicHolder;
import com.RobinNotBad.BiliClient.adapter.viewpager.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;

import java.util.ArrayList;
import java.util.List;

//用户信息页面
//2023-08-07

public class UserInfoActivity extends BaseActivity {

    UserDynamicFragment udFragment;

    @SuppressLint({"MissingInflatedId", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cell_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_simple_viewpager, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setTopbarExit();
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

            findViewById(R.id.loading).setVisibility(View.GONE);
        });
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