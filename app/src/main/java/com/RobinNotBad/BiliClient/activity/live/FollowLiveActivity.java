package com.RobinNotBad.BiliClient.activity.live;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.LiveCardAdapter;
import com.RobinNotBad.BiliClient.api.LiveApi;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;
import java.util.List;

public class FollowLiveActivity extends RefreshListActivity {
    private List<LiveRoom> roomList;
    private LiveCardAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("我关注的直播");

        recyclerView.setHasFixedSize(true);

        roomList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                roomList = LiveApi.getFollowed(page);
                adapter = new LiveCardAdapter(this,roomList);
                setOnLoadMoreListener(this::continueLoading);
                setRefreshing(false);
                setAdapter(adapter);
                if(roomList.size() < 1) showEmptyView();
            } catch (Exception e){
                report(e);
                setRefreshing(false);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                List<LiveRoom> list;
                list = LiveApi.getFollowed(page);
                Log.e("debug", "下一页");
                runOnUiThread(() -> {
                    if(list != null){
                        roomList.addAll(list);
                        adapter.notifyItemRangeInserted(roomList.size() - list.size(), list.size());
                    }
                });
                if (list != null && list.size() < 1) {
                    Log.e("debug", "到底了");
                    setBottom(true);
                }
                setRefreshing(false);
            } catch (Exception e){
                report(e);
                setRefreshing(false);
                this.page--;
            }
        });
    }
}
