package com.RobinNotBad.BiliClient.activity.video.info;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.ReplyAdapter;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

//视频下评论页面，评论详情见ReplyInfoActivity
//部分通用代码在VideoReplyAdapter内
//2023-07-22

public class VideoReplyFragment extends Fragment {

    private boolean dontload;
    private long aid;
    private int sort = 2;
    private int type;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<Reply> replyList;
    private ReplyAdapter replyAdapter;
    private boolean refreshing = false;
    private boolean bottom = false;
    private int page = 1;

    public VideoReplyFragment() {

    }

    public static VideoReplyFragment newInstance(long aid, int type) {
        VideoReplyFragment fragment = new VideoReplyFragment();
        Bundle args = new Bundle();
        args.putLong("aid", aid);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    public static VideoReplyFragment newInstance(long aid, int type,boolean dontload) {
        VideoReplyFragment fragment = new VideoReplyFragment();
        Bundle args = new Bundle();
        args.putLong("aid", aid);
        args.putInt("type", type);
        args.putBoolean("dontload",dontload);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            aid = getArguments().getLong("aid");
            type = getArguments().getInt("type");
            dontload = getArguments().getBoolean("dontload",false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_refresh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        refreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        refreshLayout.setOnRefreshListener(() -> refresh(aid));

        Log.e("debug-av号",String.valueOf(aid));

        replyList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
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
                //Log.e("debug","last="+lastItemPosition+"&itemcount="+itemCount);
                if (lastItemPosition >= (itemCount - 3) && dy > 0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                    refreshing = true;
                    CenterThreadPool.run(() -> continueLoading()); //加载第二页
                }
            }
        });
        if(!dontload) {
            refreshLayout.setRefreshing(true);
            CenterThreadPool.run(()->{
                try {
                    int result = ReplyApi.getReplies(aid,0,page,type,sort,replyList);
                    if(result != -1) {
                        if(isAdded()) requireActivity().runOnUiThread(()-> {
                            replyAdapter = new ReplyAdapter(requireContext(), replyList,aid,0,type,sort);
                            setOnSortSwitch();
                            recyclerView.setAdapter(replyAdapter);
                            refreshLayout.setRefreshing(false);
                        });
                        if(result == 1) {
                            Log.e("debug","到底了");
                            bottom = true;
                        }
                    }
                } catch (Exception e) {requireActivity().runOnUiThread(()-> {
                    MsgUtil.err(e,getContext());
                    refreshLayout.setRefreshing(false);
                });}
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading() {
        page++;
        if(isAdded())requireActivity().runOnUiThread(()->refreshLayout.setRefreshing(true));
        try {
            int lastSize = replyList.size();
            int result = ReplyApi.getReplies(aid,0,page,type,sort,replyList);
            if(result != -1){
                Log.e("debug","下一页");
                if(isAdded()) requireActivity().runOnUiThread(()-> {
                    replyAdapter.notifyItemRangeInserted(lastSize + 1, replyList.size() + 1 - lastSize);
                    refreshLayout.setRefreshing(false);
                });
                if(result == 1) {
                    //requireActivity().runOnUiThread(()-> MsgUtil.toast("到底啦QwQ",requireContext()));
                    Log.e("debug", "到底了");
                    bottom = true;
                }
            }
            refreshing = false;
        } catch (Exception e) {if(isAdded()) requireActivity().runOnUiThread(()-> {
            MsgUtil.err(e,getContext());
            refreshLayout.setRefreshing(false);
        });}
    }

    public void refresh(long aid){
        page = 1;
        this.aid = aid;
        if(isAdded())requireActivity().runOnUiThread(()->refreshLayout.setRefreshing(true));
        if(replyList!=null) replyList.clear();
        else replyList = new ArrayList<>();
        CenterThreadPool.run(()->{
            try {
                int result = ReplyApi.getReplies(aid,0,page,type,sort,replyList);
                if(result != -1) {
                    if(isAdded()) requireActivity().runOnUiThread(()->{
                        replyAdapter = new ReplyAdapter(requireContext(),replyList,aid,0,type,sort);
                        setOnSortSwitch();
                        recyclerView.setAdapter(replyAdapter);
                        refreshLayout.setRefreshing(false);
                        //replyAdapter.notifyItemRangeInserted(0,replyList.size());
                    });
                    if(result == 1) {
                        Log.e("debug","到底了");
                        bottom = true;
                    }
                    else bottom=false;
                }
            } catch (Exception e) {if(isAdded()) requireActivity().runOnUiThread(()-> {
                MsgUtil.err(e,getContext());
                refreshLayout.setRefreshing(false);
            });}
        });
    }

    private void setOnSortSwitch(){
        replyAdapter.setOnSortSwitchListener(position -> {
            sort = (sort == 0 ? 2 : 0);
            refresh(aid);
        });
    }
}