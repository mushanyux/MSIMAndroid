package com.chat.login.ui;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.LoginMenu;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.glide.ChooseMimeType;
import com.chat.base.glide.ChooseResult;
import com.chat.base.glide.GlideUtils;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.MSReader;
import com.chat.login.R;
import com.chat.login.databinding.ActPerfectUserInfoLayoutBinding;
import com.chat.login.service.LoginModel;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.List;
import java.util.Objects;

/**
 * 完善个人资料
 */
public class PerfectUserInfoActivity extends MSBaseActivity<ActPerfectUserInfoLayoutBinding> {

    String path;

    @Override
    protected ActPerfectUserInfoLayoutBinding getViewBinding() {
        return ActPerfectUserInfoLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.mslogin_perfect_userinfo);
    }

    @Override
    protected void initView() {
        msVBinding.avatarView.setSize(120);
        msVBinding.avatarView.setStrokeWidth(0);
        msVBinding.avatarView.imageView.setImageResource(R.mipmap.icon_default_header);
    }

    @Override
    protected void initListener() {
        msVBinding.sureBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.avatarView.setOnClickListener(v -> chooseIMG());
        msVBinding.sureBtn.setOnClickListener(v -> {

            if (TextUtils.isEmpty(path)) {
                showToast(R.string.mslogin_must_upload_header);
                return;
            }
            if (!checkEditInputIsEmpty(msVBinding.nameEt, R.string.nickname_not_null)) {
                loadingPopup.show();
                LoginModel.getInstance().updateUserInfo("name", Objects.requireNonNull(msVBinding.nameEt.getText()).toString(), (code, msg) -> {
                    if (code == HttpResponseCode.success) {
                        UserInfoEntity userInfoEntity = MSConfig.getInstance().getUserInfo();
                        userInfoEntity.name = msVBinding.nameEt.getText().toString();
                        MSConfig.getInstance().saveUserInfo(userInfoEntity);
                        MSConfig.getInstance().setUserName(msVBinding.nameEt.getText().toString());
                        List<LoginMenu> list = EndpointManager.getInstance().invokes(EndpointCategory.loginMenus, null);
                        if (MSReader.isNotEmpty(list)) {
                            for (LoginMenu menu : list) {
                                if (menu.iMenuClick != null)
                                    menu.iMenuClick.onClick();
                            }
                        }
                        loadingPopup.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

        });
    }

    private void chooseIMG() {
        GlideUtils.getInstance().chooseIMG(this, 1, true, ChooseMimeType.img, false, new GlideUtils.ISelectBack() {
            @Override
            public void onBack(List<ChooseResult> paths) {
                if (MSReader.isNotEmpty(paths)) {
                    path = paths.get(0).path;
                    LoginModel.getInstance().uploadAvatar(path, code -> {
                        if (code == HttpResponseCode.success) {
                            GlideUtils.getInstance().showAvatarImg(PerfectUserInfoActivity.this, MSConfig.getInstance().getUid(), MSChannelType.PERSONAL, "", msVBinding.avatarView.imageView);
                            msVBinding.coverIv.setVisibility(View.GONE);
                        }
                    });

                }
            }

            @Override
            public void onCancel() {

            }
        });
    }
}
