package com.chat.base.endpoint.entity

import com.chat.base.msg.IConversationContext
import com.mushanyux.mushanim.entity.MSMsg

class MSMsg2UiMsgMenu(
    val iConversationContext: IConversationContext,
    val msMsg: MSMsg,
    val memberCount: Int,
    val showNickName: Boolean,
    val isChoose: Boolean
)