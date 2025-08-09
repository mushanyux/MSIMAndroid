package com.chat.uikit.setting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chat.base.act.MSWebViewActivity;
import com.chat.base.base.MSBaseActivity;
import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSApiConfig;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatBgItemMenu;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.DataCleanManager;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSLogUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.MSUIKitApplication;
import com.chat.uikit.databinding.ActSettingLayoutBinding;
import com.chat.uikit.message.BackupRestoreMessageActivity;
import com.chat.uikit.user.service.UserModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannelType;

/**
 * 设置页面
 */
public class SettingActivity extends MSBaseActivity<ActSettingLayoutBinding> {
    private String str;

    @Override
    protected ActSettingLayoutBinding getViewBinding() {
        return ActSettingLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.setting);
    }

    @Override
    protected void initPresenter() {
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);
    }

    @Override
    protected void initView() {
        getCacheSize();
        EndpointManager.getInstance().invoke("set_chat_bg_view", new ChatBgItemMenu(this, msVBinding.chatBgLayout, "", MSChannelType.PERSONAL));
    }

    @Override
    protected void initListener() {
        String ms_theme_pref = Theme.getTheme();
        if (ms_theme_pref.equals(Theme.DARK_MODE)) {
            msVBinding.darkStatusTv.setText(R.string.enabled);
        } else {
            msVBinding.darkStatusTv.setText(R.string.disabled);
        }
        msVBinding.loginOutTv.setOnClickListener(v -> MSDialogUtils.getInstance().showDialog(this, getString(R.string.login_out), getString(R.string.login_out_dialog), true, "", getString(R.string.login_out), 0, 0, index -> {
            if (index == 1) {
                UserModel.getInstance().quit(null);
                MSUIKitApplication.getInstance().exitLogin(0);
            }
        }));
        SingleClickUtil.onSingleClick(msVBinding.languageLayout, view1 -> startActivity(new Intent(this, MSLanguageActivity.class)));
        SingleClickUtil.onSingleClick(msVBinding.darkLayout, view1 -> startActivity(new Intent(this, MSThemeSettingActivity.class)));
        msVBinding.clearImgCacheLayout.setOnClickListener(v -> showDialog(getString(R.string.clear_img_cache_tips), index -> {
            if (index == 1) {
                DataCleanManager.clearAllCache(SettingActivity.this);
                str = "0.00M";
                msVBinding.imageCacheTv.setText(str);
            }
        }));
        msVBinding.clearChatMsgLayout.setOnClickListener(v -> showDialog(getString(R.string.clear_all_msg_tips), index -> {
            if (index == 1) {
                MSIM.getInstance().getConversationManager().clearAll();
                MSIM.getInstance().getMsgManager().clearAll();
            }
        }));
        SingleClickUtil.onSingleClick(msVBinding.moduleLayout, view1 -> startActivity(new Intent(this, AppModulesActivity.class)));
        SingleClickUtil.onSingleClick(msVBinding.aboutLayout, view1 -> startActivity(new Intent(this, MSAboutActivity.class)));
        SingleClickUtil.onSingleClick(msVBinding.fontSizeLayout, view1 -> startActivity(new Intent(this, MSSetFontSizeActivity.class)));
        MSCommonModel.getInstance().getAppNewVersion(false, version -> {
            if (version != null && !TextUtils.isEmpty(version.download_url)) {
                msVBinding.newVersionIv.setVisibility(View.VISIBLE);
            } else {
                msVBinding.newVersionIv.setVisibility(View.GONE);
            }
        });

        SingleClickUtil.onSingleClick(msVBinding.msgBackupLayout, view1 -> {
            Intent intent = new Intent(this, BackupRestoreMessageActivity.class);
            intent.putExtra("handle_type", 1);
            startActivity(intent);
        });
        SingleClickUtil.onSingleClick(msVBinding.msgRecoveryLayout, view1 -> {
            Intent intent = new Intent(this, BackupRestoreMessageActivity.class);
            intent.putExtra("handle_type", 2);
            startActivity(intent);
        });
        SingleClickUtil.onSingleClick(msVBinding.thirdShareLayout, view1 -> {
            Intent intent = new Intent(this, MSWebViewActivity.class);
            intent.putExtra("url", MSApiConfig.baseWebUrl + "sdkinfo.html");
            startActivity(intent);
        });
        SingleClickUtil.onSingleClick(msVBinding.errorLogLayout, view1 -> startActivity(new Intent(this, ErrorLogsActivity.class)));

    }


    //获取缓存大小
    private void getCacheSize() {
        new Thread(() -> {
            try {
                str = DataCleanManager.getTotalCacheSize(SettingActivity.this);
                if (str.equalsIgnoreCase("0.0Byte")) {
                    str = "0.00M";
                }
                AndroidUtilities.runOnUIThread(() -> msVBinding.imageCacheTv.setText(str));
            } catch (Exception e) {
                MSLogUtils.e("获取图片缓存大小错误");
            }
        }).start();

    }

}
