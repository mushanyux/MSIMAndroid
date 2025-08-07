package com.chat.uikit.group;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.config.MSConfig;
import com.chat.base.msgitem.MSChannelMemberRole;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.utils.StringUtils;
import com.chat.base.views.NoEventRecycleView;
import com.chat.uikit.R;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;

public class RemindMemberAdapter extends BaseQuickAdapter<GroupMemberEntity, BaseViewHolder> {
    private final String channelID;
    private final byte channelType;
    private String searchKey;
    private int page = 1;
    private final String loginUID = MSConfig.getInstance().getUid();

    public RemindMemberAdapter(String channelID, byte channelType) {
        super(R.layout.item_choose_remind_layout);
        this.channelID = channelID;
        this.channelType = channelType;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, GroupMemberEntity groupMemberEntity) {
        AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
        avatarView.setSize(30);
        if (groupMemberEntity.member == null) {
            baseViewHolder.setText(R.id.nameTv, R.string.at_all);
            avatarView.imageView.setImageResource(R.mipmap.icon_mention_all);
        } else {
            TextView nameTv = baseViewHolder.getView(R.id.nameTv);
            String showName = groupMemberEntity.member.remark;
            if (TextUtils.isEmpty(showName)) {
                showName = TextUtils.isEmpty(groupMemberEntity.member.memberRemark) ? groupMemberEntity.member.memberName : groupMemberEntity.member.memberRemark;
            }
            if (TextUtils.isEmpty(searchKey)) {
                nameTv.setText(showName);
            } else {
                nameTv.setText(StringUtils.findSearch(Theme.colorAccount, showName, searchKey));
            }

            avatarView.showAvatar(groupMemberEntity.member.memberUID, MSChannelType.PERSONAL, groupMemberEntity.member.memberAvatarCacheKey);
        }
    }


    public void onNormal() {
        page = 1;
        this.searchKey = "";
        getChannelMember(true);
    }

    public void onSearch(String keyword) {
        this.searchKey = keyword;
        page = 1;
        getChannelMember(TextUtils.isEmpty(keyword));
    }

    private void getChannelMember(boolean isNormal) {
        int size = 100;
        MSIM.getInstance().getChannelMembersManager().getWithPageOrSearch(channelID, channelType, searchKey, page, size, (list, b) -> resort(list, isNormal));
    }

    private void resort(List<MSChannelMember> list, boolean isNormal) {
        List<GroupMemberEntity> memberList = new ArrayList<>();
        if (page == 1 && isNormal) {
            MSChannelMember channelMember = MSIM.getInstance().getChannelMembersManager().getMember(channelID, channelType, loginUID);
            if (channelMember != null && channelMember.role != MSChannelMemberRole.normal) {
                memberList.add(new GroupMemberEntity());
            }
        }
        for (MSChannelMember member : list) {
            if (member != null && member.isDeleted == 0 && !member.memberUID.equals(loginUID)) {
                memberList.add(new GroupMemberEntity(member));
            }
        }
        if (page == 1) {
            setList(memberList);
        } else {
            addData(memberList);
        }
        ((NoEventRecycleView) getRecyclerView()).setItemCount(getItemCount());
    }

    public String getSearchKey() {
        return searchKey;
    }
}
