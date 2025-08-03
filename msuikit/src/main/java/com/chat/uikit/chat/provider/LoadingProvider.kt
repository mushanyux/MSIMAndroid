package com.chat.uikit.chat.provider

import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.base.ui.components.RadialProgressView
import com.chat.uikit.R

class LoadingProvider : MSChatBaseProvider() {
    override val layoutId: Int
        get() = R.layout.chat_item_loading
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return null
    }

    override fun convert(helper: BaseViewHolder, item: MSUIChatMsgItemEntity) {
        super.convert(helper, item)
        helper.getView<RadialProgressView>(R.id.progress).setSize(50)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
    }

    override val itemViewType: Int
        get() = MSContentType.loading

}