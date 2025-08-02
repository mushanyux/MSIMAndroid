package com.chat.uikit.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.base.base.MSBaseFragment;
import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointHandler;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.base.utils.MSToastUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.TabActivity;
import com.chat.uikit.MSUIKitApplication;
import com.chat.uikit.chat.adapter.ChatConversationAdapter;
import com.chat.uikit.chat.manager.MSIMUtils;
import com.chat.uikit.contacts.service.FriendModel;
import com.chat.uikit.databinding.FragChatConversationLayoutBinding;
import com.chat.uikit.enity.ChatConversationMsg;
import com.chat.uikit.group.service.GroupModel;
import com.chat.uikit.message.MsgModel;
import com.chat.uikit.search.SearchAllActivity;
import com.chat.uikit.search.remote.GlobalActivity;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSCMDKeys;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelState;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSReminder;
import com.mushanyux.mushanim.entity.MSUIConversationMsg;
import com.mushanyux.mushanim.interfaces.IAllConversations;
import com.mushanyux.mushanim.message.type.MSConnectReason;
import com.mushanyux.mushanim.message.type.MSConnectStatus;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * 会话
 */
public class ChatFragment extends MSBaseFragment<FragChatConversationLayoutBinding> {

    private ChatConversationAdapter chatConversationAdapter;
    private Disposable disposable;
    private final List<Integer> refreshIds = new ArrayList<>();
    private Timer connectTimer;
    private TabActivity tabActivity;

    @Override
    protected boolean isShowBackLayout() {
        return false;
    }

    @Override
    protected FragChatConversationLayoutBinding getViewBinding() {
        return FragChatConversationLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        msVBinding.textSwitcher.setTag(-1);
        msVBinding.textSwitcher.setFactory(() -> {
            TextView textView = new TextView(getActivity());
            textView.setTextSize(22);
            Typeface face = Typeface.createFromAsset(getResources().getAssets(),
                    "fonts/mw_bold.ttf");
            textView.setTypeface(face);
            textView.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorDark));
            return textView;
        });
        msVBinding.textSwitcher.setText(getString(R.string.app_name));
        //去除刷新条目闪动动画
        ((DefaultItemAnimator) Objects.requireNonNull(msVBinding.recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        chatConversationAdapter = new ChatConversationAdapter(new ArrayList<>());
        initAdapter(msVBinding.recyclerView, chatConversationAdapter);
        chatConversationAdapter.setAnimationEnable(false);
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);

        Theme.setPressedBackground(msVBinding.deviceIv);
        Theme.setPressedBackground(msVBinding.searchIv);
        Theme.setPressedBackground(msVBinding.rightIv);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener() {
        msVBinding.rightIv.setOnClickListener(view -> {
            List<PopupMenuItem> list = EndpointManager.getInstance().invokes(EndpointCategory.tabMenus, null);
            MSDialogUtils.getInstance().showScreenPopup(view, list);
        });

        msVBinding.deviceIv.setOnClickListener(v -> EndpointManager.getInstance().invoke("show_pc_login_view", getActivity()));

        msVBinding.searchIv.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                @SuppressWarnings("unchecked") ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), new Pair<>(msVBinding.searchIv, "searchView"));
                startActivity(new Intent(getActivity(), GlobalActivity.class), activityOptions.toBundle());
            } else {
                startActivity(new Intent(getActivity(), GlobalActivity.class));
            }
        });
        chatConversationAdapter.addChildClickViewIds(R.id.contentLayout);
        chatConversationAdapter.setOnItemChildClickListener((adapter, view, position) -> SingleClickUtil.determineTriggerSingleClick(view, v -> {
            ChatConversationMsg uiConversationMsg = (ChatConversationMsg) adapter.getItem(position);
            if (uiConversationMsg != null && uiConversationMsg.uiConversationMsg != null) {
                if (view.getId() == R.id.contentLayout) {
                    if (uiConversationMsg.uiConversationMsg.channelType == MSChannelType.COMMUNITY) {
                        EndpointManager.getInstance().invoke("show_community", uiConversationMsg.uiConversationMsg.channelID);
                    } else
                        MSIMUtils.getInstance().startChatActivity(new ChatViewMenu(getActivity(), uiConversationMsg.uiConversationMsg.channelID, uiConversationMsg.uiConversationMsg.channelType, 0, false));
                }
            }
        }));
        chatConversationAdapter.addListener((menu, item) -> {
            if (menu == ChatConversationAdapter.ItemMenu.delete) {
                MSDialogUtils.getInstance().showDialog(getActivity(), getString(R.string.delete_chat), getString(R.string.delete_conver_msg_tips), true, "", getString(R.string.base_delete), 0, ContextCompat.getColor(requireActivity(), R.color.red), index -> {
                    if (index == 1) {
                        List<MSReminder> list = MSIM.getInstance().getReminderManager().getReminders(item.channelID, item.channelType);
                        if (MSReader.isNotEmpty(list)) {
                            List<Long> reminderIds = new ArrayList<>();
                            for (MSReminder reminder : list) {
                                if (reminder.done == 0) {
                                    reminder.done = 1;
                                    reminderIds.add(reminder.reminderID);
                                }
                            }
                            if (MSReader.isNotEmpty(reminderIds))
                                MsgModel.getInstance().doneReminder(reminderIds);
                        }
                        MsgModel.getInstance().offsetMsg(item.channelID, item.channelType, null);
                        MSIM.getInstance().getReminderManager().saveOrUpdateReminders(list);
                        MsgModel.getInstance().clearUnread(item.channelID, item.channelType, 0, null);
                        boolean result = MSIM.getInstance().getConversationManager().deleteWitchChannel(item.channelID, item.channelType);
                        if (result) {
                            if (item.getMsChannel() != null && item.getMsChannel().top == 1) {
                                updateTop(item.channelID, item.channelType, 0);
                            }
                            MSIM.getInstance().getMsgManager().clearWithChannel(item.channelID, item.channelType);
                        }
                    }
                });
            } else if (menu == ChatConversationAdapter.ItemMenu.top) {
                boolean top = false;
                if (item.getMsChannel() != null) {
                    top = item.getMsChannel().top == 1;
                }
                updateTop(item.channelID, item.channelType, top ? 0 : 1);
            } else if (menu == ChatConversationAdapter.ItemMenu.mute) {
                boolean mute = false;
                if (item.getMsChannel() != null) {
                    mute = item.getMsChannel().mute == 1;
                }
                //免打扰
                if (item.channelType == MSChannelType.GROUP) {
                    GroupModel.getInstance().updateGroupSetting(item.channelID, "mute", mute ? 0 : 1, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            MSToastUtils.getInstance().showToastNormal(msg);
                        }
                    });
                } else {
                    FriendModel.getInstance().updateUserSetting(item.channelID, "mute", mute ? 0 : 1, (code, msg) -> {
                        if (code != HttpResponseCode.success) {
                            MSToastUtils.getInstance().showToastNormal(msg);
                        }
                    });
                }
            }
        });
        //频道刷新监听
        MSIM.getInstance().getChannelManager().addOnRefreshChannelInfo("chat_fragment_refresh_channel", (channel, isEnd) -> {
            if (channel != null) {
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (!TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID) && !TextUtils.isEmpty(channel.channelID)
                            && chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(channel.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == channel.channelType) {

                        chatConversationAdapter.getData().get(i).uiConversationMsg.setMsChannel(channel);
                        // fixme 不能强制刷新整个列表，导致重新获取channel 频繁刷新UI卡顿
                        if (chatConversationAdapter.getData().get(i).isTop != channel.top) {
                            chatConversationAdapter.getData().get(i).isTop = channel.top;
                            sortMsg(chatConversationAdapter.getData());
                        } else {
                            chatConversationAdapter.getData().get(i).isRefreshChannelInfo = true;
                            chatConversationAdapter.getData().get(i).isResetCounter = true;
                            notifyRecycler(i, chatConversationAdapter.getData().get(i));
                        }
                        setAllCount();
                        break;
                    }
                }
            }
        });
        //监听移除最近会话
        MSIM.getInstance().getConversationManager().addOnDeleteMsgListener("chat_fragment", (s, b) -> {
            if (!TextUtils.isEmpty(s)) {
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(s) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == b) {
                        boolean isResetCount = chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount > 0;
                        chatConversationAdapter.removeAt(i);
                        if (isResetCount) setAllCount();
                        break;
                    }
                }
            }
        });

        MSIM.getInstance().getCMDManager().addCmdListener("chat_fragment_cmd", msCmd -> {
            if (msCmd == null || TextUtils.isEmpty(msCmd.cmdKey)) return;
            //监听正在输入
            switch (msCmd.cmdKey) {
                case MSCMDKeys.ms_typing -> {
                    String channelID = msCmd.paramJsonObject.optString("channel_id");
                    byte channelType = (byte) msCmd.paramJsonObject.optInt("channel_type");
                    String from_uid = msCmd.paramJsonObject.optString("from_uid");
                    String from_name = msCmd.paramJsonObject.optString("from_name");
                    MSChannel channel = new MSChannel(from_uid, MSChannelType.PERSONAL);
                    channel.channelName = from_name;
                    if (TextUtils.isEmpty(from_name)) {
                        MSChannel tempChannel = MSIM.getInstance().getChannelManager().getChannel(from_uid, MSChannelType.PERSONAL);
                        if (tempChannel != null) {
                            channel.channelName = tempChannel.channelName;
                            channel.channelRemark = tempChannel.channelRemark;
                        }
                    }
                    if (from_uid.equals(MSConfig.getInstance().getUid())) return;
                    for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == channelType) {
                            chatConversationAdapter.getData().get(i).isResetTyping = true;
                            chatConversationAdapter.getData().get(i).typingUserName = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
                            chatConversationAdapter.getData().get(i).typingStartTime = MSTimeUtils.getInstance().getCurrentSeconds();
                            notifyRecycler(i, chatConversationAdapter.getData().get(i));
                            if (disposable == null) {
                                startTimer();
                            }
                        }
                    }
                }
                case MSCMDKeys.ms_onlineStatus -> {
                    if (msCmd.paramJsonObject != null) {
                        int device_flag = msCmd.paramJsonObject.optInt("device_flag");
                        int online = msCmd.paramJsonObject.optInt("online");
                        String uid = msCmd.paramJsonObject.optString("uid");
                        if (uid.equals(MSConfig.getInstance().getUid()) && device_flag == 1) {
                            msVBinding.deviceIv.setVisibility(online == 1 ? View.VISIBLE : View.GONE);
                            MSSharedPreferencesUtil.getInstance().putInt(MSConfig.getInstance().getUid() + "_pc_online", online);
                        }
                    }
                }
                case "sync_channel_state" -> {
                    String fromUID = msCmd.paramJsonObject.optString("from_uid");
                    String channelId = msCmd.paramJsonObject.optString("channel_id");
                    int channelType = msCmd.paramJsonObject.optInt("channel_type");
                    if (channelId.equals(MSConfig.getInstance().getUid())) {
                        channelId = fromUID;
                    }
                    String finalChannelId = channelId;
                    MSCommonModel.getInstance().getChannelState(channelId, (byte) channelType, channelState -> {
                        if (channelState != null) {
                            int isCalling = 0;
                            if (MSReader.isNotEmpty(channelState.call_info.getCalling_participants())) {
                                isCalling = 1;
                            }
                            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                                if (chatConversationAdapter.getData().get(i).uiConversationMsg != null
                                        && !TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID)
                                        && finalChannelId.equals(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID)) {
                                    chatConversationAdapter.getData().get(i).isCalling = isCalling;
                                    chatConversationAdapter.notifyItemChanged(i);
                                    break;
                                }
                            }
                        }
                    });
                }
            }
        });
        // 监听刷新消息
        MSIM.getInstance().getMsgManager().addOnRefreshMsgListener("chat_fragment", (msg, left) -> {
            if (msg == null) return;
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                if (chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(msg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == msg.channelType
                        && chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg() != null &&
                        (chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().clientSeq == msg.clientSeq || chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().clientMsgNO.equals(msg.clientMsgNO))) {
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().status != msg.status
                            || chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.readedCount != msg.remoteExtra.readedCount) {
                        chatConversationAdapter.getData().get(i).isRefreshStatus = true;
                    }
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.revoke != msg.remoteExtra.revoke) {
                        chatConversationAdapter.getData().get(i).isResetContent = true;
                    }
                    chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().status = msg.status;
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.editedAt != msg.remoteExtra.editedAt) {
                        chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.editedAt = msg.remoteExtra.editedAt;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.contentEdit = msg.remoteExtra.contentEdit;
                        MSIMUtils.getInstance().resetMsgProhibitWord(chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg());
                    }
                    chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.revoker = msg.remoteExtra.revoker;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.revoke = msg.remoteExtra.revoke;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.unreadCount = msg.remoteExtra.unreadCount;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().remoteExtra.readedCount = msg.remoteExtra.readedCount;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().messageID = msg.messageID;
                    refreshIds.add(i);
                    break;
                }
            }
            if (left && MSReader.isNotEmpty(refreshIds)) {
                for (int i = 0, size = refreshIds.size(); i < size; i++) {
                    notifyRecycler(refreshIds.get(i), chatConversationAdapter.getData().get(refreshIds.get(i)));
                }
                refreshIds.clear();
            }
        });
        MSIM.getInstance().getMsgManager().addOnClearMsgListener("chat_fragment", (channelID, channelType, fromUID) -> {
            if (TextUtils.isEmpty(fromUID))
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == channelType) {
                        chatConversationAdapter.getData().get(i).uiConversationMsg.setMsMsg(null);
                        chatConversationAdapter.getData().get(i).isResetContent = true;
                        notifyRecycler(i, chatConversationAdapter.getData().get(i));
                        break;
                    }
                }
        });
        MSIM.getInstance().getReminderManager().addOnNewReminderListener("chat_fragment", list -> {
            if (MSReader.isEmpty(list) || MSReader.isEmpty(chatConversationAdapter.getData()))
                return;
            for (MSReminder reader : list) {
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (reader.done == 0
                            && !TextUtils.isEmpty(reader.messageID)
                            && !TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().messageID)
                            && chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg() != null
                            && reader.messageID.equals(chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().messageID)) {
                        chatConversationAdapter.getData().get(i).isResetReminders = true;
                        notifyRecycler(i, chatConversationAdapter.getData().get(i));
                        break;
                    }
                }
            }
        });
        // 监听刷新最近列表
        MSIM.getInstance().getConversationManager().addOnRefreshMsgListListener("chat_fragment", list -> {
            if (MSReader.isEmpty(list)) {
                return;
            }
            if (list.size() == 1) {
                resetData(list.get(0), true);
                return;
            }
            if (chatConversationAdapter.getData().isEmpty()) {
                List<ChatConversationMsg> uiList = new ArrayList<>();
                for (MSUIConversationMsg uiConversationMsg : list) {
                    ChatConversationMsg msg = new ChatConversationMsg(uiConversationMsg);
                    uiList.add(msg);
                }
                sortMsg(uiList);
                setAllCount();
                return;
            }
            List<ChatConversationMsg> uiList = new ArrayList<>();
            // 多条
            for (MSUIConversationMsg uiConversationMsg : list) {
                boolean isAdd = true;
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (!TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID) && !TextUtils.isEmpty(uiConversationMsg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(uiConversationMsg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == uiConversationMsg.channelType) {
                        isAdd = false;
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq != uiConversationMsg.lastMsgSeq || chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp != uiConversationMsg.lastMsgTimestamp || (chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg() != null && uiConversationMsg.getMsMsg() != null && !chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().clientMsgNO.equals(uiConversationMsg.getMsMsg().clientMsgNO))) {
                            chatConversationAdapter.getData().get(i).isResetTyping = true;
                            chatConversationAdapter.getData().get(i).typingUserName = "";
                            chatConversationAdapter.getData().get(i).typingStartTime = 0;
                            chatConversationAdapter.getData().get(i).isRefreshStatus = true;
                        }
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount != uiConversationMsg.unreadCount) {
                            chatConversationAdapter.getData().get(i).isResetCounter = true;
                        }
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp != uiConversationMsg.lastMsgTimestamp) {
                            chatConversationAdapter.getData().get(i).isResetTime = true;
                        }
                        chatConversationAdapter.getData().get(i).uiConversationMsg.setMsMsg(uiConversationMsg.getMsMsg());
                        if (!chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo.equals(uiConversationMsg.clientMsgNo)) {
                            chatConversationAdapter.getData().get(i).isResetContent = true;
                        }
                        MSIMUtils.getInstance().resetMsgProhibitWord(chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg());
                        chatConversationAdapter.getData().get(i).isResetReminders = true;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq = uiConversationMsg.lastMsgSeq;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo = uiConversationMsg.clientMsgNo;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount = uiConversationMsg.unreadCount;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp = uiConversationMsg.lastMsgTimestamp;
                        chatConversationAdapter.getData().get(i).uiConversationMsg.setRemoteMsgExtra(uiConversationMsg.getRemoteMsgExtra());

                        chatConversationAdapter.getData().get(i).uiConversationMsg.setReminderList(uiConversationMsg.getReminderList());
                        chatConversationAdapter.getData().get(i).uiConversationMsg.localExtraMap = null;
                        notifyRecycler(i, chatConversationAdapter.getData().get(i));
                        setAllCount();
                        break;
                    }
                }
                if (isAdd) {
                    uiList.add(new ChatConversationMsg(uiConversationMsg));
                }
            }
            if (!uiList.isEmpty()) {
                uiList.addAll(chatConversationAdapter.getData());
                sortMsg(uiList);
                setAllCount();
            }
        });
        // 监听连接状态
        MSIM.getInstance().getConnectionManager().addOnConnectionStatusListener("chat_fragment", (i, reason) -> {
            if (msVBinding.textSwitcher.getTag() != null) {
                Object tag = msVBinding.textSwitcher.getTag();
                if (tag instanceof Integer) {
                    int tag1 = (int) tag;
                    if (tag1 == i) {
                        return;
                    }
                }
            }
            if (i == MSConnectStatus.syncMsg) {
                msVBinding.textSwitcher.setText(getString(R.string.sync_msg));
            } else if (i == MSConnectStatus.success) {
                msVBinding.textSwitcher.setText(getString(R.string.app_name));
            } else if (i == MSConnectStatus.connecting) {
                msVBinding.textSwitcher.setText(getString(R.string.connecting));
            } else if (i == MSConnectStatus.noNetwork) {
                msVBinding.textSwitcher.setText(getString(R.string.network_error_tips));
            } else if (i == MSConnectStatus.kicked) {
                int from = 0;
                if (reason.equals(MSConnectReason.ReasonConnectKick)) {
                    from = 1;
                }
                MSUIKitApplication.getInstance().exitLogin(from);
            }
            msVBinding.textSwitcher.setTag(i);
            if (i != MSConnectStatus.success && i != MSConnectStatus.syncMsg) {
                startConnectTimer();
            } else {
                EndpointManager.getInstance().invoke("ms_close_disconnect_screen", null);
                stopConnectTimer();
            }
        });
        EndpointManager.getInstance().setMethod("", EndpointCategory.msExitChat, object -> {
            if (object != null) {
                MSChannel channel = (MSChannel) object;
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(channel.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == channel.channelType) {
                        boolean isResetCount = chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount > 0;
                        chatConversationAdapter.removeAt(i);
                        if (isResetCount) setAllCount();
                        break;
                    }
                }
            }
            return null;
        });

        EndpointManager.getInstance().setMethod("chat_cover", EndpointCategory.refreshProhibitWord, object -> {
            if (MSReader.isEmpty(chatConversationAdapter.getData())) {
                return 1;
            }
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                if (chatConversationAdapter.getData().get(i).uiConversationMsg != null && chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg() != null &&
                        chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().type == MSContentType.MS_TEXT) {
                    MSIMUtils.getInstance().resetMsgProhibitWord(chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg());
                    chatConversationAdapter.notifyItemChanged(i);
                }
            }
            return 1;
        });

        EndpointManager.getInstance().setMethod("refresh_conversation_calling", object -> {
            if (MSReader.isNotEmpty(MsgModel.getInstance().channelStatus)) {
                for (MSChannelState state : MsgModel.getInstance().channelStatus) {
                    for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                        if (chatConversationAdapter.getData().get(i).uiConversationMsg != null
                                && !TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID)
                                && state.channel_id.equals(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID)) {
                            chatConversationAdapter.getData().get(i).isCalling = state.calling;
                            chatConversationAdapter.notifyItemChanged(i);
                        }
                    }
                }
                return null;
            }
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                if (chatConversationAdapter.getData().get(i).isCalling == 1) {
                    chatConversationAdapter.getData().get(i).isCalling = 0;
                    chatConversationAdapter.notifyItemChanged(i);
                }
            }
            return null;
        });
    }


    @Override
    protected void initData() {
        getData();
    }

    private void getData() {
        getChatMsg();
    }


    private void getChatMsg() {
        MSIM.getInstance().getConversationManager().getAll(new IAllConversations() {
            @Override
            public void onResult(List<MSUIConversationMsg> list) {
                List<ChatConversationMsg> tempList = new ArrayList<>();
                if (MSReader.isNotEmpty(list)) {
                    for (int i = 0, size = list.size(); i < size; i++) {
                        tempList.add(new ChatConversationMsg(list.get(i)));
                    }
                }
                AndroidUtilities.runOnUIThread(() -> sortMsg(tempList));
            }
        });
    }

    private void setAllCount() {
        int allCount = 0;
        for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
            if (chatConversationAdapter.getData().get(i).uiConversationMsg.getMsChannel() != null && chatConversationAdapter.getData().get(i).uiConversationMsg.getMsChannel().mute == 0)
                allCount = allCount + chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount;
        }
        if (tabActivity != null) {
            tabActivity.setMsgCount(allCount);
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        tabActivity = (TabActivity) context;
    }

    private void resetChildData(MSUIConversationMsg uiConversationMsg, boolean isEnd) {
        if (MSReader.isNotEmpty(chatConversationAdapter.getData())) {
            boolean isAdd = true;
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                boolean isBreak = false;
                if (MSReader.isNotEmpty(chatConversationAdapter.getData().get(i).childList)) {
                    for (int j = 0, len = chatConversationAdapter.getData().get(i).childList.size(); j < len; j++) {
                        if (chatConversationAdapter.getData().get(i).childList.get(j).uiConversationMsg.channelID.equals(uiConversationMsg.channelID)) {
                            chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp = uiConversationMsg.lastMsgTimestamp;
                            chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq = uiConversationMsg.lastMsgSeq;
                            chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo = uiConversationMsg.clientMsgNo;
                            chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount += uiConversationMsg.unreadCount;
                            notifyRecycler(i, chatConversationAdapter.getData().get(i));
                            isBreak = true;
                            isAdd = false;
                        }
                    }
                }
                if (isBreak) break;
            }
            if (isAdd) {
                MSUIConversationMsg msg = new MSUIConversationMsg();
                msg.channelID = uiConversationMsg.parentChannelID;
                msg.channelType = uiConversationMsg.parentChannelType;
                msg.clientMsgNo = uiConversationMsg.clientMsgNo;
                msg.lastMsgSeq = uiConversationMsg.lastMsgSeq;
                msg.lastMsgTimestamp = uiConversationMsg.lastMsgTimestamp;
                msg.unreadCount = uiConversationMsg.unreadCount;
                msg.setReminderList(uiConversationMsg.getReminderList());
                msg.setRemoteMsgExtra(uiConversationMsg.getRemoteMsgExtra());

                ChatConversationMsg chatConversationMsg = new ChatConversationMsg(msg);
                ChatConversationMsg child = new ChatConversationMsg(uiConversationMsg);
                chatConversationMsg.childList = new ArrayList<>();
                chatConversationMsg.childList.add(child);
                if (!isEnd) {
                    chatConversationAdapter.addData(chatConversationMsg);
                } else {
                    int insertIndex = getInsertIndex(msg);
                    chatConversationAdapter.addData(insertIndex, chatConversationMsg);
                }
            }
        }
    }

    private int msgCount = 0;

    private void resetData(MSUIConversationMsg uiConversationMsg, boolean isEnd) {
        if (uiConversationMsg == null) {
            return;
        }
        if (uiConversationMsg.isDeleted == 1 || TextUtils.equals(uiConversationMsg.channelID, "0")) {
            if (isEnd) {
                sortMsg(chatConversationAdapter.getData());
            }
            return;
        }
        if (!TextUtils.isEmpty(uiConversationMsg.parentChannelID)) {
            resetChildData(uiConversationMsg, isEnd);
            return;
        }
        boolean isAdd = true;
        int index = -1;
        boolean isSort = false;
        if (MSReader.isNotEmpty(chatConversationAdapter.getData())) {
            for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                if (!TextUtils.isEmpty(chatConversationAdapter.getData().get(i).uiConversationMsg.channelID) && !TextUtils.isEmpty(uiConversationMsg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelID.equals(uiConversationMsg.channelID) && chatConversationAdapter.getData().get(i).uiConversationMsg.channelType == uiConversationMsg.channelType) {
                    if (!isEnd) {
                        isAdd = false;
                        chatConversationAdapter.getData().get(i).uiConversationMsg = uiConversationMsg;
                        break;
                    }
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq != uiConversationMsg.lastMsgSeq || chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp != uiConversationMsg.lastMsgTimestamp || (chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg() != null && uiConversationMsg.getMsMsg() != null && !chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg().clientMsgNO.equals(uiConversationMsg.getMsMsg().clientMsgNO))) {
                        isSort = true;
                        chatConversationAdapter.getData().get(i).isResetTyping = true;
                        chatConversationAdapter.getData().get(i).typingUserName = "";
                        chatConversationAdapter.getData().get(i).typingStartTime = 0;
                        chatConversationAdapter.getData().get(i).isRefreshStatus = true;
                        index = i;
                    }
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount != uiConversationMsg.unreadCount) {
                        chatConversationAdapter.getData().get(i).isResetCounter = true;
                    }
                    if (chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp != uiConversationMsg.lastMsgTimestamp) {
                        chatConversationAdapter.getData().get(i).isResetTime = true;
                    }
                    chatConversationAdapter.getData().get(i).uiConversationMsg.setMsMsg(uiConversationMsg.getMsMsg());
                    if (!chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo.equals(uiConversationMsg.clientMsgNo)) {
                        chatConversationAdapter.getData().get(i).isResetContent = true;
                    }
                    MSIMUtils.getInstance().resetMsgProhibitWord(chatConversationAdapter.getData().get(i).uiConversationMsg.getMsMsg());
                    chatConversationAdapter.getData().get(i).isResetReminders = true;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgSeq = uiConversationMsg.lastMsgSeq;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.clientMsgNo = uiConversationMsg.clientMsgNo;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.unreadCount = uiConversationMsg.unreadCount;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp = uiConversationMsg.lastMsgTimestamp;
                    chatConversationAdapter.getData().get(i).uiConversationMsg.setRemoteMsgExtra(uiConversationMsg.getRemoteMsgExtra());

                    chatConversationAdapter.getData().get(i).uiConversationMsg.setReminderList(uiConversationMsg.getReminderList());
                    chatConversationAdapter.getData().get(i).uiConversationMsg.localExtraMap = null;
                    isAdd = false;
                    notifyRecycler(i, chatConversationAdapter.getData().get(i));
                    setAllCount();
                    break;
                }
            }
        }
        if (!isEnd) msgCount++;

        if (isAdd) {
            if (!isEnd) {
                chatConversationAdapter.addData(new ChatConversationMsg(uiConversationMsg));
            } else {
                int insertIndex = getInsertIndex(uiConversationMsg);
                chatConversationAdapter.addData(insertIndex, new ChatConversationMsg(uiConversationMsg));
            }
            setAllCount();
        }
        if (isEnd) {
            if (isSort && msgCount == 0) {
                int insertIndex = getInsertIndex(uiConversationMsg);
                if (insertIndex != index) {
                    if (index != -1) chatConversationAdapter.removeAt(index);
                    chatConversationAdapter.addData(insertIndex, new ChatConversationMsg(uiConversationMsg));
                }
            } else {
                if (msgCount > 0) {
                    msgCount = 0;
                    sortMsg(chatConversationAdapter.getData());
                }
            }
        }
    }

    //排序消息
    private void sortMsg(List<ChatConversationMsg> list) {
        groupMsg(list);
        Collections.sort(list, (conversationMsg, t1) -> (int) (t1.uiConversationMsg.lastMsgTimestamp - conversationMsg.uiConversationMsg.lastMsgTimestamp));
        List<ChatConversationMsg> topList = new ArrayList<>();
        List<ChatConversationMsg> normalList = new ArrayList<>();
        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i).uiConversationMsg.getMsChannel() != null && list.get(i).uiConversationMsg.getMsChannel().top == 1) {
                topList.add(list.get(i));
            } else {
                normalList.add(list.get(i));
            }
        }
        List<ChatConversationMsg> tempList = new ArrayList<>();
        tempList.addAll(normalList);
        tempList.addAll(0, topList);
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                chatConversationAdapter.setList(tempList);
                setAllCount();
            }
        });
    }

    //检测正在输入的定时器
    private void startTimer() {
        Observable.interval(0, 1, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<>() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError(@NonNull Throwable e) {
            }

            @Override
            public void onSubscribe(@NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@NonNull Long value) {
                boolean isCancel = true;
                for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
                    if (chatConversationAdapter.getData().get(i).typingStartTime > 0) {
                        long typingStartTime = chatConversationAdapter.getData().get(i).typingStartTime;
                        if (MSTimeUtils.getInstance().getCurrentSeconds() - typingStartTime >= 8) {
                            chatConversationAdapter.getData().get(i).isResetTyping = true;
                            chatConversationAdapter.getData().get(i).typingStartTime = 0;
                            chatConversationAdapter.getData().get(i).typingUserName = "";
                            chatConversationAdapter.getData().get(i).isResetContent = true;
                            notifyRecycler(i, chatConversationAdapter.getData().get(i));
//                                    chatConversationAdapter.notifyItemChanged(i, chatConversationAdapter.getData().get(i));
                        }
                        isCancel = false;
                    }
                }
                if (disposable != null && isCancel) {
                    disposable.dispose();
                    disposable = null;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        MSIM.getInstance().getConversationManager().removeOnRefreshMsgListListener("chat_fragment");
        MSIM.getInstance().getConversationManager().removeOnRefreshMsgListener("chat_fragment");
        MSIM.getInstance().getConversationManager().removeOnDeleteMsgListener("chat_fragment");
        MSIM.getInstance().getCMDManager().removeCmdListener("chat_fragment_cmd");
        MSIM.getInstance().getMsgManager().removeRefreshMsgListener("chat_fragment");
        MSIM.getInstance().getConnectionManager().removeOnConnectionStatusListener("chat_fragment");
        MSIM.getInstance().getMsgManager().removeSendMsgAckListener("chat_fragment");
        MSIM.getInstance().getReminderManager().removeNewReminderListener("chat_fragment");
    }

    @Override
    public void onResume() {
        super.onResume();
        int pcOnline = MSSharedPreferencesUtil.getInstance().getInt(MSConfig.getInstance().getUid() + "_pc_online");
        msVBinding.deviceIv.setVisibility(pcOnline == 1 ? View.VISIBLE : View.GONE);
        EndpointManager.getInstance().setMethod("scroll_to_unread_channel", object -> {
            scrollToUnreadChannel();
            return null;
        });
    }

    private void startConnectTimer() {
        if (connectTimer == null) {
            connectTimer = new Timer();
        }
        connectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                EndpointManager.getInstance().invoke("show_disconnect_screen", getContext());
            }
        }, 1000);
    }

    private void stopConnectTimer() {
        if (connectTimer != null) {
            connectTimer.cancel();
            connectTimer = null;
        }
    }

    private int getTopChatCount() {
        int count = 0;
        for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
            if (chatConversationAdapter.getData().get(i).uiConversationMsg.getMsChannel() != null && chatConversationAdapter.getData().get(i).uiConversationMsg.getMsChannel().top == 1)
                count++;
        }
        return count;
    }

    private int getInsertIndex(MSUIConversationMsg msg) {
        if (msg.getMsChannel() != null && msg.getMsChannel().top == 1) return 0;
        return getTopChatCount();
    }

    private void notifyRecycler(int index, ChatConversationMsg msg) {
        if (msVBinding.recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE || (!msVBinding.recyclerView.isComputingLayout())) {
            chatConversationAdapter.notifyItemChanged(index, msg);
        }
    }

    private void updateTop(String channelID, byte channelType, int top) {
        if (channelType == MSChannelType.PERSONAL) {
            FriendModel.getInstance().updateUserSetting(channelID, "top", top, (code, msg) -> {
                if (code != HttpResponseCode.success) {
                    MSToastUtils.getInstance().showToastNormal(msg);
                }
            });
        } else {
            GroupModel.getInstance().updateGroupSetting(channelID, "top", top, (code, msg) -> {
                if (code != HttpResponseCode.success) {
                    MSToastUtils.getInstance().showToastNormal(msg);
                }
            });
        }

    }

    private void groupMsg(List<ChatConversationMsg> list) {
        // 将消息分组
        HashMap<String, List<ChatConversationMsg>> msgMap = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            if (!TextUtils.isEmpty(list.get(i).uiConversationMsg.parentChannelID)) {
                String key = list.get(i).uiConversationMsg.parentChannelID + "@" + list.get(i).uiConversationMsg.parentChannelType;
                List<ChatConversationMsg> tempList = null;
                if (msgMap.containsKey(key)) {
                    tempList = msgMap.get(key);
                }
                if (tempList == null) tempList = new ArrayList<>();
                tempList.add(list.get(i));
                msgMap.put(key, tempList);
                list.remove(i);
                i--;
            }
        }

        if (!msgMap.isEmpty()) {
            for (String key : msgMap.keySet()) {
                List<ChatConversationMsg> msgList = msgMap.get(key);
                MSUIConversationMsg lastMsg = new MSUIConversationMsg();
                ChatConversationMsg lastConvMsg = null;
                if (MSReader.isNotEmpty(msgList)) {
                    lastMsg.channelID = msgList.get(0).uiConversationMsg.parentChannelID;
                    lastMsg.channelType = msgList.get(0).uiConversationMsg.parentChannelType;
                    int unreadCount = 0;
                    List<MSReminder> reminderList = new ArrayList<>();
                    for (int i = 0, size = msgList.size(); i < size; i++) {
                        MSUIConversationMsg msg = msgList.get(i).uiConversationMsg;
                        if (msg.lastMsgSeq > lastMsg.lastMsgSeq) {
                            lastMsg.lastMsgSeq = msg.lastMsgSeq;
                        }
                        if (msg.lastMsgTimestamp > lastMsg.lastMsgTimestamp) {
                            lastMsg.lastMsgTimestamp = msg.lastMsgTimestamp;
                            lastMsg.clientMsgNo = msg.clientMsgNo;
                        }
                        unreadCount += msg.unreadCount;
                        List<MSReminder> tempReminders = msg.getReminderList();
                        if (MSReader.isNotEmpty(tempReminders)) {
                            reminderList.addAll(tempReminders);
                        }
                    }
                    lastMsg.unreadCount = unreadCount;
                    lastMsg.setReminderList(reminderList);

                    lastConvMsg = new ChatConversationMsg(lastMsg);
                    lastConvMsg.childList = msgList;
                }
                if (lastConvMsg != null)
                    list.add(lastConvMsg);
            }
        }
    }

    long lastMessageTime = 0L;

    private void scrollToUnreadChannel() {
        long firstTime = 0L;
        int firstIndex = 0;
        boolean isScrollToFirstIndex = true;
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) msVBinding.recyclerView.getLayoutManager();
        for (int i = 0, size = chatConversationAdapter.getData().size(); i < size; i++) {
            if (chatConversationAdapter.getData().get(i).getUnReadCount() > 0 && chatConversationAdapter.getData().get(i).uiConversationMsg.getMsChannel() != null && chatConversationAdapter.getData().get(i).uiConversationMsg.getMsChannel().mute == 0) {
                if (firstTime == 0) {
                    firstTime = chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp;
                    firstIndex = i;
                }
                if (lastMessageTime == 0 || lastMessageTime > chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp) {
                    lastMessageTime = chatConversationAdapter.getData().get(i).uiConversationMsg.lastMsgTimestamp;
                    if (linearLayoutManager != null) {
                        linearLayoutManager.scrollToPositionWithOffset(i, 0);
                    }
                    isScrollToFirstIndex = false;
                    break;
                }
            }

        }
        if (isScrollToFirstIndex) {
            lastMessageTime = firstTime;
            if (linearLayoutManager != null) {
                linearLayoutManager.scrollToPositionWithOffset(firstIndex, 0);
            }
        }
    }

}
