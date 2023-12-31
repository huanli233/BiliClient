package com.RobinNotBad.BiliClient.activity.article;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.user.UserInfoActivity;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;

public class ArticleInfoFragment extends Fragment {
    public ArticleInfo articleInfo;
    private ImageView cover,upIcon;
    private TextView title,content, keywords,upName,views,timeText,cvidText;
    private MaterialCardView upCard;
    private boolean keywords_expand = false;
    public ArticleInfoFragment(){}

    public static ArticleInfoFragment newInstance(ArticleInfo newArticleInfo) {
        ArticleInfoFragment fragment = new ArticleInfoFragment();
        fragment.articleInfo = newArticleInfo;
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_article_info, container, false);
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cover = view.findViewById(R.id.cover);
        upIcon = view.findViewById(R.id.upInfo_Icon);
        title = view.findViewById(R.id.title);
        content = view.findViewById(R.id.content);
        keywords = view.findViewById(R.id.keywords);
        upName = view.findViewById(R.id.upInfo_Name);
        views = view.findViewById(R.id.viewsCount);
        timeText = view.findViewById(R.id.timeText);
        cvidText = view.findViewById(R.id.cvidText);
        upCard = view.findViewById(R.id.upInfo);

        if (!SharedPreferencesUtil.getBoolean("tags_enable", true)) keywords.setVisibility(View.GONE);

        CenterThreadPool.run(()-> {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                title.setText(articleInfo.title);
                content.setText(LittleToolsUtil.htmlReString(LittleToolsUtil.htmlToString(articleInfo.content)));
                cvidText.setText("CV" + String.valueOf(articleInfo.id));
                upName.setText(articleInfo.upName);
                keywords.setText("关键词：" + articleInfo.keywords);
                views.setText(String.valueOf(articleInfo.view));
                Glide.with(view.getContext()).load(articleInfo.banner).placeholder(R.drawable.placeholder)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(LittleToolsUtil.dp2px(4,view.getContext()))))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(cover);
                Glide.with(view.getContext()).load(articleInfo.upAvatar).placeholder(R.drawable.akari)
                        .apply(RequestOptions.circleCropTransform())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(upIcon);
                upCard.setOnClickListener(view1 ->{
                    Intent intent = new Intent();
                    intent.setClass(view.getContext(), UserInfoActivity.class);
                    intent.putExtra("mid",articleInfo.upMid);
                    startActivity(intent);
                });
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                timeText.setText(sdf.format(articleInfo.ctime * 1000));
            });
        });
    }
}
