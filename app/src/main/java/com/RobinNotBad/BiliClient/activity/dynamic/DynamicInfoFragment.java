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
import com.RobinNotBad.BiliClient.adapter.DynamicHolder;
import com.RobinNotBad.BiliClient.api.DynamicApi;
import com.RobinNotBad.BiliClient.model.Dynamic;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;

import org.json.JSONException;

import java.io.IOException;

//真正的视频详情页
//2023-07-17

public class DynamicInfoFragment extends Fragment {

    long id;

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
        if (getArguments() != null) {
            this.id = getArguments().getLong("id");
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

        CenterThreadPool.run(()->{
            try {
                Dynamic dynamic = DynamicApi.getDynamic(id);
                if(isAdded()) requireActivity().runOnUiThread(() -> {
                    View dynamicView = View.inflate(requireContext(),R.layout.cell_dynamic, scrollView);
                    DynamicHolder holder = new DynamicHolder(dynamicView,false);
                    holder.showDynamic(dynamic,requireContext());

                    if(dynamic.dynamic_forward != null){
                        Log.e("debug","有子动态！");
                        View childCard = View.inflate(requireContext(),R.layout.cell_dynamic_child,holder.extraCard);
                        DynamicHolder childHolder = new DynamicHolder(childCard,true);
                        childHolder.showDynamic(dynamic.dynamic_forward,requireContext());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }
}