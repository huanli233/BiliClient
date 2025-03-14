package com.RobinNotBad.BiliClient.activity.user;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.video.VideoCardAdapter;
import com.RobinNotBad.BiliClient.api.WatchLaterApi;
import com.RobinNotBad.BiliClient.model.VideoCard;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//稍后再看
//2023-08-17

public class WatchLaterActivity extends RefreshListActivity {

    private int longClickPosition = -1;
    private long longClickTimestamp;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("稍后再看");
        recyclerView.setHasFixedSize(true);

        CenterThreadPool.run(() -> {
            try {
                ArrayList<VideoCard> videoCardList = WatchLaterApi.getWatchLaterList();
                VideoCardAdapter adapter = new VideoCardAdapter(this, videoCardList);

                adapter.setOnLongClickListener(position -> {
                    long timestamp = System.currentTimeMillis();
                    if (longClickPosition == position && timestamp - longClickTimestamp < 4000) {
                        CenterThreadPool.run(() -> {
                            try {
                                int result = WatchLaterApi.delete(videoCardList.get(position).aid);
                                longClickPosition = -1;
                                if (result == 0) runOnUiThread(() -> {
                                    MsgUtil.showMsg("删除成功");
                                    videoCardList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, videoCardList.size() - position);
                                });
                                else
                                    runOnUiThread(() -> MsgUtil.showMsg("删除失败，错误码：" + result));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        longClickPosition = position;
                        longClickTimestamp = timestamp;
                        MsgUtil.showMsg("再次长按删除");
                    }
                });

                setAdapter(adapter);
                setRefreshing(false);
            } catch (Exception e) {
                loadFail(e);
            }
        });
    }

}