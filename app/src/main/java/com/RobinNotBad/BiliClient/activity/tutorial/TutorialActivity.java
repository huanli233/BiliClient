package com.RobinNotBad.BiliClient.activity.tutorial;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends BaseActivity {
    private List<Fragment> fragmentList;
    VideoReplyFragment replyFragment;
    
    public static int tutorial_version = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);

        ViewPager viewPager = findViewById(R.id.viewPager);

        setPageName("教程");

        findViewById(R.id.top).setOnClickListener(view -> Toast.makeText(this,"看完教程吧",Toast.LENGTH_SHORT).show());

        fragmentList = new ArrayList<>(8);
        fragmentList.add(new MainFragment());
        fragmentList.add(new RecommendFragment());
        fragmentList.add(new VideoFragment());
        fragmentList.add(new SpaceFragment());
        fragmentList.add(new ArticleFragment());
        fragmentList.add(new DynamicFragment());
        fragmentList.add(new SearchFragment());
        fragmentList.add(new OtherFragment());
        viewPager.setOffscreenPageLimit(fragmentList.size());
        ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(),fragmentList);
        viewPager.setAdapter(vpfAdapter);
    }

    @Override
    public void onBackPressed() {}
}
