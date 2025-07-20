package com.chat.base.endpoint.entity;

import android.app.Activity;

import com.mushanyux.mushanim.entity.MSChannel;

import java.util.List;

public class CreateVideoCallMenu {
    public String channelID;
    public byte channelType;
    public List<MSChannel> MSChannels;
    public Activity activity;

    public CreateVideoCallMenu(Activity activity, String channelID, byte channelType, List<MSChannel> MSChannels) {
        this.MSChannels = MSChannels;
        this.activity = activity;
        this.channelID = channelID;
        this.channelType = channelType;
    }
}
