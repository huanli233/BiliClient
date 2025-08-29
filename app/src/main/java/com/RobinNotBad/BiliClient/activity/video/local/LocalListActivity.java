package com.RobinNotBad.BiliClient.activity.video.local;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.video.LocalVideoAdapter;
import com.RobinNotBad.BiliClient.model.LocalVideo;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

//本地列表
//2023-08-07

public class LocalListActivity extends InstanceActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private final ArrayList<LocalVideo> videoList = new ArrayList<>(10);
    private LocalVideoAdapter adapter;
    private TextView emptyTip;
    private MaterialButton folderButton;

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

        addFolderManagerButton();
        setRecyclerViewScrollListener();

        if (!FileUtil.checkStoragePermission()) {
            FileUtil.requestStoragePermission(this);
        }

        CenterThreadPool.run(() -> {
            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
            scan(FileUtil.getVideoDownloadPath());
            adapter = new LocalVideoAdapter(this, videoList);

            adapter.setOnLongClickListener(this::showActionMenu);
            runOnUiThread(() -> {
                recyclerView.setLayoutManager(getLayoutManager());
                recyclerView.setAdapter(adapter);
                swipeRefreshLayout.setRefreshing(false);
                started = true;
            });
        });
    }

    private void setRecyclerViewScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 10 && folderButton.getVisibility() == View.VISIBLE) {
                    folderButton.animate()
                            .translationY(folderButton.getHeight() + ((ViewGroup.MarginLayoutParams) folderButton.getLayoutParams()).bottomMargin)
                            .alpha(0.0f)
                            .setDuration(200)
                            .withEndAction(() -> folderButton.setVisibility(View.GONE));
                } else if (dy < -10 && folderButton.getVisibility() != View.VISIBLE) {
                    folderButton.setVisibility(View.VISIBLE);
                    folderButton.animate()
                            .translationY(0)
                            .alpha(0.95f)
                            .setDuration(200);
                }
            }
        });
    }

    private void addFolderManagerButton() {
        View parent = (View) findViewById(R.id.swipeRefreshLayout).getParent();
        if (parent instanceof RelativeLayout) {
            RelativeLayout rootLayout = (RelativeLayout) parent;
            folderButton = new MaterialButton(this);
            folderButton.setText("文件夹管理");
            folderButton.setAlpha(0.99f);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            int margin = (int) (16 * getResources().getDisplayMetrics().density);
            params.setMargins(0, 0, 0, margin);
            folderButton.setLayoutParams(params);
            rootLayout.addView(folderButton);

            folderButton.setOnClickListener(v -> {
                Intent intent = new Intent(LocalListActivity.this, FolderManagerActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showActionMenu(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_local_video_action, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        MaterialButton btnAddToFolder = view.findViewById(R.id.btn_add_to_folder);
        MaterialButton btnDelete = view.findViewById(R.id.btn_delete);

        btnAddToFolder.setOnClickListener(v -> {
            LocalVideo localVideo = videoList.get(position);
            if (localVideo.aid == 0 || localVideo.bvid == null) {
                MsgUtil.showMsg("视频信息不完整，无法添加");
                return;
            }
            VideoCard videoCard = new VideoCard(localVideo.title, null, null, localVideo.cover, localVideo.aid, localVideo.bvid);
            Intent intent = new Intent(LocalListActivity.this, AddToFolderActivity.class);
            intent.putExtra("videoCard", (Parcelable) videoCard);
            startActivity(intent);
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            deleteVideo(position);
            dialog.dismiss();
        });

        dialog.show();
        // Make the dialog width responsive
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        dialog.getWindow().setAttributes(lp);
    }

    private void deleteVideo(int position) {
        File file = new File(FileUtil.getVideoDownloadPath(), videoList.get(position).title);
        CenterThreadPool.run(() -> FileUtil.deleteFolder(file));
        MsgUtil.showMsg("删除成功");
        videoList.remove(position);
        adapter.notifyItemRemoved(position + 1);
        adapter.notifyItemRangeChanged(position + 1, videoList.size() - position);
        checkEmpty();
    }

    private void scan(File folder) {
        File[] files = folder.listFiles();
        if(files==null) return;

        for (File video : files) {
            if (video.isDirectory()) {
                LocalVideo localVideo = new LocalVideo();
                localVideo.title = video.getName();

                localVideo.cover = (new File(video, "cover.png")).toString();

                // Read metadata from info.json
                File infoFile = new File(video, "info.json");
                if (infoFile.exists()) {
                    try (FileReader reader = new FileReader(infoFile)) {
                        VideoCard videoCard = new Gson().fromJson(reader, VideoCard.class);
                        if (videoCard != null) {
                            localVideo.aid = videoCard.aid;
                            localVideo.bvid = videoCard.bvid;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                localVideo.pageList = new ArrayList<>();
                localVideo.danmakuFileList = new ArrayList<>();
                localVideo.videoFileList = new ArrayList<>();
                localVideo.sizeList = new ArrayList<>();

                File videoFile = new File(video, "video.mp4");
                File danmakuFile = new File(video, "danmaku.xml");

                if (videoFile.exists()) {
                    File mark = new File(video,".DOWNLOADING");
                    if(mark.exists()) continue;

                    localVideo.sizeList.add(videoFile.length());
                    localVideo.videoFileList.add(videoFile.toString());
                    localVideo.danmakuFileList.add(danmakuFile.toString());    //单集视频

                    localVideo.calcTotalSize();
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
                                if (pageVideoFile.exists()) {
                                    localVideo.pageList.add(page.getName());
                                    localVideo.sizeList.add(pageVideoFile.length());
                                    localVideo.videoFileList.add(pageVideoFile.toString());
                                    localVideo.danmakuFileList.add(pageDanmakuFile.toString());
                                }
                            }
                        }
                        localVideo.calcTotalSize();
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
            scan(FileUtil.getVideoDownloadPath());
            runOnUiThread(() -> {
                adapter.notifyItemRangeChanged(1, oldSize);
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }
}
