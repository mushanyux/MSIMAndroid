package com.chat.uikit.chat.provider

import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chat.base.config.MSApiConfig
import com.chat.base.config.MSConfig
import com.chat.base.config.MSConstants
import com.chat.base.msgitem.MSChatBaseProvider
import com.chat.base.msgitem.MSChatIteMsgFromType
import com.chat.base.msgitem.MSContentType
import com.chat.base.msgitem.MSUIChatMsgItemEntity
import com.chat.base.net.ud.MSProgressManager
import com.chat.base.net.ud.MSDownloader
import com.chat.base.ui.Theme
import com.chat.base.ui.components.SecretDeleteTimer
import com.chat.base.utils.AndroidUtilities
import com.chat.base.utils.MSCommonUtils
import com.chat.base.utils.MSFileUtils
import com.chat.base.utils.MSTimeUtils
import com.chat.base.utils.MSToastUtils
import com.chat.base.views.BubbleLayout
import com.chat.uikit.R
import com.chat.uikit.message.MsgModel
import com.chat.uikit.view.CircleProgress
import com.chat.uikit.view.MSPlayVoiceUtils
import com.chat.uikit.view.MSPlayVoiceUtils.IPlayListener
import com.chat.uikit.view.WaveformView
import com.mushanyux.mushanim.MSIM
import com.mushanyux.mushanim.message.type.MSMsgContentType
import com.mushanyux.mushanim.msgmodel.MSVoiceContent
import java.io.File
import kotlin.math.max

class MSVoiceProvider : MSChatBaseProvider() {
    private var lastClientMsgNo: String? = null

    override fun getChatViewItem(parentView: ViewGroup, from: MSChatIteMsgFromType): View? {
        return LayoutInflater.from(context).inflate(R.layout.chat_item_voice, parentView, false)
    }

    override fun setData(
        adapterPosition: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        val contentLayout = parentView.findViewById<LinearLayout>(R.id.contentLayout)

        val voiceTimeTv = parentView.findViewById<TextView>(R.id.voiceTimeTv)
        val voiceWaveform = parentView.findViewById<WaveformView>(R.id.voiceWaveform)
        val playBtn = parentView.findViewById<CircleProgress>(R.id.playBtn)
        playBtn.setProgressColor(Theme.colorAccount)

        resetCellBackground(parentView, uiChatMsgItemEntity, from)
        if (from == MSChatIteMsgFromType.SEND) {
            contentLayout.gravity = Gravity.END
            voiceTimeTv.setTextColor(ContextCompat.getColor(context, R.color.color999))
            playBtn.setShadowColor(ContextCompat.getColor(context, R.color.white))
        } else {
            contentLayout.gravity = Gravity.START
            voiceTimeTv.setTextColor(ContextCompat.getColor(context, R.color.color999))
            playBtn.setShadowColor(ContextCompat.getColor(context, R.color.homeColor))
        }

        playBtn.setBindId(uiChatMsgItemEntity.msMsg.clientMsgNO)
        if (MSPlayVoiceUtils.getInstance().playKey != uiChatMsgItemEntity.msMsg.clientMsgNO) {
            if (from == MSChatIteMsgFromType.SEND) {
                voiceWaveform.isFresh = false
            } else voiceWaveform.isFresh = uiChatMsgItemEntity.msMsg.voiceStatus == 0
        }
        val voiceContent = uiChatMsgItemEntity.msMsg.baseContentMsgModel as MSVoiceContent
        voiceWaveform.layoutParams.width =
            getVoiceWidth(voiceContent.timeTrad, uiChatMsgItemEntity.msMsg.flame)
        if (!TextUtils.isEmpty(voiceContent.waveform)) {
            val bytes = MSCommonUtils.getInstance().base64Decode(voiceContent.waveform)
            voiceWaveform.setWaveform(bytes)
        }
        if (TextUtils.isEmpty(voiceContent.localPath)) {
            playBtn.enableDownload()
        } else {
            if (!uiChatMsgItemEntity.isPlaying) {
                playBtn.setPlay()
            } else playBtn.setPause()
        }
        val showTime: String
        var mStr: String
        var sStr: String
        if (voiceContent.timeTrad >= 60) {
            val m = voiceContent.timeTrad / 60
            val s = voiceContent.timeTrad % 60
            mStr = m.toString()
            sStr = s.toString()
            if (m < 10) {
                mStr = "0$m"
            }
            if (s < 10) {
                sStr = "0$s"
            }
        } else {
            mStr = "00"
            sStr = if (voiceContent.timeTrad < 10) {
                "0" + voiceContent.timeTrad
            } else voiceContent.timeTrad.toString()
        }
        showTime = String.format("%s:%s", mStr, sStr)
        voiceTimeTv.text = showTime
        playBtn.setOnClickListener {
            lastClientMsgNo = uiChatMsgItemEntity.msMsg.clientMsgNO
            if (TextUtils.isEmpty(voiceContent.localPath)) {
                stopPlay()
                val fileDir =
                    MSConstants.voiceDir + uiChatMsgItemEntity.msMsg.channelType + "/" + uiChatMsgItemEntity.msMsg.channelID + "/"
                MSFileUtils.getInstance().createFileDir(fileDir)
                val filePath = fileDir + uiChatMsgItemEntity.msMsg.clientMsgNO + ".amr"
                val file = File(filePath)
                if (file.exists()) {
                    playBtn.setPlay()
                    MSPlayVoiceUtils.getInstance()
                        .playVoice(filePath, uiChatMsgItemEntity.msMsg.clientMsgNO)
                    updateViewed(uiChatMsgItemEntity, parentView, from)
                } else {
                    playBtn.enableLoading(1)
                    MSDownloader.instance.download(
                        MSApiConfig.getShowUrl(voiceContent.url),
                        filePath,
                        object : MSProgressManager.IProgress {
                            override fun onProgress(tag: Any?, progress: Int) {
                                playBtn.enableLoading(progress)
                            }

                            override fun onSuccess(tag: Any?, path: String?) {
                                if (!TextUtils.isEmpty(filePath)) {
                                    voiceContent.localPath = filePath
                                    uiChatMsgItemEntity.msMsg.voiceStatus = 1
                                    uiChatMsgItemEntity.msMsg.baseContentMsgModel = voiceContent
                                    MSIM.getInstance().msgManager.updateContentAndRefresh(
                                        uiChatMsgItemEntity.msMsg.clientMsgNO,
                                        voiceContent,
                                        false
                                    )
                                    MSIM.getInstance().msgManager.updateVoiceReadStatus(
                                        uiChatMsgItemEntity.msMsg.clientMsgNO,
                                        1,
                                        false
                                    )
                                    updateViewed(uiChatMsgItemEntity, parentView, from)
                                    MsgModel.getInstance().updateVoiceStatus(
                                        uiChatMsgItemEntity.msMsg.messageID,
                                        uiChatMsgItemEntity.msMsg.channelID,
                                        uiChatMsgItemEntity.msMsg.channelType,
                                        uiChatMsgItemEntity.msMsg.messageSeq
                                    )
                                    if (!TextUtils.isEmpty(lastClientMsgNo) && lastClientMsgNo == uiChatMsgItemEntity.msMsg.clientMsgNO) {
                                        playBtn.setPlay()
                                        //  voiceWaveform.setFresh(uiChatMsgItemEntity.msMsg.voiceStatus == 0);
                                        MSPlayVoiceUtils.getInstance()
                                            .playVoice(
                                                filePath,
                                                uiChatMsgItemEntity.msMsg.clientMsgNO
                                            )
                                    }
                                }

                            }

                            override fun onFail(tag: Any?, msg: String?) {
                                MSToastUtils.getInstance()
                                    .showToastNormal(context.getString(R.string.voice_download_fail))
                            }

                        })
                }
            } else {
                if (MSPlayVoiceUtils.getInstance().isPlaying) {
                    if (MSPlayVoiceUtils.getInstance()
                            .oldPlayKey == uiChatMsgItemEntity.msMsg.clientMsgNO
                    ) {
                        MSPlayVoiceUtils.getInstance().onPause()
                        playBtn.setPlay()
                    } else {

                        stopPlay()
                        updateViewed(uiChatMsgItemEntity, parentView, from)
                        MSPlayVoiceUtils.getInstance()
                            .playVoice(
                                voiceContent.localPath,
                                uiChatMsgItemEntity.msMsg.clientMsgNO
                            )
                    }
                } else {
                    val file = File(voiceContent.localPath)
                    if (file.exists()) {
                        updateViewed(uiChatMsgItemEntity, parentView, from)
                        MSPlayVoiceUtils.getInstance()
                            .playVoice(
                                voiceContent.localPath,
                                uiChatMsgItemEntity.msMsg.clientMsgNO
                            )
                    } else {
                        stopPlay()
                    }
                }
            }
        }
        MSPlayVoiceUtils.getInstance().setPlayListener(object : IPlayListener {
            override fun onCompletion(key: String) {
                if (key == uiChatMsgItemEntity.msMsg.clientMsgNO) {
                    voiceWaveform.setProgress(0f)
                    playBtn.setPlay()
                    uiChatMsgItemEntity.isPlaying = false
                    voiceWaveform.isFresh = false
                    playNext(key)
                }
            }

            override fun onProgress(key: String, pg: Float) {
                if (key == uiChatMsgItemEntity.msMsg.clientMsgNO) {
                    voiceWaveform.setProgress(pg)
                    playBtn.setPause()
                    uiChatMsgItemEntity.isPlaying = true
                }
            }

            override fun onStop(key: String) {
                if (key == uiChatMsgItemEntity.msMsg.clientMsgNO) {
                    voiceWaveform.setProgress(0f)
                    playBtn.setPlay()
                    uiChatMsgItemEntity.isPlaying = false
                    voiceWaveform.isFresh = false
                }
            }
        })
    }

    override val itemViewType: Int
        get() = MSMsgContentType.MS_VOICE


    private fun stopPlay() {
        MSPlayVoiceUtils.getInstance().stopPlay()
        var i = 0
        val size = getAdapter()!!.data.size
        while (i < size) {
            if (getAdapter()!!.data[i].msMsg != null
                && getAdapter()!!.data[i].msMsg.clientMsgNO == MSPlayVoiceUtils.getInstance().oldPlayKey
            ) {
                getAdapter()!!.data[i].isPlaying = false
                val waveformView =
                    getAdapter()!!.getViewByPosition(i, R.id.voiceWaveform) as WaveformView?
                val tempPlayBtn =
                    getAdapter()!!.getViewByPosition(i, R.id.playBtn) as CircleProgress?
                waveformView?.setProgress(0f)
                tempPlayBtn?.setPlay()
                break
            }
            i++
        }
    }

    private fun playNext(clientMsgNO: String) {
        val list: List<MSUIChatMsgItemEntity> = getAdapter()!!.data
        if (list.isNotEmpty()) {
            for (i in list.indices) {
                val mMsg = list[i].msMsg
                if (mMsg != null && mMsg.type == MSContentType.MS_VOICE && mMsg.clientMsgNO != clientMsgNO && mMsg.voiceStatus == 0 && !TextUtils.isEmpty(
                        mMsg.fromUID
                    )
                    && mMsg.fromUID != MSConfig.getInstance().uid
                ) {
                    val tempPlayBtn =
                        getAdapter()!!.getViewByPosition(i, R.id.playBtn) as CircleProgress?
                    tempPlayBtn?.performClick()
                    break
                }
            }
        }
    }

    private fun getVoiceWidth(timeTrad: Int, flame: Int): Int {
        var showWidth = 0
        val minWidth = AndroidUtilities.dp(150f)
        if (timeTrad <= 10) {
            showWidth = minWidth
        } else if (timeTrad <= 20) {
            showWidth = (minWidth * 1.1).toInt()
        } else if (timeTrad <= 30) {
            showWidth = (minWidth * 1.2).toInt()
        } else if (timeTrad <= 40) {
            showWidth = (minWidth * 1.3).toInt()
        } else if (timeTrad <= 50) {
            showWidth = (minWidth * 1.4).toInt()
        } else if (timeTrad <= 60) {
            showWidth = (minWidth * 1.5).toInt()
        }
        if (flame == 1) {
            showWidth -= AndroidUtilities.dp(45f)
        }
        return showWidth
    }

    fun updateViewed(
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        parentView: View,
        from: MSChatIteMsgFromType
    ) {
        if (uiChatMsgItemEntity.msMsg.flame == 1 && uiChatMsgItemEntity.msMsg.viewed == 0) {
            uiChatMsgItemEntity.msMsg.viewed = 1
            uiChatMsgItemEntity.msMsg.viewedAt =
                MSTimeUtils.getInstance().currentMills
            MSIM.getInstance().msgManager.updateViewedAt(
                1,
                uiChatMsgItemEntity.msMsg.viewedAt,
                uiChatMsgItemEntity.msMsg.clientMsgNO
            )
            val parentLayout = parentView as LinearLayout
            var deleteTimer: SecretDeleteTimer? = null
            if (uiChatMsgItemEntity.msMsg.flameSecond > 0 && parentLayout.childCount > 1) {
                if (from == MSChatIteMsgFromType.RECEIVED) {
                    deleteTimer =
                        parentLayout.getChildAt(1) as SecretDeleteTimer
                } else if (from == MSChatIteMsgFromType.SEND) {
                    deleteTimer =
                        parentLayout.getChildAt(0) as SecretDeleteTimer
                }
                if (deleteTimer != null) {
                    deleteTimer.visibility = View.VISIBLE
                    val flameSecond: Int =
                        if (uiChatMsgItemEntity.msMsg.type == MSContentType.MS_VOICE) {
                            val voiceContent =
                                uiChatMsgItemEntity.msMsg.baseContentMsgModel as MSVoiceContent
                            max(voiceContent.timeTrad, uiChatMsgItemEntity.msMsg.flameSecond)
                        } else {
                            uiChatMsgItemEntity.msMsg.flameSecond
                        }
                    deleteTimer.setDestroyTime(
                        uiChatMsgItemEntity.msMsg.clientMsgNO,
                        flameSecond,
                        uiChatMsgItemEntity.msMsg.viewedAt,
                        false
                    )
                }
            }
        }
    }

    override fun resetCellBackground(
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.resetCellBackground(parentView, uiChatMsgItemEntity, from)
        val bgType = getMsgBgType(
            uiChatMsgItemEntity.previousMsg,
            uiChatMsgItemEntity.msMsg,
            uiChatMsgItemEntity.nextMsg
        )
        val voiceLayout = parentView.findViewById<BubbleLayout>(R.id.voiceLayout)
        voiceLayout.setAll(bgType, from, MSContentType.MS_VOICE)

    }

    override fun resetCellListener(
        position: Int,
        parentView: View,
        uiChatMsgItemEntity: MSUIChatMsgItemEntity,
        from: MSChatIteMsgFromType
    ) {
        super.resetCellListener(position, parentView, uiChatMsgItemEntity, from)
        val voiceLayout = parentView.findViewById<BubbleLayout>(R.id.voiceLayout)
        val playBtn = parentView.findViewById<CircleProgress>(R.id.playBtn)
        addLongClick(voiceLayout, uiChatMsgItemEntity)
        addLongClick(playBtn, uiChatMsgItemEntity)
    }
}