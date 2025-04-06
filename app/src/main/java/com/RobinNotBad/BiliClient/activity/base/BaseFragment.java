package com.RobinNotBad.BiliClient.activity.base;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.RobinNotBad.BiliClient.BiliTerminal;

public class BaseFragment extends Fragment {
    public void runOnUiThread(Runnable runnable){
        if(isAdded()) requireActivity().runOnUiThread(runnable);
    }

    public Context getAppContext(){
        return BiliTerminal.context;
    }
}
