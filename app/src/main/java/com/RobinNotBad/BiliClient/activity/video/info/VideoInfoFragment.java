package com.RobinNotBad.BiliClient.activity.video.info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.activity.user.WatchLaterActivity;
import com.RobinNotBad.BiliClient.activity.video.MultiPageActivity;
import com.RobinNotBad.BiliClient.adapter.UpListAdapter;
import com.RobinNotBad.BiliClient.api.BangumiApi;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
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

//真正的视频详情页
//2023-07-17

public class VideoInfoFragment extends Fragment {

    private VideoInfo videoInfo;

    private TextView description;
    private TextView tagsText;
    private ImageButton fav;

    int RESULT_ADDED = 1;
    int RESULT_DELETED = -1;

    private boolean desc_expand = false, tags_expand = false;
    ActivityResultLauncher<Intent> favLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            int code = o.getResultCode();
            if (code == RESULT_ADDED){
                fav.setBackgroundResource(R.drawable.icon_favourite_1);
            }
            if (code == RESULT_DELETED){
                fav.setBackgroundResource(R.drawable.icon_favourite_0);
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
        TextView bvidText = view.findViewById(R.id.bvidText);
        TextView danmakuCount = view.findViewById(R.id.danmakuCount);
        ImageButton like = view.findViewById(R.id.btn_like);
        ImageButton coin = view.findViewById(R.id.btn_coin);
        TextView likeLabel = view.findViewById(R.id.like_label);
        TextView coinLabel = view.findViewById(R.id.coin_label);
        TextView favLabel = view.findViewById(R.id.fav_label);
        fav = view.findViewById(R.id.btn_fav);

        if(videoInfo.epid != -1){ //不是空的的话就应该跳转到番剧页面了
            CenterThreadPool.run(() -> {
                Intent intent = new Intent(requireContext(), VideoInfoActivity.class);
                intent.putExtra("type","media");
                intent.putExtra("aid", BangumiApi.getMdidFromEpid(videoInfo.epid));
                requireActivity().runOnUiThread(() -> {
                    startActivity(intent);
                    requireActivity().finish();
                });
            });
        }

        CenterThreadPool.run(() -> {
            try {
                HistoryApi.reportHistory(videoInfo.aid, videoInfo.cids.get(0), videoInfo.staff.get(0).mid, 0);
            } catch (Exception e) {
                if (isAdded())
                    requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
            }
        });


        if (SharedPreferencesUtil.getBoolean("tags_enable", true)) {
            CenterThreadPool.run(()->{
                try {
                    String tags;
                    if (videoInfo.bvid == null || videoInfo.bvid.isEmpty())
                        tags = VideoInfoApi.getTagsByAid(videoInfo.aid);
                    else tags = VideoInfoApi.getTagsByBvid(videoInfo.bvid);
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        tagsText.setText("标签：" + tags);
                        tagsText.setOnClickListener(view1 -> {
                            if (tags_expand) tagsText.setMaxLines(1);
                            else tagsText.setMaxLines(233);
                            tags_expand = !tags_expand;
                        });
                    });
                } catch (Exception e) {if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));}
            });
        } else tagsText.setVisibility(View.GONE);

        CenterThreadPool.run(()->{
            try {
                videoInfo.stats.coined = LikeCoinFavApi.getCoined(videoInfo.aid);
                videoInfo.stats.liked = LikeCoinFavApi.getLiked(videoInfo.aid);
                videoInfo.stats.favoured = LikeCoinFavApi.getFavoured(videoInfo.aid);
                videoInfo.stats.allow_coin = 2;
                if(isAdded()) requireActivity().runOnUiThread(()->{
                    if(videoInfo.stats.coined!=0) coin.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_coin_1));
                    if(videoInfo.stats.liked) like.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_like_1));
                    if(videoInfo.stats.favoured) fav.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_favourite_1));
                });
            } catch (Exception e) {if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));}
        });

        ToolsUtil.setCopy(title,requireContext(),videoInfo.title);

        if(!videoInfo.argueMsg.isEmpty()){
            exclusiveTipLabel.setText(videoInfo.argueMsg);
            exclusiveTip.setVisibility(View.VISIBLE);
        }

        if (isAdded()) requireActivity().runOnUiThread(() -> {
            UpListAdapter adapter = new UpListAdapter(requireContext(),videoInfo.staff);
            up_recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            up_recyclerView.setAdapter(adapter);
        }); //加载UP主

        title.setText(getTitleSpan());

        Glide.with(requireContext()).load(GlideUtil.url(videoInfo.cover)).placeholder(R.mipmap.placeholder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(4, requireContext()))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(cover);

        cover.setOnClickListener(view1 -> {
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

        ToolsUtil.setCopy(description,requireContext());
        ToolsUtil.setCopy(bvidText,requireContext());


        play.setOnClickListener(view1 -> {
            Glide.get(requireContext()).clearMemory();
            //在播放前清除内存缓存，因为手表内存太小了，播放完回来经常把Activity全释放掉
            //...经过测试，还是会释放，但会好很多
            if (videoInfo.pagenames.size() > 1) {
                Intent intent = new Intent()
                        .setClass(requireContext(), MultiPageActivity.class)
                        .putExtra("videoInfo", videoInfo);
                startActivity(intent);
            } else {
                PlayerApi.startGettingUrl(requireContext(), videoInfo, 0);
            }
        });
        play.setOnLongClickListener(view1 -> {
            Intent intent = new Intent();
            intent.setClass(requireContext(), SettingPlayerChooseActivity.class);
            startActivity(intent);
            return true;
        });

        like.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
            try {
                int result = LikeCoinFavApi.like(videoInfo.aid, (videoInfo.stats.liked ? 2 : 1));
                if (result == 0) {
                    videoInfo.stats.liked = !videoInfo.stats.liked;
                    if(isAdded()) requireActivity().runOnUiThread(() -> {
                        MsgUtil.toast((videoInfo.stats.liked ? "点赞成功" : "取消成功"), requireContext());

                        if(videoInfo.stats.liked) likeLabel.setText(ToolsUtil.toWan(++videoInfo.stats.like));
                        else likeLabel.setText(ToolsUtil.toWan(--videoInfo.stats.like));
                        like.setBackground(ContextCompat.getDrawable(requireContext(), (videoInfo.stats.liked ? R.drawable.icon_like_1 : R.drawable.icon_like_0)));
                    });
                } else if(isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.toast("操作失败：" + result, requireContext()));
            } catch (Exception e) {
                if(isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
            }
        }));

        coin.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
            if (videoInfo.stats.coined < videoInfo.stats.allow_coin) {
                try {
                    int result = LikeCoinFavApi.coin(videoInfo.aid, 1);
                    if (result == 0) {
                        videoInfo.stats.coined++;
                        if(isAdded()) requireActivity().runOnUiThread(() -> {
                            MsgUtil.toast("投币成功", requireContext());
                            coinLabel.setText(ToolsUtil.toWan(++videoInfo.stats.coin));
                            coin.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_coin_1));
                        });
                    } else if(isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.toast("投币失败："+result,requireContext()));
                } catch (Exception e) {
                    if(isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
                }
            } else {
                if(isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.toast("投币数量到达上限", requireContext()));
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
                    requireActivity().runOnUiThread(() -> MsgUtil.toast("添加成功",requireContext()));
                else
                    requireActivity().runOnUiThread(() -> MsgUtil.toast("添加失败，错误码：" + result, requireContext()));
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
                    MsgUtil.toast("已经缓存过了~", requireContext());
                else {
                    if (videoInfo.pagenames.size() > 1) {
                        Intent intent = new Intent();
                        intent.setClass(requireContext(), MultiPageActivity.class);
                        intent.putExtra("download", 1);
                        intent.putExtra("videoInfo", videoInfo);
                        startActivity(intent);
                    } else {
                        PlayerApi.startDownloadingVideo(requireContext(), videoInfo, 0);
                    }
                }
            }
        });
    }


    private SpannableString getTitleSpan(){
        String string = "";
        if(videoInfo.isCooperation) string = "联合投稿";
        else if (videoInfo.isSteinGate) string = "互动视频";
        else if(videoInfo.is360) string = "全景视频";
        else if(videoInfo.upowerExclusive) string = "充电专属";

        if(string.isEmpty()) return new SpannableString(videoInfo.title);

        SpannableString titleStr = new SpannableString(" " + string + " " + videoInfo.title);
        RadiusBackgroundSpan badgeBG = new RadiusBackgroundSpan(0, (int) getResources().getDimension(R.dimen.card_round),Color.WHITE,Color.argb(0xfb,0xfb,0x87,0x99));
        titleStr.setSpan(badgeBG, 0, string.length()+2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return titleStr;
    }
}