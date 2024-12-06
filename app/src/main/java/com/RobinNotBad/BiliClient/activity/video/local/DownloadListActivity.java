package com.RobinNotBad.BiliClient.activity.video.local;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.activity.DownloadActivity;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.DownloadAdapter;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.DownloadSection;
import com.RobinNotBad.BiliClient.service.DownloadService;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadListActivity extends RefreshListActivity {
    public static WeakReference<DownloadListActivity> weakRef;
    DownloadAdapter adapter;
    Timer timer;
    boolean emptyTipShown;
    boolean firstRefresh = true;
    boolean started;
    ArrayList<DownloadSection> sections;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("下载列表");
        setRefreshing(false);
        weakRef = new WeakReference<>(this);

        CenterThreadPool.run(()->{
            started = true;
            refreshList();

            /*
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(DownloadService.started) runOnUiThread(()->adapter.notifyItemChanged(0));
                }
            },300,500);
             */
        });


    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshList() {
        if(this.isDestroyed() || !started) return;
        Log.d("debug","刷新下载列表");

        sections = DownloadService.getAllExceptDownloading();

        if (sections == null && DownloadService.downloadingSection == null) {
            if(!emptyTipShown) {
                MsgUtil.showMsg("下载列表为空");
                showEmptyView();
                emptyTipShown = true;
            }
        }
        else {
            if(emptyTipShown) {
                emptyTipShown = false;
                hideEmptyView();
            }

            if(firstRefresh){
                adapter = new DownloadAdapter(DownloadListActivity.this,sections);
                adapter.setOnClickListener(position -> {
                    if(DownloadService.started) {
                        if (position == -1) {
                            stopService(new Intent(this, DownloadService.class));
                        }
                    }
                    else{
                        startService(new Intent(this, DownloadService.class));
                    }
                });
                adapter.setOnLongClickListener(position -> {

                });

                setAdapter(adapter);
                firstRefresh = false;
            }
            else {
                adapter.downloadList = sections;
                runOnUiThread(()->adapter.notifyDataSetChanged());
            }
        }

    }

    @Override
    protected void onDestroy() {
        if(timer!=null) timer.cancel();
        weakRef = null;
        super.onDestroy();
    }
}
