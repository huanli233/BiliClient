package com.RobinNotBad.BiliClient.activity.base;

import static com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity.getRelayDynamicLauncher;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.tutorial.TutorialActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.greenrobot.eventbus.EventBus;


public class BaseActivity extends AppCompatActivity {
    public int window_width, window_height;
    public Context old_context;
    public ActivityResultLauncher<Intent> relayDynamicLauncher = getRelayDynamicLauncher(this);

    //调整应用内dpi的代码，其他Activity要继承于BaseActivity才能调大小
    @Override
    protected void attachBaseContext(Context newBase) {
        old_context = newBase;
        super.attachBaseContext(BiliTerminal.getFitDisplayContext(newBase));
    }

    //调整页面边距，参考了hankmi的方式
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int paddingH_percent = SharedPreferencesUtil.getInt("paddingH_percent",0);
        int paddingV_percent = SharedPreferencesUtil.getInt("paddingV_percent",0);

        View rootView = this.getWindow().getDecorView().getRootView();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);

        int scrW = metrics.widthPixels;
        int scrH = metrics.heightPixels;
        if(paddingH_percent != 0 || paddingV_percent != 0) {
            Log.e("debug","调整边距");
            int paddingH = scrW * paddingH_percent / 100;
            int paddingV = scrH * paddingV_percent / 100;
            window_width = scrW - paddingH;
            window_height = scrH - paddingV;
            rootView.setPadding(paddingH, paddingV, paddingH, paddingV);
        }
        else {
            window_width = scrW;
            window_height = scrH;
        }

        // 随便加的
        int density;
        if ((density = SharedPreferencesUtil.getInt("density", -1)) >= 72) {
            setDensity(density);
        }
    }

    @Override
    public void onBackPressed() {
        if(!SharedPreferencesUtil.getBoolean("back_disable",false)) super.onBackPressed();
    }

    public void setPageName(String name){
        TextView textView = findViewById(R.id.pageName);
        if(textView!=null) textView.setText(name);
    }

    public void setTopbarExit(){
        View view = findViewById(R.id.top);
        if(view!=null && !view.hasOnClickListeners()){
            view.setOnClickListener(view1->finish());
            Log.e("debug","set_exit");
        }
    }

    public void report(Exception e){runOnUiThread(()-> MsgUtil.err(e,this));}

    private boolean eventBusInit = false;
    @Override
    protected void onStart() {
        super.onStart();
        if(!(this instanceof InstanceActivity)) setTopbarExit();
        if (eventBusEnabled() && !eventBusInit) {
            EventBus.getDefault().register(this);
            eventBusInit = true;
        }
    }

    protected boolean eventBusEnabled() {
        return false;
    }

    public void setDensity(int targetDensityDpi) {
        Resources resources = getResources();

        if (resources.getConfiguration().densityDpi == targetDensityDpi) return;

        Configuration configuration = resources.getConfiguration();
        configuration.densityDpi = targetDensityDpi;
        configuration.fontScale = 1f;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
