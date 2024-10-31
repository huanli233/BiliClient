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
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchVideoFragment extends Fragment implements SearchRefreshable {
    RecyclerView recyclerView;
    private ArrayList<VideoCard> videoCardList = new ArrayList<>();

    private VideoCardAdapter videoCardAdapter;

    private String keyword;
    private boolean refreshing = false;
    private boolean isFirstLoad = true;
    private boolean bottom = false;
    private int page = 0;
    private TextView emptyView;
    private ImageView loadingView;

    public SearchVideoFragment() {
    }

    public static SearchVideoFragment newInstance() {
        return new SearchVideoFragment();
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
        videoCardList = new ArrayList<>();
        videoCardAdapter = new VideoCardAdapter(requireContext(), videoCardList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
                if (lastItemPosition >= (itemCount - 3) && dy > 0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                    refreshing = true;
                    CenterThreadPool.run(SearchVideoFragment.this::continueLoading); //加载第二页
                }

                if (requireActivity() instanceof SearchActivity) {
                    SearchActivity activity = (SearchActivity) requireActivity();
                    activity.onScrolled(dy);
                }
            }
        });
    }

    private void continueLoading() {
        page++;
        Log.e("debug", "加载下一页");
        try {
            JSONArray result = SearchApi.search(keyword, page);
            if (result != null) {
                ArrayList<VideoCard> list = new ArrayList<>();
                SearchApi.getVideosFromSearchResult(result, list, page == 1);
                CenterThreadPool.runOnUiThread(() -> {
                    int lastSize = videoCardList.size();
                    videoCardList.addAll(list);
                    videoCardAdapter.notifyItemRangeInserted(lastSize + 1, videoCardList.size() - lastSize);
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
    }

    @Override
    public void refresh(String keyword) {
        this.isFirstLoad = true;
        this.refreshing = true;
        this.page = 0;
        this.keyword = keyword;
        CenterThreadPool.runOnUiThread(() -> {
            loadingView.setVisibility(View.VISIBLE);
            if (this.videoCardAdapter == null)
                this.videoCardAdapter = new VideoCardAdapter(this.requireContext(), this.videoCardList);
            int size_old = this.videoCardList.size();
            this.videoCardList.clear();
            if (size_old != 0) this.videoCardAdapter.notifyItemRangeRemoved(0, size_old);
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
