package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.settings.login.SpecialLoginActivity;
import com.RobinNotBad.BiliClient.activity.video.local.DownloadListActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.PrivateMsgApi;
import com.RobinNotBad.BiliClient.service.DownloadService;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class TestActivity extends BaseActivity {

    SwitchMaterial sw_wbi, sw_post;
    EditText input_link, input_data, output;
    MaterialCardView btn_request, btn_cookies, btn_start, btn_download, btn_download_goto, btn_download_clear;

    @SuppressLint("MutatingSharedPrefs")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        sw_wbi = findViewById(R.id.switch_wbi);
        sw_post = findViewById(R.id.switch_post);
        input_link = findViewById(R.id.input_link);
        input_data = findViewById(R.id.input_data);
        output = findViewById(R.id.output_json);
        btn_start = findViewById(R.id.start);
        btn_download = findViewById(R.id.download);
        btn_download_goto = findViewById(R.id.download_goto);
        btn_download_clear = findViewById(R.id.download_clear);

        sw_post.setOnCheckedChangeListener((compoundButton, checked) ->
                input_data.setVisibility(checked ? View.VISIBLE : View.GONE));

        btn_request = findViewById(R.id.request);

        btn_request.setOnClickListener(view -> CenterThreadPool.run(()->{
            try {
                String url = input_link.getText().toString();
                if(!url.startsWith("https://") && !url.startsWith("http://")) url = "https://" + url;

                if (sw_wbi.isChecked()) url = ConfInfoApi.signWBI(url);

                runOnUiThread(()->{
                    output.setText("");
                    MsgUtil.showMsg("发出请求！");
                });
                String result;
                if(sw_post.isChecked()) {
                    String data = input_data.getText().toString();
                    result = Objects.requireNonNull(NetWorkUtil.post(url, data).body()).string();
                }
                else {
                    result = Objects.requireNonNull(NetWorkUtil.get(url).body()).string();
                }

                runOnUiThread(()->{
                    output.setText(result);
                    MsgUtil.showMsg("请求成功！");
                });
            }catch (Exception e){
                runOnUiThread(()->{
                    output.setText(e.toString());
                    MsgUtil.showMsg("请求失败！");
                });
                e.printStackTrace();
            }
        }));

        btn_cookies = findViewById(R.id.cookies);
        btn_cookies.setOnClickListener(view -> {
            Intent intent = new Intent(this, SpecialLoginActivity.class);
            intent.putExtra("login", false);
            startActivity(intent);
        });

        btn_start.setOnClickListener(v -> startService(new Intent(this,DownloadService.class)));

        btn_download.setOnClickListener(v -> {
            try {
                JSONObject task = new JSONObject();
                task.put("type","video_single");
                task.put("aid", 693018306L);
                task.put("cid",971247999L);
                task.put("qn",16);
                task.put("name","我不曾忘记-致旅行中的你");
                task.put("parent","大慈树王");
                task.put("url_cover","http://i0.hdslb.com/bfs/archive/0ae3d490a8688772ff28da9e8aa24120107d55dc.jpg");
                task.put("url_dm","https://comment.bilibili.com/971247999.xml");

                SharedPreferences downloadPrefs = getSharedPreferences("download", MODE_PRIVATE);
                Set<String> set = downloadPrefs.getStringSet("list", new HashSet<>());
                set.add(task.toString());
                downloadPrefs.edit().putStringSet("list", set).apply();

                Log.d("download",set.toString());

                //startService(new Intent(TestActivity.this,DownloadService.class));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        btn_download_goto.setOnClickListener(view -> {
            Intent intent = new Intent(this, DownloadListActivity.class);
            startActivity(intent);
        });

        btn_download_clear.setOnClickListener(v -> {
            SharedPreferences downloadPrefs = getSharedPreferences("download", MODE_PRIVATE);
            LinkedHashSet<String> set = new LinkedHashSet<>();
            downloadPrefs.edit().putStringSet("list", set).apply();
        });
    }
}