package com.RobinNotBad.BiliClient.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.TutorialActivity;
import com.RobinNotBad.BiliClient.model.CustomText;
import com.RobinNotBad.BiliClient.model.Tutorial;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

//这个Helper是给教程使用的
//因为不通用(只适用教程)就放进Helper了

public class TutorialHelper {


    /**
     * 从xml加载教程对象
     *
     * @param xml 教程对应的XML的ID
     * @return 教程对象
     */
    public static Tutorial loadTutorial(XmlResourceParser xml) {
        try {
            xml.next();
            int eventType = xml.getEventType();

            boolean isInName = false;
            boolean isInDescrption = false;
            boolean isInType = false;
            boolean isInImg = false;
            boolean isInContentItem = false;
            boolean isInContentItemType = false;
            boolean isInContentItemText = false;
            boolean isInContentItemStyle = false;
            boolean isInContentItemColor = false;

            Tutorial turtorial = new Tutorial();
            List<CustomText> content = new ArrayList<>();
            CustomText item = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                //听我说，写的可能有点屎，但能用就行
                if (eventType == XmlPullParser.START_TAG) {
                    switch (xml.getName()) {
                        case "name":
                            isInName = true;
                            break;
                        case "description":
                            isInDescrption = true;
                            break;
                        case "img":
                            isInImg = true;
                            break;
                        case "type":
                            if (isInContentItem) isInContentItemType = true;
                            else isInType = true;
                            break;
                        case "item":
                            isInContentItem = true;
                            item = new CustomText();
                            break;
                        case "text":
                            isInContentItemText = true;
                            break;
                        case "style":
                            isInContentItemStyle = true;
                            break;
                        case "color":
                            isInContentItemColor = true;
                            break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    switch (xml.getName()) {
                        case "name":
                            isInName = false;
                            break;
                        case "description":
                            isInDescrption = false;
                            break;
                        case "img":
                            isInImg = false;
                            break;
                        case "type":
                            if (isInContentItem) isInContentItemType = false;
                            else isInType = false;
                            break;
                        case "item":
                            isInContentItem = false;
                            content.add(item);
                            item = null;
                            break;
                        case "text":
                            isInContentItemText = false;
                            break;
                        case "style":
                            isInContentItemStyle = false;
                            break;
                        case "color":
                            isInContentItemColor = false;
                            break;
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (isInName) turtorial.name = xml.getText();
                    else if (isInDescrption) turtorial.description = xml.getText();
                    else if (isInImg) turtorial.imgid = xml.getText();
                    else if (isInType) turtorial.type = Integer.parseInt(xml.getText());
                    else if (item != null) {
                        if (isInContentItemType) item.type = Integer.parseInt(xml.getText());
                        else if (isInContentItemText) item.text = xml.getText();
                        else if (isInContentItemStyle) item.style = xml.getText();
                        else if (isInContentItemColor) item.color = xml.getText();
                    }
                }

                xml.next();
                eventType = xml.getEventType();
            }
            turtorial.content = content;
            return turtorial;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用于解析教程中的文本内容
     *
     * @param texts 文本对象列表，从xml解析得来
     * @return 解析出来的文本内容
     */
    public static SpannableStringBuilder loadText(List<CustomText> texts) {
        SpannableStringBuilder str = new SpannableStringBuilder("");
        for (CustomText text : texts) {
            if (text.type == 0) {
                int old_len = str.length();
                str.append(text.text);
                str.setSpan(new ForegroundColorSpan(Color.parseColor(text.color)), old_len, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                switch (text.style) {
                    case "bold": //粗体
                        str.setSpan(new StyleSpan(Typeface.BOLD), old_len, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    case "italic": //斜体
                        str.setSpan(new StyleSpan(Typeface.ITALIC), old_len, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    case "underline": //下划线
                        str.setSpan(new UnderlineSpan(), old_len, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    case "strike": //删除线
                        str.setSpan(new StrikethroughSpan(), old_len, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                }
            } else if (text.type == 1) str.append("\n");
        }
        return str;
    }


    /**
     * 加载对应的教程，并且检查是否浏览过该教程
     *
     * @param xml_res_id       教程XML的ID
     * @param context
     * @param tutorial_tag     教程的Key，是保存浏览进度时使用的键值
     * @param tutorial_version 教程版本，用于保存浏览进度（大于0）
     */
    public static void show(int xml_res_id, Context context, String tutorial_tag, int tutorial_version) {
        if (SharedPreferencesUtil.getInt("tutorial_ver_" + tutorial_tag, -1) < tutorial_version) {
            if (SharedPreferencesUtil.getInt("tutorial_ver_" + tutorial_tag, 0) != 0)
                Toast.makeText(context.getApplicationContext(), "教程已更新", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, TutorialActivity.class);
            intent.putExtra("xml_id", xml_res_id);
            intent.putExtra("tag", tutorial_tag);
            intent.putExtra("version", tutorial_version);
            context.startActivity(intent);
        }
    }


    /**
     * 用于直接通过Array加载对应页面的所有教程，避免新功能的教程与之前的写在同一个教程内时用户不看的情况
     *
     * @param context
     * @param array_id     教程Array的ID
     * @param tutorial_key 教程的Key值，是保存浏览进度时使用的键值，在string.xml中存储
     */
    public static void showTutorialList(Context context, int array_id, int tutorial_key) {
        try {
            int n = context.getResources().getStringArray(array_id).length; //用于修复version错误
            for (int i = 1; i <= context.getResources().getStringArray(array_id).length; i++) {
                int indentify = context.getResources().getIdentifier(context.getPackageName() + ":" + context.getResources().getStringArray(array_id)[i - 1], null, null);
                if (indentify > 0)
                    show(indentify, context, context.getResources().getStringArray(R.array.tutorial_list)[tutorial_key], n--);
            }
        } catch (Exception e) {
            MsgUtil.showMsg("加载教程时遇到问题", context);
            e.printStackTrace();
        }
    }

    public static void showPagerTutorial(Activity activity, int pagecount){
        activity.runOnUiThread(()->{
            String pagename = activity.getClass().getSimpleName();
            TextView textView = activity.findViewById(R.id.text_tutorial_pager);
            if(SharedPreferencesUtil.getBoolean("tutorial_pager_"+ pagename, true)) {
                Log.d("debug-tutorial", pagename);
                textView.setVisibility(View.VISIBLE);
                textView.setText(activity.getString(R.string.tutorial_pager).replace("NNN", String.valueOf(pagecount)));

                View viewPager = activity.findViewById(R.id.viewPager);
                if (viewPager instanceof ViewPager) {
                    ((ViewPager) viewPager).addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            if (position != 0) textView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onPageSelected(int position) {
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });
                }
                SharedPreferencesUtil.putBoolean("tutorial_pager_" + pagename, false);
            }
            else{
                textView.setVisibility(View.GONE);
            }
        });
    }
}
