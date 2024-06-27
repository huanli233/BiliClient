package com.RobinNotBad.BiliClient.adapter.article;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.listener.OnItemLongClickListener;
import com.RobinNotBad.BiliClient.model.ArticleCard;

import java.util.ArrayList;


public class ArticleCardAdapter extends RecyclerView.Adapter<ArticleCardHolder> {

    final Context context;
    final ArrayList<ArticleCard> articleCardList;
    OnItemLongClickListener longClickListener;

    public ArticleCardAdapter(Context context, ArrayList<ArticleCard> articleCardList) {
        this.context = context;
        this.articleCardList = articleCardList;
    }

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ArticleCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_article_list, parent, false);
        return new ArticleCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleCardHolder holder, int position) {
        ArticleCard articleCard = articleCardList.get(position);
        holder.showArticleCard(articleCard, context);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(context, ArticleInfoActivity.class);
            intent.putExtra("cvid", articleCard.id);
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(view -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(position);
                return true;
            } else return false;
        });
    }

    @Override
    public int getItemCount() {
        return articleCardList.size();
    }

}
