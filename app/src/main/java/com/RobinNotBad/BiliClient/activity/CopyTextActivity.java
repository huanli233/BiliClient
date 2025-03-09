package com.RobinNotBad.BiliClient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;

public class CopyTextActivity extends BaseActivity {
    private String content = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy);

        Intent intent = getIntent();

        content = intent.getStringExtra("content");

        if (content == null) {
            finish();
            return;
        }

        EditText edittext = findViewById(R.id.content);
        edittext.setText(content);

        EditText beginEdit = findViewById(R.id.begin_index);
        EditText endEdit = findViewById(R.id.end_index);

        edittext.setOnFocusChangeListener((view, b) -> {
            if (!b) {
                beginEdit.setText(String.valueOf(edittext.getSelectionStart()));
                endEdit.setText(String.valueOf(edittext.getSelectionEnd()));
            }
        });


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    edittext.setSelection(Integer.parseInt(String.valueOf(beginEdit.getText())), Integer.parseInt(String.valueOf(endEdit.getText())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
        beginEdit.addTextChangedListener(textWatcher);
        endEdit.addTextChangedListener(textWatcher);

        findViewById(R.id.copy_all).setOnClickListener(view -> {
            ToolsUtil.copyText(this, content);
            MsgUtil.showMsg("已复制");
        });
        findViewById(R.id.copy).setOnClickListener(view -> {
            try {
                ToolsUtil.copyText(this, content.substring(Integer.parseInt(String.valueOf(beginEdit.getText()))
                        , Integer.parseInt(String.valueOf(endEdit.getText()))));   //我嘞个超级括号
                MsgUtil.showMsg("已复制");
            } catch (Exception e) {
                MsgUtil.showMsg("复制失败，请检查选择的范围");
            }
        });

        findViewById(R.id.begin_left).setOnClickListener(view -> {
            try {
                if (Integer.parseInt(String.valueOf(beginEdit.getText())) - 1 < 0)
                    beginEdit.setText("0");
                else
                    beginEdit.setText(String.valueOf(Integer.parseInt(String.valueOf(beginEdit.getText())) - 1));
            } catch (Exception e) {
                beginEdit.setText("0");
            }
        });
        findViewById(R.id.begin_right).setOnClickListener(view -> {
            try {
                if (Integer.parseInt(String.valueOf(beginEdit.getText())) + 1 > edittext.getText().length())
                    beginEdit.setText(String.valueOf(edittext.getText().length()));
                else
                    beginEdit.setText(String.valueOf(Integer.parseInt(String.valueOf(beginEdit.getText())) + 1));
            } catch (Exception e) {
                beginEdit.setText("0");
            }
        });
        findViewById(R.id.end_left).setOnClickListener(view -> {
            try {
                if (Integer.parseInt(String.valueOf(endEdit.getText())) - 1 < 0)
                    endEdit.setText("0");
                else
                    endEdit.setText(String.valueOf(Integer.parseInt(String.valueOf(endEdit.getText())) - 1));
            } catch (Exception e) {
                endEdit.setText("0");
            }
        });
        findViewById(R.id.end_right).setOnClickListener(view -> {
            try {
                if (Integer.parseInt(String.valueOf(endEdit.getText())) + 1 > edittext.getText().length())
                    endEdit.setText(String.valueOf(edittext.getText().length()));
                else
                    endEdit.setText(String.valueOf(Integer.parseInt(String.valueOf(endEdit.getText())) + 1));
            } catch (Exception e) {
                endEdit.setText("0");
            }
        });
        findViewById(R.id.begin_left).setOnLongClickListener(view -> {
            beginEdit.setText("0");
            return false;
        });
        findViewById(R.id.begin_right).setOnLongClickListener(view -> {
            beginEdit.setText(String.valueOf(edittext.getText().length()));
            return false;
        });
        findViewById(R.id.end_left).setOnLongClickListener(view -> {
            endEdit.setText("0");
            return false;
        });
        findViewById(R.id.end_right).setOnLongClickListener(view -> {
            endEdit.setText(String.valueOf(edittext.getText().length()));
            return false;
        });
    }
}
