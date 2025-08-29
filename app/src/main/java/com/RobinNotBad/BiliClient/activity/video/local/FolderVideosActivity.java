package com.RobinNotBad.BiliClient.activity.video.local;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.model.LocalFolder;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FolderVideosActivity extends InstanceActivity {

    private RecyclerView recyclerView;
    private VideoCardAdapter adapter;
    private List<VideoCard> videoList;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_main_refresh);
        setMenuClick();

        folderName = getIntent().getStringExtra("folderName");

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText(folderName);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(getLayoutManager());

        findViewById(R.id.swipeRefreshLayout).setEnabled(false);

        loadVideos(folderName);
    }

    private void showVideoActionMenu(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_folder_video_action, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        MaterialButton btnChangeOrder = view.findViewById(R.id.btn_change_order);
        MaterialButton btnRemove = view.findViewById(R.id.btn_remove_from_folder);

        btnChangeOrder.setOnClickListener(v -> {
            showChangeOrderDialog(position);
            dialog.dismiss();
        });

        btnRemove.setOnClickListener(v -> {
            removeVideoFromFolder(position);
            dialog.dismiss();
        });

        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        dialog.getWindow().setAttributes(lp);
    }

    private void showChangeOrderDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_order, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText input = view.findViewById(R.id.edit_text_order);
        input.setText(String.valueOf(position + 1));

        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String orderStr = input.getText().toString();
            if (!orderStr.isEmpty()) {
                int newPosition = Integer.parseInt(orderStr) - 1;
                if (newPosition >= 0 && newPosition < videoList.size()) {
                    VideoCard video = videoList.remove(position);
                    videoList.add(newPosition, video);
                    saveFolders();
                    adapter.notifyDataSetChanged();
                } else {
                    MsgUtil.showMsg("请输入有效的位置");
                }
                dialog.dismiss();
            } else {
                MsgUtil.showMsg("位置不能为空");
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        dialog.getWindow().setAttributes(lp);
    }

    private void removeVideoFromFolder(int position) {
        videoList.remove(position);
        saveFolders();
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, videoList.size());
        MsgUtil.showMsg("已从文件夹中删除");
    }

    private void saveFolders() {
        File foldersConfigFile = FileUtil.getFoldersConfigFile();
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<LocalFolder>>() {}.getType();
        List<LocalFolder> allFolders = new ArrayList<>();
        if (foldersConfigFile.exists()) {
            try (FileReader reader = new FileReader(foldersConfigFile)) {
                allFolders = gson.fromJson(reader, listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (allFolders != null) {
            for (LocalFolder folder : allFolders) {
                if (folder.name.equals(folderName)) {
                    folder.videoList = videoList;
                    break;
                }
            }
        }

        try (FileWriter writer = new FileWriter(foldersConfigFile)) {
            gson.toJson(allFolders, writer);
        } catch (IOException e) {
            e.printStackTrace();
            MsgUtil.showMsg("操作失败");
        }
    }

    private void loadVideos(String folderName) {
        File foldersConfigFile = FileUtil.getFoldersConfigFile();
        if (!foldersConfigFile.exists()) {
            return; // No folders exist
        }

        Gson gson = new Gson();
        try (FileReader reader = new FileReader(foldersConfigFile)) {
            Type listType = new TypeToken<ArrayList<LocalFolder>>() {}.getType();
            List<LocalFolder> allFolders = gson.fromJson(reader, listType);

            if (allFolders == null) {
                return;
            }

            for (LocalFolder folder : allFolders) {
                if (folder.name.equals(folderName)) {
                    if (folder.videoList != null) {
                        videoList = folder.videoList;
                        for (VideoCard video : videoList) {
                            // Correctly construct the local path for the video file
                            File videoFolder = FileUtil.getVideoDownloadPath(video.title, video.pageTitle);
                            video.localPath = new File(videoFolder, "video.mp4").getAbsolutePath();
                        }
                        adapter = new VideoCardAdapter(this, videoList, true);
                        adapter.setOnLongClickListener(this::showVideoActionMenu);
                        recyclerView.setAdapter(adapter);
                    }
                    break; // Folder found, no need to continue loop
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
