package com.RobinNotBad.BiliClient.activity.user.info;

import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.SeasonCardAdapter;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;
import java.util.List;

//用户合集列表
//2024-06-13

public class UserCollectionActivity extends RefreshListActivity {

    private long mid;
    private ArrayList<Collection> seasonList;
    private SeasonCardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("投稿合集列表");

        seasonList = new ArrayList<>();
        mid = getIntent().getLongExtra("mid",0);

        setOnLoadMoreListener(this::continueLoading);

        CenterThreadPool.run(()->{
            try {
                bottom = (UserInfoApi.getUserSeasons(mid, page, seasonList) == 1);
                setRefreshing(false);
                adapter = new SeasonCardAdapter(this, seasonList);
                setAdapter(adapter);
            } catch (Exception e){loadFail(e);}
        });
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                List<Collection> list = new ArrayList<>();
                int result = UserInfoApi.getUserSeasons(mid,page,list);
                if(result != -1){
                    Log.e("debug","下一页");
                    runOnUiThread(()-> {
                        seasonList.addAll(list);
                        adapter.notifyItemRangeInserted(seasonList.size() - list.size(), list.size());
                    });
                    if(result == 1) {
                        Log.e("debug","到底了");
                        bottom = true;
                    }
                }
                setRefreshing(false);
            } catch (Exception e){loadFail(e);}
        });
    }
}