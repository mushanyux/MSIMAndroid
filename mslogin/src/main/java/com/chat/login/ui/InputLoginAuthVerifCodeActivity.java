package com.chat.login.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chat.base.utils.MSReader;
import com.chat.login.entity.CountryCodeEntity;
import com.chat.login.R;
import com.chat.login.databinding.ActInputLoginAuthVerifCodeBinding;
import com.chat.login.service.LoginContract;
import com.chat.login.service.LoginPresenter;
import com.chat.base.base.MSBaseActivity;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.LoginMenu;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.SoftKeyboardUtils;

import java.util.List;
import java.util.Objects;

/**
 * 登录验认证证码
 */
public class InputLoginAuthVerifCodeActivity extends MSBaseActivity<ActInputLoginAuthVerifCodeBinding> implements LoginContract.LoginView {

    private LoginPresenter loginPresenter;
    private String uid;

    @Override
    protected ActInputLoginAuthVerifCodeBinding getViewBinding() {
        return ActInputLoginAuthVerifCodeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.hint_verfi);
    }

    @Override
    protected void initPresenter() {
        loginPresenter = new LoginPresenter(this);
        loginPresenter.startTimer();
    }

    @Override
    protected void initView() {
        msVBinding.sureBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.getVerfiCodeBtn.getBackground().setTint(Theme.colorAccount);
        uid = getIntent().getStringExtra("uid");
        String phone = getIntent().getStringExtra("phone");
        msVBinding.sendCodeTv.setText(String.format(getString(R.string.send_code_desc), phone));
    }

    @Override
    protected void initListener() {
        msVBinding.verfiEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable.toString())) {
                    msVBinding.sureBtn.setAlpha(1f);
                    msVBinding.sureBtn.setEnabled(true);
                } else {
                    msVBinding.sureBtn.setAlpha(0.2f);
                    msVBinding.sureBtn.setEnabled(false);
                }
            }
        });
        msVBinding.getVerfiCodeBtn.setOnClickListener(v -> loginPresenter.sendLoginAuthVerifCode(uid));
        msVBinding.sureBtn.setOnClickListener(v -> {
            loadingPopup.setTitle(getString(R.string.login_authing));
            loadingPopup.show();
            String code = Objects.requireNonNull(msVBinding.verfiEt.getText()).toString();
            loginPresenter.checkLoginAuth(uid, code);
        });
    }


    @Override
    public void loginResult(UserInfoEntity userInfoEntity) {
        SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.verfiEt);
        hideLoading();
        if (TextUtils.isEmpty(userInfoEntity.name)) {
            Intent intent = new Intent(this, PerfectUserInfoActivity.class);
            startActivity(intent);
        } else {

            List<LoginMenu> list = EndpointManager.getInstance().invokes(EndpointCategory.loginMenus, null);
            if (MSReader.isNotEmpty(list)) {
                for (LoginMenu loginMenu : list) {
                    if (loginMenu.iMenuClick != null) loginMenu.iMenuClick.onClick();
                }
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                setResult(RESULT_OK);
                finish();
            }, 200);
        }
    }

    @Override
    public void setCountryCode(List<CountryCodeEntity> list) {

    }

    @Override
    public void setRegisterCodeSuccess(int code, String msg, int exist) {

    }

    @Override
    public void setLoginFail(int code, String uid, String phone) {

    }

    @Override
    public void setSendCodeResult(int code, String msg) {
        if (code == HttpResponseCode.success) {
            loginPresenter.startTimer();
        } else showToast(msg);
    }

    @Override
    public void setResetPwdResult(int code, String msg) {

    }

    @Override
    public Button getVerfiCodeBtn() {
        return msVBinding.getVerfiCodeBtn;
    }

    @Override
    public EditText getNameEt() {
        return msVBinding.verfiEt;
    }

    @Override
    public void showError(String msg) {
        showToast(msg);
    }

    @Override
    public void hideLoading() {
        loadingPopup.dismiss();
    }


    @Override
    public Context getContext() {
        return this;
    }

}
