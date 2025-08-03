package com.chat.uikit.chat.search.image;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.chat.base.entity.GlobalMessage;

/**
 * 搜索聊天图片
 */
public class SearchImgEntity implements MultiItemEntity {
    public int itemType;
    public GlobalMessage message;
    public String url;
    public String date;

    @Override
    public int getItemType() {
        return itemType;
    }
}
