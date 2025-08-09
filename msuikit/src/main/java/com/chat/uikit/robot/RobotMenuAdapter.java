package com.chat.uikit.robot;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.ui.components.AvatarView;
import com.chat.uikit.R;
import com.chat.uikit.robot.entity.MSRobotMenuEntity;
import com.mushanyux.mushanim.entity.MSChannelType;

public class RobotMenuAdapter extends BaseQuickAdapter<MSRobotMenuEntity, BaseViewHolder> {

    public RobotMenuAdapter() {
        super(R.layout.item_robot_menu_layout);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder baseViewHolder, MSRobotMenuEntity entity) {
        baseViewHolder.setText(R.id.cmdTv, entity.cmd);
        baseViewHolder.setText(R.id.remarkTv, entity.remark);
        AvatarView avatarView = baseViewHolder.getView(R.id.avatarView);
        avatarView.setSize(30);
        avatarView.showAvatar(entity.robot_id, MSChannelType.PERSONAL);
    }
}
