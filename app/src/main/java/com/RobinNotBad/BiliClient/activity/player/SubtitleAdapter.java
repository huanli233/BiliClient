package com.RobinNotBad.BiliClient.activity.player;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.listener.OnItemClickListener;
import com.RobinNotBad.BiliClient.model.SubtitleLink;

public class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.Holder> {
    private SubtitleLink[] list;

    public OnItemClickListener listener;
    public int selectedItemIndex = 0;

    public SubtitleAdapter() {
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
    public void setData(SubtitleLink[] episodeList) {
        this.list = episodeList;
        selectedItemIndex = 0;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_subtitle, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (listener != null) {
            holder.listener = listener;
        }
        holder.bind(position, selectedItemIndex == position);
    }

    @Override
    public int getItemCount() {
        return list.length;
    }

    public class Holder extends RecyclerView.ViewHolder {

        private OnItemClickListener listener;
        private final Button button;

        public Holder(View view) {
            super(view);
            button = itemView.findViewById(R.id.btn);
        }

        void bind(int currentIndex, boolean isSelected) {
            button.setText(list[currentIndex].lang);
            if (isSelected) {
                button.setTextColor(0xcc262626);
                ViewCompat.setBackgroundTintList(button, AppCompatResources.getColorStateList(itemView.getContext(), R.color.background_button_selected));
            } else {
                button.setTextColor(0xffebe0e2);
                ViewCompat.setBackgroundTintList(button, AppCompatResources.getColorStateList(itemView.getContext(), R.color.background_button));
            }
            button.setOnClickListener(v -> {
                setSelectedItemIndex(currentIndex);
                if (listener != null) {
                    listener.onItemClick(currentIndex);
                }
            });
        }
    }
}


