package com.chat.uikit.message

import android.view.View
import android.widget.TextView
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.chat.base.base.MSBaseActivity
import com.chat.base.config.MSConfig
import com.chat.base.config.MSConstants
import com.chat.base.net.HttpResponseCode
import com.chat.base.utils.MSFileUtils
import com.chat.base.utils.MSFileUtils.IWriteText
import com.chat.base.utils.MSReader
import com.chat.base.utils.singleclick.SingleClickUtil
import com.chat.uikit.R
import com.chat.uikit.databinding.ActBackupMessageLayoutBinding
import com.chat.uikit.message.MsgModel.IRecovery
import com.mushanyux.mushanim.MSIM
import com.mushanyux.mushanim.entity.MSMsg
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File

class BackupRestoreMessageActivity : MSBaseActivity<ActBackupMessageLayoutBinding>() {
    private var handleType: Int = 1 // 1：备份 2：恢复
    override fun getViewBinding(): ActBackupMessageLayoutBinding {
        return ActBackupMessageLayoutBinding.inflate(layoutInflater)
    }

    override fun initPresenter() {
        handleType = intent.getIntExtra("handle_type", 1)
    }

    override fun setTitle(titleTv: TextView?) {
        if (handleType == 1) {
            titleTv!!.setText(R.string.backup_message)
        } else titleTv!!.setText(R.string.recovery_message)
    }

    override fun initListener() {
        SingleClickUtil.onSingleClick(msVBinding.startBtn) {
            msVBinding.loading.visibility = View.VISIBLE
            msVBinding.startBtn.alpha = 0.2f
            msVBinding.startBtn.isEnabled = false
            if (handleType == 1) {
                Observable.create { e ->
                    val list1 = MSIM.getInstance().msgManager.all
                    e.onNext(list1)
                }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                    .subscribe(object : Observer<List<MSMsg>> {
                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(list: List<MSMsg>) {
                            backup(list)
                        }

                        override fun onError(e: Throwable) {}
                        override fun onComplete() {}
                    })

            } else {
                recovery()
            }
        }

    }

    private fun backup(list: List<MSMsg>) {
        if (MSReader.isNotEmpty(list)) {
            val jsonArray = JSONArray()
            for (msg in list) {
                if (msg.isDeleted == 1) continue
                val json = JSONObject()
                json["channel_id"] = msg.channelID
                json["channel_type"] = msg.channelType
                json["message_id"] = msg.messageID
                json["message_seq"] = msg.messageSeq
                json["client_msg_no"] = msg.clientMsgNO
                json["from_uid"] = msg.fromUID
                json["payload"] = msg.content
                json["timestamp"] = msg.timestamp
                jsonArray.add(json)
            }
            val uid = MSConfig.getInstance().uid
            val path = MSConstants.messageBackupDir + uid + ".json"
            val file = File(path)
            if (file.exists()) file.delete()
            MSFileUtils.getInstance()
                .writeTxtToFile(jsonArray.toString(), path, object : IWriteText {
                    override fun onFail() {
                        showToast("保存备份数据错误")
                    }

                    override fun onSuccess() {
                        MsgModel.getInstance().backupMsg(
                            path
                        ) { code, msg ->
                            if (code != HttpResponseCode.success.toInt()) {
                                showToast(msg)
                            } else {
                                showToast(R.string.str_success)
                            }
                            msVBinding.startBtn.alpha = 1.0f
                            msVBinding.startBtn.isEnabled = true
                            msVBinding.loading.visibility = View.GONE
                        }
                    }
                })
        }

    }

    private fun recovery() {
        MsgModel.getInstance().recovery(object : IRecovery {
            override fun onSuccess(path: String?) {
                finish()
            }

            override fun onFail() {
                showToast(R.string.recovery_msg_fail)
                msVBinding.startBtn.alpha = 1.0f
                msVBinding.startBtn.isEnabled = true
                msVBinding.loading.visibility = View.GONE
            }
        })
    }
}