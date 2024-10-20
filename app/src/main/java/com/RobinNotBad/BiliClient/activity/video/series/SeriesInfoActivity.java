package com.RobinNotBad.BiliClient.activity.video.series;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.SeriesApi;
import com.RobinNotBad.BiliClient.model.PageInfo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;

public class SeriesInfoActivity extends RefreshListActivity {

    private long mid;
    private int sid;
    private ArrayList<VideoCard> videoList;
    private VideoCardAdapter videoCardAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if(type.equals("season")) MsgUtil.showMsg("此类型暂不支持！",this);

        mid = intent.getLongExtra("mid", 0);
        sid = intent.getIntExtra("sid", 0);
        String name = intent.getStringExtra("name");

        setPageName(name);

        videoList = new ArrayList<>();

        CenterThreadPool.run(() -> {
            try {
                PageInfo pageInfo = SeriesApi.getSeriesInfo(mid, sid, page, videoList);
                if (pageInfo.return_ps != 0) {
                    videoCardAdapter = new VideoCardAdapter(this, videoList);

                    setOnLoadMoreListener(this::continueLoading);
                    setAdapter(videoCardAdapter);

                    Log.e("debug", "return=" + pageInfo.return_ps + "require=" + pageInfo.require_ps );

                    if (pageInfo.return_ps < pageInfo.require_ps) {
                        Log.e("debug", "到底了");
                        setBottom(true);
                    }
                }
                else showEmptyView();
                setRefreshing(false);
            } catch (Exception e) {
                report(e);
                setRefreshing(false);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void continueLoading(int page) {
        CenterThreadPool.run(() -> {
            try {
                Log.e("debug", "下一页");
                int lastSize = videoList.size();
                PageInfo pageInfo = SeriesApi.getSeriesInfo(mid, sid, page, videoList);
                runOnUiThread(() -> videoCardAdapter.notifyItemRangeInserted(lastSize, pageInfo.return_ps));

                if (pageInfo.return_ps < pageInfo.require_ps || pageInfo.return_ps == 0) {
                    Log.e("debug", "到底了");
                    setBottom(true);
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
