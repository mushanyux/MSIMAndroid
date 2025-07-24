package com.chat.base.emoji;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chat.base.R;
import com.chat.base.base.MSBaseFragment;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.databinding.FragEmojiLayoutBinding;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.MSReader;

import java.util.ArrayList;
import java.util.List;

/**
 * emoji小表情fragment
 */
public class EmojiFragment extends MSBaseFragment<FragEmojiLayoutBinding> {
    EmojiAdapter emojiAdapter;
    private IEmojiClick iEmojiClick;
    int width = 0;

    @Override
    protected FragEmojiLayoutBinding getViewBinding() {
        return FragEmojiLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        width = AndroidUtilities.getScreenWidth() - (AndroidUtilities.dp(30) * 8);
        Theme.setColorFilter(getContext(), msVBinding.deleteIv, R.color.popupTextColor);
        List<EmojiEntry> emojiIndexs = new ArrayList<>();
        List<EmojiEntry> normalList = EmojiManager.getInstance().getEmojiWithType("0_");
        List<EmojiEntry> naturelList = EmojiManager.getInstance().getEmojiWithType("1_");
        List<EmojiEntry> symbolsList = EmojiManager.getInstance().getEmojiWithType("2_");
        emojiIndexs.addAll(normalList);
        emojiIndexs.addAll(naturelList);
        emojiIndexs.addAll(symbolsList);
        emojiAdapter = new EmojiAdapter(new ArrayList<>(), width);
        emojiAdapter.setList(emojiIndexs);
        msVBinding.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(8, StaggeredGridLayoutManager.VERTICAL));
        msVBinding.recyclerView.setAdapter(emojiAdapter);
        emojiAdapter.addFooterView(getFooterView());
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initListener() {
        emojiAdapter.setOnItemClickListener((adapter, view, position) -> {
            String index = (String) adapter.getItem(position);
            emojiClick(index);
        });
        msVBinding.deleteLayout.setOnClickListener(v -> {
            if (iEmojiClick != null) {
                iEmojiClick.onEmojiClick("");
            }
        });
//        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
//                outRect.left = 20;
//                outRect.right = 0;
//                outRect.bottom = 20;
//                outRect.top = 10;
//            }
//        });
    }

    @Override
    protected void initData() {
        getCommonEmoji();
    }

    @Override
    protected void setTitle(TextView titleTv) {

    }

    public void setOnEmojiClick(IEmojiClick iEmojiClick) {
        this.iEmojiClick = iEmojiClick;
    }

    private void getCommonEmoji() {
        //查看最近使用到表情
        String ids = MSSharedPreferencesUtil.getInstance().getSPWithUID("common_used_emojis");
        List<EmojiEntry> list = new ArrayList<>();
        String tempIds = "";
        if (!TextUtils.isEmpty(ids)) {
            if (ids.contains(",")) {
                String[] emojiIds = ids.split(",");
                for (String emojiId : emojiIds) {
                    if (list.size() == 32) break;
                    if (!TextUtils.isEmpty(emojiId)) {
                        EmojiEntry entry = EmojiManager.getInstance().getEmojiEntry(emojiId);
                        if (entry != null) {
                            list.add(entry);
                        }
                        if (TextUtils.isEmpty(tempIds)) {
                            tempIds = emojiId;
                        } else tempIds = tempIds + "," + emojiId;
                    }

                }
            } else {
                EmojiEntry entry = EmojiManager.getInstance().getEmojiEntry(ids);
                if (entry != null) {
                    list.add(entry);
                }
                tempIds = ids;
            }
        }
        if (MSReader.isEmpty(list)) return;
        emojiAdapter.removeAllHeaderView();
        View headerView = LayoutInflater.from(getContext()).inflate(R.layout.common_used_emoji_header_layout, null);
        RecyclerView recyclerView = headerView.findViewById(R.id.recyclerView);
        EmojiAdapter headerAdapter = new EmojiAdapter(new ArrayList<>(), width);
        headerAdapter.addData(list);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(8, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(headerAdapter);
        emojiAdapter.addHeaderView(headerView);
        MSSharedPreferencesUtil.getInstance().putSPWithUID("common_used_emojis", tempIds);
        headerAdapter.setOnItemClickListener((adapter, view, position) -> {
            String index = (String) adapter.getItem(position);
            emojiClick(index);
        });
    }

    private void emojiClick(String name) {
        if (!TextUtils.isEmpty(name)) {
            if (iEmojiClick != null) {
                iEmojiClick.onEmojiClick(name);
            }
            String usedIndexs = MSSharedPreferencesUtil.getInstance().getSPWithUID("common_used_emojis");
            String tempIndexs = "";
            if (!TextUtils.isEmpty(usedIndexs)) {
                if (usedIndexs.contains(",")) {
                    String[] strings = usedIndexs.split(",");
                    for (String string : strings) {
                        if (!string.equals(name)) {
                            if (TextUtils.isEmpty(tempIndexs)) {
                                tempIndexs = string;
                            } else {
                                tempIndexs = tempIndexs + "," + string;
                            }
                        }
                    }
                }
            }
            tempIndexs = name + "," + tempIndexs;
            MSSharedPreferencesUtil.getInstance().putSPWithUID("common_used_emojis", tempIndexs);
        }
    }

    private View getFooterView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.common_used_emoji_footer_layout, null);
    }

    public interface IEmojiClick {
        void onEmojiClick(String emojiName);
    }
}
