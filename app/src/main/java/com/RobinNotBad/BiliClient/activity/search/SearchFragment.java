package com.RobinNotBad.BiliClient.activity.search;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.listener.OnLoadMoreListener;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomGridManager;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomLinearManager;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.view.ImageAutoLoadScrollListener;

public class SearchFragment extends Fragment {
    public SwipeRefreshLayout swipeRefreshLayout;
    public RecyclerView recyclerView;
    public TextView emptyView;
    public OnLoadMoreListener listener;
    public SwipeRefreshLayout.OnRefreshListener refreshListener;
    public String keyword;
    public boolean bottom = false;
    public int page = 1;
    public long lastLoadTimestamp;
    public boolean refreshable = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_refresh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emptyView = view.findViewById(R.id.emptyTip);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState != RecyclerView.SCROLL_STATE_DRAGGING) return;

                if(!recyclerView.canScrollVertically(-1)) {
                    if (requireActivity() instanceof SearchActivity) {
                        SearchActivity activity = (SearchActivity) requireActivity();  //不能向上滚动了就显示搜索栏
                        activity.onScrolled(-114);
                    }
                }
                else if (listener != null && !recyclerView.canScrollVertically(1) && !swipeRefreshLayout.isRefreshing() && !bottom) {
                    goOnLoad();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (listener != null) {
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    assert manager != null;
                    int lastItemPosition = manager.findLastVisibleItemPosition();  //获取最后一个显示的itemPosition
                    int itemCount = manager.getItemCount();
                    if (lastItemPosition >= (itemCount - 3) && dy > 0 && !swipeRefreshLayout.isRefreshing() && !bottom) {// 滑动到倒数第三个就可以刷新了
                        goOnLoad();
                    }

                    if (requireActivity() instanceof SearchActivity) {
                        SearchActivity activity = (SearchActivity) requireActivity();
                        activity.onScrolled(dy);
                    }
                }
            }
        });
        ImageAutoLoadScrollListener.install(recyclerView);
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        runOnUiThread(() -> recyclerView.setAdapter(adapter));
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        this.refreshListener = listener;
    }

    public void setRefreshing(boolean bool) {
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(bool));
    }

    public void setOnLoadMoreListener(OnLoadMoreListener loadMore) {
        listener = loadMore;
    }

    private void goOnLoad() {
        long timeCurrent = System.currentTimeMillis();
        if (timeCurrent - lastLoadTimestamp > 100) {
            swipeRefreshLayout.setRefreshing(true);
            page++;
            listener.onLoad(page);
            lastLoadTimestamp = timeCurrent;
        }
    }

    public void setBottom(boolean bool) {
        bottom = bool;
        if (page == 1) showEmptyView(bool);
        else if (bool && isAdded()) {
            MsgUtil.showMsg("已经到底啦OwO");
        }
    }

    public void runOnUiThread(Runnable runnable) {
        if (isAdded()) requireActivity().runOnUiThread(runnable);
    }

    public void showEmptyView(boolean empty) {
        if (emptyView != null) {
            runOnUiThread(() -> emptyView.setVisibility(empty ? View.VISIBLE : View.GONE));
        }
    }

    public boolean isRefreshing() {
        if (swipeRefreshLayout != null) return swipeRefreshLayout.isRefreshing();
        return false;
    }

    public void report(Throwable e) {
        MsgUtil.err(e);
    }

    public void loadFail() {
        page--;
        MsgUtil.showMsgLong("加载失败");
        setRefreshing(false);
    }

    public void loadFail(Throwable e) {
        page--;
        report(e);
        setRefreshing(false);
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return SharedPreferencesUtil.getBoolean("ui_landscape", false)
                ? new CustomGridManager(requireContext(), 3)
                : new CustomLinearManager(requireContext());
    }

    public void update(String keyword){
        this.page = 1;
        this.keyword = keyword;
        this.refreshable = true;
        setBottom(false);
    }

    public void refresh(){
        if(!refreshable) return;
        refreshable = false;
        setRefreshing(true);
        refreshListener.onRefresh();
    }

    protected void refreshInternal(){}
}
