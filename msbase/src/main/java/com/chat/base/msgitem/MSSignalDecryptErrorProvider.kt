package com.chat.base.msgitem

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chat.base.R
import com.chat.base.views.BubbleLayout

class MSSignalDecryptErrorProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return LayoutInflater.from(context)
            .inflate(R.layout.chat_signal_decrypt_err_layout, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val linearLayout = parentView.findViewById<LinearLayout>(R.id.contentLayout)
        val bubbleLayout = parentView.findViewById<BubbleLayout>(R.id.bubbleLayout)
        val bgType = getMsgBgType(
            uiChatMsgItemEntity.previousMsg,
            uiChatMsgItemEntity.msMsg,
            uiChatMsgItemEntity.nextMsg
        )
        bubbleLayout.setAll(bgType, from, MSContentType.MS_SIGNAL_DECRYPT_ERROR)
        val contentTv = parentView.findViewById<TextView>(R.id.contentTv)

        when (from) {
            MSChatIteMsgFromType.SEND -> {
                linearLayout.gravity = Gravity.END
                contentTv.setTextColor(Color.parseColor("#313131"))
            }
            MSChatIteMsgFromType.RECEIVED -> {
                linearLayout.gravity = Gravity.START
                contentTv.setTextColor(ContextCompat.getColor(context, R.color.colorDark))
            }
            else -> {
                linearLayout.gravity = Gravity.CENTER
                contentTv.textSize = 12f
                contentTv.setTextColor(Color.parseColor("#8D8D8D"))
                contentTv.setBackgroundResource(R.drawable.radian_normal_layout)
            }
        }
        addLongClick(bubbleLayout, uiChatMsgItemEntity)
    }

    override val itemViewType: Int
        get() = MSContentType.MS_SIGNAL_DECRYPT_ERROR
}