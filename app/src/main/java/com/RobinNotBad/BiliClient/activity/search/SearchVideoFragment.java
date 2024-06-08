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
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;

import java.util.ArrayList;

public class SearchVideoFragment extends Fragment implements SearchRefreshable {
    RecyclerView recyclerView;
    private ArrayList<VideoCard> videoCardList;

    private VideoCardAdapter videoCardAdapter;

    private String keyword;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 0;

    public SearchVideoFragment(){}

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

    private void continueLoading(){
        page++;
        Log.e("debug","加载下一页");
        int lastSize = videoCardList.size();
        try {
            JSONArray result =  SearchApi.search(keyword,page);
            if(result!=null) {
                SearchApi.getVideosFromSearchResult(result, videoCardList,page==1);
                CenterThreadPool.runOnUiThread(() -> videoCardAdapter.notifyItemRangeInserted(lastSize + 1,videoCardList.size()-lastSize));
            }
            else {
                bottom = true;
                if(isAdded()) requireActivity().runOnUiThread(() ->  MsgUtil.toast("已经到底啦OwO",requireContext()));
            }
        } catch (Exception e){if(isAdded()) requireActivity().runOnUiThread(()-> MsgUtil.err(e,requireContext()));}
        refreshing = false;
    }

    @Override
    public void refresh(String keyword){
        this.refreshing = true;
        this.page = 0;
        this.keyword = keyword;
        if(this.videoCardList==null) this.videoCardList = new ArrayList<>();
        if(this.videoCardAdapter==null) this.videoCardAdapter = new VideoCardAdapter(this.requireContext(),this.videoCardList);
        int size_old = this.videoCardList.size();
        this.videoCardList.clear();
        CenterThreadPool.runOnUiThread(()->{
            if(size_old!=0) this.videoCardAdapter.notifyItemRangeRemoved(0,size_old);
            CenterThreadPool.run(this::continueLoading);
        });
    }
}
