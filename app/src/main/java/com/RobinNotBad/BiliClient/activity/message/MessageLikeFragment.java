package com.RobinNotBad.BiliClient.activity.message;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.video.info.VideoReplyFragment;
import com.RobinNotBad.BiliClient.adapter.MessageLikeAdapter;
import com.RobinNotBad.BiliClient.adapter.ReplyAdapter;
import com.RobinNotBad.BiliClient.api.MessageApi;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.RobinNotBad.BiliClient.model.MessageLikeInfo;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageLikeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<MessageLikeInfo> latestMessageList;
    private ArrayList<MessageLikeInfo> totalMessageList;

    private MessageLikeAdapter totalMessageAdapter;

    public MessageLikeFragment(){

    }

    public static MessageLikeFragment newInstance() {
        MessageLikeFragment fragment = new MessageLikeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        latestMessageList = new ArrayList<>();
        totalMessageList = new ArrayList<>();

        new Thread(()->{
            try {
                JSONObject messagesObject = MessageApi.getLikeMsg();
                totalMessageList = ((ArrayList<MessageLikeInfo>)messagesObject.get("total"));
                Log.e("总消息数", String.valueOf(totalMessageList.size()));

                totalMessageAdapter = new MessageLikeAdapter(getContext(), totalMessageList);
                if(isAdded()) requireActivity().runOnUiThread(()-> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setAdapter(totalMessageAdapter);
                });
            } catch (IOException e){
                requireActivity().runOnUiThread(()-> MsgUtil.quickErr(MsgUtil.err_net,getContext()));
                e.printStackTrace();
            } catch (JSONException e) {
                requireActivity().runOnUiThread(()-> MsgUtil.jsonErr(e,getContext()));
                e.printStackTrace();
            }
        }).start();
    }
}
