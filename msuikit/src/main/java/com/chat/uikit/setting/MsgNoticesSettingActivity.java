package com.chat.uikit.setting;

import android.view.View;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSConstants;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatBgItemMenu;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.systembar.MSOSUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActMsgNoticesSetLayoutBinding;
import com.chat.uikit.user.service.UserModel;
import com.mushanyux.mushanim.entity.MSChannelType;

/**
 * 新消息通知设置
 */
public class MsgNoticesSettingActivity extends MSBaseActivity<ActMsgNoticesSetLayoutBinding> {
    UserInfoEntity userInfoEntity;

    @Override
    protected ActMsgNoticesSetLayoutBinding getViewBinding() {
        return ActMsgNoticesSetLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.new_msg_notice);
    }

    @Override
    protected void initPresenter() {
        userInfoEntity = MSConfig.getInstance().getUserInfo();
    }

    @Override
    protected void initView() {
        msVBinding.voiceShockDescTv.setText(String.format(getString(R.string.voice_shock_desc), getString(R.string.app_name)));
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);
        msVBinding.newMsgNoticeSwitch.setChecked(userInfoEntity.setting.new_msg_notice == 1);
        msVBinding.voiceSwitch.setChecked(userInfoEntity.setting.voice_on == 1);
        msVBinding.shockSwitch.setChecked(userInfoEntity.setting.shock_on == 1);
        msVBinding.newMsgNoticeDetailSwitch.setChecked(userInfoEntity.setting.msg_show_detail == 1);
        View keepAliveView = (View) EndpointManager.getInstance().invoke("show_keep_alive_item", this);
        if (keepAliveView != null) {
            msVBinding.keepAliveLayout.addView(keepAliveView);
        }
    }

    @Override
    protected void initListener() {
        msVBinding.newMsgNoticeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                userInfoEntity.setting.new_msg_notice = b ? 1 : 0;
                UserModel.getInstance().updateUserSetting("new_msg_notice", userInfoEntity.setting.new_msg_notice, (code, msg) -> {
                    if (code == HttpResponseCode.success) {
                        MSConfig.getInstance().saveUserInfo(userInfoEntity);
                    } else showToast(msg);
                });
            }
        });
        msVBinding.voiceSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                userInfoEntity.setting.voice_on = b ? 1 : 0;
                UserModel.getInstance().updateUserSetting("voice_on", userInfoEntity.setting.voice_on, (code, msg) -> {
                    if (code == HttpResponseCode.success) {
                        MSConfig.getInstance().saveUserInfo(userInfoEntity);
                    } else showToast(msg);
                });
            }
        });
        msVBinding.shockSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                userInfoEntity.setting.shock_on = b ? 1 : 0;
                UserModel.getInstance().updateUserSetting("shock_on", userInfoEntity.setting.shock_on, (code, msg) -> {
                    if (code == HttpResponseCode.success) {
                        MSConfig.getInstance().saveUserInfo(userInfoEntity);
                    } else showToast(msg);
                });
            }
        });
        msVBinding.newMsgNoticeDetailSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                userInfoEntity.setting.msg_show_detail = b ? 1 : 0;
                UserModel.getInstance().updateUserSetting("msg_show_detail", userInfoEntity.setting.msg_show_detail, (code, msg) -> {
                    if (code == HttpResponseCode.success) {
                        MSConfig.getInstance().saveUserInfo(userInfoEntity);
                    } else showToast(msg);
                });
            }
        });
        msVBinding.openNoticeLayout.setOnClickListener(v -> {
            MSOSUtils.openChannelSetting(this, MSConstants.newMsgChannelID);
        });
        msVBinding.openRTCNoticeLayout.setOnClickListener(v -> {
            MSOSUtils.openChannelSetting(this, MSConstants.newRTCChannelID);
        });
    }
}
