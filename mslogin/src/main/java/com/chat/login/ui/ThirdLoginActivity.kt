package com.chat.login.ui

import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import com.chat.base.act.MSWebViewActivity
import com.chat.base.base.MSBaseActivity
import com.chat.base.common.MSCommonModel
import com.chat.base.config.MSApiConfig
import com.chat.base.config.MSConfig
import com.chat.base.config.MSSharedPreferencesUtil
import com.chat.base.endpoint.EndpointCategory
import com.chat.base.endpoint.EndpointManager
import com.chat.base.endpoint.entity.LoginMenu
import com.chat.base.net.HttpResponseCode
import com.chat.base.utils.MSDialogUtils
import com.chat.login.R
import com.chat.login.databinding.ActThirdLoginLayoutBinding
import com.chat.login.service.LoginModel
import java.util.Locale


class ThirdLoginActivity : MSBaseActivity<ActThirdLoginLayoutBinding>() {
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var authCode: String
    override fun getViewBinding(): ActThirdLoginLayoutBinding {
        return ActThirdLoginLayoutBinding.inflate(layoutInflater)
    }

    override fun initView() {
        msVBinding.loginTitleTv.text =
            String.format(getString(R.string.login_title), getString(R.string.app_name))
    }

    override fun initListener() {
        msVBinding.loginTitleTv.setOnLongClickListener {
            val intent = Intent(this@ThirdLoginActivity, MSLoginActivity::class.java)
            startActivity(intent)
            true
        }
        msVBinding.giteeIV.setOnClickListener {
            getAuthCode("gitee")
        }
        msVBinding.githubIV.setOnClickListener {
            getAuthCode("github")
        }
        msVBinding.settingIV.setOnClickListener {
            if (MSConfig.getInstance().appConfig == null || MSConfig.getInstance().appConfig.can_modify_api_url == 0) {
                return@setOnClickListener
            }
            val oldUrl = MSSharedPreferencesUtil.getInstance().getSP("api_base_url")
            MSDialogUtils.getInstance().showInputDialog(
                this,
                getString(R.string.update_api),
                getString(R.string.update_api_content),
                oldUrl,
                getString(R.string.update_api_ip),
                100
            ) { text ->
                if (!TextUtils.isEmpty(text)) {
                    var url = text
                    if (!text.lowercase(Locale.getDefault()).startsWith("http")) {
                        url = "http://$text"
                    }
                    MSSharedPreferencesUtil.getInstance().putSP("api_base_url", url)
                    EndpointManager.getInstance().invoke("update_base_url", url)
                }
            }

        }
        countDownTimer = object : CountDownTimer(1000 * 60 * 10, 1000) {
            override fun onTick(l: Long) {
                getAuthCodeStatus()
            }

            //时间段内最后一次定时任务
            override fun onFinish() {
            }
        }
    }

    override fun initData() {
        super.initData()
        MSCommonModel.getInstance().getAppConfig { _, _, appConfig ->
            if (appConfig != null && appConfig.can_modify_api_url == 1) {
                msVBinding.settingIV.visibility = View.VISIBLE
            }
        }
    }

    private fun getAuthCodeStatus() {
        LoginModel.getInstance().getAuthCodeStatus(
            authCode
        ) { code, _ ->
            if (code == HttpResponseCode.success.toInt()) {
                countDownTimer.cancel()

                runOnUiThread {
                    Handler(Looper.myLooper()!!).postDelayed({
                        val list = EndpointManager.getInstance()
                            .invokes<LoginMenu>(EndpointCategory.loginMenus, null)
                        if (list != null && list.size > 0) {
                            for (menu in list) {
                                if (menu.iMenuClick != null) menu.iMenuClick.onClick()
                            }
                        }
                        finish()
                    }, 200)
                }
            }
        }
    }

    private fun getAuthCode(type: String) {
        loadingPopup.show()
        loadingPopup.setTitle(getString(R.string.logging_in))
        LoginModel.getInstance().getAuthCode { code, msg, authCode ->
            loadingPopup.dismiss()
            if (code == HttpResponseCode.success.toInt()) {
                if (!TextUtils.isEmpty(authCode)) {
                    openWeb(type, authCode!!)
                }
            } else {
                showToast(msg)
            }
        }
    }

    private fun openWeb(type: String, authCode: String) {
        this.authCode = authCode
        val intent = Intent(this, MSWebViewActivity::class.java)
        intent.putExtra("url", MSApiConfig.baseUrl + "user/$type?authcode=$authCode")
        startActivity(intent)
        countDownTimer.start()
    }


}