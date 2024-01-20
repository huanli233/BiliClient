package com.RobinNotBad.BiliClient.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;

public class ReplyCardHolder extends RecyclerView.ViewHolder{
    TextView content,pubdate,tiptext;
    public ReplyCardHolder(@NonNull View itemView) {
        super(itemView);
        content = itemView.findViewById(R.id.content);
        tiptext = itemView.findViewById(R.id.tip);
    }
    public void showReplyCard(Reply replyInfo){
        content.setText(LittleToolsUtil.htmlToString(replyInfo.message));
        if(replyInfo.isDynamic) tiptext.setText("不支持查看动态");
    }
}
