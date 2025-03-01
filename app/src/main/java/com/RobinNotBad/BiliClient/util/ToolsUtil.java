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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.BuildConfig;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.CopyTextActivity;
import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;
import com.RobinNotBad.BiliClient.model.At;
import com.RobinNotBad.BiliClient.model.UserInfo;

import org.jsoup.Jsoup;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//2023-07-25

@SuppressLint("ClickableViewAccessibility")
public class ToolsUtil {

    /**
     * 单位转换
     *
     * @param num 原始数字
     * @return 转换后的字符串
     */
    public static String toWan(long num) {
        if (num >= 100000000)
            return String.format(Locale.CHINA, "%.1f", (float) num / 100000000) + "亿";
        else if (num >= 10000)
            return String.format(Locale.CHINA, "%.1f", (float) num / 10000) + "万";
        else
            return String.valueOf(num);
    }

    public static String toTime(int progress) {
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

        if (cghour > 0) return cghourStr + ":" + cgminStr + ":" + cgsecStr;
        else return cgminStr + ":" + cgsecStr;
    }

    public static String htmlToString(String html) {
        return html.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&#39;", "'")
                .replace("&#34;", "\"")
                .replace("&#38;", "&")
                .replace("&#60;", "<")
                .replace("&#62;", ">");
    }

    public static String htmlReString(String html) {
        return html.replace("<p>", "")
                .replace("</p>", "\n")
                .replace("<br>", "\n")
                .replace("<em class=\"keyword\">", "")
                .replace("</em>", "");
    }

    public static String removeHtml(String html){
        return Jsoup.parse(html).text();
    }

    public static String unEscape(String str) {
        return str.replaceAll("\\\\(.)", "$1");
    }

    public static int dp2px(float dpValue) {
        final float scale = BiliTerminal.context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = BiliTerminal.context.getResources()
                .getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static void setCopy(TextView textView, String customText) {
        if (SharedPreferencesUtil.getBoolean("copy_enable", true)) {
            textView.setOnLongClickListener(view1 -> {
                Intent intent = new Intent(textView.getContext(), CopyTextActivity.class);
                intent.putExtra("content", customText == null ? textView.getText().toString() : customText);
                textView.getContext().startActivity(intent);
                return true;
            });
        }
    }

    public static void setCopy(TextView textView) {
        setCopy(textView, null); //直接传getText()会导致文本变化后点击不了
    }

    public static void setCopy(TextView... textViews) {
        for (TextView textView : textViews) setCopy(textView);
    }

    public static void copyText(Context context, String str) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", str);
        cm.setPrimaryClip(mClipData);
    }

    public static void setLink(TextView... textViews) {
        if (!SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.LINK_ENABLE, true)) return;
        for (TextView textView : textViews) {
            if (TextUtils.isEmpty(textView.getText())) continue;
            String text = textView.getText().toString();
            SpannableString spannableString = new SpannableString(textView.getText());

            Pattern urlPattern = Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]");
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

    public static String md5(String plainText) {
        byte[] secretBytes;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            secretBytes = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有md5这个算法！");
        }
        StringBuilder md5code = new StringBuilder(new BigInteger(1, secretBytes).toString(16));
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code.insert(0, "0");
        }
        return md5code.toString();
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
        public void onClick(@NonNull View widget) {
            switch (type) {
                case TYPE_USER:
                    widget.getContext().startActivity(new Intent(widget.getContext(), UserInfoActivity.class).putExtra("mid", Long.parseLong(val)));
                    break;
                case TYPE_WEB_URL:
                    LinkUrlUtil.handleWebURL(widget.getContext(), text);
                    break;
                case TYPE_BVID:
                    TerminalContext.getInstance().enterVideoDetailPage(widget.getContext(), text);
                    break;
                case TYPE_AVID:
                    TerminalContext.getInstance().enterVideoDetailPage(widget.getContext(), Long.parseLong(text.replace("av", "")));
                    break;
                case TYPE_CVID:
                    TerminalContext.getInstance().enterArticleDetailPage(widget.getContext(), Long.parseLong(text.replace("cv", "")));
                    break;
            }
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            ds.setColor(Color.rgb(0x66,0xcc,0xff));
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

    public static String getUpdateLog(Context context) {
        StringBuilder str = new StringBuilder();
        String[] logItems = context.getResources().getStringArray(R.array.update_log_items);
        for (int i = 0; i < logItems.length; i++)
            str.append((i + 1)).append(".").append(logItems[i]).append("\n");
        return str.toString();
    }

    public static boolean isDebugBuild() {
        return BuildConfig.BETA;
    }

    static final int[] levelBadges = {
            R.mipmap.level_0,
            R.mipmap.level_1,
            R.mipmap.level_2,
            R.mipmap.level_3,
            R.mipmap.level_4,
            R.mipmap.level_5,
            R.mipmap.level_6,
            R.mipmap.level_h
    };

    public static ImageSpan getLevelBadge(Context context, UserInfo userInfo) {
        int level = userInfo.level;

        if(level <= -1 || level >= 7) level = 0;
        if(userInfo.is_senior_member == 1) level = 7;

        Drawable drawable = ToolsUtil.getDrawable(context, levelBadges[level]);

        float lineHeight = ToolsUtil.getTextHeightWithSize(context);
        float lineWidth = lineHeight * 1.56f;
        if(userInfo.is_senior_member == 1) lineWidth = lineHeight * 1.96f;
        drawable.setBounds(0, 0, (int) lineWidth, (int) lineHeight);
        return new ImageSpan(drawable);
    }

    public static float getTextHeightWithSize(Context context) {
        Paint paint = new Paint();
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, context.getResources().getDisplayMetrics()));
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        return fontMetrics.descent - fontMetrics.ascent;
    }

    public static int getRgb888(int color){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append((color >> 16) & 0xff);
        stringBuilder.append((color >> 8) & 0xff);
        stringBuilder.append((color) & 0xff);
        Log.e("颜色", stringBuilder.toString());
        return Integer.parseInt(stringBuilder.toString());
    }

    public static Drawable getDrawable(Context context, @DrawableRes int res) {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), res, context.getTheme());
        if (drawable != null) return drawable;
        else return new BitmapDrawable();
    }
}
