package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import org.json.JSONObject;

import java.util.ArrayList;

public class SearchLiveFragment extends Fragment implements SearchRefreshable {
    RecyclerView recyclerView;
    private ArrayList<LiveRoom> roomList;

    private LiveCardAdapter liveCardAdapter;

    private String keyword;
    private boolean isFirstLoad = true;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 0;

    public SearchLiveFragment(){}

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
        roomList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);

        CenterThreadPool.run(() -> {
            if(isAdded()) requireActivity().runOnUiThread(() -> {
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
                        if (lastItemPosition >= (itemCount - 3) && dy>0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                            refreshing = true;
                            CenterThreadPool.run(()->continueLoading()); //加载第二页
                        }

                        if(requireActivity() instanceof SearchActivity) {
                            SearchActivity activity = (SearchActivity) requireActivity();
                            activity.onScrolled(dy);
                        }
                    }
                });
            });
        });
    }

    private void continueLoading(){
        page++;
        Log.e("debug","加载下一页");
        int lastSize = roomList.size();
        try {
            JSONObject result = (JSONObject) SearchApi.searchType(keyword,page,"live");
            if(result!=null) {
                roomList.addAll(LiveApi.analyzeLiveRooms(result.getJSONArray("live_room")));
                CenterThreadPool.runOnUiThread(() -> liveCardAdapter.notifyItemRangeInserted(lastSize + 1, roomList.size()-lastSize));
            }
            else {
                bottom = true;
                if(isAdded() && !isFirstLoad) requireActivity().runOnUiThread(() ->  MsgUtil.toast("已经到底啦OwO",requireContext()));
            }
            isFirstLoad = false;
        } catch (Exception e){if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.err(e,requireContext()));}
        refreshing = false;
    }

    @Override
    public void refresh(String keyword){
        this.refreshing = true;
        this.page = 0;
        this.keyword = keyword;
        if(this.roomList==null)this.roomList = new ArrayList<>();
        if(this.liveCardAdapter==null)this.liveCardAdapter = new LiveCardAdapter(this.requireContext(),this.roomList);
        int size_old = this.roomList.size();
        this.roomList.clear();
        CenterThreadPool.runOnUiThread(()->{
            if(size_old!=0) this.liveCardAdapter.notifyItemRangeRemoved(0,size_old);
            CenterThreadPool.run(this::continueLoading);
        });
    }
}