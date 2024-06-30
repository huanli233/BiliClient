package com.RobinNotBad.BiliClient.activity.video.info;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.collection.CollectionInfoActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.send.SendDynamicActivity;
import com.RobinNotBad.BiliClient.activity.search.SearchActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.activity.user.WatchLaterActivity;
import com.RobinNotBad.BiliClient.activity.video.MultiPageActivity;
import com.RobinNotBad.BiliClient.activity.video.QualityChooserActivity;
import com.RobinNotBad.BiliClient.adapter.user.UpListAdapter;
import com.RobinNotBad.BiliClient.api.BangumiApi;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.api.HistoryApi;
import com.RobinNotBad.BiliClient.api.LikeCoinFavApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
import com.RobinNotBad.BiliClient.api.WatchLaterApi;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.RobinNotBad.BiliClient.view.RadiusBackgroundSpan;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//真正的视频详情页
//2023-07-17

public class VideoInfoFragment extends Fragment {
    private VideoInfo videoInfo;

    private TextView description;
    private TextView tagsText;
    private ImageButton fav;
    private Pair<Long, Integer> progressPair;
    private boolean play_clicked = false;

    private Boolean clickCoverPlayEnable = SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.cover_play_enable, false);

    final int RESULT_ADDED = 1;
    final int RESULT_DELETED = -1;

    private boolean desc_expand = false, tags_expand = false;
    final ActivityResultLauncher<Intent> favLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            int code = o.getResultCode();
            if (code == RESULT_ADDED) {
                fav.setBackgroundResource(R.drawable.icon_favourite_1);
            } else if (code == RESULT_DELETED) {
                fav.setBackgroundResource(R.drawable.icon_favourite_0);
            }
        }
    });

    // 其实我不会用，也是抄的上面的😡
    final ActivityResultLauncher<Intent> writeDynamicLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            int code = result.getResultCode();
            Intent data = result.getData();
            if (code == RESULT_OK && data != null) {
                String text = data.getStringExtra("text");
                CenterThreadPool.run(() -> {
                    try {
                        long dynId;
                        Map<String, Long> atUids = new HashMap<>();
                        Pattern pattern = Pattern.compile("@(\\S+)\\s");
                        Matcher matcher = pattern.matcher(text);
                        while (matcher.find()) {
                            String matchedString = matcher.group(1);
                            long uid;
                            if ((uid = DynamicApi.mentionAtFindUser(matchedString)) != -1) {
                                atUids.put(matchedString, uid);
                            }
                        }
                        dynId = DynamicApi.relayVideo(text, (atUids.isEmpty() ? null : atUids), videoInfo.aid);
                        if (dynId != -1) {
                            if (isAdded())
                                requireActivity().runOnUiThread(() -> MsgUtil.showMsg("转发成功~", requireContext()));
                        } else {
                            if (isAdded())
                                requireActivity().runOnUiThread(() -> MsgUtil.showMsg("转发失败", requireContext()));
                        }
                    } catch (Exception e) {
                        if (isAdded())
                            requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
                    }
                });
            }
        }
    });

    public VideoInfoFragment() {
    }


    public static VideoInfoFragment newInstance(VideoInfo videoInfo) {
        VideoInfoFragment fragment = new VideoInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("videoInfo", videoInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoInfo = (VideoInfo) getArguments().getSerializable("videoInfo");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_info, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView cover = view.findViewById(R.id.cover);
        TextView title = view.findViewById(R.id.title);
        description = view.findViewById(R.id.description);
        tagsText = view.findViewById(R.id.tags);
        MaterialCardView exclusiveTip = view.findViewById(R.id.exclusiveTip);
        RecyclerView up_recyclerView = view.findViewById(R.id.up_recyclerView);
        TextView exclusiveTipLabel = view.findViewById(R.id.exclusiveTipLabel);
        TextView viewCount = view.findViewById(R.id.viewsCount);
        TextView timeText = view.findViewById(R.id.timeText);
        TextView durationText = view.findViewById(R.id.durationText);
        MaterialButton play = view.findViewById(R.id.play);
        MaterialButton addWatchlater = view.findViewById(R.id.addWatchlater);
        MaterialButton download = view.findViewById(R.id.download);
        MaterialButton relay = view.findViewById(R.id.relay);
        TextView bvidText = view.findViewById(R.id.bvidText);
        TextView danmakuCount = view.findViewById(R.id.danmakuCount);
        ImageButton like = view.findViewById(R.id.btn_like);
        ImageButton coin = view.findViewById(R.id.btn_coin);
        TextView likeLabel = view.findViewById(R.id.like_label);
        TextView coinLabel = view.findViewById(R.id.coin_label);
        TextView favLabel = view.findViewById(R.id.fav_label);
        MaterialCardView collectionCard = view.findViewById(R.id.collection);
        TextView collectionTitle = view.findViewById(R.id.collectionText);
        fav = view.findViewById(R.id.btn_fav);

        if (videoInfo.epid != -1) { //不是空的的话就应该跳转到番剧页面了
            CenterThreadPool.run(() -> {
                Intent intent = new Intent(requireContext(), VideoInfoActivity.class);
                intent.putExtra("type", "media");
                intent.putExtra("aid", BangumiApi.getMdidFromEpid(videoInfo.epid));
                requireActivity().runOnUiThread(() -> {
                    startActivity(intent);
                    requireActivity().finish();
                });
            });
        }

        //历史上报
        CenterThreadPool.run(() -> {
            try {
                progressPair = VideoInfoApi.getWatchProgress(videoInfo.aid);
                if (progressPair.first == null || !videoInfo.cids.contains(progressPair.first))
                    progressPair = new Pair<>(videoInfo.cids.get(0), 0);

                HistoryApi.reportHistory(videoInfo.aid, progressPair.first, videoInfo.staff.get(0).mid, progressPair.second);
                //历史记录接口，如果没有记录过该视频，会返回历史记录的最后一项，神奇吧
            } catch (Exception e) {
                if (isAdded())
                    requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
            }
        });

        //标签
        if (SharedPreferencesUtil.getBoolean("tags_enable", true)) {
            CenterThreadPool.run(() -> {
                try {
                    String tags;
                    if (videoInfo.bvid == null || videoInfo.bvid.isEmpty())
                        tags = VideoInfoApi.getTagsByAid(videoInfo.aid);
                    else tags = VideoInfoApi.getTagsByBvid(videoInfo.bvid);
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        SpannableStringBuilder tag_str = new SpannableStringBuilder("标签：");
                        for (String str : tags.split("/")) {
                            int old_len = tag_str.length();
                            tag_str.append(str).append("/");
                            tag_str.setSpan(new ClickableSpan() {
                                @Override
                                public void onClick(View arg0) {
                                    Intent intent = new Intent(requireContext(), SearchActivity.class);
                                    intent.putExtra("keyword", str);
                                    requireContext().startActivity(intent);
                                }

                                @Override
                                public void updateDrawState(TextPaint ds) {
                                    super.updateDrawState(ds);
                                    ds.setUnderlineText(false);
                                    ds.setColor(Color.parseColor("#03a9f4"));
                                }
                            }, old_len, tag_str.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        }
                        tagsText.setMovementMethod(LinkMovementMethod.getInstance());
                        tagsText.setText("标签：" + tags);
                        tagsText.setOnClickListener(view1 -> {
                            if (tags_expand) {
                                tagsText.setMaxLines(1);
                                tagsText.setText("标签：" + tags);
                            } else {
                                tagsText.setMaxLines(233);
                                tagsText.setText(tag_str);
                            }
                            tags_expand = !tags_expand;
                        });
                    });
                } catch (Exception e) {
                    if (isAdded())
                        requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
                }
            });
        } else tagsText.setVisibility(View.GONE);

        CenterThreadPool.run(() -> {
            try {
                videoInfo.stats.coined = LikeCoinFavApi.getCoined(videoInfo.aid);
                videoInfo.stats.liked = LikeCoinFavApi.getLiked(videoInfo.aid);
                videoInfo.stats.favoured = LikeCoinFavApi.getFavoured(videoInfo.aid);
                videoInfo.stats.allow_coin = (videoInfo.copyright == VideoInfo.COPYRIGHT_REPRINT) ? 1 : 2;
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    if (videoInfo.stats.coined != 0)
                        coin.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_coin_1));
                    if (videoInfo.stats.liked)
                        like.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_like_1));
                    if (videoInfo.stats.favoured)
                        fav.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_favourite_1));
                });
            } catch (Exception e) {
                if (isAdded())
                    requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
            }
        });

        ToolsUtil.setCopy(title, requireContext(), videoInfo.title);

        if (!videoInfo.argueMsg.isEmpty()) {
            exclusiveTipLabel.setText(videoInfo.argueMsg);
            exclusiveTip.setVisibility(View.VISIBLE);
        }

        if (isAdded()) requireActivity().runOnUiThread(() -> {
            UpListAdapter adapter = new UpListAdapter(requireContext(), videoInfo.staff);
            up_recyclerView.setHasFixedSize(true);
            up_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            up_recyclerView.setAdapter(adapter);
        }); //加载UP主

        title.setText(getTitleSpan());

        Glide.with(requireContext()).asDrawable().load(GlideUtil.url(videoInfo.cover)).placeholder(R.mipmap.placeholder)
                .transition(GlideUtil.getTransitionOptions())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4, requireContext()))).sizeMultiplier(0.85f).skipMemoryCache(true).dontAnimate())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(cover);

        cover.setOnClickListener(view1 -> {
            if (SharedPreferencesUtil.getString("player", null) == null) {
                SharedPreferencesUtil.putBoolean(SharedPreferencesUtil.cover_play_enable, true);
                Toast.makeText(requireContext(), "将播放视频, 如需变更点击行为请至设置->偏好设置喵", Toast.LENGTH_SHORT).show();
                clickCoverPlayEnable = true;
            }
            if (clickCoverPlayEnable) {
                play();
                return;
            }
            Intent intent = new Intent();
            intent.setClass(view1.getContext(), ImageViewerActivity.class);
            ArrayList<String> imageList = new ArrayList<>();
            imageList.add(videoInfo.cover);
            intent.putExtra("imageList", imageList);
            view1.getContext().startActivity(intent);
        });

        viewCount.setText(ToolsUtil.toWan(videoInfo.stats.view));
        likeLabel.setText(ToolsUtil.toWan(videoInfo.stats.like));
        coinLabel.setText(ToolsUtil.toWan(videoInfo.stats.coin));
        favLabel.setText(ToolsUtil.toWan(videoInfo.stats.favorite));

        danmakuCount.setText(String.valueOf(videoInfo.stats.danmaku));
        bvidText.setText(videoInfo.bvid);
        timeText.setText(videoInfo.timeDesc);
        durationText.setText(videoInfo.duration);

        description.setText(videoInfo.description);
        description.setOnClickListener(view1 -> {
            if (desc_expand) description.setMaxLines(3);
            else description.setMaxLines(512);
            desc_expand = !desc_expand;
        });
        ToolsUtil.setLink(description);
        ToolsUtil.setAtLink(videoInfo.descAts, description);

        ToolsUtil.setCopy(description, requireContext());
        ToolsUtil.setCopy(bvidText, requireContext());

        play.setOnClickListener(view1 -> play());
        play.setOnLongClickListener(view1 -> {
            Intent intent = new Intent();
            intent.setClass(requireContext(), SettingPlayerChooseActivity.class);
            startActivity(intent);
            return true;
        });

        like.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
            if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                requireActivity().runOnUiThread(() -> MsgUtil.showMsg("还没有登录喵~", requireContext()));
                return;
            }
            try {
                int result = LikeCoinFavApi.like(videoInfo.aid, (videoInfo.stats.liked ? 2 : 1));
                if (result == 0) {
                    videoInfo.stats.liked = !videoInfo.stats.liked;
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        MsgUtil.showMsg((videoInfo.stats.liked ? "点赞成功" : "取消成功"), requireContext());

                        if (videoInfo.stats.liked)
                            likeLabel.setText(ToolsUtil.toWan(++videoInfo.stats.like));
                        else likeLabel.setText(ToolsUtil.toWan(--videoInfo.stats.like));
                        like.setBackground(ContextCompat.getDrawable(requireContext(), (videoInfo.stats.liked ? R.drawable.icon_like_1 : R.drawable.icon_like_0)));
                    });
                } else if (isAdded()) {
                    String msg = "操作失败：" + result;
                    switch (result) {
                        case -403:
                            msg = "当前请求触发B站风控";
                            break;
                    }
                    String finalMsg = msg;
                    requireActivity().runOnUiThread(() -> MsgUtil.showMsg(finalMsg, requireContext()));
                }
            } catch (Exception e) {
                if (isAdded())
                    requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
            }
        }));

        coin.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
            if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                requireActivity().runOnUiThread(() -> MsgUtil.showMsg("还没有登录喵~", requireContext()));
                return;
            }
            if (videoInfo.stats.coined < videoInfo.stats.allow_coin) {
                try {
                    int result = LikeCoinFavApi.coin(videoInfo.aid, 1);
                    if (result == 0) {
                        videoInfo.stats.coined++;
                        if (isAdded()) requireActivity().runOnUiThread(() -> {
                            MsgUtil.showMsg("投币成功", requireContext());
                            coinLabel.setText(ToolsUtil.toWan(++videoInfo.stats.coin));
                            coin.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_coin_1));
                        });
                    } else if (isAdded()) {
                        String msg = "投币失败：" + result;
                        switch (result) {
                            case -403:
                                msg = "当前请求触发B站风控";
                                break;
                        }
                        String finalMsg = msg;
                        requireActivity().runOnUiThread(() -> MsgUtil.showMsg(finalMsg, requireContext()));
                    }
                } catch (Exception e) {
                    if (isAdded())
                        requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
                }
            } else {
                if (isAdded())
                    requireActivity().runOnUiThread(() -> MsgUtil.showMsg("投币数量到达上限", requireContext()));
            }
        }));

        fav.setOnClickListener(view1 -> {
            Intent intent = new Intent();
            intent.setClass(requireContext(), AddFavoriteActivity.class);
            intent.putExtra("aid", videoInfo.aid);
            intent.putExtra("bvid", videoInfo.bvid);
            favLauncher.launch(intent);
        });


        addWatchlater.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
            try {
                int result = WatchLaterApi.add(videoInfo.aid);
                if (result == 0)
                    requireActivity().runOnUiThread(() -> MsgUtil.showMsg("添加成功", requireContext()));
                else
                    requireActivity().runOnUiThread(() -> MsgUtil.showMsg("添加失败，错误码：" + result, requireContext()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        addWatchlater.setOnLongClickListener(view1 -> {
            Intent intent = new Intent();
            intent.setClass(requireContext(), WatchLaterActivity.class);
            startActivity(intent);
            return true;
        });

        download.setOnClickListener(view1 -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            } else {
                File downPath = new File(ConfInfoApi.getDownloadPath(requireContext()), ToolsUtil.stringToFile(videoInfo.title));

                if (downPath.exists() && videoInfo.pagenames.size() == 1)
                    MsgUtil.showMsg("已经缓存过了~", requireContext());
                else {
                    if (videoInfo.pagenames.size() > 1) {
                        Intent intent = new Intent();
                        intent.setClass(requireContext(), MultiPageActivity.class);
                        intent.putExtra("download", 1);
                        intent.putExtra("videoInfo", videoInfo);
                        startActivity(intent);
                    } else {
                        startActivity(new Intent().putExtra("videoInfo", videoInfo).putExtra("page", 0).setClass(requireContext(), QualityChooserActivity.class));
                    }
                }
            }
        });

        relay.setOnClickListener((view1) -> {
            Intent intent = new Intent();
            intent.setClass(requireContext(), SendDynamicActivity.class).putExtra("video", videoInfo);
            writeDynamicLauncher.launch(intent);
        });

        if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
            addWatchlater.setVisibility(View.GONE);
            relay.setVisibility(View.GONE);
        }

        if (videoInfo.collection != null) {
            collectionTitle.setText(String.format("合集 · %s", videoInfo.collection.title));
            collectionCard.setOnClickListener((view1) ->
                    startActivity(new Intent(requireContext(), CollectionInfoActivity.class)
                            .putExtra("collection", videoInfo.collection)
                            .putExtra("fromVideo", videoInfo.aid)));
        } else {
            collectionCard.setVisibility(View.GONE);
        }
    }


    private SpannableString getTitleSpan() {
        String string = "";

        //优先级要对
        if (videoInfo.upowerExclusive) string = "充电专属";
        else if (videoInfo.isSteinGate) string = "互动视频";
        else if (videoInfo.is360) string = "全景视频";
        else if (videoInfo.isCooperation) string = "联合投稿";

        if (string.isEmpty()) return new SpannableString(videoInfo.title);

        SpannableString titleStr = new SpannableString(" " + string + " " + videoInfo.title);
        RadiusBackgroundSpan badgeBG = new RadiusBackgroundSpan(0, (int) getResources().getDimension(R.dimen.card_round), Color.WHITE, Color.rgb(207, 75, 95));
        titleStr.setSpan(badgeBG, 0, string.length() + 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return titleStr;
    }

    private void play() {
        Glide.get(requireContext()).clearMemory();
        //在播放前清除内存缓存，因为手表内存太小了，播放完回来经常把Activity全释放掉
        //...经过测试，还是会释放，但会好很多
        if (videoInfo.pagenames.size() > 1) {
            Intent intent = new Intent()
                    .setClass(requireContext(), MultiPageActivity.class)
                    .putExtra("videoInfo", videoInfo)
                    .putExtra("progress_cid", progressPair.first)
                    .putExtra("progress", (play_clicked ? -1 : progressPair.second));
            //这里也会传过去，如果后面选择当页就不再获取直接传，选择其他页就传-1剩下的交给解析页
            startActivity(intent);
        } else {
            PlayerApi.startGettingUrl(requireContext(), videoInfo, 0, (progressPair == null ? 0 : play_clicked ? -1 : progressPair.second));
            //避免重复获取的同时保证播放进度是新的，如果是-1会在解析页里再获取一次
        }
        play_clicked = true;
    }
}