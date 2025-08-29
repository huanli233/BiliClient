package com.RobinNotBad.BiliClient.activity.video.local;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.video.FolderAdapter;
import com.RobinNotBad.BiliClient.model.LocalFolder;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.FileUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FolderManagerActivity extends InstanceActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private final List<LocalFolder> folderList = new ArrayList<>();
    private File foldersConfigFile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_manager);
        setMenuClick();

        foldersConfigFile = FileUtil.getFoldersConfigFile();

        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refresh);

        findViewById(R.id.new_folder_button).setOnClickListener(v -> createNewFolderDialog());

        adapter = new FolderAdapter(this, folderList);
        adapter.setOnItemClickListener(position -> {
            LocalFolder folder = folderList.get(position);
            Intent intent = new Intent(FolderManagerActivity.this, FolderVideosActivity.class);
            intent.putExtra("folderName", folder.name);
            startActivity(intent);
        });
        adapter.setOnItemLongClickListener(this::showFolderActionMenu);
        recyclerView.setLayoutManager(getLayoutManager());
        recyclerView.setAdapter(adapter);

        refresh();
    }

    private void showFolderActionMenu(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_folder_action, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        MaterialButton btnChangeOrder = view.findViewById(R.id.btn_change_order);
        MaterialButton btnDelete = view.findViewById(R.id.btn_delete);

        btnChangeOrder.setOnClickListener(v -> {
            showChangeOrderDialog(position);
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            deleteFolder(position);
            dialog.dismiss();
        });

        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        dialog.getWindow().setAttributes(lp);
    }

    private void showChangeOrderDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_order, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText input = view.findViewById(R.id.edit_text_order);
        input.setText(String.valueOf(position + 1));

        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String orderStr = input.getText().toString();
            if (!orderStr.isEmpty()) {
                int newPosition = Integer.parseInt(orderStr) - 1;
                if (newPosition >= 0 && newPosition < folderList.size()) {
                    LocalFolder folder = folderList.remove(position);
                    folderList.add(newPosition, folder);
                    saveFolders();
                    adapter.notifyDataSetChanged();
                } else {
                    MsgUtil.showMsg("请输入有效的位置");
                }
                dialog.dismiss();
            } else {
                MsgUtil.showMsg("位置不能为空");
            }
        });

        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
        dialog.getWindow().setAttributes(lp);
    }

    private void deleteFolder(int position) {
        folderList.remove(position);
        saveFolders();
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, folderList.size());
        MsgUtil.showMsg("删除成功");
    }

    private void refresh() {
        CenterThreadPool.run(() -> {
            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
            loadFolders();
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }

    private void loadFolders() {
        folderList.clear();
        if (foldersConfigFile.exists()) {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(foldersConfigFile)) {
                Type listType = new TypeToken<ArrayList<LocalFolder>>() {}.getType();
                List<LocalFolder> loadedFolders = gson.fromJson(reader, listType);
                if (loadedFolders != null) {
                    folderList.addAll(loadedFolders);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createNewFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_new_folder, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText input = view.findViewById(R.id.edit_text_folder_name);
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String folderName = input.getText().toString().trim();
            if (!folderName.isEmpty()) {
                createNewFolder(folderName);
                dialog.dismiss();
            } else {
                MsgUtil.showMsg("文件夹名称不能为空");
            }
        });
        dialog.show();
    }

    private void createNewFolder(String folderName) {
        for (LocalFolder folder : folderList) {
            if (folder.name.equals(folderName)) {
                MsgUtil.showMsg("文件夹已存在");
                return;
            }
        }

        LocalFolder newFolder = new LocalFolder();
        newFolder.name = folderName;
        folderList.add(newFolder);

        saveFolders();
        refresh();
    }

    private void saveFolders() {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(foldersConfigFile)) {
            gson.toJson(folderList, writer);
        } catch (IOException e) {
            e.printStackTrace();
            MsgUtil.showMsg("操作失败");
        }
    }
}
