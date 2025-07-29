package com.chat.login.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.MSToastUtils;
import com.chat.login.R;
import com.chat.login.databinding.ActWebLoginLayoutBinding;

import org.telegram.ui.Components.RLottieDrawable;

public class MSWebLoginActivity extends MSBaseActivity<ActWebLoginLayoutBinding> {
    @Override
    protected ActWebLoginLayoutBinding getViewBinding() {
        return ActWebLoginLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText("");
    }


    @Override
    protected void initView() {
        msVBinding.urlTv.setText(MSConfig.getInstance().getAppConfig().web_url);
        msVBinding.webLoginDescTv.setText(String.format(getString(R.string.web_scan_login_desc), MSConfig.getInstance().getAppConfig().web_url));
        msVBinding.nameTv.setText(String.format(getString(R.string.web_side), getString(R.string.app_name)));
        Theme.setPressedBackground(msVBinding.copyIv);

        RLottieDrawable drawable = new RLottieDrawable(this, R.raw.qrcode_web, "", AndroidUtilities.dp(180), AndroidUtilities.dp(180), false, null);
        msVBinding.imageView.setAutoRepeat(false);
        msVBinding.imageView.setAnimation(drawable);
        msVBinding.imageView.playAnimation();
    }

    @Override
    protected void initListener() {
        msVBinding.copyIv.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", MSConfig.getInstance().getAppConfig().web_url);
            assert cm != null;
            cm.setPrimaryClip(mClipData);
            MSToastUtils.getInstance().showToastNormal(getString(R.string.copied));
        });
        msVBinding.scanLayout.setOnClickListener(v -> {
            EndpointManager.getInstance().invoke("ms_scan_show", null);
        });
    }
}
