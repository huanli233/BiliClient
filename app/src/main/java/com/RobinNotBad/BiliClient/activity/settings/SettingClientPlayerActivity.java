package com.RobinNotBad.BiliClient.activity.settings;

import android.os.Bundle;
import android.util.Log;

import android.widget.RadioButton;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

public class SettingClientPlayerActivity extends BaseActivity {
    private RadioButton SWtexture,SWsurface,SWhard,SWsoft,SWopensles,SWaudiotrack,SWonline,SWdownload,SWprivate,SWpublic;
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
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtil.putBoolean("player_display",SWtexture.isChecked());
        SharedPreferencesUtil.putBoolean("player_codec",SWhard.isChecked());
        SharedPreferencesUtil.putBoolean("player_audio",SWopensles.isChecked());
        SharedPreferencesUtil.putBoolean("player_online",SWonline.isChecked());
        SharedPreferencesUtil.putBoolean("player_privatepath",SWprivate.isChecked());
        MsgUtil.toast("设置已保存喵~",this);
    }
}