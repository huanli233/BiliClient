package com.RobinNotBad.BiliClient.activity.video;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.QualityChooseAdapter;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.PlayerData;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomLinearManager;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;

import java.util.Arrays;

public class QualityChooserActivity extends BaseActivity {

    int[] qns;

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

        TerminalContext.getInstance().getVideoInfoByAidOrBvId(aid,bvid).observe(this,result -> result.onSuccess((videoInfo -> {

            QualityChooseAdapter adapter = new QualityChooseAdapter(this);
            int page = getIntent().getIntExtra("page", 0);
            CenterThreadPool.run(() -> {
                // 我只知道它返回可用清晰度列表
                try {
                    PlayerData playerData = videoInfo.toPlayerData(page);
                    PlayerApi.getVideo(playerData, true);
                    qns = playerData.qnValueList;
                    runOnUiThread(() -> adapter.setNameList(Arrays.asList(playerData.qnStrList)));
                } catch (Exception e) {
                    runOnUiThread(() -> MsgUtil.showMsg("清晰度列表获取失败！"));
                    e.printStackTrace();
                }
            });
            adapter.setOnItemClickListener((position -> {
                if(qns == null) return;
                int qn = qns[position];
                PlayerApi.startDownloading(videoInfo, page, qn);
                finish();
            }));

            recyclerView.setLayoutManager(new CustomLinearManager(this));
            recyclerView.setAdapter(adapter);
        })));

    }
}
