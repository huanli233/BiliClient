package com.RobinNotBad.BiliClient.activity.reply;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.RobinNotBad.BiliClient.activity.base.RefreshListFragment;
import com.RobinNotBad.BiliClient.adapter.ReplyAdapter;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.event.ReplyEvent;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//视频下评论页面，评论详情见ReplyInfoActivity
//部分通用代码在VideoReplyAdapter内
//2023-07-22

public class ReplyFragment extends RefreshListFragment {

    private boolean dontload;
    protected long aid, mid;
    protected int sort = 3;
    protected int type;
    protected ArrayList<Reply> replyList;
    protected ReplyAdapter replyAdapter;
    public int replyType = ReplyApi.REPLY_TYPE_VIDEO;
    private Object source;
    private long seek;
    private String pagination = "";

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
        args.putBoolean("dontload", dontload);
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
        args.putBoolean("dontload", dontload);
        args.putLong("seek", seek_rpid);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReplyFragment newInstance(long aid, int type, long seek_rpid, long up_mid) {
        ReplyFragment fragment = new ReplyFragment();
        Bundle args = new Bundle();
        args.putLong("aid", aid);
        args.putInt("type", type);
        args.putLong("seek", seek_rpid);
        args.putLong("mid", up_mid);
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
            dontload = getArguments().getBoolean("dontload", false);
            seek = getArguments().getLong("seek", -1);
            mid = getArguments().getLong("mid", -1);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        setForceSingleColumn();
        super.onViewCreated(view, savedInstanceState);

        if(SharedPreferencesUtil.getBoolean("ui_landscape",false)) {
            WindowManager windowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            if(Build.VERSION.SDK_INT >= 17) display.getRealMetrics(metrics);
            else display.getMetrics(metrics);
            int paddings = metrics.widthPixels / 6;
            recyclerView.setPadding(paddings,0,paddings,0);
        }

        setOnRefreshListener(() -> refresh(aid));
        setOnLoadMoreListener(this::continueLoading);

        Log.e("debug-av号", String.valueOf(aid));

        replyList = new ArrayList<>();
        if (!dontload) {
            CenterThreadPool.run(() -> {
                try {
                    Pair<Integer, String> pageState = ReplyApi.getRepliesLazy(aid, seek, pagination, type, sort, replyList);
                    int result = pageState.first;
                    this.pagination = pageState.second;
                    setRefreshing(false);
                    if (result != -1 && isAdded()) {
                        replyAdapter = createReplyAdapter();
                        replyAdapter.source = source;
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
        if (replyAdapter != null) {
            replyAdapter.source = source;
        }
    }

    private ReplyAdapter createReplyAdapter() {
        return new ReplyAdapter(requireContext(), replyList, aid, 0, type, sort, source, mid);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                List<Reply> list = new ArrayList<>();
                Pair<Integer, String> pageState = ReplyApi.getRepliesLazy(aid, 0, pagination, type, sort, list);
                int result = pageState.first;
                this.pagination = pageState.second;
                setRefreshing(false);
                if (result != -1) {
                    Log.e("debug", "下一页");
                    runOnUiThread(() -> {
                        replyList.addAll(list);
                        if (replyAdapter != null)
                            replyAdapter.notifyItemRangeInserted(replyList.size() - list.size() + 1, list.size());
                    });
                    if (result == 1) {
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
        if (replyEvent.getOid() != aid) return;
        Reply reply = replyEvent.getMessage();
        if (reply.root == 0) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager());
            int pos = layoutManager.findFirstCompletelyVisibleItemPosition();
            pos = Math.max(pos, 0);
            replyList.add(pos, reply);
            int finalPos = pos;
            runOnUiThread(() -> {
                replyAdapter.notifyItemInserted(finalPos);
                replyAdapter.notifyItemRangeChanged(finalPos, replyList.size() - finalPos + 1);
                layoutManager.scrollToPositionWithOffset(finalPos + 1, 0);
            });
        } else if (replyEvent.getPos() >= 0) {
            replyList.get(replyEvent.getPos()).childMsgList.add(reply);
            replyList.get(replyEvent.getPos()).childCount++;
            runOnUiThread(() -> replyAdapter.notifyItemChanged(replyEvent.getPos() + 1));
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh(long aid) {
        pagination = "";
        this.aid = aid;
        setRefreshing(true);
        CenterThreadPool.run(() -> {
            try {
                List<Reply> list = new ArrayList<>();
                Pair<Integer, String> pageState = ReplyApi.getRepliesLazy(aid, 0, pagination, type, sort, list);
                int result = pageState.first;
                this.pagination = pageState.second;
                setRefreshing(false);
                if (result != -1 && isAdded()) {
                    runOnUiThread(() -> {
                        if (!isAdded()) return;
                        if (replyList != null) replyList.clear();
                        else replyList = new ArrayList<>();
                        replyList.addAll(list);
                        if (replyAdapter == null) {
                            replyAdapter = createReplyAdapter();
                            setAdapter(replyAdapter);
                        } else {
                            replyAdapter.notifyDataSetChanged();
                        }
                    });
                    //replyAdapter.notifyItemRangeInserted(0,replyList.size());
                    if (result == 1) {
                        Log.e("debug", "到底了");
                        bottom = true;
                    } else bottom = false;
                }
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    private void setOnSortSwitch() {
        replyAdapter.setOnSortSwitchListener(position -> {
            sort = (sort == 2 ? 3 : 2);
            replyAdapter.sort = this.sort;
            refresh(aid);
        });
    }
}