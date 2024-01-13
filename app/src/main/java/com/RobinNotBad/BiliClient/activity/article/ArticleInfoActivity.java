package com.RobinNotBad.BiliClient.activity.article;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

public class ArticleInfoActivity extends BaseActivity {
    private static final String TAG = "ArticleInfoActivity";
    private long cvid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        cvid = intent.getLongExtra("cvid", 114514);
        findViewById(R.id.top).setOnClickListener(view -> finish());

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("专栏详情");

        ViewPager viewPager = findViewById(R.id.viewPager);

        CenterThreadPool.run(() -> {
            try {
                List<Fragment> fragmentList = new ArrayList<>();
                ArticleInfoFragment articleInfoFragment = ArticleInfoFragment.newInstance(cvid);
                fragmentList.add(articleInfoFragment);
                VideoReplyFragment vrFragment = VideoReplyFragment.newInstance(cvid,12);
                fragmentList.add(vrFragment);

                runOnUiThread(() -> {
                    ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
                    viewPager.setAdapter(vpfAdapter);

                    if(SharedPreferencesUtil.getBoolean("first_articleinfo",true)){
                        Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
                        SharedPreferencesUtil.putBoolean("first_articleinfo",false);
                    }
                });
            }catch (Exception e){
                Log.wtf(TAG, e);
            }
        });
    }
}
