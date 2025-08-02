package com.chat.uikit.chat.adapter;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSSystemAccount;
import com.chat.base.emoji.MoonUtil;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.AvatarOtherViewMenu;
import com.chat.base.endpoint.entity.ShowCommunityAvatarMenu;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.entity.MSChannelState;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.msgitem.MSMsgItemViewManager;
import com.chat.base.msgitem.MSRevokeProvider;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.ui.components.CounterView;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.StringUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.uikit.R;
import com.chat.uikit.enity.ChatConversationMsg;
import com.chat.uikit.message.MsgModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelExtras;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSMentionType;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.entity.MSUIConversationMsg;
import com.mushanyux.mushanim.message.type.MSSendMsgResult;

import org.jetbrains.annotations.NotNull;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 会话记录适配器
 */
public class ChatConversationAdapter extends BaseQuickAdapter<ChatConversationMsg, BaseViewHolder> {
    private IListener iListener;

    public ChatConversationAdapter(@Nullable List<ChatConversationMsg> data) {
        super(R.layout.item_chat_conv_layout, data);
    }

    @Override
    protected void convert(@NonNull final BaseViewHolder helper, ChatConversationMsg conversationMsg) {
        MSUIConversationMsg item = conversationMsg.uiConversationMsg;
        setUnreadCount(helper, conversationMsg, false);
        showTime(helper, item);
        showChannel(helper, item);
        showContent(helper, item);
        showReminders(helper, conversationMsg);
        setStatus(helper, item, false);
        showTyping(helper, conversationMsg);
        showCalling(helper, conversationMsg);
    }

    public void addListener(IListener iItemMenuClick) {
        this.iListener = iItemMenuClick;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, ChatConversationMsg uiConversationMsg, @NotNull List<?> payloads) {
        ChatConversationMsg chatConversationMsg = (ChatConversationMsg) payloads.get(0);
        if (chatConversationMsg != null && chatConversationMsg.uiConversationMsg != null) {
            MSUIConversationMsg item = chatConversationMsg.uiConversationMsg;
//            showContent(baseViewHolder, item);
            if (chatConversationMsg.isResetCounter) {
                setUnreadCount(baseViewHolder, chatConversationMsg, true);
                chatConversationMsg.isResetCounter = false;
            }
            if (chatConversationMsg.isResetTime) {
                showTime(baseViewHolder, item);
                chatConversationMsg.isResetTime = false;
            }
            if (chatConversationMsg.isResetTyping) {
                showTyping(baseViewHolder, chatConversationMsg);
                chatConversationMsg.isResetTyping = false;
            }
            if (chatConversationMsg.isRefreshChannelInfo) {
                showChannel(baseViewHolder, item);
                chatConversationMsg.isRefreshChannelInfo = false;
            }
            if (chatConversationMsg.isResetReminders) {
                showReminders(baseViewHolder, chatConversationMsg);
                chatConversationMsg.isResetReminders = false;
            }
            if (chatConversationMsg.isRefreshStatus) {
                setStatus(baseViewHolder, item, true);
                chatConversationMsg.isRefreshStatus = false;
            }
            if (chatConversationMsg.isResetContent) {
                showContent(baseViewHolder, item);
                chatConversationMsg.isResetContent = false;
            }
            showCalling(baseViewHolder, chatConversationMsg);
        }
    }

    public interface IListener {
        void onClick(ItemMenu menu, MSUIConversationMsg item);
    }


    private String getFromName(byte channelType, MSMsg msg) {
        String fromName = "";
        if (msg != null && (MSContentType.isSystemMsg(msg.type)
                || msg.type == MSContentType.revoke
                || msg.remoteExtra.revoke == 1 || msg.type == MSContentType.screenshot)) {
            return fromName;
        }
        if (channelType == MSChannelType.PERSONAL || channelType == MSChannelType.CUSTOMER_SERVICE || msg == null || TextUtils.isEmpty(msg.fromUID) || msg.fromUID.equals(MSConfig.getInstance().getUid())) {
            return fromName;
        }
        String channelName = "";
        String channelRemark = "";
        String memberRemark = "";
        String memberName = "";
        if (msg.getFrom() != null) {
            channelRemark = msg.getFrom().channelRemark;
            channelName = msg.getFrom().channelName;
        }
        if (!TextUtils.isEmpty(channelRemark)) {
            return channelRemark;
        }
        if (msg.getMemberOfFrom() != null) {
            memberName = msg.getMemberOfFrom().memberName;
            memberRemark = msg.getMemberOfFrom().memberRemark;
        }
        if (!TextUtils.isEmpty(memberRemark)) {
            return memberRemark;
        }
        fromName = TextUtils.isEmpty(channelName) ? memberName : channelName;
        return fromName;
    }

    private String getContent(MSMsg msg) {
        String content = "";
        if (msg == null || msg.isDeleted == 1) return content;
        if (msg.baseContentMsgModel != null) {
            content = msg.baseContentMsgModel.getDisplayContent();
        }

        if (TextUtils.isEmpty(content) || MSContentType.isSystemMsg(msg.type)) {
            content = getShowContent(msg.content);
        }
        if (msg.remoteExtra.contentEditMsgModel != null) {
            content = msg.remoteExtra.contentEditMsgModel.getDisplayContent();
        }
        //判断是否被撤回
        if (msg.remoteExtra.revoke == 1)
            content = MSRevokeProvider.Companion.showRevokeMsg(msg);
        else if (msg.type == MSContentType.MS_CONTENT_FORMAT_ERROR) {
            content = getContext().getString(R.string.str_content_format_err);
        } else if (msg.type == MSContentType.MS_SIGNAL_DECRYPT_ERROR) {
            content = getContext().getString(R.string.str_signal_decrypt_err);
        } else if (msg.type == MSContentType.noRelation) {
            String showName = "";
            if (msg.getChannelInfo() != null) {
                if (TextUtils.isEmpty(msg.getChannelInfo().channelRemark)) {
                    showName = msg.getChannelInfo().channelName;
                } else {
                    showName = msg.getChannelInfo().channelRemark;
                }
            }
            content = String.format(getContext().getString(R.string.no_relation_request), showName);
        } else {
            if (!MSMsgItemViewManager.getInstance().getChatItemProviderList().containsKey(msg.type)) {
                if (TextUtils.isEmpty(content)) {
                    content = getContext().getString(R.string.unknow_msg_type);
                }
            }
        }
        return content;
    }

    private String getShowContent(String contentJson) {
        return StringUtils.getShowContent(getContext(), contentJson);
    }

    private void setStatus(BaseViewHolder helper, MSUIConversationMsg item, boolean isPlayAnimation) {
        RLottieImageView sendingMsgIv = helper.getView(R.id.statusIV);
        RLottieDrawable drawable;
        boolean autoRepeat = false;
        int status = MSSendMsgResult.send_success;
        if (item.getMsMsg() != null) {
            status = item.getMsMsg().status;
        }
        boolean isSend = item.getMsMsg() != null && item.getMsMsg().isDeleted == 0 && !TextUtils.isEmpty(item.getMsMsg().fromUID) && item.getMsMsg().fromUID.equals(MSConfig.getInstance().getUid());
        if (isSend) {
            boolean isSingle = true;
            sendingMsgIv.setVisibility(View.VISIBLE);
            boolean isError = false;
            if (status == MSSendMsgResult.send_success) {
                // 自己发送
                if (item.getMsMsg().setting.receipt == 1 && item.getMsMsg().remoteExtra.readedCount > 0) {
                    drawable = new RLottieDrawable(getContext(), R.raw.ticks_double, "ticks_double", AndroidUtilities.dp(22), AndroidUtilities.dp(22));
                    isSingle = false;
                } else {
                    drawable = new RLottieDrawable(getContext(), R.raw.ticks_single, "ticks_single", AndroidUtilities.dp(22), AndroidUtilities.dp(22));
                }
                sendingMsgIv.setColorFilter(new PorterDuffColorFilter(Theme.colorAccount, PorterDuff.Mode.MULTIPLY));
            } else if (status == MSSendMsgResult.send_loading) {
                autoRepeat = true;
                drawable = new RLottieDrawable(getContext(), R.raw.msg_sending, "msg_sending", AndroidUtilities.dp(22), AndroidUtilities.dp(22));
                sendingMsgIv.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.color999), PorterDuff.Mode.MULTIPLY));
            } else {
                isError = true;
                sendingMsgIv.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.white), PorterDuff.Mode.MULTIPLY));
                drawable = new RLottieDrawable(getContext(), R.raw.error, "error", AndroidUtilities.dp(22), AndroidUtilities.dp(22));
            }
            sendingMsgIv.setAutoRepeat(autoRepeat);

            if (autoRepeat || isPlayAnimation) {
                sendingMsgIv.setAnimation(drawable);
                sendingMsgIv.playAnimation();
            } else {
                if (isError) {
                    sendingMsgIv.setAnimation(drawable);
                } else {
                    if (isSingle) {
                        sendingMsgIv.setImageDrawable(Theme.getTicksSingleDrawable());
                    } else sendingMsgIv.setImageDrawable(Theme.getTicksDoubleDrawable());
                }
            }
        } else {
            sendingMsgIv.setVisibility(View.GONE);
        }
        int finalStatus = status;
        sendingMsgIv.setOnClickListener(view -> {
            if (finalStatus != MSSendMsgResult.send_success && finalStatus != MSSendMsgResult.send_loading && item.getMsMsg() != null) {
                String content = getContext().getString(R.string.str_resend_msg_tips);
                if (finalStatus == MSSendMsgResult.no_relation) {
                    content = getContext().getString(R.string.no_relation_group);
                } else if (finalStatus == MSSendMsgResult.black_list) {
                    content =
                            getContext().getString(item.channelType == MSChannelType.GROUP ? R.string.blacklist_group : R.string.blacklist_user);

                } else if (finalStatus == MSSendMsgResult.not_on_white_list) {
                    content = getContext().getString(R.string.no_relation_user);
                }
                MSDialogUtils.getInstance().showDialog(getContext(), getContext().getString(R.string.msg_send_fail), content, true, "", getContext().getString(R.string.msg_send_fail_resend), 0, Theme.colorAccount, index -> {
                    if (index == 1) {
                        MSMsg msg = new MSMsg();
                        msg.channelID = item.channelID;
                        msg.channelType = item.channelType;
                        msg.setting = item.getMsMsg().setting;
                        msg.header = item.getMsMsg().header;
                        msg.type = item.getMsMsg().type;
                        msg.content = item.getMsMsg().content;
                        msg.baseContentMsgModel = item.getMsMsg().baseContentMsgModel;
                        msg.fromUID = MSConfig.getInstance().getUid();
                        MSIM.getInstance().getMsgManager()
                                .deleteWithClientMsgNO(item.getMsMsg().clientMsgNO);
                        MSIM.getInstance().getMsgManager().sendMessage(msg);
                    }
                });
            }
        });
    }

    private void setUnreadCount(@NotNull BaseViewHolder baseViewHolder, ChatConversationMsg item, boolean isAnimated) {
        CounterView counterView = baseViewHolder.getView(R.id.msgCountTv);
        boolean isMute;
        if (item.uiConversationMsg.getMsChannel() != null) {
            isMute = item.uiConversationMsg.getMsChannel().mute == 1;
        } else isMute = false;
        counterView.setColors(R.color.white, isMute ? R.color.color999 : R.color.reminderColor);
        counterView.setCount(item.getUnReadCount(), isAnimated);
        counterView.setGravity(Gravity.END);
        counterView.setVisibility(item.getUnReadCount() > 0 ? View.VISIBLE : View.GONE);
    }

    private void showTime(@NotNull BaseViewHolder helper, MSUIConversationMsg item) {
        long msgTimestamp = item.lastMsgTimestamp;
        if (item.getMsMsg() != null) {
            if (item.getMsMsg().remoteExtra.editedAt != 0) {
                msgTimestamp = item.getMsMsg().remoteExtra.editedAt;
            }
        }
        String chatTime = MSTimeUtils.getInstance().getNewChatTime(msgTimestamp * 1000);
        helper.setText(R.id.timeTv, chatTime);
    }

    private void showContent(@NotNull BaseViewHolder helper, MSUIConversationMsg item) {
        String content = getContent(item.getMsMsg());
        androidx.emoji2.widget.EmojiTextView contentTv = helper.getView(R.id.contentTv);
        boolean isSetChatPwd = isSetChatPwd(item.getMsChannel());
        // 聊天密码
        if (isSetChatPwd) {
            content = "❊❊❊❊❊❊❊❊❊❊❊❊❊";
        } else {
            String fromName = getFromName(item.channelType, item.getMsMsg());
            if (!TextUtils.isEmpty(fromName)) {
                content = fromName + "：" + content;
            }
        }
        //  contentTv.setText(content);
        MoonUtil.identifyFaceExpression(getContext(), contentTv, content, MoonUtil.SMALL_SCALE);
    }

    private void showReminders(@NotNull BaseViewHolder helper, ChatConversationMsg item) {
        TextView contentTv = helper.getView(R.id.contentTv);
        String draft = "";
        String approveContent = "";
        boolean mention = false;
        if (MSReader.isNotEmpty(item.getReminders())) {
            for (int i = 0, size = item.getReminders().size(); i < size; i++) {
                if (!mention && item.getReminders().get(i).type == MSMentionType.MSReminderTypeMentionMe && item.getReminders().get(i).done == 0) {
                    //存在@
                    mention = true;
                    // break;
                }
                if (item.getReminders().get(i).type == MSMentionType.MSApplyJoinGroupApprove && item.getReminders().get(i).done == 0) {
                    approveContent = getContext().getString(R.string.apply_join_group);
                }
            }
        }
        if (item.uiConversationMsg.getRemoteMsgExtra() != null) {
            draft = item.uiConversationMsg.getRemoteMsgExtra().draft;
        }
        boolean isSetChatPwd = isSetChatPwd(item.uiConversationMsg.getMsChannel());
        // 聊天密码
        if (isSetChatPwd) {
            if (!TextUtils.isEmpty(draft))
                draft = "❊❊❊❊❊❊❊❊❊❊❊❊❊";
        }
        LinearLayout remindLayout = helper.getView(R.id.remindLayout);
        remindLayout.removeAllViews();
        if (mention) {
            TextView textView = new TextView(getContext());
            textView.setTypeface(null, Typeface.BOLD);
            textView.setText(R.string.last_msg_remind);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.reminderColor));
            textView.setTextSize(13f);
            remindLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 5, 0));
        }
        if (!TextUtils.isEmpty(draft)) {
            TextView textView = new TextView(getContext());
            textView.setText(R.string.last_msg_draft);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.reminderColor));
            textView.setTextSize(13f);
            remindLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 5, 0));
            MoonUtil.identifyFaceExpression(getContext(), contentTv, draft, MoonUtil.SMALL_SCALE);
        } else {
            showContent(helper, item.uiConversationMsg);
        }
        if (!TextUtils.isEmpty(approveContent)) {
            TextView textView = new TextView(getContext());
            textView.setText(approveContent);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.reminderColor));
            textView.setTextSize(13f);
            remindLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 5, 0));
        }
    }

    private void showChannel(@NotNull BaseViewHolder helper, MSUIConversationMsg item) {
        addEvent(helper, item);
        String showName = "";
        if (item.channelID.equals(MSSystemAccount.system_file_helper)) {
            showName = getContext().getString(R.string.ms_file_helper);
        } else if (item.channelID.equals(MSSystemAccount.system_team)) {
            showName = getContext().getString(R.string.ms_system_notice);
        }
        helper.setGone(R.id.groupIV, item.channelType != MSChannelType.GROUP);
        boolean isTop;
        AvatarView avatarView = helper.getView(R.id.avatarView);
        avatarView.setSize(50);
        if (item.getMsChannel() != null) {
            if (item.channelType == MSChannelType.COMMUNITY) {
                EndpointManager.getInstance().invoke("show_community_avatar", new ShowCommunityAvatarMenu(getContext(), avatarView, item.getMsChannel()));
            } else {
                avatarView.defaultAvatarTv.setVisibility(View.GONE);
                avatarView.imageView.setVisibility(View.VISIBLE);
                avatarView.showAvatar(item.getMsChannel(), true);
            }
            EndpointManager.getInstance().invoke("show_avatar_other_info", new AvatarOtherViewMenu(helper.getView(R.id.otherLayout), item.getMsChannel(), avatarView, false));
            isTop = item.getMsChannel().top == 1;
            if (TextUtils.isEmpty(showName))
                showName = TextUtils.isEmpty(item.getMsChannel().channelRemark) ? item.getMsChannel().channelName : item.getMsChannel().channelRemark;
            if (TextUtils.isEmpty(showName)) {
                showName = getContext().getString(R.string.chat);
//                if (!isScrolling)
                MSIM.getInstance().getChannelManager().fetchChannelInfo(item.channelID, item.channelType);
            }
            LinearLayout categoryLayout = helper.getView(R.id.categoryLayout);
            categoryLayout.removeAllViews();
            ImageView forbiddenIv = helper.getView(R.id.forbiddenIv);
            forbiddenIv.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.color999), PorterDuff.Mode.MULTIPLY));
            //设置是否置顶
            helper.setBackgroundResource(R.id.contentLayout, isTop ? R.drawable.home_bg : R.drawable.layout_bg);
            if (item.getMsChannel().mute == 1) {
                ImageView muteIV = new ImageView(getContext());
                muteIV.setImageResource(R.mipmap.list_mute);
                Theme.setColorFilter(muteIV, ContextCompat.getColor(getContext(), R.color.popupTextColor));
                categoryLayout.addView(muteIV, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 3, 1, 0, 0));
            }
            if (!TextUtils.isEmpty(item.getMsChannel().category)) {

                if (item.getMsChannel().category.equals(MSSystemAccount.accountCategorySystem)) {
                    categoryLayout.addView(Theme.getChannelCategoryTV(getContext(), getContext().getString(R.string.official), ContextCompat.getColor(getContext(), R.color.transparent), ContextCompat.getColor(getContext(), R.color.reminderColor), ContextCompat.getColor(getContext(), R.color.reminderColor)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
                if (item.getMsChannel().category.equals(MSSystemAccount.accountCategoryCustomerService)) {
                    categoryLayout.addView(Theme.getChannelCategoryTV(getContext(), getContext().getString(R.string.customer_service), Theme.colorAccount, ContextCompat.getColor(getContext(), R.color.white), Theme.colorAccount), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
                if (item.getMsChannel().category.equals(MSSystemAccount.accountCategoryVisitor)) {
                    categoryLayout.addView(Theme.getChannelCategoryTV(getContext(), getContext().getString(R.string.visitor), ContextCompat.getColor(getContext(), R.color.transparent), ContextCompat.getColor(getContext(), R.color.colorFFC107), ContextCompat.getColor(getContext(), R.color.colorFFC107)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
                if (item.getMsChannel().category.equals(MSSystemAccount.channelCategoryOrganization)) {
                    categoryLayout.addView(Theme.getChannelCategoryTV(getContext(), getContext().getString(R.string.all_staff), ContextCompat.getColor(getContext(), R.color.category_org_bg), ContextCompat.getColor(getContext(), R.color.category_org_text), ContextCompat.getColor(getContext(), R.color.transparent)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
                if (item.getMsChannel().category.equals(MSSystemAccount.channelCategoryDepartment)) {
                    categoryLayout.addView(Theme.getChannelCategoryTV(getContext(), getContext().getString(R.string.department), ContextCompat.getColor(getContext(), R.color.category_org_bg), ContextCompat.getColor(getContext(), R.color.category_org_text), ContextCompat.getColor(getContext(), R.color.transparent)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
            }
            if (item.channelType == MSChannelType.COMMUNITY) {
                categoryLayout.addView(Theme.getChannelCategoryTV(getContext(), getContext().getString(R.string.community), ContextCompat.getColor(getContext(), R.color.category_community_bg), ContextCompat.getColor(getContext(), R.color.category_community_text), ContextCompat.getColor(getContext(), R.color.transparent)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
            }
            if (item.getMsChannel().robot == 1)
                categoryLayout.addView(Theme.getChannelCategoryTV(getContext(), getContext().getString(R.string.bot), ContextCompat.getColor(getContext(), R.color.colorFFC107), ContextCompat.getColor(getContext(), R.color.white), ContextCompat.getColor(getContext(), R.color.colorFFC107)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
            //判断是否禁言
            if (item.getMsChannel().forbidden == 1) {
                MSChannelMember mChannelMember = MSIM.getInstance().getChannelMembersManager().getMember(item.channelID, item.channelType, MSConfig.getInstance().getUid());
                if (mChannelMember != null && mChannelMember.role == 0) {
                    helper.setGone(R.id.forbiddenIv, false);
                } else helper.setGone(R.id.forbiddenIv, true);
            } else {
                helper.setGone(R.id.forbiddenIv, true);
            }
        } else {
            if (TextUtils.isEmpty(showName))
                showName = getContext().getString(R.string.chat);
            avatarView.defaultAvatarTv.setVisibility(View.GONE);
            avatarView.imageView.setVisibility(View.VISIBLE);
            avatarView.imageView.setImageResource(R.drawable.default_view_bg);
            MSIM.getInstance().getChannelManager().fetchChannelInfo(item.channelID, item.channelType);
        }
        helper.setText(R.id.nameTv, showName);
    }

    private boolean isSetChatPwd(MSChannel channel) {
        if (channel == null || channel.remoteExtraMap == null || !channel.remoteExtraMap.containsKey(MSChannelExtras.chatPwdOn))
            return false;
        boolean isSetChatPwd;
        Object object = channel.remoteExtraMap.get(MSChannelExtras.chatPwdOn);
        if (object != null) {
            isSetChatPwd = (int) object == 1;
        } else {
            isSetChatPwd = false;
        }
        return isSetChatPwd;
    }

    private void showTyping(@NotNull BaseViewHolder helper, ChatConversationMsg item) {
        helper.setGone(R.id.spinKit, item.typingStartTime <= 0);
        if (item.typingStartTime > 0) {
            String content;
            if (item.uiConversationMsg.channelType == MSChannelType.GROUP) {
                String name = item.typingUserName;
                content = String.format(getContext().getString(R.string.user_is_typing), name);
            } else {
                content = getContext().getString(R.string.other_is_typing);
            }
            helper.setText(R.id.contentTv, content);
        }
    }

    private void addEvent(@NotNull BaseViewHolder helper, MSUIConversationMsg item) {
        //长按事件
        boolean top;
        boolean mute;
        if (item.getMsChannel() != null) {
            top = item.getMsChannel().top == 1;
            mute = item.getMsChannel().mute == 1;
        } else {
            top = false;
            mute = false;
        }
        List<PopupMenuItem> list = new ArrayList<>();
        if (item.getMsChannel() != null) {
            list.add(new PopupMenuItem(getContext().getString(mute ? R.string.open_channel_notice : R.string.close_channel_notice), mute ? R.mipmap.msg_unmute : R.mipmap.msg_mute, () -> iListener.onClick(ItemMenu.mute, item)));
        }
        list.add(new PopupMenuItem(top ? getContext().getString(R.string.cancel_top) : getContext().getString(R.string.msg_top), top ? R.mipmap.msg_unpin : R.mipmap.msg_pin, () -> iListener.onClick(ItemMenu.top, item)));
        list.add(new PopupMenuItem(getContext().getString(R.string.delete_msg), R.mipmap.msg_delete, () -> iListener.onClick(ItemMenu.delete, item)));
        MSDialogUtils.getInstance().setViewLongClickPopup(helper.getView(R.id.contentLayout), list);
    }

    private void showCalling(final BaseViewHolder helper, ChatConversationMsg conversationMsg) {
        helper.setGone(R.id.callingIv, conversationMsg.isCalling == 0);
    }

    public enum ItemMenu {
        delete, top, mute
    }
}
