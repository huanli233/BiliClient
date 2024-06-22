package com.RobinNotBad.BiliClient.util;

import static com.RobinNotBad.BiliClient.util.LinkUrlUtil.AV_PATTERN;
import static com.RobinNotBad.BiliClient.util.LinkUrlUtil.BV_PATTERN;
import static com.RobinNotBad.BiliClient.util.LinkUrlUtil.CV_PATTERN;
import static com.RobinNotBad.BiliClient.util.LinkUrlUtil.TYPE_AVID;
import static com.RobinNotBad.BiliClient.util.LinkUrlUtil.TYPE_BVID;
import static com.RobinNotBad.BiliClient.util.LinkUrlUtil.TYPE_CVID;
import static com.RobinNotBad.BiliClient.util.LinkUrlUtil.TYPE_USER;
import static com.RobinNotBad.BiliClient.util.LinkUrlUtil.TYPE_WEB_URL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.BuildConfig;
import com.RobinNotBad.BiliClient.activity.CopyTextActivity;
import com.RobinNotBad.BiliClient.activity.article.ArticleInfoActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.activity.video.info.VideoInfoActivity;
import com.RobinNotBad.BiliClient.model.At;

import com.RobinNotBad.BiliClient.R;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//2023-07-25

@SuppressLint("ClickableViewAccessibility")
public class ToolsUtil {
    public static String toWan(long num){
        if(num >= 100000000)
            return String.format(Locale.CHINA, "%.1f", (float)num/100000000) + "亿";
        else if(num >= 10000)
            return String.format(Locale.CHINA, "%.1f", (float)num/10000) + "万";
        else
            return String.valueOf(num);
    }

    public static String toTime(int progress){
        int cghour = progress / 3600;
        int cgminute = (progress % 3600) / 60;
        int cgsecond = progress % 60;
        String cghourStr;
        String cgminStr;
        String cgsecStr;

        if (cghour < 10) cghourStr = "0" + cghour;
        else cghourStr = String.valueOf(cghour);

        if (cgminute < 10) cgminStr = "0" + cgminute;
        else cgminStr = String.valueOf(cgminute);

        if (cgsecond < 10) cgsecStr = "0" + cgsecond;
        else cgsecStr = String.valueOf(cgsecond);

        if(cghour > 0) return cghourStr + ":" + cgminStr + ":" + cgsecStr;
        else return cgminStr + ":" + cgsecStr;
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

    public static void setCopy(TextView textView, Context context, String customText){
        if (SharedPreferencesUtil.getBoolean("copy_enable", true)) {
            textView.setOnLongClickListener(view1 -> {
                Intent intent = new Intent(context, CopyTextActivity.class);
                intent.putExtra("content", customText == null ? textView.getText().toString() : customText);
                context.startActivity(intent);
                return true;
            });
        }
    }

    public static void setCopy(TextView textView, Context context){
        setCopy(textView, context, null); //直接传getText()会导致文本变化后点击不了
    }

    public static void setCopy(Context context, TextView... textViews){
        for (TextView textView : textViews) setCopy(textView, context);
    }

    public static void setLink(TextView... textViews) {
        if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.LINK_ENABLE, true)) return;
        for (TextView textView : textViews) {
            if (TextUtils.isEmpty(textView.getText())) continue;
            String text = textView.getText().toString();
            SpannableString spannableString = new SpannableString(textView.getText());

            Pattern urlPattern = Patterns.WEB_URL;
            Matcher urlMatcher = urlPattern.matcher(text);
            while (urlMatcher.find()) {
                int start = urlMatcher.start();
                int end = urlMatcher.end();
                spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), TYPE_WEB_URL),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            Matcher matcher;

            matcher = BV_PATTERN.matcher(text);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), TYPE_BVID),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            matcher = AV_PATTERN.matcher(text);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), TYPE_AVID),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            matcher = CV_PATTERN.matcher(text);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), TYPE_CVID),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(spannableString);
            textView.setOnTouchListener(new ClickableSpanTouchListener());
        }
    }

    public static void setAtLink(Map<String, Long> atUserUids, TextView... textViews) {
        if (atUserUids == null || atUserUids.isEmpty()) return;
        for (TextView textView : textViews) {
            if (TextUtils.isEmpty(textView.getText())) continue;
            String text = textView.getText().toString();
            SpannableString spannableString = new SpannableString(textView.getText());

            for (Map.Entry<String, Long> entry : atUserUids.entrySet()) {
                String key = entry.getKey();
                long val = entry.getValue();

                Pattern pattern = Pattern.compile("@" + key);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    spannableString.setSpan(new LinkClickableSpan(text.substring(start, end), TYPE_USER, String.valueOf(val)),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            textView.setText(spannableString);
            textView.setOnTouchListener(new ClickableSpanTouchListener());
        }
    }

    public static void setAtLink(List<At> ats, TextView... textViews) {
        if (ats == null || ats.isEmpty()) return;
        for (TextView textView : textViews) {
            if (TextUtils.isEmpty(textView.getText())) continue;
            String text = textView.getText().toString();
            SpannableString spannableString = new SpannableString(textView.getText());

            for (At at : ats) {
                spannableString.setSpan(new LinkClickableSpan(text.substring(at.textStartIndex, at.textEndIndex), TYPE_USER, String.valueOf(at.rid)),
                        at.textStartIndex, at.textEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            textView.setText(spannableString);
            textView.setOnTouchListener(new ClickableSpanTouchListener());
        }
    }


    private static class LinkClickableSpan extends ClickableSpan {
        private final String text;
        /**
         * 真实值
         */
        private final String val;
        private final int type;

        public LinkClickableSpan(String text, int type, String val) {
            this.text = text;
            this.type = type;
            this.val = val;
        }

        public LinkClickableSpan(String text, int type) {
            this(text, type, null);
        }

        @Override
        public void onClick(View widget) {
            switch (type) {
                case TYPE_USER:
                    widget.getContext().startActivity(new Intent(widget.getContext(), UserInfoActivity.class).putExtra("mid", Long.parseLong(val)));
                    break;
                case TYPE_WEB_URL:
                    LinkUrlUtil.handleWebURL(widget.getContext(), text);
                    break;
                case TYPE_BVID:
                    widget.getContext().startActivity(new Intent(widget.getContext(), VideoInfoActivity.class).putExtra("bvid", text));
                    break;
                case TYPE_AVID:
                    widget.getContext().startActivity(new Intent(widget.getContext(), VideoInfoActivity.class).putExtra("aid", Long.parseLong(text.replace("av", ""))));
                    break;
                case TYPE_CVID:
                    widget.getContext().startActivity(new Intent(widget.getContext(), ArticleInfoActivity.class).putExtra("cvid", Long.parseLong(text.replace("cv", ""))));
                    break;
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            ds.setColor(Color.parseColor("#03a9f4"));
        }
    }

    // 查到的一种LinkMovementMethod问题的解决方法
    public static class ClickableSpanTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!(v instanceof TextView)) {
                return false;
            }
            TextView widget = (TextView) v;
            CharSequence text = widget.getText();
            if (!(text instanceof Spanned)) {
                return false;
            }
            Spanned buffer = (Spanned) text;
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);

                if (links.length != 0) {
                    ClickableSpan link = links[0];
                    if (action == MotionEvent.ACTION_UP) {
                        link.onClick(widget);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public static String getUpdateLog(Context context){
        String str = "";
        String[] logItems = context.getResources().getStringArray(R.array.update_log_items);
        for(int i = 0;i < logItems.length;i++) str += (i+1) + "." + logItems[i] + "\n";
        return str;
    }

    public static boolean isDebugBuild(){
        return BuildConfig.BETA;
    }
}
