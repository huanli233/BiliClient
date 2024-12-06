package com.RobinNotBad.BiliClient.adapter.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.DownloadSection;
import com.RobinNotBad.BiliClient.service.DownloadService;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Locale;

//下载列表Adapter
//2024-11-24

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadHolder> {

    Context context;
    public ArrayList<DownloadSection> downloadList;
    OnItemLongClickListener longClickListener;
    OnItemClickListener clickListener;

    public DownloadAdapter(Context context, ArrayList<DownloadSection> downloadList) {
        this.context = context;
        this.downloadList = downloadList;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
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
        boolean isDownloading = DownloadService.started && DownloadService.downloadingSection!=null;

        if(isDownloading){
            if(position==0){
                holder.show(DownloadService.downloadingSection,context);    //如果正在下载，那么第一项为正在下载的项
                holder.progress.setVisibility(View.VISIBLE);
                holder.showProgress(DownloadService.percent);
            }
            else {
                holder.progress.setVisibility(View.GONE);
                holder.show(downloadList.get(position-1),context);    //后续项为待下载的项
            }
        }
        else{
            holder.progress.setVisibility(View.GONE);
            holder.show(downloadList.get(position),context);    //如果不是正在下载，那么所有项都是待下载项
        }
        //holder.showLocalVideo(downloadList.get(position), context);

        holder.itemView.setOnClickListener(view -> {
            if(clickListener!=null) clickListener.onItemClick(isDownloading ? position-1 : position);
            //在activity端，position==-1即为正在下载的项
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(isDownloading ? position-1 : position);
                return true;
            } else return false;
        });
    }

    @Override
    public int getItemCount() {
        if(DownloadService.started){
            if(downloadList == null) return 1;
            else return downloadList.size()+1;
        }
        else if(downloadList==null) return 0;
        else return downloadList.size();
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
            title.setText(section.name_short);
            switch (section.state){
                case "error":
                    extra.setText("下载出错");
                    break;
                case "none":
                    extra.setText("等待下载");
                    break;
                default:
                    extra.setText("未知状态？");
            }

            Glide.with(context).asDrawable().load(section.url_cover)
                    .transition(GlideUtil.getTransitionOptions())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(cover);
        }

        @SuppressLint({"SetTextI18n"})
        public void showProgress(float percent){
            if(percent==-1) {
                progress.setVisibility(View.GONE);
                return;
            }
            progress.setVisibility(View.VISIBLE);
            extra.setVisibility(View.VISIBLE);
            extra.setText("下载中：" + String.format(Locale.CHINA,"%.2f", percent*100));
            int width = (int) (itemView.getMeasuredWidth() * percent);
            ViewGroup.LayoutParams layoutParams = progress.getLayoutParams();
            layoutParams.width = width;
            progress.setLayoutParams(layoutParams);
        }
    }
}
