package com.RobinNotBad.BiliClient.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.RobinNotBad.BiliClient.activity.DialogActivity;
import com.RobinNotBad.BiliClient.activity.ShowTextActivity;

import org.json.JSONException;

import java.io.IOException;

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
        else if (e instanceof JSONException) toastLong("数据解析错误：\n" + e,context);
        else toast("错误：" + e , context);
    }

    public static void showText(Context context,String title,String text){
        Intent testIntent = new Intent()
                .setClass(context, ShowTextActivity.class)
                .putExtra("title",title)
                .putExtra("content",text);
        context.startActivity(testIntent);
    }
    public static void showDialog(Context context,String title,String content,int img_resource_id,boolean wait,int wait_time){
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title",title);
        intent.putExtra("content",content);
        intent.putExtra("img_id",img_resource_id);
        intent.putExtra("wait",wait);
        intent.putExtra("wait_time",wait_time);
        context.startActivity(intent);
    }
    public static void showDialog(Context context,String title,String content,String img_resource_url,boolean wait,int wait_time){
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra("title",title);
        intent.putExtra("content",content);
        intent.putExtra("img_id",img_resource_url);
        intent.putExtra("wait",wait);
        intent.putExtra("wait_time",wait_time);
        context.startActivity(intent);
    }
}
