package com.RobinNotBad.BiliClient.activity.reply;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.ReplyAdapter;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.event.ReplyEvent;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private Object source;
    private long seek;

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

    public static ReplyFragment newInstance(long aid, int type, long seek_rpid) {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putLong("aid", aid);
        args.putInt("type", type);
        args.putLong("seek", seek_rpid);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReplyFragment newInstance(long aid, int type, boolean dontload, long seek_rpid) {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putLong("aid", aid);
        args.putInt("type", type);
        args.putBoolean("dontload",dontload);
        args.putLong("seek", seek_rpid);
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
            seek = getArguments().getLong("seek", -1);
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
                    int result = seek == -1 ? ReplyApi.getReplies(aid,0,page,type,sort,replyList) : ReplyApi.getRepliesLazy(aid,seek,page,type,3,replyList);
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

    public void setSource(Object source) {
        this.source = source;
        if (replyAdapter != null) replyAdapter.source = source;
    }

    private ReplyAdapter getReplyAdapter() {
        return new ReplyAdapter(requireContext(), replyList, aid, 0, type, sort, source);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        CenterThreadPool.run(()->{
            try {
                List<Reply> list = new ArrayList<>();
                int result = ReplyApi.getReplies(aid,0,page,type,sort,list);
                setRefreshing(false);
                if(result != -1){
                    Log.e("debug","下一页");
                    runOnUiThread(()-> {
                        replyList.addAll(list);
                        if (replyAdapter != null ) replyAdapter.notifyItemRangeInserted(replyList.size() - list.size() + 1, list.size());
                    });
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

    public void notifyReplyInserted(ReplyEvent replyEvent) {
        Reply reply = replyEvent.getMessage();
        if (reply.root == 0) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager());
            int pos = layoutManager.findFirstCompletelyVisibleItemPosition();
            pos = Math.max(pos, 0);
            replyList.add(pos, reply);
            int finalPos = pos;
            runOnUiThread(() -> {
                replyAdapter.notifyItemInserted(finalPos);
                replyAdapter.notifyItemRangeChanged(finalPos, replyList.size() - finalPos);
                layoutManager.scrollToPositionWithOffset(finalPos + 1, 0);
            });
        } else if (replyEvent.getPos() >= 0) {
            replyList.get(replyEvent.getPos()).childMsgList.add(String.format("%s：%s", reply.sender.name, reply.message));
            replyList.get(replyEvent.getPos()).childCount++;
            runOnUiThread(() -> replyAdapter.notifyItemChanged(replyEvent.getPos() + 1));
        }
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