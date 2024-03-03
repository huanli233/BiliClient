package com.RobinNotBad.BiliClient;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

public class BiliTerminal extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    @SuppressLint("StaticFieldLeak")
    public static InstanceActivity instance;

    @Override
    public void onCreate() {
        super.onCreate();
        if(context==null && getApplicationContext()!=null){
            context = getApplicationContext();
            if(Build.VERSION.SDK_INT > 19) {
                ErrorCatch errorCatch = ErrorCatch.getInstance();
                errorCatch.init(context);
            }
        }
    }

    public static void setInstance(InstanceActivity instanceActivity){
        instance = instanceActivity;
    }

    /**
     * 重写attachBaseContext方法，用于调整应用内dpi
     * 尝试下这种风格代码是否会导致低版本设备异常
     * @param old The origin context.
     */
    public static Context getFitDisplayContext(Context old){
        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.0F);
        if(dpiTimes == 1.0F) return old;
        try{
            DisplayMetrics displayMetrics = old.getResources().getDisplayMetrics();
            Configuration configuration = old.getResources().getConfiguration();
            configuration.densityDpi = (int)(displayMetrics.densityDpi * dpiTimes);
            return old.createConfigurationContext(configuration);
        }catch (Exception e){
            //MsgUtil.err(e,old);
            return old;
        }
    }

    public static int getVersion() throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
    }
}
