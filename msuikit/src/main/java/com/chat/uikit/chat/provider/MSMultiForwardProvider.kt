package com.chat.uikit.chat.provider

import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.chat.base.emoji.MoonUtil
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.base.views.BubbleLayout
import com.chat.uikit.R
import com.chat.uikit.chat.ChatMultiForwardDetailActivity
import com.chat.uikit.chat.msgmodel.MSMultiForwardContent
import com.mushanyux.mushanim.MSIM
import com.mushanyux.mushanim.entity.MSChannelType
import kotlin.math.min

class MSMultiForwardProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return LayoutInflater.from(context)
            .inflate(R.layout.chat_item_multi_forward, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val multiView = parentView.findViewById<LinearLayout>(R.id.multiView)
        multiView.layoutParams.width = getViewWidth(from, uiChatMsgItemEntity)
        val contentLayout = parentView.findViewById<BubbleLayout>(R.id.contentLayout)
        val titleTv = parentView.findViewById<TextView>(R.id.titleTv)
        val contentTv = parentView.findViewById<TextView>(R.id.contentTv)
        resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val multiForwardContent =
            uiChatMsgItemEntity.msMsg.baseContentMsgModel as MSMultiForwardContent
        val title: String = if (multiForwardContent.channelType.toInt() == 1) {
            if (multiForwardContent.userList.size > 1) {
                val sBuilder = StringBuilder()
                for (i in multiForwardContent.userList.indices) {
                    if (!TextUtils.isEmpty(sBuilder)) sBuilder.append("、")
                    sBuilder.append(multiForwardContent.userList[i].channelName)
                }
                sBuilder.toString()
            } else multiForwardContent.userList[0].channelName
        } else {
            context.getString(R.string.group_chat)
        }
        titleTv.text = String.format(context.getString(R.string.chat_title_records), title)
        //设置内容
        val sBuilder = StringBuilder()
        if (multiForwardContent.msgList != null && multiForwardContent.msgList.isNotEmpty()) {
            val size = min(multiForwardContent.msgList.size, 3)
            for (i in 0 until size) {
                var name = ""
                var content = ""
                val fromUID = multiForwardContent.msgList[i].fromUID
                val messageContent = multiForwardContent.msgList[i].baseContentMsgModel
                if (messageContent != null) {
                    content = if (!TextUtils.isEmpty(messageContent.displayContent)) {
                        messageContent.displayContent
                    } else {
                        context.getString(R.string.base_unknow_msg)
                    }
                    // 如果文字太长滑动会卡顿
                    if (content.length > 100) {
                        content = content.substring(0, 80)
                    }
                }
                if (!TextUtils.isEmpty(fromUID)) {
                    val mChannel = MSIM.getInstance().channelManager.getChannel(
                        fromUID,
                        MSChannelType.PERSONAL
                    )
                    if (mChannel != null) {
                        name = mChannel.channelName
                    } else {
                        MSIM.getInstance().channelManager.fetchChannelInfo(
                            fromUID,
                            MSChannelType.PERSONAL
                        )
                    }
                }
                if (!TextUtils.isEmpty(sBuilder)) sBuilder.append("\n")
                sBuilder.append(name).append(":").append(content)
            }
        }
        // 显示表情
        MoonUtil.identifyFaceExpression(context, contentTv, sBuilder.toString(), MoonUtil.DEF_SCALE)
        addLongClick(contentLayout, uiChatMsgItemEntity)
        contentLayout.setOnClickListener {
            val intent = Intent(context, ChatMultiForwardDetailActivity::class.java)
            intent.putExtra("client_msg_no", uiChatMsgItemEntity.msMsg.clientMsgNO)
            context.startActivity(intent)
        }
    }

    override val itemViewType: Int
        get() = MSContentType.MS_MULTIPLE_FORWARD

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
        contentLayout.setAll(bgType, from, MSContentType.MS_MULTIPLE_FORWARD)
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