package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.settings.login.SpecialLoginActivity;
import com.RobinNotBad.BiliClient.api.ConfInfoApi;
import com.RobinNotBad.BiliClient.api.CookiesApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public class TestActivity extends BaseActivity {

    SwitchMaterial sw_wbi, sw_post;
    EditText input_link, input_data, output;
    MaterialCardView btn_crash,btn_request, btn_cookies, btn_payload;

    JSONArray conversation;

    @SuppressLint({"MutatingSharedPrefs", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        sw_wbi = findViewById(R.id.switch_wbi);
        sw_post = findViewById(R.id.switch_post);
        input_link = findViewById(R.id.input_link);
        input_data = findViewById(R.id.input_data);
        output = findViewById(R.id.output_json);
        btn_crash = findViewById(R.id.crash);
        btn_payload = findViewById(R.id.payload);

        input_link.setText(SharedPreferencesUtil.getString("dev_test_link", ""));

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

        btn_payload.setOnClickListener(v -> CenterThreadPool.run(()->{
            try {
                String payload = CookiesApi.genCookiePayload();
                runOnUiThread(()-> input_link.setText("https://api.bilibili.com/x/internal/gaia-gateway/ExClimbWuzhi"));
                runOnUiThread(()-> input_data.setText(payload));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));


        //我为什么要加这个？
        btn_crash.setOnClickListener(v -> {
            input_data.setVisibility(View.VISIBLE);
            sw_wbi.setText("使用R1");
            sw_post.setVisibility(View.GONE);
            btn_cookies.setVisibility(View.GONE);
            btn_request.setVisibility(View.GONE);
            btn_payload.setVisibility(View.GONE);
            TextView desc = findViewById(R.id.desc);
            desc.setText(getString(R.string.dev_catgirl_desc));

            CenterThreadPool.run(() -> {
                try {
                    if (conversation == null) {
                        conversation = new JSONArray();
                        JSONObject prompt = new JSONObject();
                        try {
                            prompt.put("role", "system");
                            prompt.put("content", getString(R.string.dev_catgirl_prompt));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        conversation.put(prompt);
                        runOnUiThread(()-> input_link.setText(SharedPreferencesUtil.getString("dev_catgirl_apikey", "")));
                        return;
                    }

                    String api_key = input_link.getText().toString();
                    if (api_key.isEmpty()) {
                        MsgUtil.showMsg("请在链接栏填写API KEY！");
                        return;
                    } else {
                        SharedPreferencesUtil.putString("dev_catgirl_apikey", api_key);
                    }
                    ArrayList<String> deepseekHeaders = new ArrayList<>() {{
                        add("Content-Type");
                        add("application/json");
                        add("Authorization");
                        add("Bearer " + api_key);
                        add("Accept");
                        add("text/event-stream");
                    }};

                    String input_str = input_data.getText().toString();
                    if (input_str.isEmpty()) {
                        MsgUtil.showMsg("请在POST数据栏填写文字！");
                        return;
                    }
                    JSONObject input_json = new JSONObject();
                    input_json.put("role", "user");
                    input_json.put("content", input_str);
                    conversation.put(input_json);

                    JSONObject requestJson = new JSONObject();
                    String model = sw_wbi.isChecked() ? "reasoner" : "chat";
                    requestJson.put("model", "deepseek-" + model);
                    requestJson.put("stream", true);
                    requestJson.put("messages", conversation);

                    MsgUtil.showMsg("发出请求，请等待回应！");
                    runOnUiThread(() -> {
                        btn_crash.setEnabled(false);
                        output.setText("");
                        input_link.clearFocus();
                        input_data.clearFocus();
                        output.clearFocus();
                        input_link.setEnabled(false);
                        input_data.setEnabled(false);
                        output.setEnabled(false);
                    });

                    Response response = NetWorkUtil.postJson("https://api.deepseek.com/chat/completions",
                            requestJson.toString(),
                            deepseekHeaders);
                    ResponseBody body = response.body();
                    if (body == null) return;
                    BufferedSource source = body.source();

                    MsgUtil.showMsg("得到响应，请继续等待！");

                    boolean reasoning = sw_wbi.isChecked();

                    StringBuilder contentBuilder = new StringBuilder();

                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line == null) break;
                        Log.d("debug-deepseek", line);

                        if (line.startsWith("data:")) {
                            String jsonData = line.substring(6).trim();
                            if ("[DONE]".equals(jsonData)) break;

                            JSONObject data = new JSONObject(jsonData);
                            JSONArray choices = data.getJSONArray("choices");
                            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");

                            String deltaContent;
                            if (!delta.isNull("reasoning_content")) {
                                deltaContent = delta.optString("reasoning_content");
                            } else if (!delta.isNull("content")) {
                                if (reasoning) {
                                    reasoning = false;
                                    runOnUiThread(() -> output.append("\n\n*思考结束*\n\n"));
                                }
                                deltaContent = delta.optString("content");
                            } else deltaContent = "";

                            if(!reasoning) contentBuilder.append(deltaContent);
                            runOnUiThread(() -> output.append(deltaContent));
                        }
                    }

                    response.close();

                    String output_str = contentBuilder.toString();
                    if (!output_str.isEmpty()) {
                        JSONObject output_json = new JSONObject();
                        output_json.put("role", "assistant");
                        output_json.put("content", output_str);
                        conversation.put(output_json);
                    }

                    MsgUtil.showMsg("响应结束，请查看下方文本框！");
                } catch (Exception e) {
                    report(e);
                }

                runOnUiThread(() -> {
                    btn_crash.setEnabled(true);
                    output.setEnabled(true);
                    input_link.setEnabled(true);
                    input_data.setEnabled(true);
                });
            });

        });
    }

    @Override
    protected void onDestroy() {
        if(conversation == null)
            SharedPreferencesUtil.putString("dev_test_link", input_link.getText().toString());
        super.onDestroy();
    }
}