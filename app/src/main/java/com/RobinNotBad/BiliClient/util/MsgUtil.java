package com.RobinNotBad.BiliClient.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
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
        Logu.i(str);
        if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, true)) {
            CenterThreadPool.runOnUiThread(() -> EventBus.getDefault().postSticky(new SnackEvent(str)));
        } else {
            toast(str);
        }
    }

    public static void showMsgLong(String str) {
        Logu.i("long",str);
        if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.SNACKBAR_ENABLE, true)) {
            CenterThreadPool.runOnUiThread(() -> EventBus.getDefault().postSticky(new SnackEvent(str,Snackbar.LENGTH_LONG)));
        } else {
            toastLong(str);
        }
    }

    public static void toast(String str) {
        CenterThreadPool.runOnUiThread(() -> toastInternal(str, BiliTerminal.context));
    }

    public static void toastLong(String str) {
        CenterThreadPool.runOnUiThread(() -> toastLongInternal(str, BiliTerminal.context));
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
        msgView.setMaxLines(16);
        msgView.setTextSize(13);
        msgView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        msgView.setPadding(0, 0, ToolsUtil.dp2px(4), 0);

        return snackbar;
    }

    public static void err(Throwable e){
        err(null,e);
    }
    public static void err(String desc, Throwable e) {
        if(desc!=null) Log.e("debug-error",desc);
        e.printStackTrace();

        StringBuilder output = new StringBuilder(desc == null ? "" : desc);

        String e_str = e.toString();

        if (e instanceof IOException) output.append("网络错误(＃°Д°)");
        else if (e instanceof JSONException) {
            if (SharedPreferencesUtil.getBoolean("dev_jsonerr_detailed", false)) {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                showText(desc + "数据解析错误", writer.toString());
                return;
            } else if (SharedPreferencesUtil.getLong(SharedPreferencesUtil.mid, 0) == 0) {
                output.append("数据解析错误\n建议登陆后再尝试");
            } else if (e_str.contains("-352") || e_str.contains("22015") || e_str.contains("65056"))
                output.append(BiliTerminal.context.getString(R.string.err_rejected));
            else {
                output.append("数据解析错误：\n");
                output.append(e_str.replace("org.json.JSONException:", ""));
            }
        }
        else if(e instanceof IndexOutOfBoundsException) {
            if (SharedPreferencesUtil.getBoolean("dev_recyclererr_detailed", false)) {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                showText(desc + "Adapter错误", writer.toString());
                return;
            } else{
                output.append("遇到Adapter错误：\n无需上报，除非你在某个界面经常遇到");
            }
        }
        else if(e instanceof SQLException) output.append("数据库读写错误\n请清理空间或清除软件数据");
        else {
            output.append("错误：");
            output.append(e_str);
        }

        showMsgLong(output.toString());
    }

    public static void showText(String title, String text) {
        Context context = BiliTerminal.context;
        Intent testIntent = new Intent()
                .setClass(context, ShowTextActivity.class)
                .putExtra("title", title)
                .putExtra("content", text)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(testIntent);
    }

    public static void showDialog(String title, String content) {
        Context context = BiliTerminal.context;
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void showDialog(String title, String content, int wait_time) {
        Context context = BiliTerminal.context;
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("wait_time", wait_time);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
