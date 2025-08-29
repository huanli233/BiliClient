package com.RobinNotBad.BiliClient.activity.video.local;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.video.DownloadedVideoAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class DownloadedActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded);

        String path = getIntent().getStringExtra("path");
        if (path != null) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    ArrayList<File> videoFiles = new ArrayList<>();
                    for (File file : files) {
                        if (file.getName().endsWith(".blv")) { // Assuming .blv is the video format
                            videoFiles.add(file);
                        }
                    }

                    RecyclerView recyclerView = findViewById(R.id.recycler_view);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    DownloadedVideoAdapter adapter = new DownloadedVideoAdapter(this, videoFiles);
                    recyclerView.setAdapter(adapter);
                }
            }
        }
    }
}
