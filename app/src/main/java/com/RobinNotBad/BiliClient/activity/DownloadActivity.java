package com.RobinNotBad.BiliClient.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadActivity extends BaseActivity {

    View progressView;
    TextView progressText;

    File rootPath, downPath, downFile;
    String link;
    int scrHeight;

    String dldText = "";
    float dldPercent = 0;

    int type;

    boolean finish = false;

    final Timer timer = new Timer();
    final TimerTask showText = new TimerTask() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            int viewHeight = (int) (dldPercent * scrHeight);
            runOnUiThread(() -> {
                progressText.setText(dldText + "\n" + (dldPercent * 100) + "%");
                ViewGroup.LayoutParams params = progressView.getLayoutParams();
                params.height = viewHeight;
                progressView.setLayoutParams(params);
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Intent intent = getIntent();

        type = intent.getIntExtra("type",0);  //0=单个文件，1=视频，2=分页视频
        String title = ToolsUtil.stringToFile(intent.getStringExtra("title"));
        link = intent.getStringExtra("link");


        progressText = findViewById(R.id.progressText);
        progressView = findViewById(R.id.progressView);

        scrHeight = window_height;

        timer.schedule(showText,100,100);
        CenterThreadPool.run(()->{
            if(type == 0){
                rootPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"BiliClient");
                if(!rootPath.exists()) rootPath.mkdirs();
                downFile = new File(rootPath,title);
                download(link,downFile,"下载文件中",true);
            }
            else{
                rootPath = ConfInfoApi.getDownloadPath(this);

                if(type==1){
                    downPath = new File(rootPath, title);
                    rootPath = downPath;
                }
                if(type==2) {
                    rootPath = new File(rootPath, ToolsUtil.stringToFile(intent.getStringExtra("parent_title")));
                    downPath = new File(rootPath, title);
                }

                if(!downPath.exists()) downPath.mkdirs();

                String danmaku = intent.getStringExtra("danmaku");
                String cover = intent.getStringExtra("cover");
                File dmFile = new File(downPath,"danmaku.xml");
                File coverFile = new File(rootPath,"cover.png");
                File videoFile = new File(downPath,"video.mp4");
                downdanmu(danmaku,dmFile);
                if(!coverFile.exists()) download(cover,coverFile,"下载封面",false);
                download(link,videoFile,"下载视频",true);
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void download(String url, File file, String desc, boolean exitOnFinish) {
        dldText = desc;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url)
                .addHeader("Cookie", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""))
                .addHeader("Connection", "Keep-Alive")
                .addHeader("User-Agent", NetWorkUtil.USER_AGENT_WEB)
                .addHeader("Referer", "https://www.bilibili.com/")
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if(!file.exists()) file.createNewFile();
            InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int len;
            byte[] bytes = new byte[1024 * 10];
            long TotalFileSize = Objects.requireNonNull(response.body()).contentLength();
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
                long CompleteFileSize = file.length();
                dldPercent = 1.0f * CompleteFileSize / TotalFileSize;
            }
            inputStream.close();
            fileOutputStream.close();
            if (exitOnFinish) {
                runOnUiThread(() -> MsgUtil.toast("下载完成",this));
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish = true;
                        finish();
                    }
                },200);
            }
        } catch (IOException e) {
            runOnUiThread(() -> MsgUtil.toast("下载失败",this));
            e.printStackTrace();
            finish();
        }
    }


    private void downdanmu(String danmaku, File danmakuFile) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(danmaku)
                .addHeader("Cookie", SharedPreferencesUtil.getString(SharedPreferencesUtil.cookies,""))
                .addHeader("Connection", "Keep-Alive")
                .addHeader("User-Agent", NetWorkUtil.USER_AGENT_WEB)
                .addHeader("Referer", "https://www.bilibili.com/")
                .build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            BufferedSink bufferedSink = null;
            try {
                if (!danmakuFile.exists()) danmakuFile.createNewFile();
                Sink sink = Okio.sink(danmakuFile);
                byte[] decompressBytes = decompress(Objects.requireNonNull(response.body()).bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
                bufferedSink = Okio.buffer(sink);
                bufferedSink.write(decompressBytes);//将解压后数据写入文件（sink）中
                bufferedSink.close();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            } finally {
                if (bufferedSink != null) {
                    bufferedSink.close();
                }
            }
        }catch (IOException e){
            runOnUiThread(() -> MsgUtil.toast("弹幕下载失败！",this));
            finish();
            e.printStackTrace();
        }
    }


    public static byte[] decompress(byte[] data) {
        byte[] output;
        Inflater decompresser = new Inflater(true);//这个true是关键
        decompresser.reset();
        decompresser.setInput(data);
        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[2048];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        decompresser.end();
        return output;
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        if(!finish) {
            if (type != 0 && downPath != null) FileUtil.deleteFolder(downPath);
            else if (downFile != null) downFile.delete();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
