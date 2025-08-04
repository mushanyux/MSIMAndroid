package com.chat.uikit.chat;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.chat.base.act.MSWebViewActivity;
import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSSystemAccount;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatSettingCellMenu;
import com.chat.base.endpoint.entity.PrivacyMessageMenu;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.chat.search.MessageRecordActivity;
import com.chat.uikit.contacts.ChooseContactsActivity;
import com.chat.uikit.contacts.service.FriendModel;
import com.chat.uikit.databinding.ActChatPersonalLayoutBinding;
import com.chat.uikit.message.MsgModel;
import com.chat.uikit.user.UserDetailActivity;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;

/**
 * 个人会话资料页面
 */
public class ChatPersonalActivity extends MSBaseActivity<ActChatPersonalLayoutBinding> {
    private String channelId;
    private MSChannel channel;

    @Override
    protected ActChatPersonalLayoutBinding getViewBinding() {
        return ActChatPersonalLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.chat_info);
    }

    @Override
    protected void initPresenter() {
        channelId = getIntent().getStringExtra("channelId");
    }

    @Override
    protected void initView() {
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);
        View view = (View) EndpointManager.getInstance().invoke("msg_remind_view", new ChatSettingCellMenu(channelId, MSChannelType.PERSONAL, msVBinding.msgRemindLayout));
        if (view != null) {
            msVBinding.msgRemindLayout.removeAllViews();
            msVBinding.msgRemindLayout.addView(view);
        }

        View msgReceiptView = (View) EndpointManager.getInstance().invoke("msg_receipt_view", new ChatSettingCellMenu(channelId, MSChannelType.PERSONAL, msVBinding.msgSettingLayout));
        if (msgReceiptView != null) {
            msVBinding.msgSettingLayout.removeAllViews();
            msVBinding.msgSettingLayout.addView(msgReceiptView);
        }
        View msgPrivacyLayout = (View) EndpointManager.getInstance().invoke("chat_setting_msg_privacy", new ChatSettingCellMenu(channelId, MSChannelType.PERSONAL, msVBinding.msgSettingLayout));
        if (msgPrivacyLayout != null) {
            msVBinding.msgSettingLayout.addView(msgPrivacyLayout);
        }

        View chatPwdView = (View) EndpointManager.getInstance().invoke("chat_pwd_view", new ChatSettingCellMenu(channelId, MSChannelType.PERSONAL, msVBinding.chatPwdView));
        if (chatPwdView != null) {
            msVBinding.chatPwdView.addView(chatPwdView);
        }

    }

    @Override
    protected void initListener() {
        EndpointManager.getInstance().setMethod("chat_personal_activity", EndpointCategory.msExitChat, object -> {
            if (object != null) {
                MSChannel channel = (MSChannel) object;
                if (channelId.equals(channel.channelID) && channel.channelType == MSChannelType.PERSONAL) {
                    finish();
                }
            }
            return null;
        });
        SingleClickUtil.onSingleClick(msVBinding.findContentLayout, v -> {
            Intent intent = new Intent(this, MessageRecordActivity.class);
            intent.putExtra("channel_id", channelId);
            intent.putExtra("channel_type", MSChannelType.PERSONAL);
            startActivity(intent);
        });
        //免打扰
        msVBinding.muteSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed()) {
                FriendModel.getInstance().updateUserSetting(channelId, "mute", b ? 1 : 0, (code, msg) -> {
                    if (code != HttpResponseCode.success) {
                        msVBinding.muteSwitchView.setChecked(!b);
                        showToast(msg);
                    }
                });
            }
        });
        //置顶
        msVBinding.stickSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed())
                FriendModel.getInstance().updateUserSetting(channelId, "top", b ? 1 : 0, (code, msg) -> {
                    if (code != HttpResponseCode.success) {
                        msVBinding.stickSwitchView.setChecked(!b);
                        showToast(msg);
                    }
                });
        });
        msVBinding.clearChatMsgLayout.setOnClickListener(v -> {
            String content = String.format(getString(R.string.clear_history_tip), channel == null ? "" : channel.channelName);

            Object object = EndpointManager.getInstance().invoke("is_register_msg_privacy_module", null);
            if (object instanceof PrivacyMessageMenu) {
                String showName = "";
                if (channel != null) {
                    if (TextUtils.isEmpty(channel.channelRemark)) {
                        showName = channel.channelName;
                    } else {
                        showName = channel.channelRemark;
                    }
                }
                String checkBoxText = String.format(getString(R.string.str_delete_message_also_to), showName);
                MSDialogUtils.getInstance().showCheckBoxDialog(this, getString(R.string.clear_history), content, checkBoxText, true, "", getString(R.string.base_delete), 0, ContextCompat.getColor(this, R.color.red), (index, isChecked) -> {
                    if (index == 1) {
                        if (isChecked) {
                            ((PrivacyMessageMenu) object).getIClick().clearChannelMsg(channelId, MSChannelType.PERSONAL);
                        } else {
                            MsgModel.getInstance().offsetMsg(channelId, MSChannelType.PERSONAL, null);
                            MSIM.getInstance().getMsgManager().clearWithChannel(channelId, MSChannelType.PERSONAL);
                            showToast(R.string.cleared);
                        }
                    }
                });
                return;
            }
            MSDialogUtils.getInstance().showDialog(this, getString(R.string.clear_history), content, true, "", getString(R.string.base_delete), 0, ContextCompat.getColor(this, R.color.red), new MSDialogUtils.IClickListener() {
                @Override
                public void onClick(int index) {
                    if (index == 1) {
                        MsgModel.getInstance().offsetMsg(channelId, MSChannelType.PERSONAL, null);
                        MSIM.getInstance().getMsgManager().clearWithChannel(channelId, MSChannelType.PERSONAL);
                        showToast(R.string.cleared);
                    }
                }
            });
        });
        SingleClickUtil.onSingleClick(msVBinding.addIv, view1 -> {
            Intent intent = new Intent(ChatPersonalActivity.this, ChooseContactsActivity.class);
            intent.putExtra("unSelectUids", channelId);
            intent.putExtra("isIncludeUids", true);
            chooseCardResultLac.launch(intent);
        });
        SingleClickUtil.onSingleClick(msVBinding.avatarView, view1 -> {
            Intent intent = new Intent(ChatPersonalActivity.this, UserDetailActivity.class);
            intent.putExtra("uid", channelId);
            startActivity(intent);
        });
        SingleClickUtil.onSingleClick(msVBinding.reportLayout, view1 -> {
            Intent intent = new Intent(this, MSWebViewActivity.class);
            intent.putExtra("channelType", MSChannelType.PERSONAL);
            intent.putExtra("channelID", channelId);
            intent.putExtra("url", MSApiConfig.baseWebUrl + "report.html");
            startActivity(intent);
        });
    }

    @Override
    protected void initData() {
        super.initData();

        if (MSSystemAccount.isSystemAccount(channelId)) {
            Intent intent = new Intent(this, UserDetailActivity.class);
            intent.putExtra("uid", channelId);
            startActivity(intent);
            finish();
            return;
        }
        channel = MSIM.getInstance().getChannelManager().getChannel(channelId, MSChannelType.PERSONAL);
        if (channel != null) {
            msVBinding.avatarView.showAvatar(channel.channelID, channel.channelType, false);
            msVBinding.nameTv.setText(TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark);
            msVBinding.muteSwitchView.setChecked(channel.mute == 1);
            msVBinding.stickSwitchView.setChecked(channel.top == 1);

        }
    }

    ActivityResultLauncher<Intent> chooseCardResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            finish();
        }
    });

    @Override
    public void finish() {
        super.finish();
        EndpointManager.getInstance().remove("chat_personal_activity");
    }
}
