package com.RobinNotBad.BiliClient.activity.settings;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.QualityChooseAdapter;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PlayQualitySettingActivity extends BaseActivity {
    QualityChooseAdapter adapter;

    static Map<String, Integer> qnMap = new HashMap<>() {{
        put("360P", 16);
        put("720P", 64);
        put("1080P", 80);
    }};

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

        adapter = new QualityChooseAdapter(this);
        adapter.setNameList(new ArrayList<>(qnMap.keySet()));
        adapter.setOnItemClickListener((this::save));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void save(int position) {
        SharedPreferencesUtil.putInt("play_qn", qnMap.get(adapter.getName(position)));
        finish();
    }
}
