package com.chat.base.endpoint.entity;

import com.chat.base.msg.ChatAdapter;
import com.mushanyux.mushanim.entity.MSMsg;

/**
 * 消息回应
 */
public class MsgReactionMenu {
    public String emoji;
    public ChatAdapter chatAdapter;
    public int[] location;
    public MSMsg msMsg;

    public MsgReactionMenu(MSMsg msMsg, String emoji, ChatAdapter chatAdapter, int[] location) {
        this.emoji = emoji;
        this.msMsg = msMsg;
        this.chatAdapter = chatAdapter;
        this.location = location;
    }
}
