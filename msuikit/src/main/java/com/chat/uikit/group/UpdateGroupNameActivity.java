package com.chat.uikit.group;

import android.text.TextUtils;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActUpdateGroupNameLayoutBinding;
import com.chat.uikit.group.service.GroupModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.Objects;

/**
 * 修改群名称
 */
public class UpdateGroupNameActivity extends MSBaseActivity<ActUpdateGroupNameLayoutBinding> {

    String groupNo;
    MSChannel channel;

    @Override
    protected ActUpdateGroupNameLayoutBinding getViewBinding() {
        return ActUpdateGroupNameLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.group_card);
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        String name = Objects.requireNonNull(msVBinding.nameEt.getText()).toString();

        if (channel == null || TextUtils.isEmpty(channel.channelID) || TextUtils.isEmpty(name))
            return;

        if (TextUtils.equals(name, channel.channelName)) {
            finish();
        }
        channel.channelName = name;

        showTitleRightLoading();
        GroupModel.getInstance().updateGroupInfo(channel.channelID, "name", name, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                MSIM.getInstance().getChannelManager().updateName(channel.channelID, MSChannelType.GROUP, name);
                finish();
            } else {
                hideTitleRightLoading();
                showToast(msg);
            }
        });
    }

    @Override
    protected String getRightTvText(TextView textView) {
        return getString(R.string.save);
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView() {
        groupNo = getIntent().getStringExtra("groupNo");
        if (groupNo != null) {
            channel = MSIM.getInstance().getChannelManager().getChannel(groupNo, MSChannelType.GROUP);
            msVBinding.nameEt.setText(channel.channelName);
            msVBinding.nameEt.setSelection(channel.channelName.length());
        }
        SoftKeyboardUtils.getInstance().showSoftKeyBoard(UpdateGroupNameActivity.this, msVBinding.nameEt);
    }

    @Override
    protected void initListener() {

    }

    @Override
    public void finish() {
        super.finish();
        SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.nameEt);
    }
}
