package com.RobinNotBad.BiliClient.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.helper.TutorialHelper;
import com.RobinNotBad.BiliClient.model.Tutorial;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import java.util.Timer;
import java.util.TimerTask;

public class TutorialActivity extends BaseActivity {
    private int wait_time = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        
        Intent intent = getIntent();
        Tutorial tutorial = TutorialHelper.loadTutorial(getResources().getXml(intent.getIntExtra("xml_id",R.xml.tutorial_default)));
        
        ((TextView)findViewById(R.id.title)).setText(tutorial.name);
        ((TextView)findViewById(R.id.content)).setText(TutorialHelper.loadText(tutorial.content));
        
        try{
            if(tutorial.imgid != -1) ((ImageFilterView)findViewById(R.id.image_view)).setImageDrawable(getResources().getDrawable(tutorial.imgid));
            else findViewById(R.id.image_view).setVisibility(View.GONE);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        MaterialButton close_btn = findViewById(R.id.close_btn);
        close_btn.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if(wait_time-- > 0){
                        close_btn.setText("已阅(" + wait_time + "s)");
                        close_btn.setEnabled(false);
                    }else{
                        close_btn.setText("已阅");
                        close_btn.setEnabled(true);
                        timer.cancel();
                    }
                });
            }
        },0,1000);
        close_btn.setOnClickListener(view -> finish());
    }
    
    @Override
    public void onBackPressed() {}
}
