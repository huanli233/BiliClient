package com.RobinNotBad.BiliClient.activity.user.favorite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

//收藏夹内
//2023-08-08
//2024-05-01

public class FavoriteVideoListActivity extends RefreshListActivity {

    private long mid;
    private long fid;
    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter videoCardAdapter;

    private int longClickPosition = -1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mid = intent.getLongExtra("mid", 0);
        fid = intent.getLongExtra("fid",0);
        String name = intent.getStringExtra("name");

        setPageName(name);

        videoList = new ArrayList<>();

        CenterThreadPool.run(()->{
            try {
                int result = FavoriteApi.getFolderVideos(mid,fid,page,videoList);
                if(result != -1) {
                    videoCardAdapter = new VideoCardAdapter(this, videoList);

                    videoCardAdapter.setOnLongClickListener(position -> {
                        if(longClickPosition == position) {
                            CenterThreadPool.run(() -> {
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
                                } catch (Exception e) {report(e);}
                            });
                        }
                        else {
                            longClickPosition = position;
                            MsgUtil.toast("再次长按删除",this);
                        }
                    });

                    setLoadMoreListener(this::continueLoading);
                    setAdapter(videoCardAdapter);
                    setRefreshing(false);

                    if(result == 1) {
                        Log.e("debug","到底了");
                        setBottom(true);
                    }
                }

            } catch (Exception e){report(e); setRefreshing(false);}
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        CenterThreadPool.run(()-> {
            try {
                int lastSize = videoList.size();
                int result = FavoriteApi.getFolderVideos(mid, fid, page, videoList);
                if (result != -1) {
                    Log.e("debug", "下一页");
                    runOnUiThread(() -> videoCardAdapter.notifyItemRangeInserted(lastSize, videoList.size() - lastSize));
                    if (result == 1) {
                        Log.e("debug", "到底了");
                        setBottom(true);
                    }
                }
                setRefreshing(false);
            } catch (Exception e) {
                report(e);
                setRefreshing(false);
                this.page--;
            }
        });
    }
}