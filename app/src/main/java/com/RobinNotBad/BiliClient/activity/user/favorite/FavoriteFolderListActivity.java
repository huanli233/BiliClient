package com.RobinNotBad.BiliClient.activity.user.favorite;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.favorite.FavoriteFolderAdapter;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.model.FavoriteFolder;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

//收藏夹列表
//2023-08-07

public class FavoriteFolderListActivity extends BaseActivity {

    private RecyclerView recyclerView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        long mid = SharedPreferencesUtil.getLong("mid",0);

        recyclerView = findViewById(R.id.recyclerView);

        setPageName("收藏");

        CenterThreadPool.run(()->{
            try {
                ArrayList<FavoriteFolder> folderList = FavoriteApi.getFavoriteFolders(mid);
                FavoriteFolderAdapter adapter = new FavoriteFolderAdapter(this,folderList,mid);
                runOnUiThread(()->{
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                });
            } catch (Exception e) {report(e);}
        });
    }
}