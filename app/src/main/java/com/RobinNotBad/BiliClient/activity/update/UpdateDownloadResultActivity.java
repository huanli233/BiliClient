package com.RobinNotBad.BiliClient.activity.update;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.BuildConfig;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.video.MediaEpisodeAdapter;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.model.Bangumi;
import com.RobinNotBad.BiliClient.util.AsyncLayoutInflaterX;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateDownloadResultActivity extends BaseActivity {

    private static final List<String> installWays = List.of(
            "system"
    );

    private static final Map<String, String> wayIdToText = Map.of(
            "system", "调用系统安装器"
    );

    String path;
    TextView pathTv;
    Button installBtn;
    RecyclerView installWayList;
    MediaEpisodeAdapter adapter = new MediaEpisodeAdapter(true);

    int selectedInstallWay;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        path = getIntent().getStringExtra("path");
        if (path == null) {
            finish();
            return;
        }

        TutorialHelper.show(R.xml.tutorial_update_install, this, "update_install", 0);

        new AsyncLayoutInflaterX(this).inflate(R.layout.activity_update_download_result, null, ((view, resid, parent) -> {
            setContentView(view);
            setTopbarExit();

            pathTv = findViewById(R.id.path);
            installBtn = findViewById(R.id.install);
            installWayList = findViewById(R.id.install_way_list);

            pathTv.setText(String.format("下载的APK保存在: \n%s\n\n你可以另外使用自己的安装方法安装，或在下方选择一个安装方案进行安装", path));
            ToolsUtil.setCopy(pathTv, this, path);
            installWayList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            List<Bangumi.Episode> episodeList = new ArrayList<>();
            for (int i = 0; i < installWays.size(); i++) {
                Bangumi.Episode episode = new Bangumi.Episode();
                episode.id = i;
                episode.title = wayIdToText.get(installWays.get(i));
                if (episode.title == null)
                    throw new RuntimeException("Mapping between ID and text is error");
                episodeList.add(episode);
            }
            adapter.setData(episodeList);
            adapter.setOnItemClickListener((pos) -> this.selectedInstallWay = pos);
            installWayList.setAdapter(adapter);
            selectedInstallWay = 0;
            installBtn.setOnClickListener((view1 -> {
                String str = installWays.get(selectedInstallWay);
                switch (str) {
                    case "system":
                        installBySystemInstaller();
                        break;
                    default:
                        MsgUtil.toast("未知的安装方式", this);
                }
            }));
        }));
    }

    private boolean checkRequestInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return false;
            }
        }
        return true;
    }

    private void installBySystemInstaller() {
        try {
            if (!checkRequestInstallPermission()) {
                MsgUtil.toast("没有授予请求安装应用权限", this);
                return;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(UpdateDownloadResultActivity.this, BuildConfig.APPLICATION_ID + ".FileProvider", new File(path));
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                startActivity(intent);
            } else {
                if (getPackageManager().canRequestPackageInstalls()) {
                    startActivity(intent);
                } else {
                    Toast.makeText(UpdateDownloadResultActivity.this, "没有授予请求安装应用权限", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Throwable th) {
            Log.e("BiliClient", th.toString());
            MsgUtil.toast("未知错误: " + th.getMessage(), this);
        }
    }
}