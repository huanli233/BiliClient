package com.RobinNotBad.BiliClient.activity.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.listener.OnLoadMoreListener;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.view.ImageAutoLoadScrollListener;

/*
跟RefreshListActivity基本相同
2024-05-02
 */

public class RefreshListFragment extends Fragment {
    public SwipeRefreshLayout swipeRefreshLayout;
    public RecyclerView recyclerView;
    public TextView emptyView;
    public OnLoadMoreListener listener;
    public boolean bottom = false;
    public int page = 1;
    public long lastLoadTimestamp;
    public boolean force_single_column = false;

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
        swipeRefreshLayout.setRefreshing(true);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (listener != null && !recyclerView.canScrollVertically(1) && !swipeRefreshLayout.isRefreshing() && newState == RecyclerView.SCROLL_STATE_DRAGGING && !bottom) {
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
                }
            }
        });
        ImageAutoLoadScrollListener.install(recyclerView);
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        runOnUiThread(() -> recyclerView.setAdapter(adapter));
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        swipeRefreshLayout.setOnRefreshListener(listener);
        swipeRefreshLayout.setEnabled(true);
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
    }

    public void runOnUiThread(Runnable runnable) {
        if (isAdded()) requireActivity().runOnUiThread(runnable);
    }

    public void showEmptyView() {
        if (emptyView != null) {
            runOnUiThread(() -> {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            });
        }
    }

    public boolean isRefreshing() {
        if (swipeRefreshLayout != null) return swipeRefreshLayout.isRefreshing();
        return false;
    }

    public void report(Throwable e) {
        runOnUiThread(() -> MsgUtil.err(e, requireContext()));
    }

    public void loadFail() {
        page--;
        runOnUiThread(() -> MsgUtil.showMsgLong("加载失败", requireContext()));
        setRefreshing(false);
    }

    public void loadFail(Throwable e) {
        page--;
        report(e);
        setRefreshing(false);
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return SharedPreferencesUtil.getBoolean("ui_landscape", false) && !force_single_column
                ? new GridLayoutManager(requireContext(), 3)
                : new LinearLayoutManager(requireContext());
    }

    public void setForceSingleColumn() {
        force_single_column = true;
    }
}
