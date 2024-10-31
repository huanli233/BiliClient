package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.LiveCardAdapter;
import com.RobinNotBad.BiliClient.api.LiveApi;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchLiveFragment extends Fragment implements SearchRefreshable {
    RecyclerView recyclerView;
    private ArrayList<LiveRoom> roomList = new ArrayList<>();

    private LiveCardAdapter liveCardAdapter;

    private String keyword;
    private boolean isFirstLoad = true;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 0;
    private TextView emptyView;
    private ImageView loadingView;

    public SearchLiveFragment() {
    }

    public static SearchLiveFragment newInstance() {
        return new SearchLiveFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyTip);
        loadingView = view.findViewById(R.id.loading);
        roomList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);

        CenterThreadPool.run(() -> {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                liveCardAdapter = new LiveCardAdapter(requireContext(), roomList);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(liveCardAdapter);

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

                        if (requireActivity() instanceof SearchActivity) {
                            SearchActivity activity = (SearchActivity) requireActivity();
                            activity.onScrolled(dy);
                        }
                    }
                });
            });
        });
    }

    private void continueLoading() {
        page++;
        Log.e("debug", "加载下一页");
        try {
            JSONObject result = (JSONObject) SearchApi.searchType(keyword, page, "live");
            if (result != null) {
                JSONArray jsonArray = result.optJSONArray("live_room");
                List<LiveRoom> list = new ArrayList<>();
                if (jsonArray != null) list.addAll(LiveApi.analyzeLiveRooms(jsonArray));
                CenterThreadPool.runOnUiThread(() -> {
                    int lastSize = roomList.size();
                    roomList.addAll(list);
                    liveCardAdapter.notifyItemRangeInserted(lastSize + 1, roomList.size() - lastSize);
                    loadingView.setVisibility(View.GONE);
                });
            } else {
                bottom = true;
                if (isFirstLoad) showEmptyView();
                else if (isAdded()) {
                    requireActivity().runOnUiThread(() -> MsgUtil.showMsg("已经到底啦OwO", requireContext()));
                }
            }
            isFirstLoad = false;
        } catch (Exception e) {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                MsgUtil.err(e, requireContext());
                loadingView.setVisibility(View.GONE);
            });
        }
        refreshing = false;
        if (bottom && roomList.isEmpty()) {
            showEmptyView();
        }
    }

    @Override
    public void refresh(String keyword) {
        this.isFirstLoad = true;
        this.refreshing = true;
        this.page = 0;
        this.keyword = keyword;
        CenterThreadPool.runOnUiThread(() -> {
            loadingView.setVisibility(View.VISIBLE);
            if (this.liveCardAdapter == null)
                this.liveCardAdapter = new LiveCardAdapter(this.requireContext(), this.roomList);
            int size_old = this.roomList.size();
            this.roomList.clear();
            if (size_old != 0) this.liveCardAdapter.notifyItemRangeRemoved(0, size_old);
            CenterThreadPool.run(this::continueLoading);
        });
    }

    public void showEmptyView() {
        if (emptyView != null && isAdded()) {
            requireActivity().runOnUiThread(() -> {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            });
        }
    }
}
