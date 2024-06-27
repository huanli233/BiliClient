package com.RobinNotBad.BiliClient.adapter.message;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

public class ReplyCardHolder extends RecyclerView.ViewHolder {
    final TextView content;
    final TextView tiptext;

    public ReplyCardHolder(@NonNull View itemView) {
        super(itemView);
        content = itemView.findViewById(R.id.content);
        tiptext = itemView.findViewById(R.id.tip);
    }

    public void showReplyCard(Reply replyInfo) {
        content.setText(ToolsUtil.htmlToString(replyInfo.message));
    }
}
