package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.user.FollowListAdapter;
import com.RobinNotBad.BiliClient.api.FollowApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

//关注列表
//2023-07-22
//2024-05-01

public class FollowingUsersActivity extends RefreshListActivity {

    private long mid;
    private ArrayList<UserInfo> userList;
    private FollowListAdapter adapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("关注");

        recyclerView.setHasFixedSize(true);

        mid = SharedPreferencesUtil.getLong("mid",0);
        userList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                int result = FollowApi.getFollowList(mid, page, userList);
                adapter = new FollowListAdapter(this,userList);
                setOnLoadMoreListener(this::continueLoading);
                setRefreshing(false);
                setAdapter(adapter);

                if (result == 1) {
                    Log.e("debug", "到底了");
                    setBottom(true);
                }
            } catch (Exception e){
                report(e);
                setRefreshing(false);
            }
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                int lastSize = userList.size();
                int result = FollowApi.getFollowList(mid, page, userList);
                Log.e("debug", "下一页");
                runOnUiThread(() -> adapter.notifyItemRangeInserted(lastSize, userList.size() - lastSize));
                if (result == 1) {
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