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
import com.RobinNotBad.BiliClient.activity.settings.LoginActivity;
import com.RobinNotBad.BiliClient.activity.settings.SpecialLoginActivity;
import com.RobinNotBad.BiliClient.activity.user.favorite.FavoriteFolderListActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.api.UserInfoApi;
import com.RobinNotBad.BiliClient.model.UserInfo;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

public class MySpaceActivity extends InstanceActivity {

    private ImageView userAvatar;
    private TextView userName, userFans;
    private MaterialCardView myInfo,follow,watchLater,favorite,bangumi,history,creative,logout;

    private boolean confirmLogout = false;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_myspace, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setMenuClick();
            Log.e("debug","进入个人页");

            userAvatar = findViewById(R.id.userAvatar);
            userName = findViewById(R.id.userName);
            userFans = findViewById(R.id.userFans);

            myInfo = findViewById(R.id.myinfo);
            follow = findViewById(R.id.follow);
            watchLater = findViewById(R.id.watchlater);
            favorite = findViewById(R.id.favorite);
            bangumi = findViewById(R.id.bangumi);
            history = findViewById(R.id.history);
            creative = findViewById(R.id.creative);
            logout = findViewById(R.id.logout);



            CenterThreadPool.run(()->{
                try {
                    UserInfo userInfo = UserInfoApi.getCurrentUserInfo();
                    int userCoin = UserInfoApi.getCurrentUserCoin();
                    if(!this.isDestroyed()) runOnUiThread(() -> {
                        Glide.with(MySpaceActivity.this).load(GlideUtil.url(userInfo.avatar))
                                .placeholder(R.mipmap.akari).apply(RequestOptions.circleCropTransform())
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .into(userAvatar);
                        userName.setText(userInfo.name);
                        userFans.setText(ToolsUtil.toWan(userInfo.fans) + "粉丝 " + userCoin + "硬币");

                        myInfo.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, UserInfoActivity.class);
                            intent.putExtra("mid",userInfo.mid);
                            startActivity(intent);
                        });

                        follow.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, FollowingUsersActivity.class);
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

                        bangumi.setOnClickListener(view -> {
                            Intent intent = new Intent();
                            intent.setClass(MySpaceActivity.this, FollowingBangumisActivity.class);
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
                                MsgUtil.toast("账号已退出",this);
                                Intent intent = new Intent(this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }else MsgUtil.toast("再点一次退出登录！",this);
                            confirmLogout = !confirmLogout;
                        });
                    });
                } catch (Exception e) {
                    report(e);
                }
            });
        });
    }
}