package com.RobinNotBad.BiliClient.activity.article;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.event.ReplyEvent;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.AnimationUtils;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import com.RobinNotBad.BiliClient.util.TerminalContext;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ArticleInfoActivity extends BaseActivity {
    private long cvid;

    private ReplyFragment replyFragment;
    private long seek_reply;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        Intent intent = getIntent();
        cvid = intent.getLongExtra("cvid", 114514);
        this.seek_reply = getIntent().getLongExtra("seekReply", -1);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("专栏详情");

        TutorialHelper.showTutorialList(this, R.array.tutorial_article, 7);

        ViewPager viewPager = findViewById(R.id.viewPager);

        CenterThreadPool.run(() -> {
            try {
                ArticleInfo articleInfo = ArticleApi.getArticle(cvid);
                TerminalContext.getInstance().enterArticleDetailPage(articleInfo);
                List<Fragment> fragmentList = new ArrayList<>();
                ArticleInfoFragment articleInfoFragment = ArticleInfoFragment.newInstance();
                fragmentList.add(articleInfoFragment);
                replyFragment = ReplyFragment.newInstance(cvid, ReplyApi.REPLY_TYPE_ARTICLE, seek_reply, articleInfo != null ? articleInfo.upInfo.mid : -1);
                fragmentList.add(replyFragment);

                runOnUiThread(() -> {
                    ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);
                    viewPager.setAdapter(vpfAdapter);
                    View view;
                    if ((view = articleInfoFragment.getView()) != null)
                        view.setVisibility(View.GONE);
                    if (seek_reply != -1) viewPager.setCurrentItem(1);

                    articleInfoFragment.setOnFinishLoad(() -> AnimationUtils.crossFade(findViewById(R.id.loading), articleInfoFragment.getView()));

                    TutorialHelper.showPagerTutorial(this,2);
                });
            } catch (Exception e) {
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
    public void onEvent(ReplyEvent event) {
        replyFragment.notifyReplyInserted(event);
    }

    @Override
    protected void onDestroy() {
        TerminalContext.getInstance().leaveDetailPage();
        super.onDestroy();
    }
}
