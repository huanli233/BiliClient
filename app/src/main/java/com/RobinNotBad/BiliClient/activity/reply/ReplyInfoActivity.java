package com.RobinNotBad.BiliClient.activity.reply;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.ReplyAdapter;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.event.ReplyEvent;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//评论详细信息
//2023-07-22

public class ReplyInfoActivity extends BaseActivity {

    private long oid;
    private long rpid, mid;
    private int sort = 0;
    private int type;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<Reply> replyList;
    private ReplyAdapter replyAdapter;
    public Reply origReply;
    private boolean bottom = false;
    private int page = 1;
    private boolean refreshing = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_refresh);

        Intent intent = getIntent();
        rpid = intent.getLongExtra("rpid", 0);
        oid = intent.getLongExtra("oid", 0);
        type = intent.getIntExtra("type", 1);
        mid = intent.getLongExtra("up_mid", -1);
        origReply = (Reply) intent.getSerializableExtra("origReply");

        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        refreshLayout.setOnRefreshListener(this::refresh);

        setPageName("评论详情");

        replyList = new ArrayList<>();

        refreshLayout.setRefreshing(true);
        CenterThreadPool.run(() -> {
            try {
                int result = ReplyApi.getReplies(oid, rpid, page, type, sort, replyList);
                if (result != -1) {
                    replyList.add(0, origReply);
                    replyAdapter = new ReplyAdapter(this, replyList, oid, rpid, type, sort, getIntent().getSerializableExtra("source"), mid);
                    replyAdapter.isDetail = true;
                    setOnSortSwitch();
                    runOnUiThread(() -> {
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        recyclerView.setAdapter(replyAdapter);
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
                                int lastItemPosition = manager.findLastVisibleItemPosition();  //获取最后一个显示的itemPosition
                                int itemCount = manager.getItemCount();
                                if (lastItemPosition >= (itemCount - 3) && dy > 0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                                    refreshing = true;
                                    CenterThreadPool.run(() -> continueLoading()); //加载第二页
                                }
                            }
                        });
                        refreshLayout.setRefreshing(false);
                    });
                    if (result == 1) {
                        Log.e("debug", "到底了");
                        bottom = true;
                    }
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    MsgUtil.err(e, this);
                    refreshLayout.setRefreshing(false);
                });
            }
        });
    }

    private void continueLoading() {
        runOnUiThread(() -> refreshLayout.setRefreshing(true));
        page++;
        try {
            List<Reply> list = new ArrayList<>();
            int result = ReplyApi.getReplies(oid, rpid, page, type, sort, list);
            if (result != -1) {
                Log.e("debug", "下一页");
                runOnUiThread(() -> {
                    replyList.addAll(list);
                    replyAdapter.notifyItemRangeInserted(replyList.size() - list.size(), list.size());
                    refreshLayout.setRefreshing(false);
                });
                if (result == 1) {
                    //runOnUiThread(()-> MsgUtil.toast("到底啦QwQ",this));
                    Log.e("debug", "到底了");
                    bottom = true;
                }
            }
            refreshing = false;
        } catch (Exception e) {
            runOnUiThread(() -> {
                MsgUtil.err(e, this);
                refreshLayout.setRefreshing(false);
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refresh() {
        page = 1;
        refreshLayout.setRefreshing(true);

        CenterThreadPool.run(() -> {
            try {
                List<Reply> list = new ArrayList<>();
                int result = ReplyApi.getReplies(oid, rpid, page, type, sort, list);

                if (result != -1) {
                    runOnUiThread(() -> {
                        replyList.clear();
                        replyList.add(0, origReply);
                        replyList.addAll(list);
                        if (replyAdapter == null) {
                            replyAdapter = new ReplyAdapter(this, replyList, oid, rpid, type, sort, mid);
                            replyAdapter.isDetail = true;
                            setOnSortSwitch();
                            recyclerView.setAdapter(replyAdapter);
                        } else {
                            replyAdapter.notifyDataSetChanged();
                        }
                        refreshLayout.setRefreshing(false);
                    });
                    if (result == 1) {
                        Log.e("debug", "到底了");
                        bottom = true;
                    } else bottom = false;
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    MsgUtil.err(e, this);
                    refreshLayout.setRefreshing(false);
                });
            }
        });
    }

    private void setOnSortSwitch() {
        replyAdapter.setOnSortSwitchListener(position -> {
            sort = (sort == 0 ? 1 : 0);
            refresh();
        });
    }

    @Override
    protected boolean eventBusEnabled() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC, sticky = true, priority = 1)
    public void onEvent(ReplyEvent event) {
        if (event.getOid() != oid) return;
        LinearLayoutManager layoutManager = (LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager());
        int pos = layoutManager.findFirstCompletelyVisibleItemPosition();
        pos--;
        if (pos <= 0) {
            pos = layoutManager.findFirstVisibleItemPosition();
            pos--;
        }
        pos = pos <= 0 ? 1 : pos;
        replyList.add(pos, event.getMessage());
        int finalPos = pos;
        runOnUiThread(() -> {
            if (replyAdapter != null) {
                replyAdapter.notifyItemInserted(finalPos);
                replyAdapter.notifyItemRangeChanged(finalPos, replyList.size() - finalPos + 1);
                layoutManager.scrollToPositionWithOffset(finalPos + 1, 0);
            }
        });
    }
}