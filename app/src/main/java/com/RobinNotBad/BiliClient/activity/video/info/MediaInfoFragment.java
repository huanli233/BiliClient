package com.RobinNotBad.BiliClient.activity.video.info;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
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
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.settings.SettingPlayerActivity;
import com.RobinNotBad.BiliClient.activity.video.JumpToPlayerActivity;
import com.RobinNotBad.BiliClient.adapter.MediaEpisodesAdapter;
import com.RobinNotBad.BiliClient.api.bangumi_to_card;
import com.RobinNotBad.BiliClient.model.Media;
import com.RobinNotBad.BiliClient.model.MediaSectionInfo;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;

import java.io.IOException;

public class MediaInfoFragment extends Fragment {
    private String mediaId;
    private int selectedSection = 0, selectedEpidose = 0;
    private MediaSectionInfo sectionInfo;
    private Dialog dialog;
    private View rootView;
    private RecyclerView eposideRecyclerView;
    private Button section_choose;
    private TextView eposide_choose;

    public static MediaInfoFragment newInstance(long mediaId) {
        Bundle args = new Bundle();
        args.putString("media_id", String.valueOf(mediaId));
        MediaInfoFragment fragment = new MediaInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mediaId = arguments.getString("media_id");
        }
        rootView = inflater.inflate(R.layout.fragment_media_info, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        eposideRecyclerView = rootView.findViewById(R.id.rv_eposide_list);
        //拉数据
        LiveData<Pair<Media, MediaSectionInfo>> pairLiveData = CenterThreadPool.supplyAsync(() -> {
            try {
                Media mediaInfo = bangumi_to_card.getMediaInfo(mediaId);
                MediaSectionInfo sectionInfo = bangumi_to_card.getSectionInfo(String.valueOf(mediaInfo.seasonId));
                return new Pair<>(mediaInfo, sectionInfo);
            } catch (JSONException e) {
                if(isAdded()) requireActivity().runOnUiThread(()->MsgUtil.jsonErr(e,requireContext()));
                e.printStackTrace();
                return null;
            } catch (IOException e){
                if(isAdded()) requireActivity().runOnUiThread(()->MsgUtil.netErr(requireContext()));
                e.printStackTrace();
                return null;
            }
        });
        pairLiveData.observe(getViewLifecycleOwner(), pair -> {
            if (pair != null) {
                initView(pair.first, pair.second);
                Activity activity = requireActivity();
                if(activity instanceof VideoInfoActivity){
                    ((VideoInfoActivity) activity).setCurrentEpisodeInfo(pair.second.mainSection.episodes[0]);
                }
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void setSelectSectionIndex(int index) {
        MediaSectionInfo.SectionInfo section = index == 0 ? sectionInfo.mainSection : sectionInfo.sections[index - 1];
        section_choose.setText(section.title + " 点击切换");
        MediaEpisodesAdapter adapter = (MediaEpisodesAdapter) eposideRecyclerView.getAdapter();
        if (adapter != null) {
            adapter.setData(section.episodes);
            eposideRecyclerView.scrollToPosition(0);
        }
        selectedSection = index;
        eposide_choose.setOnClickListener(v -> getEposideChooseDialog(section).show());


    }

    private void initView(Media baseMediaInfo, MediaSectionInfo mediaSectionInfo) {
        //init data.
        ImageView imageMediaCover = rootView.findViewById(R.id.image_media_cover);
        TextView title = rootView.findViewById(R.id.title);
        section_choose = rootView.findViewById(R.id.section_choose);
        Button playButton = rootView.findViewById(R.id.btn_play);
        eposide_choose = rootView.findViewById(R.id.eposide_choose);
        selectedSection = 0;
        sectionInfo = mediaSectionInfo;
        Glide.with(this)
                .load(baseMediaInfo.horizontalCover)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.mipmap.loading_2233)
                .into(imageMediaCover);
        title.setText(baseMediaInfo.title);
        //section selector setting.
        MediaEpisodesAdapter adapter = new MediaEpisodesAdapter();
        adapter.setOnItemClickListener(episodeInfo -> {
            Activity activity = requireActivity();
            if(activity instanceof VideoInfoActivity){
                ((VideoInfoActivity) activity).setCurrentEpisodeInfo(episodeInfo);
            }
        });
        section_choose.setOnClickListener(v -> getSectionChooseDialog(mediaSectionInfo).show());
        eposide_choose.setOnClickListener(v -> getEposideChooseDialog(mediaSectionInfo.mainSection).show());
        adapter.setData(mediaSectionInfo.mainSection.episodes);
        eposideRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        eposideRecyclerView.setAdapter(adapter);
        //play button setting
        playButton.setOnClickListener(v -> {
            MediaSectionInfo.SectionInfo section = selectedSection == 0 ? mediaSectionInfo.mainSection : mediaSectionInfo.sections[selectedSection - 1];
            MediaSectionInfo.EpisodeInfo episodeInfo = section.episodes[adapter.getSelectedItemIndex()];
            Glide.get(requireContext()).clearMemory();
            Intent intent = new Intent(v.getContext(), JumpToPlayerActivity.class);
            intent.putExtra("cid", episodeInfo.cid);
            intent.putExtra("title", episodeInfo.title);
            intent.putExtra("aid", episodeInfo.aid);
            intent.putExtra("html5",false);
            startActivity(intent);
        });
        playButton.setOnLongClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SettingPlayerActivity.class);
            startActivity(intent);
            return true;
        });
    }

    private Dialog getSectionChooseDialog(MediaSectionInfo mediaSectionInfo) {
            String[] choices = new String[mediaSectionInfo.sections.length + 1];
            choices[0] = mediaSectionInfo.mainSection.title;
            for (int i = 0; i < mediaSectionInfo.sections.length; i++) {
                choices[i + 1] = mediaSectionInfo.sections[i].title;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setSingleChoiceItems(choices, selectedSection, (dialog, which) -> {
                setSelectSectionIndex(which);
                dialog.dismiss();
            });
            dialog = builder.create();

        return dialog;
        }

    private Dialog getEposideChooseDialog(MediaSectionInfo.SectionInfo sectionInfo) {
            String[] choices = new String[sectionInfo.episodes.length];
            for (int i = 0; i < sectionInfo.episodes.length; i++) {
                choices[i] = (i+1) + "." + sectionInfo.episodes[i].title;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setSingleChoiceItems(choices, selectedEpidose, (dialog, which) -> {
                Activity activity = requireActivity();
                if(activity instanceof VideoInfoActivity){
                    ((VideoInfoActivity) activity).setCurrentEpisodeInfo(sectionInfo.episodes[which]);
                }
                MediaEpisodesAdapter adapter = (MediaEpisodesAdapter) eposideRecyclerView.getAdapter();
                selectedEpidose = which;
                if(adapter!=null) {
                    adapter.setSelectedItemIndex(which);
                    eposideRecyclerView.scrollToPosition(which);
                }
                dialog.dismiss();
            });
            dialog = builder.create();

        return dialog;
    }
}
