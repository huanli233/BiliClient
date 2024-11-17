package com.RobinNotBad.BiliClient.activity.video.local;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class DownloadListActivity extends RefreshListActivity {

    private SharedPreferences downloadPrefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        downloadPrefs = getSharedPreferences("download",MODE_PRIVATE);

        CenterThreadPool.run(()->{
            Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
            if(set.size() == 0) {
                MsgUtil.showMsg("下载列表set为空");
                showEmptyView();
                return;
            }

            String[] array = set.toArray(new String[0]);
            if (array.length == 0) {
                MsgUtil.showMsg("下载列表array为空");
                showEmptyView();
                return;
            }

            for (String str:array) {
                try{
                    JSONObject section = new JSONObject(str);

                    //Todo

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
