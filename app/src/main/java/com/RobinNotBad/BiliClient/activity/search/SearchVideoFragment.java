package com.RobinNotBad.BiliClient.activity.search;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchVideoFragment extends SearchFragment {
    private ArrayList<VideoCard> videoCardList = new ArrayList<>();
    private VideoCardAdapter videoCardAdapter;

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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoCardList = new ArrayList<>();
        videoCardAdapter = new VideoCardAdapter(requireContext(), videoCardList);
        setAdapter(videoCardAdapter);

        setOnRefreshListener(this::refreshInternal);
        setOnLoadMoreListener(this::continueLoading);
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            Log.e("debug", "加载下一页");
            try {
                JSONArray result = SearchApi.search(keyword, page);
                if (result != null) {
                    if(page==1) showEmptyView(false);
                    ArrayList<VideoCard> list = new ArrayList<>();
                    SearchApi.getVideosFromSearchResult(result, list, page == 1);
                    Log.d("debug-size", String.valueOf(list.size()));
                    CenterThreadPool.runOnUiThread(() -> {
                        int lastSize = videoCardList.size();
                        videoCardList.addAll(list);
                        videoCardAdapter.notifyItemRangeInserted(lastSize + 1, videoCardList.size() - lastSize);
                    });
                } else {
                    bottom = true;
                    if (page==1) showEmptyView(true);
                    else if (isAdded()) {
                        MsgUtil.showMsg("已经到底啦OwO");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                loadFail(e);
            }
            setRefreshing(false);
        });
    }

    public void refreshInternal() {
        CenterThreadPool.runOnUiThread(() -> {
            page = 1;
            if (this.videoCardAdapter == null)
                this.videoCardAdapter = new VideoCardAdapter(this.requireContext(), this.videoCardList);
            int size_old = this.videoCardList.size();
            this.videoCardList.clear();
            if (size_old != 0) this.videoCardAdapter.notifyItemRangeRemoved(0, size_old);
            CenterThreadPool.run(() -> continueLoading(page));
        });
    }
}
