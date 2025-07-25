package com.chat.base.net.ud

import android.os.Handler
import android.os.Looper
import com.chat.base.utils.MSLogUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Okio
import okio.buffer
import okio.sink
import java.io.IOException


class FileRequestBody(private val requestBody: RequestBody, private val tag: Any) : RequestBody() {
    private var mCurrentLength: Long = 0
    private var bufferedSink: BufferedSink? = null
    var handler = Handler(Looper.getMainLooper())
    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    override fun isOneShot(): Boolean {
        MSLogUtils.e("是否执行一次")
        return true
    }
    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    override fun writeTo(sink: BufferedSink) {
        val contentLength = contentLength()
        MSLogUtils.e("上传总长度$contentLength")
        val forwardingSink: ForwardingSink = object : ForwardingSink(sink) {
            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                mCurrentLength += byteCount
                val f1 = mCurrentLength / contentLength.toFloat()
                handler.post {
                    var p = (f1 * 100).toInt()
                    MSLogUtils.e("当前总长度$p")
                    if (p > 100) {
                        p = 100
                    }
                    MSProgressManager.instance.seekProgress(tag, p)
                }
                super.write(source, byteCount)
            }
        }
        val bufferedSink: BufferedSink = forwardingSink.buffer()
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}