package com.RobinNotBad.BiliClient.activity.video.local;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.video.LocalVideoAdapter;
import com.RobinNotBad.BiliClient.model.LocalVideo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.io.File;
import java.util.ArrayList;

//本地列表
//2023-08-07

public class LocalListActivity extends InstanceActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private final ArrayList<LocalVideo> videoList = new ArrayList<>(10);
    private LocalVideoAdapter adapter;
    private TextView emptyTip;

    private int longClickPosition = -1;
    private Handler handler = new Handler();
    private Runnable runnable;

    private boolean started;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_main_refresh);
        setMenuClick();

        recyclerView = findViewById(R.id.recyclerView);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refresh);

        emptyTip = findViewById(R.id.emptyTip);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("缓存");

        if (!FileUtil.checkStoragePermission()) {
            FileUtil.requestStoragePermission(this);
        }

        CenterThreadPool.run(() -> {
            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
            scan(FileUtil.getDownloadPath());
            adapter = new LocalVideoAdapter(this, videoList);

            adapter.setOnLongClickListener(position -> {
                if (longClickPosition == position) {
                    File file = new File(FileUtil.getDownloadPath(), videoList.get(position).title);
                    CenterThreadPool.run(() -> FileUtil.deleteFolder(file));
                    MsgUtil.showMsg("删除成功");
                    videoList.remove(position);
                    adapter.notifyItemRemoved(position+1);
                    adapter.notifyItemRangeChanged(position+1, videoList.size() - position);
                    longClickPosition = -1;
                    checkEmpty();
                } else {
                    longClickPosition = position;
                    MsgUtil.showMsg("再次长按删除");
                    handler.postDelayed(runnable = () -> {
                        if (longClickPosition != -1) {
                            longClickPosition = -1;
                        }
                    }, 3000);
                }
            });
            runOnUiThread(() -> {
                recyclerView.setLayoutManager(getLayoutManager());
                recyclerView.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false);
                started = true;
            });
        });
    }

    private void scan(File folder) {
        File[] files = folder.listFiles();
        if(files==null) return;

        for (File video : files) {
            if (video.isDirectory()) {
                LocalVideo localVideo = new LocalVideo();
                localVideo.title = video.getName();

                localVideo.cover = (new File(video, "cover.png")).toString();

                localVideo.pageList = new ArrayList<>();
                localVideo.danmakuFileList = new ArrayList<>();
                localVideo.videoFileList = new ArrayList<>();

                File videoFile = new File(video, "video.mp4");
                File danmakuFile = new File(video, "danmaku.xml");

                if (videoFile.exists() && danmakuFile.exists()) {
                    File mark = new File(video,".DOWNLOADING");
                    if(mark.exists()) continue;

                    localVideo.videoFileList.add(videoFile.toString());
                    localVideo.danmakuFileList.add(danmakuFile.toString());    //单集视频
                    videoList.add(localVideo);
                }
                else {
                    File[] pages = video.listFiles();      //分页视频
                    if (pages != null) {
                        for (File page : pages) {
                            if (page.isDirectory()) {
                                File mark = new File(page,".DOWNLOADING");
                                if(mark.exists()) continue;

                                File pageVideoFile = new File(page, "video.mp4");
                                File pageDanmakuFile = new File(page, "danmaku.xml");
                                if (pageVideoFile.exists() && pageDanmakuFile.exists()) {
                                    localVideo.pageList.add(page.getName());
                                    localVideo.videoFileList.add(pageVideoFile.toString());
                                    localVideo.danmakuFileList.add(pageDanmakuFile.toString());
                                }
                            }
                        }
                        if(localVideo.videoFileList.size() > 0) videoList.add(localVideo);
                    }
                }

            }
        }
        checkEmpty();
    }

    private void checkEmpty() {
        runOnUiThread(() -> {
            if (videoList.isEmpty() && emptyTip != null) {
                emptyTip.setVisibility(View.VISIBLE);
            } else {
                if (emptyTip != null) {
                    emptyTip.setVisibility(View.GONE);
                }
            }
        });
    }

    public void refresh() {
        if(started) CenterThreadPool.run(() -> {
            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
            int oldSize = videoList.size();
            videoList.clear();
            scan(FileUtil.getDownloadPath());
            runOnUiThread(() -> {
                adapter.notifyItemRangeChanged(1, oldSize);
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }
}