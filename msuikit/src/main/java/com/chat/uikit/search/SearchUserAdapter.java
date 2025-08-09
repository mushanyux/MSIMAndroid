package com.chat.uikit.search;

import android.text.TextUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.config.MSConfig;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.AvatarView;
import com.chat.uikit.R;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.List;

/**
 * 搜索好友信息
 */
public class SearchUserAdapter extends BaseMultiItemQuickAdapter<SearchUserEntity, BaseViewHolder> {
    public SearchUserAdapter(@Nullable List<SearchUserEntity> data) {
        super(data);
        addItemType(0, R.layout.item_search_user_layout);
        addItemType(1, R.layout.item_nodata_layout);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, SearchUserEntity item) {
        switch (item.getItemType()) {
            case 1:
                break;
            case 0:
                AvatarView avatarView = helper.getView(R.id.avatarView);
                avatarView.showAvatar(item.data.uid, MSChannelType.PERSONAL);
                helper.setText(R.id.nameTv, item.data.name);
                helper.setGone(R.id.applyBtn, !item.showApply || !TextUtils.isEmpty(item.data.uid) && item.data.uid.equals(MSConfig.getInstance().getUid()));
                helper.getView(R.id.applyBtn).setAlpha(item.status == 0 ? 1 : 0.2f);
                Button applyBtn = helper.getView(R.id.applyBtn);
                applyBtn.getBackground().setTint(Theme.colorAccount);
                break;
        }
    }
}
