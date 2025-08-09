package com.chat.uikit.setting;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSSystemAccount;
import com.chat.base.utils.MSDeviceUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActAboutLayoutBinding;
import com.mushanyux.mushanim.entity.MSChannelType;

/**
 * 关于
 */
public class MSAboutActivity extends MSBaseActivity<ActAboutLayoutBinding> {

    @Override
    protected ActAboutLayoutBinding getViewBinding() {
        return ActAboutLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(String.format("%s%s", getString(R.string.about), getString(R.string.app_name)));
    }

    @Override
    protected void initView() {

        SingleClickUtil.onSingleClick(msVBinding.icpTV, view1 -> {
            // 隐私政策
            showWebView("https://beian.miit.gov.cn/#/home");
        });
        SingleClickUtil.onSingleClick(msVBinding.privacyPolicyLayout, view1 -> {
            // 隐私政策
            showWebView(MSApiConfig.baseWebUrl + "privacy_policy.html");
        });
        SingleClickUtil.onSingleClick(msVBinding.userAgreementLayout, view1 -> {
            // 用户协议
            showWebView(MSApiConfig.baseWebUrl + "user_agreement.html");
        });
        SingleClickUtil.onSingleClick(msVBinding.checkNewVersionLayout, view1 -> checkNewVersion(true));
        checkNewVersion(false);
        String v = MSDeviceUtils.getInstance().getVersionName(this);
        msVBinding.versionTv.setText(String.format("version %s", v));
        msVBinding.appNameTv.setText(R.string.app_name);
    }

    @Override
    protected void initListener() {
        msVBinding.avatarView.setSize(80);
        msVBinding.avatarView.showAvatar(MSSystemAccount.system_team, MSChannelType.PERSONAL);
    }

    private void checkNewVersion(boolean isShowDialog) {
        MSCommonModel.getInstance().getAppNewVersion(isShowDialog, version -> {
            if (version != null && !TextUtils.isEmpty(version.download_url)) {
                if (isShowDialog) {
                    MSDialogUtils.getInstance().showNewVersionDialog(MSAboutActivity.this, version);
                } else {
                    msVBinding.newVersionIv.setVisibility(View.VISIBLE);
                }
            } else {
                msVBinding.newVersionIv.setVisibility(View.GONE);
            }
        });
    }


}
