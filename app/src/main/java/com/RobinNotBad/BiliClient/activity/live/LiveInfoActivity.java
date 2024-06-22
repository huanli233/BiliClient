package com.RobinNotBad.BiliClient.activity.live;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.adapter.user.UpListAdapter;
import com.RobinNotBad.BiliClient.api.LiveApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LiveInfoActivity extends BaseActivity {
    private long room_id;
    private LiveRoom room;
    private boolean desc_expand;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Intent intent = getIntent();
        room_id = intent.getLongExtra("room_id",0);
        if(room_id == 0) {
            finish();
            return;
        }

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_live_info, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setTopbarExit();

            CenterThreadPool.run(() -> {
                try {
                    room = LiveApi.getRoomInfo(room_id);
                    UserInfo userInfo = UserInfoApi.getUserInfo(room.uid);
                    runOnUiThread(() -> {
                        ImageView cover = findViewById(R.id.cover);
                        TextView title = findViewById(R.id.title);
                        RecyclerView up_recyclerView = findViewById(R.id.up_recyclerView);
                        TextView viewsCount = findViewById(R.id.viewsCount);
                        MaterialButton play = findViewById(R.id.play);
                        TextView timeText = findViewById(R.id.timeText);
                        TextView idText = findViewById(R.id.idText);
                        TextView tags = findViewById(R.id.tags);
                        TextView description = findViewById(R.id.description);

                        Glide.with(this).asDrawable().load(GlideUtil.url(room.user_cover)).placeholder(R.mipmap.placeholder)
                                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4, this))).sizeMultiplier(0.85f).skipMemoryCache(true).dontAnimate())
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(cover);

                        title.setText(room.title);

                        ArrayList<UserInfo> upList = new ArrayList<>();
                        upList.add(new UserInfo(userInfo.mid,userInfo.name,userInfo.avatar,"主播",0,6,false,"",0,""));
                        UpListAdapter upListAdapter = new UpListAdapter(this,upList);
                        up_recyclerView.setHasFixedSize(true);
                        up_recyclerView.setLayoutManager(new LinearLayoutManager(this));
                        up_recyclerView.setAdapter(upListAdapter);

                        viewsCount.setText(ToolsUtil.toWan(room.online) + "人观看");
                        timeText.setText("直播开始于" + room.liveTime);

                        idText.setText(String.valueOf(room_id));
                        tags.setText(room.tags);
                        ToolsUtil.setCopy(this,idText,tags,title);

                        description.setText(ToolsUtil.htmlToString(room.description));
                        description.setOnClickListener(view1 -> {
                            if (desc_expand) description.setMaxLines(3);
                            else description.setMaxLines(512);
                            desc_expand = !desc_expand;
                        });
                    });
                }catch (Exception e){
                    runOnUiThread(() -> MsgUtil.err(e,this));
                }
            });
        });
    }
}
