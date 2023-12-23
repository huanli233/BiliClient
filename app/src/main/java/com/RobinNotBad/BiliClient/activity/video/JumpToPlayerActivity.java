package com.RobinNotBad.BiliClient.activity.video;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.DownloadActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Response;

public class JumpToPlayerActivity extends BaseActivity {
    private String videourl;
    private String danmakuurl;
    private String title;
    private TextView textView;

    int download;

    boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_jump);

        textView = findViewById(R.id.title);

        Intent intent = getIntent();
        Log.e("debug-哔哩终端-跳转页","已接收数据");
        String bvid = intent.getStringExtra("bvid");
        long aid = intent.getLongExtra("aid", 0);
        int cid = intent.getIntExtra("cid",0);
        title = intent.getStringExtra("title");
        download = intent.getIntExtra("download",0);

        Log.e("debug-哔哩终端-跳转页", "cid=" + cid);
        danmakuurl = "https://comment.bilibili.com/" + cid + ".xml";
        if (aid == 0) {
            Log.e("debug-哔哩终端-跳转页", "bid");
            requestVideo(0, bvid, cid);
        } else {
            Log.e("debug-哔哩终端-跳转页", "aid");
            requestVideo(aid, null, cid);
        }
    }

    private void requestVideo(long aid,String bvid, int cid) {
        new Thread(()->{
//            String url;
            /*if(SharedPreferencesUtil.getBoolean("high_res",false)){
                if (aid == 0) {
                    url = "https://api.bilibili.com/x/player/playurl?bvid=" + bvid + "&cid=" + cid + "&qn=80&type=mp4";
                } else {
                    url = "https://api.bilibili.com/x/player/playurl?avid=" + aid + "&cid=" + cid + "&qn=80&type=mp4";
                }
            }
            else {
                if (aid == 0) {
                    url = "https://api.bilibili.com/x/player/playurl?platform=html5&bvid=" + bvid + "&cid=" + cid + "&qn=16&type=mp4";
                } else {
                    url = "https://api.bilibili.com/x/player/playurl?platform=html5&avid=" + aid + "&cid=" + cid + "&qn=16&type=mp4";
                }
            }*/ //原来的方法看起来太多if else 闲的没事的ic改了改

            String url = "https://api.bilibili.com/x/player/playurl?"+ (aid == 0 ? ("bvid=" + bvid): ("avid=" + aid)) + "&cid=" + cid + "&type=mp4" + (SharedPreferencesUtil.getBoolean("high_res",false) ? "&qn=80" : "&qn=16");
            //顺便把platform html5给删了,实测删除后放和番剧相关的东西不会404了 (不到为啥)
            Log.e("debug-哔哩终端-跳转页","请求链接：" + url);

            try {
                Response response = NetWorkUtil.get(url, ConfInfoApi.defHeaders);

                String body = Objects.requireNonNull(response.body()).string();
                Log.e("debug-body", body);
                JSONObject body1 = new JSONObject(body);
                JSONObject data = new JSONObject(body1.get("data").toString());
                JSONArray durl = data.getJSONArray("durl");
                JSONObject video_url = durl.getJSONObject(0);
                videourl = video_url.getString("url");
                Log.e("debug-哔哩终端-跳转页", "得到链接：" + videourl);

                Log.e("debug-哔哩终端-传出cookie",SharedPreferencesUtil.getString("cookies", ""));

                if(!destroyed) {
                    if (download != 0) {
                        Intent intent = new Intent();
                        intent.setClass(this, DownloadActivity.class);
                        intent.putExtra("type", download);
                        intent.putExtra("link", videourl);
                        intent.putExtra("danmaku", danmakuurl);
                        intent.putExtra("title", title);
                        intent.putExtra("cover", getIntent().getStringExtra("cover"));
                        if (download == 2)
                            intent.putExtra("parent_title", getIntent().getStringExtra("parent_title"));
                        startActivity(intent);
                    } else {
                        PlayerApi.jumpToPlayer(JumpToPlayerActivity.this, videourl, danmakuurl, title, false);
                    }
                    finish();
                }
            } catch (IOException e) {
                runOnUiThread(()->textView.setText("网络错误！\n请检查你的网络连接是否正常"));
                e.printStackTrace();
            } catch (JSONException e) {
                runOnUiThread(()->textView.setText("解析视频地址失败！\n建议联系开发者\n（但开发者大概率已经知道了）"));
                e.printStackTrace();
            } catch (ActivityNotFoundException e){
                runOnUiThread(()->textView.setText("跳转失败！\n请安装对应的播放器\n或将哔哩终端和播放器同时更新到最新版本"));
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }
}