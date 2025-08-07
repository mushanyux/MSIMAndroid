package com.chat.uikit.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.chat.base.act.MSWebViewActivity;
import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSConfig;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatSettingCellMenu;
import com.chat.base.endpoint.entity.PrivacyMessageMenu;
import com.chat.base.entity.ChannelInfoEntity;
import com.chat.base.entity.MSChannelCustomerExtras;
import com.chat.base.entity.MSGroupType;
import com.chat.base.msgitem.MSChannelMemberRole;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSToastUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.base.views.FullyGridLayoutManager;
import com.chat.uikit.Const;
import com.chat.uikit.R;
import com.chat.uikit.chat.search.MessageRecordActivity;
import com.chat.uikit.contacts.ChooseContactsActivity;
import com.chat.uikit.databinding.ActGroupDetailLayoutBinding;
import com.chat.uikit.group.adapter.GroupMemberAdapter;
import com.chat.uikit.group.service.GroupContract;
import com.chat.uikit.group.service.GroupModel;
import com.chat.uikit.group.service.GroupPresenter;
import com.chat.uikit.message.MsgModel;
import com.chat.uikit.user.UserDetailActivity;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelExtras;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 群组详情
 */
public class GroupDetailActivity extends MSBaseActivity<ActGroupDetailLayoutBinding> implements GroupContract.GroupView {
    private String groupNo;
    private GroupMemberAdapter groupMemberAdapter;
    private GroupPresenter groupPresenter;
    private int memberRole;
    private MSChannel groupChannel;
    private int groupType = 0;
    private TextView titleTv;
    private boolean isResetMembers = false;

    @Override
    protected ActGroupDetailLayoutBinding getViewBinding() {
        return ActGroupDetailLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        this.titleTv = titleTv;
        titleTv.setText(R.string.chat_info);
    }

    @Override
    protected void initPresenter() {
        groupNo = getIntent().getStringExtra("channelId");
        groupPresenter = new GroupPresenter(this);
    }

    @Override
    protected void initView() {
        FullyGridLayoutManager layoutManager = new FullyGridLayoutManager(this, 5);
        msVBinding.userRecyclerView.setLayoutManager(layoutManager);
        groupMemberAdapter = new GroupMemberAdapter(new ArrayList<>());
        msVBinding.userRecyclerView.setAdapter(groupMemberAdapter);
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);
        View view = (View) EndpointManager.getInstance().invoke("msg_remind_view", new ChatSettingCellMenu(groupNo, MSChannelType.GROUP, msVBinding.msgRemindLayout));
        if (view != null) {
            msVBinding.msgRemindLayout.removeAllViews();
            msVBinding.msgRemindLayout.addView(view);
        }


        View msgReceiptView = (View) EndpointManager.getInstance().invoke("msg_receipt_view", new ChatSettingCellMenu(groupNo, MSChannelType.GROUP, msVBinding.msgSettingLayout));
        if (msgReceiptView != null) {
            msVBinding.msgSettingLayout.removeAllViews();
            msVBinding.msgSettingLayout.addView(msgReceiptView);
        }

        View msgPrivacyLayout = (View) EndpointManager.getInstance().invoke("chat_setting_msg_privacy", new ChatSettingCellMenu(groupNo, MSChannelType.GROUP, msVBinding.msgSettingLayout));
        if (msgPrivacyLayout != null) {
            msVBinding.msgSettingLayout.addView(msgPrivacyLayout);
        }

        View groupAvatarLayout = (View) EndpointManager.getInstance().invoke("group_avatar_view", new ChatSettingCellMenu(groupNo, MSChannelType.GROUP, msVBinding.groupAvatarLayout));
        if (groupAvatarLayout != null) {
            msVBinding.groupAvatarLayout.addView(groupAvatarLayout);
        }

        View groupManagerLayout = (View) EndpointManager.getInstance().invoke("group_manager_view", new ChatSettingCellMenu(groupNo, MSChannelType.GROUP, msVBinding.groupManageLayout));
        if (groupManagerLayout != null) {
            msVBinding.groupManageLayout.addView(groupManagerLayout);
        }
        View chatPwdView = (View) EndpointManager.getInstance().invoke("chat_pwd_view", new ChatSettingCellMenu(groupNo, MSChannelType.GROUP, msVBinding.chatPwdView));
        if (chatPwdView != null) {
            msVBinding.chatPwdView.addView(chatPwdView);
        }
    }

    @Override
    protected void initListener() {
        EndpointManager.getInstance().setMethod("group_detail", EndpointCategory.msExitChat, object -> {
            if (object != null) {
                MSChannel channel = (MSChannel) object;
                if (groupNo.equals(channel.channelID) && channel.channelType == MSChannelType.GROUP) {
                    finish();
                }
            }
            return null;
        });
        SingleClickUtil.onSingleClick(msVBinding.findContentLayout, view1 -> {
            if (groupIsEnable()) {
                Intent intent = new Intent(this, MessageRecordActivity.class);
                intent.putExtra("channel_id", groupNo);
                intent.putExtra("channel_type", MSChannelType.GROUP);
                startActivity(intent);
            }
        });
        SingleClickUtil.onSingleClick(msVBinding.remarkLayout, view1 -> {
            if (groupIsEnable()) {
                Intent intent = new Intent(GroupDetailActivity.this, MSSetGroupRemarkActivity.class);
                intent.putExtra("groupNo", groupNo);
                startActivity(intent);
            }
        });
        msVBinding.showNickSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed() && groupIsEnable()) {
                groupPresenter.updateGroupSetting(groupNo, "show_nick", b ? 1 : 0);
            }
        });
        msVBinding.saveSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed() && groupIsEnable()) {
                groupPresenter.updateGroupSetting(groupNo, "save", b ? 1 : 0);
            }
        });

        msVBinding.muteSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed() && groupIsEnable()) {
                groupPresenter.updateGroupSetting(groupNo, "mute", b ? 1 : 0);
            }
        });
        msVBinding.stickSwitchView.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isPressed() && groupIsEnable()) {
                groupPresenter.updateGroupSetting(groupNo, "top", b ? 1 : 0);
            }
        });
        groupMemberAdapter.addChildClickViewIds(R.id.handlerIv, R.id.userLayout);
        groupMemberAdapter.setOnItemChildClickListener((adapter, view1, position) -> {
            if (!groupIsEnable()) {
                return;
            }
            MSChannelMember groupMemberEntity = groupMemberAdapter.getItem(position);
            if (groupMemberEntity != null) {
                if (view1.getId() == R.id.handlerIv) {
                    //添加或删除
                    if (groupMemberEntity.memberUID.equalsIgnoreCase("-1")) {
                        //添加
                        String unSelectUidList = "";
                        List<MSChannelMember> list = MSIM.getInstance().getChannelMembersManager().getMembers(groupNo, MSChannelType.GROUP);
                        for (int i = 0, size = list.size(); i < size; i++) {
                            if (TextUtils.isEmpty(unSelectUidList)) {
                                unSelectUidList = list.get(i).memberUID;
                            } else unSelectUidList = unSelectUidList + "," + list.get(i).memberUID;
                        }

                        Intent intent = new Intent(GroupDetailActivity.this, ChooseContactsActivity.class);
                        intent.putExtra("unSelectUids", unSelectUidList);
                        intent.putExtra("isIncludeUids", false);
                        intent.putExtra("groupId", groupNo);
                        intent.putExtra("type", 1);
                        startActivity(intent);
                    } else {
                        //删除
                        Intent intent = new Intent(GroupDetailActivity.this, DeleteGroupMemberActivity.class);
                        intent.putExtra("groupId", groupNo);
                        startActivity(intent);
                    }
                } else if (view1.getId() == R.id.userLayout) {
                    Intent intent = new Intent(GroupDetailActivity.this, UserDetailActivity.class);
                    intent.putExtra("uid", groupMemberEntity.memberUID);
                    intent.putExtra("groupID", groupNo);
                    startActivity(intent);

                }
            }
        });
        SingleClickUtil.onSingleClick(msVBinding.showAllMembersTv, view1 -> {
            if (groupIsEnable()) {
                Intent intent = new Intent(this, MSAllMembersActivity.class);
                intent.putExtra("channelID", groupNo);
                intent.putExtra("channelType", MSChannelType.GROUP);
                startActivity(intent);
            }
        });
        SingleClickUtil.onSingleClick(msVBinding.reportLayout, view1 -> {
            if (groupIsEnable()) {
                Intent intent = new Intent(this, MSWebViewActivity.class);
                intent.putExtra("channelType", MSChannelType.GROUP);
                intent.putExtra("channelID", groupNo);
                intent.putExtra("url", MSApiConfig.baseWebUrl + "report.html");
                startActivity(intent);
            }
        });

        msVBinding.exitBtn.setOnClickListener(v -> MSDialogUtils.getInstance().showDialog(this, getString(R.string.delete_group), getString(R.string.exit_group_tips), true, "", getString(R.string.delete_group), 0, ContextCompat.getColor(this, R.color.red), index -> {
            if (index == 1) {
                GroupModel.getInstance().exitGroup(groupNo, (code, msg) -> {
                    if (code == HttpResponseCode.success) {
                        MSIM.getInstance().getMsgManager().clearWithChannel(groupNo, MSChannelType.GROUP);
                        MsgModel.getInstance().offsetMsg(groupNo, MSChannelType.GROUP, null);
                        MSIM.getInstance().getConversationManager().deleteWitchChannel(groupNo, MSChannelType.GROUP);
                        EndpointManager.getInstance().invokes(EndpointCategory.msExitChat, new MSChannel(groupNo, MSChannelType.GROUP));
                        finish();
                    } else showToast(msg);
                });
            }
        }));

        SingleClickUtil.onSingleClick(msVBinding.groupQrLayout, view1 -> {
            if (groupIsEnable()) {
                Intent intent = new Intent(this, GroupQrActivity.class);
                intent.putExtra("groupId", groupNo);
                startActivity(intent);
            }
        });
        msVBinding.clearChatMsgLayout.setOnClickListener(v -> {
            String showName = "";
            if (groupChannel != null) {
                if (TextUtils.isEmpty(groupChannel.channelRemark)) {
                    showName = groupChannel.channelName;
                } else {
                    showName = groupChannel.channelRemark;
                }
            }
            Object object = EndpointManager.getInstance().invoke("is_register_msg_privacy_module", null);
            MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(groupNo, MSChannelType.GROUP, MSConfig.getInstance().getUid());
            if (member != null && member.role != MSChannelMemberRole.normal) {
                if (object instanceof PrivacyMessageMenu) {
                    String checkBoxText = getString(R.string.str_delete_message_for_all);
                    MSDialogUtils.getInstance().showCheckBoxDialog(this, getString(R.string.clear_history), String.format(getString(R.string.clear_history_tip), showName), checkBoxText, true, "", getString(R.string.delete), 0, ContextCompat.getColor(this, R.color.red), new MSDialogUtils.ICheckBoxDialog() {
                        @Override
                        public void onClick(int index, boolean isChecked) {
                            if (index == 1) {
                                if (isChecked) {
                                    ((PrivacyMessageMenu) object).getIClick().clearChannelMsg(groupNo, MSChannelType.GROUP);
                                } else {
                                    MsgModel.getInstance().offsetMsg(groupNo, MSChannelType.GROUP, null);
                                    MSIM.getInstance().getMsgManager().clearWithChannel(groupNo, MSChannelType.GROUP);
                                    showToast(getString(R.string.cleared));
                                }
                            }
                        }
                    });
                    return;
                }
            }
            MSDialogUtils.getInstance().showDialog(this, getString(R.string.clear_history), String.format(getString(R.string.clear_history_tip), showName), true, "", getString(R.string.delete), 0, ContextCompat.getColor(this, R.color.red), index -> {
                if (index == 1) {
                    MsgModel.getInstance().offsetMsg(groupNo, MSChannelType.GROUP, null);
                    MSIM.getInstance().getMsgManager().clearWithChannel(groupNo, MSChannelType.GROUP);
                    showToast(getString(R.string.cleared));
                }
            });
        });
        msVBinding.inGroupNameLayout.setOnClickListener(v -> updateNameInGroupDialog());
        SingleClickUtil.onSingleClick(msVBinding.noticeLayout, view1 -> {
            if (!groupIsEnable()) return;
            String notice = "";
            if (groupChannel.localExtra != null && groupChannel.localExtra.containsKey(MSChannelExtras.notice)) {
                notice = (String) groupChannel.localExtra.get(MSChannelExtras.notice);
            }
            if (TextUtils.isEmpty(notice) && memberRole == MSChannelMemberRole.normal) {
                showSingleBtnDialog(getString(R.string.edit_group_notice));
                return;
            }
            Intent intent = new Intent(this, GroupNoticeActivity.class);
            intent.putExtra("groupNo", groupNo);
            intent.putExtra("oldNotice", notice);
            startActivity(intent);
        });
        SingleClickUtil.onSingleClick(msVBinding.groupNameLayout, view1 -> {
            if (!groupIsEnable()) return;
            if (memberRole != MSChannelMemberRole.normal) {
                Intent intent = new Intent(this, UpdateGroupNameActivity.class);
                intent.putExtra("groupNo", groupNo);
                startActivity(intent);
            } else showSingleBtnDialog(getString(R.string.edit_group_notice));
        });
        //监听频道改变通知
        MSIM.getInstance().getChannelManager().addOnRefreshChannelInfo("group_detail_refresh_channel", (channel, isEnd) -> {
            if (channel != null) {
                if (channel.channelID.equalsIgnoreCase(groupNo) && channel.channelType == MSChannelType.GROUP) {
                    //同一个会话
                    groupChannel = channel;
                    setData();
                    setNotice();
                }
            }
        });
        //监听频道成员信息改变通知
        MSIM.getInstance().getChannelMembersManager().addOnRefreshChannelMemberInfo("group_detail_refresh_channel_member", (channelMember, isEnd) -> {
            if (channelMember != null) {
                if (channelMember.channelID.equals(groupNo) && channelMember.channelType == MSChannelType.GROUP) {
                    boolean isUpdate = false;
                    //本群内某个成员
                    for (int i = 0, size = groupMemberAdapter.getData().size(); i < size; i++) {
                        if (groupMemberAdapter.getData().get(i).memberUID.equalsIgnoreCase(channelMember.memberUID)) {
                            isUpdate = true;
                            if (groupMemberAdapter.getData().get(i).role != channelMember.role || groupMemberAdapter.getData().get(i).status != channelMember.status) {
                                isResetMembers = true;
                            } else {
                                groupMemberAdapter.getData().get(i).memberName = channelMember.memberName;
                                groupMemberAdapter.getData().get(i).memberRemark = channelMember.memberRemark;
                                groupMemberAdapter.notifyItemChanged(i);
                            }
                            break;
                        }
                    }
                    if (!isUpdate) {
                        isResetMembers = true;
                    }
                }
            }
            if (isEnd && isResetMembers) {
                //如果有角色更改就重新获取成员
                getMembers();
            }
        });
        //移除群成员监听
        MSIM.getInstance().getChannelMembersManager().addOnRemoveChannelMemberListener("group_detail_remove_channel_member", list -> {
            if (MSReader.isNotEmpty(list)) {
                for (int i = 0, size = list.size(); i < size; i++) {
                    for (int j = 0, len = groupMemberAdapter.getData().size(); j < len; j++) {
                        if (list.get(i).memberUID.equalsIgnoreCase(groupMemberAdapter.getData().get(j).memberUID)
                                && list.get(i).channelID.equals(groupMemberAdapter.getData().get(j).channelID)
                                && list.get(i).channelType == MSChannelType.GROUP) {
                            groupMemberAdapter.removeAt(j);
                            break;
                        }
                    }
                }
            }
            if (groupType == MSGroupType.normalGroup) {
                int count = MSIM.getInstance().getChannelMembersManager().getMemberCount(groupNo, MSChannelType.GROUP);
                titleTv.setText(String.format("%s(%s)", getString(R.string.chat_info), count));
            }
        });
        //添加群成员监听
        MSIM.getInstance().getChannelMembersManager().addOnAddChannelMemberListener("group_detail_add_channel_member", list -> {
            //这里这是演示sdk数据转成UI层数据。
            // 当然UI层也可以直接使用sdk的数据库
            List<MSChannelMember> tempList = new ArrayList<>();
            if (MSReader.isNotEmpty(list)) {
                for (int i = 0, size = list.size(); i < size; i++) {
                    if (list.get(i).channelID.equalsIgnoreCase(groupNo)
                            && list.get(i).channelType == MSChannelType.GROUP) {
                        tempList.add(list.get(i));
                    }
                }
                if (groupType == MSGroupType.normalGroup) {
                    int count = MSIM.getInstance().getChannelMembersManager().getMemberCount(groupNo, MSChannelType.GROUP);
                    titleTv.setText(String.format("%s(%s)", getString(R.string.chat_info), count));

                    if (memberRole != MSChannelMemberRole.normal) {
                        groupMemberAdapter.addData(groupMemberAdapter.getData().size() - 2, tempList);
                    } else
                        groupMemberAdapter.addData(groupMemberAdapter.getData().size() - 1, tempList);

                }
            }
        });
        //监听隐藏群管理入口
        EndpointManager.getInstance().setMethod("chat_hide_group_manage_view", object -> {
            msVBinding.groupManageLayout.setVisibility(View.GONE);
            return null;
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initData() {
        super.initData();

        int count = MSIM.getInstance().getChannelMembersManager().getMemberCount(groupNo, MSChannelType.GROUP);
        titleTv.setText(getString(R.string.chat_info) + "(" + count + ")");
        groupChannel = MSIM.getInstance().getChannelManager().getChannel(groupNo, MSChannelType.GROUP);
        if (groupChannel != null) {
            if (groupChannel.remoteExtraMap != null) {
                if (groupChannel.remoteExtraMap.containsKey(MSChannelExtras.groupType)) {
                    Object groupTypeObject = groupChannel.remoteExtraMap.get(MSChannelExtras.groupType);
                    if (groupTypeObject instanceof Integer) {
                        groupType = (int) groupTypeObject;
                    }
                }
                if (groupType == MSGroupType.superGroup && groupChannel.remoteExtraMap.containsKey(MSChannelCustomerExtras.memberCount)) {
                    Object memberCountObject = groupChannel.remoteExtraMap.get(MSChannelCustomerExtras.memberCount);
                    if (memberCountObject instanceof Integer) {
                        int memberCount = (int) memberCountObject;
                        titleTv.setText(getString(R.string.chat_info) + "(" + memberCount + ")");
                    }
                }
            }

            setData();
            setNotice();
        }
        groupPresenter.getGroupInfo(groupNo);
        getMembers();
    }

    private void getMembers() {
        isResetMembers = false;
        MSIM.getInstance().getChannelMembersManager().getWithPageOrSearch(groupNo, MSChannelType.GROUP, "", 1, 20, (list, b) -> {
            if (groupType == 0)
                resortData(list);
            else {
                if (b) {
                    resortData(list);
                }
            }
        });
    }

    private void resortData(List<MSChannelMember> list) {
        MSChannelMember channelMember = MSIM.getInstance().getChannelMembersManager().getMember(groupNo, MSChannelType.GROUP, MSConfig.getInstance().getUid());
        if (channelMember != null) {
            if (channelMember.memberUID.equals(MSConfig.getInstance().getUid())) {
                String name = channelMember.memberRemark;
                memberRole = channelMember.role;
                if (TextUtils.isEmpty(name))
                    name = channelMember.memberName;
                msVBinding.inGroupNameTv.setText(name);
            }
        }
        int maxCount;
        if (memberRole != MSChannelMemberRole.normal) {
            maxCount = 18;
        } else {
            maxCount = 19;
        }
        if (list != null) {
            List<MSChannelMember> temp = new ArrayList<>();
            for (int i = 0, size = Math.min(list.size(), maxCount); i < size; i++) {
                if (list.get(i).role == MSChannelMemberRole.admin) {
                    //群主或管理员
                    temp.add(0, list.get(i));
                } else temp.add(list.get(i));
            }
            //添加按钮
            MSChannelMember addUser = new MSChannelMember();
            addUser.memberUID = "-1";
            temp.add(addUser);
            if (memberRole != MSChannelMemberRole.normal) {
                //删除按钮
                MSChannelMember deleteUser = new MSChannelMember();
                deleteUser.memberUID = "-2";
                temp.add(deleteUser);
                msVBinding.groupManageLayout.setVisibility(View.VISIBLE);
            }
            groupMemberAdapter.setList(temp);
            if (list.size() >= 18) {
                msVBinding.showAllMembersTv.setVisibility(View.VISIBLE);
            } else msVBinding.showAllMembersTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGroupInfo(ChannelInfoEntity groupEntity) {
        setData();
    }

    @Override
    public void onRefreshGroupSetting(String key, int value) {

    }

    @Override
    public void setQrData(int day, String qrCode, String expire) {

    }

    @Override
    public void setMyGroups(List<GroupEntity> list) {

    }

    @Override
    public void showError(String msg) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            String content = data.getStringExtra("content");
            msVBinding.groupNoticeTv.setText(content);
        }
    }

    private void setNotice() {
        HashMap hashMap = groupChannel.localExtra;
        String notice = "";
        if (hashMap != null) {
            if (hashMap.containsKey(MSChannelExtras.notice)) {
                notice = (String) hashMap.get(MSChannelExtras.notice);
            }
        }
        if (!TextUtils.isEmpty(notice)) {
            msVBinding.unsetNoticeLayout.setVisibility(View.GONE);
            msVBinding.groupNoticeTv.setVisibility(View.VISIBLE);
            msVBinding.groupNoticeTv.setText(notice);
        } else {
            msVBinding.unsetNoticeLayout.setVisibility(View.VISIBLE);
            msVBinding.groupNoticeTv.setVisibility(View.GONE);
        }

    }

    private void setData() {
        msVBinding.nameTv.setText(groupChannel.channelName);
        msVBinding.remarkTv.setText(groupChannel.channelRemark);
        msVBinding.muteSwitchView.setChecked(groupChannel.mute == 1);
        msVBinding.stickSwitchView.setChecked(groupChannel.top == 1);
        msVBinding.saveSwitchView.setChecked(groupChannel.save == 1);
        msVBinding.showNickSwitchView.setChecked(groupChannel.showNick == 1);


        if (groupType == MSGroupType.superGroup && groupChannel.remoteExtraMap != null && groupChannel.remoteExtraMap.containsKey(MSChannelCustomerExtras.memberCount)) {
            Object memberCountObject = groupChannel.remoteExtraMap.get(MSChannelCustomerExtras.memberCount);
            if (memberCountObject instanceof Integer) {
                int memberCount = (int) memberCountObject;
                String content = String.format("%s(%s)", getString(R.string.chat_info), memberCount);
                titleTv.setText(content);
            }
        }
    }

    private void updateNameInGroupDialog() {
        if (!groupIsEnable()) return;
        String showName = "";
        MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(groupNo, MSChannelType.GROUP, MSConfig.getInstance().getUid());
        if (member != null) {
            String name = member.memberRemark;
            if (TextUtils.isEmpty(name))
                name = member.memberName;
            if (!TextUtils.isEmpty(name)) {
                showName = name;
            }
        }
        MSDialogUtils.getInstance().showInputDialog(this, getString(R.string.my_in_group_name), getString(R.string.update_in_gorup_name), showName, "", 10, text -> {
            if (!TextUtils.isEmpty(text)) {
                GroupModel.getInstance().updateGroupMemberInfo(groupNo, MSConfig.getInstance().getUid(), "remark", text, (code, msg) -> {
                    if (code == HttpResponseCode.success) {
                        msVBinding.inGroupNameTv.setText(text);
                    } else MSToastUtils.getInstance().showToastNormal(msg);
                });

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EndpointManager.getInstance().remove("chat_hide_group_manage_view");
        MSIM.getInstance().getChannelManager().removeRefreshChannelInfo("group_detail_refresh_channel");
        MSIM.getInstance().getChannelMembersManager().removeRefreshChannelMemberInfo("group_detail_refresh_channel_member");
        MSIM.getInstance().getChannelMembersManager().removeRemoveChannelMemberListener("group_detail_remove_channel_member");
        MSIM.getInstance().getChannelMembersManager().removeAddChannelMemberListener("group_detail_add_channel_member");
    }

    private boolean groupIsEnable() {
        return groupChannel != null && groupChannel.status != Const.GroupStatusDisband;
    }
}
