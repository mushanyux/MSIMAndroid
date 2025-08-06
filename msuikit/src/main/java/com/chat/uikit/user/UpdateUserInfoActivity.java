package com.chat.uikit.user;

import android.content.Intent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.StringUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActUpdateUserInfoLayoutBinding;
import com.chat.uikit.user.service.UserModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.Objects;

/**
 * 修改用户资料
 */
public class UpdateUserInfoActivity extends MSBaseActivity<ActUpdateUserInfoLayoutBinding> {
    private String oldStr;
    private int updateType;
    private TextView titleTv;

    @Override
    protected ActUpdateUserInfoLayoutBinding getViewBinding() {
        return ActUpdateUserInfoLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        this.titleTv = titleTv;
    }

    @Override
    protected void initPresenter() {
        updateType = getIntent().getIntExtra("updateType", 1);
        oldStr = getIntent().getStringExtra("oldStr");
        if (!TextUtils.isEmpty(oldStr)) {
            oldStr = oldStr.replaceAll("\n","");
        }
    }

    @Override
    protected void initView() {
        msVBinding.descTv.setText(String.format(getString(R.string.single_update_id), getString(R.string.app_name)));
    }

    @Override
    protected String getRightTvText(TextView textView) {
        return getString(R.string.complete);
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        String key = "";
        if (updateType == 1)
            key = "name";
        else if (updateType == 2)
            key = "short_no";
        showTitleRightLoading();
        UserModel.getInstance().updateUserInfo(key, Objects.requireNonNull(msVBinding.contentEt.getText()).toString(), (code, msg) -> {
            if (code == HttpResponseCode.success) {
                UserInfoEntity userInfoEntity = MSConfig.getInstance().getUserInfo();
                if (updateType == 1) {
                    userInfoEntity.name = msVBinding.contentEt.getText().toString();
                } else if (updateType == 2) {
                    userInfoEntity.short_no = msVBinding.contentEt.getText().toString();
                    userInfoEntity.short_status = 1;
                }
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL);
                if (channel != null && updateType == 1) {
                    channel.channelName = userInfoEntity.name;
                    MSIM.getInstance().getChannelManager().saveOrUpdateChannel(channel);
                }
                MSConfig.getInstance().saveUserInfo(userInfoEntity);
                Intent intent = new Intent();
                intent.putExtra("updateType", updateType);
                intent.putExtra("result", msVBinding.contentEt.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                hideTitleRightLoading();
                showToast(msg);
            }
        });
    }

    @Override
    protected void initListener() {
        msVBinding.contentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String content = editable.toString();
                if (content.contains("\n")){
                    content = content.replace("\n", "");
                    msVBinding.contentEt.setText(content);
                    msVBinding.contentEt.setSelection(content.length());
                }
                if (TextUtils.isEmpty(content) || content.equals(oldStr)) {
                    hideTitleRightView();
                } else showTitleRightView();


            }
        });

    }

    @Override
    protected void initData() {
        super.initData();
        if (updateType == 1) {
            titleTv.setText(R.string.update_name);
            msVBinding.descTv.setVisibility(View.GONE);
        } else if (updateType == 2) {
            titleTv.setText(String.format(getString(R.string.update_app_id), getString(R.string.app_name)));
            msVBinding.descTv.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(oldStr)) {
            msVBinding.contentEt.setText(oldStr);
            msVBinding.contentEt.setSelection(oldStr.length());
        }

        msVBinding.contentEt.setFilters(new InputFilter[]{StringUtils.getInputFilter(10)});
        SoftKeyboardUtils.getInstance().showSoftKeyBoard(UpdateUserInfoActivity.this, msVBinding.contentEt);
    }

    @Override
    public void finish() {
        super.finish();
        SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);
    }


}
