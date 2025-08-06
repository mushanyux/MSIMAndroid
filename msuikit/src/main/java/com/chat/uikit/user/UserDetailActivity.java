package com.chat.uikit.user;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSConstants;
import com.chat.base.config.MSSystemAccount;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.endpoint.entity.UserDetailViewMenu;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.NormalClickableContent;
import com.chat.base.ui.components.NormalClickableSpan;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.base.utils.MSToastUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.chat.manager.MSIMUtils;
import com.chat.uikit.contacts.service.FriendModel;
import com.chat.uikit.databinding.ActUserDetailLayoutBinding;
import com.chat.uikit.db.MSContactsDB;
import com.chat.uikit.message.MsgModel;
import com.chat.uikit.user.service.UserModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelMemberExtras;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 个人资料
 */
public class UserDetailActivity extends MSBaseActivity<ActUserDetailLayoutBinding> {
    String uid;
    String groupID;
    private String vercode;
    private MSChannel userChannel;

    @Override
    protected ActUserDetailLayoutBinding getViewBinding() {
        return ActUserDetailLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.user_card);
    }

    @Override
    protected void initPresenter() {
        initParams(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initParams(intent);
        initView();
        initListener();
        initData();
    }

    private void initParams(Intent mIntent) {
        uid = mIntent.getStringExtra("uid");
        if (TextUtils.isEmpty(uid)) finish();
        if (uid.equals(MSSystemAccount.system_file_helper)) {
            Intent intent = new Intent(this, MSFileHelperActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (uid.equals(MSSystemAccount.system_team)) {
            Intent intent = new Intent(this, MSSystemTeamActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (uid.equals(MSConfig.getInstance().getUid())) {
            Intent intent = new Intent(this, MyInfoActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (mIntent.hasExtra("groupID")) {
            groupID = mIntent.getStringExtra("groupID");
        } else {
            groupID = "";
        }
        if (mIntent.hasExtra("vercode")) {
            vercode = mIntent.getStringExtra("vercode");
        } else {
            vercode = "";
        }
        userChannel = MSIM.getInstance().getChannelManager().getChannel(uid, MSChannelType.PERSONAL);
        if (!TextUtils.isEmpty(groupID)) {
            MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(groupID, MSChannelType.GROUP, uid);
            if (member != null && member.extraMap != null && member.extraMap.containsKey(MSChannelMemberExtras.MSCode)) {
                vercode = (String) member.extraMap.get(MSChannelMemberExtras.MSCode);
            }
            if (member != null && !TextUtils.isEmpty(member.memberRemark)) {
                msVBinding.inGroupNameLayout.setVisibility(View.VISIBLE);
                msVBinding.inGroupNameTv.setText(member.memberRemark);
            }
            if (member != null && !TextUtils.isEmpty(member.memberInviteUID) && member.isDeleted == 0) {
                String name = "";
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(member.memberInviteUID, MSChannelType.PERSONAL);
                if (channel != null) {
                    name = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
                }
                if (TextUtils.isEmpty(name)) {
                    MSChannelMember member1 = MSIM.getInstance().getChannelMembersManager().getMember(groupID, MSChannelType.GROUP, member.memberInviteUID);
                    if (member1 != null) {
                        name = TextUtils.isEmpty(member1.memberRemark) ? member1.memberName : member1.memberRemark;
                    }
                }
                if (!TextUtils.isEmpty(name)) {
                    msVBinding.joinGroupWayLayout.setVisibility(View.VISIBLE);
                    String showTime = "";
                    if (!TextUtils.isEmpty(member.createdAt) && member.createdAt.contains(" ")) {
                        showTime = member.createdAt.split(" ")[0];
                    }
                    String content = String.format("%s %s", showTime, String.format(getString(R.string.invite_join_group), name));
                    msVBinding.joinGroupWayTv.setText(content);
                    int index = content.indexOf(name);
                    SpannableString span = new SpannableString(content);
                    span.setSpan(new NormalClickableSpan(false, Theme.colorAccount, new NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""), view -> {

                    }), index, index + name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    msVBinding.joinGroupWayTv.setText(span);
                }
            }
        } else {
            msVBinding.joinGroupWayLayout.setVisibility(View.GONE);
        }

    }

    @Override
    protected void initView() {
        msVBinding.applyBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.sendMsgBtn.getBackground().setTint(Theme.colorAccount);
        msVBinding.avatarView.setSize(50);
        msVBinding.appIdNumLeftTv.setText(String.format(getString(R.string.app_idnum), getString(R.string.app_name)));
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);
        msVBinding.otherLayout.removeAllViews();
        List<View> list = EndpointManager.getInstance().invokes(EndpointCategory.msUserDetailView, new UserDetailViewMenu(this, msVBinding.otherLayout, uid, groupID));
        if (MSReader.isNotEmpty(list)) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) != null)
                    msVBinding.otherLayout.addView(list.get(i));
            }
        }
        if (msVBinding.otherLayout.getChildCount() > 0) {
            LinearLayout view = new LinearLayout(this);
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.homeColor));
            view.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 15));
            msVBinding.otherLayout.addView(view);
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener() {
        if (!TextUtils.isEmpty(groupID) && !uid.equals(MSConfig.getInstance().getUid())) {
            MSIM.getInstance().getChannelManager().addOnRefreshChannelInfo("user_detail_refresh_channel", (channel, isEnd) -> {
                if (channel != null && channel.channelID.equals(groupID) && channel.channelType == MSChannelType.GROUP) {
                    getUserInfo();
                    msVBinding.avatarView.showAvatar(channel);
                }
            });
        }

        msVBinding.pushBlackLayout.setOnClickListener(v -> {

            if (userChannel == null) return;
            String title = getString(userChannel.status == 2 ? R.string.pull_out_black_list : R.string.push_black_list);
            String content = getString(userChannel.status == 2 ? R.string.pull_out_black_list_tips : R.string.join_black_list_tips);

            MSDialogUtils.getInstance().showDialog(this, title, content, true, "", "", 0, 0, index -> {
                if (index == 1) {
                    if (userChannel.status != 2)
                        UserModel.getInstance().addBlackList(uid, (code, msg) -> {
                            if (code == HttpResponseCode.success) {
                                finish();
                            } else showToast(msg);
                        });
                    else UserModel.getInstance().removeBlackList(uid, (code, msg) -> {
                        if (code == HttpResponseCode.success) {
                            finish();
                        } else showToast(msg);
                    });

                }
            });

        });
        setonLongClick(msVBinding.nameTv, msVBinding.nameTv);
        setonLongClick(msVBinding.identityLayout, msVBinding.appIdNumTv);
        setonLongClick(msVBinding.nickNameLayout, msVBinding.nickNameTv);

        //频道资料刷新
        MSIM.getInstance().getChannelManager().addOnRefreshChannelInfo("user_detail_refresh_channel1", (channel, isEnd) -> {
            if (channel != null && channel.channelID.equals(uid) && channel.channelType == MSChannelType.PERSONAL) {
                userChannel = MSIM.getInstance().getChannelManager().getChannel(uid, MSChannelType.PERSONAL);
                setData();
            }
        });
        SingleClickUtil.onSingleClick(msVBinding.applyBtn, v -> MSDialogUtils.getInstance().showInputDialog(UserDetailActivity.this, getString(R.string.apply), getString(R.string.input_remark), "", getString(R.string.input_remark), 20, text -> FriendModel.getInstance().applyAddFriend(uid, vercode, text, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                msVBinding.applyBtn.setText(R.string.applyed);
                msVBinding.applyBtn.setAlpha(0.2f);
                msVBinding.applyBtn.setEnabled(false);
            } else showToast(msg);
        })));
        SingleClickUtil.onSingleClick(msVBinding.sendMsgBtn, v -> {
            MSIMUtils.getInstance().startChatActivity(new ChatViewMenu(this, uid, MSChannelType.PERSONAL, 0, true));
            finish();
        });
        msVBinding.deleteLayout.setOnClickListener(v -> {
            String content = String.format(getString(R.string.delete_friends_tips), msVBinding.nameTv.getText().toString());
            MSDialogUtils.getInstance().showDialog(this, getString(R.string.delete_friends), content, true, "", getString(R.string.delete), 0, ContextCompat.getColor(this, R.color.red), index -> {
                if (index == 1) {
                    UserModel.getInstance().deleteUser(uid, (code, msg) -> {
                        if (code == HttpResponseCode.success) {
                            MSIM.getInstance().getConversationManager().deleteWitchChannel(uid, MSChannelType.PERSONAL);
                            MsgModel.getInstance().offsetMsg(uid, MSChannelType.PERSONAL, null);
                            MSIM.getInstance().getMsgManager().clearWithChannel(uid, MSChannelType.PERSONAL);
                            MSContactsDB.getInstance().updateFriendStatus(uid, 0);
                            MSIM.getInstance().getChannelManager().updateFollow(uid, MSChannelType.PERSONAL, 0);
                            EndpointManager.getInstance().invoke(MSConstants.refreshContacts, null);
                            EndpointManager.getInstance().invokes(EndpointCategory.msExitChat, new MSChannel(uid, MSChannelType.PERSONAL));
                            finish();
                        } else showToast(msg);
                    });
                }
            });
        });
        SingleClickUtil.onSingleClick(msVBinding.remarkLayout, v -> {
            Intent intent = new Intent(this, SetUserRemarkActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("oldStr", userChannel == null ? "" : userChannel.channelRemark);
            chooseResultLac.launch(intent);
        });
        msVBinding.avatarView.setOnClickListener(v -> showImg());
    }

    private void showCopy(View view, float[] coordinate, String content) {
        List<PopupMenuItem> list = new ArrayList<>();
        list.add(new PopupMenuItem(getString(R.string.copy), R.mipmap.msg_copy, () -> {
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", content);
            assert cm != null;
            cm.setPrimaryClip(mClipData);
            MSToastUtils.getInstance().showToastNormal(getString(R.string.copyed));
        }));
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.color999));
        MSDialogUtils.getInstance().showScreenPopup(view, coordinate, list, () -> view.setBackgroundColor(ContextCompat.getColor(UserDetailActivity.this, R.color.transparent)));
    }

    @Override
    protected void initData() {
        super.initData();
        setData();
        getUserInfo();
    }

    private void setData() {
        msVBinding.avatarView.showAvatar(uid, MSChannelType.PERSONAL);
        if (uid.equals(MSConfig.getInstance().getUid())) hideTitleRightView();
        if (userChannel != null) {
            if (!TextUtils.isEmpty(userChannel.channelRemark)) {
                msVBinding.nickNameLayout.setVisibility(View.VISIBLE);
                msVBinding.nickNameTv.setText(userChannel.channelName);
                msVBinding.nameTv.setText(userChannel.channelRemark);
            } else {
                msVBinding.nameTv.setText(userChannel.channelName);
                msVBinding.nickNameLayout.setVisibility(View.GONE);
            }
        } else {
            msVBinding.deleteLayout.setVisibility(View.GONE);
            msVBinding.sendMsgBtn.setVisibility(View.GONE);
        }
    }

    private void getUserInfo() {
        MSIM.getInstance().getChannelManager().fetchChannelInfo(uid, MSChannelType.PERSONAL);
        UserModel.getInstance().getUserInfo(uid, groupID, (code, msg, userInfo) -> {
            if (code == HttpResponseCode.success) {
                if (userInfo != null) {
                    if (!TextUtils.isEmpty(userInfo.vercode)) {
                        vercode = userInfo.vercode;
                    }
                    msVBinding.nameTv.setText(TextUtils.isEmpty(userInfo.remark) ? userInfo.name : userInfo.remark);
                    msVBinding.nickNameTv.setText(userInfo.name);
                    msVBinding.nickNameLayout.setVisibility(TextUtils.isEmpty(userInfo.remark) ? View.GONE : View.VISIBLE);
                    if (TextUtils.isEmpty(userInfo.short_no)) {
                        msVBinding.identityLayout.setVisibility(View.GONE);
                    } else {
                        msVBinding.identityLayout.setVisibility(View.VISIBLE);
                        msVBinding.appIdNumTv.setText(userInfo.short_no);
                    }
                    if (!TextUtils.isEmpty(userInfo.source_desc)) {
                        msVBinding.sourceFromTv.setText(userInfo.source_desc);
                        msVBinding.fromLayout.setVisibility(View.VISIBLE);
                    } else {
                        msVBinding.fromLayout.setVisibility(View.GONE);
                    }

                    if (userInfo.status == 2) {
                        msVBinding.blacklistTv.setText(R.string.pull_out_black_list);
                    } else {
                        msVBinding.blacklistTv.setText(R.string.push_black_list);
                    }
                    msVBinding.sendMsgBtn.setVisibility(userInfo.follow == 1 ? View.VISIBLE : View.GONE);
                    msVBinding.applyBtn.setVisibility(userInfo.follow == 1 ? View.GONE : View.VISIBLE);
                    msVBinding.deleteLayout.setVisibility(userInfo.follow == 1 ? View.VISIBLE : View.GONE);
                    msVBinding.blacklistDescTv.setVisibility(userInfo.status == 2 ? View.VISIBLE : View.GONE);
                    if (userInfo.follow == 0) {
                        msVBinding.applyBtn.setVisibility(TextUtils.isEmpty(vercode) ? View.GONE : View.VISIBLE);
                    } else {
                        msVBinding.applyBtn.setVisibility(View.GONE);
                    }

                    if (!TextUtils.isEmpty(userInfo.join_group_invite_uid)){
                        msVBinding.joinGroupWayLayout.setVisibility(View.VISIBLE);
                        String content = String.format("%s %s", userInfo.join_group_time, String.format(getString(R.string.invite_join_group), userInfo.join_group_invite_name));
                        msVBinding.joinGroupWayTv.setText(content);
                        int index = content.indexOf(userInfo.join_group_invite_name);
                        SpannableString span = new SpannableString(content);
                        span.setSpan(new NormalClickableSpan(false, Theme.colorAccount, new NormalClickableContent(NormalClickableContent.NormalClickableTypes.Other, ""), view -> {

                        }), index, index + userInfo.join_group_invite_name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        msVBinding.joinGroupWayTv.setText(span);
                    }
                }
            } else {
                showToast(msg);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MSIM.getInstance().getChannelManager().removeRefreshChannelInfo("user_detail_refresh_channel");
        MSIM.getInstance().getChannelManager().removeRefreshChannelInfo("user_detail_refresh_channel1");
    }


    private void showImg() {
        String uri = MSApiConfig.getAvatarUrl(uid) + "?key=" + MSTimeUtils.getInstance().getCurrentMills();
        //查看大图
        List<Object> tempImgList = new ArrayList<>();
        List<ImageView> imageViewList = new ArrayList<>();
        imageViewList.add(msVBinding.avatarView.imageView);
        tempImgList.add(MSApiConfig.getShowUrl(uri));
        int index = 0;
        MSDialogUtils.getInstance().showImagePopup(this, tempImgList, imageViewList, msVBinding.avatarView.imageView, index, new ArrayList<>(), null, null);
        MSIM.getInstance().getChannelManager().updateAvatarCacheKey(uid, MSChannelType.PERSONAL, UUID.randomUUID().toString().replaceAll("-", ""));
    }

    ActivityResultLauncher<Intent> chooseResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            getUserInfo();
        }
    });

    @SuppressLint("ClickableViewAccessibility")
    private void setonLongClick(View view, TextView textView) {
        final float[][] location = {new float[2]};
        view.setOnTouchListener((var view12, var motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                location[0] = new float[]{motionEvent.getRawX(), motionEvent.getRawY()};
            }
            return false;
        });
        view.setOnLongClickListener(view1 -> {
            showCopy(textView, location[0], textView.getText().toString());
            return true;
        });
    }
}
