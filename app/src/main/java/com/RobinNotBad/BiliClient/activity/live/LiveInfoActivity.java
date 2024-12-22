package com.RobinNotBad.BiliClient.activity.live;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.adapter.user.UpListAdapter;
import com.RobinNotBad.BiliClient.adapter.video.MediaEpisodeAdapter;
import com.RobinNotBad.BiliClient.api.LiveApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.Bangumi;
import com.RobinNotBad.BiliClient.model.LivePlayInfo;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LiveInfoActivity extends BaseActivity {
    private long room_id;
    private LiveRoom room;
    private boolean desc_expand = false, tags_expand = false;

    private RecyclerView host_list;
    private int selectedHost = 0;
    private MediaEpisodeAdapter hostAdapter;
    private LivePlayInfo playInfo;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        room_id = getIntent().getLongExtra("room_id", 0);
        if (room_id == 0) {
            finish();
            return;
        }

        asyncInflate(R.layout.activity_live_info, (layoutView, id) -> {
            ImageView loading = findViewById(R.id.loading);
            View scrollView = findViewById(R.id.scrollView);
            ImageView cover = findViewById(R.id.img_cover);
            TextView title = findViewById(R.id.text_title);
            RecyclerView up_recyclerView = findViewById(R.id.up_recyclerView);
            TextView viewsCount = findViewById(R.id.viewsCount);
            MaterialButton play = findViewById(R.id.play);
            TextView timeText = findViewById(R.id.timeText);
            TextView idText = findViewById(R.id.idText);
            TextView tags = findViewById(R.id.tags);
            TextView description = findViewById(R.id.description);
            host_list = findViewById(R.id.host_list);
            RecyclerView quality_list = findViewById(R.id.quality_list);

            AnimationUtils.crossFade(loading, scrollView);
            TerminalContext.getInstance().getLiveInfoByRoomId(room_id).observe(this, (liveInfoResult)-> liveInfoResult.onSuccess((liveInfo) -> {
                room = liveInfo.getLiveRoom();
                UserInfo userInfo = liveInfo.getUserInfo();
                playInfo = liveInfo.getLivePlayInfo();

                Glide.with(this).asDrawable().load(GlideUtil.url(room.user_cover)).placeholder(R.mipmap.placeholder)
                        .transition(GlideUtil.getTransitionOptions())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4))).sizeMultiplier(0.85f).skipMemoryCache(true).dontAnimate())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(cover);

                cover.setOnClickListener((view) -> startActivity(new Intent(view.getContext(), ImageViewerActivity.class).putExtra("imageList", new ArrayList<>(List.of(room.user_cover)))));

                title.setText(ToolsUtil.removeHtml(room.title));

                ArrayList<UserInfo> upList = new ArrayList<>();
                if (userInfo != null) {
                    UserInfo displayUserInfo = new UserInfo(userInfo.mid, userInfo.name, userInfo.avatar, "主播", 0, 0, 6, false, "", 0, "", 0);
//                            displayUserInfo.vip_nickname_color = userInfo.vip_nickname_color;
                    upList.add(displayUserInfo);
                }
                UpListAdapter upListAdapter = new UpListAdapter(this, upList);
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
                ToolsUtil.setCopy(idText, tags, title);

                description.setText(ToolsUtil.removeHtml(ToolsUtil.htmlToString(room.description)));
                description.setOnClickListener(view1 -> {
                    if (desc_expand) description.setMaxLines(3);
                    else description.setMaxLines(512);
                    desc_expand = !desc_expand;
                });
                play.setOnClickListener(view -> CenterThreadPool.run(() -> {
                    try {
                        LivePlayInfo.Codec codec;
                        if (playInfo != null) {
                            codec = playInfo.playUrl.stream.get(0).format.get(0).codec.get(0);
                            LivePlayInfo.UrlInfo urlInfo = codec.url_info.get(selectedHost);
                            String play_url = urlInfo.host + codec.base_url + urlInfo.extra;
                            runOnUiThread(() -> {
                                try {
                                    long mid;
                                    try {
                                        mid = SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0);
                                    } catch (Throwable ignored) {
                                        mid = 0;
                                    }
                                    PlayerApi.jumpToPlayer(this, play_url, "","", "直播·" + room.title, false, room_id, "", 0, mid, 0, true);
                                } catch (ActivityNotFoundException e) {
                                    MsgUtil.showMsg("没有找到播放器，请检查是否安装");
                                } catch (Exception e) {
                                    MsgUtil.err(e);
                                }
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> MsgUtil.err(e));
                    }
                }));
                play.setOnLongClickListener(view -> {
                    if (!SharedPreferencesUtil.getString("player", "null").equals("terminalPlayer"))
                        MsgUtil.showMsgLong("若无法播放请更换为内置播放器");
                    Intent intent = new Intent();
                    intent.setClass(this, SettingPlayerChooseActivity.class);
                    startActivity(intent);
                    return true;
                });
                if (playInfo.playUrl == null) {
                    MsgUtil.showMsg("直播已结束");
                    play.setVisibility(View.GONE);
                } else {
                    //清晰度选择
                    MediaEpisodeAdapter qualityAdapter = new MediaEpisodeAdapter();
                    ArrayList<Bangumi.Episode> qualityList = new ArrayList<>();
                    qualityAdapter.setOnItemClickListener(index -> {
                        hostAdapter.setData(new ArrayList<>()); //先留空
                        play.setEnabled(false);
                        CenterThreadPool.run(() -> {
                            try {
                                playInfo = LiveApi.getRoomPlayInfo(room_id, (int) qualityList.get(index).id);
                                runOnUiThread(() -> {
                                    refresh_host_list();
                                    play.setEnabled(true);
                                });
                            } catch (Exception e) {
                                runOnUiThread(() -> MsgUtil.err(e));
                            }
                        });
                    });
                    for (Map.Entry<String, Integer> entry : LiveApi.QualityMap.entrySet()) {
                        Bangumi.Episode episode = new Bangumi.Episode();
                        episode.id = entry.getValue();
                        episode.title = entry.getKey();
                        qualityList.add(episode);
                    }
                    qualityAdapter.setData(qualityList);
                    quality_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    quality_list.setAdapter(qualityAdapter);
                    qualityAdapter.setSelectedItemIndex(0);

                    //路线选择
                    hostAdapter = new MediaEpisodeAdapter();
                    hostAdapter.setOnItemClickListener(i -> selectedHost = i);
                    hostAdapter.setData(new ArrayList<>());
                    host_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    runOnUiThread(() -> host_list.setAdapter(hostAdapter));
                    refresh_host_list();
                }
                if (!SharedPreferencesUtil.getString("player", "null").equals("terminalPlayer"))
                    MsgUtil.showMsgLong("直播可能只有内置播放器可以正常播放");

            }).onFailure((e) -> {
                runOnUiThread(() -> MsgUtil.showMsg("直播不存在"));
                CenterThreadPool.runOnUIThreadAfter(1, TimeUnit.MINUTES, () -> MsgUtil.err(e));
                finish();
            }));
        });
    }

    private void refresh_host_list() {
        ArrayList<Bangumi.Episode> hostList = new ArrayList<>();
        for (int i = 0; i < playInfo.playUrl.stream.get(0).format.get(0).codec.get(0).url_info.size(); i++) {
            Bangumi.Episode episode = new Bangumi.Episode();
            episode.id = i;
            episode.title = "路线" + (i + 1);
            hostList.add(episode);
        }
        hostAdapter.setData(hostList);
        selectedHost = 0;
        hostAdapter.setSelectedItemIndex(0);
    }

    @Override
    protected void onDestroy() {
        TerminalContext.getInstance().leaveDetailPage();
        super.onDestroy();
    }
}
