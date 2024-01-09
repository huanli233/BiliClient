package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity;
import com.RobinNotBad.BiliClient.activity.message.MessageActivity;
import com.RobinNotBad.BiliClient.activity.search.SearchActivity;
import com.RobinNotBad.BiliClient.activity.settings.QRLoginActivity;
import com.RobinNotBad.BiliClient.activity.settings.SettingMainActivity;
import com.RobinNotBad.BiliClient.activity.user.MySpaceActivity;
import com.RobinNotBad.BiliClient.activity.video.RecommendActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

//菜单页面
//2023-07-14

public class MenuActivity extends BaseActivity {

    private int from;
    /*private final List<Activity> activityList = new ArrayList<Activity>() {{
        add(RecommendActivity.instance);
        add(SearchActivity.instance);
        add(DynamicActivity.instance);
        add(QRLoginActivity.instance);
        add(MySpaceActivity.instance);
        add(MessageActivity.instance);
        add(LocalListActivity.instance);
        add(SettingMainActivity.instance);
    }};*/
    private final List<Class<? extends Activity>> classList = new ArrayList<Class<? extends Activity>>(){{
        add(RecommendActivity.class);
        add(SearchActivity.class);
        add(DynamicActivity.class);
        add(QRLoginActivity.class);
        add(MySpaceActivity.class);
        add(MessageActivity.class);
        add(LocalListActivity.class);
        add(SettingMainActivity.class);
    }};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Log.e("debug","进入菜单页");

        Intent intent = getIntent();
        from = intent.getIntExtra("from",0);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        List<MaterialCardView> cardList = new ArrayList<MaterialCardView>() {{
            add(findViewById(R.id.main));
            add(findViewById(R.id.search));
            add(findViewById(R.id.dynamic));
            add(findViewById(R.id.login));
            add(findViewById(R.id.profile));
            add(findViewById(R.id.message));
            add(findViewById(R.id.local));
            add(findViewById(R.id.settings));
            add(findViewById(R.id.exit));
        }};

        if(SharedPreferencesUtil.getLong("mid",0)==0) {
            findViewById(R.id.profile).setVisibility(View.GONE);
            findViewById(R.id.dynamic).setVisibility(View.GONE);
            findViewById(R.id.message).setVisibility(View.GONE);
        }
        else findViewById(R.id.login).setVisibility(View.GONE);

        for (int i = 0; i < cardList.size(); i++) {
            int finalI = i;
            cardList.get(i).setOnClickListener(view -> {
                if(from == finalI) finish();
                else if(finalI != cardList.size() - 1) start(classList.get(finalI));
            });
        }

    }

    /*private void killAndJump(int i){
        if(activityList.get(from)!=null) activityList.get(from).finish();
        if(i != activityList.size()) {
            Intent intent = new Intent();
            intent.setClass(MenuActivity.this, classList.get(i));
            startActivity(intent);
        }
        finish();
    }*/

    private void start(Class<? extends Activity> activityClass){
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("android.activity.launchSource", "android-internal");
        startActivity(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(null);
        }
        Activity activity;
        while ((activity = getParent()) != null){
            activity.finish();
        }
        finish();
    }
}