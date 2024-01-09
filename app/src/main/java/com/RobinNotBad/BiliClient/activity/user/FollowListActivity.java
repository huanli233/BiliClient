package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.FollowListAdapter;
import com.RobinNotBad.BiliClient.api.FollowApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//评论详细信息
//2023-07-22

public class FollowListActivity extends BaseActivity {

    private long mid;
    private RecyclerView recyclerView;
    private ArrayList<UserInfo> userList;
    private FollowListAdapter adapter;
    private boolean bottom = false;
    private int page = 1;
    private boolean refreshing = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        recyclerView = findViewById(R.id.recyclerView);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("关注");

        mid = SharedPreferencesUtil.getLong("mid",0);
        userList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                int result = FollowApi.getFollowList(mid, page, userList);
                adapter = new FollowListAdapter(this,userList);
                runOnUiThread(() -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setAdapter(adapter);
                    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                        }

                        @Override
                        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);
                            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                            assert manager != null;
                            int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();  //获取最后一个完全显示的itemPosition
                            int itemCount = manager.getItemCount();
                            if (lastItemPosition >= (itemCount - 3) && dy > 0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                                refreshing = true;
                                CenterThreadPool.run(() -> continueLoading()); //加载第二页
                            }
                        }
                    });
                });
                if (result == 1) {
                    Log.e("debug", "到底了");
                    bottom = true;
                }


            } catch (IOException e){
                runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,this));
                e.printStackTrace();
            } catch (JSONException e) {
                runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_json,this));
                e.printStackTrace();
            }
        });
    }

    private void continueLoading() {
        page++;
        try {
            int lastSize = userList.size();
            int result = FollowApi.getFollowList(mid, page, userList);
            Log.e("debug", "下一页");
            runOnUiThread(() -> adapter.notifyItemRangeInserted(lastSize, userList.size() - lastSize));
            if (result == 1) {
                Log.e("debug", "到底了");
                bottom = true;
            }
            refreshing = false;
        } catch (IOException e){
            runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,this));
            e.printStackTrace();
        } catch (JSONException e) {
            runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_json,this));
            e.printStackTrace();
        }
    }
}