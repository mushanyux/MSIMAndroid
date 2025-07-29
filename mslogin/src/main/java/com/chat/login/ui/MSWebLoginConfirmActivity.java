package com.chat.login.ui;

import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.login.R;
import com.chat.login.databinding.ActWebLoginAuthLayoutBinding;
import com.chat.login.service.LoginModel;

/**
 * web登录确认
 */
public class MSWebLoginConfirmActivity extends MSBaseActivity<ActWebLoginAuthLayoutBinding> {
    private String auth_code;

    @Override
    protected ActWebLoginAuthLayoutBinding getViewBinding() {
        return ActWebLoginAuthLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initPresenter() {
        Theme.setColorFilter(this, msVBinding.closeIv,R.color.popupTextColor);
    }

    @Override
    protected void initView() {
        msVBinding.loginBtn.getBackground().setTint(Theme.colorAccount);
        auth_code = getIntent().getStringExtra("auth_code");
    }

    @Override
    protected void initListener() {
        msVBinding.webLoginDescTv.setText(String.format(getString(R.string.web_login_desc), getString(R.string.app_name)));
        msVBinding.closeIv.setOnClickListener(v -> finish());
        msVBinding.closeTv.setOnClickListener(v -> finish());
        msVBinding.loginBtn.setOnClickListener(v -> LoginModel.getInstance().webLogin(auth_code, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                finish();
            } else showToast(msg);
        }));
    }

}
