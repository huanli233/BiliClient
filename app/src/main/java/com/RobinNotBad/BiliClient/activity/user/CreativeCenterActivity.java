package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.CreativeCenterApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CreativeCenterActivity extends BaseActivity {
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creative_center);

        findViewById(R.id.top).setOnClickListener(view -> finish());

        CenterThreadPool.run(() -> {
            try {
                JSONObject stats = CreativeCenterApi.getVideoStat();
                runOnUiThread(() -> {
                    TextView[] textViews = {findViewById(R.id.totalFans_number),findViewById(R.id.totalClick_number),findViewById(R.id.totalLike_number),
                            findViewById(R.id.totalCoin_number),findViewById(R.id.totalFavourite_number),findViewById(R.id.totalShare_number),
                            findViewById(R.id.totalReply_number),findViewById(R.id.totalDm_number)};
                    String[] strings = {"fans","click","like","coin","fav","share","reply","dm"};
                    try {
                        for (int i = 0; i < textViews.length; i++)
                            textViews[i].setText(LittleToolsUtil.toWan(stats.getInt("total_"+strings[i]))
                                    + "(" + LittleToolsUtil.toWan(stats.getInt("incr_"+strings[i])) + ")");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> MsgUtil.toast("加载失败",this));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> MsgUtil.netErr(this));
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> MsgUtil.jsonErr(e,this));
            }

        });
    }
}
