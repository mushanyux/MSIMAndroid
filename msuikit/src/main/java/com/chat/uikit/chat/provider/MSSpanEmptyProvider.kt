package com.chat.uikit.chat.provider

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.base.utils.AndroidUtilities
import com.chat.uikit.R

class MSSpanEmptyProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return null
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val contentLayout = parentView.findViewById<LinearLayout>(R.id.contentLayout)
        var height = AndroidUtilities.dp(50f)
        if (uiChatMsgItemEntity.msMsg != null) {
            height = uiChatMsgItemEntity.msMsg.messageSeq
        }
        contentLayout.layoutParams.height = height
    }

    override val itemViewType: Int
        get() = MSContentType.spanEmptyView

    override val layoutId: Int
        get() = R.layout.chat_item_span_empty_view
}