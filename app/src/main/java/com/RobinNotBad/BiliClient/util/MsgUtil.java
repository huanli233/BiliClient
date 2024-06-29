package com.RobinNotBad.BiliClient.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    public static void showMsg(String str, Context context) {
        if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, false)) {
            EventBus.getDefault().postSticky(new SnackEvent(str));
        } else {
            toast(str, context);
        }
    }

    public static void showMsgLong(String str, Context context) {
        if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, false)) {
            EventBus.getDefault().postSticky(new SnackEvent(str));
        } else {
            toastLong(str, context);
        }
    }

    public static void toast(String str, Context context) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
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

    public static Snackbar createSnack(View view, CharSequence text, int duration) {
        return createSnack(view, text, duration, null);
    }

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    public static Snackbar createSnack(View view, CharSequence text, int duration, Action action) {
        Snackbar snackbar;
        (snackbar = Snackbar.make(view, text, duration))
                .setBackgroundTint(Color.parseColor("#85808080"))
                .setTextMaxLines(3);
        if (action != null) snackbar.setAction(action.getText(), action.getOnClickListener());
        else if (duration == Snackbar.LENGTH_INDEFINITE || duration >= 5000) snackbar.setAction("x", (view1 -> snackbar.dismiss()));
        View snackBarView = snackbar.getView();
        snackBarView.setOnTouchListener(((view12, motionEvent) -> false));
        SnackbarContentLayout contentLayout = ((SnackbarContentLayout) ((FrameLayout) snackBarView).getChildAt(0));
        Button actionView = contentLayout.getActionView();
        actionView.setTextSize(12);
        actionView.setMinWidth(ToolsUtil.dp2px(30, view.getContext()));
        actionView.setMinimumWidth(ToolsUtil.dp2px(30, view.getContext()));
        actionView.setMaxWidth(ToolsUtil.dp2px(48, view.getContext()));
        actionView.setPadding(0, 0, ToolsUtil.dp2px(4, view.getContext()), 0);
        actionView.setPaddingRelative(0, 0, ToolsUtil.dp2px(4, view.getContext()), 0);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) actionView.getLayoutParams();
        layoutParams.setMarginStart(0);
        layoutParams.setMargins(0, layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
        TextView msgView = contentLayout.getMessageView();
        msgView.setTextSize(11);
        msgView.setTypeface(null, Typeface.BOLD);
        msgView.setPadding(0, 0, 0, 0);
        msgView.setPaddingRelative(0, 0, 0, 0);
        ((ViewGroup.MarginLayoutParams) msgView.getLayoutParams()).setMargins(0, 0, 0, 0);
        snackBarView.setPadding(ToolsUtil.dp2px(6, view.getContext()), 0, 0, 0);
        return snackbar;
    }

    public static void err(Throwable e, Context context) {
        Log.e("BiliClient", e.getMessage(), e);
        if (e instanceof IOException) showMsg("网络错误(＃°Д°)", context);
        else if (e instanceof JSONException) {
            if (SharedPreferencesUtil.getBoolean("develop_error_detailed", false)) {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                showText(context, "数据解析错误", writer.toString());
            } else if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                showMsgLong("解析错误，可登陆后再次尝试", context);
            } else if (e.toString().replace("org.json.JSONException:", "")  .contains("-352"))
                showMsgLong("账号疑似被风控", context);
            else
                showMsgLong("数据解析错误：\n" + e.toString().replace("org.json.JSONException:", ""), context);
        } else showMsgLong("错误：" + e, context);
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
