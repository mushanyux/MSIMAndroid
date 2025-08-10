package com.mushanyux.msimandroid

import android.app.Activity
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.TextUtils
import androidx.multidex.MultiDexApplication
import com.chat.base.MSBaseApplication
import com.chat.base.config.MSApiConfig
import com.chat.base.config.MSConfig
import com.chat.base.config.MSConstants
import com.chat.base.config.MSSharedPreferencesUtil
import com.chat.base.endpoint.EndpointManager
import com.chat.base.ui.Theme
import com.chat.base.utils.ActManagerUtils
import com.chat.base.utils.MSPlaySound
import com.chat.base.utils.MSTimeUtils
import com.chat.base.utils.language.MSMultiLanguageUtil
import com.chat.login.MSLoginApplication
import com.chat.push.MSPushApplication
import com.chat.scan.MSScanApplication
import com.chat.uikit.TabActivity
import com.chat.uikit.MSUIKitApplication
import com.chat.uikit.chat.manager.MSIMUtils
import com.chat.uikit.user.service.UserModel
import kotlin.system.exitProcess

class MSApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        val processName = getProcessName(this, Process.myPid())
        if (processName != null) {
            val defaultProcess = processName == getAppPackageName()
            if (defaultProcess) {
                initAll()
            }
        }
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
            }

            override fun onActivityStarted(p0: Activity) {
            }

            override fun onActivityResumed(p0: Activity) {
                ActManagerUtils.getInstance().currentActivity = p0
            }

            override fun onActivityPaused(p0: Activity) {
            }

            override fun onActivityStopped(p0: Activity) {
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityDestroyed(p0: Activity) {
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (applicationContext != null && applicationContext.resources != null && applicationContext.resources.configuration != null && applicationContext.resources.configuration.uiMode != newConfig.uiMode) {
            MSMultiLanguageUtil.getInstance().setConfiguration()
            Theme.applyTheme()
            killAppProcess()
        }
    }

    private fun killAppProcess() {
        ActManagerUtils.getInstance().clearAllActivity()
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(MSMultiLanguageUtil.getInstance().attachBaseContext(base))
    }

    private fun initAll() {
        MSMultiLanguageUtil.getInstance().init(this)
        MSBaseApplication.getInstance().init(getAppPackageName(), this)
        Theme.applyTheme()
        initApi()
        MSLoginApplication.getInstance().init(this)
        MSScanApplication.getInstance().init(this)
        MSUIKitApplication.getInstance().init(this)
        MSPushApplication.getInstance().init(getAppPackageName(), this)
        addAppFrontBack()
        addListener()
    }

    private fun initApi() {
        var apiURL = MSSharedPreferencesUtil.getInstance().getSP("api_base_url")
        if (TextUtils.isEmpty(apiURL)) {
            apiURL = "https://api.****.cn"
            MSApiConfig.initBaseURL(apiURL)
        } else {
            MSApiConfig.initBaseURLIncludeIP(apiURL)
        }
    }

    private fun getAppPackageName(): String {
        return "com.mushanyux.msimandroid"
    }

    private fun getProcessName(cxt: Context, pid: Int): String? {
        val am = cxt.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses ?: return null
        for (app in runningApps) {
            if (app.pid == pid) {
                return app.processName
            }
        }
        return null
    }

    private fun addAppFrontBack() {
        val helper = AppFrontBackHelper()
        helper.register(this, object : AppFrontBackHelper.OnAppStatusListener {
            override fun onFront() {
                if (!TextUtils.isEmpty(MSConfig.getInstance().token)) {
                    if (MSBaseApplication.getInstance().disconnect) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            EndpointManager.getInstance()
                                .invoke("chow_check_lock_screen_pwd", null)
                        }, 1000)
                    }
                    MSIMUtils.getInstance().initIMListener()
                    MSUIKitApplication.getInstance().startChat()
                    UserModel.getInstance().getOnlineUsers()

                }
            }

            override fun onBack() {
                val result = EndpointManager.getInstance().invoke("rtc_is_calling", null)
                var isCalling = false
                if (result != null) {
                    isCalling = result as Boolean
                }
                if (MSBaseApplication.getInstance().disconnect && !isCalling) {
                    MSUIKitApplication.getInstance().stopConn()
                }
                MSIMUtils.getInstance().removeListener()
                MSSharedPreferencesUtil.getInstance()
                    .putLong("lock_start_time", MSTimeUtils.getInstance().currentSeconds)

            }
        })
    }

    private fun addListener() {
        createNotificationChannel()
        EndpointManager.getInstance().setMethod("main_show_home_view") { `object` ->
            if (`object` != null) {
                val from = `object` as Int
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("from", from)
                startActivity(intent)
            }
            null
        }
        EndpointManager.getInstance().setMethod("show_tab_home") {
            val intent = Intent(applicationContext, TabActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            null
        }

        EndpointManager.getInstance().setMethod("play_new_msg_Media") {
            MSPlaySound.getInstance().playRecordMsg(R.raw.newmsg)
            null
        }
    }


    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = applicationContext.getString(R.string.new_msg_notification)
            val description = applicationContext.getString(R.string.new_msg_notification_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(MSConstants.newMsgChannelID, name, importance)
            channel.description = description
            channel.enableVibration(true) //是否有震动
            channel.setSound(
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.newmsg),
                Notification.AUDIO_ATTRIBUTES_DEFAULT
            )
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
        createNotificationRTCChannel()
    }

    private fun createNotificationRTCChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = applicationContext.getString(R.string.new_rtc_notification)
            val description = applicationContext.getString(R.string.new_rtc_notification_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(MSConstants.newRTCChannelID, name, importance)
            channel.description = description
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 100, 100, 100, 100, 100)
            channel.setSound(
                Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.newrtc),
                Notification.AUDIO_ATTRIBUTES_DEFAULT
            )
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = applicationContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

}