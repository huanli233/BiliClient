package com.RobinNotBad.BiliClient.activity.dynamic;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.adapter.dynamic.DynamicHolder;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.RobinNotBad.BiliClient.util.TerminalContext;

//真正的视频详情页
//2023-07-17

public class DynamicInfoFragment extends Fragment {
    private static final String TAG = "DynamicInfoFragment";

    Dynamic dynamic;
    Runnable onFinishLoad;


    public DynamicInfoFragment() {
    }

    public static DynamicInfoFragment newInstance(long id) {
        DynamicInfoFragment fragment = new DynamicInfoFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            long id = getArguments().getLong("id", 0);
            dynamic = TerminalContext.getInstance().getDynamicById(id).getValue().getOrThrow();
        } catch (Exception e) {
            Log.wtf(TAG, e);
            MsgUtil.showMsg("找不到动态信息QAQ");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ScrollView scrollView = view.findViewById(R.id.scrollView);

        if(SharedPreferencesUtil.getBoolean("ui_landscape",false)) {
            WindowManager windowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            if(Build.VERSION.SDK_INT >= 17) display.getRealMetrics(metrics);
            else display.getMetrics(metrics);
            int paddings = metrics.widthPixels / 6;
            scrollView.setPadding(paddings,0,paddings,0);
        }

        CenterThreadPool.run(() -> {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                View dynamicView = View.inflate(requireContext(), R.layout.cell_dynamic, scrollView);
                DynamicHolder holder = new DynamicHolder(dynamicView, (BaseActivity) getActivity(), false);
                holder.showDynamic(dynamic, requireContext(), false);
                View.OnLongClickListener onDeleteLongClick = DynamicHolder.getDeleteListener(requireActivity(), dynamic);
                holder.item_dynamic_delete.setOnLongClickListener(onDeleteLongClick);
                if (dynamic.canDelete) holder.item_dynamic_delete.setVisibility(View.VISIBLE);

                if (dynamic.dynamic_forward != null) {
                    Log.e("debug", "有子动态！");
                    View childCard = holder.cell_dynamic_child;
                    DynamicHolder childHolder = new DynamicHolder(childCard, (BaseActivity) getActivity(), true);
                    childHolder.showDynamic(dynamic.dynamic_forward, requireContext(), true);
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