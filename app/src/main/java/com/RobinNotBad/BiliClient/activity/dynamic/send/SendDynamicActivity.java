package com.RobinNotBad.BiliClient.activity.dynamic.send;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.EmoteActivity;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.adapter.article.ArticleCardHolder;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardHolder;
import com.RobinNotBad.BiliClient.api.EmoteApi;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * 发送动态输入Activity，直接copy的WriteReplyActivity
 * 换成了ActivityResult
 * （我并不怎么会写）
 */
public class SendDynamicActivity extends BaseActivity {

    EditText editText;

    private final ActivityResultLauncher<Intent> emoteLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
        int code = result.getResultCode();
        Intent data = result.getData();
        if (code == RESULT_OK && data != null && data.hasExtra("text")) {
            editText.append(data.getStringExtra("text"));
        }
    });

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_send_dynamic, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setTopbarExit();

            if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                setResult(RESULT_CANCELED);
                finish();
                MsgUtil.showMsg("还没有登录喵~");
            }

            editText = findViewById(R.id.editText);
            MaterialCardView send = findViewById(R.id.send);

            ConstraintLayout extraCard = findViewById(R.id.extraCard);
            VideoInfo video = null;
            Dynamic forward = null;
            if(TerminalContext.getInstance().getForwardContent() instanceof VideoInfo) {
                video = (VideoInfo) TerminalContext.getInstance().getForwardContent();
            } else {
                forward = (Dynamic) TerminalContext.getInstance().getForwardContent();
            }
            if (forward != null) {
                View childCard = View.inflate(this, R.layout.cell_dynamic_child, extraCard);
                showChildDyn(childCard, forward);
            } else if (video != null) {
                VideoCardHolder holder = new VideoCardHolder(LayoutInflater.from(this).inflate(R.layout.cell_video_list, extraCard));
                holder.showVideoCard(video.toCard(),this);
            }

            send.setOnClickListener(view -> {
                // 不了解遂直接保留cookie刷新判断了
                if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.cookie_refresh, true)) {
                    String text = editText.getText().toString();
                    Intent result = new Intent();
                    // 原神级的传数据
                    Bundle bundle = SendDynamicActivity.this.getIntent().getExtras();
                    if (bundle != null) result.putExtras(bundle);
                    result.putExtra("text", text);
                    setResult(RESULT_OK, result);
                    finish();
                } else
                    MsgUtil.showDialog("无法发送", "上一次的Cookie刷新失败了，\n您可能需要重新登录以进行敏感操作", -1);
            });

            findViewById(R.id.emote).setOnClickListener(view ->
                    emoteLauncher.launch(new Intent(this, EmoteActivity.class).putExtra("from", EmoteApi.BUSINESS_DYNAMIC)));
        });
    }

    @SuppressLint("SetTextI18n")
    private void showChildDyn(View itemView, Dynamic dynamic) {
        TextView username, content;
        ImageView avatar;
        LinearLayout extraCard;
        MaterialCardView cell_dynamic_video, cell_dynamic_image, cell_dynamic_article;
        username = itemView.findViewById(R.id.child_username);
        content = itemView.findViewById(R.id.child_content);
        avatar = itemView.findViewById(R.id.child_avatar);
        extraCard = itemView.findViewById(R.id.child_extraCard);
        cell_dynamic_video = extraCard.findViewById(R.id.dynamic_video_child);
        cell_dynamic_article = extraCard.findViewById(R.id.dynamic_article_child);
        cell_dynamic_image = extraCard.findViewById(R.id.dynamic_image_child);
        username.setText(dynamic.userInfo.name);
        if (dynamic.content != null && !dynamic.content.isEmpty()) {
            content.setVisibility(View.VISIBLE);
            content.setText(dynamic.content);
            if (dynamic.emotes != null) {
                CenterThreadPool.run(() -> {
                    try {
                        SpannableString spannableString = EmoteUtil.textReplaceEmote(dynamic.content, dynamic.emotes, 1.0f, this, content.getText());
                        CenterThreadPool.runOnUiThread(() -> {
                            content.setText(spannableString);
                            ToolsUtil.setLink(content);
                            ToolsUtil.setAtLink(dynamic.ats, content);
                        });
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        } else content.setVisibility(View.GONE);
        ToolsUtil.setLink(content);
        ToolsUtil.setAtLink(dynamic.ats, content);
        Glide.with(this).load(GlideUtil.url(dynamic.userInfo.avatar))
                .transition(GlideUtil.getTransitionOptions())
                .placeholder(R.mipmap.akari)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(avatar);

        avatar.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, UserInfoActivity.class);
            intent.putExtra("mid", dynamic.userInfo.mid);
            this.startActivity(intent);
        });

        boolean isPgc = false;
        for (View view1 : Arrays.asList(cell_dynamic_video, cell_dynamic_image, cell_dynamic_article)) {
            if (view1 != null) {
                view1.setVisibility(View.GONE);
            }
        }
        if (dynamic.major_type != null) switch (dynamic.major_type) {
            case "MAJOR_TYPE_PGC":
                isPgc = true;
            case "MAJOR_TYPE_ARCHIVE":
            case "MAJOR_TYPE_UGC_SEASON":
                VideoCard childVideoCard = (VideoCard) dynamic.major_object;
                VideoCardHolder video_holder = new VideoCardHolder(cell_dynamic_video);
                video_holder.showVideoCard(childVideoCard, this);
                boolean finalIsPgc = isPgc;
                cell_dynamic_video.setOnClickListener(view -> TerminalContext.getInstance().enterVideoDetailPage(this, childVideoCard.aid, "", finalIsPgc ? "media": null));
                cell_dynamic_video.setVisibility(View.VISIBLE);
                break;

            case "MAJOR_TYPE_ARTICLE":
                ArticleCard articleCard = (ArticleCard) dynamic.major_object;
                ArticleCardHolder article_holder = new ArticleCardHolder(cell_dynamic_article);
                article_holder.showArticleCard(articleCard, this);
                cell_dynamic_article.setOnClickListener(view -> TerminalContext.getInstance().enterArticleDetailPage(this, articleCard.id));
                cell_dynamic_article.setVisibility(View.VISIBLE);
                break;

            case "MAJOR_TYPE_DRAW":
                ArrayList<String> pictureList = (ArrayList<String>) dynamic.major_object;
                ImageView imageView = cell_dynamic_image.findViewById(R.id.imageView);
                Glide.with(this).asDrawable().load(GlideUtil.url(pictureList.get(0)))
                        .transition(GlideUtil.getTransitionOptions())
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);
                TextView textView = cell_dynamic_image.findViewById(R.id.imageCount);
                textView.setText("共" + pictureList.size() + "张图片");
                cell_dynamic_image.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(this, ImageViewerActivity.class);
                    intent.putExtra("imageList", pictureList);
                    startActivity(intent);
                });
                cell_dynamic_image.setVisibility(View.VISIBLE);
                break;
        }
        content.setMaxLines(999);
        content.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TerminalContext.getInstance().setForwardContent(null);
    }
}