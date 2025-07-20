package com.chat.base.endpoint.entity

import com.chat.base.msg.IConversationContext
import com.mushanyux.mushanim.entity.MSMsg

class ChatItemPopupMenu(
    var imageResource: Int,
    var text: String,
    var iPopupItemClick: IPopupItemClick
) {
    var subText: String = ""
    var tag: String = ""

    interface IPopupItemClick {
        fun onClick(mMsg: MSMsg, iConversationContext: IConversationContext)
    }
}