package com.RobinNotBad.BiliClient.activity.message;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.message.PrivateMsgSessionsAdapter;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.model.PrivateMsgSession;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomLinearManager;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MessageActivity extends InstanceActivity {
    private RecyclerView sessionsView;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_message, null, (layoutView, id, parent) -> {
            setContentView(layoutView);
            setMenuClick();
            SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setEnabled(false);
            swipeRefreshLayout.setRefreshing(true);

            MaterialCardView reply = findViewById(R.id.reply);
            reply.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, NoticeActivity.class);
                intent.putExtra("type", "reply");
                startActivity(intent);
                ((TextView) findViewById(R.id.reply_text)).setText("回复我的");
            });


            MaterialCardView like = findViewById(R.id.like);
            like.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, NoticeActivity.class);
                intent.putExtra("type", "like");
                startActivity(intent);
                ((TextView) findViewById(R.id.like_text)).setText("收到的赞");
            });

            MaterialCardView at = findViewById(R.id.at);
            at.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, NoticeActivity.class);
                intent.putExtra("type", "at");
                startActivity(intent);
                ((TextView) findViewById(R.id.at_text)).setText("@我");
            });

            MaterialCardView system = findViewById(R.id.system);
            system.setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, NoticeActivity.class);
                intent.putExtra("type", "system");
                startActivity(intent);
            });

            sessionsView = findViewById(R.id.sessions_list);
            sessionsView.setNestedScrollingEnabled(false);

            CenterThreadPool.run(() -> {
                try {
                    JSONObject stats = MessageApi.getUnread();
                    ArrayList<PrivateMsgSession> sessionsList = PrivateMsgApi.getSessionsList(20);
                    ArrayList<Long> uidList = new ArrayList<>();
                    for (PrivateMsgSession item : sessionsList) {
                        uidList.add(item.talkerUid);
                    }
                    HashMap<Long, UserInfo> userMap = PrivateMsgApi.getUsersInfo(uidList);
                    PrivateMsgSessionsAdapter adapter = new PrivateMsgSessionsAdapter(this, sessionsList, userMap);
                    runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        try {
                            ((TextView) findViewById(R.id.reply_text)).setText("回复我的" + ((stats.getInt("reply") > 0) ? ("(" + stats.getInt("reply") + "未读)") : ""));
                            ((TextView) findViewById(R.id.like_text)).setText("收到的赞" + ((stats.getInt("like") > 0) ? ("(" + stats.getInt("like") + "未读)") : ""));
                            ((TextView) findViewById(R.id.at_text)).setText("@我" + ((stats.getInt("at") > 0) ? ("(" + stats.getInt("at") + "未读)") : ""));
                            sessionsView.setLayoutManager(new CustomLinearManager(this));
                            sessionsView.setAdapter(adapter);
                        } catch (Exception e) {
                            MsgUtil.err(e);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> MsgUtil.err(e));
                }
            });

            TutorialHelper.showTutorialList(this, R.array.tutorial_message, 5);
        });
    }
}
