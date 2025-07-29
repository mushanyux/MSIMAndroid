package com.chat.login.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.login.entity.CountryCodeEntity;
import com.chat.login.R;
import com.chat.login.databinding.ActResetLoginPwdLayoutBinding;
import com.chat.login.service.LoginContract;
import com.chat.login.service.LoginPresenter;

import java.util.List;
import java.util.Objects;

/**
 * 重置登录密码
 */
public class MSResetLoginPwdActivity extends MSBaseActivity<ActResetLoginPwdLayoutBinding> implements LoginContract.LoginView {

    private String code = "0086";
    private LoginPresenter presenter;

    @Override
    protected ActResetLoginPwdLayoutBinding getViewBinding() {
        return ActResetLoginPwdLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initPresenter() {
        presenter = new LoginPresenter(this);
    }

    @Override
    protected void initView() {
        msVBinding.sureBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.getVerCodeBtn.getBackground().setTint(Theme.colorAccount);
        Theme.setPressedBackground(msVBinding.backIv);
        msVBinding.backIv.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorDark), PorterDuff.Mode.MULTIPLY));
        boolean canEditPhone = getIntent().getBooleanExtra("canEditPhone", false);
        msVBinding.nameEt.setEnabled(canEditPhone);
        msVBinding.nameEt.setText(MSConfig.getInstance().getUserInfo().phone);
        String zone = MSConfig.getInstance().getUserInfo().zone;
        if (!TextUtils.isEmpty(zone)) {
            code = zone;
            String codeName = code.substring(2);
            msVBinding.codeTv.setText(String.format("+%s", codeName));
        }
        if (!canEditPhone || !TextUtils.isEmpty(Objects.requireNonNull(msVBinding.nameEt.getText()).toString())) {
            msVBinding.getVerCodeBtn.setEnabled(true);
            msVBinding.getVerCodeBtn.setAlpha(1);
        }

        msVBinding.resetLoginPwdTv.setText(String.format(getString(R.string.auth_phone_tips), getString(R.string.app_name)));
    }

    @Override
    protected void initListener() {
        msVBinding.nameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    msVBinding.getVerCodeBtn.setEnabled(true);
                    msVBinding.getVerCodeBtn.setAlpha(1f);
                } else {
                    msVBinding.getVerCodeBtn.setEnabled(false);
                    msVBinding.getVerCodeBtn.setAlpha(0.2f);
                }
                checkStatus();
            }
        });
        msVBinding.verfiEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkStatus();
            }
        });
        msVBinding.pwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkStatus();
            }
        });
        msVBinding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                msVBinding.pwdEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                msVBinding.pwdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            msVBinding.pwdEt.setSelection(Objects.requireNonNull(msVBinding.pwdEt.getText()).length());
        });
        msVBinding.chooseCodeTv.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseAreaCodeActivity.class);
            intentActivityResultLauncher.launch(intent);
        });
        msVBinding.sureBtn.setOnClickListener(v -> {

            String phone = Objects.requireNonNull(msVBinding.nameEt.getText()).toString();
            String verCode = msVBinding.verfiEt.getText().toString();
            String pwd = msVBinding.pwdEt.getText().toString();
            if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(verCode) && !TextUtils.isEmpty(pwd)) {
                if (pwd.length() < 6 || pwd.length() > 16) {
                    showToast(R.string.pwd_length_error);
                } else {
                    loadingPopup.show();
                    presenter.resetPwd(code, phone, verCode, pwd);
                }
            }

        });
        msVBinding.getVerCodeBtn.setOnClickListener(v -> {
            String phone = msVBinding.nameEt.getText().toString();
            if (!TextUtils.isEmpty(phone)) {
                presenter.forgetPwd(code, phone);
            }
        });
        msVBinding.backIv.setOnClickListener(v -> finish());
    }


    private void checkStatus() {
        String phone = msVBinding.nameEt.getText().toString();
        String verCode = msVBinding.verfiEt.getText().toString();
        String pwd = msVBinding.pwdEt.getText().toString();
        if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(verCode) && !TextUtils.isEmpty(pwd)) {
            msVBinding.sureBtn.setAlpha(1f);
            msVBinding.sureBtn.setEnabled(true);
        } else {
            msVBinding.sureBtn.setAlpha(0.2f);
            msVBinding.sureBtn.setEnabled(false);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            CountryCodeEntity entity = data.getParcelableExtra("entity");
            assert entity != null;
            code = entity.code;
            String codeName = code.substring(2);
            msVBinding.codeTv.setText(String.format("+%s", codeName));
        }
    }

    @Override
    public void loginResult(UserInfoEntity userInfoEntity) {

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
            presenter.startTimer();
        } else {
            showToast(msg);
        }
    }

    @Override
    public void setResetPwdResult(int code, String msg) {
        if (code == HttpResponseCode.success) {
            finish();
        }
    }

    @Override
    public Button getVerfiCodeBtn() {
        return msVBinding.getVerCodeBtn;

    }

    @Override
    public EditText getNameEt() {
        return msVBinding.nameEt;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showError(String msg) {
        showToast(msg);
    }

    @Override
    public void hideLoading() {
        loadingPopup.dismiss();
    }


    ActivityResultLauncher<Intent> intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        //此处是跳转的result回调方法
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            CountryCodeEntity entity = result.getData().getParcelableExtra("entity");
            assert entity != null;
            code = entity.code;
            String codeName = code.substring(2);
            msVBinding.codeTv.setText(String.format("+%s", codeName));
        }
    });
}
