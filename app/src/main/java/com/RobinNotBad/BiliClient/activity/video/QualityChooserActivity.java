package com.RobinNotBad.BiliClient.activity.video;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.QualityChooseAdapter;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import com.RobinNotBad.BiliClient.util.Result;
import com.RobinNotBad.BiliClient.util.TerminalContext;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class QualityChooserActivity extends BaseActivity {
    private static final String TAG = "QualityChooserActivity";

    List<Integer> qns = new LinkedList<>();
    private VideoInfo videoInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simple_list);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        findViewById(R.id.top).setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        ((TextView) findViewById(R.id.pageName)).setText("请选择清晰度");

        long aid = getIntent().getLongExtra("aid", 0);
        String bvid = getIntent().getStringExtra("bvid");
        Result<VideoInfo> videoInfoResult = TerminalContext.getInstance().getVideoInfoByAidOrBvId(aid, bvid).getValue();
        if (videoInfoResult == null) {
            videoInfoResult = Result.failure(new TerminalContext.IllegalTerminalStateException("video object is null"));
        }
        videoInfoResult.onSuccess((videoInfo) -> {
            this.videoInfo = videoInfo;
        }).onFailure((e) -> {
            Log.wtf(TAG, e);
            MsgUtil.showMsg("找不到视频信息QAQ");
        });
        QualityChooseAdapter adapter = new QualityChooseAdapter(this);
        int page = getIntent().getIntExtra("page", 0);
        CenterThreadPool.run(() -> {
            // 我只知道它返回可用清晰度列表
            try {
                String response = PlayerApi.getVideo(videoInfo.aid, videoInfo.cids.get(page), 16, true).second;
                JSONObject data = new JSONObject(response).getJSONObject("data");
                JSONArray accept_description = data.getJSONArray("accept_description");
                JSONArray accept_quality = data.getJSONArray("accept_quality");
                ArrayList<String> descs = new ArrayList<>();
                for (int i = 0; i < accept_description.length(); i++) {
                    String desc = accept_description.getString(i);
                    int qn = accept_quality.getInt(i);
                    qns.add(qn);
                    descs.add(desc);
                }
                runOnUiThread(() -> adapter.setNameList(descs));
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.showMsg("清晰度列表获取失败！"));
                e.printStackTrace();
            }
        });
        adapter.setOnItemClickListener((position -> {
            int qn = qns.get(position);
            PlayerApi.startDownloading(videoInfo, page, qn);
            finish();
        }));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
