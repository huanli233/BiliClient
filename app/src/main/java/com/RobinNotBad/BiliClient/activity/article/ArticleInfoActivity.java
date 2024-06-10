package com.RobinNotBad.BiliClient.activity.article;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.reply.ReplyFragment;
import com.RobinNotBad.BiliClient.adapter.viewpager.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.api.ArticleApi;
import com.RobinNotBad.BiliClient.event.ReplyEvent;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ArticleInfoActivity extends BaseActivity {
    private static final String TAG = "ArticleInfoActivity";
    private long cvid;

    private ReplyFragment replyFragment;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        cvid = intent.getLongExtra("cvid", 114514);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("专栏详情");
        
        TutorialHelper.show(R.xml.tutorial_article,this,"article",1);

        ViewPager viewPager = findViewById(R.id.viewPager);

        CenterThreadPool.run(() -> {
            try {
                ArticleInfo articleInfo;
                List<Fragment> fragmentList = new ArrayList<>();
                ArticleInfoFragment articleInfoFragment = ArticleInfoFragment.newInstance(articleInfo = ArticleApi.getArticle(cvid));
                fragmentList.add(articleInfoFragment);
                replyFragment = ReplyFragment.newInstance(cvid, 12);
                replyFragment.setSource(articleInfo);
                fragmentList.add(replyFragment);

                runOnUiThread(() -> {
                    ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
                    viewPager.setAdapter(vpfAdapter);
                    findViewById(R.id.loading).setVisibility(View.GONE);
                });
            }catch (Exception e){
                runOnUiThread(() -> {
                    ((ImageView) findViewById(R.id.loading)).setImageResource(R.mipmap.loading_2233_error);
                    MsgUtil.err(e, this);
                });
            }
        });
    }

    @Override
    protected boolean eventBusEnabled() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true, priority = 1)
    public void onEvent(ReplyEvent event){
        replyFragment.notifyReplyInserted(event.getMessage());
    }
}
