package com.RobinNotBad.BiliClient.activity.article;

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
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArticleInfoActivity extends BaseActivity {
    private long cvid;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        cvid = intent.getLongExtra("cvid",(long)114514);
        findViewById(R.id.top).setOnClickListener(view -> finish());

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("专栏详情");

        ViewPager viewPager = findViewById(R.id.viewPager);

        CenterThreadPool.run(() -> {
            try {
                ArticleInfo articleInfo = ArticleApi.getArticle(cvid);

                List<Fragment> fragmentList = new ArrayList<>();
                ArticleInfoFragment articleInfoFragment = ArticleInfoFragment.newInstance(articleInfo);
                fragmentList.add(articleInfoFragment);
                VideoReplyFragment vpFragment = VideoReplyFragment.newInstance(articleInfo.id,12);
                fragmentList.add(vpFragment);

                runOnUiThread(() -> {
                    ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
                    viewPager.setAdapter(vpfAdapter);

                    if(SharedPreferencesUtil.getBoolean("first_articleinfo",true)){
                        Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
                        SharedPreferencesUtil.putBoolean("first_articleinfo",false);
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }catch (JSONException e){
                e.printStackTrace();
            }
        });
    }
}
