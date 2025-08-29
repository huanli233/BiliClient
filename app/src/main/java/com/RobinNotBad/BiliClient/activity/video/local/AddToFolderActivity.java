package com.RobinNotBad.BiliClient.activity.video.local;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.video.FolderAdapter;
import com.RobinNotBad.BiliClient.model.LocalFolder;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AddToFolderActivity extends InstanceActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private final List<LocalFolder> folderList = new ArrayList<>();
    private VideoCard videoCard;
    private File foldersConfigFile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_main_refresh);
        setMenuClick();

        videoCard = getIntent().getParcelableExtra("videoCard");
        foldersConfigFile = FileUtil.getFoldersConfigFile();

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("添加到文件夹");

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refresh);

        adapter = new FolderAdapter(this, folderList);
        adapter.setOnItemClickListener(position -> {
            LocalFolder selectedFolder = folderList.get(position);
            addVideoToFolder(selectedFolder);
        });
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.setAdapter(adapter);

        refresh();
    }

    private void refresh() {
        CenterThreadPool.run(() -> {
            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
            loadFolders();
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }

    private void loadFolders() {
        folderList.clear();
        if (foldersConfigFile.exists()) {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(foldersConfigFile)) {
                Type listType = new TypeToken<ArrayList<LocalFolder>>() {}.getType();
                List<LocalFolder> loadedFolders = gson.fromJson(reader, listType);
                if (loadedFolders != null) {
                    folderList.addAll(loadedFolders);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addVideoToFolder(LocalFolder selectedFolder) {
        if (videoCard == null) {
            MsgUtil.showMsg("要添加的视频信息丢失，请重试");
            return;
        }

        for (LocalFolder folder : folderList) {
            if (folder.name.equals(selectedFolder.name)) {
                if (folder.videoList == null) {
                    folder.videoList = new ArrayList<>();
                }
                for (VideoCard item : folder.videoList) {
                    if (item.aid == videoCard.aid) {
                        MsgUtil.showMsg("视频已在该文件夹中");
                        return;
                    }
                }

                folder.videoList.add(videoCard);
                if (folder.videoList.size() == 1) { //如果添加的是第一个视频，则将该视频的封面设置为文件夹封面
                    folder.cover = videoCard.cover;
                }
                saveFolders();
                return;
            }
        }
    }

    private void saveFolders() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(foldersConfigFile)) {
            gson.toJson(folderList, writer);
            MsgUtil.showMsg("添加成功");
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            MsgUtil.showMsg("添加失败");
        }
    }
}
