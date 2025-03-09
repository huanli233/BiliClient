package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.adapter.LiveCardAdapter;
import com.RobinNotBad.BiliClient.api.LiveApi;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchLiveFragment extends SearchFragment {

    private ArrayList<LiveRoom> roomList = new ArrayList<>();
    private LiveCardAdapter liveCardAdapter;

    public SearchLiveFragment() {
    }

    public static SearchLiveFragment newInstance() {
        return new SearchLiveFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        roomList = new ArrayList<>();
        liveCardAdapter = new LiveCardAdapter(requireContext(), roomList);
        setAdapter(liveCardAdapter);

        setOnRefreshListener(this::refreshInternal);
        setOnLoadMoreListener(this::continueLoading);
    }

    private void continueLoading(int page) {
        CenterThreadPool.run(()-> {
            Log.e("debug", "加载下一页");
            try {
                Object result = SearchApi.searchType(keyword, page, "live");
                if (result != null) {
                    if (page == 1) showEmptyView(false);
                    JSONArray jsonArray = null;
                    if(result instanceof JSONObject) jsonArray = ((JSONObject)result).optJSONArray("live_room");
                    else if(result instanceof JSONArray) jsonArray = (JSONArray) result;

                    List<LiveRoom> list = new ArrayList<>();
                    if (jsonArray != null) list.addAll(LiveApi.analyzeLiveRooms(jsonArray));
                    if(list.size()==0) setBottom(true);
                    else CenterThreadPool.runOnUiThread(() -> {
                        int lastSize = roomList.size();
                        roomList.addAll(list);
                        liveCardAdapter.notifyItemRangeInserted(lastSize + 1, roomList.size() - lastSize);
                    });
                }
                else setBottom(true);
            } catch (Exception e) {
                report(e);
            }
            setRefreshing(false);
            if (bottom && roomList.isEmpty()) {
                showEmptyView(true);
            }
        });
    }

    public void refreshInternal() {
        CenterThreadPool.runOnUiThread(() -> {
            page = 1;
            if (this.liveCardAdapter == null)
                this.liveCardAdapter = new LiveCardAdapter(this.requireContext(), this.roomList);
            int size_old = this.roomList.size();
            this.roomList.clear();
            if (size_old != 0) this.liveCardAdapter.notifyItemRangeRemoved(0, size_old);
            CenterThreadPool.run(()->continueLoading(page));
        });
    }

}
