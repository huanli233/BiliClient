package com.RobinNotBad.BiliClient.activity.user.favorite;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.favorite.FavoriteFolderAdapter;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.model.FavoriteFolder;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

//收藏夹列表
//2023-08-07
//2024-07-25

public class FavoriteFolderListActivity extends RefreshListActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("收藏");

        long mid = SharedPreferencesUtil.getLong("mid", 0);

        CenterThreadPool.run(() -> {
            try {
                ArrayList<FavoriteFolder> folderList = FavoriteApi.getFavoriteFolders(mid);
                FavoriteFolderAdapter adapter = new FavoriteFolderAdapter(this, folderList, mid);
                setAdapter(adapter);
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });

    }
}