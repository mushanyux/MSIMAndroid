package com.chat.base.net.ud

import java.io.File

class MSDownloader private constructor() {

    companion object {
        val instance: MSDownloader by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MSDownloader()
        }
    }

    fun download(url: String, savePath: String, iProgress: MSProgressManager.IProgress?) {
        Downloader.instance.download(url, savePath, object : OnDownload {
            override fun invoke(url: String, progress: Int) = if (iProgress != null) {
                iProgress.run { onProgress(url, progress) }
            } else {
                MSProgressManager.instance.seekProgress(url, progress)
            }

        }, object : OnComplete {
            override fun invoke(url: String, file: File) = if (iProgress != null) {
                iProgress.run { onSuccess(url, file.absolutePath) }
            } else {
                MSProgressManager.instance.onSuccess(url, file.absolutePath)
            }

        }, object : OnFail {
            override fun invoke(url: String, reason: String) = if (iProgress != null) {
                iProgress.run { onFail(url, reason) }
            } else {
                MSProgressManager.instance.onFail(url, reason)
            }
        })
    }
}