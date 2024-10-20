package com.RobinNotBad.BiliClient.activity.video.series;

import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.SeriesCardAdapter;
import com.RobinNotBad.BiliClient.api.SeriesApi;
import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.model.Series;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;
import java.util.List;

//用户的视频系列列表
//2024-06-13

//我宣布：
//在此之后：series统一叫系列，collection统一叫合集
//我看b站他们自己都没搞明白，全都叫合集，请允许我在此问候下他们的开发者……
//2024-10-20

public class UserSeriesActivity extends RefreshListActivity {

    private long mid;
    private ArrayList<Series> seriesList;
    private SeriesCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("投稿的系列");

        seriesList = new ArrayList<>();
        mid = getIntent().getLongExtra("mid", 0);

        setOnLoadMoreListener(this::continueLoading);

        CenterThreadPool.run(() -> {
            try {
                bottom = (SeriesApi.getUserSeries(mid, page, seriesList) == 1);
                setRefreshing(false);
                adapter = new SeriesCardAdapter(this, seriesList);
                setAdapter(adapter);
                if (bottom && seriesList.isEmpty()) showEmptyView();
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                int last = seriesList.size();
                int result = SeriesApi.getUserSeries(mid, page, seriesList);
                if (result != -1) {
                    Log.e("debug", "下一页");
                    runOnUiThread(() -> adapter.notifyItemRangeInserted(last, seriesList.size() - last));
                    if (result == 1) {
                        Log.e("debug", "到底了");
                        bottom = true;
                    }
                }
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }
}