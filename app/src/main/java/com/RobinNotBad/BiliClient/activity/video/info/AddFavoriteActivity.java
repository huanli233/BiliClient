package com.RobinNotBad.BiliClient.activity.video.info;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.FolderChooseAdapter;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

//添加收藏
//2023-08-28

public class AddFavoriteActivity extends BaseActivity {
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
        setContentView(R.layout.activity_simple_list);

        setPageName("添加收藏");

        Intent intent = getIntent();
        aid = intent.getLongExtra("aid",0);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        CenterThreadPool.run(()->{
            try {
                FavoriteApi.getFavoriteState(aid,folderList,fidList,stateList);

                adapter = new FolderChooseAdapter(this,folderList,fidList,stateList,aid);

                runOnUiThread(()->{
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                });
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.err(e, this));
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(adapter!=null) {
            if (SharedPreferencesUtil.getBoolean("fav_notice", false)) {
                if (adapter.added) MsgUtil.toast("添加成功", this);
                else if (adapter.changed) MsgUtil.toast("更改成功", this);
            }
            Intent intent = new Intent();
            if(adapter.added) {
                setResult(RESULT_ADDED, intent);
            }
            else if(adapter.isAllDeleted()){
                setResult(RESULT_DELETED,intent);
            }
        }
        
        super.onDestroy();
        
    }
}