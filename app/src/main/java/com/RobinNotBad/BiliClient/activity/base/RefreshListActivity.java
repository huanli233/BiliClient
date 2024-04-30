package com.RobinNotBad.BiliClient.activity.base;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.listener.OnLoadMoreListener;


/*
尝试造轮子以减少代码量
2024-05-01
 */

public class RefreshListActivity extends BaseActivity{
    public SwipeRefreshLayout swipeRefreshLayout;
    public RecyclerView recyclerView;
    public OnLoadMoreListener listener;
    public boolean bottom = false;
    public int page = 1;
    public long lastLoadTimestamp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_refresh);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setEnabled(false);
        recyclerView = findViewById(R.id.recyclerView);
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter){
        runOnUiThread(()-> {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (listener != null && !recyclerView.canScrollVertically(1) && !swipeRefreshLayout.isRefreshing() && newState==RecyclerView.SCROLL_STATE_DRAGGING) {
                        goOnLoad();
                    }
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (listener != null) {
                        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                        assert manager != null;
                        int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();  //获取最后一个完全显示的itemPosition
                        int itemCount = manager.getItemCount();
                        if (lastItemPosition >= (itemCount - 3) && dy > 0 && !swipeRefreshLayout.isRefreshing() && !bottom) {// 滑动到倒数第三个就可以刷新了
                            goOnLoad();
                        }
                    }
                }
            });
        });
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener){
        swipeRefreshLayout.setOnRefreshListener(listener);
        swipeRefreshLayout.setEnabled(true);
    }

    public void setRefreshing(boolean bool){runOnUiThread(()->swipeRefreshLayout.setRefreshing(bool));}

    public void setLoadMoreListener(OnLoadMoreListener loadMore){listener = loadMore;}

    private void goOnLoad(){
        long timeCurrent = System.currentTimeMillis();
        if(timeCurrent - lastLoadTimestamp > 100) {
            swipeRefreshLayout.setRefreshing(true);
            page++;
            listener.onLoad(page);
            lastLoadTimestamp = timeCurrent;
        }
    }

    public void setBottom(boolean bool){bottom = bool;}
}
