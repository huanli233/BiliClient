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
    String[] str_array;
    DownloadAdapter adapter;
    Timer timer;
    boolean emptyTipShown;
    boolean firstRefresh = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("下载列表");
        setRefreshing(false);
        weakRef = new WeakReference<>(this);

        CenterThreadPool.run(()->{
            refreshList();

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(DownloadService.started)
                        runOnUiThread(()->adapter.notifyItemRangeChanged(0,1));
                }
            },300,500);
        });


    }

    @SuppressLint("NotifyDataSetChanged")
    public void refreshList() {
        if(this.isDestroyed()) return;

        str_array = DownloadService.getArray();

        if (str_array == null) {
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

            ArrayList<DownloadSection> sections = new ArrayList<>();

            for (String str : str_array) {
                try {
                    Log.d("debug-download",str);
                    DownloadSection section = new DownloadSection(new JSONObject(str));
                    if (!section.state.equals("downloading")) sections.add(section);
                } catch (JSONException e) {
                    e.printStackTrace();
                    DownloadService.deleteSection(str);
                }
            }

            if(firstRefresh){
                adapter = new DownloadAdapter(this,sections);
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
            else runOnUiThread(()->adapter.notifyDataSetChanged());
        }

    }

    @Override
    protected void onDestroy() {
        if(timer!=null) timer.cancel();
        weakRef = null;
        super.onDestroy();
    }
}
