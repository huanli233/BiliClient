package com.RobinNotBad.BiliClient.service;

import static com.RobinNotBad.BiliClient.util.CenterThreadPool.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.Inflater;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadService extends Service {
    public float percent;
    public int index;
    public boolean success;
    public static boolean started;

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent serviceIntent, int flags, int startId) {
        if(serviceIntent == null) {
            stopSelf();
            return super.onStartCommand(serviceIntent,flags,startId);
        }
        started = true;

        CenterThreadPool.run(()->{
            try {
                JSONObject task = new JSONObject(serviceIntent.getStringExtra("task"));
                //TODO:想要做队列下载但暂时没想出好方案
                String type = task.getString("type");
                String url = task.getString("url");
                String name = ToolsUtil.stringToFile(task.getString("name"));

                switch (type) {
                    case "file":
                        File file = new File(FileUtil.getDownloadPicturePath(this), name);
                        if(!file.exists()) download(url, file);
                        else MsgUtil.showMsg("文件已存在！");
                        break;
                    case "video_single":
                        File path_single = new File(FileUtil.getDownloadPath(this), name);
                        if (!path_single.exists()) path_single.mkdirs();

                        String url_dm_single = task.getString("url_dm");
                        downdanmu(url_dm_single, new File(path_single, "danmaku.xml"));

                        String url_cover_single = task.getString("url_cover");
                        download(url_cover_single, new File(path_single, "cover.png"));

                        download(url, new File(path_single, "video.mp4"));
                        break;
                    case "video_multi":
                        String parent = task.getString("parent");
                        File path_parent = new File(FileUtil.getDownloadPath(this), parent);

                        String url_cover_multi = task.getString("url_cover");
                        File cover_multi = new File(path_parent, "cover.png");
                        if (!cover_multi.exists()) download(url_cover_multi, cover_multi);

                        File path_page = new File(path_parent, name);

                        String url_dm_page = task.getString("url_dm");
                        downdanmu(url_dm_page, new File(path_page, "danmaku.xml"));

                        download(url, new File(path_page, "video.mp4"));
                        break;
                }
                success = true;
                MsgUtil.showMsg("下载成功");
                stopSelf();
            } catch (Exception e){
                MsgUtil.err("下载失败，",e);
                stopSelf();
            }
        });

        return super.onStartCommand(serviceIntent, flags, startId);
    }

    @SuppressLint("SetTextI18n")
    private void download(String url, File file) {
        try {
            Response response = NetWorkUtil.get(url);
            if (!file.exists()) file.createNewFile();
            InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            int len;
            byte[] bytes = new byte[1024 * 10];
            long TotalFileSize = Objects.requireNonNull(response.body()).contentLength();
            while ((len = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, len);
                long CompleteFileSize = file.length();
                percent = 1.0f * CompleteFileSize / TotalFileSize;
            }
            inputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            runOnUiThread(() -> MsgUtil.showMsg("下载失败"));
            e.printStackTrace();
            stopSelf();
        }
    }


    private void downdanmu(String danmaku, File danmakuFile) {
        try {
            Response response = NetWorkUtil.get(danmaku);
            BufferedSink bufferedSink = null;
            try {
                if (!danmakuFile.exists()) danmakuFile.createNewFile();
                Sink sink = Okio.sink(danmakuFile);
                byte[] decompressBytes = decompress(Objects.requireNonNull(response.body()).bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
                bufferedSink = Okio.buffer(sink);
                bufferedSink.write(decompressBytes);//将解压后数据写入文件（sink）中
                bufferedSink.close();
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.showMsg("弹幕下载失败！"));
                e.printStackTrace();
                stopSelf();
            } finally {
                if (bufferedSink != null) {
                    bufferedSink.close();
                }
            }
        } catch (Exception e) {
            runOnUiThread(() -> MsgUtil.showMsg("弹幕下载失败！"));
            e.printStackTrace();
            stopSelf();
        }
    }


    @Override
    public void onDestroy() {
        if(!success){

        }
        super.onDestroy();
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
}