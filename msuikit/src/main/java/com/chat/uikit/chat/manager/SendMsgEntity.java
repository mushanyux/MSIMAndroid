package com.chat.uikit.chat.manager;

import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSMsgSetting;
import com.mushanyux.mushanim.entity.MSSendOptions;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;

public class SendMsgEntity {
    public MSMessageContent messageContent;
    public MSChannel msChannel;
    public MSSendOptions options;

    public SendMsgEntity(MSMessageContent messageContent, MSChannel channel, MSSendOptions options) {
        this.msChannel = channel;
        this.messageContent = messageContent;
        this.options = options;
    }
}
