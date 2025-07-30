package com.chat.uikit.enity;

import android.text.TextUtils;

import com.chat.base.config.MSConfig;
import com.chat.base.utils.MSReader;
import com.chat.uikit.chat.manager.MSIMUtils;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.entity.MSReminder;
import com.mushanyux.mushanim.entity.MSUIConversationMsg;

import java.util.ArrayList;
import java.util.List;

public class ChatConversationMsg {
    public MSUIConversationMsg uiConversationMsg;
    public boolean isRefreshChannelInfo;
    public boolean isResetCounter;
    public boolean isResetReminders;
    public boolean isResetContent;
    public boolean isResetTime;
    public boolean isResetTyping;
    public boolean isRefreshStatus;
    public long typingStartTime = 0;
    public String typingUserName;
    public int isTop;
    public List<ChatConversationMsg> childList;
    private final String loginUID;
    public int isCalling = 0;

    public ChatConversationMsg(MSUIConversationMsg msg) {
        this.uiConversationMsg = msg;
        if (uiConversationMsg.getMsChannel() != null) {
            isTop = uiConversationMsg.getMsChannel().top;
        }
        loginUID = MSConfig.getInstance().getUid();
        MSIMUtils.getInstance().resetMsgProhibitWord(msg.getMsMsg());
    }

    public int getUnReadCount() {
        if (MSReader.isEmpty(childList))
            return uiConversationMsg.unreadCount;
        int count = 0;
        for (ChatConversationMsg msg : childList) {
            count += msg.uiConversationMsg.unreadCount;
        }
        return count;
    }

    public List<MSReminder> getReminders() {
        List<MSReminder> list = new ArrayList<>();
        if (MSReader.isEmpty(childList)) {
            list.addAll(uiConversationMsg.getReminderList());
        } else {
            for (ChatConversationMsg msg : childList) {
                list.addAll(msg.uiConversationMsg.getReminderList());
            }
        }
        List<MSReminder> resultList = new ArrayList<>();
        for (MSReminder reminder : list) {
            if (TextUtils.isEmpty(reminder.publisher) || (!TextUtils.isEmpty(reminder.publisher) && !reminder.publisher.equals(loginUID))) {
                resultList.add(reminder);
            }
        }
        return resultList;
    }

    private MSMsg lastMsg;
    private String lastClientMsgNo = "";

    public MSMsg getMsg() {
        if (MSReader.isEmpty(childList))
            return uiConversationMsg.getMsMsg();
        String clientMsgNo = "";
        long lastMsgTimestamp = 0;
        for (ChatConversationMsg msg : childList) {
            if (msg.uiConversationMsg.lastMsgTimestamp > lastMsgTimestamp) {
                lastMsgTimestamp = msg.uiConversationMsg.lastMsgTimestamp;
                clientMsgNo = msg.uiConversationMsg.clientMsgNo;
            }
        }
        if (lastClientMsgNo.equals(clientMsgNo) && lastMsg != null) {
            return lastMsg;
        }

        lastClientMsgNo = clientMsgNo;
        lastMsg = MSIM.getInstance().getMsgManager().getWithClientMsgNO(lastClientMsgNo);
        return lastMsg;
    }
}
