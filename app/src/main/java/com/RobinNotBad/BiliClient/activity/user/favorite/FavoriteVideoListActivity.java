package com.RobinNotBad.BiliClient.activity.user.favorite;

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
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//收藏夹内
//2023-08-08

public class FavoriteVideoListActivity extends BaseActivity {

    private long mid;
    private long fid;
    private RecyclerView recyclerView;
    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter videoCardAdapter;
    private boolean bottom = false;
    private int page = 1;
    private boolean refreshing = false;

    private int longClickPosition = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        Intent intent = getIntent();
        mid = intent.getLongExtra("mid", 0);
        fid = intent.getLongExtra("fid",0);
        String name = intent.getStringExtra("name");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText(name);

        recyclerView = findViewById(R.id.recyclerView);

        videoList = new ArrayList<>();

        new Thread(()->{
            try {
                int result = FavoriteApi.getFolderVideos(mid,fid,page,videoList);
                if(result != -1) {
                    videoCardAdapter = new VideoCardAdapter(this, videoList);

                    videoCardAdapter.setOnLongClickListener(position -> {
                        if(longClickPosition == position) {
                            new Thread(() -> {
                                try {
                                    int delResult = FavoriteApi.deleteFavorite(videoList.get(position).aid,fid);
                                    longClickPosition = -1;
                                    if (delResult == 0) runOnUiThread(() -> {
                                        MsgUtil.toast("删除成功",this);
                                        videoList.remove(position);
                                        videoCardAdapter.notifyItemRemoved(position);
                                        videoCardAdapter.notifyItemRangeChanged(position,videoList.size() - position);
                                    });
                                    else runOnUiThread(()-> MsgUtil.toast("删除失败，错误码：" + delResult,this));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                        else {
                            longClickPosition = position;
                            MsgUtil.toast("再次长按删除",this);
                        }
                    });

                    runOnUiThread(()->{
                        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                                    new Thread(() -> continueLoading()).start(); //加载第二页
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
        }).start();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading() {
        page++;
        try {
            int lastSize = videoList.size();
            int result = FavoriteApi.getFolderVideos(mid,fid,page,videoList);
            if(result != -1){
                Log.e("debug","下一页");
                runOnUiThread(()-> videoCardAdapter.notifyItemRangeInserted(lastSize,videoList.size()-lastSize));
                if(result == 1) {
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