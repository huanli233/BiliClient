package com.RobinNotBad.BiliClient.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.activity.DialogActivity;
import com.RobinNotBad.BiliClient.activity.ShowTextActivity;
import com.RobinNotBad.BiliClient.event.SnackEvent;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MsgUtil {
    private static Toast toast;

    public static void showMsg(String str) {
        if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, false)) {
            EventBus.getDefault().postSticky(new SnackEvent(str));
        } else {
            toast(str, BiliTerminal.context);
        }
    }

    public static void showMsgLong(String str) {
        if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, false)) {
            EventBus.getDefault().postSticky(new SnackEvent(str));
        } else {
            toastLong(str, BiliTerminal.context);
        }
    }

    public static void toast(String str, Context context) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            CenterThreadPool.runOnUiThread(() -> toastInternal(str, context));
        } else {
            toastInternal(str, context);
        }
    }

    public static void toastLong(String str, Context context) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            CenterThreadPool.runOnUiThread(() -> toastLongInternal(str, context));
        } else {
            toastLongInternal(str, context);
        }
    }

    private static void toastInternal(String str, Context context) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        toast.show();
    }

    private static void toastLongInternal(String str, Context context) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void processSnackEvent(SnackEvent snackEvent, View view) {
        long currentTime = System.currentTimeMillis();
        int duration = 2750;
        if (snackEvent.getDuration() > 0) {
            duration = snackEvent.getDuration();
        } else if (snackEvent.getDuration() == Snackbar.LENGTH_SHORT) {
            duration = 1950;
        } else if (snackEvent.getDuration() == Snackbar.LENGTH_INDEFINITE) {
            duration = Integer.MAX_VALUE;
        }
        long endTime = snackEvent.getStartTime() + duration;
        if (currentTime >= endTime) {
            EventBus.getDefault().removeStickyEvent(snackEvent);
        } else {
            createSnack(view, snackEvent.getMessage(), (int) (endTime - currentTime))
                    .show();
        }

    }

    public static void snackText(View view, CharSequence text) {
        createSnack(view, text).show();
    }

    public static void snackTextLong(View view, CharSequence text) {
        createSnack(view, text, Snackbar.LENGTH_LONG).show();
    }

    public static Snackbar createSnack(View view, CharSequence text) {
        return createSnack(view, text, Snackbar.LENGTH_SHORT);
    }

    public static Snackbar createSnack(View view, CharSequence text, int duration) {
        return createSnack(view, text, duration, null);
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    public static Snackbar createSnack(View view, CharSequence text, int duration, Action action) {
        Snackbar snackbar;
        snackbar = Snackbar.make(view, text, duration);
        snackbar.setBackgroundTint(Color.argb(0x85,0x80,0x80,0x80));
        snackbar.setTextColor(Color.rgb(0xeb,0xe0,0xe2));
        View snackBarView = snackbar.getView();
        snackBarView.setOnTouchListener((v, event) -> false);
        snackBarView.setPadding(ToolsUtil.dp2px(6), 0, 0, 0);
        SnackbarContentLayout contentLayout = ((SnackbarContentLayout) ((FrameLayout) snackBarView).getChildAt(0));

        if (action != null) snackbar.setAction(action.getText(), action.getOnClickListener());
        else if (duration == Snackbar.LENGTH_INDEFINITE || duration >= 5000) {
            snackbar.setAction("x", (view1 -> snackbar.dismiss()));
            Button actionView = contentLayout.getActionView();
            //actionView.setTextSize(ToolsUtil.sp2px(13));
            actionView.setMinWidth(ToolsUtil.dp2px(30));
            actionView.setMinimumWidth(ToolsUtil.dp2px(30));
            actionView.setMaxWidth(ToolsUtil.dp2px(48));
            actionView.setPadding(0, 0, ToolsUtil.dp2px(4), 0);
        }

        TextView msgView = contentLayout.getMessageView();
        msgView.setTextSize(13);
        msgView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        msgView.setPadding(0, 0, ToolsUtil.dp2px(4), 0);

        return snackbar;
    }

    public static void err(Throwable e){
        err("",e);
    }
    public static void err(String desc, Throwable e) {
        Log.e("BiliClient", e.getMessage(), e);
        Context context = BiliTerminal.context;
        if (e instanceof IOException) showMsg(desc + "网络错误(＃°Д°)");
        else if (e instanceof JSONException) {
            if (SharedPreferencesUtil.getBoolean("dev_jsonerr_detailed", false)) {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                showText(desc + "数据解析错误", writer.toString());
            } else if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                showMsgLong(desc + "解析错误，可登陆后再次尝试");
            } else if (e.toString().replace("org.json.JSONException:", "").contains("-352"))
                showMsgLong(desc + "账号疑似被风控");
            else
                showMsgLong(desc + "数据解析错误：\n" + e.toString().replace("org.json.JSONException:", ""));
        } else showMsgLong(desc + "错误：" + e);
    }

    public static void showText(String title, String text) {
        Context context = BiliTerminal.context;
        Intent testIntent = new Intent()
                .setClass(context, ShowTextActivity.class)
                .putExtra("title", title)
                .putExtra("content", text);
        context.startActivity(testIntent);
    }

    public static void showDialog(String title, String content) {
        Context context = BiliTerminal.context;
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        context.startActivity(intent);
    }

    public static void showDialog(String title, String content, int wait_time) {
        Context context = BiliTerminal.context;
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("wait_time", wait_time);
        context.startActivity(intent);
    }

    public static class Action {
        private String text;
        private View.OnClickListener onClickListener;

        public Action(String text, View.OnClickListener onClickListener) {
            this.text = text;
            this.onClickListener = onClickListener;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public View.OnClickListener getOnClickListener() {
            return onClickListener;
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            this.onClickListener = onClickListener;
        }
    }

}
