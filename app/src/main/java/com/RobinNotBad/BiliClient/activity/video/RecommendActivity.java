package com.RobinNotBad.BiliClient.activity.video;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.RecommendApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//推荐页面
//2023-07-13

public class RecommendActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<VideoCard> videoCardList;
    private VideoCardAdapter videoCardAdapter;
    private boolean firstRefresh = true;
    private boolean refreshing = false;
    public static RecommendActivity instance = null;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_main_refresh);
        Log.e("debug","进入推荐页");
        instance = this;    //给菜单页面调用，用来结束本页面

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshRecommend);
        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(RecommendActivity.this, MenuActivity.class);
            intent.putExtra("from",0);
            startActivity(intent);
        });

        TextView title = findViewById(R.id.pageName);
        title.setText("推荐");

        refreshRecommend();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshRecommend() {
        Log.e("debug", "刷新");
        if (firstRefresh) {
            recyclerView.setLayoutManager(new LinearLayoutManager(RecommendActivity.this));
            videoCardList = new ArrayList<>();
            videoCardAdapter = new VideoCardAdapter(this, videoCardList);
            recyclerView.setAdapter(videoCardAdapter);
        } else {
            int last = videoCardList.size();
            videoCardList.clear();
            videoCardAdapter.notifyItemRangeRemoved(0,last);
        }
        swipeRefreshLayout.setRefreshing(true);

        refreshing = true;
        CenterThreadPool.run(this::addRecommend);

    }

    private void addRecommend() {
        Log.e("debug", "加载下一页");
        runOnUiThread(()->swipeRefreshLayout.setRefreshing(true));
        int lastSize = videoCardList.size();
        try {
            RecommendApi.getRecommend(videoCardList);
        } catch (IOException e){
            runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,this));
            e.printStackTrace();
        } catch (JSONException e) {
            runOnUiThread(()-> MsgUtil.jsonErr(e, this));
            e.printStackTrace();
        }

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
                        if (lastItemPosition >= (itemCount - 3) && dy>0 && !refreshing) {// 滑动到倒数第三个就可以刷新了
                            refreshing = true;
                            CenterThreadPool.run(()->addRecommend()); //加载第二页
                        }
                    }
                });
            }
            Log.e("debug","last="+lastSize+"&now="+videoCardList.size());
            videoCardAdapter.notifyItemRangeInserted(lastSize,videoCardList.size()-lastSize);
            swipeRefreshLayout.setRefreshing(false);
            refreshing = false;
        });
    }

}