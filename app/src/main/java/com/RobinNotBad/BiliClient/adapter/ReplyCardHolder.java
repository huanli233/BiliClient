package com.RobinNotBad.BiliClient.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Reply;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

public class ReplyCardHolder extends RecyclerView.ViewHolder{
    TextView content,pubdate,tiptext;
    public ReplyCardHolder(@NonNull View itemView) {
        super(itemView);
        content = itemView.findViewById(R.id.content);
        tiptext = itemView.findViewById(R.id.tip);
    }
    public void showReplyCard(Reply replyInfo){
        content.setText(ToolsUtil.htmlToString(replyInfo.message));
        if(replyInfo.isDynamic) tiptext.setText("不支持查看动态");
    }
}
