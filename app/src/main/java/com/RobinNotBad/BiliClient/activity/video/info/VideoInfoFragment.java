package com.RobinNotBad.BiliClient.activity.video.info;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import com.RobinNotBad.BiliClient.activity.CopyTextActivity;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.user.WatchLaterActivity;
import com.RobinNotBad.BiliClient.activity.video.MultiPageActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.HistoryApi;
import com.RobinNotBad.BiliClient.api.LikeCoinFavApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.api.VideoInfoApi;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

//真正的视频详情页
//2023-07-17

public class VideoInfoFragment extends Fragment {

    private VideoInfo videoInfo;

    private ImageView cover, upIcon;
    private TextView title, description, tagsText, upName, views, timeText, durationText, bvidText, danmakuCount;
    private ImageButton fav;

    private boolean desc_expand = false, tags_expand = false,isLiked = false,isCoined = false,isFavourited= false;
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

        cover = view.findViewById(R.id.cover);
        upIcon = view.findViewById(R.id.upInfo_Icon);
        title = view.findViewById(R.id.title);
        description = view.findViewById(R.id.description);
        tagsText = view.findViewById(R.id.tags);
        upName = view.findViewById(R.id.upInfo_Name);
        views = view.findViewById(R.id.viewsCount);
        timeText = view.findViewById(R.id.timeText);
        durationText = view.findViewById(R.id.durationText);
        MaterialButton play = view.findViewById(R.id.play);
        MaterialButton addWatchlater = view.findViewById(R.id.addWatchlater);
        MaterialCardView upCard = view.findViewById(R.id.upInfo);
        MaterialButton addFavorite = view.findViewById(R.id.addFavorite);
        MaterialButton download = view.findViewById(R.id.download);
        MaterialCardView like_coin_fav = view.findViewById(R.id.like_coin_fav);
        bvidText = view.findViewById(R.id.bvidText);
        danmakuCount = view.findViewById(R.id.danmakuCount);
        ImageButton like = view.findViewById(R.id.btn_like);
        ImageButton coin = view.findViewById(R.id.btn_coin);
        fav = view.findViewById(R.id.btn_fav);

        if (SharedPreferencesUtil.getBoolean("like_coin_fav_enable", false)) like_coin_fav.setVisibility(View.VISIBLE);

        CenterThreadPool.run(() -> {

            try {
                HistoryApi.reportHistory(videoInfo.aid, videoInfo.cids.get(0), videoInfo.upMid, 0);
            } catch (Exception e) {if (isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e,requireContext()));}


            if (SharedPreferencesUtil.getBoolean("tags_enable", true)) {
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
                } catch (Exception e) {if(isAdded()) requireActivity().runOnUiThread(() -> MsgUtil.err(e,requireContext()));}
            } else requireActivity().runOnUiThread(() -> tagsText.setVisibility(View.GONE));

            if (isAdded()) requireActivity().runOnUiThread(() -> {
                Glide.with(requireContext()).load(videoInfo.cover + "@20q.webp").placeholder(R.mipmap.placeholder)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(LittleToolsUtil.dp2px(4, requireContext()))))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(cover);
                Glide.with(requireContext()).load(videoInfo.upAvatar + "@20q.webp").placeholder(R.mipmap.akari)
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
                title.setText(videoInfo.title);
                timeText.setText(videoInfo.timeDesc);
                durationText.setText(videoInfo.duration);

                description.setText(videoInfo.description);
                description.setOnClickListener(view1 -> {
                    if (desc_expand) description.setMaxLines(3);
                    else description.setMaxLines(512);
                    desc_expand = !desc_expand;
                });

                if (SharedPreferencesUtil.getBoolean("copy_enable", true)) {
                    description.setOnLongClickListener(view1 -> {
                        Intent intent = new Intent(requireContext(), CopyTextActivity.class);
                        intent.putExtra("content", videoInfo.description);
                        requireContext().startActivity(intent);
                        return false;
                    });
                    bvidText.setOnLongClickListener(view1 -> {
                        Intent intent = new Intent(requireContext(), CopyTextActivity.class);
                        intent.putExtra("content", videoInfo.bvid);
                        requireContext().startActivity(intent);
                        return false;
                    });
                }

                play.setOnClickListener(view1 -> {
                    Glide.get(requireContext()).clearMemory();
                    //在播放前清除内存缓存，因为手表内存太小了，播放完回来经常把Activity全释放掉
                    //...经过测试，还是会释放，但会好很多
                    if (videoInfo.pagenames.size() > 1) {
                        Intent intent = new Intent()
                                .setClass(requireContext(), MultiPageActivity.class)
                                .putExtra("videoInfo", (Serializable) videoInfo);
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
                coin.setOnClickListener(view1 -> CenterThreadPool.run(() -> {
                    try {
                        int result = LikeCoinFavApi.coin(videoInfo.aid, 1);
                        if (result == 0) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "投币成功,长按可投2币", Toast.LENGTH_SHORT).show());
                            coin.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.icon_coin_1));
                            coin.setOnClickListener(view2 -> CenterThreadPool.run(() -> Toast.makeText(requireContext(), "暂未完成", Toast.LENGTH_SHORT).show()));
                        } else
                            requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "投币失败，错误码：" + result, Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
                fav.setOnClickListener(view1 -> {
                    Intent intent = new Intent();
                    intent.setClass(requireContext(), AddFavoriteActivity.class);
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
                    intent.setClass(requireContext(), WatchLaterActivity.class);
                    startActivity(intent);
                    return true;
                });

                addFavorite.setOnClickListener(view1 -> {
                    Intent intent = new Intent();
                    intent.setClass(requireContext(), AddFavoriteActivity.class);
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
                            if (videoInfo.pagenames.size() > 1) {
                                Intent intent = new Intent();
                                intent.setClass(requireContext(), MultiPageActivity.class);
                                intent.putExtra("download", 1);
                                intent.putExtra("videoInfo", (Serializable) videoInfo);
                                startActivity(intent);
                            } else {
                                PlayerApi.startDownloadingVideo(requireContext(), videoInfo, 0);
                            }
                        }
                    }
                });

                upCard.setOnClickListener(view1 -> {
                    Intent intent = new Intent();
                    intent.setClass(requireContext(), UserInfoActivity.class);
                    intent.putExtra("mid", videoInfo.upMid);
                    startActivity(intent);
                });
            });
        });
    }


}