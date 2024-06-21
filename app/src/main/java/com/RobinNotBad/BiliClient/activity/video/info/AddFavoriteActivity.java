package com.RobinNotBad.BiliClient.activity.video.info;

import android.content.Intent;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.favorite.FolderChooseAdapter;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

//添加收藏
//2023-08-28

public class AddFavoriteActivity extends RefreshListActivity {
    FolderChooseAdapter adapter;
    ArrayList<String> folderList = new ArrayList<>();
    ArrayList<Boolean> stateList = new ArrayList<>();
    ArrayList<Long> fidList = new ArrayList<>();
    long aid;
    int RESULT_ADDED = 1;
    int RESULT_DELETED = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageName("添加收藏");

        Intent intent = getIntent();
        aid = intent.getLongExtra("aid",0);

        if(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0) == 0) {
            MsgUtil.toast("还没有登录喵~",this);
            finish();
            return;
        }

        CenterThreadPool.run(()->{
            try {
                FavoriteApi.getFavoriteState(aid,folderList,fidList,stateList);

                adapter = new FolderChooseAdapter(this,folderList,fidList,stateList,aid);

                setAdapter(adapter);

                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    @Override
    public void finish() {
        if (adapter != null) {
            if (adapter.added) {
                setResult(RESULT_ADDED);
            } else if (adapter.isAllDeleted()) {
                setResult(RESULT_DELETED);
            }
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        if (adapter!=null) {
            if (SharedPreferencesUtil.getBoolean("fav_notice", false)) {
                if (adapter.added) MsgUtil.toast("添加成功", this);
                else if (adapter.changed) MsgUtil.toast("更改成功", this);
            }
        }
        
        super.onDestroy();
        
    }
}