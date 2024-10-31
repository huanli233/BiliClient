package com.RobinNotBad.BiliClient.activity.settings.setup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.settings.login.LoginActivity;
import com.RobinNotBad.BiliClient.activity.settings.login.SpecialLoginActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

public class IntroductionActivity extends BaseActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_introduction);

        MaterialCardView confirm = findViewById(R.id.confirm);

        confirm.setOnClickListener(view -> {
            SharedPreferencesUtil.putBoolean("setup", true);

            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= 19) {
                intent.setClass(IntroductionActivity.this, LoginActivity.class);   //去扫码登录页面
            } else {
                intent.setClass(IntroductionActivity.this, SpecialLoginActivity.class);   //4.4以下系统去特殊登录页面
                intent.putExtra("from_setup", true);
            }
            startActivity(intent);
            finish();
        });
    }

}