package com.RobinNotBad.BiliClient.adapter.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.video.local.DownloadListActivity;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.model.DownloadSection;
import com.RobinNotBad.BiliClient.service.DownloadService;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadHolder> {

    Context context;
    public ArrayList<DownloadSection> downloadList;
    OnItemClickListener clickListener;

    public DownloadAdapter(Context context, ArrayList<DownloadSection> downloadList) {
        this.context = context;
        this.downloadList = downloadList;
    }

    public void setOnClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public DownloadHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_local, parent, false);
        return new DownloadHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadHolder holder, int position) {
        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null) clickListener.onItemClick(position - 1);
        });

        if (position == 0) {
            holder.show(DownloadService.section, context);    //第一项为正在下载的项（不存在就gone掉）
            holder.showProgress(DownloadService.state, DownloadService.percent);
            holder.itemView.setOnLongClickListener(null);
        } else {
            int listPosition = position - 1;
            holder.show(downloadList.get(listPosition), context);    //后续项为待下载的项
            holder.showProgress(null, -1);
            holder.itemView.setOnLongClickListener(view -> {
                showFolderLongPressDialog(listPosition);
                return true;
            });
        }
    }

    private void showFolderLongPressDialog(final int position) {
        new AlertDialog.Builder(context)
                .setTitle("操作")
                .setItems(new CharSequence[]{"删除此文件夹", "移动文件夹位置"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            deleteFolder(position);
                            break;
                        case 1:
                            showMoveFolderDialog(position);
                            break;
                    }
                })
                .show();
    }

    private void deleteFolder(int position) {
        DownloadSection section = downloadList.get(position);
        CenterThreadPool.run(() -> {
            try {
                DownloadService.deleteSection(section.id);
                if (context instanceof DownloadListActivity) {
                    ((Activity) context).runOnUiThread(() -> {
                        ((DownloadListActivity) context).refreshList(false);
                        MsgUtil.showMsg("删除成功");
                    });
                }
            } catch (Exception e) {
                MsgUtil.err(e);
            }
        });
    }

    private void showMoveFolderDialog(int currentPosition) {
        String[] items = new String[downloadList.size()];
        for (int i = 0; i < downloadList.size(); i++) {
            items[i] = "移动到位置 " + (i + 1);
        }

        new AlertDialog.Builder(context)
                .setTitle("移动到")
                .setItems(items, (dialog, which) -> {
                    if (which != currentPosition) {
                        Collections.swap(downloadList, currentPosition, which);
                        notifyItemMoved(currentPosition + 1, which + 1);
                        Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }


    @Override
    public int getItemCount() {
        if (downloadList == null) return 1;
        else return downloadList.size() + 1;
    }


    public static class DownloadHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView extra;
        final ImageView cover;
        final View progress;

        public DownloadHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
            cover = itemView.findViewById(R.id.img_cover);
            progress = itemView.findViewById(R.id.progress);
            extra = itemView.findViewById(R.id.text_extra);
        }

        public void show(DownloadSection section, Context context) {
            if (section == null) {
                title.setText("没有下载中的项");
                extra.setText("点击下面继续下载喵？");
                cover.setImageResource(R.mipmap.placeholder);
                return;
            }
            title.setText(section.name_short);
            switch (section.state) {
                case "error":
                    extra.setText("下载出错");
                    break;
                case "none":
                    extra.setText("等待下载");
                    break;
                case "downloading":
                    if (DownloadService.section == null) extra.setText("下载中断");
                    break;
                default:
                    extra.setText("未知状态？");
            }

            if (!section.url_cover.isEmpty())
                Glide.with(BiliTerminal.context).asDrawable().load(section.url_cover)
                        .transition(GlideUtil.getTransitionOptions())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(cover);
        }

        @SuppressLint({"SetTextI18n"})
        public void showProgress(String state, float percent) {
            if (state == null || percent == -1) {
                progress.setVisibility(View.GONE);
                return;
            }
            progress.setVisibility(View.VISIBLE);
            extra.setVisibility(View.VISIBLE);
            extra.setText(state + "：" + String.format(Locale.CHINA, "%.2f", percent * 100));
            int width = (int) (itemView.getMeasuredWidth() * percent);
            ViewGroup.LayoutParams layoutParams = progress.getLayoutParams();
            layoutParams.width = width;
            progress.setLayoutParams(layoutParams);
        }
    }
}
