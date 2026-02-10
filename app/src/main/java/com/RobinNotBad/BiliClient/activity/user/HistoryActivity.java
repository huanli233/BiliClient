package com.RobinNotBad.BiliClient.activity.user;

import android.os.Bundle;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.HistoryApi;
import com.RobinNotBad.BiliClient.model.ApiResult;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

//历史记录
//2023-08-18
//2024-04-30

public class HistoryActivity extends RefreshListActivity {

    private ApiResult lastResult = new ApiResult();
    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter videoCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("历史记录");

        recyclerView.setHasFixedSize(true);

        videoList = new ArrayList<>();

        CenterThreadPool.run(() -> {
            try {
                List<VideoCard> rawList = new ArrayList<>();
                lastResult = HistoryApi.getHistory(lastResult, rawList);
                if (lastResult.code == 0) {
                    // 按 type + aid 去重，只保留最新一条
                    java.util.LinkedHashMap<String, VideoCard> dedup = new java.util.LinkedHashMap<>();
                    for (VideoCard card : rawList) {
                        String key = card.type + "_" + card.aid;
                        if (!dedup.containsKey(key)) {
                            dedup.put(key, card);
                        }
                    }
                    videoList.addAll(dedup.values());

                    videoCardAdapter = new VideoCardAdapter(this, videoList);
                    setOnLoadMoreListener(this::continueLoading);
                    setRefreshing(false);
                    setAdapter(videoCardAdapter);

                    if (lastResult.isBottom) {
                        setBottom(true);
                    }
                } else MsgUtil.showMsg(lastResult.message);

            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                List<VideoCard> list = new ArrayList<>();
                lastResult = HistoryApi.getHistory(lastResult, list);
                if (lastResult.code == 0) {
                    runOnUiThread(() -> {
                        java.util.LinkedHashMap<String, VideoCard> dedup = new java.util.LinkedHashMap<>();
                        for (VideoCard card : list) {
                            String key = card.type + "_" + card.aid;
                            if (!dedup.containsKey(key)) {
                                dedup.put(key, card);
                            }
                        }
                        int start = videoList.size();
                        videoList.addAll(dedup.values());
                        videoCardAdapter.notifyItemRangeInserted(start, dedup.size());
                    });
                    if (lastResult.isBottom) {
                        setBottom(true);
                    }
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}