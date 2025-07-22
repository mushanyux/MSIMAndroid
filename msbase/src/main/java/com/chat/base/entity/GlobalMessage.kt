package com.chat.base.entity


import android.text.TextUtils
import com.chat.base.R
import com.chat.base.MSBaseApplication
import com.mushanyux.mushanim.MSIM
import com.mushanyux.mushanim.msgmodel.MSMessageContent
import org.json.JSONObject


class GlobalMessage {
    var setting: Int = 0
    lateinit var message_idstr: String
    var message_seq: Long = 0
    lateinit var client_msg_no: String
    lateinit var from_uid: String
    var timestamp: Long = 0L
    var is_deleted: Int = 0
    lateinit var channel: GlobalChannel
    lateinit var from_channel: GlobalChannel
    lateinit var payload: Map<String, Any>
    private var messageContent: MSMessageContent? = null

    fun getMessageModel(): MSMessageContent? {
        if (messageContent == null) {
            val jsonObject = JSONObject(payload)
            messageContent = MSIM.getInstance().msgManager.getMsgContentModel(jsonObject)
        }
        return messageContent
    }

    fun getContentType(): Int {
        val type = payload["type"]
        if (type is Int) {
            return type
        }
        return 0
    }


    fun getHtmlText(): String {
        val content = getMessageModel()?.content
        if (!TextUtils.isEmpty(content)) {
            return content!!.replace("<mark>", "<font color=#f65835>")
                .replace("</mark>", "</font>")
        }
        return ""
    }

    fun getHtmlWithField(field: String): String {
        val content = payload[field]
        if (content is String && !TextUtils.isEmpty(content)) {
            return MSBaseApplication.getInstance().application.getString(R.string.last_message_file) + " " + content.replace(
                "<mark>",
                "<font color=#f65835>"
            )
                .replace("</mark>", "</font>")
        }
        return ""
    }
}