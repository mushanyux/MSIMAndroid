package com.chat.base.endpoint.entity

import com.mushanyux.mushanim.entity.MSMsg

class PrivacyMessageMenu(val iClick: IClick) {

    interface IClick {
        fun onDelete(mMsg: MSMsg)
        fun clearChannelMsg(channelID: String, channelType: Byte)
    }
}