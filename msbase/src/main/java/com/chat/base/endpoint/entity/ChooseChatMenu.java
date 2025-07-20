package com.chat.base.endpoint.entity;



import com.mushanyux.mushanim.msgmodel.MSMessageContent;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择会话
 */
public class ChooseChatMenu {
    public ChatChooseContacts mChatChooseContacts;
    public List<MSMessageContent> list;

    public ChooseChatMenu(ChatChooseContacts mChatChooseContacts, MSMessageContent messageContent) {
        this.mChatChooseContacts = mChatChooseContacts;
        list = new ArrayList<>();
        list.add(messageContent);
    }

    public ChooseChatMenu(ChatChooseContacts mChatChooseContacts, List<MSMessageContent> list) {
        this.mChatChooseContacts = mChatChooseContacts;
        this.list = list;
    }
}
