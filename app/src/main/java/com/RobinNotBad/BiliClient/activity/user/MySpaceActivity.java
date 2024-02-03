package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.settings.QRLoginActivity;
import com.RobinNotBad.BiliClient.activity.settings.SpecialLoginActivity;
import com.RobinNotBad.BiliClient.activity.user.favorite.FavoriteFolderListActivity;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.io.IOException;

public class MySpaceActivity extends InstanceActivity {

    private ImageView userAvatar;
    private TextView userName, userFans, userDesc;
    private MaterialCardView myInfo,follow,watchLater,favorite,history,creative,logout;

    private boolean confirmLogout = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myspace);
        setMenuClick(5);
        Log.e("debug","进入个人页");

        userAvatar = findViewById(R.id.userAvatar);
        userName = findViewById(R.id.userName);
        userFans = findViewById(R.id.userFans);
        userDesc = findViewById(R.id.userDesc);

        myInfo = findViewById(R.id.myinfo);
        follow = findViewById(R.id.follow);
        watchLater = findViewById(R.id.watchlater);
        favorite = findViewById(R.id.favorite);
        history = findViewById(R.id.history);
        creative = findViewById(R.id.creative);
        logout = findViewById(R.id.logout);



        CenterThreadPool.run(()->{
            try {
                UserInfo userInfo = UserInfoApi.getCurrentUserInfo();
                int userCoin = UserInfoApi.getCurrentUserCoin();
                runOnUiThread(() -> {
                    Glide.with(MySpaceActivity.this).load(userInfo.avatar)
                            .placeholder(R.mipmap.akari).apply(RequestOptions.circleCropTransform())
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(userAvatar);
                    userName.setText(userInfo.name);
                    userFans.setText(LittleToolsUtil.toWan(userInfo.fans) + "粉丝 " + userCoin + "硬币");
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

                    creative.setOnClickListener(view -> {
                        Intent intent = new Intent();
                        intent.setClass(MySpaceActivity.this, CreativeCenterActivity.class);
                        startActivity(intent);
                    });
                    if(!SharedPreferencesUtil.getBoolean("creative_enable",true)) creative.setVisibility(View.GONE);

                    logout.setOnClickListener(view -> {
                        if(confirmLogout){
                            CenterThreadPool.run(UserInfoApi::exitLogin);
                            SharedPreferencesUtil.removeValue(SharedPreferencesUtil.cookies);
                            SharedPreferencesUtil.removeValue(SharedPreferencesUtil.mid);
                            SharedPreferencesUtil.removeValue(SharedPreferencesUtil.csrf);
                            SharedPreferencesUtil.removeValue(SharedPreferencesUtil.refresh_token);
                            SharedPreferencesUtil.removeValue(SharedPreferencesUtil.cookie_refresh);
                            MsgUtil.toast("账号已退出",getApplicationContext());
                            Intent intent = new Intent(this, QRLoginActivity.class);
                            startActivity(intent);
                            finish();
                        }else MsgUtil.toast("再点一次退出登录",getApplicationContext());
                        confirmLogout = !confirmLogout;
                    });
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }
}