package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.player.PlayerActivity;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingPlayerInsideActivity extends BaseActivity {
    private MaterialRadioButton SWtexture, SWsurface, SWhard, SWsoft, SWopensles, SWaudiotrack;
    private EditText DMmaxline, danmakusize, danmakuspeed, danmaku_transparency;
    private SwitchMaterial fromLast, online_total, SWLClick, SWloop, ui_round, SWbackground, SWautolandscape, SWscale, SWdoublemove, danmaku_allowoverlap, danmaku_mergeduplicate, ui_showRotateBtn, ui_showDanmakuBtn, ui_showLoopBtn;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_setting_insideplayer, null, (layoutView, resId, parent) -> {
            setContentView(layoutView);
            setTopbarExit();
            Log.e("debug", "设置内置播放器");

            SWLClick = findViewById(R.id.SWLClick);
            SWloop = findViewById(R.id.SWloop);
            ui_round = findViewById(R.id.ui_round);
            SWbackground = findViewById(R.id.SWbackground);
            SWautolandscape = findViewById(R.id.SWautolandscape);
            SWscale = findViewById(R.id.SWscale);
            SWdoublemove = findViewById(R.id.SWdoublemove);
            online_total = findViewById(R.id.online_total);
            fromLast = findViewById(R.id.fromLast);

            SWtexture = findViewById(R.id.SWtexture);
            SWsurface = findViewById(R.id.SWsurface);
            SWhard = findViewById(R.id.SWhard);
            SWsoft = findViewById(R.id.SWsoft);
            SWopensles = findViewById(R.id.SWopensles);
            SWaudiotrack = findViewById(R.id.SWaudiotrack);

            DMmaxline = findViewById(R.id.DMmaxline);
            danmakusize = findViewById(R.id.danmakusize);
            danmakuspeed = findViewById(R.id.danmakuspeed);
            danmaku_transparency = findViewById(R.id.danmaku_transparency);
            danmaku_allowoverlap = findViewById(R.id.danmaku_allowoverlap);
            danmaku_mergeduplicate = findViewById(R.id.danmaku_mergeduplicate);
            ui_showRotateBtn = findViewById(R.id.ui_showRotateBtn);
            ui_showDanmakuBtn = findViewById(R.id.ui_showDanmakuBtn);
            ui_showLoopBtn = findViewById(R.id.ui_showLoopBtn);
            fromLast = findViewById(R.id.fromLast);

            SWaudiotrack.setChecked(!SharedPreferencesUtil.getBoolean("player_audio", false));
            SWLClick.setChecked(SharedPreferencesUtil.getBoolean("player_longclick", true));
            SWloop.setChecked(SharedPreferencesUtil.getBoolean("player_loop", false));
            ui_round.setChecked(SharedPreferencesUtil.getBoolean("player_ui_round", false));
            SWbackground.setChecked(SharedPreferencesUtil.getBoolean("player_background", false));
            SWautolandscape.setChecked(SharedPreferencesUtil.getBoolean("player_autolandscape", false));
            SWscale.setChecked(SharedPreferencesUtil.getBoolean("player_scale", true));
            SWdoublemove.setChecked(SharedPreferencesUtil.getBoolean("player_doublemove", false));
            online_total.setChecked(SharedPreferencesUtil.getBoolean("show_online", true));
            fromLast.setChecked(SharedPreferencesUtil.getBoolean("player_from_last", true));

            SWtexture.setChecked(SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT <= 19));
            SWsurface.setChecked(!SharedPreferencesUtil.getBoolean("player_display", Build.VERSION.SDK_INT <= 19));
            SWhard.setChecked(SharedPreferencesUtil.getBoolean("player_codec", true));
            SWsoft.setChecked(!SharedPreferencesUtil.getBoolean("player_codec", true));
            SWopensles.setChecked(SharedPreferencesUtil.getBoolean("player_audio", false));

            DMmaxline.setText(SharedPreferencesUtil.getInt("player_danmaku_maxline", 15) + "");
            danmakusize.setText((SharedPreferencesUtil.getFloat("player_danmaku_size", 1.0f)) + "");
            danmakuspeed.setText((SharedPreferencesUtil.getFloat("player_danmaku_speed", 1.0f)) + "");
            danmaku_transparency.setText((SharedPreferencesUtil.getFloat("player_danmaku_transparency", 0.5f) * 100) + "");
            danmaku_allowoverlap.setChecked(SharedPreferencesUtil.getBoolean("player_danmaku_allowoverlap", true));
            danmaku_mergeduplicate.setChecked(SharedPreferencesUtil.getBoolean("player_danmaku_mergeduplicate", false));
            ui_showRotateBtn.setChecked(SharedPreferencesUtil.getBoolean("player_ui_showRotateBtn", true));
            ui_showDanmakuBtn.setChecked(SharedPreferencesUtil.getBoolean("player_ui_showDanmakuBtn", true));
            ui_showLoopBtn.setChecked(SharedPreferencesUtil.getBoolean("player_ui_showLoopBtn", true));

            findViewById(R.id.preview).setOnClickListener(view -> {
                Intent intent = new Intent();
                intent.setClass(this, PlayerActivity.class);
                intent.putExtra("mode", -1);
                intent.putExtra("title", "页面预览");
                startActivity(intent);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtil.putBoolean("player_display", SWtexture.isChecked());
        SharedPreferencesUtil.putBoolean("player_codec", SWhard.isChecked());
        SharedPreferencesUtil.putBoolean("player_audio", SWopensles.isChecked());
        SharedPreferencesUtil.putBoolean("player_longclick", SWLClick.isChecked());
        SharedPreferencesUtil.putBoolean("player_loop", SWloop.isChecked());
        SharedPreferencesUtil.putBoolean("player_background", SWbackground.isChecked());
        SharedPreferencesUtil.putBoolean("player_autolandscape", SWautolandscape.isChecked());
        SharedPreferencesUtil.putBoolean("player_scale", SWscale.isChecked());
        SharedPreferencesUtil.putBoolean("player_doublemove", SWdoublemove.isChecked());
        SharedPreferencesUtil.putBoolean("show_online", online_total.isChecked());
        SharedPreferencesUtil.putBoolean("player_from_last", fromLast.isChecked());

        String newline = DMmaxline.getText().toString();
        String newtextsize = danmakusize.getText().toString();
        String newspeed = danmakuspeed.getText().toString();
        String newtransparency = danmaku_transparency.getText().toString();
        if (newspeed.length() <= 0) newspeed = "1.0";
        if (newtextsize.length() <= 0) newtextsize = "1.0";
        if (newtransparency.length() <= 0) newtransparency = "50";
        if (newline.length() <= 0) newline = "0";
        SharedPreferencesUtil.putInt("player_danmaku_maxline", Integer.parseInt(newline));
        SharedPreferencesUtil.putFloat("player_danmaku_size", Float.parseFloat(newtextsize));
        SharedPreferencesUtil.putFloat("player_danmaku_speed", Float.parseFloat(newspeed));
        SharedPreferencesUtil.putFloat("player_danmaku_transparency", Float.parseFloat(newtransparency) / 100f);
        SharedPreferencesUtil.putBoolean("player_danmaku_allowoverlap", danmaku_allowoverlap.isChecked());
        SharedPreferencesUtil.putBoolean("player_danmaku_mergeduplicate", danmaku_mergeduplicate.isChecked());
        SharedPreferencesUtil.putBoolean("player_ui_round", ui_round.isChecked());
        SharedPreferencesUtil.putBoolean("player_ui_showRotateBtn", ui_showRotateBtn.isChecked());
        SharedPreferencesUtil.putBoolean("player_ui_showDanmakuBtn", ui_showDanmakuBtn.isChecked());
        SharedPreferencesUtil.putBoolean("player_ui_showLoopBtn", ui_showLoopBtn.isChecked());

        MsgUtil.toast("设置已保存喵~", this);
    }
}