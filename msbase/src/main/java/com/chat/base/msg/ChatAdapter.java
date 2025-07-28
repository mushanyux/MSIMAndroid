package com.chat.base.msg;

import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseProviderMultiAdapter;
import com.chad.library.adapter.base.provider.BaseItemProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.R;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ShowMsgReactionMenu;
import com.chat.base.msgitem.MSChatBaseProvider;
import com.chat.base.msgitem.MSChatIteMsgFromType;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.msgitem.MSMsgItemViewManager;
import com.chat.base.msgitem.MSUIChatMsgItemEntity;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.ui.components.SecretDeleteTimer;
import com.chat.base.utils.MSReader;
import com.chat.base.views.ChatItemView;
import com.chat.base.views.pinnedsectionitemdecoration.utils.FullSpanUtil;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.entity.MSMsgReaction;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息适配器
 */
public class ChatAdapter extends BaseProviderMultiAdapter<MSUIChatMsgItemEntity> {
    private final IConversationContext iConversationContext;

    public enum AdapterType {
        normalMessage, pinnedMessage
    }


    @Override
    public void onAttachedToRecyclerView(@NotNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        FullSpanUtil.onAttachedToRecyclerView(recyclerView, this, MSContentType.msgPromptTime);
    }

    @Override
    public void onViewAttachedToWindow(@NotNull BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        FullSpanUtil.onViewAttachedToWindow(holder, this, MSContentType.msgPromptTime);
    }

    private final AdapterType adapterType;

    ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> getItemProviderList() {
        return adapterType == AdapterType.normalMessage ? MSMsgItemViewManager.getInstance().getChatItemProviderList() : MSMsgItemViewManager.getInstance().getPinnedChatItemProviderList();
    }

    public ChatAdapter(@NonNull IConversationContext iConversationContext, AdapterType adapterType) {
        super();
        this.adapterType = adapterType;
        this.iConversationContext = iConversationContext;
        ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> list = getItemProviderList();
        for (int type : list.keySet()) {
            addItemProvider(Objects.requireNonNull(list.get(type)));
        }
    }

    @Override
    protected int getItemType(@NotNull List<? extends MSUIChatMsgItemEntity> list, int i) {
        if (list.get(i).msMsg.remoteExtra != null && list.get(i).msMsg.remoteExtra.revoke == 1) {
            //撤回消息
            return MSContentType.revoke;
        }
        if (getItemProviderList().containsKey(list.get(i).msMsg.type)) {
            return list.get(i).msMsg.type;
        }
        if (list.get(i).msMsg.type >= 1000 && list.get(i).msMsg.type <= 2000) {
            //系统消息
            return MSContentType.systemMsg;
        }
        return MSContentType.unknown_msg;
    }

    public long getLastTimeMsg() {
        long timestamp = 0;
        for (int i = getData().size() - 1; i >= 0; i--) {
            if (getData().get(i).msMsg != null && getData().get(i).msMsg.timestamp > 0) {
                timestamp = getData().get(i).msMsg.timestamp;
                break;
            }
        }
        return timestamp;
    }


    public IConversationContext getConversationContext() {
        return iConversationContext;
    }

    //显示多选
    public void showMultipleChoice() {
        iConversationContext.showMultipleChoice();
    }

    public void hideSoftKeyboard() {
        iConversationContext.hideSoftKeyboard();
    }

    //回复某条消息
    public void replyMsg(MSMsg msMsg) {
        iConversationContext.showReply(msMsg);
    }

    public void showTitleRightText(String content) {
        iConversationContext.setTitleRightText(content);
    }

    //提示某条消息
    public void showTipsMsg(String clientMsgNo) {
        iConversationContext.tipsMsg(clientMsgNo);
    }

    //设置输入框内容
    public void setEditContent(String content) {
        iConversationContext.setEditContent(content);
    }

    //是否存在某条消息
    public boolean isExist(String clientMsgNo, String messageId) {
        if (TextUtils.isEmpty(clientMsgNo)) return false;
        boolean isExist = false;
        for (int i = 0, size = getData().size(); i < size; i++) {
            if (getData().get(i).msMsg == null) {
                continue;
            }
            if (!TextUtils.isEmpty(messageId) && !TextUtils.isEmpty(getData().get(i).msMsg.messageID) && getData().get(i).msMsg.messageID.equals(messageId)) {
                isExist = true;
                break;
            }

            if (!TextUtils.isEmpty(getData().get(i).msMsg.clientMsgNO) && getData().get(i).msMsg.clientMsgNO.equals(clientMsgNo)) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    //获取最后一条消息
    public MSMsg getLastMsg() {
        MSMsg msMsg = null;
        for (int i = getData().size() - 1; i >= 0; i--) {
            if (getData().get(i).msMsg != null
                    && getData().get(i).msMsg.type != MSContentType.msgPromptNewMsg
                    && getData().get(i).msMsg.type != MSContentType.typing) {
                msMsg = getData().get(i).msMsg;
                break;
            }
        }
        return msMsg;
    }

    //获取最后一条消息是否为正在输入
    public boolean lastMsgIsTyping() {
        boolean isTyping = false;
        for (int i = getData().size() - 1; i >= 0; i--) {
            if (getData().get(i).msMsg != null && getData().get(i).msMsg.type == MSContentType.typing) {
                isTyping = true;
                break;
            }
        }

        return isTyping;
    }

    public long getEndMsgOrderSeq() {
        long oldestOrderSeq = 0;
        for (int i = getData().size() - 1; i >= 0; i--) {
            if (getData().get(i).msMsg != null && getData().get(i).msMsg.orderSeq != 0) {
                oldestOrderSeq = getData().get(i).msMsg.orderSeq;
                break;
            }
        }
        return oldestOrderSeq;
    }

    public long getFirstMsgOrderSeq() {
        long oldestOrderSeq = 0;
        for (int i = 0, size = getData().size(); i < size; i++) {
            if (getData().get(i).msMsg != null && getData().get(i).msMsg.orderSeq != 0) {
                oldestOrderSeq = getData().get(i).msMsg.orderSeq;
                break;
            }
        }
        return oldestOrderSeq;
    }

    public void resetData(List<MSUIChatMsgItemEntity> list) {
        if (MSReader.isEmpty(list)) return;
        for (int i = 0, size = list.size(); i < size; i++) {
            int previousIndex = i - 1;
            int nextIndex = i + 1;
            if (previousIndex >= 0) {
                list.get(i).previousMsg = list.get(previousIndex).msMsg;
            }
            if (nextIndex <= list.size() - 1) {
                list.get(i).nextMsg = list.get(nextIndex).msMsg;
            }
        }
    }

    public int getFirstVisibleItemIndex(int startIndex) {
        int index = startIndex;
        if (startIndex <= getData().size() - 1) {
            if (getData().get(startIndex).msMsg == null || getData().get(startIndex).msMsg.orderSeq == 0) {
                for (int i = startIndex; i < getData().size(); i++) {
                    if (getData().get(i).msMsg != null && getData().get(i).msMsg.orderSeq != 0) {
                        index = i;
                        break;
                    }
                }
            }
        }
        return index;
    }

    public MSMsg getFirstVisibleItem(int startIndex) {
        MSMsg msMsg = null;
        if (startIndex <= getData().size() - 1) {
            if (getData().get(startIndex).msMsg == null || getData().get(startIndex).msMsg.orderSeq == 0) {
                for (int i = startIndex; i < getData().size(); i++) {
                    if (getData().get(i).msMsg != null && getData().get(i).msMsg.orderSeq != 0) {
                        msMsg = getData().get(i).msMsg;
                        break;
                    }
                }
            } else {
                msMsg = getData().get(startIndex).msMsg;
            }
        }
        return msMsg;
    }

    public boolean isShowChooseItem() {
        boolean isShowChoose = false;
        for (int i = 0, size = getData().size(); i < size; i++) {
            if (getData().get(i).isChoose) {
                isShowChoose = true;
                break;
            }
        }
        return isShowChoose;
    }

    public boolean isCanSwipe(int index) {
        if (index < 0 || index >= getData().size()) {
            return false;
        }
        int type = getData().get(index).msMsg.type;
        if (type <= 0 || getData().get(index).msMsg.flame == 1 || (getData().get(index).msMsg.remoteExtra != null && getData().get(index).msMsg.remoteExtra.revoke == 1)) {
            return false;
        }
        MSChannel channel = iConversationContext.getChatChannelInfo();
        ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> list = getItemProviderList();
        MSChatBaseProvider baseItemProvider = (MSChatBaseProvider) list.get(type);
        if (baseItemProvider != null && channel.status == 1)
            return baseItemProvider.getMsgConfig(type).isCanReply;
        return false;
    }

    public void updateDeleteTimer(int position) {
        MSUIChatMsgItemEntity entity = getData().get(position);
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getRecyclerView().getLayoutManager();
        if (linearLayoutManager == null) return;
        View view = linearLayoutManager.findViewByPosition(position);
        LinearLayout baseView = null;
        if (view != null) {
            baseView = view.findViewById(R.id.msBaseContentLayout);
        }
        if (baseView == null) return;
        ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> list = getItemProviderList();
        MSChatBaseProvider baseItemProvider = (MSChatBaseProvider) list.get(entity.msMsg.type);
        if (baseItemProvider != null) {
            SecretDeleteTimer deleteTimer = null;
            MSChatIteMsgFromType from = baseItemProvider.getMsgFromType(entity.msMsg);
            if (baseView.getChildCount() > 1) {
                if (from == MSChatIteMsgFromType.SEND) {
                    View childView = baseView.getChildAt(0);
                    if (childView instanceof SecretDeleteTimer) {
                        deleteTimer = (SecretDeleteTimer) childView;
                    }
                } else if (from == MSChatIteMsgFromType.RECEIVED) {
                    View childView = baseView.getChildAt(1);
                    if (childView instanceof SecretDeleteTimer) {
                        deleteTimer = (SecretDeleteTimer) childView;
                    }
                }
            }

            if (deleteTimer != null) {
                deleteTimer.setVisibility(View.VISIBLE);
                deleteTimer.setDestroyTime(entity.msMsg.clientMsgNO, entity.msMsg.flameSecond, entity.msMsg.viewedAt, false);
            }
        }
    }


    public enum RefreshType {
        status, background, data, reaction, reply, listener
    }

    public void notifyStatus(int position) {
        notify(position, RefreshType.status, null);
    }

    public void notifyData(int position) {
        notify(position, RefreshType.data, null);
    }

    public void notifyListener(int position) {
        notify(position, RefreshType.listener, null);
    }

    public void notifyBackground(int position) {
        notify(position, RefreshType.background, null);
    }

    public void notifyReaction(int position, List<MSMsgReaction> reactionList) {
        notify(position, RefreshType.reaction, reactionList);
    }

    private void notify(int position, RefreshType refreshType, List<MSMsgReaction> reactionList) {
        MSUIChatMsgItemEntity entity = getData().get(position);
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getRecyclerView().getLayoutManager();
        if (linearLayoutManager == null) return;
        View view = linearLayoutManager.findViewByPosition(position);
        View baseView = null;
        if (view != null) {
            baseView = view.findViewById(R.id.msBaseContentLayout);
        }
        if (baseView == null) return;
        ConcurrentHashMap<Integer, BaseItemProvider<MSUIChatMsgItemEntity>> list = getItemProviderList();
        MSChatBaseProvider baseItemProvider = (MSChatBaseProvider) list.get(entity.msMsg.type);
        if (baseItemProvider != null) {
            MSChatIteMsgFromType from = baseItemProvider.getMsgFromType(entity.msMsg);
            // 刷新
            if (refreshType == RefreshType.data) {
                baseItemProvider.refreshData(position, baseView, entity, from);
                return;
            }
            if (refreshType == RefreshType.reaction) {
                FrameLayout reactionsView = view.findViewById(R.id.reactionsView);
                EndpointManager.getInstance().invoke(
                        "refresh_msg_reaction", new ShowMsgReactionMenu(
                                reactionsView,
                                from,
                                this,
                                reactionList)
                );
                AvatarView avatarView = view.findViewById(R.id.avatarView);
                if (avatarView != null) {
                    baseItemProvider.setAvatarLayoutParams(entity, from, avatarView);
                }
                return;
            }
            if (refreshType == RefreshType.background) {
                AvatarView avatarView = view.findViewById(R.id.avatarView);
                if (avatarView != null) {
                    baseItemProvider.setAvatarLayoutParams(entity, from, avatarView);
                }
                baseItemProvider.resetCellBackground(baseView, entity, from);
                LinearLayout fullContentLayout = view.findViewById(R.id.fullContentLayout);
                if (fullContentLayout != null) {
                    baseItemProvider.setFullLayoutParams(entity, from, fullContentLayout);
                }
                ChatItemView viewGroupLayout = view.findViewById(R.id.viewGroupLayout);
                if (viewGroupLayout != null) {
                    baseItemProvider.setItemPadding(position, viewGroupLayout);
                }
                return;
            }

            if (refreshType == RefreshType.status) {
                baseItemProvider.resetCellListener(position, baseView, entity, from);
                baseItemProvider.setMsgTimeAndStatus(
                        entity,
                        baseView,
                        from
                );
                return;
            }
            if (refreshType == RefreshType.listener) {
                baseItemProvider.resetCellListener(position, baseView, entity, from);
                return;
            }

            if (refreshType == RefreshType.reply) {
                baseItemProvider.refreshReply(position, baseView, entity, from);
            }
        }

    }

    public void refreshReplyMsg(MSMsg msMsg) {
        if (msMsg == null || msMsg.remoteExtra == null || TextUtils.isEmpty(msMsg.remoteExtra.messageID))
            return;
        List<MSUIChatMsgItemEntity> list = getData();
        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i).msMsg.baseContentMsgModel == null || list.get(i).msMsg.baseContentMsgModel.reply == null) {
                continue;
            }
            if (list.get(i).msMsg.baseContentMsgModel.reply.message_seq == msMsg.messageSeq) {
                list.get(i).msMsg.baseContentMsgModel.reply.contentEditMsgModel = msMsg.remoteExtra.contentEditMsgModel;
                list.get(i).msMsg.baseContentMsgModel.reply.contentEdit = msMsg.remoteExtra.contentEdit;
                list.get(i).msMsg.baseContentMsgModel.reply.editAt = msMsg.remoteExtra.editedAt;
                list.get(i).msMsg.baseContentMsgModel.reply.revoke = msMsg.remoteExtra.revoke;
                notify(i, RefreshType.reply, null);
            }
        }

    }
}
