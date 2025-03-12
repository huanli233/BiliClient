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

import java.io.File;
import java.io.IOException;
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
    boolean created;
    ArrayList<DownloadSection> sections;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("下载列表");
        setRefreshing(false);
        weakRef = new WeakReference<>(this);

        MsgUtil.showMsg("提醒：能用就行\n此页面可能存在诸多问题");

        CenterThreadPool.run(()->{
            created = true;
            refreshList(false);

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(DownloadService.section !=null && started) runOnUiThread(()->adapter.notifyItemChanged(0));
                }
            },300,500);
        });


    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshList(boolean fromOutside) {
        if(this.isDestroyed() || !created) return;
        if(fromOutside && !started) return;
        Log.d("debug","刷新下载列表");

        boolean downloading = DownloadService.section != null;

        sections = downloading ? DownloadService.getAllExceptDownloading() : DownloadService.getAll();

        if (sections == null && !downloading) {
            if(!emptyTipShown) {
                runOnUiThread(()->MsgUtil.showMsg("下载列表为空"));
                showEmptyView();
                emptyTipShown = true;
            }
        }
        else {
            if(sections != null) for (DownloadSection s:sections) {
                Log.d("debug-download",s.name_short);
            }

            if(emptyTipShown) {
                emptyTipShown = false;
                hideEmptyView();
            }

            if(firstRefresh){
                adapter = new DownloadAdapter(DownloadListActivity.this,sections);
                adapter.setOnClickListener(position -> CenterThreadPool.run(()->{
                    Log.d("debug-download","click:"+position);
                    if (position == -1) {
                        if(DownloadService.started) {
                            stopService(new Intent(this, DownloadService.class));
                            MsgUtil.showMsg("已结束下载服务");
                        }
                        else{
                            DownloadService.start(-1);
                        }
                    }
                    else{
                        if(position < sections.size()){
                            DownloadSection section = sections.get(position);
                            if(section.state.equals("downloading")) {
                                try {
                                    File folder = section.getPath();
                                    FileUtil.deleteFolder(folder);
                                    folder.mkdirs();
                                    File sign = new File(folder,".DOWNLOADING");
                                    sign.createNewFile();
                                } catch (IOException e) {
                                    MsgUtil.err("文件错误：",e);
                                }
                            }

                            DownloadService.setState(section.id,"none");
                            DownloadService.start(section.id);
                        }
                    }
                }));

                adapter.setOnLongClickListener(position -> CenterThreadPool.run(()->{
                    try {
                        final DownloadSection delete;
                        if(position == -1){
                            delete = DownloadService.section;
                            stopService(new Intent(this, DownloadService.class));
                        }
                        else delete = sections.get(position);
                        if(delete == null) return;

                        DownloadService.deleteSection(delete.id);

                        refreshList(false);
                        MsgUtil.showMsg("删除成功");
                    } catch (Exception e){
                        MsgUtil.err(e);
                    }
                }));

                setAdapter(adapter);
                started = true;
                firstRefresh = false;
            }
            else {
                adapter.downloadList = sections;
                runOnUiThread(()->adapter.notifyDataSetChanged());
                Log.d("debug-adapter",String.valueOf(adapter.getItemCount()));
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
