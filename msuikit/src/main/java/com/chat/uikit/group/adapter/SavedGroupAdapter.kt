package com.chat.uikit.group.adapter

import android.text.TextUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.entity.PopupMenuItem
import com.chat.base.net.HttpResponseCode
import com.chat.base.ui.components.AvatarView
import com.chat.base.utils.MSDialogUtils
import com.chat.base.utils.MSToastUtils
import com.chat.uikit.R
import com.chat.uikit.group.GroupEntity
import com.chat.uikit.group.service.GroupModel
import com.mushanyux.mushanim.entity.MSChannelType

class SavedGroupAdapter :
    BaseQuickAdapter<GroupEntity, BaseViewHolder>(R.layout.item_group_layout) {
    override fun convert(holder: BaseViewHolder, item: GroupEntity) {
        holder.setText(R.id.nameTv, if (TextUtils.isEmpty(item.remark)) item.name else item.remark)
        val avatarView: AvatarView = holder.getView(R.id.avatarView)
        avatarView.showAvatar(item.group_no, MSChannelType.GROUP, item.avatar)

        val list: MutableList<PopupMenuItem> = ArrayList()
        list.add(
            PopupMenuItem(
                context.getString(R.string.delete),
                0,
                object : PopupMenuItem.IClick {
                    override fun onClick() {
                        GroupModel.getInstance().updateGroupSetting(
                            item.group_no, "save", 0
                        ) { code, msg ->
                            if (code == HttpResponseCode.success.toInt()) {
                                removeAt(holder.bindingAdapterPosition)
                            } else {
                                MSToastUtils.getInstance().showToast(msg)
                            }
                        }
                    }
                })
        )
        MSDialogUtils.getInstance().setViewLongClickPopup(holder.getView(R.id.contentLayout), list)
    }
}