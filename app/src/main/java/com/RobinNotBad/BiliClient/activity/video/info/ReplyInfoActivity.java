package com.RobinNotBad.BiliClient.activity.video.info;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.ReplyAdapter;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//评论详细信息
//2023-07-22

public class ReplyInfoActivity extends BaseActivity {

    private long oid;
    private long rpid;
    private int type;
    private RecyclerView recyclerView;
    private ArrayList<Reply> replyList;
    private ReplyAdapter replyAdapter;
    private boolean bottom = false;
    private int page = 1;
    private boolean refreshing = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        Intent intent = getIntent();
        rpid = intent.getLongExtra("rpid", 0);
        oid = intent.getLongExtra("oid",0);
        type = intent.getIntExtra("type",1);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        recyclerView = findViewById(R.id.recyclerView);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("评论详情");

        replyList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                int result = ReplyApi.getReplies(oid,rpid,page,type,replyList);
                if(result != -1) {
                    replyAdapter = new ReplyAdapter(this, replyList,oid,rpid,type);
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
                    });
                    if(result == 1) {
                        Log.e("debug","到底了");
                        bottom = true;
                    }
                }

            } catch (IOException e){
                runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,this));
                e.printStackTrace();
            } catch (JSONException e) {
                runOnUiThread(()-> MsgUtil.jsonErr(e,this));
                e.printStackTrace();
            }
        });
    }

    private void continueLoading() {
        page++;
        try {
            int lastSize = replyList.size();
            int result = ReplyApi.getReplies(oid,rpid,page,type,replyList);
            if(result != -1){
                Log.e("debug","下一页");
                runOnUiThread(()-> replyAdapter.notifyItemRangeInserted(lastSize + 1,replyList.size() + 1 - lastSize));
                if(result == 1) {
                    runOnUiThread(()-> MsgUtil.toast("到底啦QwQ",this));
                    Log.e("debug","到底了");
                    bottom = true;
                }
            }
            refreshing = false;
        } catch (IOException e){
            runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,this));
            e.printStackTrace();
        } catch (JSONException e) {
            runOnUiThread(()-> MsgUtil.jsonErr(e,this));
            e.printStackTrace();
        }
    }
}