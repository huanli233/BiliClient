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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

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
        
        Glide.with(this).load(R.drawable.avatar_robin)
                .placeholder(R.drawable.akari)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into((ImageView)findViewById(R.id.robinAvatar));
        Glide.with(this).load(R.drawable.avatar_dudu)
                .placeholder(R.drawable.akari)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into((ImageView)findViewById(R.id.duduAvatar));
        Glide.with(this).load(R.drawable.avatar_dada)
                .placeholder(R.drawable.akari)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into((ImageView)findViewById(R.id.dadaAvatar));
        Glide.with(this).load(R.drawable.avatar_moye)
                .placeholder(R.drawable.akari)
                .apply(RequestOptions.circleCropTransform())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into((ImageView)findViewById(R.id.moyeAvatar));
        findViewById(R.id.robin_card).setOnClickListener(view -> {
            Intent intent = new Intent()
                    .setClass(this, UserInfoActivity.class)
                    .putExtra("mid", (long)646521226);
            startActivity(intent);
        });
        findViewById(R.id.dudu_card).setOnClickListener(view -> {
            Intent intent = new Intent()
                    .setClass(this, UserInfoActivity.class)
                    .putExtra("mid", (long)517053179);
            startActivity(intent);
        });
        findViewById(R.id.moye_card).setOnClickListener(view -> {
            Intent intent = new Intent()
                    .setClass(this, UserInfoActivity.class)
                    .putExtra("mid", (long)394675616);
            startActivity(intent);
        });
    }
}
