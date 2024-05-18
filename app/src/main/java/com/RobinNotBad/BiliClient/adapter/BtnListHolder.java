package com.RobinNotBad.BiliClient.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

public class BtnListHolder extends RecyclerView.ViewHolder{
    TextView text_view;

    public BtnListHolder(@NonNull View itemView) {
        super(itemView);
        text_view = itemView.findViewById(R.id.text);
    }

    public void show(String text){
        text_view.setText(ToolsUtil.htmlToString(text));
    }
}
