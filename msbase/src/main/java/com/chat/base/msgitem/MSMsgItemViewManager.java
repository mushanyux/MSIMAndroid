package com.chat.base.msgitem;

import com.chad.library.adapter.base.provider.BaseItemProvider;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 各类消息itemView管理
 */
public class MSMsgItemViewManager {

    private MSMsgItemViewManager() {
    }

    private static class MsgItemViewManagerBinder {
        final static MSMsgItemViewManager itemView = new MSMsgItemViewManager();
    }

    public static MSMsgItemViewManager getInstance() {
        return MsgItemViewManagerBinder.itemView;
    }

    private ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> chatItemProviderList;
    private ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> pinnedChatItemProviderList;


    public void addChatItemViewProvider(int type, BaseItemProvider<MSUIChatMsgItemEntity> itemProvider) {
        if (chatItemProviderList == null) {
            chatItemProviderList = new ConcurrentHashMap<>();
            chatItemProviderList.put(MSContentType.MS_SIGNAL_DECRYPT_ERROR, new MSSignalDecryptErrorProvider());
            chatItemProviderList.put(MSContentType.MS_CONTENT_FORMAT_ERROR, new MSChatFormatErrorProvider());
            chatItemProviderList.put(MSContentType.unknown_msg, new MSUnknownProvider());
            chatItemProviderList.put(MSContentType.typing, new MSTypingProvider());
            chatItemProviderList.put(MSContentType.revoke, new MSRevokeProvider());
            chatItemProviderList.put(MSContentType.systemMsg, new MSSystemProvider(MSContentType.systemMsg));
            chatItemProviderList.put(MSContentType.msgPromptTime, new MSSystemProvider(MSContentType.msgPromptTime));
            for (int i = 1000; i <= 2000; i++) {
                chatItemProviderList.put(i, new MSSystemProvider(i));
            }
        }
        chatItemProviderList.put(type, itemProvider);
        // 置顶消息的itemProvider
        if (pinnedChatItemProviderList == null) {
            pinnedChatItemProviderList = new ConcurrentHashMap<>();
            pinnedChatItemProviderList.put(MSContentType.MS_SIGNAL_DECRYPT_ERROR, new MSSignalDecryptErrorProvider());
            pinnedChatItemProviderList.put(MSContentType.MS_CONTENT_FORMAT_ERROR, new MSChatFormatErrorProvider());
            pinnedChatItemProviderList.put(MSContentType.unknown_msg, new MSUnknownProvider());
            pinnedChatItemProviderList.put(MSContentType.typing, new MSTypingProvider());
            pinnedChatItemProviderList.put(MSContentType.revoke, new MSRevokeProvider());
            pinnedChatItemProviderList.put(MSContentType.systemMsg, new MSSystemProvider(MSContentType.systemMsg));
            pinnedChatItemProviderList.put(MSContentType.msgPromptTime, new MSSystemProvider(MSContentType.msgPromptTime));
            for (int i = 1000; i <= 2000; i++) {
                pinnedChatItemProviderList.put(i, new MSSystemProvider(i));
            }
        }
        try {
            Object myObject = itemProvider.getClass().newInstance();
            pinnedChatItemProviderList.put(type, (BaseItemProvider<MSUIChatMsgItemEntity>) myObject);
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

    }

    public ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> getChatItemProviderList() {
        return chatItemProviderList;
    }

    public ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> getPinnedChatItemProviderList() {
        return pinnedChatItemProviderList;
    }

    public BaseItemProvider<MSUIChatMsgItemEntity> getItemProvider(Integer type) {
        if (chatItemProviderList != null) {
            return chatItemProviderList.get(type);
        }
        return null;
    }

    public BaseItemProvider<MSUIChatMsgItemEntity> getPinnedItemProvider(Integer type) {
        if (pinnedChatItemProviderList != null) {
            return pinnedChatItemProviderList.get(type);
        }
        return null;
    }
}
