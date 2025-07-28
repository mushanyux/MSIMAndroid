package com.chat.base.msgitem

import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.chat.base.R
import com.chat.base.msg.ChatAdapter
import com.chat.base.ui.components.SystemMsgBackgroundColorSpan
import com.chat.base.utils.AndroidUtilities

class MSSystemProvider(val type: Int) : MSChatBaseProvider() {
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
        get() = type

    override val layoutId: Int
        get() = R.layout.chat_system_layout

    override fun convert(
        helper: BaseViewHolder,
        item: MSUIChatMsgItemEntity
    ) {
        super.convert(helper, item)
        helper.getView<View>(R.id.systemRootView).setOnClickListener {
            val chatAdapter = getAdapter() as ChatAdapter
            chatAdapter.conversationContext.hideSoftKeyboard()
        }
        val textView = helper.getView<TextView>(R.id.contentTv)
        val content: String? = if (type == MSContentType.msgPromptTime) item.msMsg.content else {
            getShowContent(item.msMsg.content)
        }
        textView.setShadowLayer(AndroidUtilities.dp(5f).toFloat(), 0f, 0f, 0)
        val str = SpannableString(content)
        str.setSpan(
            SystemMsgBackgroundColorSpan(
                ContextCompat.getColor(
                    context,
                    R.color.colorSystemBg
                ), AndroidUtilities.dp(5f), AndroidUtilities.dp((2 * 5).toFloat())
            ), 0, content!!.length, 0
        )
        textView.text = str
    }
}