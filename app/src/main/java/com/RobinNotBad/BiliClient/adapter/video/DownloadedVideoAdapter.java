package com.RobinNotBad.BiliClient.adapter.video;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.player.PlayerActivity;
import com.RobinNotBad.BiliClient.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class DownloadedVideoAdapter extends RecyclerView.Adapter<DownloadedVideoAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<File> videoFiles;

    public DownloadedVideoAdapter(Context context, ArrayList<File> videoFiles) {
        this.context = context;
        this.videoFiles = videoFiles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File videoFile = videoFiles.get(position);
        String videoName = videoFile.getParentFile().getName(); // Use parent folder name as display name
        holder.textView.setText(videoName);
        holder.itemView.setClickable(true);
        holder.itemView.setFocusable(true);
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            String videoPath = videoFile.getAbsolutePath();
            String danmakuPath = new File(videoFile.getParentFile(), "danmaku.xml").getAbsolutePath();
            intent.putExtra("url", videoPath);
            intent.putExtra("danmaku", danmakuPath);
            intent.putExtra("title", videoName);
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            showLongPressDialog(holder.getAdapterPosition());
            return true;
        });
    }

    private void showLongPressDialog(final int position) {
        new AlertDialog.Builder(context)
                .setTitle("操作")
                .setItems(new CharSequence[]{"将此视频从文件夹中移除", "移动视频位置"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            removeVideo(position);
                            break;
                        case 1:
                            showMoveVideoDialog(position);
                            break;
                    }
                })
                .show();
    }

    private void removeVideo(int position) {
        File videoFile = videoFiles.get(position);
        File videoFolder = videoFile.getParentFile();
        if (FileUtil.deleteFolder(videoFolder)) {
            videoFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, videoFiles.size());
            Toast.makeText(context, "已移除", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "移除失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void showMoveVideoDialog(int currentPosition) {
        String[] items = new String[videoFiles.size()];
        for (int i = 0; i < videoFiles.size(); i++) {
            items[i] = "移动到位置 " + (i + 1);
        }

        new AlertDialog.Builder(context)
                .setTitle("移动到")
                .setItems(items, (dialog, which) -> {
                    if (which != currentPosition) {
                        Collections.swap(videoFiles, currentPosition, which);
                        notifyItemMoved(currentPosition, which);
                        Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return videoFiles.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
