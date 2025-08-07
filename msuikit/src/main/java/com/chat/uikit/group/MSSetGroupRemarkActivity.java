package com.chat.uikit.group;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.StringUtils;
import com.chat.uikit.databinding.ActUpdateGroupRemarkLayoutBinding;
import com.chat.uikit.group.service.GroupModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;

/**
 * 修改群备注
 */
public class MSSetGroupRemarkActivity extends MSBaseActivity<ActUpdateGroupRemarkLayoutBinding> {
    private String groupNo;
    MSChannel channel;

    @Override
    protected ActUpdateGroupRemarkLayoutBinding getViewBinding() {
        return ActUpdateGroupRemarkLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText("");
    }

    @Override
    protected void initPresenter() {
        groupNo = getIntent().getStringExtra("groupNo");
    }

    @Override
    protected void initView() {
        msVBinding.saveBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.enter.setTextColor(Theme.colorAccount);
        msVBinding.remarkEt.setFilters(new InputFilter[]{StringUtils.getInputFilter(40)});
        channel = MSIM.getInstance().getChannelManager().getChannel(groupNo, MSChannelType.GROUP);
        if (channel != null) {
            if (!TextUtils.isEmpty(channel.channelRemark)) {
                msVBinding.remarkEt.setText(channel.channelRemark);
                msVBinding.remarkEt.setSelection(channel.channelRemark.length());
            }
            msVBinding.avatarView.showAvatar(channel);
            msVBinding.groupNameTv.setText(channel.channelName);
        }
        SoftKeyboardUtils.getInstance().showSoftKeyBoard(MSSetGroupRemarkActivity.this, msVBinding.remarkEt);
    }

    @Override
    protected void initListener() {
        msVBinding.enter.setOnClickListener(v -> {
            msVBinding.remarkEt.setText(channel.channelName);
            msVBinding.remarkEt.setSelection(channel.channelName.length());
        });
        msVBinding.groupNameTv.setMaxWidth(AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(135));
        msVBinding.remarkEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ((TextUtils.isEmpty(s.toString()) && TextUtils.isEmpty(channel.channelRemark)) || channel.channelRemark.equals(s.toString())) {
                    msVBinding.saveBtn.setAlpha(0.2f);
                    msVBinding.saveBtn.setEnabled(false);
                } else {
                    msVBinding.saveBtn.setAlpha(1f);
                    msVBinding.saveBtn.setEnabled(true);
                }
            }
        });
        msVBinding.saveBtn.setOnClickListener(v -> {
            String remark = msVBinding.remarkEt.getText().toString();
            GroupModel.getInstance().updateGroupSetting(groupNo, "remark", remark, (code, msg) -> {
                if (code != HttpResponseCode.success) {
                    showToast(msg);
                } else {
                    finish();
                }
            });
        });
    }

    @Override
    public void finish() {
        super.finish();
        SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.remarkEt);
    }
}
