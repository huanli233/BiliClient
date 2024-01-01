package com.RobinNotBad.BiliClient.activity.article;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.user.UserInfoActivity;
import com.RobinNotBad.BiliClient.model.ArticleInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.card.MaterialCardView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ArticleInfoFragment extends Fragment {
    public ArticleInfo articleInfo;
    private ImageView cover,upIcon;
    private TextView title, keywords,upName,views,timeText,cvidText;
    private LinearLayout content;
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
        content = view.findViewById(R.id.content_list);
        keywords = view.findViewById(R.id.keywords);
        upName = view.findViewById(R.id.upInfo_Name);
        views = view.findViewById(R.id.viewsCount);
        timeText = view.findViewById(R.id.timeText);
        cvidText = view.findViewById(R.id.cvidText);
        upCard = view.findViewById(R.id.upInfo);

        if (!SharedPreferencesUtil.getBoolean("tags_enable", true)) keywords.setVisibility(View.GONE);

        keywords.setOnClickListener(view1 -> {
            if(keywords_expand) keywords.setMaxLines(1);
            else keywords.setMaxLines(200);
            keywords_expand = !keywords_expand;
        });

        CenterThreadPool.run(()-> {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                title.setText(articleInfo.title);
                cvidText.setText("CV" + articleInfo.id + " " + articleInfo.wordCount + "字");
                upName.setText(articleInfo.upName);
                keywords.setText("关键词：" + articleInfo.keywords);
                views.setText(articleInfo.view);
                if(articleInfo.banner.isEmpty()) cover.setVisibility(View.GONE);
                else{
                    Glide.with(view.getContext()).load(articleInfo.banner).placeholder(R.drawable.placeholder)
                            .apply(RequestOptions.bitmapTransform(new RoundedCorners(LittleToolsUtil.dp2px(4,view.getContext()))))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(cover);
                }
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

                //开始解析内容的html
                Document document = Jsoup.parse(articleInfo.content);
                loadContentHtml(document.select("body").get(0),view);
            });
        });
    }

    private void loadContentHtml(Element element,View view){
        for(Element e : element.children()){
            if (e.is("p")){
                TextView textView = new TextView(view.getContext());
                textView.setText(e.text());
                textView.setAlpha(0.85F);
                content.addView(textView);
            }
            else if (e.is("strong")){
                TextView textView = new TextView(view.getContext());
                textView.setTextSize(LittleToolsUtil.sp2px(13,view.getContext()));
                textView.setText(e.text());
                textView.setAlpha(0.92F);
                content.addView(textView);
            }
            else if (e.is("br")){
                TextView textView = new TextView(view.getContext());
                textView.setHeight(LittleToolsUtil.dp2px(6f,view.getContext()));
                content.addView(textView);
            }
            else if (e.is("img")){
                ImageView imageView = new ImageView(view.getContext());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5,0,5,0);
                imageView.setLayoutParams(layoutParams);
                String url = "http:" + e.attr("src");
                ArrayList<String> img_list = new ArrayList<>();
                img_list.add(url);
                imageView.setOnClickListener(view1 -> {
                    Intent intent = new Intent();
                    intent.setClass(view.getContext(), ImageViewerActivity.class);
                    intent.putExtra("imageList", img_list);
                    view.getContext().startActivity(intent);
                });

                content.addView(imageView);
                try {
                    Glide.with(view.getContext())
                            .asDrawable()
                            .thumbnail(0.12f)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.placeholder)
                            .load(url)
                            .into(imageView);
                }catch (OutOfMemoryError ee){
                    ee.printStackTrace();
                    MsgUtil.toast("内存溢出哩（悲",view.getContext());
                }
            }
            else loadContentHtml(e,view);
        }
    }
}
