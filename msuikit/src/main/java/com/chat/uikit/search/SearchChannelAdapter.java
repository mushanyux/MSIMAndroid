package com.chat.uikit.search;

import android.text.SpannableString;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.utils.StringUtils;
import com.chat.uikit.R;
import com.mushanyux.mushanim.entity.MSChannelSearchResult;

import org.jetbrains.annotations.NotNull;

/**
 * 搜索频道
 */
public class SearchChannelAdapter extends BaseQuickAdapter<MSChannelSearchResult, BaseViewHolder> {
    private String searchKey;

    public SearchChannelAdapter() {
        super(R.layout.item_search_channel_layout);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, MSChannelSearchResult result) {
        AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
        avatarView.showAvatar(result.msChannel);
        if (TextUtils.isEmpty(searchKey)) {
            baseViewHolder.setText(R.id.nameTv, TextUtils.isEmpty(result.msChannel.channelRemark) ? result.msChannel.channelName : result.msChannel.channelRemark);
        } else {
            String name = result.msChannel.channelRemark;
            if (TextUtils.isEmpty(name))
                name = result.msChannel.channelName;
            SpannableString key = StringUtils.findSearch(Theme.colorAccount, name, searchKey);
            baseViewHolder.setText(R.id.nameTv, key);
        }
        if (!TextUtils.isEmpty(result.containMemberName)) {
            SpannableString key = StringUtils.findSearch(Theme.colorAccount, getContext().getString(R.string.contian) + result.containMemberName, searchKey);
            baseViewHolder.setText(R.id.contentTv, key);
        }
        baseViewHolder.setGone(R.id.contentTv, TextUtils.isEmpty(result.containMemberName));
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
       notifyItemRangeChanged(0,getItemCount());
    }
}
