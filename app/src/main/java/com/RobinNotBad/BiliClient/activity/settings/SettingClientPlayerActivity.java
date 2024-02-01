package com.RobinNotBad.BiliClient.activity.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import android.widget.EditText;
import android.widget.RadioButton;
import androidx.appcompat.widget.SwitchCompat;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.player.PlayerActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

public class SettingClientPlayerActivity extends BaseActivity {
    private RadioButton SWtexture,SWsurface,SWhard,SWsoft,SWopensles,SWaudiotrack,SWonline,SWdownload,SWprivate,SWpublic;
    private EditText DMmaxline,danmakusize,danmakuspeed,danmaku_transparency;
    private SwitchCompat SWLClick,SWloop,danmaku_allowoverlap,danmaku_mergeduplicate,ui_round,ui_showRotateBtn,ui_showDanmakuBtn,ui_showLoopBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_clientplayer);
        Log.e("debug","设置内置播放器");

        findViewById(R.id.top).setOnClickListener(view -> finish());
        
        SWtexture = findViewById(R.id.SWtexture);
        SWsurface = findViewById(R.id.SWsurface);
        SWhard = findViewById(R.id.SWhard);
        SWsoft = findViewById(R.id.SWsoft);
        SWopensles = findViewById(R.id.SWopensles);
        SWaudiotrack = findViewById(R.id.SWaudiotrack);
        SWonline = findViewById(R.id.SWonline);
        SWdownload = findViewById(R.id.SWdownload);
        SWprivate = findViewById(R.id.SWprivate);
        SWpublic = findViewById(R.id.SWpublic);
        SWLClick = findViewById(R.id.SWLClick);
        SWloop = findViewById(R.id.SWloop);
        DMmaxline = findViewById(R.id.DMmaxline);
        danmakusize = findViewById(R.id.danmakusize);
        danmakuspeed = findViewById(R.id.danmakuspeed);
        danmaku_transparency = findViewById(R.id.danmaku_transparency);
        danmaku_allowoverlap = findViewById(R.id.danmaku_allowoverlap);
        danmaku_mergeduplicate = findViewById(R.id.danmaku_mergeduplicate);
        ui_round = findViewById(R.id.ui_round);
        ui_showRotateBtn = findViewById(R.id.ui_showRotateBtn);
        ui_showDanmakuBtn = findViewById(R.id.ui_showDanmakuBtn);
        ui_showLoopBtn = findViewById(R.id.ui_showLoopBtn);
        
        SWtexture.setChecked(SharedPreferencesUtil.getBoolean("player_display",false));
        SWsurface.setChecked(!SharedPreferencesUtil.getBoolean("player_display",false));
        SWhard.setChecked(SharedPreferencesUtil.getBoolean("player_codec",true));
        SWsoft.setChecked(!SharedPreferencesUtil.getBoolean("player_codec",true));
        SWopensles.setChecked(SharedPreferencesUtil.getBoolean("player_audio",false));
        SWaudiotrack.setChecked(!SharedPreferencesUtil.getBoolean("player_audio",false));
        SWonline.setChecked(SharedPreferencesUtil.getBoolean("player_online",true));
        SWdownload.setChecked(!SharedPreferencesUtil.getBoolean("player_online",true));
        SWprivate.setChecked(SharedPreferencesUtil.getBoolean("player_privatepath",true));
        SWpublic.setChecked(!SharedPreferencesUtil.getBoolean("player_privatepath",true));
        SWLClick.setChecked(SharedPreferencesUtil.getBoolean("player_longclick",false));
        SWloop.setChecked(SharedPreferencesUtil.getBoolean("player_loop",false));
        DMmaxline.setText(SharedPreferencesUtil.getInt("player_danmaku_maxline",25)+"");
        danmakusize.setText((SharedPreferencesUtil.getFloat("player_danmaku_size",1.0f))+"");
        danmakuspeed.setText((SharedPreferencesUtil.getFloat("player_danmaku_speed",1.0f))+"");
        danmaku_transparency.setText((SharedPreferencesUtil.getFloat("player_danmaku_transparency",0.5f)*100)+"");
        danmaku_allowoverlap.setChecked(SharedPreferencesUtil.getBoolean("player_danmaku_allowoverlap",true));
        danmaku_mergeduplicate.setChecked(SharedPreferencesUtil.getBoolean("player_danmaku_mergeduplicate",false));
        ui_round.setChecked(SharedPreferencesUtil.getBoolean("player_ui_round",false));
        ui_showRotateBtn.setChecked(SharedPreferencesUtil.getBoolean("player_ui_showRotateBtn",true));
        ui_showDanmakuBtn.setChecked(SharedPreferencesUtil.getBoolean("player_ui_showDanmakuBtn",true));
        ui_showLoopBtn.setChecked(SharedPreferencesUtil.getBoolean("player_ui_showLoopBtn",true));
        
        SWpublic.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(!Environment.isExternalStorageManager()) {
                    SWpublic.setChecked(false);
                    SWprivate.setChecked(true);
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setTitle("请授权...")
                            .setMessage("使用此选项需要“访问所有文件”权限。\n" +
                                "你的安卓系统版本较高，需要在设置中手动打开“访问所有文件”（或类似名称）的权限开关。\n" +
                                "点击<确定>，将跳转到设置界面。如果跳转不成功，请手动进入设置打开。\n" +
                                "如果找不到该选项，那就老老实实用私有目录吧(T_T)")
                            .setPositiveButton("确定", (dialogInterface, i) -> request())
                            .setNegativeButton("取消", (dialogInterface, i) -> {})
                            .create();
                    alertDialog.show();
                }
            }
        });
        
        findViewById(R.id.preview).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setClass(this,PlayerActivity.class);
            intent.putExtra("cookie", SharedPreferencesUtil.getString("cookies", ""));
            intent.putExtra("mode", "1");
            intent.putExtra("url", "114514test");
            intent.putExtra("danmaku", "114514test");
            intent.putExtra("title", "页面预览");
            startActivity(intent);
        });
    }
    
    private void request(){
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + this.getPackageName()));
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtil.putBoolean("player_display",SWtexture.isChecked());
        SharedPreferencesUtil.putBoolean("player_codec",SWhard.isChecked());
        SharedPreferencesUtil.putBoolean("player_audio",SWopensles.isChecked());
        SharedPreferencesUtil.putBoolean("player_online",SWonline.isChecked());
        SharedPreferencesUtil.putBoolean("player_privatepath",SWprivate.isChecked());
        SharedPreferencesUtil.putBoolean("player_longclick",SWLClick.isChecked());
        SharedPreferencesUtil.putBoolean("player_loop",SWloop.isChecked());
        
        String newline = DMmaxline.getText().toString();
        String newtextsize = danmakusize.getText().toString();
        String newspeed = danmakuspeed.getText().toString();
        String newtransparency = danmaku_transparency.getText().toString();
        if (newspeed.length()<=0) newspeed = "1.0";
        if (newtextsize.length()<=0) newtextsize = "1.0";
        if (newtransparency.length()<=0) newtransparency = "50";
        if (newline.length()<=0) newline = "0";
        SharedPreferencesUtil.putInt("player_danmaku_maxline",Integer.valueOf(newline));
        SharedPreferencesUtil.putFloat("player_danmaku_size",Float.parseFloat(newtextsize));
        SharedPreferencesUtil.putFloat("player_danmaku_speed",Float.parseFloat(newspeed));
        SharedPreferencesUtil.putFloat("player_danmaku_transparency",Float.parseFloat(newtransparency)/100f);
        SharedPreferencesUtil.putBoolean("player_danmaku_allowoverlap",danmaku_allowoverlap.isChecked());
        SharedPreferencesUtil.putBoolean("player_danmaku_mergeduplicate",danmaku_mergeduplicate.isChecked());
        SharedPreferencesUtil.putBoolean("player_ui_round",ui_round.isChecked());
        SharedPreferencesUtil.putBoolean("player_ui_showRotateBtn",ui_showRotateBtn.isChecked());
        SharedPreferencesUtil.putBoolean("player_ui_showDanmakuBtn",ui_showDanmakuBtn.isChecked());
        SharedPreferencesUtil.putBoolean("player_ui_showLoopBtn",ui_showLoopBtn.isChecked());
        
        MsgUtil.toast("设置已保存喵~",this);
    }
}