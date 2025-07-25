package com.chat.base.endpoint.entity;

import androidx.activity.ComponentActivity;

import com.mushanyux.mushanim.msgmodel.MSMessageContent;

import java.util.List;

/**
 * 显示聊天信息
 */
public class ChatViewMenu {
    public String channelID;
    public byte channelType;
    // tipMsgOrderSeq >0 需要强提醒某条消息 场景：搜索进入聊天等
    // tipMsgOrderSeq =0 正常会话列表进入聊天
    public long tipMsgOrderSeq;
    public ComponentActivity activity;
    public boolean isNewTask;
    public List<MSMessageContent> forwardMsgList;

    public ChatViewMenu(ComponentActivity activity, String channelID, byte channelType, long tipMsgOrderSeq, boolean isNewTask) {
        this.channelID = channelID;
        this.channelType = channelType;
        this.tipMsgOrderSeq = tipMsgOrderSeq;
        this.activity = activity;
        this.isNewTask = isNewTask;
        this.forwardMsgList = null;
    }

    public ChatViewMenu(ComponentActivity activity, String channelID, byte channelType, long tipMsgOrderSeq, boolean isNewTask, List<MSMessageContent> forwardMsgList) {
        this.channelID = channelID;
        this.channelType = channelType;
        this.tipMsgOrderSeq = tipMsgOrderSeq;
        this.activity = activity;
        this.isNewTask = isNewTask;
        this.forwardMsgList = forwardMsgList;
    }
}
