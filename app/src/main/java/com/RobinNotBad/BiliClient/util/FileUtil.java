package com.RobinNotBad.BiliClient.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.RobinNotBad.BiliClient.BiliTerminal;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Objects;

//用于清除缓存，因为glide实际上会往本地存不少缩略图，时间一长就会爆炸
//清除调用我放在了每次刷新推荐页

public class FileUtil {
    public static void clearCache(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.exists()) {
            if (deleteFolder(cacheDir)) {
                Log.d("FileUtil", "Cache cleared successfully");
            } else {
                Log.d("FileUtil", "Failed to clear cache");
            }
        }
    }

    public static boolean deleteFolder(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!deleteFolder(f)) {
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    public static String readString(File file) {
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
            return outputStream.toString();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean checkStoragePermission(){
        int sdk = Build.VERSION.SDK_INT;
        if(sdk < 17) return true;
        Context context = BiliTerminal.context;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestStoragePermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
    }

    public static File getVideoDownloadPath() {
        File path = new File(SharedPreferencesUtil.getString("save_path_video",
                Environment.getExternalStorageDirectory() + "/Android/media/" + BiliTerminal.context.getPackageName() + "/"));

        try {
            File nomedia = new File(path, ".nomedia");
            if (SharedPreferencesUtil.getBoolean("save_ban_gallery", true) && !nomedia.exists())
                nomedia.createNewFile();
            else if (nomedia.exists()) nomedia.delete();
        } catch (Exception ignored){}
        return path;
    }

    public static File getVideoDownloadPath(String title, String child){
        File parentFolder = new File(getVideoDownloadPath(), stringToFile(title));
        if(child==null || child.isEmpty()) return parentFolder;
        return new File(parentFolder, stringToFile(child));
    }

    public static File getFoldersConfigFile() {
        return new File(getVideoDownloadPath(), "folders.json");
    }

    public static File getPicturePath() {
        return new File(SharedPreferencesUtil.getString("save_path_pictures", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/哔哩终端/"));
    }

    public static File getDownloadPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static File getInternalFilesPath() {
        return BiliTerminal.context.getFilesDir();
    }

    public static void requireTFCardPermission(){

    }

    public static String stringToFile(String str) {
        return str.substring(0,Math.min(85,str.length()))    //防止长度溢出
                .replace("|", "｜")
                .replace(":", "：")
                .replace("*", "﹡")
                .replace("?", "？")
                .replace("\"", "”")
                .replace("<", "＜")
                .replace(">", "＞")
                .replace("/", "／")
                .replace("\\", "＼");    //文件名里不能包含非法字符
    }

    public static String getFileNameFromLink(String link) {
        int length = link.length();
        for (int i = length - 1; i > 0; i--) {
            if (link.charAt(i) == '/') {
                return link.substring(i + 1);
            }
        }
        return "fail";
    }

    public static String getFileFirstName(String file) {
        for (int i = 0; i < file.length(); i++) {
            if (file.charAt(i) == '.') {
                return file.substring(0, i);
            }
        }
        return "fail";
    }
}
