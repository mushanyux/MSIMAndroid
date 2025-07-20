package com.chat.base.endpoint.entity;

import com.chat.base.msg.IConversationContext;

public class RTCMenu {

    public int callType;//0语音1视频
    public IConversationContext iConversationContext;
    public RTCMenu(IConversationContext iConversationContext, int callType) {
       this.iConversationContext = iConversationContext;
        this.callType = callType;
    }
}
