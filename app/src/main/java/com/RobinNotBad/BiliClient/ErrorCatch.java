package com.RobinNotBad.BiliClient;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ErrorCatch implements Thread.UncaughtExceptionHandler{
    @SuppressLint("StaticFieldLeak")
    public static ErrorCatch instance;
    private Context context;

    public static ErrorCatch getInstance(){
        if(instance==null) instance = new ErrorCatch();
        return instance;
    }

    public void init(Context context){
        this.context = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);

        MsgUtil.showText(context,"错误报告",writer.toString());
        
        throwable.printStackTrace();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
