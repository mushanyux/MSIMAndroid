package com.chat.uikit.chat.adapter;

import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.endpoint.entity.ChatToolBarMenu;
import com.chat.base.ui.Theme;
import com.chat.uikit.R;

import org.jetbrains.annotations.NotNull;

/**
 * 工具栏
 */
public class MSChatToolBarAdapter extends BaseQuickAdapter<ChatToolBarMenu, BaseViewHolder> {
    public MSChatToolBarAdapter() {
        super(R.layout.ms_item_chat_tool_bar);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, ChatToolBarMenu chatToolBarMenu) {
        ImageView imageView = baseViewHolder.getView(R.id.imageView);
        imageView.setImageAlpha(chatToolBarMenu.isDisable ? 100 : 255);
        Theme.setPressedBackground(imageView);
        if (chatToolBarMenu.isSelected) {
            if (chatToolBarMenu.toolBarImageSelectedRecourseID != 0) {
                imageView.setImageResource(chatToolBarMenu.toolBarImageSelectedRecourseID);
            } else
                imageView.setImageResource(chatToolBarMenu.toolBarImageRecourseID);
        } else {
            imageView.setImageResource(chatToolBarMenu.toolBarImageRecourseID);
        }

    }
}
