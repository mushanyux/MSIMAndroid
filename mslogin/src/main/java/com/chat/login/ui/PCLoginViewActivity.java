package com.chat.login.ui;

import static android.view.View.VISIBLE;

import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.config.MSSystemAccount;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.MSToastUtils;
import com.chat.login.R;
import com.chat.login.databinding.PcLoginViewLayoutBinding;
import com.chat.login.service.LoginModel;
import com.mushanyux.mushanim.entity.MSChannelType;

/**
 * pc登录
 */
public class PCLoginViewActivity extends MSBaseActivity<PcLoginViewLayoutBinding> {
    @Override
    protected PcLoginViewLayoutBinding getViewBinding() {
        overridePendingTransition(R.anim.bottom_in, R.anim.bottom_silent);
        return PcLoginViewLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        Theme.setColorFilter(this, msVBinding.closeIv, R.color.popupTextColor);
        msVBinding.closeIv.setOnClickListener(v -> finish());
        msVBinding.exitBtn.setTextColor(Theme.colorAccount);
        msVBinding.exitBtn.setText(String.format(getString(R.string.exit_pc_login), getString(R.string.app_name)));
        msVBinding.pcLoginTv.setText(String.format(getString(R.string.pc_login), getString(R.string.app_name)));
        msVBinding.phoneMuteBtn.setOnClickListener(v -> {
            int muteForApp = MSSharedPreferencesUtil.getInstance().getInt(MSConfig.getInstance().getUid() + "_mute_of_app");
            LoginModel.getInstance().updateUserSetting("mute_of_app", muteForApp == 1 ? 0 : 1, (code, msg) -> {
                if (code == HttpResponseCode.success) {
                    MSSharedPreferencesUtil.getInstance().putInt(MSConfig.getInstance().getUid() + "_mute_of_app", muteForApp == 1 ? 0 : 1);
                    updateMuteStatus(muteForApp == 1 ? 0 : 1);
                } else MSToastUtils.getInstance().showToastNormal(msg);
            });

        });

        int muteForApp = MSSharedPreferencesUtil.getInstance().getInt(MSConfig.getInstance().getUid() + "_mute_of_app");
        updateMuteStatus(muteForApp);
        findViewById(R.id.exitBtn).setOnClickListener(v -> LoginModel.getInstance().quitPc((code, msg) -> {
            if (code == HttpResponseCode.success) {
                finish();
            } else MSToastUtils.getInstance().showToastNormal(msg);
        }));
        findViewById(R.id.fileLayout).setOnClickListener(v -> {
            finish();
            EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(PCLoginViewActivity.this, MSSystemAccount.system_file_helper, MSChannelType.PERSONAL, 0, false));
        });
        msVBinding.lockLayout.setOnClickListener(v -> {
            //锁定
            msVBinding.lockLayout.setBackground(Theme.getBackground(Theme.colorAccount,55));
            msVBinding.lockIv.setImageResource(R.mipmap.icon_lock_white);
            msVBinding.topLockIv.setVisibility(VISIBLE);
        });
    }

    @Override
    protected void initListener() {

    }


    private void updateMuteStatus(int muteForApp) {
        if (muteForApp == 1) {
            msVBinding.noticeTv.setText(R.string.phone_notice_close);
            msVBinding.phoneMuteBtn.setBackground(Theme.getBackground(Theme.colorAccount, 55, 55, 55));
            msVBinding.pcLoginIV.setImageResource(R.mipmap.device_status_pc_online_silence);
            msVBinding.muteIv.setImageResource(R.mipmap.icon_mute_white);
        } else {
            msVBinding.noticeTv.setText(R.string.phone_notice_open);
            msVBinding.phoneMuteBtn.setBackgroundResource(R.drawable.pc_login_btn_bg);
            msVBinding.pcLoginIV.setImageResource(R.mipmap.device_status_pc_online_normal);
            msVBinding.muteIv.setImageResource(R.mipmap.icon_mute);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.bottom_silent, R.anim.bottom_out);
    }
}
