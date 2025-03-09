package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.MenuActivity;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.DragAdapter;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SortSettingActivity extends BaseActivity {

    final List<String> data = new ArrayList<>();
    final Map<String, String> displayKeyMap = new HashMap<>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_list);

        setPageName("菜单排序");

        String sortConf = SharedPreferencesUtil.getString(SharedPreferencesUtil.MENU_SORT, "");
        String[] splitName;
        if (!TextUtils.isEmpty(sortConf) && (splitName = sortConf.split(";")).length == MenuActivity.btnNames.size()) {
            for (String name : splitName) {
                if (!MenuActivity.btnNames.containsKey(name)) {
                    data.clear();
                    displayKeyMap.clear();
                    for (Map.Entry<String, Pair<String, Class<? extends InstanceActivity>>> entry : MenuActivity.btnNames.entrySet()) {
                        String displayText = entry.getValue().first;
                        data.add(displayText);
                        displayKeyMap.put(displayText, entry.getKey());
                    }
                    break;
                } else {
                    String displayText = Objects.requireNonNull(MenuActivity.btnNames.get(name)).first;
                    data.add(displayText);
                    displayKeyMap.put(displayText, name);
                }
            }
        } else {
            for (Map.Entry<String, Pair<String, Class<? extends InstanceActivity>>> entry : MenuActivity.btnNames.entrySet()) {
                String key = entry.getKey();
                String displayText = entry.getValue().first;
                data.add(displayText);
                displayKeyMap.put(displayText, key);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DragAdapter dragAdapter = new DragAdapter(this, data);
        recyclerView.setAdapter(dragAdapter);

        DragCallBack dragCallBack = new DragCallBack(dragAdapter, data);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(dragCallBack);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        dragAdapter.setOnItemClickListener(new DragAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
            }

            @Override
            public void onItemLongClick(DragAdapter.ViewHolder holder) {
                if (holder.getAdapterPosition() != dragAdapter.getFixedPosition()) {
                    itemTouchHelper.startDrag(holder);
                }
            }
        });

        dragAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                save();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MsgUtil.showMsg("拖动以排序~");
    }

    @Override
    protected void onPause() {
        super.onPause();
        save();
        MsgUtil.showMsg("已保存");
    }

    private void save() {
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (String s : data) {
            if (displayKeyMap.containsKey(s)) {
                if (flag) {
                    sb.append(";");
                } else {
                    flag = true;
                }
                sb.append(displayKeyMap.get(s));
            }
        }
        SharedPreferencesUtil.putString(SharedPreferencesUtil.MENU_SORT, sb.toString());
    }

    static class DragCallBack extends ItemTouchHelper.Callback {

        final DragAdapter mAdapter;
        final List<String> mData;

        public DragCallBack(DragAdapter adapter, List<String> data) {
            this.mAdapter = adapter;
            this.mData = data;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags;
            int swipeFlags = 0;
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.UP | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, swipeFlags);
            } else if (layoutManager instanceof LinearLayoutManager) {
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            } else {
                return 0;
            }
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            // 起始位置
            int fromPosition = viewHolder.getAdapterPosition();
            // 结束位置
            int toPosition = target.getAdapterPosition();
            // 固定位置
            if (fromPosition == mAdapter.getFixedPosition() || toPosition == mAdapter.getFixedPosition()) {
                return false;
            }
            // 根据滑动方向交换数据
            if (fromPosition < toPosition) {
                // 含头不含尾
                for (int index = fromPosition; index < toPosition; index++) {
                    Collections.swap(mData, index, index + 1);
                }
            } else {
                for (int index = fromPosition; index > toPosition; index--) {
                    Collections.swap(mData, index, index - 1);
                }
            }
            // 刷新布局
            mAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            mData.remove(position);
            mAdapter.notifyItemRemoved(position);
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder != null) {
                    ViewCompat.animate(viewHolder.itemView).setDuration(200).scaleX(1.3F).scaleY(1.3F).start();
                }
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // 恢复显示
            // 这里不能用if判断，因为GridLayoutManager是LinearLayoutManager的子类，改用switch，类型推导有区别
            switch (Objects.requireNonNull(recyclerView.getLayoutManager()).getClass().getSimpleName()) {
                case "GridLayoutManager":
                case "LinearLayoutManager":
                    ViewCompat.animate(viewHolder.itemView).setDuration(200).scaleX(1F).scaleY(1F).start();
                    break;
            }
            super.clearView(recyclerView, viewHolder);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return super.isLongPressDragEnabled();
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return super.isItemViewSwipeEnabled();
        }
    }

}