package com.RobinNotBad.BiliClient.activity.reply;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.reply.ReplyAdapter;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;

//视频下评论页面，评论详情见ReplyInfoActivity
//部分通用代码在VideoReplyAdapter内
//2023-07-22

public class ReplyFragment extends RefreshListFragment {

    private boolean dontload;
    protected long aid;
    protected int sort = 2;
    protected int type;
    protected ArrayList<Reply> replyList;
    protected ReplyAdapter replyAdapter;
    public int replyType = ReplyApi.REPLY_TYPE_VIDEO;

    public static ReplyFragment newInstance(long aid, int type) {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putLong("aid", aid);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReplyFragment newInstance(long aid, int type, boolean dontload) {
        ReplyFragment fragment = new ReplyFragment();
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
            replyType = type;
            dontload = getArguments().getBoolean("dontload",false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setOnRefreshListener(() -> refresh(aid));
        setOnLoadMoreListener(this::continueLoading);

        Log.e("debug-av号",String.valueOf(aid));

        replyList = new ArrayList<>();
        if(!dontload) {
            CenterThreadPool.run(()->{
                try {
                    int result = ReplyApi.getReplies(aid,0,page,type,sort,replyList);
                    setRefreshing(false);
                    if(result != -1 && isAdded()) {
                        replyAdapter = getReplyAdapter();
                        setOnSortSwitch();
                        setAdapter(replyAdapter);

                        if (result == 1) {
                            Log.e("debug", "到底了");
                            setBottom(true);
                        }
                    }
                } catch (Exception e) {
                    setRefreshing(false);
                }
            });
        }
    }

    private ReplyAdapter getReplyAdapter() {
        return new ReplyAdapter(requireContext(), replyList, aid, 0, type, sort, replyType);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                int lastSize = replyList.size();
                int result = ReplyApi.getReplies(aid,0,page,type,sort,replyList);
                setRefreshing(false);
                if(result != -1){
                    Log.e("debug","下一页");
                    runOnUiThread(()->
                            replyAdapter.notifyItemRangeInserted(lastSize + 1, replyList.size() + 1 - lastSize));
                    if(result == 1) {
                        Log.e("debug", "到底了");
                        bottom = true;
                    }
                }
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    public void notifyReplyInserted(Reply reply) {
        if (reply.root != 0) return;
        replyList.add(0, reply);
        runOnUiThread(() -> {
            replyAdapter.notifyItemInserted(0);
            replyAdapter.notifyItemRangeChanged(0, replyList.size());
        });
    }

    public void refresh(long aid){
        page = 1;
        this.aid = aid;
        setRefreshing(true);
        if(replyList!=null) replyList.clear();
        else replyList = new ArrayList<>();
        CenterThreadPool.run(()->{
            try {
                int result = ReplyApi.getReplies(aid,0,page,type,sort,replyList);
                setRefreshing(false);
                if(result != -1 && isAdded()) {
                    replyAdapter = getReplyAdapter();
                    setOnSortSwitch();
                    setAdapter(replyAdapter);
                    //replyAdapter.notifyItemRangeInserted(0,replyList.size());
                    if(result == 1) {
                        Log.e("debug","到底了");
                        bottom = true;
                    }
                    else bottom = false;
                }
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    private void setOnSortSwitch(){
        replyAdapter.setOnSortSwitchListener(position -> {
            sort = (sort == 0 ? 2 : 0);
            refresh(aid);
        });
    }
}