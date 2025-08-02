package com.chat.uikit.chat.manager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Vibrator;
import android.text.TextUtils;

import com.chat.base.MSBaseApplication;
import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.db.ApplyDB;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.entity.NewFriendEntity;
import com.chat.base.entity.UserInfoSetting;
import com.chat.base.entity.MSGroupType;
import com.chat.base.msg.IConversationContext;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.msgitem.MSUIChatMsgItemEntity;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.utils.NotificationCompatUtil;
import com.chat.base.utils.MSCommonUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSLogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.base.utils.MSToastUtils;
import com.chat.base.views.pwdview.NumPwdDialog;
import com.chat.uikit.R;
import com.chat.uikit.MSUIKitApplication;
import com.chat.uikit.chat.ChatActivity;
import com.chat.uikit.contacts.service.FriendModel;
import com.chat.uikit.db.MSContactsDB;
import com.chat.uikit.enity.ProhibitWord;
import com.chat.uikit.group.service.GroupModel;
import com.chat.uikit.message.MsgModel;
import com.chat.uikit.message.ProhibitWordModel;
import com.chat.uikit.search.SearchUserActivity;
import com.chat.uikit.user.UserDetailActivity;
import com.chat.uikit.user.service.UserModel;
import com.chat.uikit.utils.PushNotificationHelper;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSCMDKeys;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelExtras;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSConversationMsg;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.entity.MSUIConversationMsg;
import com.mushanyux.mushanim.message.type.MSSendMsgResult;
import com.mushanyux.mushanim.msgmodel.MSTextContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * im监听相关处理
 */
public class MSIMUtils {

    private MSIMUtils() {
    }

    private static class IMUtilsBinder {
        private final static MSIMUtils util = new MSIMUtils();
    }

    public static MSIMUtils getInstance() {
        return IMUtilsBinder.util;
    }

    /**
     * 初始化事件
     */
    public void initIMListener() {
        EndpointManager.getInstance().setMethod("show_rtc_notification", object -> {
            if (object instanceof String fromUID) {
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(fromUID, MSChannelType.PERSONAL);
                var fromName = "";
                if (channel != null) {
                    if (TextUtils.isEmpty(channel.channelRemark)) {
                        fromName = channel.channelName;
                    } else fromName = channel.channelRemark;
                }

                Vibrator mVibrator = (Vibrator) MSBaseApplication.getInstance().getContext().getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 1000, 1000};
                AudioAttributes audioAttributes;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    audioAttributes = new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION) //key
                            .build();
                    mVibrator.vibrate(pattern, 0, audioAttributes);
                } else {
                    mVibrator.vibrate(pattern, 0);
                }
                PushNotificationHelper.INSTANCE.notifyCall(MSUIKitApplication.getInstance().getContext(), 2, fromName, MSBaseApplication.getInstance().getContext().getString(R.string.invite_call));
            }
            return null;
        });
        EndpointManager.getInstance().setMethod("cancel_rtc_notification", object -> {
            Vibrator vibrator = (Vibrator) MSBaseApplication.getInstance().getContext().getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.cancel();
            NotificationCompatUtil.Companion.cancel(MSUIKitApplication.getInstance().getContext(), 2);
            return null;
        });
        //监听sdk获取IP和port
        MSIM.getInstance().getConnectionManager().addOnGetIpAndPortListener(andPortListener -> MsgModel.getInstance().getChatIp((code, ip, port) -> andPortListener.onGetSocketIpAndPort(ip, Integer.parseInt(port))));
        //消息存库拦截器监听
        MSIM.getInstance().getMsgManager().addMessageStoreBeforeIntercept(msg -> {
            if (msg != null && msg.type == MSContentType.screenshot) {
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(msg.channelID, msg.channelType);
                if (channel != null && channel.remoteExtraMap != null && channel.remoteExtraMap.containsKey(MSChannelExtras.screenshot)) {
                    Object object = channel.remoteExtraMap.get(MSChannelExtras.screenshot);
                    int screenshot = 0;
                    if (object != null) {
                        screenshot = (int) object;
                    }
                    return screenshot != 0;
                } else {
                    return true;
                }
            }
            return true;
        });
        //监听聊天附件上传
        MSIM.getInstance().getMsgManager().addOnUploadAttachListener((msg, listener) -> MSSendMsgUtils.getInstance().uploadChatAttachment(msg, listener));
        //监听同步会话
        MSIM.getInstance().getConversationManager().addOnSyncConversationListener((s, i, l, iSyncConvChatBack) -> MsgModel.getInstance().syncChat(s, i, l, iSyncConvChatBack));
        //监听同步频道会话
        MSIM.getInstance().getMsgManager().addOnSyncChannelMsgListener((channelID, channelType, startMessageSeq, endMessageSeq, limit, pullMode, iSyncChannelMsgBack) -> MsgModel.getInstance().syncChannelMsg(channelID, channelType, startMessageSeq, endMessageSeq, limit, pullMode, iSyncChannelMsgBack));
        //新消息监听
        MSIM.getInstance().getMsgManager().addOnNewMsgListener("system", msgList -> {
            boolean isAlertMsg = false;
            String channelID = "";
            byte channelType = MSChannelType.PERSONAL;
            MSMsg sensitiveWordsMsg = null;
            String loginUID = MSConfig.getInstance().getUid();
            if (MSReader.isNotEmpty(msgList)) {
                channelID = msgList.get(msgList.size() - 1).channelID;
                channelType = msgList.get(msgList.size() - 1).channelType;
                for (int i = 0, size = msgList.size(); i < size; i++) {
                    if (msgList.get(i).type == MSContentType.setNewGroupAdmin) {
                        GroupModel.getInstance().groupMembersSync(msgList.get(i).channelID, null);
                    } else if (msgList.get(i).type == MSContentType.groupSystemInfo) {
                        MSCommonModel.getInstance().getChannel(msgList.get(i).channelID, MSChannelType.GROUP, null);
                        GroupModel.getInstance().groupMembersSync(msgList.get(i).channelID, null);
                    } else if (msgList.get(i).type == MSContentType.addGroupMembersMsg || msgList.get(i).type == MSContentType.removeGroupMembersMsg) {
                        //同步信息
                        GroupModel.getInstance().groupMembersSync(msgList.get(i).channelID, null);
                    } else {
                        if (msgList.get(i).type != MSContentType.MS_INSIDE_MSG) {
                            isAlertMsg = true;
                        }
                    }

                    if (msgList.get(i).header.noPersist || !msgList.get(i).header.redDot || !MSContentType.isSupportNotification(msgList.get(i).type)) {
                        isAlertMsg = false;
                    }
                    if (!TextUtils.isEmpty(loginUID) && !TextUtils.isEmpty(msgList.get(i).fromUID) && msgList.get(i).fromUID.equals(loginUID)) {
                        isAlertMsg = false;
                    }
                    if (msgList.get(i).type == MSContentType.MS_TEXT) {
                        boolean isContains = false;
                        MSTextContent textContent = (MSTextContent) msgList.get(i).baseContentMsgModel;
                        // 判断是否包含敏感词
                        if (MSUIKitApplication.getInstance().sensitiveWords != null
                                && MSReader.isNotEmpty(MSUIKitApplication.getInstance().sensitiveWords.list)
                                && textContent != null && !TextUtils.isEmpty(textContent.getDisplayContent())) {
                            for (String word : MSUIKitApplication.getInstance().sensitiveWords.list) {
                                if (textContent.getDisplayContent().contains(word)) {
                                    isContains = true;
                                    break;
                                }
                            }
                        }
                        if (isContains) {
                            sensitiveWordsMsg = new MSMsg();
                            sensitiveWordsMsg.channelID = msgList.get(i).channelID;
                            sensitiveWordsMsg.channelType = msgList.get(i).channelType;
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("content", MSUIKitApplication.getInstance().sensitiveWords.tips);
                                jsonObject.put("type", MSContentType.sensitiveWordsTips);
                            } catch (JSONException e) {
                                MSLogUtils.e("解析敏感词错误");
                            }
                            MSChannel channel = new MSChannel(msgList.get(i).channelID, msgList.get(i).channelType);
                            sensitiveWordsMsg.setChannelInfo(channel);
                            sensitiveWordsMsg.content = jsonObject.toString();
                            sensitiveWordsMsg.type = MSContentType.sensitiveWordsTips;
                            long tempOrderSeq = MSIM.getInstance().getMsgManager().getMessageOrderSeq(0, msgList.get(i).channelID, msgList.get(i).channelType);
                            sensitiveWordsMsg.orderSeq = tempOrderSeq + 1;
                            sensitiveWordsMsg.status = MSSendMsgResult.send_success;

                        }
                    }
                }
            }
            boolean isVibrate = true;
            boolean playNewMsgMedia = true;
            boolean newMsgNotice = true;
            UserInfoSetting setting = MSConfig.getInstance().getUserInfo().setting;
            int msgShowDetail = 1;
            if (setting != null) {
                msgShowDetail = setting.msg_show_detail;
                if (setting.new_msg_notice == 0) {
                    newMsgNotice = false;
                    playNewMsgMedia = false;
                    isVibrate = false;
                } else {
                    if (setting.voice_on == 0) {
                        playNewMsgMedia = false;
                    }
                    if (setting.shock_on == 0) {
                        isVibrate = false;
                    }
                }
            }
            if (newMsgNotice && isAlertMsg && (TextUtils.isEmpty(MSUIKitApplication.getInstance().chattingChannelID) || !MSUIKitApplication.getInstance().chattingChannelID.equals(channelID))) {
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelID, channelType);
                if (channel != null && channel.mute == 0) {
                    showNotification(msgList.get(msgList.size() - 1), msgShowDetail, channel, playNewMsgMedia, isVibrate);
                }
            }

            assert msgList != null;

            if (sensitiveWordsMsg != null) {
                MSMsg finalSensitiveWordsMsg = sensitiveWordsMsg;
                new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(() -> MSIM.getInstance().getMsgManager().saveAndUpdateConversationMsg(finalSensitiveWordsMsg, false), 1000 * 2);
            }
        });
        MSIM.getInstance().getMsgManager().addOnUploadMsgExtraListener(msgExtra -> {
            MSMsg msg = MSIM.getInstance().getMsgManager().getWithMessageID(msgExtra.messageID);
            int msgSeq = 0;
            if (msg != null) {
                msgSeq = msg.messageSeq;
            }
            MsgModel.getInstance().editMsg(msgExtra.messageID, msgSeq, msgExtra.channelID, msgExtra.channelType, msgExtra.contentEdit, null);
        });

        /*
         * 设置获取频道信息的监听
         */
        MSIM.getInstance().getChannelManager().addOnGetChannelInfoListener((channelId, channelType, iChannelInfoListener) -> {
            MSCommonModel.getInstance().getChannel(channelId, channelType, null);
            return null;
        });
        MSIM.getInstance().getChannelMembersManager().addOnGetChannelMembersListener((channelID, b, keyword, page, limit, iChannelMemberListResult) -> GroupModel.getInstance().getChannelMembers(channelID, keyword, page, limit, iChannelMemberListResult));
        /*
         * 获取频道成员
         */
        MSIM.getInstance().getChannelMembersManager().addOnGetChannelMemberListener((channelId, channelType, uid, iChannelMemberInfoListener) -> {
            MSCommonModel.getInstance().getChannel(uid, MSChannelType.PERSONAL, (code, msg, entity) -> {
                MSChannelMember channelMember = new MSChannelMember();
                channelMember.memberName = entity.name;
                channelMember.memberUID = entity.channel.channel_id;
                channelMember.channelID = channelId;
                channelMember.channelType = channelType;
                MSIM.getInstance().getChannelMembersManager().refreshChannelMemberCache(channelMember);
                iChannelMemberInfoListener.onResult(channelMember);
            });
            return null;
        });

        //监听频道修改头像
        MSIM.getInstance().getChannelManager().addOnRefreshChannelAvatar((s, b) -> {
            // 头像需要本地修改
            String key = UUID.randomUUID().toString().replace("-", "");
            AvatarView.clearCache(s, b);
            MSIM.getInstance().getChannelManager().updateAvatarCacheKey(s, b, key);
        });
        //刷新群成员
        MSIM.getInstance().getChannelMembersManager().addOnSyncChannelMembers((channelID, channelType) -> {
            if (!TextUtils.isEmpty(channelID) && channelType == MSChannelType.GROUP) {
                GroupModel.getInstance().groupMembersSync(channelID, null);
            }
        });

        MSIM.getInstance().getCMDManager().addCmdListener("system", cmd -> {
            if (!TextUtils.isEmpty(cmd.cmdKey)) {
                switch (cmd.cmdKey) {
                    case MSCMDKeys.ms_messageRevoke -> revokeMsg(cmd.paramJsonObject);
                    case MSCMDKeys.ms_friendRequest ->
                            FriendModel.getInstance().saveNewFriendsMsg(cmd.paramJsonObject.toString());
                    case MSCMDKeys.ms_friendDeleted, MSCMDKeys.ms_friendAccept -> {
                        FriendModel.getInstance().syncFriends(null);
                        if (cmd.cmdKey.equals(MSCMDKeys.ms_friendAccept)
                                && cmd.paramJsonObject != null && cmd.paramJsonObject.has("to_uid")) {
                            String uid = cmd.paramJsonObject.optString("to_uid");
                            MSContactsDB.getInstance().updateFriendStatus(uid, 1);
                            NewFriendEntity entity = ApplyDB.getInstance().query(uid);
                            if (entity != null && entity.status == 0) {
                                entity.status = 1;
                                ApplyDB.getInstance().update(entity);
                            }
                        }
                    }
                    case MSCMDKeys.ms_sync_message_extra -> {
                        if (cmd.paramJsonObject == null) {
                            return;
                        }
                        String channelID = cmd.paramJsonObject.optString("channel_id");
                        byte channelType = (byte) cmd.paramJsonObject.optInt("channel_type");
                        if (TextUtils.isEmpty(channelID)) {
                            return;
                        }
                        MsgModel.getInstance().syncExtraMsg(channelID, channelType);
                    }
                    case MSCMDKeys.ms_memberUpdate -> {
                        if (cmd.paramJsonObject == null) {
                            return;
                        }
                        String groupNo = cmd.paramJsonObject.optString("group_no");
                        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(groupNo, MSChannelType.GROUP);
                        if (channel == null || channel.remoteExtraMap == null) {
                            return;
                        }
                        Object groupTypeObject = channel.remoteExtraMap.get(MSChannelExtras.groupType);
                        if (groupTypeObject instanceof Integer) {
                            int groupType = (int) groupTypeObject;
                            if (groupType == MSGroupType.superGroup) {
                                String uid = cmd.paramJsonObject.optString("uid");
                                if (!TextUtils.isEmpty(uid)) {
                                    UserModel.getInstance().getUserInfo(uid,groupNo,null);
                                }
                            }
                        }
                    }
                    case MSCMDKeys.ms_sync_reminders -> MsgModel.getInstance().syncReminder();
                    case MSCMDKeys.ms_sync_conversation_extra ->
                            MsgModel.getInstance().syncCoverExtra();
                }
            }
        });
    }

    public MSUIChatMsgItemEntity msg2UiMsg(IConversationContext context, MSMsg msg, int memberCount, boolean showNickName, boolean isChoose) {
        if (msg.remoteExtra.readedCount == 0) {
            msg.remoteExtra.unreadCount = memberCount - 1;
        }
        if (msg.type == MSContentType.MS_TEXT) {
            resetMsgProhibitWord(msg);
        }
        MSUIChatMsgItemEntity uiChatMsgItemEntity = new MSUIChatMsgItemEntity(context, msg, new MSUIChatMsgItemEntity.ILinkClick() {
            @Override
            public void onShowUserDetail(String uid, String groupNo) {
                Intent intent = new Intent(context.getChatActivity(), UserDetailActivity.class);
                intent.putExtra("uid", uid);
                if (!TextUtils.isEmpty(groupNo)) {
                    intent.putExtra("groupID", groupNo);
                }
                context.getChatActivity().startActivity(intent);
            }

            @Override
            public void onShowSearchUser(String phone) {
                Intent intent = new Intent(context.getChatActivity(), SearchUserActivity.class);
                intent.putExtra("phone", phone);
                context.getChatActivity().startActivity(intent);
            }
        });
        uiChatMsgItemEntity.msMsg = msg;
        uiChatMsgItemEntity.isChoose = isChoose;
        uiChatMsgItemEntity.showNickName = showNickName;

        // 计算气泡类型
        return uiChatMsgItemEntity;
    }

    public void resetMsgProhibitWord(MSMsg msg) {
        if (msg == null || msg.type != MSContentType.MS_TEXT) {
            return;
        }
        List<ProhibitWord> list = ProhibitWordModel.Companion.getInstance().getAll();
        if (MSReader.isNotEmpty(list)) {
            String content = getContent(msg);
            for (ProhibitWord word : list) {
                if (content.contains(word.content)) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < word.content.length(); i++) {
                        sb.append("*");
                    }
                    content = content.replaceAll(word.content, sb.toString());
                }
            }

            if (msg.remoteExtra.contentEditMsgModel != null && !TextUtils.isEmpty(msg.remoteExtra.contentEditMsgModel.getDisplayContent())) {
                msg.remoteExtra.contentEditMsgModel.content = content;
            } else {
                msg.baseContentMsgModel.content = content;
            }
        }
    }

    private String getContent(MSMsg msg) {
        String showContent = msg.baseContentMsgModel.getDisplayContent();
        if (msg.remoteExtra.contentEditMsgModel != null && !TextUtils.isEmpty(msg.remoteExtra.contentEditMsgModel.getDisplayContent())) {
            showContent = msg.remoteExtra.contentEditMsgModel.getDisplayContent();
        }
        return showContent;
    }


    public void revokeMsg(JSONObject jsonObject) {
        //撤回消息
        if (jsonObject != null) {
            if (jsonObject.has("message_id")) {
                String messageId = jsonObject.optString("message_id");
                //  String client_msg_no = jsonObject.optString("client_msg_no");
                String channelID = jsonObject.optString("channel_id");
                byte channelType = (byte) jsonObject.optInt("channel_type");
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelID, channelType);
                //是否撤回提醒
                int revokeRemind = 1;
                if (channel != null && channel.remoteExtraMap != null && channel.remoteExtraMap.containsKey(MSChannelExtras.revokeRemind)) {
                    Object object = channel.remoteExtraMap.get(MSChannelExtras.revokeRemind);
                    if (object != null) {
                        revokeRemind = (int) object;
                    }
                }
                if (revokeRemind == 1) {
                    // todo 同步消息接口
                    MsgModel.getInstance().syncExtraMsg(channelID, channelType);
//                    MSIM.getInstance().getMsgManager().updateMsgRevokeWithMessageID(messageId, 1);
                } else {
                    // todo 删除服务器消息
                    MSMsg msMsg = MSIM.getInstance().getMsgManager().getWithMessageID(messageId);
                    if (msMsg != null) {
                        List<MSMsg> list = new ArrayList<>();
                        list.add(msMsg);
                        MsgModel.getInstance().deleteMsg(list, null);
                    }

                    int rowNo = MSIM.getInstance().getMsgManager().getRowNoWithMessageID(channelID, channelType, messageId);
                    //要先删除
                    MSIM.getInstance().getMsgManager().deleteWithMessageID(messageId);
                    MSConversationMsg msg = MSIM.getInstance().getConversationManager().getWithChannel(channelID, channelType);
                    if (msg != null) {
                        if (rowNo < msg.unreadCount) {
                            msg.unreadCount--;
                        }
                        MSIM.getInstance().getConversationManager().updateWithMsg(msg);
                    }
                }

            }
        }
    }


    /**
     * 显示聊天
     *
     * @param chatViewMenu 参数
     */
    public void startChatActivity(ChatViewMenu chatViewMenu) {
        if (chatViewMenu == null || chatViewMenu.activity == null || TextUtils.isEmpty(chatViewMenu.channelID)) {
            return;
        }
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(chatViewMenu.channelID, chatViewMenu.channelType);
        int chatPwdON = 0;
        if (channel != null && channel.remoteExtraMap != null && channel.remoteExtraMap.containsKey(MSChannelExtras.chatPwdOn)) {
            Object object = channel.remoteExtraMap.get(MSChannelExtras.chatPwdOn);
            if (object instanceof Integer) {
                chatPwdON = (int) object;
            }
        }
        if (chatPwdON == 1) {
            showChatPwdDialog(chatViewMenu, channel);
            return;
        }
        startChat(chatViewMenu);
    }

    private void startChat(ChatViewMenu chatViewMenu) {
        if (MSTimeUtils.isFastDoubleClick()) {
            return;
        }
        MsgModel.getInstance().deleteFlameMsg();
        Intent intent = new Intent(chatViewMenu.activity, ChatActivity.class);
        intent.putExtra("channelId", chatViewMenu.channelID);
        intent.putExtra("channelType", chatViewMenu.channelType);
        MSConversationMsg conversationMsg = MSIM.getInstance().getConversationManager().getWithChannel(chatViewMenu.channelID, chatViewMenu.channelType);
        MSMsg msg = null;
        int redDot = 0;
        long aroundMsgSeq = 0;
        if (conversationMsg != null) {
            redDot = conversationMsg.unreadCount;
            msg = MSIM.getInstance().getMsgManager().getWithClientMsgNO(conversationMsg.lastClientMsgNO);
            if (msg != null) {
                aroundMsgSeq = msg.orderSeq;
            }
        }
        if (chatViewMenu.tipMsgOrderSeq != 0) {
            // 强提醒某条消息
            intent.putExtra("tipsOrderSeq", chatViewMenu.tipMsgOrderSeq);
        } else {
            if (redDot > 0) {
                long orderSeq;
                int messageSeq = 0;
                if (msg != null) {
                    if (msg.messageSeq == 0) {
                        int maxMsgSeq = MSIM.getInstance().getMsgManager().getMaxMessageSeqWithChannel(chatViewMenu.channelID, chatViewMenu.channelType);
                        messageSeq = maxMsgSeq - redDot + 1;
                    } else {
                        messageSeq = msg.messageSeq - redDot + 1;
                    }
                    if (messageSeq <= 0) {
                        messageSeq = MSIM.getInstance().getMsgManager().getMinMessageSeqWithChannel(chatViewMenu.channelID, chatViewMenu.channelType);
                    }
                }
                orderSeq = MSIM.getInstance().getMsgManager().getMessageOrderSeq(messageSeq, chatViewMenu.channelID, chatViewMenu.channelType);
                intent.putExtra("unreadStartMsgOrderSeq", orderSeq);
                intent.putExtra("redDot", redDot);
            } else {
                MSUIConversationMsg uiMsg = MSIM.getInstance().getConversationManager().getUIConversationMsg(chatViewMenu.channelID, chatViewMenu.channelType);
                if (uiMsg != null && uiMsg.getRemoteMsgExtra() != null && uiMsg.getRemoteMsgExtra().keepMessageSeq != 0) {
                    long lastPreviewMsgOrderSeq = MSIM.getInstance().getMsgManager().getMessageOrderSeq(uiMsg.getRemoteMsgExtra().keepMessageSeq, chatViewMenu.channelID, chatViewMenu.channelType);
                    intent.putExtra("lastPreviewMsgOrderSeq", lastPreviewMsgOrderSeq);
                    intent.putExtra("keepOffsetY", uiMsg.getRemoteMsgExtra().keepOffsetY);
                }
            }
        }
        if (chatViewMenu.isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (MSReader.isNotEmpty(chatViewMenu.forwardMsgList)) {
            intent.putParcelableArrayListExtra("msgContentList", (ArrayList<? extends Parcelable>) chatViewMenu.forwardMsgList);
        }
        intent.putExtra("aroundMsgSeq", aroundMsgSeq);
        chatViewMenu.activity.startActivity(intent);
    }

    private void showChatPwdDialog(ChatViewMenu chatViewMenu, MSChannel channel) {
        NumPwdDialog.getInstance().showNumPwdDialog(chatViewMenu.activity, chatViewMenu.activity.getString(R.string.chat_pwd), chatViewMenu.activity.getString(R.string.input_chat_pwd), channel.channelName, new NumPwdDialog.IPwdInputResult() {
            @Override
            public void onResult(String numPwd) {

                if (!MSCommonUtils.digest(numPwd + MSConfig.getInstance().getUid()).equals(MSConfig.getInstance().getUserInfo().chat_pwd)) {
                    int chatPwdCount = MSSharedPreferencesUtil.getInstance().getInt("ms_chat_pwd_count", 3);
                    if (chatPwdCount == 0) {
                        // 清空聊天记录
                        MSSharedPreferencesUtil.getInstance().putInt("ms_chat_pwd_count", 0);
                        MSIM.getInstance().getMsgManager().clearWithChannel(channel.channelID, channel.channelType);
                        MSToastUtils.getInstance().showToastNormal(chatViewMenu.activity.getString(R.string.chat_msg_is_cleard));
                        return;
                    }

                    String content = String.format(chatViewMenu.activity.getString(R.string.forget_chat_pwd), chatPwdCount, chatPwdCount);
                    MSDialogUtils.getInstance().showDialog(chatViewMenu.activity, chatViewMenu.activity.getString(R.string.chat_pwd_error), content, false, chatViewMenu.activity.getString(R.string.cancel), chatViewMenu.activity.getString(R.string.chat_pwd_reset_pwd), 0, Theme.colorAccount, index -> {
                        if (index == 1) {
                            EndpointManager.getInstance().invoke("show_set_chat_pwd", null);
                        }
                    });
                    MSSharedPreferencesUtil.getInstance().putInt("ms_chat_pwd_count", --chatPwdCount);
                } else {
                    MSSharedPreferencesUtil.getInstance().putInt("ms_chat_pwd_count", 3);
                    startChat(chatViewMenu);
                }

            }

            @Override
            public void forgetPwd() {
                EndpointManager.getInstance().invoke("show_set_chat_pwd", null);
            }
        });

    }


    private void showNotification(MSMsg msg, int msgShowDetail, MSChannel channel, boolean playNewMsgMedia, boolean isVibrate) {
        int msgNotice = MSConfig.getInstance().getUserInfo().setting.new_msg_notice;
        if (msgNotice == 0) {
            return;
        }
//        Activity activity = ActManagerUtils.getInstance().getCurrentActivity();
//        if (activity == null || activity.getComponentName().getClassName().equals(TabActivity.class.getName())) {
        if (playNewMsgMedia) {
            defaultMediaPlayer();
        }
        if (isVibrate) {
            vibrate();
        }
//            return;
//        }
        String showTitle = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
        String showContent = MSBaseApplication.getInstance().getContext().getString(R.string.default_new_msg);
        if (msgShowDetail == 1 && msg.baseContentMsgModel != null && !TextUtils.isEmpty(msg.baseContentMsgModel.getDisplayContent())) {
            showContent = msg.baseContentMsgModel.getDisplayContent();
        }
        PushNotificationHelper.INSTANCE.notifyMessage(MSUIKitApplication.getInstance().getContext(), 1, showTitle, showContent);
    }


    private void defaultMediaPlayer() {
        EndpointManager.getInstance().invoke("play_new_msg_Media", null);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) MSUIKitApplication.getInstance().getContext().getSystemService(Service.VIBRATOR_SERVICE);
        long[] pattern = {100, 200};
        vibrator.vibrate(pattern, -1);
    }

    public void removeListener() {
        MSIM.getInstance().getCMDManager().removeCmdListener("system");
        MSIM.getInstance().getMsgManager().removeNewMsgListener("system");
    }


}
