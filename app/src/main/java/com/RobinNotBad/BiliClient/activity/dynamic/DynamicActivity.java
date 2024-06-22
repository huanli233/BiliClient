package com.RobinNotBad.BiliClient.activity.dynamic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.base.RefreshMainActivity;
import com.RobinNotBad.BiliClient.adapter.dynamic.DynamicAdapter;
import com.RobinNotBad.BiliClient.adapter.dynamic.DynamicHolder;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//动态页面
//2023-09-17

public class DynamicActivity extends RefreshMainActivity {

    private ArrayList<Dynamic> dynamicList;
    private DynamicAdapter dynamicAdapter;
    private long offset = 0;
    private boolean firstRefresh = true;
    private String type = "all";
    private static final Map<String, String> typeNameMap = Map.of(
            "全部", "all",
            "视频投稿", "video",
            "追番", "pgc",
            "专栏", "article"
    );
    public final ActivityResultLauncher<Intent> selectTypeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        int code = result.getResultCode();
        Intent data = result.getData();
        if (code == RESULT_OK && data != null && data.getStringExtra("item") != null) {
            String type = typeNameMap.get(data.getStringExtra("item"));
            if (type != null) {
                this.type = type;
                refreshDynamic();
            }
        }
    });

    public ActivityResultLauncher<Intent> writeDynamicLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        int code = result.getResultCode();
        Intent data = result.getData();
        if (code == RESULT_OK && data != null) {
            String text = data.getStringExtra("text");
            CenterThreadPool.run(() -> {
                try {
                    long dynId;
                    Map<String, Long> atUids = new HashMap<>();
                    Pattern pattern = Pattern.compile("@(\\S+)\\s");
                    Matcher matcher = pattern.matcher(text);
                    while (matcher.find()) {
                        String matchedString = matcher.group(1);
                        long uid;
                        if ((uid = DynamicApi.mentionAtFindUser(matchedString)) != -1) {
                            atUids.put(matchedString, uid);
                        }
                    }
                    if (atUids.isEmpty()) {
                        dynId = DynamicApi.publishTextContent(text);
                    } else {
                        dynId = DynamicApi.publishTextContent(text, atUids);
                    }
                    if (!(dynId == -1)) {
                        runOnUiThread(() -> MsgUtil.toast("发送成功~", DynamicActivity.this));
                        CenterThreadPool.run(() -> {
                            try {
                                Dynamic dynamic = DynamicApi.getDynamic(dynId);
                                dynamicList.add(0, dynamic);
                                runOnUiThread(() -> {
                                    dynamicAdapter.notifyItemInserted( 0);
                                    dynamicAdapter.notifyItemRangeChanged(0, dynamicList.size());
                                });
                            } catch (Exception e) {
                                MsgUtil.err(e, DynamicActivity.this);
                            }
                        });
                    } else {
                        runOnUiThread(() -> MsgUtil.toast("发送失败", DynamicActivity.this));
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> MsgUtil.err(e, DynamicActivity.this));
                }
            });
        }
    });

    /**
     * 该方法务必在Activity的onStart生命周期之前调用,否则系统底层会抛异常!!!
     * @param activity
     * @return
     */
    public static ActivityResultLauncher<Intent> getRelayDynamicLauncher(BaseActivity activity) {
        return activity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
            int code = result.getResultCode();
            Intent data = result.getData();
            if (code == RESULT_OK && data != null) {
                String text = data.getStringExtra("text");
                if (TextUtils.isEmpty(text)) text = "转发动态";
                long dynamicId = data.getLongExtra("dynamicId", -1);
                String finalText = text;
                CenterThreadPool.run(() -> {
                    try {
                        long dynId;
                        Map<String, Long> atUids = new HashMap<>();
                        Pattern pattern = Pattern.compile("@(\\S+)\\s");
                        Matcher matcher = pattern.matcher(finalText);
                        while (matcher.find()) {
                            String matchedString = matcher.group(1);
                            long uid;
                            if ((uid = DynamicApi.mentionAtFindUser(matchedString)) != -1) {
                                atUids.put(matchedString, uid);
                            }
                        }
                        dynId = DynamicApi.relayDynamic(finalText, (atUids.isEmpty() ? null : atUids), dynamicId);
                        if (!(dynId == -1)) {
                            activity.runOnUiThread(() -> MsgUtil.toast("转发成功~", activity));
                        } else {
                            activity.runOnUiThread(() -> MsgUtil.toast("转发失败", activity));
                        }
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> MsgUtil.err(e, activity));
                    }
                });
            }
        });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMenuClick();
        Log.e("debug","进入动态页");

        setOnRefreshListener(this::refreshDynamic);
        setOnLoadMoreListener(page -> addDynamic(type));

        setPageName("动态");
        
        TutorialHelper.show(R.xml.tutorial_dynamic,this,"dynamic",1);

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

        addDynamic(type);
    }

    private void addDynamic(String type) {
        Log.e("debug", "加载下一页");
        CenterThreadPool.run(()->{
            try {
                List<Dynamic> list = new ArrayList<>();
                offset = DynamicApi.getDynamicList(list,offset,0, type);
                bottom = (offset==-1);
                setRefreshing(false);

                runOnUiThread(() -> {
                    dynamicList.addAll(list);
                    if (firstRefresh) {
                        firstRefresh = false;
                        dynamicAdapter = new DynamicAdapter(this, dynamicList, recyclerView);
                        setAdapter(dynamicAdapter);
                    } else {
                        dynamicAdapter.notifyItemRangeInserted(dynamicList.size() - list.size(), list.size());
                    }
                });

            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DynamicHolder.GO_TO_INFO_REQUEST && resultCode == RESULT_OK) {
            try {
                if (data != null) {
                    DynamicHolder.removeDynamicFromList(dynamicList, data.getIntExtra("position", 0) - 1, dynamicAdapter);
                }
            } catch (Throwable ignored) {}
        }
    }
}