package com.RobinNotBad.BiliClient.activity.settings;

import android.os.Bundle;
import android.util.Log;

import android.widget.RadioButton;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

public class SettingClientPlayerActivity extends BaseActivity {
    private RadioButton SWtexture,SWsurface,SWhard,SWsoft;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_clientplayer);
        Log.e("debug","设置设置内置播放器~");

        findViewById(R.id.top).setOnClickListener(view -> finish());
        
        SWtexture = findViewById(R.id.SWtexture);
        SWsurface = findViewById(R.id.SWsurface);
        SWhard = findViewById(R.id.SWhard);
        SWsoft = findViewById(R.id.SWsoft);
        
        SWtexture.setChecked(SharedPreferencesUtil.getBoolean("player_display",false));
        SWsurface.setChecked(!SharedPreferencesUtil.getBoolean("player_display",false));
        SWhard.setChecked(SharedPreferencesUtil.getBoolean("player_codec",true));
        SWsoft.setChecked(!SharedPreferencesUtil.getBoolean("player_codec",true));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesUtil.putBoolean("player_display",SWtexture.isChecked());
        SharedPreferencesUtil.putBoolean("player_codec",SWhard.isChecked());
        MsgUtil.toast("设置已保存喵~",this);
    }
}