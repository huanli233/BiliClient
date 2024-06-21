package com.RobinNotBad.BiliClient.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.EmoteApi;
import com.RobinNotBad.BiliClient.model.Emote;
import com.RobinNotBad.BiliClient.model.EmotePackage;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class EmoteActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emote);

        ImageView loading = findViewById(R.id.loading);
        TabLayout tabLayout = findViewById(R.id.tl_tab);
        ViewPager viewPager = findViewById(R.id.viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.bgblack));
        RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };
        CenterThreadPool.run(() -> {
            try {
                List<EmotePackage> packages = EmoteApi.getEmotes(EmoteApi.BUSINESS_DYNAMIC);
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), packages, origin -> {
                        origin.setOnListScroll(onScrollListener);
                        return origin;
                    }));
                    tabLayout.setupWithViewPager(viewPager);
                    tabLayout.setInlineLabel(true);
                    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        }
                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {
                            tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED);
                        }
                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {
                            tab.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED);
                        }
                    });
                    tabLayout.setTabIconTint(null);
                    int count = tabLayout.getTabCount();
                    for (int i = 0; i < count; i++) {
                        int finalI = i;
                        Objects.requireNonNull(tabLayout.getTabAt(finalI)).setText(packages.get(finalI).text);
                        if (finalI != 0) Objects.requireNonNull(tabLayout.getTabAt(finalI)).setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED);
                        CenterThreadPool.run(() -> {
                            try {
                                Drawable drawable = Glide.with(this).asDrawable().load(packages.get(finalI).url).placeholder(R.mipmap.placeholder).submit().get();
                                runOnUiThread(() -> Objects.requireNonNull(tabLayout.getTabAt(finalI)).setIcon(drawable));
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> MsgUtil.err(e, this));
            }
        });
    }

    static class PagerAdapter extends FragmentPagerAdapter {

        final List<EmotePackage> emotes;
        final FragmentHandler handler;

        public PagerAdapter(@NonNull FragmentManager fm, List<EmotePackage> emotes, FragmentHandler handler) {
            super(fm);
            this.emotes = emotes;
            this.handler = handler;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return handler.handleCreateFragment(EmoteFragment.newInstance(emotes.get(position)));
        }

        @Override
        public int getCount() {
            return emotes.size();
        }

        interface FragmentHandler {
            Fragment handleCreateFragment(EmoteFragment origin);
        }
    }

    public static class EmoteFragment extends Fragment {
        private EmotePackage emotePackage;
        private RecyclerView recyclerView;
        private boolean hasListener;
        private RecyclerView.OnScrollListener onListScroll;

        public static EmoteFragment newInstance(EmotePackage emotePackage) {
            EmoteFragment emoteFragment = new EmoteFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("emotePackage", emotePackage);
            emoteFragment.setArguments(bundle);
            return emoteFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle bundle = getArguments();
            if (bundle != null) {
                this.emotePackage = (EmotePackage) bundle.getSerializable("emotePackage");
            }
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_simple_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            this.recyclerView = view.findViewById(R.id.recyclerView);
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4, RecyclerView.VERTICAL, false);
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return emotePackage.type == 4 ? 2 : emotePackage.emotes.get(position).size;
                }
            });
            recyclerView.setLayoutManager(layoutManager);
            if (onListScroll != null) {
                hasListener = true;
                recyclerView.addOnScrollListener(onListScroll);
            }

            EmoteAdapter adapter = new EmoteAdapter(emotePackage, getContext());
            adapter.setOnClickEmote((emote) -> {
                requireActivity().setResult(RESULT_OK, new Intent().putExtra("text", emote.name));
                requireActivity().finish();
            });
            recyclerView.addItemDecoration(new GridSpacingItemDecoration(4, getResources().getDimensionPixelSize(R.dimen.grid_spacing), true));
            recyclerView.setAdapter(adapter);
        }

        public void setOnListScroll(RecyclerView.OnScrollListener onScrollListener) {
            if (this.onListScroll == null) this.onListScroll = onScrollListener;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            if (this.onListScroll != null && hasListener) recyclerView.removeOnScrollListener(onListScroll);
        }
    }

    static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    static class EmoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final EmotePackage emotePackage;
        private final Context context;
        private OnClickEmoteListener listener;

        public EmoteAdapter(EmotePackage emotePackage, Context context) {
            this.emotePackage = emotePackage;
            this.context = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return viewType == 1 ? new Holder(new ImageView(context)) : new TextHolder(new TextView(context));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Emote emote = emotePackage.emotes.get(position);
            if (holder instanceof Holder) {
                Glide.with(context).asDrawable().load(GlideUtil.url(emote.url))
                        .into(((Holder) holder).itemView);
                ((Holder) holder).itemView.setOnClickListener((view) -> {
                    if (listener != null) {
                        listener.onClickEmote(emote);
                    }
                });
            } else {
                ((TextHolder) holder).itemView.setSingleLine();
                ((TextHolder) holder).itemView.setEllipsize(TextUtils.TruncateAt.END);
                ((TextHolder) holder).itemView.setText(emote.name);
                ((TextHolder) holder).itemView.setOnClickListener((view) -> {
                    if (listener != null) {
                        listener.onClickEmote(emote);
                    }
                });
            }
            TooltipCompat.setTooltipText(holder.itemView, emote.alias != null ? emote.alias : emote.name);
        }

        @Override
        public int getItemCount() {
            return emotePackage.emotes.size();
        }

        @Override
        public int getItemViewType(int position) {
            return emotePackage.type == 4 ? 0 : 1;
        }

        public void setOnClickEmote(OnClickEmoteListener listener) {
            this.listener = listener;
        }

        interface OnClickEmoteListener {
            void onClickEmote(Emote emote);
        }

        static class Holder extends RecyclerView.ViewHolder {
            private final ImageView itemView;
            public Holder(@NonNull View itemView) {
                super(itemView);
                this.itemView = (ImageView) itemView;
            }
        }
        static class TextHolder extends RecyclerView.ViewHolder {
            private final TextView itemView;
            public TextHolder(@NonNull TextView itemView) {
                super(itemView);
                this.itemView = itemView;
            }
        }
    }

}