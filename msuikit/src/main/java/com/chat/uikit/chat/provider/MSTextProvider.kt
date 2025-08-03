package com.chat.uikit.chat.provider

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.emoji2.widget.EmojiTextView
import com.chat.base.MSBaseApplication
import com.chat.base.act.MSWebViewActivity
import com.chat.base.config.MSApiConfig
import com.chat.base.emoji.EmojiManager
import com.chat.base.emoji.MoonUtil
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.EndpointSID
import com.chat.base.endpoint.entity.CanReactionMenu
import com.chat.base.endpoint.entity.ChatChooseContacts
import com.chat.base.endpoint.entity.ChatItemPopupMenu
import com.chat.base.endpoint.entity.ChooseChatMenu
import com.chat.base.endpoint.entity.MsgConfig
import com.chat.base.entity.BottomSheetItem
import com.chat.base.glide.GlideUtils
import com.chat.base.msg.ChatAdapter
import com.chat.base.msg.model.MSGifContent
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.base.ui.components.AlignImageSpan
import com.chat.base.ui.components.AvatarView
import com.chat.base.ui.components.NormalClickableContent
import com.chat.base.ui.components.NormalClickableSpan
import com.chat.base.utils.LayoutHelper
import com.chat.base.utils.SoftKeyboardUtils
import com.chat.base.utils.StringUtils
import com.chat.base.utils.MSDialogUtils
import com.chat.base.utils.MSPermissions
import com.chat.base.utils.MSPermissions.IPermissionResult
import com.chat.base.utils.MSToastUtils
import com.chat.base.views.BubbleLayout
import com.chat.uikit.R
import com.chat.uikit.user.UserDetailActivity
import com.google.android.material.snackbar.Snackbar
import com.mushanyux.mushanim.MSIM
import com.mushanyux.mushanim.entity.MSChannel
import com.mushanyux.mushanim.entity.MSChannelType
import com.mushanyux.mushanim.entity.MSMsg
import com.mushanyux.mushanim.entity.MSMsgSetting
import com.mushanyux.mushanim.entity.MSSendOptions
import com.mushanyux.mushanim.msgmodel.MSImageContent
import com.mushanyux.mushanim.msgmodel.MSTextContent
import java.io.File
import java.util.Objects
import kotlin.math.abs


open class MSTextProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return LayoutInflater.from(context).inflate(R.layout.chat_item_text, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val contentTv = parentView.findViewById<EmojiTextView>(R.id.contentTv)
        val receivedTextNameTv = parentView.findViewById<TextView>(R.id.receivedTextNameTv)
        val contentTvLayout = parentView.findViewById<BubbleLayout>(R.id.contentTvLayout)
        val contentLayout = parentView.findViewById<LinearLayout>(R.id.contentLayout)
        resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val textColor: Int
        if (from == MSChatIteMsgFromType.SEND) {
            contentTv.setBackgroundResource(R.drawable.send_chat_text_bg)
            contentLayout.gravity = Gravity.END
            receivedTextNameTv.visibility = View.GONE
            textColor = ContextCompat.getColor(context, R.color.colorDark)
        } else {
            contentTv.setBackgroundResource(R.drawable.received_chat_text_bg)
            setFromName(uiChatMsgItemEntity, from, receivedTextNameTv)
            contentLayout.gravity = Gravity.START
            textColor = ContextCompat.getColor(context, R.color.receive_text_color)
        }
        contentTv.setTextColor(textColor)
        contentTv.text = uiChatMsgItemEntity.displaySpans
        contentTv.movementMethod = LinkMovementMethod.getInstance()
        selectText(contentTv, contentTvLayout, uiChatMsgItemEntity)
        if (uiChatMsgItemEntity.msMsg.baseContentMsgModel.reply != null && uiChatMsgItemEntity.msMsg.baseContentMsgModel.reply.payload != null) {
            replyView(contentTvLayout, from, uiChatMsgItemEntity)
        }
    }

    private var mSelectableTextHelper: SelectTextHelper? = null
    var selectText: String? = null
    private fun selectText(
        textView: TextView,
        fullLayout: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity
    ) {
//        textMsgBean = msgBean
        val menu = EndpointManager.getInstance()
            .invoke("favorite_item", uiChatMsgItemEntity.msMsg)
        var favoritePopupMenu: ChatItemPopupMenu? = null
        if (menu != null) {
            favoritePopupMenu = menu as ChatItemPopupMenu
        }

        val builder = SelectTextHelper.Builder(textView, fullLayout) // 放你的textView到这里！！
            .setCursorHandleColor(ContextCompat.getColor(context, R.color.colorAccent)) // 游标颜色
            .setCursorHandleSizeInDp(22f) // 游标大小 单位dp
            .setSelectedColor(
                ContextCompat.getColor(
                    context,
                    R.color.color_text_select_cursor
                )
            ) // 选中文本的颜色
            .setSelectAll(true) // 初次选中是否全选 default true
            .setScrollShow(false) // 滚动时是否继续显示 default true
            .setSelectedAllNoPop(true) // 已经全选无弹窗，设置了监听会回调 onSelectAllShowCustomPop 方法
            .setMagnifierShow(true) // 放大镜 default true
            .setSelectTextLength(2)// 首次选中文本的长度 default 2
            .setPopDelay(100)// 弹窗延迟时间 default 100毫秒
            .setFlame(uiChatMsgItemEntity.msMsg.flame)
            .setIsShowPinnedMessage(if (uiChatMsgItemEntity.isShowPinnedMessage) 1 else 0)
            .addItem(R.mipmap.msg_copy,
                R.string.copy,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        EndpointManager.getInstance().invoke("chat_activity_touch", null)
                        // mSelectableTextHelper?.reset()
                        val cm =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val mClipData = ClipData.newPlainText("Label", selectText)
                        cm.setPrimaryClip(mClipData)
                        MSToastUtils.getInstance()
                            .showToastNormal(context.getString(R.string.copyed))
                    }
                }).addItem(
                R.mipmap.msg_forward,
                R.string.base_forward,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        EndpointManager.getInstance().invoke("chat_activity_touch", null)
                        if (TextUtils.isEmpty(selectText)) return
                        val textContent = MSTextContent(selectText)
                        val chooseChatMenu =
                            ChooseChatMenu(
                                ChatChooseContacts { channelList: List<MSChannel>? ->
                                    if (!channelList.isNullOrEmpty()) {
                                        for (mChannel in channelList) {
                                            textContent.mentionAll = 0
                                            textContent.mentionInfo = null
                                            val option = MSSendOptions()
                                            option.setting.receipt = mChannel.receipt
                                            MSIM.getInstance().msgManager.sendWithOptions(
                                                textContent,
                                                mChannel, option
                                            )
                                        }
                                        val viewGroup =
                                            (context as Activity).findViewById<View>(android.R.id.content)
                                                .rootView as ViewGroup
                                        Snackbar.make(
                                            viewGroup,
                                            context.getString(com.chat.base.R.string.str_forward),
                                            1000
                                        )
                                            .setAction(
                                                ""
                                            ) { }
                                            .show()
                                    }
                                },
                                textContent
                            )
                        EndpointManager.getInstance()
                            .invoke(EndpointSID.showChooseChatView, chooseChatMenu)
                    }

                }).setPopSpanCount(3) // 设置操作弹窗每行个数 default 5
        mSelectableTextHelper = builder.build()
//            .setPopStyle(
//                R.drawable.shape_color_4c4c4c_radius_8 /*操作弹窗背*/, R.mipmap.ic_arrow /*箭头图片*/
//            ) // 设置操作弹窗背景色、箭头图片
        if (favoritePopupMenu != null) {
            builder.addItem(
                favoritePopupMenu.imageResource,
                favoritePopupMenu.text,
                object : SelectTextHelper.Builder.onSeparateItemClickListener {
                    override fun onClick() {
                        EndpointManager.getInstance().invoke("chat_activity_touch", null)

                        if (!TextUtils.isEmpty(selectText)) {
                            val mMsg = MSMsg()
                            mMsg.type = MSContentType.MS_TEXT
                            mMsg.baseContentMsgModel = MSTextContent(selectText)
                            mMsg.from = uiChatMsgItemEntity.msMsg.from
                            mMsg.channelID = uiChatMsgItemEntity.msMsg.channelID
                            mMsg.channelType = uiChatMsgItemEntity.msMsg.channelType
                            if (uiChatMsgItemEntity.msMsg.remoteExtra != null && uiChatMsgItemEntity.msMsg.remoteExtra.contentEditMsgModel != null) {
                                mMsg.remoteExtra.contentEditMsgModel = MSTextContent(selectText)
                            }
                            val chatAdapter = getAdapter() as ChatAdapter
                            uiChatMsgItemEntity.msMsg.baseContentMsgModel.content = selectText
                            favoritePopupMenu.iPopupItemClick.onClick(
                                mMsg,
                                chatAdapter.conversationContext
                            )
                        }
                    }
                })
        }

        mSelectableTextHelper!!.setSelectListener(object : SelectTextHelper.OnSelectListener {
            override fun onClick(v: View?, originalContent: String?) {
            }


            /**
             * 长按回调
             */
            override fun onLongClick(v: View, local: FloatArray) {
                // showPopup(messageContent,textView,local)
            }

            override fun onTextSelected(content: String?) {
                selectText = content
            }


            /**
             * 弹窗关闭回调
             */
            override fun onDismiss() {}
            override fun onClickLink(clickableContent: NormalClickableSpan) {
                if (clickableContent.clickableContent.type == NormalClickableContent.NormalClickableTypes.URL) {
                    val intent = Intent(
                        context, MSWebViewActivity::class.java
                    )
                    intent.putExtra("url", clickableContent.clickableContent.content)
                    context.startActivity(intent)
                } else if (clickableContent.clickableContent.type == NormalClickableContent.NormalClickableTypes.Remind) {
                    val uid: String
                    var groupNo = ""
                    if (clickableContent.clickableContent.content.contains("|")) {
                        uid = clickableContent.clickableContent.content.split("|")[0]
                        groupNo = clickableContent.clickableContent.content.split("|")[1]
                    } else {
                        uid = clickableContent.clickableContent.content
                    }
                    val intent = Intent(context, UserDetailActivity::class.java)
                    intent.putExtra("uid", uid)
                    if (!TextUtils.isEmpty(groupNo)) intent.putExtra("groupID", groupNo)
                    context.startActivity(intent)
                } else {
                    val content = clickableContent.clickableContent.content
                    if (StringUtils.isMobile(content)) {
                        val chatAdapter = getAdapter() as ChatAdapter
                        chatAdapter.hideSoftKeyboard()
                        val list = ArrayList<BottomSheetItem>()
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.copy),
                                R.mipmap.msg_copy,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        val cm =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val mClipData = ClipData.newPlainText("Label", content)
                                        cm.setPrimaryClip(mClipData)
                                        MSToastUtils.getInstance()
                                            .showToastNormal(context.getString(R.string.copyed))
                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.call),
                                R.mipmap.msg_calls,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        val desc = String.format(
                                            context.getString(R.string.call_phone_permissions_desc),
                                            context.getString(R.string.app_name)
                                        );
                                        MSPermissions.getInstance().checkPermissions(
                                            object : IPermissionResult {
                                                override fun onResult(result: Boolean) {
                                                    if (result) {
                                                        val intent =
                                                            Intent(
                                                                Intent.ACTION_CALL,
                                                                Uri.parse("tel:$content")
                                                            )
                                                        context.startActivity(intent)
                                                    }
                                                }

                                                override fun clickResult(isCancel: Boolean) {

                                                }
                                            },
                                            chatAdapter.conversationContext.chatActivity,
                                            desc,
                                            Manifest.permission.CALL_PHONE
                                        )

                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.add_to_phone_book),
                                R.mipmap.msg_contacts,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {

                                        val addIntent = Intent(
                                            Intent.ACTION_INSERT,
                                            Uri.withAppendedPath(
                                                Uri.parse("content://com.android.contacts"),
                                                "contacts"
                                            )
                                        )
                                        addIntent.type = "vnd.android.cursor.dir/person"
                                        addIntent.type = "vnd.android.cursor.dir/contact"
                                        addIntent.type = "vnd.android.cursor.dir/raw_contact"
                                        addIntent.putExtra(
                                            ContactsContract.Intents.Insert.NAME,
                                            ""
                                        )
                                        addIntent.putExtra(
                                            ContactsContract.Intents.Insert.PHONE,
                                            content
                                        )
                                        context.startActivity(addIntent)

                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.str_search),
                                R.mipmap.ic_ab_search,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        if (uiChatMsgItemEntity.iLinkClick != null)
                                            uiChatMsgItemEntity.iLinkClick.onShowSearchUser(
                                                content
                                            )
                                    }
                                })
                        )
                        val displaySpans = SpannableStringBuilder()
                        displaySpans.append(content)
                        displaySpans.setSpan(
                            StyleSpan(Typeface.BOLD), 0,
                            content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        displaySpans.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), 0,
                            content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        MSDialogUtils.getInstance()
                            .showBottomSheet(context, displaySpans, false, list)
                        return
                    }
                    if (StringUtils.isEmail(content)) {
                        val displaySpans = SpannableStringBuilder()
                        displaySpans.append(content)
                        displaySpans.setSpan(
                            StyleSpan(Typeface.BOLD), 0,
                            content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        displaySpans.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context, R.color.blue)), 0,
                            content.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val list = ArrayList<BottomSheetItem>()
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.copy),
                                R.mipmap.msg_copy,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        val cm =
                                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val mClipData = ClipData.newPlainText("Label", content)
                                        cm.setPrimaryClip(mClipData)
                                        MSToastUtils.getInstance()
                                            .showToastNormal(context.getString(R.string.copyed))
                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.send_email),
                                R.mipmap.msg2_email,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        val uri = Uri.parse("mailto:$content")
                                        val email = arrayOf(content)
                                        val intent = Intent(Intent.ACTION_SENDTO, uri)
                                        intent.putExtra(Intent.EXTRA_CC, email) // 抄送人
                                        intent.putExtra(Intent.EXTRA_SUBJECT, "") // 主题
                                        intent.putExtra(Intent.EXTRA_TEXT, "") // 正文
                                        context.startActivity(Intent.createChooser(intent, ""))
                                    }
                                })
                        )
                        list.add(
                            BottomSheetItem(
                                context.getString(R.string.str_search),
                                R.mipmap.ic_ab_search,
                                object : BottomSheetItem.IBottomSheetClick {
                                    override fun onClick() {
                                        if (uiChatMsgItemEntity.iLinkClick != null)
                                            uiChatMsgItemEntity.iLinkClick.onShowSearchUser(
                                                content
                                            )
                                        // if (iLinkClick != null) iLinkClick.onShowSearchUser(content)
                                    }
                                })
                        )
                        MSDialogUtils.getInstance()
                            .showBottomSheet(context, displaySpans, false, list)
                        return
                    }
                }
            }


            /**
             * 全选显示自定义弹窗回调
             */
            override fun onSelectAllShowCustomPop(local: FloatArray) {
                showPopup(uiChatMsgItemEntity, textView, local)
            }

            /**
             * 重置回调
             */
            override fun onReset() {
            }

            /**
             * 解除自定义弹窗回调
             */
            override fun onDismissCustomPop() {
            }

            /**
             * 是否正在滚动回调
             */
            override fun onScrolling() {
            }
        })


    }

    private fun showPopup(uiChatMsgItemEntity: MSUIChatMsgItemEntity, v: View, local: FloatArray) {
        val mMsgConfig: MsgConfig = getMsgConfig(uiChatMsgItemEntity.msMsg.type)
        var isShowReaction = false
        val `object` = EndpointManager.getInstance()
            .invoke(
                "is_show_reaction",
                CanReactionMenu(uiChatMsgItemEntity.msMsg, mMsgConfig)
            )
        if (`object` != null) {
            isShowReaction = `object` as Boolean
        }
        if (uiChatMsgItemEntity.msMsg.flame == 1) isShowReaction = false
        val finalIsShowReaction = isShowReaction
        showChatPopup(
            uiChatMsgItemEntity.msMsg,
            v,
            local,
            finalIsShowReaction,
            getPopupList(uiChatMsgItemEntity.msMsg)
        )
    }

    override val itemViewType: Int
        get() = MSContentType.MS_TEXT


    private fun shotTipsMsg(mTextContent: MSTextContent) {
        var clientMsgNo = mTextContent.reply.message_id
        val mMsg =
            MSIM.getInstance().msgManager.getWithMessageID(mTextContent.reply.message_id)
        if (mMsg != null) {
            clientMsgNo = mMsg.clientMsgNO
        }
        (Objects.requireNonNull(getAdapter()) as ChatAdapter).showTipsMsg(clientMsgNo)
    }

    override fun resetCellListener(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.resetCellListener(position, parentView, uiChatMsgItemEntity, from)
    }

    override fun resetCellBackground(
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val contentTvLayout = parentView.findViewById<BubbleLayout>(R.id.contentTvLayout)
        val textContentLayout = parentView.findViewById<View>(R.id.textContentLayout)
        val msgTimeView = parentView.findViewById<View>(R.id.msgTimeView)
        // 这里要指定文本宽度 - padding的距离
        if (textContentLayout == null || msgTimeView == null) {
            return
        }
        textContentLayout.layoutParams.width = getViewWidth(from, uiChatMsgItemEntity)
        val bgType = getMsgBgType(
            uiChatMsgItemEntity.previousMsg,
            uiChatMsgItemEntity.msMsg,
            uiChatMsgItemEntity.nextMsg
        )
        contentTvLayout.setAll(bgType, from, MSContentType.MS_TEXT)
        if (textContentLayout.layoutParams.width < msgTimeView.layoutParams.width) {
            textContentLayout.layoutParams.width = msgTimeView.layoutParams.width
        }
    }

    override fun resetFromName(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val receivedTextNameTv = parentView.findViewById<TextView>(R.id.receivedTextNameTv)
        setFromName(uiChatMsgItemEntity, from, receivedTextNameTv)
    }

    override fun refreshReply(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.refreshReply(adapterPosition, parentView, uiChatMsgItemEntity, from)
        val textModel = uiChatMsgItemEntity.msMsg.baseContentMsgModel as MSTextContent
        val replyContentRevokedTv = parentView.findViewWithTag<View>("replyRevokedTv")
        val replyContentLayout = parentView.findViewWithTag<View>("replyContentLayout")
        if (replyContentRevokedTv == null || replyContentLayout == null)
            return
        if (textModel.reply != null) {
            if (uiChatMsgItemEntity.msMsg.baseContentMsgModel.reply.revoke == 1) {
                replyContentRevokedTv.visibility = View.VISIBLE
                replyContentLayout.visibility = View.GONE
            } else {
                val replyIV = parentView.findViewWithTag<AppCompatImageView>("replyIV")
                val replyTV = parentView.findViewWithTag<AppCompatTextView>("replyTV")
                if (replyIV != null && replyTV != null) {
                    showReplyContent(textModel, replyIV, replyTV)
                }
            }
        }
    }

    private fun replyView(
        contentLayout: BubbleLayout,
        from: MSChatIteMsgFromType,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity
    ) {
        val replyLayout = LinearLayout(context)
        replyLayout.orientation = LinearLayout.HORIZONTAL
        replyLayout.setBackgroundResource(R.drawable.reply_bg)
        contentLayout.addView(
            replyLayout, 1,
            LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                0f,
                5f,
                0f,
                10f
            )
        )
        val lineView = View(context)
        lineView.setBackgroundResource(R.drawable.reply_line)
        replyLayout.addView(
            lineView,
            LayoutHelper.createLinear(3, LayoutHelper.MATCH_PARENT, 0f, 0f, 5f, 0f)
        )

        // revoke
        val replyContentRevokedTv = AppCompatTextView(context)
        replyLayout.addView(
            replyContentRevokedTv,
            LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                0f,
                10f,
                10f,
                10f
            )
        )
        replyContentRevokedTv.setTextColor(ContextCompat.getColor(context, R.color.popupTextColor))
        replyContentRevokedTv.setText(R.string.reply_msg_is_revoked)
        val size = context.resources.getDimension(R.dimen.font_size_14)
        val pSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            size,
            contentLayout.resources.displayMetrics
        )
        replyContentRevokedTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, pSize)
        // reply content layout
        val replyContentLayout = LinearLayout(context)
        replyContentLayout.orientation = LinearLayout.VERTICAL
        replyLayout.addView(
            replyContentLayout,
            LayoutHelper.createLinear(
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.WRAP_CONTENT,
                0f,
                5f,
                5f,
                5f
            )
        )

        val userLayout = LinearLayout(context)
        replyContentLayout.addView(
            userLayout,
            LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT)
        )
        userLayout.orientation = LinearLayout.HORIZONTAL
        val avatarView = AvatarView(context)
        avatarView.setSize(20f)
        val userNameTv = AppCompatTextView(context)
        val nameSize = context.resources.getDimension(R.dimen.font_size_12)
        val namePSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            nameSize,
            contentLayout.resources.displayMetrics
        )
        userNameTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, namePSize)
        userNameTv.setTextColor(ContextCompat.getColor(context, R.color.color999))
        userLayout.addView(
            avatarView,
            LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT)
        )
        userLayout.addView(
            userNameTv,
            LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                5f,
                0f,
                0f,
                0f
            )
        )
        val replyTV = AppCompatTextView(context)
        replyTV.ellipsize = TextUtils.TruncateAt.END
        replyTV.isSingleLine = true
        replyTV.setLines(1)
        replyContentLayout.addView(
            replyTV,
            LayoutHelper.createLinear(
                LayoutHelper.WRAP_CONTENT,
                LayoutHelper.WRAP_CONTENT,
                0f,
                10f,
                0f,
                0f
            )
        )
        replyTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, pSize)
        val textColor: Int = if (from == MSChatIteMsgFromType.SEND) {
            ContextCompat.getColor(context, R.color.colorDark)
        } else {
            ContextCompat.getColor(context, R.color.receive_text_color)
        }
        replyTV.setTextColor(textColor)

        val replyIV = AppCompatImageView(context)
        replyIV.scaleType = ImageView.ScaleType.CENTER
        replyContentLayout.addView(replyIV, LayoutHelper.createLinear(80, 80, 0f, 10f, 0f, 0f))

        val textModel = uiChatMsgItemEntity.msMsg.baseContentMsgModel as MSTextContent
        val mChannel = MSIM.getInstance().channelManager.getChannel(
            textModel.reply.from_uid, MSChannelType.PERSONAL
        )
        if (mChannel != null) {
            val showName =
                if (TextUtils.isEmpty(mChannel.channelRemark)) {
                    mChannel.channelName
                } else mChannel.channelRemark
            userNameTv.text = showName
            avatarView.showAvatar(mChannel)
        }
        if (!TextUtils.isEmpty(uiChatMsgItemEntity.msMsg.fromUID)) {
            val colors =
                MSBaseApplication.getInstance().context.resources.getIntArray(R.array.name_colors)
            val index = abs(textModel.reply.from_uid.hashCode()) % colors.size
            val myShapeDrawable = lineView.background as GradientDrawable
            myShapeDrawable.setColor(colors[index])
            userNameTv.setTextColor(colors[index])
            val bgColor = ColorUtils.setAlphaComponent(colors[index], 30)
            val bgShapeDrawable = replyLayout.background as GradientDrawable
            bgShapeDrawable.setColor(bgColor)
        }
        if (textModel.reply.revoke == 1) {
            replyContentLayout.visibility = View.GONE
            replyContentRevokedTv.visibility = View.VISIBLE
            return
        }
        replyContentRevokedTv.visibility = View.GONE
        showReplyContent(textModel, replyIV, replyTV)
        replyLayout.setOnClickListener {
            shotTipsMsg(
                textModel
            )
        }
        replyTV.setOnClickListener {
            shotTipsMsg(
                textModel
            )
        }

        replyContentRevokedTv.tag = "replyRevokedTv"
        replyIV.tag = "replyIV"
        replyTV.tag = "replyTV"
        replyContentLayout.tag = "replyContentLayout"
    }

    private fun showReplyContent(
        mTextContent: MSTextContent,
        replyIv: AppCompatImageView,
        replyTv: AppCompatTextView
    ) {
        when (mTextContent.reply.payload.type) {
            MSContentType.MS_GIF -> {
                replyIv.visibility = View.VISIBLE
                replyTv.visibility = View.GONE
                val gifContent = mTextContent.reply.payload as MSGifContent
                GlideUtils.getInstance()
                    .showGif(
                        context,
                        MSApiConfig.getShowUrl(gifContent.url),
                        replyIv,
                        null
                    )
            }

            MSContentType.MS_IMAGE -> {
                replyIv.visibility = View.VISIBLE
                replyTv.visibility = View.GONE
                val imageContent = mTextContent.reply.payload as MSImageContent
                var showUrl: String
                if (!TextUtils.isEmpty(imageContent.localPath)) {
                    showUrl = imageContent.localPath
                    val file = File(showUrl)
                    if (!file.exists()) {
                        //如果本地文件被删除就显示网络图片
                        showUrl = MSApiConfig.getShowUrl(imageContent.url)
                    }
                } else {
                    showUrl = MSApiConfig.getShowUrl(imageContent.url)
                }
                GlideUtils.getInstance().showImg(context, showUrl, replyIv)
            }

            else -> {
                replyIv.visibility = View.GONE
                replyTv.visibility = View.VISIBLE
                var content = mTextContent.reply.payload.displayContent
                if (mTextContent.reply.contentEditMsgModel != null && !TextUtils.isEmpty(
                        mTextContent.reply.contentEditMsgModel.displayContent
                    )
                ) {
                    content = mTextContent.reply.contentEditMsgModel.displayContent
                }
                if (TextUtils.isEmpty(content)) {
                    content = context.getString(R.string.base_unknow_msg)
                }
                replyTv.movementMethod = LinkMovementMethod.getInstance()
                val strUrls = StringUtils.getStrUrls(content)
                val replySpan = SpannableStringBuilder()
                replySpan.append(content)
                if (strUrls.isNotEmpty()) {
                    for (url in strUrls) {
                        if (TextUtils.isEmpty(url)) {
                            continue
                        }
                        var fromIndex = 0
                        while (fromIndex >= 0) {
                            fromIndex = content.indexOf(url, fromIndex)
                            if (fromIndex >= 0) {
                                replySpan.setSpan(
                                    StyleSpan(Typeface.BOLD),
                                    fromIndex,
                                    fromIndex + url.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                replySpan.setSpan(
                                    NormalClickableSpan(true,
                                        ContextCompat.getColor(context, R.color.blue),
                                        NormalClickableContent(
                                            NormalClickableContent.NormalClickableTypes.URL,
                                            url
                                        ),
                                        object : NormalClickableSpan.IClick {
                                            override fun onClick(view: View) {
                                                SoftKeyboardUtils.getInstance()
                                                    .hideSoftKeyboard(context as Activity)
                                                val intent = Intent(
                                                    context, MSWebViewActivity::class.java
                                                )
                                                intent.putExtra("url", url)
                                                context.startActivity(intent)
                                            }
                                        }),
                                    fromIndex,
                                    fromIndex + url.length,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                fromIndex += url.length
                            }
                        }
                    }
                }

                // emoji
                val matcher = EmojiManager.getInstance().pattern.matcher(content)
                while (matcher.find()) {
                    val start = matcher.start()
                    val end = matcher.end()
                    val emoji = content.substring(start, end)
                    val d = MoonUtil.getEmotDrawable(context, emoji, MoonUtil.SMALL_SCALE)
                    if (d != null) {
                        val span: AlignImageSpan =
                            object : AlignImageSpan(d, ALIGN_CENTER) {
                                override fun onClick(view: View) {}
                            }
                        replySpan.setSpan(
                            span,
                            start,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                replyTv.text = replySpan
            }
        }
    }
}