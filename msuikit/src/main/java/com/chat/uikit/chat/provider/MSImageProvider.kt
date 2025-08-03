package com.chat.uikit.chat.provider

import android.app.Activity
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.alibaba.fastjson.JSONObject
import com.chat.base.config.MSApiConfig
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.EndpointSID
import com.chat.base.endpoint.entity.ChatChooseContacts
import com.chat.base.endpoint.entity.ChooseChatMenu
import com.chat.base.glide.GlideUtils
import com.chat.base.msg.ChatAdapter
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSMsgBgType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.base.net.ud.MSProgressManager
import com.chat.base.ui.Theme
import com.chat.base.ui.components.FilterImageView
import com.chat.base.ui.components.SecretDeleteTimer
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.ImageUtils
import com.chat.base.utils.LayoutHelper
import com.chat.base.utils.MSDialogUtils
import com.chat.base.utils.MSDialogUtils.IImagePopupListener
import com.chat.base.utils.MSTimeUtils
import com.chat.base.utils.MSToastUtils
import com.chat.base.views.CircularProgressView
import com.chat.base.views.CustomImageViewerPopup.IImgPopupMenu
import com.chat.base.views.blurview.ShapeBlurView
import com.chat.uikit.R
import com.google.android.material.snackbar.Snackbar
import com.mushanyux.mushanim.MSIM
import com.mushanyux.mushanim.entity.MSCMD
import com.mushanyux.mushanim.entity.MSCMDKeys
import com.mushanyux.mushanim.entity.MSChannel
import com.mushanyux.mushanim.entity.MSMsg
import com.mushanyux.mushanim.message.type.MSMsgContentType
import com.mushanyux.mushanim.msgmodel.MSImageContent
import java.io.File
import java.util.Objects

class MSImageProvider : MSChatBaseProvider() {
    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return LayoutInflater.from(context).inflate(R.layout.chat_item_img, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val contentLayout = parentView.findViewById<LinearLayout>(R.id.contentLayout)
        val imgMsgModel = uiChatMsgItemEntity.msMsg.baseContentMsgModel as MSImageContent
        val imageView = parentView.findViewById<FilterImageView>(R.id.imageView)
        val blurView = parentView.findViewById<ShapeBlurView>(R.id.blurView)
        setCorners(from, uiChatMsgItemEntity, imageView, blurView)
        val progressTv = parentView.findViewById<TextView>(R.id.progressTv)
        val progressView = parentView.findViewById<CircularProgressView>(R.id.progressView)
        progressView.setProgColor(Theme.colorAccount)
        val imageLayout = parentView.findViewById<View>(R.id.imageLayout)
        val otherLayout = parentView.findViewById<FrameLayout>(R.id.otherLayout)
        val deleteTimer = SecretDeleteTimer(context)

        otherLayout.removeAllViews()
        otherLayout.addView(deleteTimer, LayoutHelper.createFrame(35, 35, Gravity.CENTER))
        contentLayout.gravity =
            if (from == MSChatIteMsgFromType.RECEIVED) Gravity.START else Gravity.END
        val layoutParams = imageView.layoutParams as FrameLayout.LayoutParams
        val blurViewLayoutParams = blurView.layoutParams as FrameLayout.LayoutParams
        val ints = ImageUtils.getInstance()
            .getImageWidthAndHeightToTalk(imgMsgModel.width, imgMsgModel.height)
        blurView.visibility = if (uiChatMsgItemEntity.msMsg.flame == 1) View.VISIBLE else View.GONE
        if (uiChatMsgItemEntity.msMsg.flame == 1) {
            otherLayout.visibility = View.VISIBLE
            deleteTimer.setSize(35)
            if (uiChatMsgItemEntity.msMsg.viewedAt > 0 && uiChatMsgItemEntity.msMsg.flameSecond > 0) {
                deleteTimer.setDestroyTime(
                    uiChatMsgItemEntity.msMsg.clientMsgNO,
                    uiChatMsgItemEntity.msMsg.flameSecond,
                    uiChatMsgItemEntity.msMsg.viewedAt,
                    false
                )
            }
        } else {
            otherLayout.visibility = View.GONE
        }
        val showUrl = getShowURL(uiChatMsgItemEntity)
        GlideUtils.getInstance().showImg(context, showUrl, ints[0], ints[1], imageView)

        val layoutParams1 = imageLayout.layoutParams as LinearLayout.LayoutParams
        if (uiChatMsgItemEntity.msMsg.flame == 1) {
            layoutParams.height = AndroidUtilities.dp(150f)
            layoutParams.width = AndroidUtilities.dp(150f)
            blurViewLayoutParams.height = AndroidUtilities.dp(150f)
            blurViewLayoutParams.width = AndroidUtilities.dp(150f)
            layoutParams1.height = AndroidUtilities.dp(150f)
            layoutParams1.width = AndroidUtilities.dp(150f)
        } else {
            layoutParams.height = ints[1]
            layoutParams.width = ints[0]
            blurViewLayoutParams.height = ints[1]
            blurViewLayoutParams.width = ints[0]
            layoutParams1.height = ints[1]
            layoutParams1.width = ints[0]
        }
        imageView.layoutParams = layoutParams
        blurView.layoutParams = blurViewLayoutParams
        imageLayout.layoutParams = layoutParams1

        //设置上传进度
        if (TextUtils.isEmpty(imgMsgModel.url)) {
            MSProgressManager.instance.registerProgress(uiChatMsgItemEntity.msMsg.clientSeq,
                object : MSProgressManager.IProgress {
                    override fun onProgress(tag: Any?, progress: Int) {

                        if (tag is Long) {
                            if (tag == uiChatMsgItemEntity.msMsg.clientSeq) {
                                progressView.progress = progress
                                progressTv.text =
                                    String.format("%s%%", progress)
                                if (progress >= 100) {
                                    progressTv.visibility = View.GONE
                                    progressView.visibility = View.GONE
                                    deleteTimer.visibility = View.VISIBLE
                                } else {
                                    progressView.visibility = View.VISIBLE
                                    progressTv.visibility = View.VISIBLE
                                    deleteTimer.visibility = View.GONE
                                }
                            }
                        }

                    }

                    override fun onSuccess(tag: Any?, path: String?) {
                        progressTv.visibility = View.GONE
                        progressView.visibility = View.GONE
                        deleteTimer.visibility = View.VISIBLE
                        if (tag != null) {
                            MSProgressManager.instance.unregisterProgress(tag)
                        }
                    }

                    override fun onFail(tag: Any?, msg: String?) {
                    }

                })
        }
        addLongClick(imageView, uiChatMsgItemEntity)
        imageView.setOnClickListener {
            onImageClick(
                uiChatMsgItemEntity,
                adapterPosition,
                imageView,
                getShowURL(uiChatMsgItemEntity)
            )
        }
    }

    override val itemViewType: Int
        get() = MSMsgContentType.MS_IMAGE


    //查看大图
    private fun showImages(mMsg: MSMsg, uri: String, imageView: ImageView) {
        val flame = mMsg.flame
        val list: List<MSUIChatMsgItemEntity> = getAdapter()!!.data
        val imgList: MutableList<ImageView?> = ArrayList()
        val showImgList: MutableList<MSMsg> = ArrayList()
        val tempImgList: MutableList<Any?> = ArrayList()
        if (flame == 1) {
            tempImgList.add(uri)
            imgList.add(imageView)
        } else
            run {
                var i = 0
                val size = list.size
                while (i < size) {
                    if (list[i].msMsg != null && list[i].msMsg.type == MSContentType.MS_IMAGE && list[i].msMsg.remoteExtra.revoke == 0 && list[i].msMsg.isDeleted == 0 && list[i].msMsg.flame == 0
                    ) {
                        val showUrl: String = getShowURL(list[i])
                        showImgList.add(list[i].msMsg)
                        val itemView =
                            getAdapter()!!.recyclerView.layoutManager!!.findViewByPosition(i)
                        if (itemView != null) {
                            val imageView1 =
                                itemView.findViewById<ImageView>(R.id.imageView)
                            imgList.add(imageView1)
                        } else imgList.add(null)
                        if (!TextUtils.isEmpty(showUrl)) {
                            tempImgList.add(showUrl)
                        }
                    }
                    i++
                }
            }

        if (tempImgList.isEmpty()) return
        var index = 0
        for (i in tempImgList.indices) {
            if (!TextUtils.isEmpty(uri) && tempImgList[i] != null && tempImgList[i] == uri) {
                index = i
                break
            }
        }
        imageView.tag = flame
        val popupView = MSDialogUtils.getInstance().showImagePopup(
            context,
            mMsg,
            tempImgList,
            imgList,
            imageView,
            index,
            null,
            object : IImgPopupMenu {
                override fun onForward(position: Int) {
                    val mMessageContent = showImgList[position].baseContentMsgModel
                    EndpointManager.getInstance().invoke(
                        EndpointSID.showChooseChatView,
                        ChooseChatMenu(
                            ChatChooseContacts { list1: List<MSChannel>? ->
                                if (!list1.isNullOrEmpty()) {
                                    for (mChannel in list1) {
                                        MSIM.getInstance().msgManager.send(
                                            mMessageContent,
                                            mChannel
                                        )
                                    }
                                    val viewGroup =
                                        (context as Activity).findViewById<View>(android.R.id.content)
                                            .rootView as ViewGroup
                                    Snackbar.make(
                                        viewGroup,
                                        context.getString(R.string.is_forward),
                                        1000
                                    )
                                        .setAction(
                                            ""
                                        ) { }
                                        .show()
                                }
                            },
                            mMessageContent
                        )
                    )
                }

                override fun onFavorite(position: Int) {
                    collect(showImgList[position])
                }

                override fun onShowInChat(position: Int) {
                    (Objects.requireNonNull(getAdapter()) as ChatAdapter).showTipsMsg(
                        showImgList[position].clientMsgNO
                    )
                }
            },
            object : IImagePopupListener {
                override fun onShow() {
                    val adapter = getAdapter() as ChatAdapter
                    adapter.conversationContext.onViewPicture(true)
                }

                override fun onDismiss() {
                    val adapter = getAdapter() as ChatAdapter
                    adapter.conversationContext.onViewPicture(false)
                    MSIM.getInstance().msgManager.removeRefreshMsgListener("show_chat_img")
                    MSIM.getInstance().cmdManager.removeCmdListener("show_chat_img")
                }
            })
        MSIM.getInstance().cmdManager.addCmdListener(
            "show_chat_img"
        ) { cmd: MSCMD ->
            if (!TextUtils.isEmpty(cmd.cmdKey)) {
                if (cmd.cmdKey == MSCMDKeys.ms_messageRevoke) {
                    if (cmd.paramJsonObject != null && cmd.paramJsonObject.has("message_id")) {
                        val msgID = cmd.paramJsonObject.optString("message_id")
                        val mMsg1 =
                            MSIM.getInstance().msgManager.getWithMessageID(msgID)
                        if (mMsg1 != null) {
                            for (msg in showImgList) {
                                if (msg.clientMsgNO == mMsg1.clientMsgNO && popupView != null && popupView.isShow) {
                                    MSToastUtils.getInstance()
                                        .showToast(context.getString(R.string.msg_revoked))
                                    popupView.dismiss()
                                    break
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun collect(mMsg: MSMsg) {
        val jsonObject = JSONObject()
        val mImageContent = mMsg.baseContentMsgModel as MSImageContent
        jsonObject["content"] = MSApiConfig.getShowUrl(mImageContent.url)
        jsonObject["width"] = mImageContent.width
        jsonObject["height"] = mImageContent.height
        val hashMap = HashMap<String, Any>()
        hashMap["type"] = mMsg.type
        var uniqueKey = mMsg.messageID
        if (TextUtils.isEmpty(uniqueKey)) uniqueKey = mMsg.clientMsgNO
        hashMap["unique_key"] = uniqueKey
        if (mMsg.from != null) {
            hashMap["author_uid"] = mMsg.from.channelID
            hashMap["author_name"] = mMsg.from.channelName
        }
        hashMap["payload"] = jsonObject
        hashMap["activity"] = context
        EndpointManager.getInstance().invoke("favorite_add", hashMap)
    }

    private fun onImageClick(
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        adapterPosition: Int,
        imageView: ImageView,
        tempShowImgUrl: String
    ) {
        if (uiChatMsgItemEntity.msMsg.flame == 1 && uiChatMsgItemEntity.msMsg.viewed == 0) {
            for (i in 0 until getAdapter()!!.data.size) {
                if (getAdapter()!!.data[i].msMsg.clientMsgNO.equals(uiChatMsgItemEntity.msMsg.clientMsgNO)) {
                    getAdapter()!!.data[i].msMsg.viewed = 1
                    getAdapter()!!.data[i].msMsg.viewedAt =
                        MSTimeUtils.getInstance().currentMills
                    getAdapter()!!.notifyItemChanged(adapterPosition)
                    uiChatMsgItemEntity.msMsg.viewedAt = getAdapter()!!.data[i].msMsg.viewedAt
                    MSIM.getInstance().msgManager.updateViewedAt(
                        1,
                        getAdapter()!!.data[i].msMsg.viewedAt,
                        getAdapter()!!.data[i].msMsg.clientMsgNO
                    )
                    break
                }
            }

        }
        showImages(
            uiChatMsgItemEntity.msMsg,
            tempShowImgUrl,
            imageView
        )

    }

    private fun getShowURL(uiChatMsgItemEntity: MSUIChatMsgItemEntity): String {
        val imgMsgModel = uiChatMsgItemEntity.msMsg.baseContentMsgModel as MSImageContent
        if (!TextUtils.isEmpty(imgMsgModel.localPath)) {
            val file = File(imgMsgModel.localPath)
            if (file.exists() && file.length() > 0L) {
                return file.absolutePath
            }
        }
        if (!TextUtils.isEmpty(imgMsgModel.url)) {
            return MSApiConfig.getShowUrl(imgMsgModel.url)
        }
        return ""
    }

    override fun resetCellListener(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.resetCellListener(position, parentView, uiChatMsgItemEntity, from)
        val imageView = parentView.findViewById<FilterImageView>(R.id.imageView)
        addLongClick(imageView, uiChatMsgItemEntity)
    }

    override fun resetCellBackground(
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val imageView = parentView.findViewById<FilterImageView>(R.id.imageView)
        val blurView = parentView.findViewById<ShapeBlurView>(R.id.blurView)
        if (imageView != null && blurView != null) {
            setCorners(from, uiChatMsgItemEntity, imageView, blurView)
        }
    }

    private fun setCorners(
        from: MSChatIteMsgFromType,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        imageView: FilterImageView,
        blurView: ShapeBlurView
    ) {
        imageView.strokeWidth = 0f
        val bgType = getMsgBgType(
            uiChatMsgItemEntity.previousMsg,
            uiChatMsgItemEntity.msMsg,
            uiChatMsgItemEntity.nextMsg
        )
        if (bgType == MSMsgBgType.center) {
            if (from == MSChatIteMsgFromType.SEND) {
                imageView.setCorners(10, 5, 10, 5)
                blurView.setCornerRadius(
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(5f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(5f).toFloat()
                )
            } else {
                imageView.setCorners(5, 10, 5, 10)
                blurView.setCornerRadius(
                    AndroidUtilities.dp(5f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(5f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat()
                )
            }
        } else if (bgType == MSMsgBgType.top) {
            if (from == MSChatIteMsgFromType.SEND) {
                imageView.setCorners(10, 10, 10, 5)
                blurView.setCornerRadius(
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(5f).toFloat()
                )
            } else {
                imageView.setCorners(10, 10, 5, 10)
                blurView.setCornerRadius(
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(5f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat()
                )
            }
        } else if (bgType == MSMsgBgType.bottom) {
            if (from == MSChatIteMsgFromType.SEND) {
                imageView.setCorners(10, 5, 10, 10)
                blurView.setCornerRadius(
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(5f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat()
                )
            } else {
                imageView.setCorners(5, 10, 10, 10)
                blurView.setCornerRadius(
                    AndroidUtilities.dp(5f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat(),
                    AndroidUtilities.dp(10f).toFloat()
                )
            }
        } else {
            imageView.setAllCorners(10)
            blurView.setCornerRadius(
                AndroidUtilities.dp(10f).toFloat(),
                AndroidUtilities.dp(10f).toFloat(),
                AndroidUtilities.dp(10f).toFloat(),
                AndroidUtilities.dp(10f).toFloat()
            )
        }
    }
}