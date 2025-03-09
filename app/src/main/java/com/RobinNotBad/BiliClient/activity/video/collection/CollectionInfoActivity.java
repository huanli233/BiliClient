package com.RobinNotBad.BiliClient.activity.video.collection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardHolder;
import com.RobinNotBad.BiliClient.model.Collection;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CollectionInfoActivity extends RefreshListActivity {
    private Collection collection = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long from_aid = getIntent().getLongExtra("fromVideo", -1);
        int season_id = getIntent().getIntExtra("season_id", -1);
        long mid = getIntent().getLongExtra("mid", -1);
        setPageName("合集详情");

        TerminalContext.getInstance().getVideoInfoByAidOrBvId(from_aid,null).observe(this, result -> result.onSuccess((videoInfo -> {
            collection = videoInfo.collection;

            RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;
            if (collection.sections == null && collection.cards != null) {
                adapter = new CardAdapter(this, collection);
            } else if (collection.sections != null) {
                adapter = new SectionAdapter(this, collection, recyclerView);
                List<Collection.Section> sections = collection.sections;
                int pos = 1;
                for (Collection.Section section : sections) {
                    pos++;
                    List<Collection.Episode> episodes = section.episodes;
                    for (Collection.Episode episode : episodes) {
                        pos++;
                        if (episode.aid == from_aid) {
                            Objects.requireNonNull(recyclerView.getLayoutManager()).scrollToPosition(--pos);
                        }
                    }
                }
            } else {
                finish();
                return;
            }

            setAdapter(adapter);
            setRefreshing(false);
        })));
    }

    static class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final Collection collection;
        final Context context;
        final List<VideoCard> data;

        public CardAdapter(Context context, Collection collection) {
            this.context = context;
            this.data = collection.cards;
            this.collection = collection;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? -1 : 0;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == -1) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.cell_collection_info, parent, false);
                return new CollectionInfoHolder(view);
            } else {
                View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false);
                return new VideoCardHolder(view);
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof VideoCardHolder) {
                position--;
                VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
                VideoCard videoCard = data.get(position);
                videoCardHolder.itemView.setOnClickListener((view) -> TerminalContext.getInstance().enterVideoDetailPage(context, videoCard.aid, videoCard.bvid));
                videoCardHolder.showVideoCard(videoCard, context);
            } else if (holder instanceof CollectionInfoHolder) {
                CollectionInfoHolder collectionInfoHolder = (CollectionInfoHolder) holder;
                collectionInfoHolder.name.setText(collection.title);
                collectionInfoHolder.desc.setText(TextUtils.isEmpty(collection.intro) ? "这里没有简介哦" : collection.intro);
                collectionInfoHolder.playTimes.setText("共" + collection.view);
                Glide.with(context).asDrawable().load(GlideUtil.url(collection.cover))
                        .transition(GlideUtil.getTransitionOptions())
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))).sizeMultiplier(0.85f).dontAnimate())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(collectionInfoHolder.cover);
                collectionInfoHolder.cover.setOnClickListener(view -> context.startActivity(new Intent(context, ImageViewerActivity.class).putExtra("imageList", new ArrayList<>(Collections.singletonList(collection.cover)))));
                ToolsUtil.setCopy(collectionInfoHolder.name, collectionInfoHolder.desc);
                ToolsUtil.setLink(collectionInfoHolder.desc);
            }
        }

        @Override
        public int getItemCount() {
            return data.size() + 1;
        }

        static class CollectionInfoHolder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView desc;
            final TextView playTimes;
            final ImageView cover;

            public CollectionInfoHolder(@NonNull View itemView) {
                super(itemView);
                this.name = itemView.findViewById(R.id.name);
                this.desc = itemView.findViewById(R.id.desc);
                this.cover = itemView.findViewById(R.id.img_cover);
                this.playTimes = itemView.findViewById(R.id.playTimes);
            }
        }
    }

    static class SectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final Collection collection;
        final RecyclerView recyclerView;
        final Context context;
        final List<Collection.Section> data;
        final List<Integer> types = new ArrayList<>();

        public SectionAdapter(Context context, Collection collection, RecyclerView recyclerView) {
            this.context = context;
            this.data = collection.sections;
            this.collection = collection;
            this.recyclerView = recyclerView;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return -1;
            return getTypes().get(--position);
        }

        private List<Integer> getTypes() {
            synchronized (this) {
                types.clear();
                for (Collection.Section section : data) {
                    types.add(1);
                    for (int i = 0; i < section.episodes.size(); i++) {
                        types.add(0);
                    }
                }
                return types;
            }
        }

        private int getSectionPos(int pos) {
            List<Integer> list = getTypes();
            int sectionPos = -1;
            for (int i = 0; i <= pos; i++) {
                if (list.get(i) == 1) sectionPos++;
            }
            return sectionPos;
        }

        private int getEpisodePos(int pos) {
            List<Integer> list = getTypes();
            int episodePos = -1;
            for (int i = pos; i >= 0; i--) {
                if (list.get(i) == 1) return episodePos;
                episodePos++;
            }
            return 1;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == -1) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.cell_collection_info, parent, false);
                return new CollectionInfoHolder(view);
            } else if (viewType == 0) {
                View view = LayoutInflater.from(this.context).inflate(R.layout.cell_video_list, parent, false);
                return new VideoCardHolder(view);
            } else {
                return new SectionHolder(new TextView(context));
            }
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SectionHolder) {
                position--;
                ((SectionHolder) holder).item.setText(data.get(getSectionPos(position)).title);
            } else if (holder instanceof VideoCardHolder) {
                position--;
                VideoCardHolder videoCardHolder = (VideoCardHolder) holder;
                VideoInfo videoInfo = data.get(getSectionPos(position)).episodes.get(getEpisodePos(position)).arc;
                VideoCard videoCard = new VideoCard(videoInfo.title, "", ToolsUtil.toWan(videoInfo.stats.view), videoInfo.cover, videoInfo.aid, videoInfo.bvid);
                videoCardHolder.itemView.setOnClickListener((view) -> TerminalContext.getInstance().enterVideoDetailPage(context, videoCard.aid, videoCard.bvid));
                videoCardHolder.showVideoCard(videoCard, context);
            } else if (holder instanceof CollectionInfoHolder) {
                CollectionInfoHolder collectionInfoHolder = (CollectionInfoHolder) holder;
                collectionInfoHolder.name.setText(collection.title);
                collectionInfoHolder.desc.setText(TextUtils.isEmpty(collection.intro) ? "这里没有简介哦" : collection.intro);
                collectionInfoHolder.playTimes.setText("共" + collection.view);
                Glide.with(context).asDrawable().load(GlideUtil.url(collection.cover))
                        .transition(GlideUtil.getTransitionOptions())
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))).sizeMultiplier(0.85f).dontAnimate())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(collectionInfoHolder.cover);
                collectionInfoHolder.cover.setOnClickListener(view -> context.startActivity(new Intent(context, ImageViewerActivity.class).putExtra("imageList", new ArrayList<>(Collections.singletonList(collection.cover)))));
            }
        }

        @Override
        public int getItemCount() {
            int count = 0;
            for (Collection.Section section : data) {
                count++;
                count += section.episodes.size();
            }
            return ++count;
        }

        static class SectionHolder extends RecyclerView.ViewHolder {
            private final TextView item;

            public SectionHolder(@NonNull TextView itemView) {
                super(itemView);
                this.item = itemView;
                this.item.setLeft(5);
            }
        }

        static class CollectionInfoHolder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView desc;
            final TextView playTimes;
            final ImageView cover;

            public CollectionInfoHolder(@NonNull View itemView) {
                super(itemView);
                this.name = itemView.findViewById(R.id.name);
                this.desc = itemView.findViewById(R.id.desc);
                this.cover = itemView.findViewById(R.id.img_cover);
                this.playTimes = itemView.findViewById(R.id.playTimes);
            }
        }
    }
}