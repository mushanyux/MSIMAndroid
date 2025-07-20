package com.chat.base.endpoint.entity;

/**
 * 通讯录红点
 */
public class MailListDot {
    public int numCount;
    public boolean showDot;

    public MailListDot(int numCount, boolean showDot) {
        this.showDot = showDot;
        this.numCount = numCount;
    }
}
