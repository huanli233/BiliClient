package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.user.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends BaseActivity {
    int eggClick = 0;
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_about);
        Log.e("debug","进入关于页面");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        try{
            ((TextView)findViewById(R.id.app_version)).setText(getPackageManager().getPackageInfo(getPackageName(),0).versionName);
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        List<ImageView> developerAvaterViews = new ArrayList<ImageView>() {{
            add(findViewById(R.id.robinAvatar));
            add(findViewById(R.id.duduAvatar));
            add(findViewById(R.id.dadaAvatar));
            add(findViewById(R.id.moyeAvatar));
            add(findViewById(R.id.silentAvatar));
        }};
        List<Integer> developerAvaters = new ArrayList<Integer>() {{
            add(R.mipmap.avatar_robin);
            add(R.mipmap.avatar_dudu);
            add(-1);
            add(R.mipmap.avatar_moye);
            add(R.mipmap.avatar_silent);
        }};
        List<MaterialCardView> developerCardList = new ArrayList<MaterialCardView>() {{
            add(findViewById(R.id.robin_card));
            add(findViewById(R.id.dudu_card));
            add(findViewById(R.id.dada_card));
            add(findViewById(R.id.moye_card));
            add(findViewById(R.id.silent_card));
        }};
        List<Long> developerUidList = new ArrayList<Long>() {{
            add((long)646521226);
            add((long)517053179);
            add((long)432128342);
            add((long)394675616);
            add((long)40140732);
        }};

        for(int i = 0;i < developerAvaterViews.size();i++){
            int finalI = i;
            if(developerAvaters.get(i) != -1) Glide.with(this).load(developerAvaters.get(i))
                    .placeholder(R.mipmap.akari)
                    .apply(RequestOptions.circleCropTransform())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(developerAvaterViews.get(i));

            developerCardList.get(i).setOnClickListener(view -> {
                Intent intent = new Intent()
                        .setClass(this, UserInfoActivity.class)
                        .putExtra("mid", developerUidList.get(finalI));
                startActivity(intent);
            });
        }

        findViewById(R.id.author_words).setOnClickListener(view -> {
            eggClick++;
            if (eggClick == 7) {
                eggClick = 0;
                MsgUtil.toastLong("无论当下的境遇如何，\n这片星空下永远有你的一片位置。\n抱抱屏幕前的你，\n真诚地祝愿你永远快乐幸福。\n让我们一起，迈入“下一个远方”。",this);
                Intent intent = new Intent();
                intent.setClass(this, VideoInfoActivity.class);
                intent.putExtra("bvid", "BV1UC411B7Co");
                startActivity(intent);
            }
        });
    }
}
