package com.chat.uikit.chat.provider

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.msg.ChatAdapter
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.base.net.HttpResponseCode
import com.chat.base.ui.Theme
import com.chat.base.utils.MSDialogUtils
import com.chat.base.utils.MSToastUtils
import com.chat.base.views.WordToSpan
import com.chat.uikit.R
import com.chat.uikit.contacts.service.FriendModel
import com.mushanyux.mushanim.MSIM
import java.util.*

class MSNoRelationProvider : MSChatBaseProvider() {
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

    override val itemViewType: Int
        get() = MSContentType.noRelation

    override val layoutId: Int
        get() = R.layout.chat_item_no_relation_layout

    override fun convert(
        helper: BaseViewHolder,
        item: MSUIChatMsgItemEntity
    ) {
        super.convert(helper, item)
        var showName = ""
        val mChannel = MSIM.getInstance().channelManager.getChannel(
            item.msMsg.channelID,
            item.msMsg.channelType
        )
        if (mChannel != null) {
            showName =
                if (TextUtils.isEmpty(mChannel.channelRemark)) mChannel.channelName else mChannel.channelRemark
        }
        val content = String.format(context.getString(R.string.no_relation_request), showName)
        helper.setText(R.id.contentTv, content)
        val link = WordToSpan()
        link.setColorCUSTOM(Theme.colorAccount)
            .setUnderlineURL(true).setRegexCUSTOM(context.getString(R.string.send_request))
            .setLink(content)
            .into(helper.getView(R.id.contentTv))
            .setClickListener { _: String?, _: String? ->
                (Objects.requireNonNull(
                    getAdapter()
                ) as ChatAdapter).conversationContext.hideSoftKeyboard()
                MSDialogUtils.getInstance().showInputDialog(
                    context,
                    context.getString(R.string.apply),
                    context.getString(R.string.input_remark),
                    "",
                    "",
                    20
                ) { text ->
                    FriendModel.getInstance()
                        .applyAddFriend(
                            item.msMsg.channelID, "", text
                        ) { code: Int, msg: String? ->
                            if (code == HttpResponseCode.success.toInt()) {
                                MSToastUtils.getInstance()
                                    .showToastNormal(context.getString(R.string.applyed))
                            } else {
                                MSToastUtils.getInstance().showToastNormal(msg)
                            }
                        }
                }
            }
    }

}