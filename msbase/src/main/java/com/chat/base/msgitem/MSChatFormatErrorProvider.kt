package com.chat.base.msgitem

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chat.base.R
import com.chat.base.views.BubbleLayout

class MSChatFormatErrorProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return LayoutInflater.from(context)
            .inflate(R.layout.chat_content_format_err_layout, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val linearLayout = parentView.findViewById<LinearLayout>(R.id.contentLayout)
        val contentTv = parentView.findViewById<TextView>(R.id.contentTv)
        val bubbleLayout = parentView.findViewById<BubbleLayout>(R.id.bubbleLayout)
        val bgType = getMsgBgType(
            uiChatMsgItemEntity.previousMsg,
            uiChatMsgItemEntity.msMsg,
            uiChatMsgItemEntity.nextMsg
        )
        bubbleLayout.setAll(bgType, from, MSContentType.MS_CONTENT_FORMAT_ERROR)
        if (from == MSChatIteMsgFromType.SEND) {
            linearLayout.gravity = Gravity.END
            contentTv.setTextColor(ContextCompat.getColor(context, R.color.black))
        } else if (from == MSChatIteMsgFromType.RECEIVED) {
            linearLayout.gravity = Gravity.START
            contentTv.setTextColor(ContextCompat.getColor(context, R.color.colorDark))
        }
        addLongClick(bubbleLayout, uiChatMsgItemEntity)
    }

    override val itemViewType: Int
        get() = MSContentType.MS_CONTENT_FORMAT_ERROR
}