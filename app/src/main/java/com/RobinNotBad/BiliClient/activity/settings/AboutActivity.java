package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends BaseActivity {
    int eggClick_authorWords = 0, eggClick_toUncle = 0;
    @SuppressLint({"MissingInflatedId", "SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_setting_about, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            Log.e("debug","进入关于页面");
            setTopbarExit();

            try{
                SpannableString version_str = new SpannableString("版本名\n" + getPackageManager().getPackageInfo(getPackageName(),0).versionName);
                version_str.setSpan(new StyleSpan(Typeface.BOLD),0,3, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                ((TextView)findViewById(R.id.app_version)).setText(version_str);

                SpannableString code_str = new SpannableString("版本号\n" + getPackageManager().getPackageInfo(getPackageName(),0).versionCode);
                code_str.setSpan(new StyleSpan(Typeface.BOLD),0,3, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                ((TextView)findViewById(R.id.app_version_code)).setText(code_str);

                ((TextView)findViewById(R.id.updatelog_view)).setText("\n更新日志：\n "+ ToolsUtil.getUpdateLog(this));
                ToolsUtil.setCopy(findViewById(R.id.updatelog_view),this,ToolsUtil.getUpdateLog(this));
            }catch(PackageManager.NameNotFoundException e){
                e.printStackTrace();
            }

            List<ImageView> developerAvaterViews = new ArrayList<>() {{
                add(findViewById(R.id.robinAvatar));
                add(findViewById(R.id.duduAvatar));
                add(findViewById(R.id.dadaAvatar));
                add(findViewById(R.id.moyeAvatar));
                add(findViewById(R.id.silentAvatar));
            }};
            List<Integer> developerAvaters = new ArrayList<>() {{
                add(R.mipmap.avatar_robin);
                add(R.mipmap.avatar_dudu);
                add(-1);
                add(R.mipmap.avatar_moye);
                add(R.mipmap.avatar_silent);
            }};
            List<MaterialCardView> developerCardList = new ArrayList<>() {{
                add(findViewById(R.id.robin_card));
                add(findViewById(R.id.dudu_card));
                add(findViewById(R.id.dada_card));
                add(findViewById(R.id.moye_card));
                add(findViewById(R.id.silent_card));
            }};
            List<Long> developerUidList = new ArrayList<>() {{
                add((long) 646521226);
                add((long) 517053179);
                add((long) 432128342);
                add((long) 394675616);
                add((long) 40140732);
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
                eggClick_authorWords++;
                if (eggClick_authorWords == 7) {
                    eggClick_authorWords = 0;
                    MsgUtil.showText(this,"作者的话","无论当下的境遇如何，\n这片星空下永远有你的一片位置。\n抱抱屏幕前的你，\n真诚地祝愿你永远快乐幸福。\n让我们一起，迈入“下一个远方”。<extra_insert>{\"type\":\"video\",\"content\":\"BV1UC411B7Co\",\"title\":\"【原神新春会】下一个远方\"}");
                }
            });

            findViewById(R.id.toUncle).setOnClickListener(view -> {
                eggClick_toUncle++;
                if (eggClick_toUncle == 7) {
                    eggClick_toUncle = 0;
                    MsgUtil.showText(this,"给叔叔","\"你指尖跃动的电光，是我此生不灭的信仰。\"<extra_insert>{\"type\":\"video\",\"content\":\"BV157411v76Z\",\"title\":\"【B站入站曲】\"}");
                }
            });

            findViewById(R.id.sponsor_list).setOnClickListener(view -> {
                Intent intent = new Intent(this,SponsorActivity.class);
                startActivity(intent);
            });
        });

        if(!ToolsUtil.isDebugBuild()) findViewById(R.id.debug_tip).setVisibility(View.GONE);
    }
}
