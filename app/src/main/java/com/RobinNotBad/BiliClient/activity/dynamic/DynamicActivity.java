package com.RobinNotBad.BiliClient.activity.dynamic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshMainActivity;
import com.RobinNotBad.BiliClient.adapter.DynamicAdapter;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;

//动态页面
//2023-09-17

public class DynamicActivity extends RefreshMainActivity {

    private ArrayList<Dynamic> dynamicList;
    private DynamicAdapter dynamicAdapter;
    private long offset = 0;
    private boolean firstRefresh = true;

    public ActivityResultLauncher<Intent> writeDynamicLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int code = result.getResultCode();
            Intent data = result.getData();
            if (code == RESULT_OK && data != null) {
                String text = data.getStringExtra("text");
                CenterThreadPool.run(() -> {
                    try {
                        long dynId = DynamicApi.publishTextContent(text);
                        if (!(dynId == -1)) {
                            runOnUiThread(() -> MsgUtil.toast("发送成功~", DynamicActivity.this));
                        } else {
                            runOnUiThread(() -> MsgUtil.toast("发送失败", DynamicActivity.this));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> MsgUtil.err(e, DynamicActivity.this));
                    }
                });
            }
        }
    });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMenuClick(4);
        Log.e("debug","进入动态页");

        setOnRefreshListener(this::refreshDynamic);
        setOnLoadMoreListener(page -> addDynamic());

        setPageName("动态");

        if(!SharedPreferencesUtil.getBoolean("tutorial_dynamic",false)){
            MsgUtil.showTutorial(this,"使用教程","点击动态的文字部分可以查看动态详情，打开详情后左右滑动可以查看评论区（被转发的动态和投稿类动态不支持查看详情，暂不支持显示直播预约和专栏动态）",-1);
            SharedPreferencesUtil.putBoolean("tutorial_dynamic",true);
        }

        refreshDynamic();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDynamic() {
        Log.e("debug", "刷新");
        if (firstRefresh) {
            dynamicList = new ArrayList<>();
        } else {
            offset = 0;
            bottom = false;
            dynamicList.clear();
            dynamicAdapter.notifyDataSetChanged();
        }

        addDynamic();
    }

    private void addDynamic() {
        Log.e("debug", "加载下一页");
        CenterThreadPool.run(()->{
            int lastSize = dynamicList.size();
            try {
                offset = DynamicApi.getDynamicList(dynamicList,offset,0);
                bottom = (offset==-1);
                setRefreshing(false);

                runOnUiThread(() -> {
                    if (firstRefresh) {
                        firstRefresh = false;
                        dynamicAdapter = new DynamicAdapter(this, dynamicList);
                        setAdapter(dynamicAdapter);
                    }
                    else {
                        dynamicAdapter.notifyItemRangeInserted(lastSize, dynamicList.size() - lastSize);
                    }
                });

            } catch (Exception e) {
                loadFail(e);
            }
        });

    }

}