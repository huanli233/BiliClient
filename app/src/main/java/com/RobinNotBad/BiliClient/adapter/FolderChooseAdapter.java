package com.RobinNotBad.BiliClient.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.api.FavoriteApi;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.LittleToolsUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

//选择收藏夹
//2023-08-28

public class FolderChooseAdapter extends RecyclerView.Adapter<FolderChooseAdapter.FolderHolder> {

    Context context;
    ArrayList<String> folderList;
    ArrayList<Boolean> chooseState;
    ArrayList<Long> fidList;
    long aid;
    boolean adding;
    public boolean added;
    public boolean changed;

    public FolderChooseAdapter(Context context, ArrayList<String> folderList, ArrayList<Long> fidList, ArrayList<Boolean> chooseState, long aid) {
        this.context = context;
        this.folderList = folderList;
        this.fidList = fidList;
        this.chooseState = chooseState;
        this.aid = aid;
    }

    @NonNull
    @Override
    public FolderHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.cell_choose,parent,false);
        return new FolderHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderHolder holder, int position) {

        MaterialCardView cardView = (MaterialCardView) holder.itemView;

        holder.folder_name.setText(folderList.get(position));
        setCardView(cardView,chooseState.get(position));

        holder.itemView.setOnClickListener(view -> {
            if(!adding) {
                adding = true;
                cardView.setStrokeColor(context.getResources().getColor(R.color.low_pink));
                cardView.setStrokeWidth(LittleToolsUtil.dp2px(1f, context));

                if (chooseState.get(position)) {
                    CenterThreadPool.run(() -> {
                        try {
                            int result = FavoriteApi.deleteFavorite(aid, fidList.get(position));
                            adding = false;
                            if (result == 0) {
                                chooseState.set(position, false);
                                ((Activity) context).runOnUiThread(() -> setCardView(cardView, false));
                                changed = true;
                            } else ((Activity) context).runOnUiThread(() -> {
                                MsgUtil.toast("删除失败！错误码：" + result, context);
                                setCardView(cardView, true);
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    cardView.setStrokeColor(context.getResources().getColor(R.color.gray));
                    cardView.setStrokeWidth(LittleToolsUtil.dp2px(0.1f, context));
                    CenterThreadPool.run(() -> {
                        try {
                            int result = FavoriteApi.addFavorite(aid, fidList.get(position));
                            adding = false;
                            if (result == 0) {
                                chooseState.set(position, true);
                                ((Activity) context).runOnUiThread(() -> setCardView(cardView,true));
                                changed = true;
                                added = true;
                            } else ((Activity) context).runOnUiThread(() -> {
                                    MsgUtil.toast( "添加失败！错误码：" + result, context);
                                    setCardView(cardView,false);
                                });
                            if (SharedPreferencesUtil.getBoolean("fav_single", false))
                                ((Activity) context).finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    public static class FolderHolder extends RecyclerView.ViewHolder{
        TextView folder_name;

        public FolderHolder(@NonNull View itemView) {
            super(itemView);
            folder_name = itemView.findViewById(R.id.folder_name);
        }
    }

    private void setCardView(MaterialCardView cardView, boolean bool){
        if(bool){
            cardView.setStrokeColor(context.getResources().getColor(R.color.pink));
            cardView.setStrokeWidth(LittleToolsUtil.dp2px(1,context));
        }
        else{
            cardView.setStrokeColor(context.getResources().getColor(R.color.gray));
            cardView.setStrokeWidth(LittleToolsUtil.dp2px(0.1f,context));
        }
    }

    public boolean isAllDeleted(){
        for (boolean b:chooseState) {
            if(b) return false;
        }
        return true;
    }
}
