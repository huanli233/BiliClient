package com.RobinNotBad.BiliClient.activity.base;

import static com.RobinNotBad.BiliClient.activity.dynamic.DynamicActivity.getRelayDynamicLauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.RobinNotBad.BiliClient.util.Logu;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

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
                ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);

        int paddingH_percent = SharedPreferencesUtil.getInt("paddingH_percent", 0);
        int paddingV_percent = SharedPreferencesUtil.getInt("paddingV_percent", 0);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        if(Build.VERSION.SDK_INT >= 17) display.getRealMetrics(metrics);
        else display.getMetrics(metrics);

        int scrW = metrics.widthPixels;
        int scrH = metrics.heightPixels;
        if (paddingH_percent != 0 || paddingV_percent != 0) {
            Logu.d("debug", "调整边距");
            int paddingH = scrW * paddingH_percent / 100;
            int paddingT = scrH * paddingV_percent / 100;
            int paddingB = paddingT;
            if(SharedPreferencesUtil.getBoolean("player_ui_round", false))
                paddingB += scrH * 0.03;
            window_width = scrW - paddingH * 2;
            window_height = scrH - paddingT - paddingB;
            View rootView = this.getWindow().getDecorView().getRootView();
            rootView.setPadding(paddingH, paddingT, paddingH, paddingB);
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
        Logu.d("debug", "set_exit");
    }

    public void setRound(){
        //圆屏适配

        TextView pagename = findViewById(R.id.pageName);
        TextView clock = findViewById(R.id.timeText);
        if(pagename != null) {
            pagename.setMaxLines(1);
            pagename.setEllipsize(TextUtils.TruncateAt.END);
            if (SharedPreferencesUtil.getBoolean("player_ui_round", false)) {
                try {
                    ViewGroup.LayoutParams params = pagename.getLayoutParams();
                    int paddingH = (int) (window_width * 0.18);
                    int paddingV = (int) (window_width * 0.03);
                    pagename.setPadding(paddingH, paddingV, paddingH, 0);
                    if (params instanceof RelativeLayout.LayoutParams) {
                        RelativeLayout.LayoutParams clockParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        clockParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        clock.setLayoutParams(clockParams);
                        clock.setAlpha(0.85f);
                        clock.setTextSize(12);

                        RelativeLayout.LayoutParams pnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        pnParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        pnParams.topMargin = (int) (window_height * 0.01) + ToolsUtil.sp2px(12);
                        pnParams.bottomMargin = (int) (window_height * 0.01);
                        pagename.setLayoutParams(pnParams);
                        pagename.setPadding(0,0,ToolsUtil.dp2px(5),0);
                        Logu.d("round", "ok");
                    }
                } catch (Throwable e){
                    MsgUtil.err("圆屏适配执行错误：", e);
                }
            }
        }
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
        runOnUiThread(() -> MsgUtil.err(getClassName(), e));
    }

    private boolean eventBusInit = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (!(this instanceof InstanceActivity)) setTopbarExit();
        setRound();
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
        if (isDestroyed()) return;
        MsgUtil.processSnackEvent(event, getWindow().getDecorView().getRootView());
    }

    protected boolean eventBusEnabled() {
        return SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, true);
    }

    public void setDensity(int targetDensityDpi) {
        if(Build.VERSION.SDK_INT < 17) return;
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

            if (this instanceof InstanceActivity) ((InstanceActivity) this).setMenuClick();
            else setTopbarExit();

            setRound();
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
        if(SharedPreferencesUtil.getBoolean("ui_rotatory_enable", false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {    //既然不支持，那低版本直接跳过
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

                    float multiple = -114;
                    if (viewChild instanceof ScrollView || viewChild instanceof NestedScrollView) multiple=SharedPreferencesUtil.getFloat("ui_rotatory_scroll",0);
                    if (viewChild instanceof RecyclerView) multiple=SharedPreferencesUtil.getFloat("ui_rotatory_recycler",0);
                    if (viewChild instanceof ListView) multiple=SharedPreferencesUtil.getFloat("ui_rotatory_list",0);

                    if(multiple==-114) setRotaryScroll(viewChild);  //不符合上面的情况说明不是可滑动列表
                    if(multiple<=0) return;    //负值和0都不执行

                    float finalMultiple = multiple;
                    viewChild.setOnGenericMotionListener((v, ev) -> {
                        if (ev.getAction() == MotionEvent.ACTION_SCROLL && ev.getSource() == InputDevice.SOURCE_ROTARY_ENCODER) {
                            float delta = -ev.getAxisValue(MotionEvent.AXIS_SCROLL) * ViewConfigurationCompat.getScaledVerticalScrollFactor(ViewConfiguration.get(this),
                                    this) * 2;

                            if (viewChild instanceof ScrollView)
                                ((ScrollView) viewChild).smoothScrollBy(0, Math.round(delta * finalMultiple));
                            else if (viewChild instanceof NestedScrollView)
                                ((NestedScrollView) viewChild).smoothScrollBy(0, Math.round(delta * finalMultiple));
                            else if (viewChild instanceof RecyclerView)
                                ((RecyclerView) viewChild).smoothScrollBy(0, Math.round(delta * finalMultiple));
                            else ((ListView) viewChild).smoothScrollBy(0, Math.round(delta * finalMultiple));

                            viewChild.requestFocus();

                            return true;
                        }

                        return false;
                    });
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        } 
    }

    @Override
    public boolean isDestroyed(){
        return getLifecycle().getCurrentState().equals(Lifecycle.State.DESTROYED) || isFinishing();
    }

    public String getClassName(){
        return this.getClass().getSimpleName();
    }
}
