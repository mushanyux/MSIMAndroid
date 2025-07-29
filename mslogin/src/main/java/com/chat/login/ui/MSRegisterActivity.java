package com.chat.login.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSConstants;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.LoginMenu;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.entity.MSAPPConfig;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.MSReader;
import com.chat.login.R;
import com.chat.login.databinding.ActRegisterLayoutBinding;
import com.chat.login.entity.CountryCodeEntity;
import com.chat.login.service.LoginContract;
import com.chat.login.service.LoginPresenter;

import java.util.List;
import java.util.Objects;

/**
 * 注册
 */
public class MSRegisterActivity extends MSBaseActivity<ActRegisterLayoutBinding> implements LoginContract.LoginView {
    private String code = "0086";
    private LoginPresenter presenter;
    private MSAPPConfig appConfig;

    @Override
    protected ActRegisterLayoutBinding getViewBinding() {
        return ActRegisterLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initPresenter() {
        presenter = new LoginPresenter(this);
    }

    @Override
    protected void initView() {
        msVBinding.getVCodeBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.registerBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.privacyPolicyTv.setTextColor(Theme.colorAccount);
        msVBinding.userAgreementTv.setTextColor(Theme.colorAccount);
        msVBinding.loginTv.setTextColor(Theme.colorAccount);
        msVBinding.authCheckBox.setResId(getContext(), R.mipmap.round_check2);
        msVBinding.authCheckBox.setDrawBackground(true);
        msVBinding.authCheckBox.setHasBorder(true);
        msVBinding.authCheckBox.setStrokeWidth(AndroidUtilities.dp(1));
        msVBinding.authCheckBox.setBorderColor(ContextCompat.getColor(getContext(), R.color.color999));
        msVBinding.authCheckBox.setSize(18);
        msVBinding.authCheckBox.setColor(Theme.colorAccount, ContextCompat.getColor(getContext(), R.color.white));
        msVBinding.authCheckBox.setVisibility(View.VISIBLE);
        msVBinding.authCheckBox.setEnabled(true);
        msVBinding.authCheckBox.setChecked(false, true);

        msVBinding.privacyPolicyTv.setOnClickListener(v -> showWebView(MSApiConfig.baseWebUrl + "privacy_policy.html"));
        msVBinding.userAgreementTv.setOnClickListener(v -> showWebView(MSApiConfig.baseWebUrl + "user_agreement.html"));
        msVBinding.registerAppTv.setText(String.format(getString(R.string.register_app), getString(R.string.app_name)));
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
                    msVBinding.getVCodeBtn.setAlpha(1f);
                    msVBinding.getVCodeBtn.setEnabled(true);
                } else {
                    msVBinding.getVCodeBtn.setEnabled(false);
                    msVBinding.getVCodeBtn.setAlpha(0.2f);
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
        msVBinding.loginTv.setOnClickListener(v -> startActivity(new Intent(this, MSLoginActivity.class)));
        msVBinding.chooseCodeTv.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChooseAreaCodeActivity.class);
            intentActivityResultLauncher.launch(intent);
        });
        msVBinding.registerBtn.setOnClickListener(v -> {
            if (!msVBinding.authCheckBox.isChecked()) {
                showToast(R.string.agree_auth_tips);
                return;
            }

            String phone = Objects.requireNonNull(msVBinding.nameEt.getText()).toString();
            String smsCode = Objects.requireNonNull(msVBinding.verfiEt.getText()).toString();
            String pwd = Objects.requireNonNull(msVBinding.pwdEt.getText()).toString();
            String inviteCode = Objects.requireNonNull(msVBinding.inviteCodeTv.getText()).toString();
            if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(smsCode) && !TextUtils.isEmpty(pwd)) {
                if (pwd.length() < 6 || pwd.length() > 16) {
                    showSingleBtnDialog(getString(R.string.pwd_length_error));
                } else {
                    if (appConfig != null && appConfig.register_invite_on == 1 && TextUtils.isEmpty(inviteCode)) {
                        showSingleBtnDialog(getString(R.string.invite_code_not_null));
                        return;
                    }
                    loadingPopup.show();
                    presenter.registerApp(smsCode, code, "", phone, pwd, inviteCode);
                }
            }
        });
        msVBinding.getVCodeBtn.setOnClickListener(v -> {
            String phone = Objects.requireNonNull(msVBinding.nameEt.getText()).toString();
            if (!TextUtils.isEmpty(phone)) {
                if (code.equals("0086") && msVBinding.nameEt.getText().toString().length() != 11) {
                    showSingleBtnDialog(getString(R.string.phone_error));
                    return;
                }
                presenter.registerCode(code, phone);
            }
        });

        msVBinding.myTv.setOnClickListener(view1 -> msVBinding.authCheckBox.setChecked(!msVBinding.authCheckBox.isChecked(), true));
        msVBinding.authCheckBox.setOnClickListener(view1 -> msVBinding.authCheckBox.setChecked(!msVBinding.authCheckBox.isChecked(), true));
    }

    @Override
    protected void initListener() {
        msVBinding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                msVBinding.pwdEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                msVBinding.pwdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            msVBinding.pwdEt.setSelection(Objects.requireNonNull(msVBinding.pwdEt.getText()).length());
        });
    }

    @Override
    protected void initData() {
        MSCommonModel.getInstance().getAppConfig((code, msg, msappConfig) -> {
            if (code == HttpResponseCode.success) {
                appConfig = msappConfig;
                if (appConfig != null && appConfig.register_invite_on == 1) {
                    msVBinding.inviteCodeTv.setHint(R.string.input_invite_code_must);
                    msVBinding.inviteLayout.setVisibility(View.VISIBLE);
                    msVBinding.inviteLineView.setVisibility(View.VISIBLE);
                } else {
                    msVBinding.inviteCodeTv.setHint(R.string.input_invite_code_not_must);
                    msVBinding.inviteLayout.setVisibility(View.GONE);
                    msVBinding.inviteLineView.setVisibility(View.GONE);
                }
            } else {
                showToast(msg);
            }
        });
    }

    private void checkStatus() {
        String phone = Objects.requireNonNull(msVBinding.nameEt.getText()).toString();
        String smsCode = Objects.requireNonNull(msVBinding.verfiEt.getText()).toString();
        String pwd = Objects.requireNonNull(msVBinding.pwdEt.getText()).toString();
        if (!TextUtils.isEmpty(phone) && !TextUtils.isEmpty(smsCode) && !TextUtils.isEmpty(pwd)) {
            msVBinding.registerBtn.setAlpha(1f);
            msVBinding.registerBtn.setEnabled(true);
        } else {
            msVBinding.registerBtn.setAlpha(0.2f);
            msVBinding.registerBtn.setEnabled(false);
        }
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

    @Override
    public void loginResult(UserInfoEntity userInfoEntity) {
        loadingPopup.dismiss();
        SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.pwdEt);
        hideLoading();

        if (TextUtils.isEmpty(userInfoEntity.name)) {
            Intent intent = new Intent(this, PerfectUserInfoActivity.class);
            startActivity(intent);
            finish();
        } else {
            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                List<LoginMenu> list = EndpointManager.getInstance().invokes(EndpointCategory.loginMenus, null);
                if (MSReader.isNotEmpty(list)) {
                    for (LoginMenu menu : list) {
                        if (menu.iMenuClick != null) menu.iMenuClick.onClick();
                    }
                }
                finish();
            }, 500);
        }
    }

    @Override
    public void setCountryCode(List<CountryCodeEntity> list) {

    }

    @Override
    public void setRegisterCodeSuccess(int code, String msg, int exist) {
        if (code == HttpResponseCode.success) {
            if (exist == 1) {
                showSingleBtnDialog(getString(R.string.account_exist));
            } else {
                msVBinding.nameEt.setEnabled(false);
                presenter.startTimer();
            }
        } else {
            showToast(msg);
        }
    }

    @Override
    public void setLoginFail(int code, String uid, String phone) {

    }

    @Override
    public void setSendCodeResult(int code, String msg) {

    }

    @Override
    public void setResetPwdResult(int code, String msg) {
    }

    @Override
    public Button getVerfiCodeBtn() {
        return msVBinding.getVCodeBtn;
    }

    @Override
    public EditText getNameEt() {
        return msVBinding.nameEt;
    }

    @Override
    public void showError(String msg) {
        showSingleBtnDialog(msg);
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
