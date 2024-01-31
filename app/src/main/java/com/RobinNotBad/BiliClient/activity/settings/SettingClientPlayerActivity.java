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
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

public class SettingClientPlayerActivity extends BaseActivity {
    private RadioButton SWtexture,SWsurface,SWhard,SWsoft,SWopensles,SWaudiotrack,SWonline,SWdownload,SWprivate,SWpublic;
    private EditText DMmaxline;
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
        DMmaxline = findViewById(R.id.DMmaxline);
        
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
        DMmaxline.setText(SharedPreferencesUtil.getInt("player_danmaku_maxline",25)+"");
        
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
        
        String newline = DMmaxline.getText().toString();
        if (newline.length()<=0) newline = "0";
        SharedPreferencesUtil.putInt("player_danmaku_maxline",Integer.valueOf(newline));
        MsgUtil.toast("设置已保存喵~",this);
    }
}