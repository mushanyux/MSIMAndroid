package com.chat.uikit.chat.provider

import android.view.View
import android.view.ViewGroup
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.uikit.R

class MSPromptNewMsgProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return null
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
    }

    override val layoutId: Int
        get() = R.layout.chat_item_unread_msg_line
    override val itemViewType: Int
        get() = MSContentType.msgPromptNewMsg

}