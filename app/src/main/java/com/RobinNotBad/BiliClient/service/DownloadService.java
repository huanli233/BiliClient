package com.RobinNotBad.BiliClient.service;

import static com.RobinNotBad.BiliClient.util.SharedPreferencesUtil.downloadPrefs;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.IBinder;
import android.util.Log;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.activity.video.local.DownloadListActivity;
import com.RobinNotBad.BiliClient.activity.video.local.LocalListActivity;
import com.RobinNotBad.BiliClient.api.PlayerApi;
import com.RobinNotBad.BiliClient.model.DownloadSection;
import com.RobinNotBad.BiliClient.sql.DownloadSqlHelper;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Inflater;

import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadService extends Service {
    public static float percent;
    public static boolean started;
    public static DownloadSection downloadingSection;
    public static String str_section;
    public static short count_finish;

    private Timer toastTimer;
    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            if(downloadingSection == null)this.cancel();
            MsgUtil.showMsg("下载进度："+ String.format(Locale.CHINA,"%.2f",percent * 100) + "%\n" + downloadingSection.name_short);
        }
    };

    public DownloadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MutatingSharedPrefs")
    @Override
    public int onStartCommand(Intent serviceIntent, int flags, int startId) {
        if(started) {
            stopSelf();
            return super.onStartCommand(serviceIntent, flags, startId);
        }

        started = true;
        count_finish = 0;
        MsgUtil.showMsg("下载服务已启动");
        toastTimer = new Timer();
        //toastTimer.schedule(timerTask,5000,5000);

        CenterThreadPool.run(()->{
            while (true) {
                String[] array = getArray();
                if (array==null) {
                    MsgUtil.showMsg("下载列表为空");
                    break;
                }

                int i = 0;
                for (; i < array.length; i++) {
                    if(!array[i].contains("\"state\":\"error\"")) break;  //遍历列表，如果失败就跳过
                }
                if(i >= array.length) break;
                str_section = array[i];
                Log.d("debug-download",str_section);

                try {
                    downloadingSection = new DownloadSection(new JSONObject(str_section));

                    String url;
                    try{
                        url = PlayerApi.getVideo(downloadingSection.aid, downloadingSection.cid, downloadingSection.qn, true).first;
                    } catch (JSONException e){
                        MsgUtil.showMsg("下载链接获取失败");
                        setError(str_section,true);
                        continue;
                    }

                    try {
                        setDownloading(str_section,true);
                        refreshActivities();

                        MsgUtil.showMsg("开始下载：\n" + downloadingSection.name_short);

                        File file_sign = null;
                        switch (downloadingSection.type) {
                            case "video_single":  //单集视频
                                MsgUtil.showMsg("开始下载：\n" + downloadingSection.name_short);

                                File path_single = new File(FileUtil.getDownloadPath(this), downloadingSection.name);
                                if (!path_single.exists()) path_single.mkdirs();

                                file_sign = new File(path_single,".DOWNLOADING");
                                if(!file_sign.exists())file_sign.createNewFile();

                                String url_dm_single = downloadingSection.url_dm;
                                downdanmu(url_dm_single, new File(path_single, "danmaku.xml"));

                                String url_cover_single = downloadingSection.url_cover;
                                download(url_cover_single, new File(path_single, "cover.png"));

                                download(url, new File(path_single, "video.mp4"));

                                MsgUtil.showMsg("下载成功：\n" + downloadingSection.name_short);
                                break;
                            case "video_multi":  //多集视频
                                String parent = downloadingSection.parent;

                                MsgUtil.showMsg("开始下载：\n" + downloadingSection.name_short);

                                File path_parent = new File(FileUtil.getDownloadPath(this), parent);
                                if(!path_parent.exists()) path_parent.mkdirs();

                                String url_cover_multi = downloadingSection.url_cover;
                                File cover_multi = new File(path_parent, "cover.png");
                                if (!cover_multi.exists()) download(url_cover_multi, cover_multi);

                                File path_page = new File(path_parent, downloadingSection.name);
                                if(!path_page.exists()) path_page.mkdirs();

                                file_sign = new File(path_page,".DOWNLOADING");
                                if(!file_sign.exists())file_sign.createNewFile();

                                String url_dm_page = downloadingSection.url_dm;
                                downdanmu(url_dm_page, new File(path_page, "danmaku.xml"));

                                download(url, new File(path_page, "video.mp4"));

                                MsgUtil.showMsg("下载成功：\n" + downloadingSection.name_short);
                                count_finish++;
                                break;
                        }

                        if(file_sign!=null && file_sign.exists()) file_sign.delete();

                        deleteSection(str_section);
                    } catch (RuntimeException e){
                        MsgUtil.showMsg("下载失败："+e.getMessage());
                        e.printStackTrace();
                        setError(str_section,true);
                    }

                } catch (JSONException e){
                    MsgUtil.err("下载项格式错误：",e);
                    deleteSection(str_section);
                } catch (IOException e) {
                    MsgUtil.showMsg("下载失败，网络错误\n请手动前往缓存页面重新下载");
                    stopSelf();
                }

            }

            refreshActivities();
            if(count_finish != 0) MsgUtil.showMsg("全部下载完成");
            stopSelf();
        });

        return super.onStartCommand(serviceIntent, flags, startId);
    }

    private void refreshActivities(){
        InstanceActivity instance = BiliTerminal.getInstanceActivityOnTop();
        if (instance instanceof LocalListActivity && !instance.isDestroyed())
            ((LocalListActivity) (instance)).refresh();

        if(DownloadListActivity.weakRef != null) {
            DownloadListActivity.weakRef.get().refreshList();
        }
    }


    public static String[] getArray(){
        Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
        if(set.size() == 0) {
            return null;
        }

        String[] array = set.toArray(new String[0]);
        if (array.length == 0) {
            return null;
        }

        return array;
    }

    @SuppressLint({"MutatingSharedPrefs", "ApplySharedPref"})
    public static void deleteSection(String section){
        Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
        set.remove(section);
        downloadPrefs.edit().putStringSet("list",set).commit();
    }

    @SuppressLint({"MutatingSharedPrefs", "ApplySharedPref"})
    public static void setError(String section, boolean bool){
        try {
            Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
            JSONObject task = new JSONObject(section);
            task.put("state", bool ? "error" : "none");
            set.remove(section);
            set.add(task.toString());    //链接获取失败后标记为失败且不下载此项
            section = task.toString();    //原来传入的section也必须被更改
            downloadPrefs.edit().putStringSet("list", set).commit();
        }catch (JSONException e){
            MsgUtil.showMsg("下载项错误，该项已删除");
            deleteSection(section);
        }
    }

    @SuppressLint("MutatingSharedPrefs")
    public static void setDownloading(String section, boolean bool){
        try {
            Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
            JSONObject task = new JSONObject(section);
            task.put("state", bool ? "downloading" : "none");
            set.remove(section);
            set.add(task.toString());    //链接获取失败后标记为失败且不下载此项
            section = task.toString();
            downloadPrefs.edit().putStringSet("list", set).apply();
        }catch (JSONException e){
            MsgUtil.showMsg("下载项错误，该项已删除");
            deleteSection(section);
        }
    }

    private void download(String url, File file) throws IOException {
        Response response = NetWorkUtil.get(url);
        try {
            if (!file.exists()) file.createNewFile();
            else {
                file.delete();
                file.createNewFile();
            }
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件错误");
        }
    }

    private void downdanmu(String danmaku, File danmakuFile) throws IOException{
            Response response = NetWorkUtil.get(danmaku);
            BufferedSink bufferedSink = null;
            try {
                if (!danmakuFile.exists()) danmakuFile.createNewFile();
                else {
                    danmakuFile.delete();
                    danmakuFile.createNewFile();
                }
                Sink sink = Okio.sink(danmakuFile);
                byte[] decompressBytes = decompress(Objects.requireNonNull(response.body()).bytes());//调用解压函数进行解压，返回包含解压后数据的byte数组
                bufferedSink = Okio.buffer(sink);
                bufferedSink.write(decompressBytes);//将解压后数据写入文件（sink）中
                bufferedSink.close();
            } catch (IOException e) {
                throw new RuntimeException("文件错误");
            } finally {
                if (bufferedSink != null) {
                    bufferedSink.close();
                }
            }
    }



    @Override
    public void onDestroy() {
        if(downloadingSection!=null) {
            setDownloading(str_section, false);
        }
        started = false;
        if(toastTimer!=null) toastTimer.cancel();
        percent = 0;
        downloadingSection = null;
        str_section = null;
        count_finish = 0;
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



    //以下为外部调用方法
    @SuppressLint({"MutatingSharedPrefs", "ApplySharedPref"})
    public static void startDownload(String title, long aid, long cid, String danmaku, String cover, int qn){
        CenterThreadPool.run(()->{
            try {
                JSONObject task = new JSONObject();
                task.put("type", "video_single");
                task.put("aid", aid);
                task.put("cid", cid);
                task.put("qn", qn);
                task.put("name", title);
                task.put("parent", "");
                task.put("url_cover", cover);
                task.put("url_dm", danmaku);

                SharedPreferences downloadPrefs = BiliTerminal.context.getSharedPreferences("download", MODE_PRIVATE);
                Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
                set.add(task.toString());
                downloadPrefs.edit().putStringSet("list", set).commit();

                Log.d("download",set.toString());

                MsgUtil.showMsg("已添加下载");

                if(!started) BiliTerminal.context.startService(new Intent(BiliTerminal.context,DownloadService.class));
            } catch (Exception e){
                MsgUtil.err("启动下载时发生错误",e);
            }
        });
    }
    @SuppressLint({"MutatingSharedPrefs", "ApplySharedPref"})
    public static void startDownload(String parent, String child, long aid, long cid, String danmaku, String cover, int qn){
        CenterThreadPool.run(()-> {
            try {
                JSONObject task = new JSONObject();
                task.put("type", "video_multi");
                task.put("aid", aid);
                task.put("cid", cid);
                task.put("qn", qn);
                task.put("name", child);
                task.put("parent", parent);
                task.put("url_cover", cover);
                task.put("url_dm", danmaku);

                SharedPreferences downloadPrefs = BiliTerminal.context.getSharedPreferences("download", MODE_PRIVATE);
                Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
                set.add(task.toString());
                downloadPrefs.edit().putStringSet("list", set).commit();

                MsgUtil.showMsg("已添加下载");

                if(!started) BiliTerminal.context.startService(new Intent(BiliTerminal.context,DownloadService.class));
            } catch (Exception e) {
                MsgUtil.err("启动下载时发生错误", e);
            }
        });
    }



}