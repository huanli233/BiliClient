package com.RobinNotBad.BiliClient.activity.live;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.adapter.user.UpListAdapter;
import com.RobinNotBad.BiliClient.adapter.video.MediaEpisodeAdapter;
import com.RobinNotBad.BiliClient.api.LiveApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.Bangumi;
import com.RobinNotBad.BiliClient.model.LivePlayInfo;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class LiveInfoActivity extends BaseActivity {
    private long room_id;
    private LiveRoom room;
    private boolean desc_expand = false,tags_expand = false;

    private RecyclerView protocol_list,host_list;
    private int selectedProtocol = 0,selectedHost = 0;
    private MediaEpisodeAdapter hostAdapter;
    private LivePlayInfo playInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        room_id = getIntent().getLongExtra("room_id",0);
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
                    playInfo = LiveApi.getRoomPlayInfo(room_id,LiveApi.PlayerQnMap.get(SharedPreferencesUtil.getInt("play_qn", 16)));
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
                        protocol_list = findViewById(R.id.protocol_list);
                        host_list = findViewById(R.id.host_list);

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
                        tags.setText("标签：" + room.tags);
                        tags.setOnClickListener(view1 -> {
                            if (tags_expand) tags.setMaxLines(1);
                            else tags.setMaxLines(233);
                            tags_expand = !tags_expand;
                        });
                        ToolsUtil.setCopy(this,idText,tags,title);

                        description.setText(ToolsUtil.htmlToString(room.description));
                        description.setOnClickListener(view1 -> {
                            if (desc_expand) description.setMaxLines(3);
                            else description.setMaxLines(512);
                            desc_expand = !desc_expand;
                        });

                        play.setOnClickListener(view -> {
                            CenterThreadPool.run(() -> {
                                try {
                                    LivePlayInfo.Codec codec;
                                    if (playInfo != null) {
                                        codec = playInfo.playUrl.stream.get(selectedProtocol).format.get(0).codec.get(0);
                                        LivePlayInfo.UrlInfo urlInfo = codec.url_info.get(selectedHost);
                                        String play_url = urlInfo.host + codec.base_url + urlInfo.extra;
                                        runOnUiThread(() -> PlayerApi.jumpToPlayer(this,play_url,"","直播·" + room.title,false,0,"",0,userInfo.mid,0,true));
                                    }
                                }catch (Exception e){
                                    runOnUiThread(() -> MsgUtil.err(e,this));
                                }
                            });
                        });
                        play.setOnLongClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(this, SettingPlayerChooseActivity.class);
                            startActivity(intent);
                            return true;
                        });

                        //协议选择
                        MediaEpisodeAdapter protocolAdapter = new MediaEpisodeAdapter();
                        protocolAdapter.setOnItemClickListener(index -> {
                            selectedProtocol = index;
                            refresh_host_list();
                        });
                        ArrayList<Bangumi.Episode> protocolList = new ArrayList<>();
                        for (int i = 0;i < playInfo.playUrl.stream.size();i++){
                            Bangumi.Episode episode = new Bangumi.Episode();
                            episode.id = i;
                            episode.title = playInfo.playUrl.stream.get(i).protocol_name;
                            protocolList.add(episode);
                        }
                        protocolAdapter.setData(protocolList);
                        protocol_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                        runOnUiThread(() -> protocol_list.setAdapter(protocolAdapter));

                        //路线选择
                        hostAdapter = new MediaEpisodeAdapter();
                        hostAdapter.setOnItemClickListener(index -> selectedHost = index);
                        hostAdapter.setData(new ArrayList<>());
                        host_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                        runOnUiThread(() -> host_list.setAdapter(hostAdapter));
                        refresh_host_list();
                    });
                }catch (Exception e){
                    runOnUiThread(() -> MsgUtil.err(e,this));
                }
            });
        });
    }

    private void refresh_host_list(){
        ArrayList<Bangumi.Episode> hostList = new ArrayList<>();
        for (int i = 0;i < playInfo.playUrl.stream.get(selectedProtocol).format.get(0).codec.get(0).url_info.size();i++){
            Bangumi.Episode episode = new Bangumi.Episode();
            episode.id = i;
            episode.title = "源" + i;
            hostList.add(episode);
        }
        hostAdapter.setData(hostList);
    }
}
