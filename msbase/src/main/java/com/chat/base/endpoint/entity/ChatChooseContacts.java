package com.chat.base.endpoint.entity;


import com.mushanyux.mushanim.entity.MSChannel;

import java.util.List;

/**
 * 选择联系人
 */
public class ChatChooseContacts {
    public IChoose iChoose;

    public ChatChooseContacts(IChoose iChoose) {
        this.iChoose = iChoose;
    }

    public interface IChoose {
        void onResult(List<MSChannel> list);
    }
}
