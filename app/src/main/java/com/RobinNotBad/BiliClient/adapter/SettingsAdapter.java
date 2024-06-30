package com.RobinNotBad.BiliClient.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.SettingSection;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//设置项
//2024-06-06

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Context context;
    final List<SettingSection> list;
    private static final Map<String, Integer> typeMap = new HashMap<>() {{
        put("switch", 0);
        put("choose", 1);
        //put("input_int",2);
        //put("input_float",3);

    }};

    public SettingsAdapter(Context context, List<SettingSection> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        return typeMap.get(list.get(position).type);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 1:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_setting_choose, parent, false);
                return new ChooseHolder(view);
            default:
                view = LayoutInflater.from(this.context).inflate(R.layout.cell_setting_switch, parent, false);
                return new SwitchHolder(view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SettingSection settingSection = list.get(position);
        switch (holder.getItemViewType()) {
            case 1:
                ChooseHolder chooseHolder = (ChooseHolder) holder;
                chooseHolder.bind(settingSection);
                break;
            default:
                SwitchHolder switchHolder = (SwitchHolder) holder;
                switchHolder.bind(settingSection);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SwitchHolder extends RecyclerView.ViewHolder {
        final TextView desc;
        final SwitchMaterial switchMaterial;

        public SwitchHolder(@NonNull View itemView) {
            super(itemView);
            desc = itemView.findViewById(R.id.setting_switch_desc);
            switchMaterial = itemView.findViewById(R.id.setting_switch);
        }

        public void bind(SettingSection settingSection) {
            desc.setText(settingSection.desc);
            switchMaterial.setText(settingSection.name);
            switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) ->
                    SharedPreferencesUtil.putBoolean(settingSection.id, isChecked));
            switchMaterial.setChecked(SharedPreferencesUtil.getBoolean(settingSection.id, Boolean.parseBoolean(settingSection.defaultValue)));
        }
    }

    public static class ChooseHolder extends RecyclerView.ViewHolder {
        final RadioButton chocola;
        final RadioButton vanilla;    //我在思考这样的命名方式是否合理（玩艹猫玩的
        final TextView name;
        final TextView desc;

        public ChooseHolder(@NonNull View itemView) {
            super(itemView);
            chocola = itemView.findViewById(R.id.setting_choose_chocola);
            vanilla = itemView.findViewById(R.id.setting_choose_vanilla);
            desc = itemView.findViewById(R.id.setting_choose_desc);
            name = itemView.findViewById(R.id.setting_choose_name);
        }

        public void bind(SettingSection settingSection) {
            desc.setText(settingSection.desc);
            name.setText(settingSection.name);
            chocola.setOnCheckedChangeListener((buttonView, isChecked) ->
                    SharedPreferencesUtil.putBoolean(settingSection.id, isChecked));
            boolean value = SharedPreferencesUtil.getBoolean(settingSection.id, Boolean.parseBoolean(settingSection.defaultValue));
            chocola.setChecked(value);
            vanilla.setChecked(!value);
        }
    }
}
