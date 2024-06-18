package com.RobinNotBad.BiliClient.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.EmoteApi;
import com.RobinNotBad.BiliClient.model.Emote;
import com.RobinNotBad.BiliClient.model.EmotePackage;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.GlideUtil;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.ToolsUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class EmoteActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emote);

        TabLayout tabLayout = findViewById(R.id.tl_tab);
        ViewPager viewPager = findViewById(R.id.viewPager);
        tabLayout.setBackgroundColor(getResources().getColor(R.color.bgblack));
        CenterThreadPool.run(() -> {
            try {
                List<EmotePackage> packages = EmoteApi.getEmotes(EmoteApi.BUSINESS_DYNAMIC);
                runOnUiThread(() -> {
                    viewPager.setAdapter(new PagerAdapter(getSupportFragmentManager(), packages));
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
                    tabLayout.setTabTextColors(Color.WHITE, Color.WHITE);
                    int count = tabLayout.getTabCount();
                    for (int i = 0; i < count; i++) {
                        int finalI = i;
                        CenterThreadPool.run(() -> {
                            try {
                                Drawable drawable = Glide.with(this).asDrawable().load(packages.get(finalI).url).placeholder(R.mipmap.placeholder).submit().get();
                                runOnUiThread(() -> {
                                    Objects.requireNonNull(tabLayout.getTabAt(finalI)).setIcon(drawable);
                                    Objects.requireNonNull(tabLayout.getTabAt(finalI)).setText(packages.get(finalI).text);
                                    if (finalI != 0) Objects.requireNonNull(tabLayout.getTabAt(finalI)).setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED);
                                });
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

        List<EmotePackage> emotes;

        public PagerAdapter(@NonNull FragmentManager fm, List<EmotePackage> emotes) {
            super(fm);
            this.emotes = emotes;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return new EmoteFragment(emotes.get(position));
        }

        @Override
        public int getCount() {
            return emotes.size();
        }
    }

    public static class EmoteFragment extends Fragment {
        private final EmotePackage emotePackage;

        public EmoteFragment(EmotePackage emotePackage) {
            this.emotePackage = emotePackage;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_simple_list, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), emotePackage.type == 4 ? 2 : 6, RecyclerView.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);

            EmoteAdapter adapter = new EmoteAdapter(emotePackage, getContext());
            adapter.setOnClickEmote((emote) -> {
                requireActivity().setResult(RESULT_OK, new Intent().putExtra("text", emote.name));
                requireActivity().finish();
            });
            recyclerView.setAdapter(adapter);
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
            if (holder instanceof Holder) {
                Glide.with(context).asDrawable().load(GlideUtil.url(emotePackage.emotes.get(position).url))
                        .into(((Holder) holder).itemView);
                ((Holder) holder).itemView.setOnClickListener((view) -> {
                    if (listener != null) {
                        listener.onClickEmote(emotePackage.emotes.get(position));
                    }
                });
            } else {
                ((TextHolder) holder).itemView.setText(emotePackage.emotes.get(position).name);
                ((TextHolder) holder).itemView.setOnClickListener((view) -> {
                    if (listener != null) {
                        listener.onClickEmote(emotePackage.emotes.get(position));
                    }
                });
            }
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