package com.chat.uikit.chat.manager;

import android.text.TextUtils;

import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.MSSendMsgMenu;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.net.ud.MSUploader;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.entity.MSSendOptions;
import com.mushanyux.mushanim.interfaces.IUploadAttacResultListener;
import com.mushanyux.mushanim.msgmodel.MSMediaMessageContent;
import com.mushanyux.mushanim.msgmodel.MSVideoContent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * 发送消息管理
 */
public class MSSendMsgUtils {
    private MSSendMsgUtils() {

    }

    private static class SendMsgUtilsBinder {
        private static final MSSendMsgUtils utils = new MSSendMsgUtils();
    }

    public static MSSendMsgUtils getInstance() {
        return SendMsgUtilsBinder.utils;
    }

    public void sendMessage(MSMsg msMsg) {
        MSSendOptions options = new MSSendOptions();
        options.robotID = msMsg.robotID;
        MSChannel channel = msMsg.getChannelInfo();
        if (channel == null) {
            channel = new MSChannel(msMsg.channelID, msMsg.channelType);
        }
        EndpointManager.getInstance().invokes(EndpointSID.sendMessage, new MSSendMsgMenu(channel, options));
        MSIM.getInstance().getMsgManager().sendWithOptions(msMsg.baseContentMsgModel, channel, options);
    }

    public void sendMessages(List<SendMsgEntity> list) {
        final Timer[] timer = {new Timer()};
        final int[] i = {0};
        timer[0].schedule(new TimerTask() {
            @Override
            public void run() {
                if (i[0] == list.size() - 1) {
                    timer[0].cancel();
                    timer[0] = null;
                }
                MSMsg msMsg = new MSMsg();
                msMsg.channelID = list.get(i[0]).msChannel.channelID;
                msMsg.channelType = list.get(i[0]).msChannel.channelType;
                msMsg.type = list.get(i[0]).messageContent.type;
                msMsg.baseContentMsgModel = list.get(i[0]).messageContent;
                sendMessage(msMsg);
                i[0]++;
            }
        }, 0, 150);
    }

    /**
     * 上传聊天附件
     *
     * @param msg      消息
     * @param listener 上传返回
     */
    void uploadChatAttachment(MSMsg msg, IUploadAttacResultListener listener) {
        //存在附件待上传
        if (msg.type == MSContentType.MS_IMAGE || msg.type == MSContentType.MS_GIF || msg.type == MSContentType.MS_VOICE || msg.type == MSContentType.MS_LOCATION || msg.type == MSContentType.MS_FILE) {
            MSMediaMessageContent contentMsgModel = (MSMediaMessageContent) msg.baseContentMsgModel;
            //已经有网络地址无需在上传
            if (!TextUtils.isEmpty(contentMsgModel.url)) {
                listener.onUploadResult(true, contentMsgModel);
            } else {
                if (!TextUtils.isEmpty(contentMsgModel.localPath)) {
                    MSUploader.getInstance().getUploadFileUrl(msg.channelID, msg.channelType, contentMsgModel.localPath, (url, filePath) -> {
                        if (!TextUtils.isEmpty(url)) {
                            MSUploader.getInstance().upload(url, contentMsgModel.localPath, msg.clientSeq, new MSUploader.IUploadBack() {
                                @Override
                                public void onSuccess(String url) {
                                    contentMsgModel.url = url;
                                    listener.onUploadResult(true, contentMsgModel);
                                }

                                @Override
                                public void onError() {
                                    listener.onUploadResult(false, contentMsgModel);
                                }
                            });
                        } else {
                            listener.onUploadResult(false, contentMsgModel);
                        }
                    });
                } else {
                    listener.onUploadResult(false, msg.baseContentMsgModel);
                }
            }

        } else if (msg.type == MSContentType.MS_VIDEO) {
            //视频
            MSVideoContent videoMsgModel = (MSVideoContent) msg.baseContentMsgModel;
            if (!TextUtils.isEmpty(videoMsgModel.cover) && !TextUtils.isEmpty(videoMsgModel.url)) {
                listener.onUploadResult(true, msg.baseContentMsgModel);
            } else {
                if (TextUtils.isEmpty(videoMsgModel.cover)) {
                    MSUploader.getInstance().getUploadFileUrl(msg.channelID, msg.channelType, videoMsgModel.coverLocalPath, (url, filePath) -> {
                        if (!TextUtils.isEmpty(url)) {
                            MSUploader.getInstance().upload(url, videoMsgModel.coverLocalPath, UUID.randomUUID().toString().replaceAll("-", ""),
                                    new MSUploader.IUploadBack() {
                                        @Override
                                        public void onSuccess(String url) {
                                            videoMsgModel.cover = url;
                                            MSUploader.getInstance().getUploadFileUrl(msg.channelID, msg.channelType, videoMsgModel.localPath, (url1, fileUrl) -> MSUploader.getInstance().upload(url1, videoMsgModel.localPath, msg.clientSeq, new MSUploader.IUploadBack() {
                                                @Override
                                                public void onSuccess(String url1) {
                                                    videoMsgModel.url = url1;
                                                    listener.onUploadResult(true, videoMsgModel);
                                                }

                                                @Override
                                                public void onError() {
                                                    listener.onUploadResult(false, videoMsgModel);
                                                }
                                            }));
                                        }

                                        @Override
                                        public void onError() {
                                            listener.onUploadResult(false, msg.baseContentMsgModel);
                                        }
                                    });
                        } else {
                            listener.onUploadResult(false, msg.baseContentMsgModel);
                        }
                    });
                }
            }
        }
    }
}
