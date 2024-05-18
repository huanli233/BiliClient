package com.RobinNotBad.BiliClient.util;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.activity.CopyTextActivity;

import java.util.Locale;

//2023-07-25

public class ToolsUtil {
    public static String toWan(long num){
        if(num>=10000){
            return String.format(Locale.CHINA, "%.1f", (float)num/10000) + "万";
        }
        else return String.valueOf(num);
    }

    public static String htmlToString(String html){
        return html.replace("&lt;","<")
                .replace("&gt;",">")
                .replace("&quot;","\"")
                .replace("&amp;","&")
                .replace("&#39;", "'")
                .replace("&#34;", "\"")
                .replace("&#38;", "&")
                .replace("&#60;", "<")
                .replace("&#62;", ">");
    }

    public static String htmlReString(String html){
        return html.replace("<p>","")
                .replace("</p>","\n")
                .replace("<br>","\n")
                .replace("<em class=\"keyword\">","")
                .replace("</em>","");
    }

    public static String stringToFile(String str){
        return str.replace("|", "｜")
                .replace(":", "：")
                .replace("*", "﹡")
                .replace("?", "？")
                .replace("\"", "”")
                .replace("<", "＜")
                .replace(">", "＞")
                .replace("/", "／")
                .replace("\\", "＼");    //文件名里不能包含非法字符
    }

    public static String unEscape(String str){
        return str.replaceAll("\\\\(.)","$1");
    }

    public static int dp2px(float dpValue, Context context)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(float spValue,Context context)
    {
        final float fontScale = context.getResources()
                .getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static String getFileNameFromLink(String link){
        int length = link.length();
        for (int i = length - 1; i > 0; i--) {
            if(link.charAt(i)=='/'){
                return link.substring(i+1);
            }
        }
        return "fail";
    }

    public static String getFileFirstName(String file){
        for (int i = 0; i < file.length(); i++) {
            if(file.charAt(i)=='.'){
                return file.substring(0,i);
            }
        }
        return "fail";
    }

    public static void setCopy(TextView textView,Context context){
        if (SharedPreferencesUtil.getBoolean("copy_enable", true)) {
            textView.setOnLongClickListener(view1 -> {
                Intent intent = new Intent(context, CopyTextActivity.class);
                intent.putExtra("content", textView.getText().toString());
                context.startActivity(intent);
                return true;
            });
        }
    }
    public static void setCopy(TextView textView,Context context,String customText){
        if (SharedPreferencesUtil.getBoolean("copy_enable", true)) {
            textView.setOnLongClickListener(view1 -> {
                Intent intent = new Intent(context, CopyTextActivity.class);
                intent.putExtra("content", customText);
                context.startActivity(intent);
                return true;
            });
        }
    }
}
