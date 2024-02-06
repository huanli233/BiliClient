package com.RobinNotBad.BiliClient.activity.dynamic;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.DynamicAdapter;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.model.DynamicOld;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONObject;

import java.util.ArrayList;

//动态页面
//2023-09-17

public class DynamicActivity extends InstanceActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<DynamicOld> dynamicList;
    private DynamicAdapter dynamicAdapter;
    private long offset = 0;
    private boolean firstRefresh = true;
    private boolean refreshing = false;
    private boolean bottom = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_main_refresh);
        setMenuClick(4);
        Log.e("debug","进入动态页");

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshDynamic);

        TextView title = findViewById(R.id.pageName);
        title.setText("动态");

        if(!SharedPreferencesUtil.getBoolean("tutorial_dynamic",false)){
            MsgUtil.showDialog(this,"使用教程","点击动态的文字部分可以查看动态详情，打开详情后左右滑动可以查看评论区（被转发的动态和投稿类动态不支持查看详情，暂不支持显示直播预约和专栏动态）",-1,true,5);
            SharedPreferencesUtil.putBoolean("tutorial_dynamic",true);
        }

        refreshDynamic();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDynamic() {
        Log.e("debug","刷新");
        CenterThreadPool.run(()->{
            if (firstRefresh) {
                recyclerView.setLayoutManager(new LinearLayoutManager(DynamicActivity.this));
                dynamicList = new ArrayList<>();
                dynamicAdapter = new DynamicAdapter(this,dynamicList);
                recyclerView.setAdapter(dynamicAdapter);
            } else {
                offset = 0;
                bottom = false;
                dynamicList.clear();
                dynamicAdapter.notifyDataSetChanged();
            }
            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));

            refreshing = true;
            addRecommend();

        });
    }

    private void addRecommend() {
        Log.e("debug", "加载下一页");
        runOnUiThread(()->swipeRefreshLayout.setRefreshing(true));
        int lastSize = dynamicList.size();
        try {
            JSONObject jsonObject = DynamicApi.getSelfDynamic(offset);
            offset = DynamicApi.analyzeDynamicList(jsonObject, dynamicList);
            bottom = offset == -1;

            runOnUiThread(() -> {
                if (firstRefresh) {
                    firstRefresh = false;
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
                                CenterThreadPool.run(() -> addRecommend()); //加载第二页
                            }
                        }
                    });
                }
                dynamicAdapter.notifyItemRangeInserted(lastSize, dynamicList.size() - lastSize);
                swipeRefreshLayout.setRefreshing(false);
                refreshing = false;
            });

        } catch (Exception e) {
            runOnUiThread(()->{
                MsgUtil.err(e,this);
                swipeRefreshLayout.setRefreshing(false);
                refreshing = false;
            });
        }

    }

}