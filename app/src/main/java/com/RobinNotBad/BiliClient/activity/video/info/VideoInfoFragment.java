package com.RobinNotBad.BiliClient.activity.video.info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerActivity;
import com.RobinNotBad.BiliClient.activity.user.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.user.WatchLaterActivity;
import com.RobinNotBad.BiliClient.activity.user.favorite.AddFavoriteActivity;
import com.RobinNotBad.BiliClient.activity.video.JumpToPlayerActivity;
import com.RobinNotBad.BiliClient.activity.video.MultiPageActivity;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.HistoryApi;
import com.RobinNotBad.BiliClient.api.LikeCoinFavApi;
import com.RobinNotBad.BiliClient.api.WatchLaterApi;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ArrayList;

//真正的视频详情页
//2023-07-17

public class VideoInfoFragment extends Fragment {

    private VideoInfo videoInfo;

    private ImageView cover, upIcon;
    private TextView title, description, tags, upName, views, timeText, durationText, bvidText, danmakuCount;
    private ImageButton fav;

    private boolean desc_expand = false, tags_expand = false;
    ActivityResultLauncher<Intent> favoriteActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if(o.getResultCode() == Activity.RESULT_OK){
                fav.setBackgroundResource(R.drawable.icon_favourite_1);
            }
        }
    });

    public VideoInfoFragment() {
    }


    public static VideoInfoFragment newInstance(VideoInfo videoInfo) {
        VideoInfoFragment fragment = new VideoInfoFragment();
        Bundle args = new Bundle();
        args.putParcelable("videoInfo", videoInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoInfo = getArguments().getParcelable("videoInfo");
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

        cover = view.findViewById(R.id.cover);
        upIcon = view.findViewById(R.id.upInfo_Icon);
        title = view.findViewById(R.id.title);
        description = view.findViewById(R.id.description);
        tags = view.findViewById(R.id.tags);
        upName = view.findViewById(R.id.upInfo_Name);
        views = view.findViewById(R.id.viewsCount);
        timeText = view.findViewById(R.id.timeText);
        durationText = view.findViewById(R.id.durationText);
        MaterialCardView play = view.findViewById(R.id.play);
        MaterialCardView addWatchlater = view.findViewById(R.id.addWatchlater);
        MaterialCardView upCard = view.findViewById(R.id.upInfo);
        MaterialCardView addFavorite = view.findViewById(R.id.addFavorite);
        MaterialCardView download = view.findViewById(R.id.download);
        MaterialCardView ai_summary = view.findViewById(R.id.ai_summary);
        bvidText = view.findViewById(R.id.bvidText);
        danmakuCount = view.findViewById(R.id.danmakuCount);
        ImageButton like = view.findViewById(R.id.btn_like);
        ImageButton coin = view.findViewById(R.id.btn_coin);
        fav = view.findViewById(R.id.btn_fav);

        if (!SharedPreferencesUtil.getBoolean("tags_enable", true)) tags.setVisibility(View.GONE);

        CenterThreadPool.run(() -> {
            try {

                HistoryApi.reportHistory(videoInfo.aid, videoInfo.cids.get(0), videoInfo.upMid, 0);

                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    Glide.with(view.getContext()).load(videoInfo.cover).placeholder(R.drawable.placeholder)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(LittleToolsUtil.dp2px(4, view.getContext()))))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(cover);
                    Glide.with(view.getContext()).load(videoInfo.upAvatar).placeholder(R.drawable.akari)
                            .apply(RequestOptions.circleCropTransform())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(upIcon);
                    upName.setText(videoInfo.upName);

                    cover.setOnClickListener(view1 -> {
                        Intent intent = new Intent();
                        intent.setClass(view1.getContext(), ImageViewerActivity.class);
                        ArrayList<String> imageList = new ArrayList<>();
                        imageList.add(videoInfo.cover);
                        intent.putExtra("imageList", imageList);
                        view1.getContext().startActivity(intent);
                    });
                            
                    int viewsInt = videoInfo.view;
                    String viewsStr;
                    if (viewsInt >= 10000)
                        viewsStr = String.format(Locale.CHINA, "%.1f", (float) viewsInt / 10000) + "万观看";
                    else viewsStr = viewsInt + "观看";
                    views.setText(viewsStr);

                    danmakuCount.setText(String.valueOf(videoInfo.danmaku));
                    bvidText.setText(videoInfo.bvid);
                    description.setText(videoInfo.description);
                    tags.setText("标签：" + videoInfo.tagsDesc);
                    title.setText(videoInfo.title);
                    timeText.setText(videoInfo.timeDesc);
                    durationText.setText(videoInfo.duration);

                    description.setOnClickListener(view1 -> {
                        if (desc_expand) description.setMaxLines(3);
                        else description.setMaxLines(512);
                        desc_expand = !desc_expand;
                    });

                    if(SharedPreferencesUtil.getBoolean("copy_enable", true)){
                        description.setOnLongClickListener(view1 -> {
                            ClipboardManager clipboardManager = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText("label",videoInfo.description);
                            clipboardManager.setPrimaryClip(clipData);
                            MsgUtil.toast("已复制简介",requireContext());
                            return false;
                        });
                    }

                    tags.setOnClickListener(view1 -> {
                        if (tags_expand) tags.setMaxLines(1);
                        else tags.setMaxLines(512);
                        tags_expand = !tags_expand;
                    });

                    play.setOnClickListener(view1 -> {
                        Glide.get(requireContext()).clearMemory();
                        //在播放前清除内存缓存，因为手表内存太小了，播放完回来经常把Activity全释放掉
                        //...经过测试，还是会释放，但会好很多

                        Intent intent = new Intent();
                        if (videoInfo.pagenames.size() > 1) {
                            intent.setClass(view.getContext(), MultiPageActivity.class);
                            intent.putExtra("mid", videoInfo.upMid);
                            intent.putExtra("cids", videoInfo.cids);
                            intent.putExtra("pages", videoInfo.pagenames);
                        } else {
                            intent.setClass(view.getContext(), JumpToPlayerActivity.class);
                            intent.putExtra("cid", videoInfo.cids.get(0));
                            intent.putExtra("title", videoInfo.title);
                        }
                        intent.putExtra("bvid", videoInfo.bvid);
                        intent.putExtra("aid", videoInfo.aid);
                        startActivity(intent);
                    });
                    play.setOnLongClickListener(view1 -> {
                        Intent intent = new Intent();
                        intent.setClass(view.getContext(), SettingPlayerActivity.class);
                        startActivity(intent);
                        return true;
                    });
                    like.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                        try {
                            int result = LikeCoinFavApi.like(videoInfo.aid, 1);
                            if (result == 0) {
                                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "点赞成功", Toast.LENGTH_SHORT).show());
                                like.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_like_1));
                                like.setOnClickListener(view2 -> CenterThreadPool.run(() -> Toast.makeText(requireContext(), "暂未完成", Toast.LENGTH_SHORT).show()));
                            } else
                                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "点赞失败，错误码：" + result, Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));
                    coin.setOnClickListener(view1 ->CenterThreadPool.run(() -> {
                        try {
                            int result = LikeCoinFavApi.coin(videoInfo.aid, 1);
                            if (result == 0) {
                                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "投币成功,长按可投2币", Toast.LENGTH_SHORT).show());
                                like.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_coin_1));
                                like.setOnClickListener(view2 -> CenterThreadPool.run(() -> Toast.makeText(requireContext(), "暂未完成", Toast.LENGTH_SHORT).show()));
                            } else
                                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "投币失败，错误码：" + result, Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));
                    fav.setOnClickListener(view1 -> {
                        Intent intent = new Intent();
                        intent.setClass(view.getContext(), AddFavoriteActivity.class);
                        intent.putExtra("aid", videoInfo.aid);
                        intent.putExtra("bvid", videoInfo.bvid);
                        favoriteActivityLauncher.launch(intent);
                    });
                    addWatchlater.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                        try {
                            int result = WatchLaterApi.add(videoInfo.aid);
                            if (result == 0)
                                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "添加成功", Toast.LENGTH_SHORT).show());
                            else
                                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "添加失败，错误码：" + result, Toast.LENGTH_SHORT).show());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));
                    addWatchlater.setOnLongClickListener(view1 -> {
                        Intent intent = new Intent();
                        intent.setClass(view.getContext(), WatchLaterActivity.class);
                        startActivity(intent);
                        return true;
                    });

                    addFavorite.setOnClickListener(view1 -> {
                        Intent intent = new Intent();
                        intent.setClass(view.getContext(), AddFavoriteActivity.class);
                        intent.putExtra("aid", videoInfo.aid);
                        intent.putExtra("bvid", videoInfo.bvid);
                        startActivity(intent);
                    });

                    download.setOnClickListener(view1 -> {
                        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                        } else {
                            File downPath = new File(ConfInfoApi.getDownloadPath(requireContext()), LittleToolsUtil.stringToFile(videoInfo.title));

                            if (downPath.exists() && videoInfo.pagenames.size() == 1)
                                MsgUtil.toast("已经缓存过了~", requireContext());
                            else {
                                Intent intent = new Intent();
                                intent.putExtra("download", 1);
                                intent.putExtra("cover", videoInfo.cover);
                                intent.putExtra("bvid", videoInfo.bvid);
                                intent.putExtra("aid", videoInfo.aid);

                                if (videoInfo.pagenames.size() > 1) {
                                    intent.setClass(view.getContext(), MultiPageActivity.class);
                                    intent.putExtra("cids", videoInfo.cids);
                                    intent.putExtra("pages", videoInfo.pagenames);
                                    intent.putExtra("title", videoInfo.title);
                                } else {
                                    intent.setClass(view.getContext(), JumpToPlayerActivity.class);
                                    intent.putExtra("cid", videoInfo.cids.get(0));
                                    intent.putExtra("title", videoInfo.title);
                                }
                                startActivity(intent);
                            }
                        }
                    });

                    ai_summary.setOnClickListener(view1 -> {
                        Intent intent = new Intent();
                        intent.setClass(view.getContext(), AiSummaryActivity.class);
                        intent.putExtra("aid", videoInfo.aid);
                        intent.putExtra("bvid", videoInfo.bvid);
                        intent.putExtra("cid",videoInfo.cids.get(0));
                        intent.putExtra("mid", videoInfo.upMid);
                        startActivity(intent);
                    });

                    upCard.setOnClickListener(view1 -> {
                        Intent intent = new Intent();
                        intent.setClass(view.getContext(), UserInfoActivity.class);
                        intent.putExtra("mid", videoInfo.upMid);
                        startActivity(intent);
                    });
                });
            } catch (IOException e) {
                if (isAdded())
                    requireActivity().runOnUiThread(() -> MsgUtil.quickErr(MsgUtil.err_net, view.getContext()));
                e.printStackTrace();
            }
        });
    }


}