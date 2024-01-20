package com.RobinNotBad.BiliClient.activity.search;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.SearchApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class SearchVideoFragment extends Fragment {
    RecyclerView recyclerView;
    private ArrayList<VideoCard> videoCardList;

    private VideoCardAdapter videoCardAdapter;

    private String keyword;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 0;

    public SearchVideoFragment(){}

    public static SearchVideoFragment newInstance(ArrayList<VideoCard> cardList, String keyword) {
        SearchVideoFragment fragment = new SearchVideoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("list",cardList);
        bundle.putString("keyword",keyword);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            videoCardList = (ArrayList<VideoCard>) getArguments().getSerializable("list");
            keyword = getArguments().getString("keyword");
        }
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
        CenterThreadPool.run(() -> {
            if(isAdded()) requireActivity().runOnUiThread(() -> {
                videoCardAdapter = new VideoCardAdapter(requireContext(), videoCardList);
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
                        if (lastItemPosition >= (itemCount - 3) && dy>0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                            refreshing = true;
                            CenterThreadPool.run(() -> continueLoading(requireContext())); //加载第二页
                        }
                    }
                });
            });
        });
    }

    private void continueLoading(Context context){
        refreshing = true;
        page++;
        Log.e("debug","加载下一页");
        int lastSize = videoCardList.size();
        try {
            JSONArray result =  SearchApi.search(keyword,page);
            if(result!=null) {
                SearchApi.getVideosFromSearchResult(result, videoCardList);
                CenterThreadPool.runOnMainThread(() -> videoCardAdapter.notifyItemRangeInserted(lastSize + 1,videoCardList.size()-lastSize));
            }
            else {
                bottom = true;
                MsgUtil.toast("已经到底啦OwO",context);
            }
        } catch (IOException e){
            CenterThreadPool.runOnMainThread(()-> MsgUtil.quickErr(MsgUtil.err_net,context));
            e.printStackTrace();
        } catch (JSONException e) {
            CenterThreadPool.runOnMainThread(()-> MsgUtil.jsonErr(e,context));
            e.printStackTrace();
        }
        refreshing = false;
    }


    public void refresh(ArrayList<VideoCard> newList, String keyword){
        this.keyword = keyword;
        int size_old = this.videoCardList.size();
        this.videoCardList = newList;
        CenterThreadPool.runOnMainThread(()-> {
            videoCardAdapter.notifyItemRangeRemoved(0,size_old);
            videoCardAdapter.notifyItemRangeInserted(0,videoCardList.size());
            Log.e("debug","size=" + this.videoCardList.size() + "&last="+size_old);

            refreshing = false;
            bottom = false;
            page = 0;
        });
    }
}
