package com.RobinNotBad.BiliClient.activity.user;

import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.HistoryApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;

//历史记录
//2023-08-18
//2024-04-30

public class HistoryActivity extends RefreshListActivity {

    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter videoCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("历史记录");

        videoList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                int result = HistoryApi.getHistory(page,videoList);
                if(result != -1) {
                    videoCardAdapter = new VideoCardAdapter(this, videoList);
                    setLoadMoreListener(this::continueLoading);
                    setRefreshing(false);
                    setAdapter(videoCardAdapter);

                    if(result == 1) {
                        Log.e("debug","到底了");
                        setBottom(true);
                    }
                }

            } catch (Exception e) {
                report(e);
                setRefreshing(false);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                int lastSize = videoList.size();
                int result = HistoryApi.getHistory(page,videoList);
                if(result != -1){
                    Log.e("debug","下一页");
                    runOnUiThread(()-> videoCardAdapter.notifyItemRangeInserted(lastSize,videoList.size()-lastSize));
                    if(result == 1) {
                        Log.e("debug","到底了");
                        setBottom(true);
                    }
                }
                setRefreshing(false);
            } catch (Exception e) {
                report(e);
                setRefreshing(false);
                this.page--;
            }
        });
    }
}