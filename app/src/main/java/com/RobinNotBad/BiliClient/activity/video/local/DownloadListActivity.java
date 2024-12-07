package com.RobinNotBad.BiliClient.activity.video.local;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.DownloadAdapter;
import com.RobinNotBad.BiliClient.model.DownloadSection;
import com.RobinNotBad.BiliClient.service.DownloadService;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;

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

        MsgUtil.showMsg("提醒：能用就行\n此页面可能存在诸多问题");

        CenterThreadPool.run(()->{
            started = true;
            refreshList();

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(DownloadService.downloadingSection!=null) runOnUiThread(()->adapter.notifyItemChanged(0));
                }
            },300,500);
        });


    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshList() {
        if(this.isDestroyed() || !started) return;
        Log.d("debug","刷新下载列表");

        sections = DownloadService.getAllExceptDownloading();

        if (sections == null && DownloadService.downloadingSection == null) {
            if(!emptyTipShown) {
                runOnUiThread(()->MsgUtil.showMsg("下载列表为空"));
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
                adapter.setOnClickListener(position -> CenterThreadPool.run(()->{
                    Log.d("debug-download","click:"+position);
                    if(DownloadService.downloadingSection!=null) {
                        if (position == -1) {
                            stopService(new Intent(this, DownloadService.class));
                            MsgUtil.showMsg("已结束下载服务");
                        }
                    }
                    else{
                        DownloadService.setState(sections.get(position).id,"none");
                        Intent intent = new Intent(this, DownloadService.class);
                        intent.putExtra("first",sections.get(position).id);
                        startService(intent);
                    }
                }));

                adapter.setOnLongClickListener(position -> CenterThreadPool.run(()->{
                    try {
                        DownloadSection delete;
                        if(position == -1 && DownloadService.downloadingSection != null){
                            delete = DownloadService.downloadingSection;
                            stopService(new Intent(this, DownloadService.class));
                        }
                        else delete = sections.get(position);

                        FileUtil.deleteFolder(delete.getPath());
                        DownloadService.deleteSection(delete.id);

                        refreshList();
                        MsgUtil.showMsg("删除成功");
                    } catch (Exception e){
                        MsgUtil.err(e);
                    }
                }));

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
