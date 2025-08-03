package com.chat.uikit.chat.provider

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.base.ui.components.AvatarView
import com.chat.base.views.BubbleLayout
import com.chat.uikit.R
import com.chat.uikit.chat.msgmodel.MSCardContent
import com.chat.uikit.user.UserDetailActivity
import com.mushanyux.mushanim.entity.MSChannelType

class MSCardProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return LayoutInflater.from(context).inflate(R.layout.chat_item_card, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val cardView = parentView.findViewById<LinearLayout>(R.id.cardView)
        cardView.layoutParams.width = getViewWidth(from, uiChatMsgItemEntity)
        val cardNameTv = parentView.findViewById<TextView>(R.id.userNameTv)
        val cardAvatarIv = parentView.findViewById<AvatarView>(R.id.userCardAvatarIv)
        val cardContent = uiChatMsgItemEntity.msMsg.baseContentMsgModel as MSCardContent
        cardNameTv.text = cardContent.name
        cardAvatarIv.showAvatar(cardContent.uid, MSChannelType.PERSONAL)
        resetCellBackground(parentView, uiChatMsgItemEntity, from)
        parentView.findViewById<View>(R.id.contentLayout).setOnClickListener {
            val intent = Intent(context, UserDetailActivity::class.java)
            intent.putExtra("uid", cardContent.uid)
            intent.putExtra("vercode", cardContent.vercode)
            intent.putExtra("name", cardContent.name)
            context.startActivity(intent)
        }

    }

    override val itemViewType: Int
        get() = MSContentType.MS_CARD

    override fun resetCellBackground(
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val bgType = getMsgBgType(
            uiChatMsgItemEntity.previousMsg,
            uiChatMsgItemEntity.msMsg,
            uiChatMsgItemEntity.nextMsg
        )
        val contentLayout = parentView.findViewById<BubbleLayout>(R.id.contentLayout)
        contentLayout.setAll(bgType, from, MSContentType.MS_CARD)
    }

    override fun resetCellListener(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.resetCellListener(position, parentView, uiChatMsgItemEntity, from)
        val contentLayout = parentView.findViewById<BubbleLayout>(R.id.contentLayout)
        addLongClick(contentLayout, uiChatMsgItemEntity)
    }
}