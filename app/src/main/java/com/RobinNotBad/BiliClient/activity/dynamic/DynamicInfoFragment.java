package com.RobinNotBad.BiliClient.activity.dynamic;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.dynamic.DynamicHolder;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.PreInflateHelper;

//真正的视频详情页
//2023-07-17

public class DynamicInfoFragment extends Fragment {

    Dynamic dynamic;
    Runnable onFinishLoad;
    PreInflateHelper preInflateHelper;


    public DynamicInfoFragment() {
    }

    public static DynamicInfoFragment newInstance(Dynamic dynamic) {
        DynamicInfoFragment fragment = new DynamicInfoFragment();
        Bundle args = new Bundle();
        args.putSerializable("dynamic", dynamic);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.dynamic = (Dynamic) getArguments().getSerializable("dynamic");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        ScrollView scrollView = view.findViewById(R.id.scrollView);
        if (preInflateHelper == null) preInflateHelper = new PreInflateHelper(requireContext());

        CenterThreadPool.run(()->{
            if(isAdded()) requireActivity().runOnUiThread(() -> {
                View dynamicView = View.inflate(requireContext(),R.layout.cell_dynamic, scrollView);
                DynamicHolder holder = new DynamicHolder(dynamicView, (BaseActivity) getActivity(), false);
                holder.showDynamic(dynamic,requireContext(),false);
                View.OnLongClickListener onDeleteLongClick = DynamicHolder.getDeleteListener(requireActivity(), dynamic);
                holder.item_dynamic_delete.setOnLongClickListener(onDeleteLongClick);
                if (dynamic.canDelete) holder.item_dynamic_delete.setVisibility(View.VISIBLE);

                if(dynamic.dynamic_forward != null){
                    Log.e("debug","有子动态！");
                    View childCard = holder.cell_dynamic_child;
                    DynamicHolder childHolder = new DynamicHolder(childCard, (BaseActivity) getActivity(), true);
                    childHolder.showDynamic(dynamic.dynamic_forward,requireContext(),true);
                    childCard.setVisibility(View.VISIBLE);
                }

                if (onFinishLoad != null) onFinishLoad.run();
            });
        });

    }

    public void setOnFinishLoad(Runnable runnable) {
        this.onFinishLoad = runnable;
    }
}