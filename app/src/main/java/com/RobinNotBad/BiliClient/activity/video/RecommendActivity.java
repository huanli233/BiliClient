package com.RobinNotBad.BiliClient.activity.video;

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
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.RecommendApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

//推荐页面
//2023-07-13

public class RecommendActivity extends InstanceActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<VideoCard> videoCardList;
    private VideoCardAdapter videoCardAdapter;
    private boolean firstRefresh = true;
    private boolean refreshing = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_main_refresh);
        setMenuClick(0);
        Log.e("debug","进入推荐页");

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshRecommend);


        TextView title = findViewById(R.id.pageName);
        title.setText("推荐");

        if(!SharedPreferencesUtil.getBoolean("tutorial_recommend",false)){
            MsgUtil.showTutorial(this,"使用教程","点击上方标题栏可以打开菜单\n\n*我知道你可能不喜欢强制看教程，但这是必要的，敬请谅解QwQ",R.mipmap.tutorial_recommend);
            SharedPreferencesUtil.putBoolean("tutorial_recommend",true);
        }

        refreshRecommend();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshRecommend() {
        Log.e("debug", "刷新");
        if (firstRefresh) {
            recyclerView.setLayoutManager(new LinearLayoutManager(RecommendActivity.this));
            videoCardList = new ArrayList<>();
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

            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
                refreshing = false;

                if (firstRefresh) {
                    firstRefresh = false;
                    videoCardAdapter = new VideoCardAdapter(this, videoCardList);
                    recyclerView.setAdapter(videoCardAdapter);

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
                }else {
                    Log.e("debug","last="+lastSize+"&now="+videoCardList.size());
                    videoCardAdapter.notifyItemRangeInserted(lastSize,videoCardList.size()-lastSize);
                }
            });
        } catch (Exception e) {
            runOnUiThread(()-> {
                MsgUtil.err(e, this);
                swipeRefreshLayout.setRefreshing(false);
                refreshing = false;
            });
        }


    }
}