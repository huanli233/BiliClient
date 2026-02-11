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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//历史记录
//2023-08-18
//2024-04-30
//2026-02-11 修复专栏重复记录问题

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
                    // 增强去重逻辑，处理专栏重复记录问题
                    videoList.addAll(deduplicateHistory(rawList));

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
                        List<VideoCard> dedupList = deduplicateHistory(list);
                        int start = videoList.size();
                        videoList.addAll(dedupList);
                        videoCardAdapter.notifyItemRangeInserted(start, dedupList.size());
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

    /**
     * 增强的去重逻辑，处理专栏重复记录问题。
     *
     * 历史接口按时间倒序返回，首条为最新。遇到重复key时保留首条，
     * 后续重复记录（通常是较旧或异常形态）标记为待删除。
     */
    private List<VideoCard> deduplicateHistory(List<VideoCard> rawList) {
        android.util.Log.d("HistoryActivity", "开始去重,原始记录数: " + rawList.size());
        
        // 防御性检查
        if (rawList == null || rawList.isEmpty()) {
            android.util.Log.d("HistoryActivity", "列表为空,跳过去重");
            return new ArrayList<>();
        }
        
        java.util.LinkedHashMap<String, VideoCard> dedup = new java.util.LinkedHashMap<>();
        Set<Long> toDelete = new LinkedHashSet<>();
        
        int index = 0;
        for (VideoCard card : rawList) {
            index++;
            
            // 防御性检查
            if (card == null) {
                android.util.Log.w("HistoryActivity", "第" + index + "条记录为null,跳过");
                continue;
            }
            
            String key = card.type + "_" + card.aid;
            
            android.util.Log.d("HistoryActivity", "处理第" + index + "条: type=" + card.type + ", aid=" + card.aid + ", kid=" + card.kid + ", title=" + card.title);
            
            if (!dedup.containsKey(key)) {
                // 第一次遇到这个内容，直接添加（首条即最新）
                dedup.put(key, card);
                android.util.Log.d("HistoryActivity", "  -> 首次添加");
            } else {
                // 发现重复记录：优先保留非占位符
                VideoCard keptCard = dedup.get(key);
                boolean keptPlaceholder = keptCard != null && keptCard.historyPlaceholder;
                boolean currentPlaceholder = card.historyPlaceholder;

                if (keptPlaceholder && !currentPlaceholder) {
                    // 用正常记录替换占位符
                    android.util.Log.d("HistoryActivity", "  -> 替换占位符: 保留kid=" + card.kid + ", 删除占位符kid=" + (keptCard != null ? keptCard.kid : "null"));
                    if (keptCard != null && keptCard.kid > 0) {
                        toDelete.add(keptCard.kid);
                        android.util.Log.d("HistoryActivity", "  -> 标记删除占位符kid=" + keptCard.kid);
                    }
                    dedup.put(key, card);
                } else {
                    // 保留已存在记录，删除当前（较旧或占位符）记录
                    android.util.Log.d("HistoryActivity", "  -> 发现重复! 保留kid=" + (keptCard != null ? keptCard.kid : "null") + ", 删除kid=" + card.kid);
                    if (card.kid > 0) {
                        toDelete.add(card.kid);
                        android.util.Log.d("HistoryActivity", "  -> 标记删除重复记录kid=" + card.kid);
                    }
                }
            }
        }
        
        android.util.Log.d("HistoryActivity", "去重完成,保留记录数: " + dedup.size() + ", 待删除记录数: " + toDelete.size());
        
        // 异步清理重复记录和占位符
        if (!toDelete.isEmpty()) {
            android.util.Log.d("HistoryActivity", "开始异步删除记录: " + toDelete);
            CenterThreadPool.run(() -> {
                for (long kid : toDelete) {
                    try {
                        android.util.Log.d("HistoryActivity", "删除历史记录kid=" + kid);
                        HistoryApi.deleteHistory(kid);
                    } catch (Exception e) {
                        android.util.Log.e("HistoryActivity", "删除历史记录失败: kid=" + kid, e);
                    }
                }
                android.util.Log.d("HistoryActivity", "异步删除完成");
            });
        }
        
        return new ArrayList<>(dedup.values());
    }
}
