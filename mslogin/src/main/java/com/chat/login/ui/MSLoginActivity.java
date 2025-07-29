package com.chat.login.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSConstants;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.LoginMenu;
import com.chat.base.endpoint.entity.OtherLoginResultMenu;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.entity.MSAPPConfig;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.login.R;
import com.chat.login.databinding.ActLoginLayoutBinding;
import com.chat.login.entity.CountryCodeEntity;
import com.chat.login.service.LoginContract;
import com.chat.login.service.LoginPresenter;

import java.util.List;
import java.util.Objects;

/**
 * 登录
 */
public class MSLoginActivity extends MSBaseActivity<ActLoginLayoutBinding> implements LoginContract.LoginView {
    private MSAPPConfig msappConfig;
    private String code = "0086";
    private LoginPresenter loginPresenter;

    @Override
    protected ActLoginLayoutBinding getViewBinding() {
        return ActLoginLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initPresenter() {
        loginPresenter = new LoginPresenter(this);
    }

    @Override
    protected void initView() {
        msVBinding.loginBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.privacyPolicyTv.setTextColor(Theme.colorAccount);
        msVBinding.userAgreementTv.setTextColor(Theme.colorAccount);
        msVBinding.registerTv.setTextColor(Theme.colorAccount);
        msVBinding.forgetPwdTv.setTextColor(Theme.colorAccount);
        msVBinding.checkbox.setResId(getContext(), R.mipmap.round_check2);
        msVBinding.checkbox.setDrawBackground(true);
        msVBinding.checkbox.setHasBorder(true);
        msVBinding.checkbox.setStrokeWidth(AndroidUtilities.dp(1));
        msVBinding.checkbox.setBorderColor(ContextCompat.getColor(getContext(), R.color.color999));
        msVBinding.checkbox.setSize(18);
        msVBinding.checkbox.setColor(Theme.colorAccount, ContextCompat.getColor(getContext(), R.color.white));
        msVBinding.checkbox.setVisibility(View.VISIBLE);
        msVBinding.checkbox.setEnabled(true);
        msVBinding.checkbox.setChecked(false, true);
        int from = getIntent().getIntExtra("from", 0);
        if (from == 1 || from == 2) {
            String content = getString(R.string.ms_ban);
            if (from == 1) {
                content = getString(R.string.other_device_login);
            }
            MSDialogUtils.getInstance().showSingleBtnDialog(this, "", content, getString(R.string.sure), index -> {
            });
        }
        UserInfoEntity userInfoEntity = MSConfig.getInstance().getUserInfo();
        if (userInfoEntity != null) {
            if (!TextUtils.isEmpty(userInfoEntity.phone)) {
                msVBinding.nameEt.setText(userInfoEntity.phone);
                msVBinding.nameEt.setSelection(userInfoEntity.phone.length());

                String zone = MSConfig.getInstance().getUserInfo().zone;
                if (!TextUtils.isEmpty(zone)) {
                    code = zone;
                    String codeName = code.substring(2);
                    msVBinding.codeTv.setText(String.format("+%s", codeName));
                }
            }
        }
        msVBinding.loginTitleTv.setText(String.format(getString(R.string.login_title), getString(R.string.app_name)));
        msVBinding.privacyPolicyTv.setOnClickListener(v -> showWebView(MSApiConfig.baseWebUrl + "privacy_policy.html"));
        msVBinding.userAgreementTv.setOnClickListener(v -> showWebView(MSApiConfig.baseWebUrl + "user_agreement.html"));
        //  EndpointManager.getInstance().invoke("other_login_view", new OtherLoginViewMenu(this, msVBinding.otherView));
    }

    @Override
    public boolean supportSlideBack() {
        return false;
    }

    @Override
    protected void initListener() {
        msVBinding.myTv.setOnClickListener(view1 -> msVBinding.checkbox.setChecked(!msVBinding.checkbox.isChecked(), true));
        msVBinding.checkbox.setOnClickListener(view1 -> msVBinding.checkbox.setChecked(!msVBinding.checkbox.isChecked(), true));

        msVBinding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                msVBinding.pwdEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                msVBinding.pwdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            msVBinding.pwdEt.setSelection(Objects.requireNonNull(msVBinding.pwdEt.getText()).length());
        });
        msVBinding.loginBtn.setOnClickListener(v -> {
            if (checkEditInputIsEmpty(msVBinding.nameEt, R.string.name_not_null)) return;
            if (checkEditInputIsEmpty(msVBinding.pwdEt, R.string.pwd_not_null)) return;
            if (code.equals("0086") && Objects.requireNonNull(msVBinding.nameEt.getText()).toString().length() != 11) {
                showSingleBtnDialog(getString(R.string.phone_error));
                return;
            }
            if (!msVBinding.checkbox.isChecked()) {
                showSingleBtnDialog(getString(R.string.agree_auth_tips));
                return;
            }
            if (Objects.requireNonNull(msVBinding.pwdEt.getText()).toString().length() < 6 || msVBinding.pwdEt.getText().toString().length() > 16) {
                showSingleBtnDialog(getString(R.string.pwd_length_error));
                return;
            }
            loadingPopup.show();
            loadingPopup.setTitle(getString(R.string.logging_in));
            String name = Objects.requireNonNull(msVBinding.nameEt.getText()).toString();
            loginPresenter.login(code + name, msVBinding.pwdEt.getText().toString());
        });
        SingleClickUtil.onSingleClick(msVBinding.registerTv, v -> startActivity(new Intent(this, MSRegisterActivity.class)));
        SingleClickUtil.onSingleClick(msVBinding.chooseCodeTv, v -> {
            Intent intent = new Intent(this, ChooseAreaCodeActivity.class);
            intentActivityResultLauncher.launch(intent);
        });
        SingleClickUtil.onSingleClick(msVBinding.forgetPwdTv, v -> {
            Intent intent = new Intent(this, MSResetLoginPwdActivity.class);
            intent.putExtra("canEditPhone", true);
            startActivity(intent);
        });

        EndpointManager.getInstance().setMethod("other_login_result", object -> {
            OtherLoginResultMenu menu = (OtherLoginResultMenu) object;
            if (menu.getCode() == 0) {
                loginResult(menu.getUserInfoEntity());
            } else {
                setLoginFail(menu.getCode(), menu.getUserInfoEntity().uid, menu.getUserInfoEntity().phone);
            }
            return null;
        });
        msVBinding.baseUrlTv.setOnClickListener(v -> {
            if (msappConfig == null || msappConfig.can_modify_api_url == 0) {
                return;
            }
            String url = MSSharedPreferencesUtil.getInstance().getSP("api_base_url", "");
            MSDialogUtils.getInstance().showInputDialog(this, getString(R.string.update_api), getString(R.string.update_api_content), url, getString(R.string.update_api_ip), 100, text -> {
                if (!TextUtils.isEmpty(text)) {
                    if (!text.toLowerCase().startsWith("http")) {
                        text = "http://" + text;
                    }
                    MSSharedPreferencesUtil.getInstance().putSP("api_base_url", text);
                    showBaseUrl();
                    EndpointManager.getInstance().invoke("update_base_url", text);
                }
            });
        });
        msVBinding.resetTv.setOnClickListener(view -> {
            MSSharedPreferencesUtil.getInstance().putSP("api_base_url", "");
            EndpointManager.getInstance().invoke("update_base_url", "");
        });
        showBaseUrl();
    }

    @Override
    protected void initData() {
        super.initData();
        MSCommonModel.getInstance().getAppConfig((code, msg, msappConfig) -> {
            this.msappConfig = msappConfig;
            if (msappConfig != null && msappConfig.can_modify_api_url == 1) {
                msVBinding.settingLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showBaseUrl() {
        String apiURL = MSSharedPreferencesUtil.getInstance().getSP("api_base_url");
        if (!TextUtils.isEmpty(apiURL)) {
            msVBinding.baseUrlTv.setText(apiURL);
            msVBinding.resetTv.setVisibility(View.VISIBLE);
        } else {
            msVBinding.baseUrlTv.setText(R.string.update_api);
            msVBinding.resetTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void loginResult(UserInfoEntity userInfoEntity) {
        SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.pwdEt);

        if (TextUtils.isEmpty(userInfoEntity.name)) {
            Intent intent = new Intent(this, PerfectUserInfoActivity.class);
            startActivity(intent);
            finish();
        } else {
            hideLoading();
            new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> {
                List<LoginMenu> list = EndpointManager.getInstance().invokes(EndpointCategory.loginMenus, null);
                if (MSReader.isNotEmpty(list)) {
                    for (LoginMenu menu : list) {
                        if (menu.iMenuClick != null) menu.iMenuClick.onClick();
                    }
                }
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
        Intent intent = new Intent(this, LoginAuthActivity.class);
        intent.putExtra("phone", phone);
        intent.putExtra("uid", uid);
        startActivity(intent);
    }

    @Override
    public void setSendCodeResult(int code, String msg) {

    }

    @Override
    public void setResetPwdResult(int code, String msg) {

    }

    @Override
    public Button getVerfiCodeBtn() {
        return null;
    }

    @Override
    public EditText getNameEt() {
        return null;
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
    public void finish() {
        super.finish();
        EndpointManager.getInstance().remove("other_login_result");
    }
}
