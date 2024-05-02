package com.RobinNotBad.BiliClient.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.RobinNotBad.BiliClient.activity.DialogActivity;
import com.RobinNotBad.BiliClient.activity.ShowTextActivity;

import org.json.JSONException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MsgUtil {
    private static Toast toast;

    public static void toast(String str, Context context){
        if(toast!=null)toast.cancel();
        if(!str.isEmpty()) {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    public static void toastLong(String str, Context context){
        if(toast!=null)toast.cancel();
        toast = Toast.makeText(context, str, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void err(Exception e,Context context){
        e.printStackTrace();
        if(e instanceof IOException) toast("网络错误(＃°Д°)",context);
        else if (e instanceof JSONException) {
            if(SharedPreferencesUtil.getBoolean("develop_error_detailed",false)) {
                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                e.printStackTrace(printWriter);
                showText(context,"数据解析错误",writer.toString());
            }
            else toastLong("数据解析错误：\n" + e.toString().replace("org.json.JSONException:",""),context);
        }
        else toastLong("错误：" + e , context);
    }

    public static void showText(Context context,String title,String text){
        Intent testIntent = new Intent()
                .setClass(context, ShowTextActivity.class)
                .putExtra("title",title)
                .putExtra("content",text);
        context.startActivity(testIntent);
    }

    public static void showTutorial(Context context,String title,String content,int img_resource_id){
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title",title);
        intent.putExtra("content",content);
        intent.putExtra("img_id",img_resource_id);
        intent.putExtra("wait",true);
        intent.putExtra("wait_time",2);
        context.startActivity(intent);
    }
    public static void showDialog(Context context, String title, String content, int img_resource_id){
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title",title);
        intent.putExtra("content",content);
        intent.putExtra("img_id",img_resource_id);
        context.startActivity(intent);
    }
    public static void showDialog(Context context, String title, String content, String img_resource_url){
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title",title);
        intent.putExtra("content",content);
        intent.putExtra("img_id",img_resource_url);
        context.startActivity(intent);
    }
}
