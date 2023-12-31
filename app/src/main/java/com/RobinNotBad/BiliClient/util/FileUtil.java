package com.RobinNotBad.BiliClient.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.Objects;

//用于清除缓存，因为glide实际上会往本地存不少缩略图，时间一长就会爆炸
//清除调用我放在了每次刷新推荐页

public class FileUtil {
    public static void clearCache(Context context) {
        File cacheDir = context.getCacheDir();
        if (cacheDir.exists() && Objects.requireNonNull(cacheDir.listFiles()).length != 0) deleteFolder(cacheDir);
        Log.e("debug","清除了缓存");
    }

    public static void deleteFolder(File folder) {
        File[] templist = folder.listFiles();
        assert templist != null;
        for (File file : templist) {
            if (file.isFile()) {   //如果该项是文件，直接删除
                file.delete();
            } else {    //如果该项是目录
                if (Objects.requireNonNull(file.listFiles()).length != 0) deleteFolder(file);    //如果子文件夹不是空的，继续扫描下去，实现套娃效果
            }
            Log.e("debug", file.toString());
        }
        folder.delete();
    }

}
