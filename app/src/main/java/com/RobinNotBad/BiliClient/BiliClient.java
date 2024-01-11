package com.RobinNotBad.BiliClient;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.HashSet;
import java.util.Set;

public class BiliClient extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private static Set<InstanceActivity> activityInstances;
    @Override
    public void onCreate() {
        super.onCreate();
        if(context==null && getApplicationContext()!=null){
            context = getApplicationContext();
            ErrorCatch errorCatch = ErrorCatch.getInstance();
            errorCatch.init(getApplicationContext());
            SharedPreferencesUtil.initSharedPrefs(this);
            activityInstances = new HashSet<>();
        }
    }

    public static void addInstance(InstanceActivity instanceActivity){
        activityInstances.add(instanceActivity);
    }
    public static void removeInstance(InstanceActivity instanceActivity){
        activityInstances.remove(instanceActivity);
    }

    @Nullable
    public static InstanceActivity getInstance(Class<? extends InstanceActivity> cls) {
        for (InstanceActivity instanceActivity : activityInstances) {
            if (instanceActivity != null) {
                if (cls.isInstance(instanceActivity)){
                    return instanceActivity;
                }
            }
        }
        return null;
    }

    /**
     * 重写attachBaseContext方法，用于调整应用内dpi
     * 尝试下这种风格代码是否会导致低版本设备异常
     * @param old The origin context.
     */
    public static Context getFitDisplayContext(Context old){
        Context newContext = old;
        float dpiTimes = SharedPreferencesUtil.getFloat("dpi", 1.0F);
        if(dpiTimes == 1.0F) return newContext;
        try{
            DisplayMetrics displayMetrics = old.getResources().getDisplayMetrics();
            Configuration configuration = old.getResources().getConfiguration();
            configuration.densityDpi = (int)(displayMetrics.densityDpi * dpiTimes);
            newContext = old.createConfigurationContext(configuration);
        }catch (Exception e){
            Toast.makeText(newContext, "调整缩放失败, 请联系开发者", Toast.LENGTH_SHORT).show();
            Log.wtf("调整dpi", "不支持application");
        }
        return newContext;
    }
}
