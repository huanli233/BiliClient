package com.RobinNotBad.BiliClient.adapter;

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

import java.util.ArrayList;

public class MediaEpisodesAdapter extends RecyclerView.Adapter<MediaEpisodesAdapter.EposidesHolder> {
    private ArrayList<Bangumi.Episode> episodeList;

    public OnItemClickListener listener;
    private int selectedItemIndex = 0;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public int getSelectedItemIndex() {
        return selectedItemIndex;
    }

    public void setSelectedItemIndex(int selectedItemIndex) {
        // Cancel previous selected item and set current item
        int previousSelectedIndex = this.selectedItemIndex;
        this.selectedItemIndex = selectedItemIndex;
        notifyItemChanged(previousSelectedIndex);
        notifyItemChanged(selectedItemIndex);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(ArrayList<Bangumi.Episode> episodeList) {
        this.episodeList = episodeList;
        selectedItemIndex = 0;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EposidesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_button_only_text, parent, false);
        return new EposidesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EposidesHolder holder, int position) {
        if(listener != null){
            holder.listener = listener;
        }
        holder.bind(position, selectedItemIndex == position);
    }

    @Override
    public int getItemCount() {return episodeList.size();}

    public class EposidesHolder extends RecyclerView.ViewHolder {

        private OnItemClickListener listener;
        private final MaterialButton button;

        public EposidesHolder(View view) {
            super(view);
            button = itemView.findViewById(R.id.btn);
        }

        void bind(int currentIndex, boolean isSelected) {
            button.setText(String.valueOf(currentIndex + 1));
            if (isSelected) {
                button.setTextColor(0x78242424);
                ViewCompat.setBackgroundTintList(button, AppCompatResources.getColorStateList(itemView.getContext(), R.color.background_button_selected));
                button.setOnClickListener(null);
            } else {
                button.setTextColor(0xFFFFFFFF);
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


