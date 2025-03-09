package com.RobinNotBad.BiliClient.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Pair;

import com.RobinNotBad.BiliClient.activity.user.info.UserInfoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Response;
import okhttp3.ResponseBody;

public class LinkUrlUtil {
    public static final int TYPE_USER = -1;
    public static final int TYPE_WEB_URL = 0;
    public static final int TYPE_BVID = 1;
    public static final int TYPE_AVID = 2;
    public static final int TYPE_CVID = 3;
    public static final int TYPE_UID = 4;
    public static final Pattern BV_PATTERN = Pattern.compile("BV[A-Za-z0-9]{10}");
    public static final Pattern AV_PATTERN = Pattern.compile("av\\d{1,10}");
    public static final Pattern CV_PATTERN = Pattern.compile("cv\\d{1,10}");
    public static final Pattern UID_PATTERN = Pattern.compile("^(?i)uid\\d+$");

    public static void handleWebURL(Context context, String text) {
        try {
            text = (text.startsWith("http://") || text.startsWith("https://") ? text : "http://" + text);
            // 很傻逼的一系列解析
            URL url = new URL(text);
            String path = url.getPath();
            int index = path.indexOf('?');
            if (index != -1) {
                path = path.substring(0, index);
            }
            String domain = url.getHost();
            if (domain.equals("b23.tv")) {
                handleShortUrl(context, text);
                return;
            }
            if (domain.matches(".*\\.bilibili\\.com$")) {
                if (!path.isEmpty()) {
                    String lastPathItem = path.replaceAll(".*/([^/]+)/?$", "$1");
                    if (domain.equals("space.bilibili.com")) {
                        lastPathItem = "UID" + lastPathItem;
                    }
                    Pair<String, Integer> parse = parseId(lastPathItem);
                    if (parse != null) {
                        String val = parse.first;
                        int type = parse.second;
                        switch (type) {
                            case TYPE_BVID:
                                TerminalContext.getInstance().enterVideoDetailPage(context, val);
                                return;
                            case TYPE_AVID:
                                TerminalContext.getInstance().enterVideoDetailPage(context, Long.parseLong(val.replace("av", "")));
                                return;
                            case TYPE_CVID:
                                TerminalContext.getInstance().enterArticleDetailPage(context, Long.parseLong(val.replace("cv", "")));
                                return;
                            case TYPE_UID:
                                context.startActivity(new Intent(context, UserInfoActivity.class).putExtra("mid", Long.parseLong(val.replaceFirst("(?i)^uid", ""))));
                                return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            MsgUtil.err(e);
        }
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(text)));
        } catch (ActivityNotFoundException e) {
            MsgUtil.showMsg("没有可处理此链接的应用！");
        } catch (Throwable th) {
            MsgUtil.err(th);
        }
    }

    public static void handleId(Context context, String text) {
        Pair<String, Integer> parse = parseId(text);
        if (parse != null) {
            String val = parse.first;
            int type = parse.second;
            switch (type) {
                case TYPE_BVID:
                    TerminalContext.getInstance().enterVideoDetailPage(context, val);
                    break;
                case TYPE_AVID:
                    TerminalContext.getInstance().enterVideoDetailPage(context, Long.parseLong(val.replace("av", "")));
                    break;
                case TYPE_CVID:
                    TerminalContext.getInstance().enterArticleDetailPage(context, Long.parseLong(val.replace("cv", "")));
                    break;
                case TYPE_UID:
                    context.startActivity(new Intent(context, UserInfoActivity.class).putExtra("mid", Long.parseLong(val.replaceFirst("(?i)^uid", ""))));
                    break;
                case TYPE_WEB_URL:
                    handleWebURL(context, val);
                    break;
            }
        }
    }

    private static void handleShortUrl(Context context, String url) {
        CenterThreadPool.run(() -> {
            try {
                Response response = NetWorkUtil.get(url, NetWorkUtil.webHeaders, location -> handleWebURL(context, location));
                ResponseBody body;
                if (response.code() == 200 && (body = response.body()) != null) {
                    JSONObject json = new JSONObject(body.string());
                    if (json.has("code") && json.getInt("code") == -404) {
                        MsgUtil.showMsg("啥都木有~");
                    }
                }
            } catch (IOException | JSONException e) {
                MsgUtil.showMsg("解析失败！");
            }
        });
    }

    private static Pair<String, Integer> parseId(String item) {
        Matcher matcher;

        matcher = BV_PATTERN.matcher(item);
        if (matcher.find()) {
            return new Pair<>(matcher.group(), TYPE_BVID);
        }

        matcher = AV_PATTERN.matcher(item);
        if (matcher.find()) {
            return new Pair<>(matcher.group(), TYPE_AVID);
        }

        matcher = CV_PATTERN.matcher(item);
        if (matcher.find()) {
            return new Pair<>(matcher.group(), TYPE_CVID);
        }

        matcher = UID_PATTERN.matcher(item);
        if (matcher.find()) {
            return new Pair<>(matcher.group(), TYPE_UID);
        }

        matcher = Pattern.compile("(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]").matcher(item);
        if (matcher.find()) {
            return new Pair<>(matcher.group(), TYPE_WEB_URL);
        }

        return null;
    }
}
