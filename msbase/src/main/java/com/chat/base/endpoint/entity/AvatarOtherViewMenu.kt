package com.chat.base.endpoint.entity

import android.widget.FrameLayout
import com.chat.base.ui.components.AvatarView
import com.mushanyux.mushanim.entity.MSChannel

class AvatarOtherViewMenu(
    val otherView: FrameLayout,
    val channel: MSChannel,
    val avatarView: AvatarView,
    val showUpdateDialog: Boolean
) {
}