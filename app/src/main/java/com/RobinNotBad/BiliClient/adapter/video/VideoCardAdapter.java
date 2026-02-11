package com.RobinNotBad.BiliClient.adapter.video;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.article.ArticleCardHolder;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.TerminalContext;

import java.util.List;

//视频卡片Adapter 适用于各种场景（迫真
//日期不记得了

//2023-10-01 把一些公用代码移动到VideoCardHolder里了

public class VideoCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Context context;
    final List<VideoCard> videoCardList;
    OnItemLongClickListener longClickListener;

    public VideoCardAdapter(Context context, List<VideoCard> videoCardList) {
        this.context = context;
        this.videoCardList = videoCardList;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= videoCardList.size())
            return 0;
        VideoCard videoCard = videoCardList.get(position);
        if (videoCard != null && "article".equals(videoCard.type)) {
            return 1;
        }
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_list_compact, parent, false);
            return new ArticleCardHolder(view);
        } else {
            View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false);
            return new VideoCardHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position < 0 || position >= videoCardList.size())
            return;
        VideoCard videoCard = videoCardList.get(position);
        if (videoCard == null)
            return;

        if (holder instanceof ArticleCardHolder) {
            ((ArticleCardHolder) holder).showArticleCard(videoCard, context);
        } else if (holder instanceof VideoCardHolder) {
            ((VideoCardHolder) holder).showVideoCard(videoCard, context);
        }

        holder.itemView.setOnClickListener(view -> {
            try {
                switch (videoCard.type) {
                    case "video":
                        TerminalContext.getInstance().enterVideoDetailPage(context, videoCard.aid, videoCard.bvid, "video");
                        break;
                    case "media_bangumi":
                        TerminalContext.getInstance().enterVideoDetailPage(context, videoCard.aid, null, "media");
                        break;
                    case "article":
                        TerminalContext.getInstance().enterArticleDetailPage(context, videoCard.aid);
                        break;
                }
            } catch (Exception e) {
                // 改进的错误处理：自动清理失效的历史记录
                handleInvalidRecord(videoCard, holder.getAdapterPosition(), e);
            }
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
                return true;
            } else
                return false;
        });
    }

    /**
     * 处理无效的历史记录
     * 当记录无法访问时，自动清理并从列表中移除
     */
    private void handleInvalidRecord(VideoCard videoCard, int position, Exception e) {
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        
        // 自动清理失效的历史记录（事件级）
        if (videoCard.kid > 0) {
            com.RobinNotBad.BiliClient.util.CenterThreadPool.run(() -> {
                com.RobinNotBad.BiliClient.api.HistoryApi.deleteHistory(videoCard.kid);
            });
        }
        
        // 从列表中移除
        videoCardList.remove(position);
        notifyItemRemoved(position);
        
        // 显示友好的错误提示
        String errorMsg = "该内容已失效";
        if (videoCard.type.equals("article")) {
            errorMsg = "该专栏已失效或被删除";
        }
        com.RobinNotBad.BiliClient.util.MsgUtil.showMsg(errorMsg);
    }

    @Override
    public int getItemCount() {
        return videoCardList != null ? videoCardList.size() : 0;
    }

}
