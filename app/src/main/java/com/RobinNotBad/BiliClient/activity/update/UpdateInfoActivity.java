package com.RobinNotBad.BiliClient.activity.update;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.AppInfoApi;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UpdateInfoActivity extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;

    TextView versionNameTv;
    TextView versionCodeTv;
    TextView isReleaseTv;
    TextView pubTimeTv;
    TextView updateLogTv;
    MaterialButton downloadBtn;

    boolean stopDownload;
    long lastClickTime;
    boolean downloading;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        Intent intent = getIntent();
        String versionName = intent.getStringExtra("versionName");
        int versionCode = intent.getIntExtra("versionCode", -1);
        int isRelease = intent.getIntExtra("isRelease", 0);
        long ctime = intent.getLongExtra("ctime", -1);
        String updateLog = intent.getStringExtra("updateLog");
        int canDownload = intent.getIntExtra("canDownload", 0);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_update_info, null, ((view, resid, parent) -> {
            setContentView(view);
            setTopbarExit();
            versionNameTv = findViewById(R.id.versionName);
            versionCodeTv = findViewById(R.id.versionCode);
            isReleaseTv = findViewById(R.id.isRelease);
            pubTimeTv = findViewById(R.id.pubTime);
            updateLogTv = findViewById(R.id.updateLog);
            downloadBtn = findViewById(R.id.download);

            if (versionName == null || versionCode == -1) {
                finish();
                return;
            }

            versionNameTv.setText(String.format("版本名: %s", versionName));
            versionCodeTv.setText(String.format(Locale.getDefault(), "版本号: %d", versionCode));
            isReleaseTv.setText(String.format("是否为正式版: %s", isRelease == 1 ? "是" : "否"));
            pubTimeTv.setText(String.format("发布时间: %s", ctime == -1 ? "未知" : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(String.valueOf(ctime).length() == 13 ? ctime : ctime * 1000))));
            updateLogTv.setText(TextUtils.isEmpty(updateLog) ? "暂无更新日志" : updateLog);

            if (canDownload != 1) downloadBtn.setVisibility(View.GONE);
            downloadBtn.setOnClickListener((view1) -> {
                long time = System.currentTimeMillis();
                if (downloading) {
                    if (time >= lastClickTime) {
                        if (time - lastClickTime <= 3000) {
                            stopDownload = true;
                        } else {
                            MsgUtil.showMsg("再按一次中止下载", this);
                        }
                    }
                    lastClickTime = time;
                    return;
                }
                if (!checkAndRequestPermissions()) {
                    MsgUtil.showMsg("没有写入存储空间权限", this);
                    return;
                }
                CenterThreadPool.run(() -> {
                    try {
                        runOnUiThread(() -> MsgUtil.showMsg("开始获取下载地址", this));
                        String url = AppInfoApi.getDownloadUrl(versionCode);
                        if (TextUtils.isEmpty(url)) {
                            runOnUiThread(() -> MsgUtil.showMsg("没有该版本的下载地址", this));
                        } else {
                            Request request = new Request.Builder()
                                    .get()
                                    .url(url)
                                    .build();

                            NetWorkUtil.getOkHttpInstance().newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    runOnUiThread(() -> {
                                        MsgUtil.showMsg("下载失败: " + e.getMessage(), UpdateInfoActivity.this);
                                        downloadBtn.setText("下载");
                                    });
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    if (!response.isSuccessful()) {
                                        runOnUiThread(() -> MsgUtil.showMsg("下载失败！", UpdateInfoActivity.this));
                                    } else {
                                        try {
                                            downloading = true;
                                            stopDownload = false;
                                            ResponseBody responseBody = response.body();
                                            if (responseBody != null) {
                                                long fileSize = responseBody.contentLength();
                                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BiliTerminal_" + versionCode + ".apk");
                                                if (file.exists() && !file.delete()) {
                                                    int num = 1;
                                                    while (file.exists()) {
                                                        if (file.delete()) break;
                                                        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BiliTerminal_" + versionCode + "(" + num++ + ").apk");
                                                    }
                                                }
                                                FileOutputStream fos = new FileOutputStream(file);
                                                int bytesRead;
                                                byte[] data = new byte[1024];
                                                long totalBytesRead = 0;
                                                while ((bytesRead = responseBody.byteStream().read(data)) != -1) {
                                                    if (stopDownload) break;
                                                    fos.write(data, 0, bytesRead);
                                                    totalBytesRead += bytesRead;
                                                    int progress = (int) ((totalBytesRead * 100) / fileSize);
                                                    downloadBtn.setText(String.format(Locale.getDefault(), "下载(%d%%)", progress));
                                                }
                                                fos.flush();
                                                fos.close();
                                                if (!stopDownload) {
                                                    startActivity(new Intent(UpdateInfoActivity.this, UpdateDownloadResultActivity.class)
                                                            .putExtra("path", file.getAbsolutePath()));
                                                } else {
                                                    if (file.delete()) {
                                                        runOnUiThread(() -> MsgUtil.showMsg("已中止下载并删除已下载的文件", UpdateInfoActivity.this));
                                                    } else {
                                                        runOnUiThread(() -> MsgUtil.showMsg("已中止下载但删除已下载的文件失败", UpdateInfoActivity.this));
                                                    }
                                                }
                                                stopDownload = false;
                                                downloading = false;
                                            }
                                        } catch (Throwable th) {
                                            Log.e("BiliClient", th.toString());
                                            runOnUiThread(() -> MsgUtil.showMsg("下载时发生了错误", UpdateInfoActivity.this));
                                            downloading = false;
                                        }
                                    }
                                    downloadBtn.setText("下载");
                                }
                            });
                        }
                    } catch (IOException e) {
                        runOnUiThread(() -> MsgUtil.showMsg("网络异常", this));
                    } catch (Throwable th) {
                        runOnUiThread(() -> MsgUtil.showMsg("错误: " + th.getMessage(), this));
                    }
                });
            });
        }));
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return false;
            }
        } else {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                MsgUtil.showMsg("拒绝了存储权限", this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopDownload = true;
        super.onDestroy();
    }
}
