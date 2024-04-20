package com.RobinNotBad.BiliClient.activity.video.info;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerChooseActivity;
import com.RobinNotBad.BiliClient.activity.video.JumpToPlayerActivity;
import com.RobinNotBad.BiliClient.adapter.MediaEpisodeAdapter;
import com.RobinNotBad.BiliClient.api.BangumiApi;
import com.RobinNotBad.BiliClient.model.Bangumi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

public class BangumiInfoFragment extends Fragment {
    private long mediaId;
    private int selectedSection = 0, selectedEpisode = 0;
    private Dialog dialog;
    private View rootView;
    private RecyclerView eposideRecyclerView;
    private Button section_choose;
    private TextView eposide_choose;

    private Bangumi bangumi;

    public static BangumiInfoFragment newInstance(long mediaId) {
        Bundle args = new Bundle();
        args.putLong("media_id", mediaId);
        BangumiInfoFragment fragment = new BangumiInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mediaId = arguments.getLong("media_id");
        }
        rootView = inflater.inflate(R.layout.fragment_media_info, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        eposideRecyclerView = rootView.findViewById(R.id.rv_eposide_list);
        //拉数据
        CenterThreadPool.run(() -> {
            try {
                bangumi = BangumiApi.getBangumi(mediaId);
                if (isAdded()) requireActivity().runOnUiThread(this::initView);
            } catch (Exception e) {
                if (isAdded())
                    requireActivity().runOnUiThread(() -> MsgUtil.err(e, requireContext()));
            }
        });
    }

    private void initView() {
        //init data.
        ImageView imageMediaCover = rootView.findViewById(R.id.image_media_cover);
        TextView title = rootView.findViewById(R.id.title);
        section_choose = rootView.findViewById(R.id.section_choose);
        Button playButton = rootView.findViewById(R.id.btn_play);
        eposide_choose = rootView.findViewById(R.id.eposide_choose);
        selectedSection = 0;

        Glide.with(this)
                .load(GlideUtil.url(bangumi.info.cover_horizontal) )
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.mipmap.loading_2233)
                .into(imageMediaCover);
        title.setText(bangumi.info.title);
        //section selector setting.
        MediaEpisodeAdapter adapter = new MediaEpisodeAdapter();

        adapter.setOnItemClickListener(index -> {
            selectedEpisode = index;
            refreshReplies();
        });

        section_choose.setOnClickListener(v -> getSectionChooseDialog().show());
        eposide_choose.setOnClickListener(v -> getEposideChooseDialog().show());

        adapter.setData(bangumi.sectionList.get(0).episodeList);
        eposideRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        eposideRecyclerView.setAdapter(adapter);

        //play button setting
        playButton.setOnClickListener(v -> {
            Bangumi.Episode episode = bangumi.sectionList.get(selectedSection).episodeList.get(selectedEpisode);
            Glide.get(requireContext()).clearMemory();
            Intent intent = new Intent(v.getContext(), JumpToPlayerActivity.class);
            intent.putExtra("cid", episode.cid);
            intent.putExtra("title", episode.title);
            intent.putExtra("aid", episode.aid);
            intent.putExtra("html5",false);
            startActivity(intent);
        });
        playButton.setOnLongClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SettingPlayerChooseActivity.class);
            startActivity(intent);
            return true;
        });

        refreshReplies();
    }

    @SuppressLint("SetTextI18n")
    private Dialog getSectionChooseDialog() {
        String[] choices = new String[bangumi.sectionList.size()];
        for (int i = 0; i < bangumi.sectionList.size(); i++) {
            choices[i] = bangumi.sectionList.get(i).title;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setSingleChoiceItems(choices, selectedSection, (dialog, which) -> {
            selectedSection = which;
            selectedEpisode = 0;

            refreshReplies();
            Bangumi.Section section = bangumi.sectionList.get(which);
            section_choose.setText(section.title + " 点击切换");
            MediaEpisodeAdapter adapter = (MediaEpisodeAdapter) eposideRecyclerView.getAdapter();
            if (adapter != null) {
                adapter.setData(bangumi.sectionList.get(which).episodeList);
                eposideRecyclerView.scrollToPosition(0);
            }
            eposide_choose.setOnClickListener(v -> getEposideChooseDialog().show());
            dialog.dismiss();
        });
        dialog = builder.create();

        return dialog;
    }

    private Dialog getEposideChooseDialog() {
        ArrayList<Bangumi.Episode> episodeList = bangumi.sectionList.get(selectedSection).episodeList;

        String[] choices = new String[episodeList.size()];
        for (int i = 0; i < episodeList.size(); i++) {
            Bangumi.Episode episode = episodeList.get(i);
            choices[i] = episode.title + "." + episode.title_long;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setSingleChoiceItems(choices, selectedEpisode, (dialog, which) -> {
            selectedEpisode = which;
            refreshReplies();

            MediaEpisodeAdapter adapter = (MediaEpisodeAdapter) eposideRecyclerView.getAdapter();
            if (adapter != null) {
                adapter.setSelectedItemIndex(which);
                eposideRecyclerView.scrollToPosition(which);
            }
            dialog.dismiss();
        });
        dialog = builder.create();

        return dialog;
    }

    private void refreshReplies(){
        Activity activity = requireActivity();
        if(activity instanceof VideoInfoActivity){
            ((VideoInfoActivity) activity).setCurrentAid(bangumi.sectionList.get(selectedSection).episodeList.get(selectedEpisode).aid);
        }
    }
}
