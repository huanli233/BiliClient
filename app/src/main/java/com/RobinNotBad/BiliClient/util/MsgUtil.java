package com.RobinNotBad.BiliClient.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RobinNotBad.BiliClient.activity.DialogActivity;
import com.RobinNotBad.BiliClient.activity.ShowTextActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;

import org.json.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MsgUtil {
    private static Toast toast;

    public static void toast(String str, Context context) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void toastLong(String str, Context context) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
        toast.show();
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

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    public static Snackbar createSnack(View view, CharSequence text, int duration) {
        Snackbar snackbar;
        (snackbar = Snackbar.make(view, text, duration))
                .setBackgroundTint(Color.parseColor("#90000000"))
                .setAction("x", (view1 -> snackbar.dismiss()))
                .setTextMaxLines(3);
        View snackBarView = snackbar.getView();
        snackBarView.setOnTouchListener(((view12, motionEvent) -> false));
        TextView actionView = ((SnackbarContentLayout) ((FrameLayout) snackBarView).getChildAt(0)).getActionView();
        actionView.setTextSize(12);
        actionView.setMinWidth(ToolsUtil.dp2px(30, view.getContext()));
        actionView.setMinimumWidth(ToolsUtil.dp2px(30, view.getContext()));
        actionView.setMaxWidth(ToolsUtil.dp2px(48, view.getContext()));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) actionView.getLayoutParams();
        layoutParams.setMarginStart(0);
        TextView msgView = ((SnackbarContentLayout) ((FrameLayout) snackBarView).getChildAt(0)).getMessageView();
        msgView.setTextSize(10);
        msgView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams layoutParams1 = (LinearLayout.LayoutParams) msgView.getLayoutParams();
        layoutParams1.setMarginEnd(0);
        return snackbar;
    }

    public static void err(Throwable e, Context context) {
        e.printStackTrace();
        if (e instanceof IOException) toast("网络错误(＃°Д°)", context);
        else if (e instanceof JSONException) {
            if (SharedPreferencesUtil.getBoolean("develop_error_detailed", false)) {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                showText(context, "数据解析错误", writer.toString());
            } else if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                toastLong("解析错误，可登陆后再次尝试", context);
            } else if (e.toString().replace("org.json.JSONException:", "").contains("-352"))
                toastLong("账号疑似被风控", context);
            else
                toastLong("数据解析错误：\n" + e.toString().replace("org.json.JSONException:", ""), context);
        } else toastLong("错误：" + e, context);
    }

    public static void showText(Context context, String title, String text) {
        Intent testIntent = new Intent()
                .setClass(context, ShowTextActivity.class)
                .putExtra("title", title)
                .putExtra("content", text);
        context.startActivity(testIntent);
    }

    public static void showDialog(Context context, String title, String content) {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        context.startActivity(intent);
    }

    public static void showDialog(Context context, String title, String content, int wait_time) {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("wait_time", wait_time);
        context.startActivity(intent);
    }

}
