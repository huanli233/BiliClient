package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.RobinNotBad.BiliClient.activity.base.RefreshMainActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.RecommendApi;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//推荐页面
//2023-07-13

public class RecommendActivity extends RefreshMainActivity {

    private List<VideoCard> videoCardList;
    private VideoCardAdapter videoCardAdapter;
    private boolean firstRefresh = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuClick();
        Log.e("debug","进入推荐页");

        setOnRefreshListener(this::refreshRecommend);
        setOnLoadMoreListener(page -> addRecommend());

        setPageName("推荐");

        recyclerView.setHasFixedSize(true);
        
        TutorialHelper.show(R.xml.tutorial_recommend,this,"recommend",1);

        refreshRecommend();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshRecommend() {
        Log.e("debug", "刷新");
        if (firstRefresh) {
            videoCardList = new ArrayList<>();
        } else {
            int last = videoCardList.size();
            videoCardList.clear();
            videoCardAdapter.notifyItemRangeRemoved(0,last);
        }

        addRecommend();
    }

    private void addRecommend() {
        Log.e("debug", "加载下一页");
        CenterThreadPool.run(()->{
            try {
                List<VideoCard> list = new ArrayList<>();
                RecommendApi.getRecommend(list);
                setRefreshing(false);

                runOnUiThread(() -> {
                    videoCardList.addAll(list);
                    if (firstRefresh) {
                        firstRefresh = false;
                        videoCardAdapter = new VideoCardAdapter(this, videoCardList);
                        setAdapter(videoCardAdapter);
                    } else {
                        videoCardAdapter.notifyItemRangeInserted(videoCardList.size() - list.size(), list.size());
                    }
                });
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}