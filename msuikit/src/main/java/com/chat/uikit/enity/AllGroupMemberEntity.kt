package com.chat.uikit.enity

import com.mushanyux.mushanim.entity.MSChannelMember

class AllGroupMemberEntity(
    val channelMember: MSChannelMember,
    val onLine: Int,
    val lastOfflineTime: String,
    val lastOnlineTime: String,
) {
}