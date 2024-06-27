package com.RobinNotBad.BiliClient.adapter.video;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.model.Bangumi;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MediaEpisodeAdapter extends RecyclerView.Adapter<MediaEpisodeAdapter.EposidesHolder> {
    private List<Bangumi.Episode> episodeList;

    public OnItemClickListener listener;
    public int selectedItemIndex = 0;
    private boolean useVerticalLayout;

    public MediaEpisodeAdapter() {
    }

    public MediaEpisodeAdapter(boolean useVerticalLayout) {
        this.useVerticalLayout = useVerticalLayout;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        // Cancel previous selected item and set current item
        int previousSelectedIndex = this.selectedItemIndex;
        this.selectedItemIndex = selectedItemIndex;
        notifyItemChanged(previousSelectedIndex);
        notifyItemChanged(selectedItemIndex);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Bangumi.Episode> episodeList) {
        this.episodeList = episodeList;
        selectedItemIndex = 0;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EposidesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(useVerticalLayout ? R.layout.cell_item_vertical : R.layout.cell_episode, parent, false);
        return new EposidesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EposidesHolder holder, int position) {
        if (listener != null) {
            holder.listener = listener;
        }
        holder.bind(position, selectedItemIndex == position);
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    public class EposidesHolder extends RecyclerView.ViewHolder {

        private OnItemClickListener listener;
        private final MaterialButton button;

        public EposidesHolder(View view) {
            super(view);
            button = itemView.findViewById(R.id.btn);
        }

        void bind(int currentIndex, boolean isSelected) {
            button.setText(episodeList.get(currentIndex).title);
            if (isSelected) {
                button.setTextColor(0xcc262626);
                ViewCompat.setBackgroundTintList(button, AppCompatResources.getColorStateList(itemView.getContext(), R.color.background_button_selected));
                button.setOnClickListener(null);
            } else {
                button.setTextColor(0xffebe0e2);
                ViewCompat.setBackgroundTintList(button, AppCompatResources.getColorStateList(itemView.getContext(), R.color.background_button));
                button.setOnClickListener(v -> {
                    setSelectedItemIndex(currentIndex);
                    if (listener != null) {
                        listener.onItemClick(currentIndex);
                    }
                });
            }
        }
    }
}


