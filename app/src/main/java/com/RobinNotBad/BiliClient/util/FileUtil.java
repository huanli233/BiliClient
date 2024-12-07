package com.RobinNotBad.BiliClient.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.RobinNotBad.BiliClient.BiliTerminal;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

//用于清除缓存，因为glide实际上会往本地存不少缩略图，时间一长就会爆炸
//清除调用我放在了每次刷新推荐页

public class FileUtil {
    public static void clearCache(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir.exists() && Objects.requireNonNull(cacheDir.listFiles()).length != 0)
            deleteFolder(cacheDir);
        Log.e("debug", "清除了缓存");
    }

    public static void deleteFolder(File folder) {
        if(folder.isFile()) {
            folder.delete();
            return;
        }
        File[] templist = folder.listFiles();
        assert templist != null;
        for (File file : templist) {
            if (file.isFile()) {   //如果该项是文件，直接删除
                file.delete();
            } else {    //如果该项是目录
                if (Objects.requireNonNull(file.listFiles()).length != 0)
                    deleteFolder(file);    //如果子文件夹不是空的，继续扫描下去，实现套娃效果
            }
            Log.e("debug", file.toString());
        }
        folder.delete();
    }

    public static JSONObject readJson(File file) {
        if(file==null || !file.exists() || !file.canRead() || !file.isFile()) return null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            FileInputStream inputStream = new FileInputStream(file);
            FileChannel channel = inputStream.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1<<13);
            int i;
            while ((i = channel.read(buffer)) != -1){
                buffer.flip();
                outputStream.write(buffer.array(),0,i);
                buffer.clear();
            }
            return new JSONObject(outputStream.toString());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkStoragePermission(){
        int sdk = BiliTerminal.getSystemSdk();
        if(sdk < 17) return true;
        return ContextCompat.checkSelfPermission(BiliTerminal.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(BiliTerminal.context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestStoragePermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
    }

    public static File getDownloadPath(Context context) {
        return new File(SharedPreferencesUtil.getString("save_path_video", Environment.getExternalStorageDirectory() + "/Android/media/" + context.getPackageName() + "/"));
    }

    public static File getDownloadPicturePath(Context context) {
        return new File(SharedPreferencesUtil.getString("save_path_pictures", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/哔哩终端/"));
    }
}
