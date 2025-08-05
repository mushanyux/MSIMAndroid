package com.chat.uikit.message;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chat.base.base.MSBaseModel;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSConstants;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.db.MSBaseCMD;
import com.chat.base.db.MSBaseCMDManager;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.ICommonListener;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.net.entity.CommonResponse;
import com.chat.base.net.ud.MSDownloader;
import com.chat.base.net.ud.MSProgressManager;
import com.chat.base.net.ud.MSUploader;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.MSLogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.uikit.MSUIKitApplication;
import com.chat.uikit.enity.SensitiveWords;
import com.chat.uikit.enity.MSSyncReminder;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelState;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSConversationMsg;
import com.mushanyux.mushanim.entity.MSConversationMsgExtra;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.entity.MSReminder;
import com.mushanyux.mushanim.entity.MSSyncChannelMsg;
import com.mushanyux.mushanim.entity.MSSyncChat;
import com.mushanyux.mushanim.entity.MSSyncConvMsgExtra;
import com.mushanyux.mushanim.entity.MSSyncExtraMsg;
import com.mushanyux.mushanim.entity.MSSyncMsg;
import com.mushanyux.mushanim.interfaces.ISyncChannelMsgBack;
import com.mushanyux.mushanim.interfaces.ISyncConversationChatBack;
import com.mushanyux.mushanim.message.type.MSMsgContentType;
import com.mushanyux.mushanim.message.type.MSSendMsgResult;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 消息管理
 */
public class MsgModel extends MSBaseModel {
    private MsgModel() {

    }
   public List<MSChannelState> channelStatus;
    private int last_message_seq;

    private static class MsgModelBinder {
        final static MsgModel msgModel = new MsgModel();
    }

    public static MsgModel getInstance() {
        return MsgModelBinder.msgModel;
    }

    private Timer timer;

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public synchronized void startCheckFlameMsgTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    deleteFlameMsg();
                }
            }, 100, 1000);
        }
    }

    public void deleteFlameMsg() {
        if (!MSConstants.isLogin()) return;
        List<MSMsg> list = MSIM.getInstance().getMsgManager().getWithFlame();
        if (MSReader.isEmpty(list)) return;
        List<String> deleteClientMsgNoList = new ArrayList<>();
        List<MSMsg> deleteMsgList = new ArrayList<>();
        boolean isStopTimer = true;
        for (MSMsg msg : list) {
            if (msg.flame == 1 && msg.viewed == 1) {
                long time = MSTimeUtils.getInstance().getCurrentMills() - msg.viewedAt;
                if (time / 1000 > msg.flameSecond || msg.flameSecond == 0) {
                    deleteClientMsgNoList.add(msg.clientMsgNO);
                    deleteMsgList.add(msg);
                }
                isStopTimer = false;
            }
        }
        if (isStopTimer && timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        deleteMsg(deleteMsgList, null);
        MSIM.getInstance().getMsgManager().deleteWithClientMsgNos(deleteClientMsgNoList);
    }

    private void ackMsg() {
        request(createService(MsgService.class).ackMsg(last_message_seq), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {

            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    /**
     * 删除消息
     */
    public void deleteMsg(List<MSMsg> list, final ICommonListener iCommonListener) {
        if (MSReader.isEmpty(list)) return;
        JSONArray jsonArray = new JSONArray();
        for (MSMsg msg : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message_id", msg.messageID);
            jsonObject.put("channel_id", msg.channelID);
            jsonObject.put("channel_type", msg.channelType);
            jsonObject.put("message_seq", msg.messageSeq);
            jsonArray.add(jsonObject);
        }
        request(createService(MsgService.class).deleteMsg(jsonArray), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {
                if (iCommonListener != null)
                    iCommonListener.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                if (iCommonListener != null)
                    iCommonListener.onResult(code, msg);
            }
        });
    }

    public void offsetMsg(String channelID, byte channelType, ICommonListener iCommonListener) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channel_id", channelID);
        jsonObject.put("channel_type", channelType);
        int msgSeq = MSIM.getInstance().getMsgManager().getMaxMessageSeqWithChannel(channelID, channelType);
        jsonObject.put("message_seq", msgSeq);
        request(createService(MsgService.class).offsetMsg(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {
                if (iCommonListener != null)
                    iCommonListener.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                if (iCommonListener != null)
                    iCommonListener.onResult(code, msg);
            }
        });
    }

    /**
     * 撤回消息
     *
     * @param msgId           消息ID
     * @param channelID       频道ID
     * @param channelType     频道类型
     * @param iCommonListener 返回
     */
    public void revokeMsg(String msgId, String channelID, byte channelType, String clientMsgNo, final ICommonListener iCommonListener) {
        request(createService(MsgService.class).revokeMsg(msgId, channelID, channelType, clientMsgNo), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {
                iCommonListener.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                iCommonListener.onResult(code, msg);
            }
        });
    }

    /**
     * 同步红点
     *
     * @param channelId   频道ID
     * @param channelType 频道类型
     */
    public void clearUnread(String channelId, byte channelType, int unreadCount, ICommonListener iCommonListener) {
        if (unreadCount < 0) unreadCount = 0;
        MSIM.getInstance().getConversationManager().updateRedDot(channelId, channelType, unreadCount);
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put("channel_id", channelId);
        jsonObject.put("channel_type", channelType);
        jsonObject.put("unread", unreadCount);
        request(createService(MsgService.class).clearUnread(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {
                if (iCommonListener != null)
                    iCommonListener.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
            }
        });
    }

    /**
     * 修改语音已读
     *
     * @param messageID 服务器消息ID
     */
    public void updateVoiceStatus(String messageID, String channel_id, byte channel_type, int message_seq) {
        if (TextUtils.isEmpty(messageID)) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message_id", messageID);
        jsonObject.put("channel_id", channel_id);
        jsonObject.put("channel_type", channel_type);
        jsonObject.put("message_seq", message_seq);
        request(createService(MsgService.class).updateVoiceStatus(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {
            }

            @Override
            public void onFail(int code, String msg) {
            }
        });
    }

    public void getChatIp(IChatIp iChatIp) {
        request(createService(MsgService.class).getImIp(MSConfig.getInstance().getUid()), new IRequestResultListener<>() {
            @Override
            public void onSuccess(Ipentity result) {
                if (result != null && !TextUtils.isEmpty(result.tcp_addr)) {
                    String[] strings = result.tcp_addr.split(":");
                    iChatIp.onResult(HttpResponseCode.success, strings[0], strings[1]);
                }
            }

            @Override
            public void onFail(int code, String msg) {
                iChatIp.onResult(code, "", "0");
            }
        });
    }

    public interface IChatIp {
        void onResult(int code, String ip, String port);
    }

    public void typing(String channelID, byte channelType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channel_id", channelID);
        jsonObject.put("channel_type", channelType);
        request(createService(MsgService.class).typing(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {

            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    private MSSyncMsg getMSSyncMsg(SyncMsg syncMsg) {
        MSMsg msg = new MSMsg();
        MSSyncMsg MSSyncMsg = new MSSyncMsg();
        msg.status = MSSendMsgResult.send_success;
        msg.messageID = syncMsg.message_id;
        msg.messageSeq = syncMsg.message_seq;
        msg.clientMsgNO = syncMsg.client_msg_no;
        msg.fromUID = syncMsg.from_uid;
        msg.channelID = syncMsg.channel_id;
        msg.channelType = syncMsg.channel_type;
        msg.voiceStatus = syncMsg.voice_status;
        msg.timestamp = syncMsg.timestamp;
        msg.isDeleted = syncMsg.is_delete;
        msg.remoteExtra.unreadCount = syncMsg.unread_count;
        msg.remoteExtra.readedCount = syncMsg.readed_count;
        msg.remoteExtra.extraVersion = syncMsg.extra_version;
        if (syncMsg.payload != null)
            msg.content = JSONObject.toJSONString(syncMsg.payload);
        if (syncMsg.payload != null && syncMsg.payload.containsKey("type")) {
            Object typeObject = syncMsg.payload.get("type");
            if (typeObject != null)
                msg.type = (int) typeObject;
        }
        MSSyncMsg.msMsg = msg;
        MSSyncMsg.red_dot = syncMsg.header.red_dot;
        MSSyncMsg.sync_once = syncMsg.header.sync_once;
        MSSyncMsg.no_persist = syncMsg.header.no_persist;
        return MSSyncMsg;
    }

    /**
     * 同步会话
     *
     * @param last_msg_seqs 最后一条消息的msgseq数组
     * @param msg_count     同步消息条数
     * @param version       最大版本号
     */
    public void syncChat(String last_msg_seqs, int msg_count, long version, ISyncConversationChatBack iSyncConversationChatBack) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("last_msg_seqs", last_msg_seqs);
        jsonObject.put("msg_count", msg_count);
        jsonObject.put("version", version);
        jsonObject.put("device_uuid", MSConstants.getDeviceUUID());
        request(createService(MsgService.class).syncChat(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(MSSyncChat result) {
                if (result != null && !TextUtils.isEmpty(result.uid) && result.uid.equals(MSConfig.getInstance().getUid())) {
                    if (MSReader.isNotEmpty(result.conversations)) {
                        MSUIKitApplication.getInstance().isRefreshChatActivityMessage = true;
                    }
                    channelStatus = result.channel_status;
                    iSyncConversationChatBack.onBack(result);
                    last_message_seq = 0;
                    syncCmdMsgs(0);
                    ackDeviceUUID();
                    syncReminder();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> EndpointManager.getInstance().invoke("refresh_conversation_calling",null),300);
                } else {
                    iSyncConversationChatBack.onBack(null);
                }
            }

            @Override
            public void onFail(int code, String msg) {
                iSyncConversationChatBack.onBack(null);
            }
        });

    }

    public void ackDeviceUUID() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("device_uuid", MSConstants.getDeviceUUID());
        request(createService(MsgService.class).ackCoverMsg(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {

            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    /**
     * 同步某个频道的消息
     *
     * @param channelID           频道ID
     * @param channelType         频道类型
     * @param startMessageSeq     最小messageSeq
     * @param endMessageSeq       最大messageSeq
     * @param limit               获取条数
     * @param pullMode            拉取模式 0:向下拉取 1:向上拉取
     * @param iSyncChannelMsgBack 返回
     */
    public void syncChannelMsg(String channelID, byte channelType, long startMessageSeq, long endMessageSeq, int limit, int pullMode, final ISyncChannelMsgBack iSyncChannelMsgBack) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channel_id", channelID);
        jsonObject.put("channel_type", channelType);
        jsonObject.put("start_message_seq", startMessageSeq);
        jsonObject.put("end_message_seq", endMessageSeq);
        jsonObject.put("limit", limit);
        jsonObject.put("pull_mode", pullMode);
        jsonObject.put("device_uuid", MSConstants.getDeviceUUID());
        request(createService(MsgService.class).syncChannelMsg(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(MSSyncChannelMsg result) {
                iSyncChannelMsgBack.onBack(result);
                ackDeviceUUID();
            }

            @Override
            public void onFail(int code, String msg) {
                iSyncChannelMsgBack.onBack(null);
            }
        });
    }

    /**
     * 同步cmd消息
     *
     * @param max_message_seq 最大消息编号
     */
    private void syncCmdMsgs(long max_message_seq) {

        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("limit", 500);
        jsonObject1.put("max_message_seq", max_message_seq);
        request(createService(MsgService.class).syncMsg(jsonObject1), new IRequestResultListener<>() {
            @Override
            public void onSuccess(List<SyncMsg> list) {
                if (MSReader.isNotEmpty(list)) {
                    List<MSBaseCMD> cmdList = new ArrayList<>();
                    for (int i = 0, size = list.size(); i < size; i++) {
                        MSSyncMsg MSSyncMsg = getMSSyncMsg(list.get(i));
                        MSBaseCMD MSBaseCmd = new MSBaseCMD();
                        if (MSSyncMsg.msMsg.type == MSMsgContentType.MS_INSIDE_MSG) {
                            MSBaseCmd.client_msg_no = MSSyncMsg.msMsg.clientMsgNO;
                            MSBaseCmd.created_at = MSSyncMsg.msMsg.createdAt;
                            MSBaseCmd.message_id = MSSyncMsg.msMsg.messageID;
                            MSBaseCmd.message_seq = MSSyncMsg.msMsg.messageSeq;
                            MSBaseCmd.timestamp = MSSyncMsg.msMsg.timestamp;
                            try {
                                org.json.JSONObject jsonObject = new org.json.JSONObject(MSSyncMsg.msMsg.content);
                                if (jsonObject.has("cmd")) {
                                    MSBaseCmd.cmd = jsonObject.optString("cmd");
                                }
                                if (jsonObject.has("sign")) {
                                    MSBaseCmd.sign = jsonObject.optString("sign");
                                }
                                if (jsonObject.has("param")) {
                                    org.json.JSONObject paramJson = jsonObject.optJSONObject("param");
                                    if (paramJson != null) {
                                        if (!paramJson.has("channel_id") && !TextUtils.isEmpty(MSSyncMsg.msMsg.channelID)) {
                                            paramJson.put("channel_id", MSSyncMsg.msMsg.channelID);
                                        }
                                        if (!paramJson.has("channel_type")) {
                                            paramJson.put("channel_type", MSSyncMsg.msMsg.channelType);
                                        }
                                        MSBaseCmd.param = paramJson.toString();
                                    }
                                }
                            } catch (JSONException e) {
                                MSLogUtils.e("MsgModel", "cmd messages not json struct");
                            }
                            cmdList.add(MSBaseCmd);
                        }
                        if (MSSyncMsg.msMsg.messageSeq > last_message_seq) {
                            last_message_seq = MSSyncMsg.msMsg.messageSeq;
                        }
                    }
                    //保存cmd
                    MSBaseCMDManager.getInstance().addCmd(cmdList);
                    if (last_message_seq != 0) {
                        ackMsg();
                    }
                    AndroidUtilities.runOnUIThread(() -> syncCmdMsgs(last_message_seq),1000);

                } else {
                    if (last_message_seq != 0) {
                        ackMsg();
                    }
                    //处理cmd
                    MSBaseCMDManager.getInstance().handleCmd();
                }
            }

            @Override
            public void onFail(int code, String msg) {
                if (last_message_seq != 0) {
                    ackMsg();
                    MSBaseCMDManager.getInstance().handleCmd();
                }
            }
        });
    }

    /**
     * 同步某个会话的扩展消息
     *
     * @param channelID   频道ID
     * @param channelType 频道类型
     */
    public void syncExtraMsg(String channelID, byte channelType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channel_id", channelID);
        jsonObject.put("channel_type", channelType);
        long maxExtraVersion = MSIM.getInstance().getMsgManager().getMsgExtraMaxVersionWithChannel(channelID, channelType);
        jsonObject.put("extra_version", maxExtraVersion);
        jsonObject.put("limit", 100);
        String deviceUUID = MSConstants.getDeviceUUID();
        jsonObject.put("source", deviceUUID);
        request(createService(MsgService.class).syncExtraMsg(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(List<MSSyncExtraMsg> result) {
                if (MSReader.isNotEmpty(result)) {
                    // 更改扩展消息
                    MSIM.getInstance().getMsgManager().saveRemoteExtraMsg(new MSChannel(channelID, channelType), result);
                    new Handler(Looper.getMainLooper()).postDelayed(() -> syncExtraMsg(channelID, channelType), 500);
                }
            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }


    // 同步敏感词
    public void syncSensitiveWords() {
        if (TextUtils.isEmpty(MSConfig.getInstance().getToken())) return;
        long version = MSSharedPreferencesUtil.getInstance().getLong("ms_sensitive_words_version");
        request(createService(MsgService.class).syncSensitiveWords(version), new IRequestResultListener<>() {
            @Override
            public void onSuccess(SensitiveWords result) {
                MSSharedPreferencesUtil.getInstance().putLong("ms_sensitive_words_version", result.version);
                if (!TextUtils.isEmpty(result.tips)) {
                    MSUIKitApplication.getInstance().sensitiveWords = result;
                    String json = JSON.toJSONString(result);
                    MSSharedPreferencesUtil.getInstance().putSP("ms_sensitive_words", json);
                }
            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    public void editMsg(String msgID, int msgSeq, String channelID, byte channelType, String content, ICommonListener iCommonListener) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message_id", msgID);
        jsonObject.put("message_seq", msgSeq);
        jsonObject.put("channel_id", channelID);
        jsonObject.put("channel_type", channelType);
        jsonObject.put("content_edit", content);
        request(createService(MsgService.class).editMsg(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {
                if (iCommonListener != null)
                    iCommonListener.onResult(result.status, result.msg);
            }

            @Override
            public void onFail(int code, String msg) {
                if (iCommonListener != null)
                    iCommonListener.onResult(code, msg);
            }
        });
    }

    public void syncReminder() {
        long version = MSIM.getInstance().getReminderManager().getMaxVersion();
        List<String> channelIDs = new ArrayList<>();
        List<MSConversationMsg> list = MSIM.getInstance().getConversationManager().getWithChannelType(MSChannelType.GROUP);
        for (MSConversationMsg mConversationMsg : list) {
            channelIDs.add(mConversationMsg.channelID);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version", version);
        jsonObject.put("limit", 200);
        jsonObject.put("channel_ids", channelIDs);
        request(createService(MsgService.class).syncReminder(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(List<MSSyncReminder> result) {
                if (MSReader.isNotEmpty(result)) {
                    String loginUID = MSConfig.getInstance().getUid();
                    List<MSReminder> list = new ArrayList<>();
                    for (MSSyncReminder reminder : result) {
                        MSReminder MSReminder = syncReminderToReminder(reminder);
                        if (!TextUtils.isEmpty(reminder.publisher) && reminder.publisher.equals(loginUID)) {
                            MSReminder.done = 1;
                        }
                        list.add(MSReminder);
                    }
                    MSIM.getInstance().getReminderManager().saveOrUpdateReminders(list);

                }

            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    public void doneReminder(List<Long> list) {
        if (MSReader.isEmpty(list)) return;
        request(createService(MsgService.class).doneReminder(list), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {

            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    public void updateCoverExtra(String channelID, byte channelType, long browseTo, long keepMsgSeq, int keepOffsetY, String draft) {
        MSConversationMsgExtra extra = new MSConversationMsgExtra();
        extra.draft = draft;
        extra.keepOffsetY = keepOffsetY;
        extra.keepMessageSeq = keepMsgSeq;
        extra.channelID = channelID;
        extra.channelType = channelType;
        extra.browseTo = browseTo;
        if (!TextUtils.isEmpty(draft)) {
            extra.draftUpdatedAt = MSTimeUtils.getInstance().getCurrentSeconds();
        }
        MSIM.getInstance().getConversationManager().updateMsgExtra(extra);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("browse_to", browseTo);
        jsonObject.put("keep_message_seq", keepMsgSeq);
        jsonObject.put("keep_offset_y", keepOffsetY);
        jsonObject.put("draft", draft);
        request(createService(MsgService.class).updateCoverExtra(channelID, channelType, jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(CommonResponse result) {

            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    public void syncCoverExtra() {
        long version = MSIM.getInstance().getConversationManager().getMsgExtraMaxVersion();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("version", version);
        request(createService(MsgService.class).syncCoverExtra(jsonObject), new IRequestResultListener<>() {
            @Override
            public void onSuccess(List<MSSyncConvMsgExtra> result) {
                MSIM.getInstance().getConversationManager().saveSyncMsgExtras(result);
            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    private MSReminder syncReminderToReminder(MSSyncReminder syncReminder) {
        MSReminder reminder = new MSReminder();
        reminder.reminderID = syncReminder.id;
        reminder.channelID = syncReminder.channel_id;
        reminder.channelType = syncReminder.channel_type;
        reminder.messageSeq = syncReminder.message_seq;
        reminder.type = syncReminder.reminder_type;
        reminder.isLocate = syncReminder.is_locate;
        reminder.text = syncReminder.text;
        reminder.version = syncReminder.version;
        reminder.messageID = syncReminder.message_id;
        reminder.uid = syncReminder.uid;
        reminder.done = syncReminder.done;
        reminder.data = syncReminder.data;
        reminder.publisher = syncReminder.publisher;
        return reminder;
    }

    public void backupMsg(String filePath, ICommonListener iCommonListener) {
        String url = MSApiConfig.baseUrl + "message/backup";
        MSUploader.getInstance().upload(url, filePath, new MSUploader.IUploadBack() {
            @Override
            public void onSuccess(String url) {
                iCommonListener.onResult(HttpResponseCode.success, "");
            }

            @Override
            public void onError() {
                iCommonListener.onResult(HttpResponseCode.error, "");
            }
        });
    }

    public void recovery(final IRecovery iRecovery) {
        String uid = MSConfig.getInstance().getUid();
        String url = MSApiConfig.baseUrl + "message/recovery";
        String path = MSConstants.messageBackupDir + uid + "_recovery.json";
        MSDownloader.Companion.getInstance().download(url, path, new MSProgressManager.IProgress() {
            @Override
            public void onProgress(@Nullable Object tag, int progress) {

            }

            @Override
            public void onSuccess(@Nullable Object tag, @Nullable String path) {
                iRecovery.onSuccess(path);
            }

            @Override
            public void onFail(@Nullable Object tag, @Nullable String msg) {
                iRecovery.onFail();
            }
        });
    }

    public interface IRecovery {
        void onSuccess(String path);

        void onFail();
    }
}
