package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshMainActivity;
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.RecommendApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

//推荐页面
//2023-07-13

public class RecommendActivity extends RefreshMainActivity {

    private ArrayList<VideoCard> videoCardList;
    private VideoCardAdapter videoCardAdapter;
    private boolean firstRefresh = true;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMenuClick(0);
        Log.e("debug","进入推荐页");

        setOnRefreshListener(this::refreshRecommend);
        setOnLoadMoreListener(page -> addRecommend());

        setPageName("推荐");

        if(!SharedPreferencesUtil.getBoolean("tutorial_recommend",false)){
            MsgUtil.showTutorial(this,"使用教程","点击上方标题栏可以打开菜单\n\n*我知道你可能不喜欢强制看教程，但这是必要的，敬请谅解QwQ",R.mipmap.tutorial_recommend);
            SharedPreferencesUtil.putBoolean("tutorial_recommend",true);
        }

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
            int lastSize = videoCardList.size();
            try {
                RecommendApi.getRecommend(videoCardList);
                setRefreshing(false);

                runOnUiThread(() -> {
                    if (firstRefresh) {
                        firstRefresh = false;
                        videoCardAdapter = new VideoCardAdapter(this, videoCardList);
                        setAdapter(videoCardAdapter);
                    }else {
                        Log.e("debug","last="+lastSize+"&now="+videoCardList.size());
                        videoCardAdapter.notifyItemRangeInserted(lastSize,videoCardList.size()-lastSize);
                    }
                });
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}