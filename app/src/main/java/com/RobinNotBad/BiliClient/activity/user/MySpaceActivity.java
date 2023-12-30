package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.activity.settings.SpecialLoginActivity;
import com.RobinNotBad.BiliClient.activity.user.favorite.FavoriteFolderListActivity;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.io.IOException;

public class MySpaceActivity extends BaseActivity {

    @SuppressLint("StaticFieldLeak")
    public static MySpaceActivity instance = null;
    private ImageView userAvatar;
    private TextView userName, userFans, userDesc;
    private MaterialCardView myInfo,follow,watchLater,favorite,history;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myspace);
        instance = this;
        Log.e("debug","进入个人页");

        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(MySpaceActivity.this, MenuActivity.class);
            intent.putExtra("from",5);
            startActivity(intent);
        });
        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        userFans = findViewById(R.id.userFans);
        userDesc = findViewById(R.id.userDesc);

        myInfo = findViewById(R.id.myinfo);
        follow = findViewById(R.id.follow);
        watchLater = findViewById(R.id.watchlater);
        favorite = findViewById(R.id.favorite);
        history = findViewById(R.id.history);



        new Thread(()->{
            try {
                UserInfo userInfo = UserInfoApi.getCurrentUserInfo();
                int userCoin = UserInfoApi.getCurrentUserCoin();
                runOnUiThread(() -> {
                    Glide.with(MySpaceActivity.this).load(userInfo.avatar)
                            .placeholder(R.drawable.akari).apply(RequestOptions.circleCropTransform())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(userAvatar);
                    userName.setText(userInfo.name);
                    userFans.setText(LittleToolsUtil.toWan(userInfo.fans) + "粉丝 " + String.valueOf(userCoin) + "硬币");
                    userDesc.setText(userInfo.sign);

                    myInfo.setOnClickListener(view -> {
                        Intent intent = new Intent();
                        intent.setClass(MySpaceActivity.this, UserInfoActivity.class);
                        intent.putExtra("mid",userInfo.mid);
                        startActivity(intent);
                    });

                    myInfo.setOnLongClickListener(view -> {
                        Intent intent = new Intent();
                        intent.setClass(MySpaceActivity.this, SpecialLoginActivity.class);
                        intent.putExtra("login",false);
                        startActivity(intent);
                        return true;
                    });

                    follow.setOnClickListener(view -> {
                        Intent intent = new Intent();
                        intent.setClass(MySpaceActivity.this, FollowListActivity.class);
                        startActivity(intent);
                    });

                    watchLater.setOnClickListener(view -> {
                        Intent intent = new Intent();
                        intent.setClass(MySpaceActivity.this, WatchLaterActivity.class);
                        startActivity(intent);
                    });

                    favorite.setOnClickListener(view -> {
                        Intent intent = new Intent();
                        intent.setClass(MySpaceActivity.this, FavoriteFolderListActivity.class);
                        startActivity(intent);
                    });

                    history.setOnClickListener(view -> {
                        Intent intent = new Intent();
                        intent.setClass(MySpaceActivity.this, HistoryActivity.class);
                        startActivity(intent);
                    });
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();


    }
}