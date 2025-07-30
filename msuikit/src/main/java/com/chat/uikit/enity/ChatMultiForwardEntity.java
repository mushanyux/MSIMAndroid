package com.chat.uikit.enity;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.mushanyux.mushanim.entity.MSMsg;

/**
 * 合并转发
 */
public class ChatMultiForwardEntity implements MultiItemEntity {

    public int itemType;
    public String title;
    public MSMsg msg;

    @Override
    public int getItemType() {
        return itemType;
    }
}
