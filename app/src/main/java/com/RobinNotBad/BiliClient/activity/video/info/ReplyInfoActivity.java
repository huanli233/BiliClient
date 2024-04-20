package com.RobinNotBad.BiliClient.activity.video.info;

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
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

//评论详细信息
//2023-07-22

public class ReplyInfoActivity extends BaseActivity {

    private long oid;
    private long rpid;
    private int sort = 0;
    private int type;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ArrayList<Reply> replyList;
    private ReplyAdapter replyAdapter;
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
        oid = intent.getLongExtra("oid",0);
        type = intent.getIntExtra("type",1);

        refreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        refreshLayout.setOnRefreshListener(this::refresh);

        setPageName("评论详情");

        replyList = new ArrayList<>();

        refreshLayout.setRefreshing(true);
        CenterThreadPool.run(()->{
            try {
                int result = ReplyApi.getReplies(oid,rpid,page,type,sort,replyList);
                if(result != -1) {
                    replyAdapter = new ReplyAdapter(this, replyList,oid,rpid,type,sort);
                    setOnSortSwitch();
                    runOnUiThread(()->{
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
                                int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();  //获取最后一个完全显示的itemPosition
                                int itemCount = manager.getItemCount();
                                if (lastItemPosition >= (itemCount - 3) && dy > 0 && !refreshing && !bottom) {// 滑动到倒数第三个就可以刷新了
                                    refreshing = true;
                                    CenterThreadPool.run(() -> continueLoading()); //加载第二页
                                }
                            }
                        });
                        refreshLayout.setRefreshing(false);
                    });
                    if(result == 1) {
                        Log.e("debug","到底了");
                        bottom = true;
                    }
                }

            } catch (Exception e) {runOnUiThread(()-> {
                MsgUtil.err(e,this);
                refreshLayout.setRefreshing(false);
            });}
        });
    }

    private void continueLoading() {
        runOnUiThread(()->refreshLayout.setRefreshing(true));
        page++;
        try {
            int lastSize = replyList.size();
            int result = ReplyApi.getReplies(oid,rpid,page,type,sort,replyList);
            if(result != -1){
                Log.e("debug","下一页");
                runOnUiThread(()-> {
                    replyAdapter.notifyItemRangeInserted(lastSize + 1,replyList.size() + 1 - lastSize);
                    refreshLayout.setRefreshing(false);
                });
                if(result == 1) {
                    //runOnUiThread(()-> MsgUtil.toast("到底啦QwQ",this));
                    Log.e("debug","到底了");
                    bottom = true;
                }
            }
            refreshing = false;
        } catch (Exception e) {runOnUiThread(()-> {
            MsgUtil.err(e,this);
            refreshLayout.setRefreshing(false);
        });}
    }

    private void refresh(){
        page = 1;
        replyList.clear();
        refreshLayout.setRefreshing(true);

        CenterThreadPool.run(()->{
            try {
                int result = ReplyApi.getReplies(oid,rpid,page,type,sort,replyList);
                if(result != -1) {
                    runOnUiThread(()->{
                        replyAdapter = new ReplyAdapter(this,replyList,oid,rpid,type,sort);
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
            } catch (Exception e) {runOnUiThread(()-> {
                MsgUtil.err(e,this);
                refreshLayout.setRefreshing(false);
            });}
        });
    }

    private void setOnSortSwitch(){
        replyAdapter.setOnSortSwitchListener(position -> {
            sort = (sort == 0 ? 1 : 0);
            refresh();
        });
    }
}