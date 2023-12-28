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
import com.google.android.material.card.MaterialCardView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class SettingAboutActivity extends BaseActivity {
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
            add(R.drawable.avatar_robin);
            add(R.drawable.avatar_dudu);
            add(R.drawable.avatar_dada);
            add(R.drawable.avatar_moye);
            add(R.drawable.avatar_silent);
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
            add((long)0);
            add((long)394675616);
            add((long)40140732);
        }};

        for(int i = 0;i < developerAvaterViews.size();i++){
            int finalI = i;
            Glide.with(this).load(developerAvaters.get(i))
                    .placeholder(R.drawable.akari)
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
    }
}
