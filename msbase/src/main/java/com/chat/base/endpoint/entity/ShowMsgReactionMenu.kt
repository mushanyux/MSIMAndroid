package com.chat.base.endpoint.entity

import android.widget.FrameLayout
import com.chat.base.msg.ChatAdapter
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.mushanyux.mushanim.entity.MSMsgReaction

class ShowMsgReactionMenu(
    val parentView: FrameLayout,
    val from: MSChatIteMsgFromType,
    val chatAdapter: ChatAdapter,
    val list: List<MSMsgReaction>?
)