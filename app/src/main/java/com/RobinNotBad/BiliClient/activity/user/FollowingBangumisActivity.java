package com.RobinNotBad.BiliClient.activity.user;

import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.BangumiApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;

//追番列表
//2024-06-07

public class FollowingBangumisActivity extends RefreshListActivity {

    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter videoCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("追番列表");

        recyclerView.setHasFixedSize(true);

        videoList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                int result = BangumiApi.getFollowingList(page,videoList);
                if(result != -1) {
                    videoCardAdapter = new VideoCardAdapter(this, videoList);
                    setOnLoadMoreListener(this::continueLoading);
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
                int result = BangumiApi.getFollowingList(page,videoList);
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