package com.chat.base.entity

import com.mushanyux.mushanim.entity.MSChannelType

class GlobalChannel {
    var channel_id: String = ""
    var channel_type: Byte = MSChannelType.PERSONAL
    var channel_name: String = ""
    var channel_remark: String = ""


    fun getHtmlName():String{
        return channel_name.replace("<mark>", "<font color=#f65835>")
            .replace("</mark>", "</font>")
    }
}