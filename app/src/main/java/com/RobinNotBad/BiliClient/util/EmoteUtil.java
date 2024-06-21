package com.RobinNotBad.BiliClient.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import com.RobinNotBad.BiliClient.model.Emote;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


//表情包工具，用于将文本中的表情包替换为对应图片，部分代码来自catGPT
//2023-07-23

public class EmoteUtil {
    public static SpannableString textReplaceEmote(String text, JSONArray emote, float scale, Context context) throws JSONException, ExecutionException, InterruptedException {
        SpannableString result = new SpannableString(text);
        if(emote!=null && emote.length()>0) {
            for (int i = 0; i < emote.length(); i++) {    //遍历每一个表情包
                JSONObject key = emote.getJSONObject(i);

                String name = key.getString("name");
                String emoteUrl = key.getString("url");
                int size = key.getInt("size");  //B站十分贴心的帮你把表情包大小都写好了，快说谢谢蜀黍

                replaceSingle(text,result,name,emoteUrl,size,scale,context);
            }
        }
        return result;
    }

    public static SpannableString textReplaceEmote(String text, ArrayList<Emote> emotes, float scale, Context context, CharSequence source) throws ExecutionException, InterruptedException {
        SpannableString result = source == null ? new SpannableString(text) : new SpannableString(source);
        if(emotes!=null && emotes.size()>0) {
            for (int i = 0; i < emotes.size(); i++) {    //遍历每一个表情包
                Emote key = emotes.get(i);

                String name = key.name;
                String emoteUrl = key.url;
                int size = key.size;  //B站十分贴心的帮你把表情包大小都写好了，快说谢谢蜀黍

                replaceSingle(text,result,name,emoteUrl,size,scale,context);
            }
        }
        return result;
    }

    public static SpannableString textReplaceEmote(String text, ArrayList<Emote> emotes, float scale, Context context) throws JSONException, ExecutionException, InterruptedException {
        return textReplaceEmote(text, emotes, scale, context, null);
    }

    public static void replaceSingle(String origText, SpannableString spannableString, String name, String url, int size, float scale, Context context) throws ExecutionException, InterruptedException {
        Drawable drawable = Glide.with(context).asDrawable().load(url).submit().get();  //获得url并通过glide得到一张图片

        drawable.setBounds(0, 0, (int) (size * ToolsUtil.sp2px(18,context) * scale), (int) (size * ToolsUtil.sp2px(18,context) * scale));  //参考了隔壁腕上哔哩并进行了改进

        int start = origText.indexOf(name);    //检测此字符串的起始位置
        while (start>=0) {
            int end = start + name.length();    //计算得出结束位置
            ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);  //获得一个imagespan  这句不能放while上面，imagespan不可以复用，我也不知道为什么
            spannableString.setSpan(imageSpan,start,end,SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE);  //替换
            start = origText.indexOf(name,end);    //重新检测起始位置，直到找不到，然后开启下一个循环
        }
    }

}
