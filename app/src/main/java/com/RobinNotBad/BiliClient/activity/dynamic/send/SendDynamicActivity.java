package com.RobinNotBad.BiliClient.activity.dynamic.send;

import static com.RobinNotBad.BiliClient.util.ToolsUtil.toWan;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.ImageViewerActivity;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicInfoActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.adapter.ArticleCardHolder;
import com.RobinNotBad.BiliClient.adapter.VideoCardHolder;
import com.RobinNotBad.BiliClient.model.ArticleCard;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.model.VideoInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.EmoteUtil;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * 发送动态输入Activity，直接copy的WriteReplyActivity
 * 换成了ActivityResult
 * （我并不怎么会写）
 */
public class SendDynamicActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_dynamic);

        if(SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid,0)==0){
            MsgUtil.toast("还没有登录喵~",this);
            setResult(RESULT_CANCELED);
            finish();
        }

        EditText editText = findViewById(R.id.editText);
        MaterialCardView send = findViewById(R.id.send);

        ConstraintLayout extraCard = findViewById(R.id.extraCard);
        Dynamic forward = (Dynamic) getIntent().getSerializableExtra("forward");
        VideoInfo video = (VideoInfo) getIntent().getSerializableExtra("video");
        if (forward != null) {
            View childCard = View.inflate(this, R.layout.cell_dynamic_child, extraCard);
            showChildDyn(childCard, forward);
        } else if (video != null) {
            View view = LayoutInflater.from(this).inflate(R.layout.cell_video_list, extraCard);
            showVideo(view, video);
        }

        send.setOnClickListener(view -> {
            // 不了解遂直接保留cookie刷新判断了
            if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.cookie_refresh,true)){
                String text = editText.getText().toString();
                Intent result = new Intent();
                // 原神级的传数据
                Bundle bundle = SendDynamicActivity.this.getIntent().getExtras();
                if (bundle != null) result.putExtras(bundle);
                result.putExtra("text", text);
                setResult(RESULT_OK, result);
                finish();
            } else MsgUtil.showDialog(this,"无法发送","上一次的Cookie刷新失败了，\n您可能需要重新登录以进行敏感操作",-1);
        });
    }

    private void showChildDyn(View itemView, Dynamic dynamic) {
        TextView username, content;
        ImageView avatar;
        ConstraintLayout extraCard;
        username = itemView.findViewById(R.id.child_username);
        content = itemView.findViewById(R.id.child_content);
        avatar = itemView.findViewById(R.id.child_avatar);
        extraCard = itemView.findViewById(R.id.child_extraCard);
        username.setText(dynamic.userInfo.name);
        if(dynamic.content != null && !dynamic.content.isEmpty()) {
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
                    } catch (JSONException e) {
                        e.printStackTrace();
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

        if(dynamic.major_type != null) switch (dynamic.major_type) {
            case "MAJOR_TYPE_ARCHIVE":
            case "MAJOR_TYPE_UGC_SEASON":
                VideoCard childVideoCard = (VideoCard) dynamic.major_object;
                VideoCardHolder video_holder = new VideoCardHolder(View.inflate(this,R.layout.cell_dynamic_video,extraCard));
                video_holder.showVideoCard(childVideoCard,this);
                video_holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(this, VideoInfoActivity.class);
                    intent.putExtra("bvid", "");
                    intent.putExtra("aid", childVideoCard.aid);
                    this.startActivity(intent);
                });
                break;

            case "MAJOR_TYPE_ARTICLE":
                ArticleCard articleCard = (ArticleCard) dynamic.major_object;
                ArticleCardHolder article_holder = new ArticleCardHolder(View.inflate(this,R.layout.cell_dynamic_article,extraCard));
                article_holder.showArticleCard(articleCard,this);
                article_holder.itemView.findViewById(R.id.cardView).setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(this, ArticleInfoActivity.class);
                    intent.putExtra("cvid", articleCard.id);
                    this.startActivity(intent);
                });
                break;

            case "MAJOR_TYPE_DRAW":
                ArrayList<String> pictureList = (ArrayList<String>) dynamic.major_object;
                View imageCard = View.inflate(this,R.layout.cell_dynamic_image,extraCard);
                ImageView imageView = imageCard.findViewById(R.id.imageView);
                Glide.with(this).load(GlideUtil.url(pictureList.get(0)))
                        .placeholder(R.mipmap.placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);
                TextView textView = imageCard.findViewById(R.id.imageCount);
                textView.setText("共" + pictureList.size() + "张图片");
                MaterialCardView cardView = imageCard.findViewById(R.id.imageCard);
                cardView.setOnClickListener(view -> {
                    Intent intent = new Intent();
                    intent.setClass(this, ImageViewerActivity.class);
                    intent.putExtra("imageList", pictureList);
                    this.startActivity(intent);
                });
                break;
        } else {
            content.setMaxLines(999);
        }
    }

    private void showVideo(View itemView, VideoInfo videoInfo) {
        TextView title,upName,playTimes;
        ImageView cover,playIcon,upIcon;
        title = itemView.findViewById(R.id.listVideoTitle);
        upName = itemView.findViewById(R.id.listUpName);
        playTimes = itemView.findViewById(R.id.listPlayTimes);
        cover = itemView.findViewById(R.id.listCover);
        playIcon = itemView.findViewById(R.id.imageView3);
        upIcon = itemView.findViewById(R.id.avatarIcon);

        title.setText(ToolsUtil.htmlToString(videoInfo.title));
        String upNameStr = videoInfo.staff.get(0).name;
        if (upNameStr.isEmpty()){
            upName.setVisibility(View.GONE);
            upIcon.setVisibility(View.GONE);
        }
        else upName.setText(upNameStr);

        // 很抽象的方法名
        String playTimesStr = toWan(videoInfo.stats.view);
        if(playTimesStr.isEmpty()){
            playIcon.setVisibility(View.GONE);
            playTimes.setVisibility(View.GONE);
        }
        else playTimes.setText(playTimesStr);

        Glide.with(this).load(GlideUtil.url(videoInfo.cover))
                .placeholder(R.mipmap.placeholder)
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5, this))))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(cover);
    }

}