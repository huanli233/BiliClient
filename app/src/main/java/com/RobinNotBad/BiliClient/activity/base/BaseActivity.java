package com.RobinNotBad.BiliClient.activity.base;

import static com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity.getRelayDynamicLauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewConfigurationCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.event.SnackEvent;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomGridManager;
import com.RobinNotBad.BiliClient.ui.widget.recycler.CustomLinearManager;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class BaseActivity extends AppCompatActivity {
    public int window_width, window_height;
    public Context old_context;
    public final ActivityResultLauncher<Intent> relayDynamicLauncher = getRelayDynamicLauncher(this);
    public boolean force_single_column = false;

    //调整应用内dpi的代码，其他Activity要继承于BaseActivity才能调大小
    @Override
    protected void attachBaseContext(Context newBase) {
        old_context = newBase;
        super.attachBaseContext(BiliTerminal.getFitDisplayContext(newBase));
    }

    //调整页面边距，参考了hankmi的方式
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(SharedPreferencesUtil.getBoolean("ui_landscape", false)
                ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);

        int paddingH_percent = SharedPreferencesUtil.getInt("paddingH_percent", 0);
        int paddingV_percent = SharedPreferencesUtil.getInt("paddingV_percent", 0);

        View rootView = this.getWindow().getDecorView().getRootView();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        if(BiliTerminal.getSystemSdk() >= 17) display.getRealMetrics(metrics);
        else display.getMetrics(metrics);

        int scrW = metrics.widthPixels;
        int scrH = metrics.heightPixels;
        if (paddingH_percent != 0 || paddingV_percent != 0) {
            Log.e("debug", "调整边距");
            int paddingH = scrW * paddingH_percent / 100;
            int paddingV = scrH * paddingV_percent / 100;
            window_width = scrW - paddingH;
            window_height = scrH - paddingV;
            rootView.setPadding(paddingH, paddingV, paddingH, paddingV);
        } else {
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
        if (!SharedPreferencesUtil.getBoolean("back_disable", false)) super.onBackPressed();
    }

    public void setPageName(String name) {
        TextView textView = findViewById(R.id.pageName);
        if (textView != null) textView.setText(name);
    }

    public void setTopbarExit() {
        View view = findViewById(R.id.top);
        if(view==null) return;
        if(Build.VERSION.SDK_INT > 17 && view.hasOnClickListeners()) return;
        view.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT < 17 || !isDestroyed()) {
                finish();
            }
        });
        Log.e("debug", "set_exit");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU) {
            if (Build.VERSION.SDK_INT < 17 || !isDestroyed()) {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void report(Exception e) {
        runOnUiThread(() -> MsgUtil.err(e));
    }

    private boolean eventBusInit = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (!(this instanceof InstanceActivity)) setTopbarExit();
        if (eventBusEnabled() && !eventBusInit) {
            EventBus.getDefault().register(this);
            eventBusInit = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventBusEnabled()) {
            SnackEvent snackEvent;
            if ((snackEvent = EventBus.getDefault().getStickyEvent(SnackEvent.class)) != null)
                onEvent(snackEvent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventBusInit) {
            EventBus.getDefault().unregister(this);
            eventBusInit = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onEvent(SnackEvent event) {
        if (isFinishing()) return;
        MsgUtil.processSnackEvent(event, getWindow().getDecorView().getRootView());
    }

    protected boolean eventBusEnabled() {
        return SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, true);
    }

    public void setDensity(int targetDensityDpi) {
        Resources resources = getResources();

        if (resources.getConfiguration().densityDpi == targetDensityDpi) return;

        Configuration configuration = resources.getConfiguration();
        configuration.densityDpi = targetDensityDpi;
        configuration.fontScale = 1f;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    protected void asyncInflate(int id, InflateCallBack callBack) {
        setContentView(R.layout.activity_loading);
        new AsyncLayoutInflaterX(this).inflate(id, null, (view, layoutId, parent) -> {
            setContentView(view);
            if (this instanceof InstanceActivity) {
                ((InstanceActivity) this).setMenuClick();
            } else {
                setTopbarExit();
            }
            callBack.finishInflate(view, layoutId);
        });
    }

    protected interface InflateCallBack {
        void finishInflate(View view, int id);
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return SharedPreferencesUtil.getBoolean("ui_landscape", false) && !force_single_column
                ? new CustomGridManager(this, 3)
                : new CustomLinearManager(this);
    }

    public void setForceSingleColumn() {
        force_single_column = true;
    }
    
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        //自动适配表冠
        if(BiliTerminal.getSystemSdk() >= Build.VERSION_CODES.O) {    //既然不支持，那低版本直接跳过
            ViewGroup rootView = (ViewGroup) this.getWindow().getDecorView();
            setRotaryScroll(rootView);
        }
    }

    private void setRotaryScroll(View view) {
        if(view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            try {
                for (int i = 0; i < vp.getChildCount(); i++) {
                    View viewChild = vp.getChildAt(i);

                    viewChild.setOnGenericMotionListener((v, ev) -> {
                        if (ev.getAction() == MotionEvent.ACTION_SCROLL && ev.getSource() == InputDevice.SOURCE_ROTARY_ENCODER) {
                            float delta = -ev.getAxisValue(MotionEvent.AXIS_SCROLL)
                                    * ViewConfigurationCompat.getScaledVerticalScrollFactor(
                                    ViewConfiguration.get(BiliTerminal.context),
                                    BiliTerminal.context) * 2;

                            boolean set = true;
                            if (viewChild instanceof ScrollView)
                                ((ScrollView) viewChild).smoothScrollBy(0, Math.round(delta));
                            else if (viewChild instanceof NestedScrollView)
                                ((NestedScrollView) viewChild).smoothScrollBy(0, Math.round(delta));
                            else if (viewChild instanceof RecyclerView)
                                ((RecyclerView) viewChild).smoothScrollBy(0, Math.round(delta));
                            else if (viewChild instanceof ListView)
                                ((ListView) viewChild).smoothScrollBy(0, Math.round(delta));
                            else set = false;

                            if (set) viewChild.requestFocus();

                            return true;
                        }

                        return false;
                    });

                    setRotaryScroll(viewChild);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } 
    }

    @Override
    public boolean isDestroyed(){
        return getLifecycle().getCurrentState().equals(Lifecycle.State.DESTROYED);
    }
}
