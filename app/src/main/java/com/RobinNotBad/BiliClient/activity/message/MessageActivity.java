package com.RobinNotBad.BiliClient.activity.message;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.ViewPagerFragmentAdapter;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends BaseActivity {
    public static MessageActivity instance = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_viewpager);
        instance = this;

        findViewById(R.id.top).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this, MenuActivity.class);
            intent.putExtra("from",4);
            startActivity(intent);
        });

        ImageView loading = findViewById(R.id.loading);
        loading.setVisibility(View.VISIBLE);

        TextView pageName = findViewById(R.id.pageName);
        pageName.setText("消息");

        ViewPager viewPager = findViewById(R.id.viewPager);
        new Thread(() -> {
            try {
                List<Fragment> fragmentList = new ArrayList<>();
                MessageLikeFragment viFragment = MessageLikeFragment.newInstance();
                fragmentList.add(viFragment);

                ViewPagerFragmentAdapter vpfAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), fragmentList);

                runOnUiThread(()->{
                    loading.setVisibility(View.GONE);
                    viewPager.setAdapter(vpfAdapter);

                    if(SharedPreferencesUtil.getBoolean("first_messagepage",true)){
                        Toast.makeText(this, "提示：本页面可以左右滑动", Toast.LENGTH_LONG).show();
                        SharedPreferencesUtil.putBoolean("first_messagepage",false);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();
    }
}
