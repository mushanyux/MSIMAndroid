package com.chat.base.msgitem

import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.MSBaseApplication
import com.chat.base.R
import com.chat.base.config.MSConfig
import com.chat.base.msg.ChatAdapter
import com.chat.base.ui.Theme
import com.chat.base.ui.components.NormalClickableContent
import com.chat.base.ui.components.NormalClickableSpan
import com.chat.base.ui.components.SystemMsgBackgroundColorSpan
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.MSTimeUtils
import com.mushanyux.mushanim.MSIM
import com.mushanyux.mushanim.entity.MSChannelType
import com.mushanyux.mushanim.entity.MSMsg

class MSRevokeProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return null
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
    }

    override val layoutId: Int
        get() = R.layout.chat_system_layout
    override val itemViewType: Int
        get() = MSContentType.revoke

    override fun convert(helper: BaseViewHolder, item: MSUIChatMsgItemEntity) {
        super.convert(helper, item)
        val isReEdit: Boolean
        var content: String = showRevokeMsg(item.msMsg)
        if (!TextUtils.isEmpty(item.msMsg.fromUID) && !TextUtils.isEmpty(
                item.msMsg.remoteExtra.revoker
            ) && item.msMsg.fromUID == MSConfig.getInstance().uid && item.msMsg.remoteExtra.revoker == MSConfig.getInstance().uid
        ) {
            if (item.msMsg.type == MSContentType.MS_TEXT && MSTimeUtils.getInstance().currentSeconds - item.msMsg.timestamp < 300) {
                isReEdit = true
                content = String.format("%s %s", content, context.getString(R.string.re_edit))
            } else {
                content = String.format("%s", content)
                isReEdit = false
            }
        } else {
            isReEdit = false
            content = String.format("%s", content)
        }

        val textView: TextView = helper.getView(R.id.contentTv)
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.setShadowLayer(AndroidUtilities.dp(5f).toFloat(), 0f, 0f, 0)
        val str = SpannableString(content)
        str.setSpan(
            SystemMsgBackgroundColorSpan(
                ContextCompat.getColor(
                    context,
                    R.color.colorSystemBg
                ), AndroidUtilities.dp(5f), AndroidUtilities.dp((2 * 5).toFloat())
            ), 0, content.length, 0
        )
        if (isReEdit) {
            val index = content.indexOf(context.getString(R.string.re_edit))
            str.setSpan(
                NormalClickableSpan(false,
                    Theme.colorAccount,
                    NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""),
                    object : NormalClickableSpan.IClick {
                        override fun onClick(view: View) {
                            val chatAdapter = getAdapter() as ChatAdapter
                            if (item.msMsg.baseContentMsgModel.reply != null && !TextUtils.isEmpty(
                                    item.msMsg.baseContentMsgModel.reply.message_id
                                )
                            ) {
                                val mMsg =
                                    MSIM.getInstance().msgManager.getWithMessageID(item.msMsg.baseContentMsgModel.reply.message_id)
                                if (mMsg != null) {
                                    chatAdapter.replyMsg(mMsg)
                                }
                            }
                            chatAdapter.setEditContent(item.msMsg.baseContentMsgModel.displayContent)
                        }
                    }),
                index,
                index + context.getString(R.string.re_edit).length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textView.text = str
    }


    companion object {

        fun showRevokeMsg(mMsg: MSMsg?): String {
            val content: String
            if (mMsg == null) return ""
            if (!TextUtils.isEmpty(mMsg.remoteExtra.revoker) && !TextUtils.isEmpty(mMsg.fromUID)) {
                if (mMsg.remoteExtra.revoker == mMsg.fromUID) {
                    if (mMsg.fromUID == MSConfig.getInstance().uid) {
                        content =
                            MSBaseApplication.getInstance().context.getString(R.string.my_revoke_msg)
                    } else {
                        val mChannel = mMsg.from
                        var showName: String? = ""
                        if (mChannel != null) {
                            showName = mChannel.channelRemark
                            if (TextUtils.isEmpty(showName)) showName = mChannel.channelName
                        } else {
                            val member = mMsg.memberOfFrom
                            if (member != null) showName =
                                if (TextUtils.isEmpty(member.memberRemark)) member.memberName else member.memberRemark
                        }
                        if (TextUtils.isEmpty(showName)){
                            MSIM.getInstance().channelManager.fetchChannelInfo(mMsg.fromUID,MSChannelType.PERSONAL)
                        }
                        content = String.format(
                            MSBaseApplication.getInstance().context.getString(R.string.user_revoke_msg),
                            showName
                        )
                    }
                } else {
                    var showName: String? = ""
                    if (mMsg.remoteExtra.revoker == MSConfig.getInstance().uid) {
                        // 你撤回了一条成员''的消息
                        if (mMsg.memberOfFrom != null) {
                            showName =
                                if (TextUtils.isEmpty(mMsg.memberOfFrom.memberRemark)) mMsg.memberOfFrom.memberName else mMsg.memberOfFrom.memberRemark
                        }
                        if (TextUtils.isEmpty(showName)) {
                            val mChannel = MSIM.getInstance().channelManager.getChannel(
                                mMsg.fromUID,
                                MSChannelType.PERSONAL
                            )
                            if (mChannel != null) {
                                showName =
                                    if (TextUtils.isEmpty(mChannel.channelRemark)) mChannel.channelName else mChannel.channelRemark
                            }
                        }
                        content = String.format(
                            MSBaseApplication.getInstance().context.getString(R.string.manager_revoke_user_msg),
                            showName
                        )
                    } else {
                        // ''撤回了一条成员消息
                        val member = MSIM.getInstance().channelMembersManager.getMember(
                            mMsg.channelID,
                            mMsg.channelType,
                            mMsg.remoteExtra.revoker
                        )
                        if (member != null) showName =
                            if (TextUtils.isEmpty(member.memberRemark)) member.memberName else member.memberRemark
                        if (TextUtils.isEmpty(showName)) {
                            val mChannel = MSIM.getInstance().channelManager.getChannel(
                                mMsg.remoteExtra.revoker,
                                MSChannelType.PERSONAL
                            )
                            if (mChannel != null) {
                                showName =
                                    if (TextUtils.isEmpty(mChannel.channelRemark)) mChannel.channelName else mChannel.channelRemark
                            }
                        }
                        content = String.format(
                            MSBaseApplication.getInstance().context.getString(R.string.manager_revoke_user_msg1),
                            showName
                        )
                    }
                }
            } else {
                if (mMsg.fromUID != MSConfig.getInstance().uid) {
                    val mChannel = mMsg.from
                    var showName: String? = ""
                    if (mChannel != null) {
                        showName = mChannel.channelRemark
                        if (TextUtils.isEmpty(showName)) showName = mChannel.channelName
                    } else {
                        val member = mMsg.memberOfFrom
                        if (member != null) showName =
                            if (TextUtils.isEmpty(member.memberRemark)) member.memberName else member.memberRemark
                    }
                    content = String.format(
                        MSBaseApplication.getInstance().context.getString(R.string.user_revoke_msg),
                        showName
                    )
                } else {
                    content =
                        MSBaseApplication.getInstance().context.getString(R.string.my_revoke_msg)
                }
            }
            return content
        }
    }

}