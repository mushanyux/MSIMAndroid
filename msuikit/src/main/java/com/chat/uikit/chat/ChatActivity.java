package com.chat.uikit.chat;

import static androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSBinder;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSConstants;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.config.MSSystemAccount;
import com.chat.base.emoji.MoonUtil;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.AvatarOtherViewMenu;
import com.chat.base.endpoint.entity.CallingViewMenu;
import com.chat.base.endpoint.entity.RTCMenu;
import com.chat.base.endpoint.entity.ReadMsgMenu;
import com.chat.base.endpoint.entity.SetChatBgMenu;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.entity.UserOnlineStatus;
import com.chat.base.entity.MSChannelCustomerExtras;
import com.chat.base.entity.MSGroupType;
import com.chat.base.msg.ChatAdapter;
import com.chat.base.msg.ChatContentSpanType;
import com.chat.base.msg.IConversationContext;
import com.chat.base.msgitem.MSChannelMemberRole;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.msgitem.MSUIChatMsgItemEntity;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.NumberTextView;
import com.chat.base.ui.components.SystemMsgBackgroundColorSpan;
import com.chat.base.utils.ActManagerUtils;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.UserUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSPermissions;
import com.chat.base.utils.MSPlaySound;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.base.utils.MSToastUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.base.utils.systembar.MSStatusBarUtils;
import com.chat.base.views.CommonAnim;
import com.chat.base.views.swipeback.SwipeBackActivity;
import com.chat.base.views.swipeback.SwipeBackLayout;
import com.chat.uikit.R;
import com.chat.uikit.MSUIKitApplication;
import com.chat.uikit.chat.manager.SendMsgEntity;
import com.chat.uikit.chat.manager.MSIMUtils;
import com.chat.uikit.chat.manager.MSSendMsgUtils;
import com.chat.uikit.chat.msgmodel.MSCardContent;
import com.chat.uikit.contacts.ChooseContactsActivity;
import com.chat.uikit.databinding.ActChatLayoutBinding;
import com.chat.uikit.group.ChooseVideoCallMembersActivity;
import com.chat.uikit.group.GroupDetailActivity;
import com.chat.uikit.group.service.GroupModel;
import com.chat.uikit.message.MsgModel;
import com.chat.uikit.robot.service.MSRobotModel;
import com.chat.uikit.user.service.UserModel;
import com.chat.uikit.view.MSPlayVoiceUtils;
import com.effective.android.panel.PanelSwitchHelper;
import com.effective.android.panel.interfaces.ContentScrollMeasurer;
import com.effective.android.panel.interfaces.listener.OnPanelChangeListener;
import com.effective.android.panel.view.panel.IPanelView;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSCMD;
import com.mushanyux.mushanim.entity.MSCMDKeys;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelExtras;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelState;
import com.mushanyux.mushanim.entity.MSChannelStatus;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSConversationMsgExtra;
import com.mushanyux.mushanim.entity.MSMentionType;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.entity.MSMsgReaction;
import com.mushanyux.mushanim.entity.MSReminder;
import com.mushanyux.mushanim.entity.MSSendOptions;
import com.mushanyux.mushanim.interfaces.IGetOrSyncHistoryMsgBack;
import com.mushanyux.mushanim.message.type.MSConnectStatus;
import com.mushanyux.mushanim.message.type.MSSendMsgResult;
import com.mushanyux.mushanim.msgmodel.MSImageContent;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;
import com.mushanyux.mushanim.msgmodel.MSMsgEntity;
import com.mushanyux.mushanim.msgmodel.MSReply;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ChatActivity extends SwipeBackActivity implements IConversationContext {
    private String channelId = "";
    private byte channelType = MSChannelType.PERSONAL;
    private ChatAdapter chatAdapter;
    //是否在查看历史消息
    private boolean isShowHistory;
    private boolean isSyncLastMsg = false;
    private boolean isToEnd = true;
    private boolean isViewingPicture = false;
    private final boolean showNickName = true; // 是否显示聊天昵称
    private long lastPreviewMsgOrderSeq = 0; //上次浏览消息
    private long unreadStartMsgOrderSeq = 0; //新消息开始位置
    private long tipsOrderSeq = 0; //需要强提示的msg
    private int keepOffsetY = 0; // 上次浏览消息的偏移量
    private int redDot = 0; // 未读消息数量
    private int lastVisibleMsgSeq = 0; // 最后可见消息序号
    private int maxMsgSeq = 0;
    private long maxMsgOrderSeq = 0;
    //回复的消息对象
    private MSMsg replyMSMsg;
    // 编辑对象
    private MSMsg editMsg;
    // 群成员数量
    private int count;
    private int groupType = MSGroupType.normalGroup;
    //已读消息ID
    private final List<String> readMsgIds = new ArrayList<>();
    private Disposable disposable;
    private boolean isUploadReadMsg = true;
    private NumberTextView numberTextView;
    //    boolean isUpdateCoverMsg = false;
    private boolean isCanLoadMore;
    boolean isRefreshLoading = false;
    boolean isMoreLoading = false;
    boolean isCanRefresh = true;
    private boolean isShowChatActivity = true;
    LinearLayoutManager linearLayoutManager;
    private final List<MSReminder> reminderList = new ArrayList<>();
    private final List<MSReminder> groupApproveList = new ArrayList<>();
    private final List<Long> reminderIds = new ArrayList<>();
    private long browseTo = 0;
    private boolean isUpdateRedDot = true;
    private ImageView callIV;
    //查询聊天数据偏移量
    private final int limit = 30;
    private boolean isShowPinnedView = false;
    private boolean isShowCallingView = false;
    private boolean isTipMessage = false;
    private int hideChannelAllPinnedMessage = 0;
    private PanelSwitchHelper mHelper;
    private ChatPanelManager chatPanelManager;
    private ActChatLayoutBinding msVBinding;
    private int unfilledHeight = 0;
    private final String loginUID = MSConfig.getInstance().getUid();
    private final int callingViewHeight = AndroidUtilities.dp(40f);
    private final int pinnedViewHeight = AndroidUtilities.dp(50f);

    private int getTopPinViewHeight() {
        int totalHeight = 0;
        if (isShowCallingView) {
            totalHeight += callingViewHeight;
        }
        if (isShowPinnedView) {
            totalHeight += pinnedViewHeight;
        }
        return totalHeight;
    }

    private void p2pCall(int callType) {
        EndpointManager.getInstance().invoke("ms_p2p_call", new RTCMenu(this, callType));
    }

    private void toggleStatusBarMode() {
        Window window = getWindow();
        if (window == null) return;
        MSStatusBarUtils.transparentStatusBar(window);
        if (!Theme.getDarkModeStatus(this))
            MSStatusBarUtils.setDarkMode(window);
        else MSStatusBarUtils.setLightMode(window);
    }

    private void initParam() {
        toggleStatusBarMode();
        //频道ID
        channelId = getIntent().getStringExtra("channelId");
        //频道类型
        channelType = getIntent().getByteExtra("channelType", MSChannelType.PERSONAL);
        maxMsgOrderSeq = MSIM.getInstance().getMsgManager().getMaxOrderSeqWithChannel(channelId, channelType);
        maxMsgSeq = MSIM.getInstance().getMsgManager().getMaxMessageSeqWithChannel(channelId, channelType);
        resetHideChannelAllPinnedMessage();
        // 是否含有带转发的消息
        if (getIntent().hasExtra("msgContentList")) {
            List<MSMessageContent> msgContentList = getIntent().getParcelableArrayListExtra("msgContentList");
            if (MSReader.isNotEmpty(msgContentList)) {
                List<MSChannel> list = new ArrayList<>();
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelId, channelType);
                list.add(channel);
                MSUIKitApplication.getInstance().showChatConfirmDialog(this, list, msgContentList, (list1, messageContentList) -> {
                    List<SendMsgEntity> msgList = new ArrayList<>();
                    MSSendOptions options = new MSSendOptions();
                    options.setting.receipt = getChatChannelInfo().receipt;
                    for (int i = 0, size = msgContentList.size(); i < size; i++) {
                        msgList.add(new SendMsgEntity(msgContentList.get(i), channel, options));
                    }
                    MSSendMsgUtils.getInstance().sendMessages(msgList);
                });

            }
        }

    }

    private void initSwipeBackFinish() {
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        mSwipeBackLayout.setEnableGesture(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSwipeBackFinish();
        msVBinding = DataBindingUtil.setContentView(this, R.layout.act_chat_layout);
//        setContentView(R.layout.act_chat_layout1);
        initParam();
        initView();
        initListener();
        //initData();
        ActManagerUtils.getInstance().addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isShowChatActivity = true;
        MSUIKitApplication.getInstance().chattingChannelID = channelId;
        isUploadReadMsg = true;
        chatPanelManager.initRefreshListener();
        EndpointManager.getInstance().invoke("start_screen_shot", this);

        Object addSecurityModule = EndpointManager.getInstance().invoke("add_security_module", null);
        if (addSecurityModule instanceof Boolean) {
            boolean disable_screenshot;
            String uid = MSConfig.getInstance().getUid();
            if (!TextUtils.isEmpty(uid)) {
                disable_screenshot = MSSharedPreferencesUtil.getInstance().getBoolean(uid + "_disable_screenshot", false);
            } else {
                disable_screenshot = MSSharedPreferencesUtil.getInstance().getBoolean("disable_screenshot", false);
            }
            if (disable_screenshot)
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mHelper == null) {
            mHelper = new PanelSwitchHelper.Builder(this)
                    //可选
                    .addKeyboardStateListener((visible, height) -> {
                        if (visible && height > 0) {
                            MSConstants.setKeyboardHeight(height);
                        }
                    })
                    //可选
                    .addPanelChangeListener(new OnPanelChangeListener() {

                        @Override
                        public void onKeyboard() {
                            chatPanelManager.resetToolBar();
                            SoftKeyboardUtils.getInstance().requestFocus(msVBinding.editText);
                        }

                        @Override
                        public void onNone() {
                        }

                        @Override
                        public void onPanel(IPanelView view) {
                        }


                        @Override
                        public void onPanelSizeChange(IPanelView panelView, boolean portrait, int oldWidth, int oldHeight, int width, int height) {

                        }
                    }).addContentScrollMeasurer(new ContentScrollMeasurer() {
                        @Override
                        public int getScrollDistance(int i) {
                            View bottomView = findViewById(R.id.bottomView);
                            View followView = findViewById(R.id.followScrollView);
                            return i - (bottomView.getTop() - followView.getBottom());
                        }

                        @Override
                        public int getScrollViewId() {
                            return R.id.recyclerViewLayout;
                        }
                    }).addContentScrollMeasurer(new ContentScrollMeasurer() {
                        @Override
                        public int getScrollDistance(int i) {
                            return 0;
                        }

                        @Override
                        public int getScrollViewId() {
                            return R.id.scrollViewLayout;
                        }
                    }).addContentScrollMeasurer(new ContentScrollMeasurer() {
                        @Override
                        public int getScrollDistance(int i) {
                            return 0;
                        }

                        @Override
                        public int getScrollViewId() {
                            return R.id.timeTv;
                        }
                    }).addContentScrollMeasurer(new ContentScrollMeasurer() {
                        @Override
                        public int getScrollDistance(int i) {
                            return 0;
                        }

                        @Override
                        public int getScrollViewId() {
                            return R.id.imageView;
                        }
                    }).addContentScrollMeasurer(new ContentScrollMeasurer() {
                        @Override
                        public int getScrollDistance(int i) {
                            return i - unfilledHeight;
                        }

                        @Override
                        public int getScrollViewId() {
                            return R.id.recyclerView;
                        }
                    })
                    .logTrack(MSBinder.isDebug)
                    .build(false);
        }
        if (chatPanelManager == null) {
            FrameLayout moreView = findViewById(R.id.chatMoreLayout);
            chatPanelManager = new ChatPanelManager(mHelper, findViewById(R.id.bottomView), moreView, findViewById(R.id.followScrollView), this, () -> {
                CommonAnim.getInstance().rotateImage(msVBinding.topLayout.backIv, 180f, 360f, R.mipmap.ic_ab_back);
                numberTextView.setNumber(0, true);
                CommonAnim.getInstance().showOrHide(numberTextView, false, true);
                CommonAnim.getInstance().showOrHide(callIV, true, true);
                return null;
            }, path -> {
                Intent intent = new Intent(ChatActivity.this, PreviewNewImgActivity.class);
                intent.putExtra("path", path);
                previewNewImgResultLac.launch(intent);
                return null;
            });
            initData();
        }
    }

    protected void initView() {
        EndpointManager.getInstance().invoke("set_chat_bg", new SetChatBgMenu(channelId, channelType, msVBinding.imageView, msVBinding.rootView, msVBinding.blurView));
        Object pinnedLayoutView = EndpointManager.getInstance().invoke("get_pinned_message_view", this);
        if (pinnedLayoutView instanceof View) {
            msVBinding.pinnedLayout.addView((View) pinnedLayoutView);
        }
        msVBinding.timeTv.setShadowLayer(AndroidUtilities.dp(5f), 0f, 0f, 0);
        CommonAnim.getInstance().showOrHide(msVBinding.timeTv, false, true);
        Theme.setPressedBackground(msVBinding.topLayout.backIv);
        msVBinding.topLayout.backIv.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.titleBarIcon), PorterDuff.Mode.MULTIPLY));
        msVBinding.topLayout.avatarView.setSize(40);
        msVBinding.chatUnreadLayout.progress.setSize(40);
        msVBinding.chatUnreadLayout.progress.setStrokeWidth(1.5f);
        msVBinding.chatUnreadLayout.progress.setProgressColor(ContextCompat.getColor(this, R.color.popupTextColor));

        msVBinding.chatUnreadLayout.msgCountTv.setColors(R.color.white, R.color.reminderColor);
        msVBinding.chatUnreadLayout.remindCountTv.setColors(R.color.white, R.color.reminderColor);
        msVBinding.chatUnreadLayout.approveCountTv.setColors(R.color.white, R.color.reminderColor);

        numberTextView = new NumberTextView(this);
        numberTextView.setTextSize(18);
        numberTextView.setTextColor(Theme.colorAccount);
        msVBinding.topLayout.rightView.addView(numberTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.END, 0, 0, 15, 0));

        Object isRegisterRTC = EndpointManager.getInstance().invoke("is_register_rtc", null);

        callIV = new AppCompatImageView(this);
        callIV.setImageResource(R.mipmap.ic_call);
        if (isRegisterRTC instanceof Boolean) {
            boolean isRegister = (boolean) isRegisterRTC;
            if (isRegister) {
                msVBinding.topLayout.rightView.addView(callIV, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.END, 0, 0, 15, 0));
            }
        }
        callIV.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.popupTextColor), PorterDuff.Mode.MULTIPLY));
        callIV.setBackground(Theme.createSelectorDrawable(Theme.getPressedColor()));

        CommonAnim.getInstance().showOrHide(numberTextView, false, false);

        //去除刷新条目闪动动画
        ((DefaultItemAnimator) Objects.requireNonNull(msVBinding.recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        chatAdapter = new ChatAdapter(this, ChatAdapter.AdapterType.normalMessage);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        msVBinding.recyclerView.setLayoutManager(linearLayoutManager);
        msVBinding.recyclerView.setAdapter(chatAdapter);
        msVBinding.recyclerView.setItemAnimator(new MyItemAnimator());
        chatAdapter.setAnimationFirstOnly(true);
        chatAdapter.setAnimationEnable(false);

    }

    private void initListener() {
        ItemTouchHelper helper = new ItemTouchHelper(new MessageSwipeController(this, new SwipeControllerActions() {
            @Override
            public void showReplyUI(int position) {
                showReply(chatAdapter.getData().get(position).msMsg);
            }

            @Override
            public void hideSoft() {
                //   mHelper.resetState();
            }
        }));
        helper.attachToRecyclerView(msVBinding.recyclerView);
        msVBinding.topLayout.backIv.setOnClickListener(v -> setBackListener());
        callIV.setOnClickListener(view -> {
            MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, loginUID);
            if (getChatChannelInfo().forbidden == 1 || (member != null && member.forbiddenExpirationTime > 0)) {
                MSToastUtils.getInstance().showToast(getString(R.string.can_not_call_forbidden));
                return;
            }
            String desc = String.format(getString(R.string.microphone_permissions_des), getString(R.string.app_name));
            MSPermissions.getInstance().checkPermissions(new MSPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        if (channelType == MSChannelType.PERSONAL) {
                            if (UserUtils.getInstance().checkMyFriendDelete(channelId) || UserUtils.getInstance().checkFriendRelation(channelId)) {
                                showToast(R.string.non_friend_relationship);
                                return;
                            }
                            if (UserUtils.getInstance().checkBlacklist(channelId)) {
                                showToast(R.string.call_be_blacklist);
                                return;
                            }
                            if (getChatChannelInfo().status == MSChannelStatus.statusBlacklist) {
                                showToast(R.string.call_blacklist);
                                return;
                            }
                            List<PopupMenuItem> list = new ArrayList<>();
                            list.add(new PopupMenuItem(getString(R.string.video_call), R.mipmap.chat_calls_video, () -> p2pCall(1)));
                            list.add(new PopupMenuItem(getString(R.string.audio_call), R.mipmap.chat_calls_voice, () -> p2pCall(0)));
                            MSDialogUtils.getInstance().showScreenPopup(view, list);
                        } else {
                            MSChannelMember channelMember = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, loginUID);
                            if (channelMember != null && channelMember.status == MSChannelStatus.statusBlacklist) {
                                showToast(R.string.call_blacklist_group);
                                return;
                            }
                            Intent intent = new Intent(ChatActivity.this, ChooseVideoCallMembersActivity.class);
                            intent.putExtra("channelID", channelId);
                            intent.putExtra("channelType", channelType);
                            intent.putExtra("isCreate", true);
                            startActivity(intent);
                        }
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {
                }
            }, this, desc, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
        });

        MSDialogUtils.getInstance().setViewLongClickPopup(msVBinding.chatUnreadLayout.groupApproveLayout, getGroupApprovePopupItems());
        msVBinding.chatUnreadLayout.groupApproveLayout.setOnClickListener(view -> {
            if (MSReader.isNotEmpty(groupApproveList)) {
                MSMsg msg = MSIM.getInstance().getMsgManager().getWithMessageID(groupApproveList.get(0).messageID);
                if (msg != null && !TextUtils.isEmpty(msg.clientMsgNO)) {
                    tipsMsg(msg.clientMsgNO);
                }
            }
        });
        MSDialogUtils.getInstance().setViewLongClickPopup(msVBinding.chatUnreadLayout.remindLayout, getRemindPopupItems());
        msVBinding.chatUnreadLayout.remindLayout.setOnClickListener(view -> {

            if (MSReader.isNotEmpty(reminderList)) {
                reminderIds.add(reminderList.get(0).reminderID);
                MSMsg msg = MSIM.getInstance().getMsgManager().getWithMessageID(reminderList.get(0).messageID);
                if (msg != null && !TextUtils.isEmpty(msg.clientMsgNO)) {
                    tipsMsg(msg.clientMsgNO);
                } else {
                    long orderSeq = MSIM.getInstance().getMsgManager().getMessageOrderSeq(reminderList.get(0).messageSeq, channelId, channelType);
                    unreadStartMsgOrderSeq = 0;
                    tipsOrderSeq = orderSeq;
                    getData(1, true, orderSeq, false);
                    isCanLoadMore = true;
                }
            }
        });

        SingleClickUtil.onSingleClick(msVBinding.topLayout.titleView, view -> {
            MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, loginUID);

            if ((member != null && member.isDeleted == 1) || channelType == MSChannelType.CUSTOMER_SERVICE)
                return;
//              SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.toolbarView.editText);
            Intent intent = new Intent(ChatActivity.this, channelType == MSChannelType.GROUP ? GroupDetailActivity.class : ChatPersonalActivity.class);
            intent.putExtra("channelId", channelId);
            startActivity(intent);
        });

        msVBinding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (chatAdapter.getData().size() <= 1) return;
                setShowTime();
                int lastItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                if (lastItemPosition < chatAdapter.getItemCount() - 1) {
                    msVBinding.chatUnreadLayout.newMsgLayout.post(() -> CommonAnim.getInstance().showOrHide(msVBinding.chatUnreadLayout.newMsgLayout, dy > 0 || redDot > 0, true, false));
                } else {
                    msVBinding.chatUnreadLayout.newMsgLayout.post(() -> CommonAnim.getInstance().showOrHide(msVBinding.chatUnreadLayout.newMsgLayout, redDot > 0, true, false));
                }
                resetRemindView();
                resetGroupApproveView();

                View lastChildView = linearLayoutManager.findViewByPosition(lastItemPosition);
                if (lastChildView != null) {
                    int bottom = lastChildView.getBottom();
                    int listHeight = msVBinding.recyclerView.getHeight() - msVBinding.recyclerView.getPaddingBottom();
                    unfilledHeight = listHeight - bottom;
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                isShowHistory = lastItemPosition < chatAdapter.getItemCount() - 1;
                if (newState == SCROLL_STATE_IDLE) {
                    isTipMessage = false;
                    CommonAnim.getInstance().showOrHide(msVBinding.timeTv, false, true);
                    EndpointManager.getInstance().invoke("stop_reaction_animation", null);
                    if (!msVBinding.recyclerView.canScrollVertically(1)) { // 到达底部
                        showMoreLoading();
                    } else if (!msVBinding.recyclerView.canScrollVertically(-1)) { // 到达顶部
                        showRefreshLoading();
                    }
                } else {
                    MsgModel.getInstance().doneReminder(reminderIds);
                    if (!isUpdateRedDot) return;
                    MsgModel.getInstance().clearUnread(channelId, channelType, redDot, (code, msg) -> {
                        if (code == HttpResponseCode.success && redDot == 0) {
                            isUpdateRedDot = false;
                        }
                    });
                }
            }
        });

        msVBinding.chatUnreadLayout.newMsgLayout.setOnClickListener(v -> {
            redDot = 0;
            MsgModel.getInstance().clearUnread(channelId, channelType, redDot, (code, msg) -> {
                if (code == HttpResponseCode.success && redDot == 0) {
                    isUpdateRedDot = false;
                }
            });
            if (isCanLoadMore) {
                isSyncLastMsg = true;
                // chatAdapter.setList(new ArrayList<>());
                msVBinding.chatUnreadLayout.progress.setVisibility(View.VISIBLE);
                msVBinding.chatUnreadLayout.msgDownIv.setVisibility(View.GONE);
                unreadStartMsgOrderSeq = 0;
                lastPreviewMsgOrderSeq = 0;
                long maxSeq = MSIM.getInstance().getMsgManager().getMaxOrderSeqWithChannel(channelId, channelType);
                new Handler().postDelayed(() -> {
                    getData(0, true, maxSeq, true);
                    showUnReadCountView();
                }, 500);
            } else {
                scrollToPosition(chatAdapter.getItemCount() - 1);
                showUnReadCountView();
            }

            isShowHistory = false;
            isCanLoadMore = false;
        });

        //监听频道改变通知
        MSIM.getInstance().getChannelManager().addOnRefreshChannelInfo(channelId, (channel, isEnd) -> {
            if (channel == null) return;
            if (channel.channelID.equals(channelId) && channel.channelType == channelType) { //同一个会话
                showChannelName(channel);
                msVBinding.topLayout.avatarView.showAvatar(channel);
                EndpointManager.getInstance().invoke("show_avatar_other_info", new AvatarOtherViewMenu(msVBinding.topLayout.otherLayout, channel, msVBinding.topLayout.avatarView, true));
                //用户在线状态
                if (channel.channelType == MSChannelType.PERSONAL) {
                    setOnlineView(channel);
                } else {
                    if (channel.remoteExtraMap != null) {
                        Object memberCountObject = channel.remoteExtraMap.get(MSChannelCustomerExtras.memberCount);
                        if (memberCountObject instanceof Integer) {
                            int count = (int) memberCountObject;
                            msVBinding.topLayout.subtitleTv.setText(String.format(getString(R.string.group_member), count));
                        }
                        Object onlineCountObject = channel.remoteExtraMap.get(MSChannelCustomerExtras.onlineCount);
                        if (onlineCountObject instanceof Integer) {
                            int onlineCount = (int) onlineCountObject;
                            if (onlineCount > 0) {
                                msVBinding.topLayout.subtitleCountTv.setVisibility(View.VISIBLE);
                                msVBinding.topLayout.subtitleCountTv.setText(String.format(getString(R.string.online_count), onlineCount));
                            }
                        }
                    }
                }
                EndpointManager.getInstance().invoke("set_chat_bg", new SetChatBgMenu(channelId, channelType, msVBinding.imageView, msVBinding.rootView, msVBinding.blurView));
            } else {
                for (int i = 0, size = chatAdapter.getData().size(); i < size; i++) {
                    if (TextUtils.isEmpty(chatAdapter.getData().get(i).msMsg.fromUID)) continue;
                    boolean isRefresh = false;
                    if (chatAdapter.getData().get(i).msMsg.fromUID.equals(channel.channelID) && channel.channelType == MSChannelType.PERSONAL) {
                        chatAdapter.getData().get(i).msMsg.setFrom(channel);
                        isRefresh = true;
                    }
                    if (chatAdapter.getData().get(i).msMsg.getMemberOfFrom() != null && chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberUID.equals(channel.channelID) && channel.channelType == MSChannelType.PERSONAL) {
                        chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberRemark = channel.channelRemark;
                        chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberName = channel.channelName;
                        chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberAvatar = channel.avatar;
                        chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberAvatarCacheKey = channel.avatarCacheKey;
                        isRefresh = true;
                    }
                    if (chatAdapter.getData().get(i).msMsg.baseContentMsgModel != null && MSReader.isNotEmpty(chatAdapter.getData().get(i).msMsg.baseContentMsgModel.entities)) {
                        for (MSMsgEntity entity : chatAdapter.getData().get(i).msMsg.baseContentMsgModel.entities) {
                            if (entity.type.equals(ChatContentSpanType.getMention()) && !TextUtils.isEmpty(entity.value) && entity.value.equals(channel.channelID)) {
                                isRefresh = true;
                                chatAdapter.getData().get(i).formatSpans(ChatActivity.this, chatAdapter.getData().get(i).msMsg);
                                break;
                            }
                        }
                    }
                    if (isRefresh) {
                        chatAdapter.getData().get(i).isRefreshAvatarAndName = true;
                        chatAdapter.notifyItemChanged(i, chatAdapter.getData().get(i));
                    }
                }
            }
        });

        //监听频道成员信息改变通知
        MSIM.getInstance().getChannelMembersManager().addOnRefreshChannelMemberInfo(channelId, (channelMember, isEnd) -> {
            if (channelMember != null && !TextUtils.isEmpty(channelMember.channelID)) {
                if (channelMember.channelID.equals(channelId) && channelMember.channelType == channelType) {
                    if (channelMember.channelType == MSChannelType.PERSONAL) {
                        String name = channelMember.memberRemark;
                        if (TextUtils.isEmpty(name)) name = channelMember.memberName;
                        msVBinding.topLayout.titleCenterTv.setText(name);
                    } else {
                        //成员名字改变
                        for (int i = 0, size = chatAdapter.getData().size(); i < size; i++) {
                            if (chatAdapter.getData().get(i).msMsg != null && chatAdapter.getData().get(i).msMsg.getMemberOfFrom() != null && !TextUtils.isEmpty(chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberUID) && chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberUID.equals(channelMember.memberUID)) {
                                chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberName = channelMember.memberName;
                                chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberRemark = channelMember.memberRemark;
                                chatAdapter.getData().get(i).msMsg.getMemberOfFrom().memberAvatar = channelMember.memberAvatar;
                                chatAdapter.getData().get(i).isRefreshAvatarAndName = true;
                                chatAdapter.notifyItemChanged(i, chatAdapter.getData().get(i));
                            }
                        }
                    }
                }
            }
            if (isEnd) {
                checkLoginUserInGroupStatus();
            }
        });

        //监听移除频道成员
        MSIM.getInstance().getChannelMembersManager().addOnRemoveChannelMemberListener(channelId, list -> {
            if (MSReader.isNotEmpty(list) && !TextUtils.isEmpty(list.get(0).channelID) && list.get(0).channelID.equals(channelId) && list.get(0).channelType == channelType) {
                if (groupType == MSGroupType.normalGroup) {
                    count = MSIM.getInstance().getChannelMembersManager().getMemberCount(channelId, channelType);
                    msVBinding.topLayout.subtitleTv.setText(String.format(getString(R.string.group_member), count));
                }
                //查询登录用户是否在本群
                checkLoginUserInGroupStatus();
                MSRobotModel.getInstance().syncRobotData(getChatChannelInfo());
            }
        });
        //监听添加频道成员
        MSIM.getInstance().getChannelMembersManager().addOnAddChannelMemberListener(channelId, list -> {
            if (MSReader.isNotEmpty(list) && !TextUtils.isEmpty(list.get(0).channelID) && list.get(0).channelID.equals(channelId) && list.get(0).channelType == channelType && groupType == MSGroupType.normalGroup) {
                count = MSIM.getInstance().getChannelMembersManager().getMemberCount(channelId, channelType);
                msVBinding.topLayout.subtitleTv.setText(String.format(getString(R.string.group_member), count));
                MSRobotModel.getInstance().syncRobotData(getChatChannelInfo());
                checkLoginUserInGroupStatus();
            }
        });
        //监听删除消息
        MSIM.getInstance().getMsgManager().addOnDeleteMsgListener(channelId, msg -> {
            if (msg != null) {
                removeMsg(msg);
            }
        });
        // 命令消息监听
        MSIM.getInstance().getCMDManager().addCmdListener(channelId, msCmd -> {
            if (msCmd == null || TextUtils.isEmpty(msCmd.cmdKey)) return;
            // 监听正在输入
            switch (msCmd.cmdKey) {
                case MSCMDKeys.ms_typing -> typing(msCmd);
                case MSCMDKeys.ms_unreadClear -> {
                    if (msCmd.paramJsonObject.has("channel_id") && msCmd.paramJsonObject.has("channel_type")) {
                        String channelId = msCmd.paramJsonObject.optString("channel_id");
                        int channelType = msCmd.paramJsonObject.optInt("channel_type");
                        int unreadCount = msCmd.paramJsonObject.optInt("unread");
                        if (channelId.equals(this.channelId) && channelType == this.channelType) {
                            if (unreadCount < redDot) {
                                this.redDot = unreadCount;
                                msVBinding.chatUnreadLayout.newMsgLayout.post(() -> CommonAnim.getInstance().showOrHide(msVBinding.chatUnreadLayout.newMsgLayout, redDot > 0, true, false));
                            }
                        }
                    }
                }
                case "sync_channel_state" -> {
                    String sourceChannelId = msCmd.paramJsonObject.optString("channel_id");
                    int sourceChannelType = msCmd.paramJsonObject.optInt("channel_type");
                    if (sourceChannelId.equals(channelId) && sourceChannelType == channelType) {
                        getChannelState();
                    }
                }
            }
        });

        //监听消息刷新
        MSIM.getInstance().getMsgManager().addOnRefreshMsgListener(channelId, (msMsg, left) -> {
            if (msMsg.remoteExtra.isMutualDeleted == 1) {
                removeMsg(msMsg);
                return;
            }
            refreshMsg(msMsg);
        });
        //监听发送消息返回
        MSIM.getInstance().getMsgManager().addOnSendMsgCallback(channelId, this::sendMsgInserted);

        //监听新消息
        MSIM.getInstance().getMsgManager().addOnNewMsgListener(channelId, this::receivedMessages);
        //监听清空聊天记录
        MSIM.getInstance().getMsgManager().addOnClearMsgListener(channelId, (channelID, channelType, fromUID) -> {
            if (!TextUtils.isEmpty(channelID) && ChatActivity.this.channelId.equals(channelID) && ChatActivity.this.channelType == channelType) {
                if (TextUtils.isEmpty(fromUID)) {
                    chatAdapter = new ChatAdapter(ChatActivity.this, ChatAdapter.AdapterType.normalMessage);
                    msVBinding.recyclerView.setAdapter(chatAdapter);
                } else {
                    for (int i = 0; i < chatAdapter.getData().size(); i++) {
                        if (chatAdapter.getData().get(i).msMsg != null && !TextUtils.isEmpty(chatAdapter.getData().get(i).msMsg.fromUID) && chatAdapter.getData().get(i).msMsg.fromUID.equals(fromUID)) {
                            chatAdapter.removeAt(i);
                            i--;
                        }
                    }
                }
            }

        });

        MSIM.getInstance().getReminderManager().addOnNewReminderListener(channelId, this::resetReminder);
        EndpointManager.getInstance().setMethod(channelId, EndpointCategory.msExitChat, object -> {
            if (object != null) {
                MSChannel channel = (MSChannel) object;
                if (channelId.equals(channel.channelID) && channel.channelType == channelType) {
                    finish();
                }
            }
            return null;
        });
        MSIM.getInstance().getConnectionManager().addOnConnectionStatusListener(channelId, (i, s) -> {
            if (i == MSConnectStatus.syncCompleted && MSUIKitApplication.getInstance().isRefreshChatActivityMessage) {
                MSUIKitApplication.getInstance().isRefreshChatActivityMessage = false;
                int maxOrderSeq = MSIM.getInstance().getMsgManager().getMaxOrderSeqWithChannel(channelId, channelType);
                long tempMaxOrderSeq = 0;
                if (chatAdapter != null && chatAdapter.getLastMsg() != null) {
                    tempMaxOrderSeq = chatAdapter.getLastMsg().orderSeq;
                }
                if (maxOrderSeq > tempMaxOrderSeq) {
                    // scrollToEnd();
//                    isCanRefresh = true;
//                    isShowHistory = false;
                    getData(0, true, maxOrderSeq, true);
                }
//                int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
//                if (firstItemPosition == -1) return;
//                if (MSReader.isNotEmpty(chatAdapter.getData())) {
//                    MSMsg msg = chatAdapter.getFirstVisibleItem(firstItemPosition);
//                    if (msg != null) {
////                            keepMsgSeq = msg.messageSeq;
//                        lastPreviewMsgOrderSeq = msg.orderSeq;
//                        int index = chatAdapter.getFirstVisibleItemIndex(firstItemPosition);
//                        View view = linearLayoutManager.findViewByPosition(index);
//                        if (view != null) {
//                            keepOffsetY = view.getTop();
//                        }
//                    }
//                }
//                getData(1, true, lastPreviewMsgOrderSeq, false);
            }
        });
        EndpointManager.getInstance().setMethod(channelId, EndpointCategory.refreshProhibitWord, object -> {
            if (MSReader.isEmpty(chatAdapter.getData())) {
                return 1;
            }
            for (int i = 0, size = chatAdapter.getData().size(); i < size; i++) {
                if (chatAdapter.getData().get(i).msMsg != null && chatAdapter.getData().get(i).msMsg.type == MSContentType.MS_TEXT) {
                    MSIMUtils.getInstance().resetMsgProhibitWord(chatAdapter.getData().get(i).msMsg);
                    chatAdapter.getData().get(i).formatSpans(ChatActivity.this, chatAdapter.getData().get(i).msMsg);
                    chatAdapter.notifyItemChanged(i);
                }
            }
            return 1;
        });
        EndpointManager.getInstance().setMethod("hide_pinned_view", object -> {
            if (!isShowPinnedView) return null;
            isShowPinnedView = false;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) msVBinding.timeTv.getLayoutParams();
            lp.topMargin = AndroidUtilities.dp(10) + getTopPinViewHeight();
            msVBinding.timeTv.setVisibility(View.GONE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(msVBinding.pinnedLayout, "translationY", 0, -AndroidUtilities.dp(53));
            animator.setDuration(200);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    msVBinding.pinnedLayout.clearAnimation();
                    msVBinding.pinnedLayout.setVisibility(View.GONE);
                    if (MSReader.isNotEmpty(chatAdapter.getData()) && chatAdapter.getData().get(0).msMsg != null && chatAdapter.getData().get(0).msMsg.type == MSContentType.spanEmptyView) {
                        if (!isShowCallingView) {
                            chatAdapter.getData().remove(0);
                            chatAdapter.notifyItemRemoved(0);
                        }
                        //chatAdapter.notifyDataSetChanged();
                    }
                }

                public void onAnimationStart(Animator animation) {
                    msVBinding.pinnedLayout.setVisibility(View.VISIBLE);
                }
            });
            msVBinding.pinnedLayout.setVisibility(View.VISIBLE);
            animator.start();
            return null;
        });
        EndpointManager.getInstance().setMethod("show_pinned_view", object -> {
            if (isShowPinnedView) {
                return null;
            }
            isShowPinnedView = true;

            if (MSReader.isNotEmpty(chatAdapter.getData()) && chatAdapter.getData().get(0).msMsg != null && chatAdapter.getData().get(0).msMsg.type != MSContentType.spanEmptyView) {
                MSMsg msg = getSpanEmptyMsg();
                chatAdapter.addData(0, new MSUIChatMsgItemEntity(this, msg, null));
            }
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) msVBinding.timeTv.getLayoutParams();
            lp.topMargin = AndroidUtilities.dp(10) + getTopPinViewHeight();
            msVBinding.timeTv.setVisibility(View.GONE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(msVBinding.pinnedLayout, "translationY", -msVBinding.pinnedLayout.getHeight(), 0);
            animator.setDuration(200);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // msVBinding.pinnedLayout.clearAnimation();
                    msVBinding.pinnedLayout.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
            msVBinding.pinnedLayout.setVisibility(View.VISIBLE);
            return null;
        });
        EndpointManager.getInstance().setMethod("tip_msg_in_chat", object -> {
            tipsMsg((String) object);
            return null;
        });
        EndpointManager.getInstance().setMethod("reset_channel_all_pinned_msg", object -> {
            resetHideChannelAllPinnedMessage();
            for (int i = 0, size = chatAdapter.getData().size(); i < size; i++) {
                if (hideChannelAllPinnedMessage == 1) {
                    if (chatAdapter.getData().get(i).isPinned == 1) {
                        chatAdapter.getData().get(i).isPinned = 0;
                        chatAdapter.notifyStatus(i);
                    }
                } else {
                    if (chatAdapter.getData().get(i).isPinned == 0) {
                        if (chatAdapter.getData().get(i).msMsg.remoteExtra != null && chatAdapter.getData().get(i).msMsg.remoteExtra.isPinned == 1) {
                            chatAdapter.getData().get(i).isPinned = 1;
                            chatAdapter.notifyStatus(i);
                        }
                    }
                }
            }
            return null;
        });
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initParam();
        initData();
    }

    private void initData() {
        startTimer();
        EndpointManager.getInstance().invoke(EndpointSID.openChatPage, getChatChannelInfo());
        //获取网络频道信息
        MSIM.getInstance().getChannelManager().fetchChannelInfo(channelId, channelType);
        MsgModel.getInstance().syncExtraMsg(channelId, channelType);
        MSRobotModel.getInstance().syncRobotData(getChatChannelInfo());
        getChannelState();

        chatAdapter.setList(new ArrayList<>());
        if (MSSystemAccount.isSystemAccount(channelId) || channelType == MSChannelType.CUSTOMER_SERVICE) {
            CommonAnim.getInstance().showOrHide(callIV, false, false);
        }
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelId, channelType);

        String avatarKey = "";
        if (channel != null) {
            msVBinding.topLayout.categoryLayout.removeAllViews();
            avatarKey = channel.avatarCacheKey;
            if (channel.remoteExtraMap != null && channel.remoteExtraMap.containsKey(MSChannelExtras.groupType)) {
                Object object = channel.remoteExtraMap.get(MSChannelExtras.groupType);
                if (object instanceof Integer) {
                    groupType = (int) object;
                }
            }
            if (!TextUtils.isEmpty(channel.category)) {
                if (channel.category.equals(MSSystemAccount.accountCategorySystem)) {
                    msVBinding.topLayout.categoryLayout.addView(Theme.getChannelCategoryTV(this, getString(R.string.official), ContextCompat.getColor(this, R.color.transparent), ContextCompat.getColor(this, R.color.reminderColor), ContextCompat.getColor(this, R.color.reminderColor)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
                if (channel.category.equals(MSSystemAccount.accountCategoryCustomerService)) {
                    msVBinding.topLayout.categoryLayout.addView(Theme.getChannelCategoryTV(this, getString(R.string.customer_service), Theme.colorAccount, ContextCompat.getColor(this, R.color.white), Theme.colorAccount), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
                if (channel.category.equals(MSSystemAccount.accountCategoryVisitor)) {
                    msVBinding.topLayout.categoryLayout.addView(Theme.getChannelCategoryTV(this, getString(R.string.visitor), ContextCompat.getColor(this, R.color.transparent), ContextCompat.getColor(this, R.color.colorFFC107), ContextCompat.getColor(this, R.color.colorFFC107)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
                if (channel.category.equals(MSSystemAccount.channelCategoryOrganization)) {
                    msVBinding.topLayout.categoryLayout.addView(Theme.getChannelCategoryTV(this, getString(R.string.all_staff), ContextCompat.getColor(this, R.color.category_org_bg), ContextCompat.getColor(this, R.color.category_org_text), ContextCompat.getColor(this, R.color.transparent)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
                if (channel.category.equals(MSSystemAccount.channelCategoryDepartment)) {
                    msVBinding.topLayout.categoryLayout.addView(Theme.getChannelCategoryTV(this, getString(R.string.department), ContextCompat.getColor(this, R.color.category_org_bg), ContextCompat.getColor(this, R.color.category_org_text), ContextCompat.getColor(this, R.color.transparent)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 0, 0));
                }
            }
            showChannelName(channel);
            if (channel.robot == 1) {
                msVBinding.topLayout.categoryLayout.addView(Theme.getChannelCategoryTV(this, getString(R.string.bot), ContextCompat.getColor(this, R.color.colorFFC107), ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.colorFFC107)), LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 1, 1, 0));
            }
            EndpointManager.getInstance().invoke("show_avatar_other_info", new AvatarOtherViewMenu(msVBinding.topLayout.otherLayout, channel, msVBinding.topLayout.avatarView, true));
        }
        msVBinding.topLayout.avatarView.showAvatar(channelId, channelType, avatarKey);

        //如果是群聊就同步群成员信息
        if (channelType == MSChannelType.GROUP) {
            if (groupType == MSGroupType.normalGroup) {
                GroupModel.getInstance().groupMembersSync(channelId, (code, msg) -> {
                    if (code == HttpResponseCode.success) {
                        MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, loginUID);
                        hideOrShowRightView(member == null || member.isDeleted != 1);
                        MSRobotModel.getInstance().syncRobotData(getChatChannelInfo());
                        chatPanelManager.showOrHideForbiddenView();
                    }
                });
            } else {
                UserModel.getInstance().getUserInfo(MSConfig.getInstance().getUid(), channelId, null);
            }
            //获取sdk频道信息
            if (channel != null) {
                count = MSIM.getInstance().getChannelMembersManager().getMemberCount(channelId, channelType);
                showChannelName(channel);
                // showNickName = channel.showNick == 1;
                if (channel.forbidden == 1) {
                    chatPanelManager.showOrHideForbiddenView();
                }
                if (channel.status == MSChannelStatus.statusDisabled) {
                    chatPanelManager.showBan();
                } else {
                    chatPanelManager.hideBan();
                }
            }

            MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, loginUID);
            hideOrShowRightView(member == null || member.isDeleted == 0);
            if (groupType == MSGroupType.normalGroup) {
                msVBinding.topLayout.subtitleTv.setText(String.format(getString(R.string.group_member), count));
            }
            msVBinding.topLayout.subtitleView.setVisibility(View.VISIBLE);
            chatPanelManager.showOrHideForbiddenView();
        } else {
            hideOrShowRightView(true);
            msVBinding.topLayout.subtitleCountTv.setVisibility(View.GONE);
            if (channel != null) {
                setOnlineView(channel);
                showChannelName(channel);
            }
        }


        //定位消息
        if (getIntent().hasExtra("lastPreviewMsgOrderSeq")) {
            lastPreviewMsgOrderSeq = getIntent().getLongExtra("lastPreviewMsgOrderSeq", 0L);
            isCanLoadMore = lastPreviewMsgOrderSeq > 0;
        }
        if (getIntent().hasExtra("keepOffsetY")) {
            keepOffsetY = getIntent().getIntExtra("keepOffsetY", 0);
        }
        if (getIntent().hasExtra("redDot")) redDot = getIntent().getIntExtra("redDot", 0);
        if (getIntent().hasExtra("tipsOrderSeq")) {
            tipsOrderSeq = getIntent().getLongExtra("tipsOrderSeq", 0);
        }
        if (getIntent().hasExtra("unreadStartMsgOrderSeq")) {
            unreadStartMsgOrderSeq = getIntent().getLongExtra("unreadStartMsgOrderSeq", 0);
        }

        List<MSReminder> allReminder = MSIM.getInstance().getReminderManager().getReminders(channelId, channelType);
        if (MSReader.isNotEmpty(allReminder)) {
            for (MSReminder reminder : allReminder) {
                boolean isPublisher = !TextUtils.isEmpty(reminder.publisher) && reminder.publisher.equals(loginUID);
                if (reminder.type == MSMentionType.MSReminderTypeMentionMe && reminder.done == 0 && !isPublisher) {
                    reminderList.add(reminder);
                }
                if (reminder.type == MSMentionType.MSApplyJoinGroupApprove && reminder.done == 0) {
                    groupApproveList.add(reminder);
                }
            }
        }
        // 先获取聊天数据
        boolean isScrollToEnd = unreadStartMsgOrderSeq == 0 && lastPreviewMsgOrderSeq == 0;
        long aroundMsgSeq = 0;
        if (unreadStartMsgOrderSeq != 0) {
            aroundMsgSeq = unreadStartMsgOrderSeq;
            isCanLoadMore = true;
        }
        isUpdateRedDot = unreadStartMsgOrderSeq > 0;
        if (lastPreviewMsgOrderSeq != 0) aroundMsgSeq = lastPreviewMsgOrderSeq;
        if (tipsOrderSeq != 0) {
            aroundMsgSeq = tipsOrderSeq;
            isCanLoadMore = true;
        }
        if (aroundMsgSeq == 0 && getIntent().hasExtra("aroundMsgSeq")) {
            aroundMsgSeq = getIntent().getLongExtra("aroundMsgSeq", 0);
        }
        getData(lastPreviewMsgOrderSeq == 0 ? 0 : 1, unreadStartMsgOrderSeq > 0, aroundMsgSeq, isScrollToEnd);

        //查询高光内容
        MSConversationMsgExtra extra = MSIM.getInstance().getConversationManager().getMsgExtraWithChannel(channelId, channelType);
        if (extra != null) {
            if (!TextUtils.isEmpty(extra.draft)) {
                chatPanelManager.setEditContent(extra.draft);
            }
            browseTo = extra.browseTo;
        }
        new Handler().postDelayed(() -> {
            resetRemindView();
            resetGroupApproveView();
        }, 150);

    }

    private void getChannelState() {
        MSCommonModel.getInstance().getChannelState(channelId, channelType, channelState -> {
            if (channelState != null) {
                if (channelType == MSChannelType.GROUP && channelState.online_count > 0) {
                    msVBinding.topLayout.subtitleCountTv.setVisibility(View.VISIBLE);
                    msVBinding.topLayout.subtitleCountTv.setText(String.format(getString(R.string.online_count), channelState.online_count));
                }
                if (channelType == MSChannelType.PERSONAL) {
                    return;
                }
                if (channelState.call_info == null || MSReader.isEmpty(channelState.call_info.getCalling_participants())) {
                    msVBinding.callLayout.setVisibility(View.GONE);
                    isShowCallingView = false;
                    if (MSReader.isNotEmpty(chatAdapter.getData()) && chatAdapter.getData().get(0).msMsg.type == MSContentType.spanEmptyView) {
                        if (!isShowPinnedView) {
                            chatAdapter.getData().remove(0);
                            chatAdapter.notifyItemRemoved(0);
                        } else {
                            chatAdapter.getData().get(0).msMsg.messageSeq = getTopPinViewHeight();
                            chatAdapter.notifyItemChanged(0);
                        }
                    }
                } else {
                    Object object = EndpointManager.getInstance().invoke("show_calling_participants", new CallingViewMenu(this, channelState.call_info));
                    if (object != null) {
                        View view = (View) object;
                        msVBinding.callLayout.removeAllViews();
                        msVBinding.callLayout.addView(view, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
                        msVBinding.callLayout.setVisibility(View.VISIBLE);
                        isShowCallingView = true;
                        if (isAddedSpanEmptyView()) {
                            chatAdapter.getData().get(0).msMsg.messageSeq = getTopPinViewHeight();
                            chatAdapter.notifyItemChanged(0);
                        } else {
                            MSMsg msg = getSpanEmptyMsg();
                            chatAdapter.addData(0, new MSUIChatMsgItemEntity(this, msg, null));
                        }
                    } else {
                        isShowCallingView = false;
                    }
                }
            }

            if (MSReader.isEmpty(MsgModel.getInstance().channelStatus)) {
                MsgModel.getInstance().channelStatus = new ArrayList<>();
            }
            boolean isAdd = true;
            for (int i = 0; i < MsgModel.getInstance().channelStatus.size(); i++) {
                if (MsgModel.getInstance().channelStatus.get(i).channel_id.equals(channelId)) {
                    MsgModel.getInstance().channelStatus.get(i).calling = isShowCallingView ? 1 : 0;
                    isAdd = false;
                    break;
                }
            }
            if (isAdd) {
                MSChannelState state = new MSChannelState();
                state.channel_id = channelId;
                state.channel_type = channelType;
                state.calling = isShowCallingView ? 1 : 0;
                MsgModel.getInstance().channelStatus.add(state);
            }
            EndpointManager.getInstance().invoke("refresh_conversation_calling", null);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) msVBinding.timeTv.getLayoutParams();
            lp.topMargin = AndroidUtilities.dp(10) + getTopPinViewHeight();
            msVBinding.timeTv.setVisibility(View.GONE);
        });
    }

    // 获取聊天记录
    private void getData(int pullMode, boolean isSetNewData, long aroundMsgOrderSeq, boolean isScrollToEnd) {
        boolean contain = false;
        long oldestOrderSeq;
        if (pullMode == 1) {
            oldestOrderSeq = chatAdapter.getEndMsgOrderSeq();
        } else {
            oldestOrderSeq = chatAdapter.getFirstMsgOrderSeq();
        }
        if (isSyncLastMsg) {
            oldestOrderSeq = 0;
        }
        //定位消息
        if (lastPreviewMsgOrderSeq != 0) {
            contain = true;
            oldestOrderSeq = lastPreviewMsgOrderSeq;
        }
        if (unreadStartMsgOrderSeq != 0) contain = true;
        MSIM.getInstance().getMsgManager().getOrSyncHistoryMessages(channelId, channelType, oldestOrderSeq, contain, pullMode, limit, aroundMsgOrderSeq, new IGetOrSyncHistoryMsgBack() {
            @Override
            public void onSyncing() {

                if (isShowPinnedView && !isRefreshLoading && !isMoreLoading && !isSyncLastMsg) {
                    EndpointManager.getInstance().invoke("is_syncing_message", 1);
                } else {
                    if (MSReader.isEmpty(chatAdapter.getData())) {
                        MSMsg msMsg = new MSMsg();
                        msMsg.type = MSContentType.loading;
                        chatAdapter.addData(new MSUIChatMsgItemEntity(ChatActivity.this, msMsg, null));
                    }
                }
            }

            @Override
            public void onResult(List<MSMsg> list) {
                Log.e("实际多少条", list.size() + "");
                if (isShowPinnedView) {
                    EndpointManager.getInstance().invoke("is_syncing_message", 0);
                }
                if (pullMode == 0) {
                    if (MSReader.isEmpty(list))
                        isCanRefresh = false;
                } else {
                    if (MSReader.isEmpty(list)) {
                        isCanLoadMore = false;
                    }
                }
                isSyncLastMsg = false;
                List<MSMsg> tempList = new ArrayList<>();
                for (MSMsg msg : list) {
                    if (isSetNewData || !chatAdapter.isExist(msg.clientMsgNO, msg.messageID)){
                        tempList.add(msg);
                    }
                }
                showData(tempList, pullMode, isSetNewData, isScrollToEnd);
                msVBinding.chatUnreadLayout.progress.setVisibility(View.GONE);
                msVBinding.chatUnreadLayout.msgDownIv.setVisibility(View.VISIBLE);

                if (MSReader.isNotEmpty(chatAdapter.getData())) {
                    for (int i = 0, size = chatAdapter.getData().size(); i < size; i++) {
                        if (chatAdapter.getData().get(i).msMsg != null && chatAdapter.getData().get(i).msMsg.type == MSContentType.loading) {
                            chatAdapter.removeAt(i);
                            break;
                        }
                    }
                }
                isRefreshLoading = false;
                isMoreLoading = false;

            }
        });


    }

    /**
     * 显示数据
     *
     * @param msgList       数据源
     * @param pullMode      拉取模式 0:向下拉取 1:向上拉取
     * @param isSetNewData  是否重新显示新数据
     * @param isScrollToEnd 是否滚动到底部
     */
    private void showData(List<MSMsg> msgList, int pullMode, boolean isSetNewData, boolean isScrollToEnd) {
        boolean isAddEmptyView = MSReader.isNotEmpty(msgList) && msgList.size() < limit;
        if (isAddEmptyView) {
            MSMsg msg = new MSMsg();
            msg.timestamp = 0;
            msg.type = MSContentType.emptyView;
            msgList.add(0, msg);
        }

        if ((isShowCallingView || isShowPinnedView) && pullMode == 0) {
            if (MSReader.isNotEmpty(chatAdapter.getData())) {
                for (int i = 0; i < chatAdapter.getData().size(); i++) {
                    if (chatAdapter.getData().get(i).msMsg != null && chatAdapter.getData().get(i).msMsg.type == MSContentType.spanEmptyView) {
                        chatAdapter.removeAt(i);
                        break;
                    }
                }
            }
            msgList.add(0, getSpanEmptyMsg());
        }
        List<MSUIChatMsgItemEntity> list = new ArrayList<>();
        if (MSReader.isNotEmpty(msgList)) {
            long pre_msg_time = chatAdapter.getLastTimeMsg();
            for (int i = 0, size = msgList.size(); i < size; i++) {
                if (!MSTimeUtils.getInstance().isSameDay(msgList.get(i).timestamp, pre_msg_time) && msgList.get(i).type != MSContentType.emptyView && msgList.get(i).type != MSContentType.spanEmptyView) {
                    //显示聊天时间
                    MSUIChatMsgItemEntity uiChatMsgEntity = new MSUIChatMsgItemEntity(this, new MSMsg(), null);
                    uiChatMsgEntity.msMsg.type = MSContentType.msgPromptTime;
                    uiChatMsgEntity.msMsg.content = MSTimeUtils.getInstance().getShowDate(msgList.get(i).timestamp * 1000);
                    uiChatMsgEntity.msMsg.timestamp = msgList.get(i).timestamp;
                    list.add(uiChatMsgEntity);
                }
                pre_msg_time = msgList.get(i).timestamp;
                MSUIChatMsgItemEntity uiMsg = MSIMUtils.getInstance().msg2UiMsg(this, msgList.get(i), count, showNickName, chatAdapter.isShowChooseItem());
                if (msgList.get(i).remoteExtra != null) {
                    if (hideChannelAllPinnedMessage == 1) {
                        uiMsg.isPinned = 0;
                    } else {
                        uiMsg.isPinned = msgList.get(i).remoteExtra.isPinned;
                    }
                }
                list.add(uiMsg);
            }
        }

        if (isSetNewData) {
            if (unreadStartMsgOrderSeq != 0) {
                for (int i = 0, size = list.size(); i < size; i++) {
                    if (list.get(i).msMsg != null && list.get(i).msMsg.orderSeq == unreadStartMsgOrderSeq) {
                        //插入一条本地的新消息分割线
                        MSUIChatMsgItemEntity uiChatMsgItemEntity = new MSUIChatMsgItemEntity(this, new MSMsg(), null);
                        uiChatMsgItemEntity.msMsg.type = MSContentType.msgPromptNewMsg;
                        int index = i;
                        if (index <= 0) index = 0;
                        if (index > list.size() - 1) index = list.size() - 1;
                        list.add(index, uiChatMsgItemEntity);
                        if (index >= 1) {
                            linearLayoutManager.scrollToPositionWithOffset(index, 50);
                        } else msVBinding.recyclerView.scrollToPosition(index);
                        unreadStartMsgOrderSeq = 0;
                        break;
                    }
                }
            }
            chatAdapter.resetData(list);
            chatAdapter.setNewInstance(list);
        } else {
            chatAdapter.resetData(list);
            if (pullMode == 1) {
                if (MSReader.isNotEmpty(chatAdapter.getData()) && MSReader.isNotEmpty(list))
                    list.get(0).previousMsg = chatAdapter.getData().get(chatAdapter.getData().size() - 1).msMsg;
                chatAdapter.addData(list);
            } else {
                if (MSReader.isNotEmpty(list) && MSReader.isNotEmpty(chatAdapter.getData())) {
                    list.get(list.size() - 1).nextMsg = chatAdapter.getData().get(0).msMsg;
                }
                chatAdapter.addData(0, list);
            }
        }
        if (tipsOrderSeq != 0 || lastPreviewMsgOrderSeq != 0) {
            msVBinding.recyclerView.setVisibility(View.VISIBLE);
            if (tipsOrderSeq != 0) {
                for (int i = 0; i < chatAdapter.getData().size(); i++) {
                    if (chatAdapter.getItem(i).msMsg.orderSeq == tipsOrderSeq) {
                        linearLayoutManager.scrollToPositionWithOffset(i, AndroidUtilities.dp(50));
                        chatAdapter.getItem(i).isShowTips = true;
                        chatAdapter.notifyItemChanged(i);
                        tipsOrderSeq = 0;
                        break;
                    }
                }
            }
            if (lastPreviewMsgOrderSeq != 0) {
                for (int i = 0; i < chatAdapter.getData().size(); i++) {
                    if (chatAdapter.getItem(i).msMsg.orderSeq == lastPreviewMsgOrderSeq) {
                        linearLayoutManager.scrollToPositionWithOffset(i, keepOffsetY);
                        break;
                    }
                }
            }
        } else {
            if (isScrollToEnd)
                msVBinding.recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            else msVBinding.recyclerView.setVisibility(View.VISIBLE);
        }
        if (isCanLoadMore && MSReader.isNotEmpty(chatAdapter.getData()) && chatAdapter.getData().get(chatAdapter.getData().size() - 1).msMsg != null) {
            int maxSeq = MSIM.getInstance().getMsgManager().getMaxMessageSeqWithChannel(channelId, channelType);
            if (chatAdapter.getData().get(chatAdapter.getData().size() - 1).msMsg.messageSeq == maxSeq) {
                isCanLoadMore = false;
            }
        }

        new Handler().postDelayed(() -> {
            if (isUpdateRedDot) {
                MsgModel.getInstance().clearUnread(channelId, channelType, redDot, (code, msg) -> {
                    if (code == HttpResponseCode.success && redDot == 0) {
                        isUpdateRedDot = false;
                    }
                });
            }
        }, 500);
    }


    private void hideOrShowRightView(boolean isShow) {
        if (((channelId.equals(MSSystemAccount.system_file_helper) || channelId.equals(MSSystemAccount.system_team)) && channelType == MSChannelType.PERSONAL) || channelType == MSChannelType.CUSTOMER_SERVICE) {
            isShow = false;
        }
        MSChannel channel = getChatChannelInfo();
        if (channelType == MSChannelType.PERSONAL && (channel.isDeleted == 1 || UserUtils.getInstance().checkFriendRelation(channelId))) {
            isShow = false;
        }
        CommonAnim.getInstance().showOrHide(callIV, isShow, true);
    }

    private void resetReminder(List<MSReminder> list) {
        if (MSReader.isEmpty(list)) {
            return;
        }
        List<MSUIChatMsgItemEntity> msgList = chatAdapter.getData();
        List<Long> ids = new ArrayList<>();
        for (int i = 0, size = msgList.size(); i < size; i++) {
            for (MSReminder reminder : list) {
                if (msgList.get(i).msMsg != null && !TextUtils.isEmpty(msgList.get(i).msMsg.messageID) && msgList.get(i).msMsg.messageID.equals(reminder.messageID)) {
                    if (msgList.get(i).msMsg.viewed == 1 && reminder.done == 0) {
                        ids.add(reminder.reminderID);
                    }
                }
            }
        }

        // 先完成提醒项
        MsgModel.getInstance().doneReminder(ids);

        for (MSReminder reminder : list) {
            boolean isPublisher = !TextUtils.isEmpty(reminder.publisher) && reminder.publisher.equals(loginUID);
            if (!reminder.channelID.equals(channelId) || isPublisher) continue;
            if (reminder.done == 0) {
                boolean isAdd = true;
                for (int i = 0, size = reminderList.size(); i < size; i++) {
                    if (reminder.reminderID == reminderList.get(i).reminderID && reminder.type == reminderList.get(i).type) {
                        isAdd = false;
                        reminderList.get(i).done = 0;
                        break;
                    }
                }
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i) == reminder.reminderID) {
                        isAdd = false;
                        break;
                    }
                }
                if (isAdd && reminder.type == MSMentionType.MSReminderTypeMentionMe)
                    reminderList.add(reminder);
                boolean isAddApprove = true;
                for (int i = 0, size = groupApproveList.size(); i < size; i++) {
                    if (reminder.reminderID == groupApproveList.get(i).reminderID && reminder.type == groupApproveList.get(i).type) {
                        isAddApprove = false;
                        groupApproveList.get(i).done = 0;
                        break;
                    }
                }
                if (isAddApprove && reminder.type == MSMentionType.MSApplyJoinGroupApprove)
                    groupApproveList.add(reminder);
            } else {
                if (MSReader.isNotEmpty(reminderList)) {
                    for (int i = 0, size = reminderList.size(); i < size; i++) {
                        if (reminder.messageID.equals(reminderList.get(i).messageID)) {
//                            reminderList.get(i).done = 1;
                            reminderList.remove(i);
                            break;
                        }
                    }
                }
                if (MSReader.isNotEmpty(groupApproveList)) {
                    for (int i = 0, size = groupApproveList.size(); i < size; i++) {
                        if (reminder.messageID.equals(groupApproveList.get(i).messageID)) {
//                            groupApproveList.get(i).done = 1;
                            groupApproveList.remove(i);
                            break;
                        }
                    }
                }
            }
        }
        resetRemindView();
        resetGroupApproveView();

//        if (MSReader.isNotEmpty(list)) {
//            List<MSUIChatMsgItemEntity> msgList = chatAdapter.getData();
//            List<Long> ids = new ArrayList<>();
//            for (int i = 0, size = list.size(); i < size; i++) {
//                if (list.get(i).done == 1) continue;
//                for (int j = 0, len = msgList.size(); j < len; j++) {
//                    if (msgList.get(j).msMsg != null && !TextUtils.isEmpty(msgList.get(j).msMsg.messageID) && msgList.get(j).msMsg.messageID.equals(list.get(i).messageID)) {
//                        if (msgList.get(j).msMsg.viewed == 1) {
//                            ids.add(list.get(i).reminderID);
//                            list.remove(i);
//                            i--;
//                            size--;
//                            break;
//                        }
//                    }
//                }
//            }
//            MsgModel.getInstance().doneReminder(ids);
//            if (MSReader.isEmpty(list)) {
//                return;
//            }
//            for (MSReminder reminder : list) {
//                boolean isPublisher = !TextUtils.isEmpty(reminder.publisher) && reminder.publisher.equals(loginUID);
//                if (!reminder.channelID.equals(channelId) || isPublisher) continue;
//                if (reminder.done == 0) {
//                    boolean isAdd = true;
//                    for (int i = 0, size = reminderList.size(); i < size; i++) {
//                        if (reminder.reminderID == reminderList.get(i).reminderID && reminder.type == reminderList.get(i).type) {
//                            isAdd = false;
//                            reminderList.get(i).done = 0;
//                            break;
//                        }
//                    }
//                    if (isAdd && reminder.type == MSMentionType.MSReminderTypeMentionMe)
//                        reminderList.add(reminder);
//                    boolean isAddApprove = true;
//                    for (int i = 0, size = groupApproveList.size(); i < size; i++) {
//                        if (reminder.reminderID == groupApproveList.get(i).reminderID && reminder.type == groupApproveList.get(i).type) {
//                            isAddApprove = false;
//                            groupApproveList.get(i).done = 0;
//                            break;
//                        }
//                    }
//                    if (isAddApprove && reminder.type == MSMentionType.MSApplyJoinGroupApprove)
//                        groupApproveList.add(reminder);
//                }
//            }
//            resetRemindView();
//            resetGroupApproveView();
//        }
    }

    private void resetRemindView() {
        msVBinding.chatUnreadLayout.remindCountTv.setCount(reminderList.size(), true);
        msVBinding.chatUnreadLayout.remindCountTv.setVisibility(MSReader.isNotEmpty(reminderList) ? View.VISIBLE : View.GONE);
        msVBinding.chatUnreadLayout.remindLayout.post(() -> CommonAnim.getInstance().showOrHide(msVBinding.chatUnreadLayout.remindLayout, MSReader.isNotEmpty(reminderList), MSReader.isNotEmpty(reminderList), false));
    }

    private void resetGroupApproveView() {
        msVBinding.chatUnreadLayout.approveCountTv.setCount(groupApproveList.size(), true);
        msVBinding.chatUnreadLayout.approveCountTv.setVisibility(MSReader.isNotEmpty(groupApproveList) ? View.VISIBLE : View.GONE);
        msVBinding.chatUnreadLayout.groupApproveLayout.post(() -> CommonAnim.getInstance().showOrHide(msVBinding.chatUnreadLayout.groupApproveLayout, MSReader.isNotEmpty(groupApproveList), MSReader.isNotEmpty(reminderList), false));
    }

    private void showUnReadCountView() {
        msVBinding.chatUnreadLayout.msgCountTv.setCount(redDot, false);
        msVBinding.chatUnreadLayout.msgCountTv.setVisibility(redDot > 0 ? View.VISIBLE : View.GONE);
        msVBinding.chatUnreadLayout.newMsgLayout.post(() -> CommonAnim.getInstance().showOrHide(msVBinding.chatUnreadLayout.newMsgLayout, redDot > 0, redDot > 0, false));
    }

    private void showChannelName(MSChannel channel) {
        if (channelId.equals(MSSystemAccount.system_team)) {
            msVBinding.topLayout.titleCenterTv.setText(R.string.ms_system_notice);
        } else if (channelId.equals(MSSystemAccount.system_file_helper)) {
            msVBinding.topLayout.titleCenterTv.setText(R.string.ms_file_helper);
        } else {
            String showName = TextUtils.isEmpty(channel.channelRemark) ? channel.channelName : channel.channelRemark;
            msVBinding.topLayout.titleCenterTv.setText(showName);
        }
    }

    private void removeMsg(MSMsg msg) {
        EndpointManager.getInstance().invoke("stop_reaction_animation", null);
        int tempIndex = 0;
        for (int i = 0, size = chatAdapter.getData().size(); i < size; i++) {
            if (chatAdapter.getData().get(i).msMsg != null && (chatAdapter.getData().get(i).msMsg.clientSeq == msg.clientSeq || chatAdapter.getData().get(i).msMsg.clientMsgNO.equals(msg.clientMsgNO))) {
                tempIndex = i;
                if (i - 1 >= 0) {
                    if (i + 1 <= chatAdapter.getData().size() - 1) {
                        chatAdapter.getData().get(i - 1).nextMsg = chatAdapter.getData().get(i + 1).msMsg;
                    } else {
                        chatAdapter.getData().get(i - 1).nextMsg = null;
                    }
                }
                if (i + 1 <= chatAdapter.getData().size() - 1) {
                    if (i - 1 >= 0) {
                        chatAdapter.getData().get(i + 1).previousMsg = chatAdapter.getData().get(i - 1).msMsg;
                    } else chatAdapter.getData().get(i + 1).previousMsg = null;
                }
                chatAdapter.removeAt(i);
                break;
            }
        }

        int timeIndex = tempIndex - 1;
        if (timeIndex < 0) return;
        //如果是时间也删除
        if (chatAdapter.getData().size() >= timeIndex) {
            if (chatAdapter.getData().get(timeIndex).msMsg.type == MSContentType.msgPromptTime) {

                if (timeIndex - 1 >= 0) {
                    if (timeIndex + 1 <= chatAdapter.getData().size() - 1) {
                        chatAdapter.getData().get(timeIndex - 1).nextMsg = chatAdapter.getData().get(timeIndex + 1).msMsg;
                    } else {
                        chatAdapter.getData().get(timeIndex - 1).nextMsg = null;
                    }
                }
                if (timeIndex + 1 <= chatAdapter.getData().size() - 1) {
                    if (timeIndex - 1 >= 0) {
                        chatAdapter.getData().get(timeIndex + 1).previousMsg = chatAdapter.getData().get(timeIndex - 1).msMsg;
                    } else chatAdapter.getData().get(timeIndex + 1).previousMsg = null;
                }
                chatAdapter.removeAt(timeIndex);
            }
        }
    }

    private void showToast(int textId) {
        MSToastUtils.getInstance().showToast(getString(textId));
    }

    private synchronized void setShowTime() {
        String showTime = "";
        int index = linearLayoutManager.findFirstVisibleItemPosition();
        if (index > 0 && index < chatAdapter.getData().size()) {
            MSUIChatMsgItemEntity MSUIChatMsgItemEntity = chatAdapter.getData().get(index);
            if (MSUIChatMsgItemEntity.msMsg != null && MSUIChatMsgItemEntity.msMsg.timestamp > 0) {
                showTime = MSTimeUtils.getInstance().getShowDate(MSUIChatMsgItemEntity.msMsg.timestamp * 1000);
            }
        }
        if (!TextUtils.isEmpty(showTime)) {
            SpannableString str = new SpannableString(showTime);
            str.setSpan(new SystemMsgBackgroundColorSpan(ContextCompat.getColor(this, R.color.colorSystemBg), AndroidUtilities.dp(5), AndroidUtilities.dp(2 * 5)), 0, showTime.length(), 0);
            msVBinding.timeTv.setText(str);
            CommonAnim.getInstance().showOrHide(msVBinding.timeTv, true, true);
        } else {
            CommonAnim.getInstance().showOrHide(msVBinding.timeTv, false, false);
        }
    }

    private boolean isRefreshReaction(List<MSMsgReaction> oldList, List<MSMsgReaction> newList) {
        if (MSReader.isEmpty(oldList) && MSReader.isEmpty(newList)) return false;
        if ((MSReader.isEmpty(oldList) && MSReader.isNotEmpty(newList)) || (MSReader.isEmpty(newList) && MSReader.isNotEmpty(oldList)) || (oldList.size() != newList.size())) {
            return true;
        }
        boolean isRefresh = false;
        for (MSMsgReaction reaction : newList) {
            boolean refresh = true;
            for (MSMsgReaction reaction1 : oldList) {
                if (reaction1.messageID.equals(reaction.messageID) && reaction1.emoji.equals(reaction.emoji) && reaction1.isDeleted == reaction.isDeleted) {
                    refresh = false;
                    break;
                }
            }
            if (refresh) {
                isRefresh = true;
                break;
            }
        }
        return isRefresh;
    }

    private void scrollToPosition(int index) {
        linearLayoutManager.scrollToPosition(index);
    }


    private void showRefreshLoading() {
        if (isRefreshLoading || !isCanRefresh) return;
        isRefreshLoading = true;
        MSMsg msMsg = new MSMsg();
        msMsg.type = MSContentType.loading;
        int index = 0;
        if (isShowPinnedView || isShowCallingView) {
            for (int i = 0, size = chatAdapter.getData().size(); i < size; i++) {
                if (chatAdapter.getData().get(i).msMsg != null && chatAdapter.getData().get(i).msMsg.type == MSContentType.spanEmptyView) {
                    index = i + 1;
                    break;
                }
            }
        }
        chatAdapter.addData(index, new MSUIChatMsgItemEntity(this, msMsg, null));
        msVBinding.recyclerView.scrollToPosition(0);
        lastPreviewMsgOrderSeq = 0;
        new Handler().postDelayed(() -> getData(0, false, 0, false), 300);
    }

    private void showMoreLoading() {
        if (isMoreLoading || !isCanLoadMore) return;
        isMoreLoading = true;
        MSMsg msMsg = new MSMsg();
        msMsg.type = MSContentType.loading;
        chatAdapter.addData(new MSUIChatMsgItemEntity(this, msMsg, null));
        msVBinding.recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
        lastPreviewMsgOrderSeq = 0;
        unreadStartMsgOrderSeq = 0;
        new Handler().postDelayed(() -> getData(1, false, 0, false), 300);
    }

    private List<PopupMenuItem> getGroupApprovePopupItems() {
        PopupMenuItem item = new PopupMenuItem(getString(R.string.clear_all_remind), R.mipmap.msg_seen, () -> {
            List<MSReminder> list = MSIM.getInstance().getReminderManager().getRemindersWithType(channelId, channelType, MSMentionType.MSApplyJoinGroupApprove);
            List<Long> ids = new ArrayList<>();
            for (MSReminder reminder : list) {
                if (reminder.done == 0) {
                    ids.add(reminder.reminderID);
                }
            }
            groupApproveList.clear();
            resetGroupApproveView();
            MsgModel.getInstance().doneReminder(ids);
        });

        List<PopupMenuItem> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    private List<PopupMenuItem> getRemindPopupItems() {
        PopupMenuItem item = new PopupMenuItem(getString(R.string.clear_all_remind), R.mipmap.msg_seen, () -> {
            List<MSReminder> list = MSIM.getInstance().getReminderManager().getRemindersWithType(channelId, channelType, MSMentionType.MSReminderTypeMentionMe);
            List<Long> ids = new ArrayList<>();
            for (MSReminder reminder : list) {
                if (reminder.done == 0) {
                    ids.add(reminder.reminderID);
                }
            }
            reminderList.clear();
            resetRemindView();
            MsgModel.getInstance().doneReminder(ids);
        });

        List<PopupMenuItem> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    private void checkLoginUserInGroupStatus() {
        if (channelType == MSChannelType.GROUP) {
            MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, loginUID);
            hideOrShowRightView(member == null || member.isDeleted == 0);
        }
    }

    private void scrollToEnd() {
        linearLayoutManager.scrollToPosition(chatAdapter.getItemCount() - 1);
    }

    // 显示一条时间消息
    private synchronized MSMsg addTimeMsg(long newMsgTime) {
        long lastMsgTime = chatAdapter.getLastTimeMsg();
        MSMsg msg = null;
        if (!MSTimeUtils.getInstance().isSameDay(newMsgTime, lastMsgTime)) {
            int lastIndex = chatAdapter.getData().size() - 1;
            MSUIChatMsgItemEntity uiChatMsgEntity = new MSUIChatMsgItemEntity(this, null, null);
            msg = new MSMsg();
            uiChatMsgEntity.msMsg = msg;
            uiChatMsgEntity.isChoose = (chatAdapter.getItemCount() > 0 && chatAdapter.getData().get(0).isChoose);
            uiChatMsgEntity.msMsg.type = MSContentType.msgPromptTime;
            uiChatMsgEntity.msMsg.content = MSTimeUtils.getInstance().getShowDate(newMsgTime * 1000);
            uiChatMsgEntity.msMsg.timestamp = MSTimeUtils.getInstance().getCurrentSeconds();
            chatAdapter.addData(uiChatMsgEntity);
            if (lastIndex >= 0) {
                chatAdapter.notifyBackground(lastIndex);
            }
        }
        return msg;
    }

    private boolean setBackListener() {
        if (!isViewingPicture) {

            if (numberTextView.getVisibility() == View.VISIBLE) {
                for (int i = 0, size = chatAdapter.getItemCount(); i < size; i++) {
                    chatAdapter.getItem(i).isChoose = false;
                    chatAdapter.getItem(i).isChecked = false;
                    chatAdapter.notifyItemChanged(i, chatAdapter.getItem(i));
                }
                chatPanelManager.hideMultipleChoice();
                CommonAnim.getInstance().rotateImage(msVBinding.topLayout.backIv, 180f, 360f, R.mipmap.ic_ab_back);
                numberTextView.setNumber(0, true);
                hideOrShowRightView(true);
                EndpointManager.getInstance().invoke("chat_page_reset", getChatChannelInfo());
                CommonAnim.getInstance().showOrHide(numberTextView, false, true);
            } else {
                if (chatPanelManager.isCanBack()) {
                    new Handler(Objects.requireNonNull(Looper.myLooper())).postDelayed(this::finish, 150);
                }
            }
        }
        return false;
    }


    // 定时上报已读消息
    private void startTimer() {
        Observable.interval(0, 3, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<>() {
            @Override
            public void onComplete() {
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
            }

            @Override
            public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                disposable = d;
            }

            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull Long value) {
                if (MSReader.isEmpty(readMsgIds) || !isUploadReadMsg) {
                    return;
                }
                List<String> msgIds = new ArrayList<>(readMsgIds);
                EndpointManager.getInstance().invoke("read_msg", new ReadMsgMenu(channelId, channelType, msgIds));
                readMsgIds.clear();
            }
        });
    }

    private void resetHideChannelAllPinnedMessage() {
        String key = String.format("hide_pin_msg_%s_%s", channelId, channelType);
        hideChannelAllPinnedMessage = MSSharedPreferencesUtil.getInstance().getIntWithUID(key);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN)
            EndpointManager.getInstance().invoke("chat_activity_touch", null);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float density = getResources().getDisplayMetrics().density;
        AndroidUtilities.setDensity(density);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // 横屏
            AndroidUtilities.isPORTRAIT = false;
            chatAdapter.notifyItemRangeChanged(0, chatAdapter.getItemCount());
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 竖屏
            AndroidUtilities.isPORTRAIT = true;
            chatAdapter.notifyItemRangeChanged(0, chatAdapter.getItemCount());
        }
    }

    @Override
    public void sendMessage(MSMessageContent messageContent) {

        if (messageContent.type == MSContentType.MS_TEXT && editMsg != null) {
            JSONObject jsonObject = messageContent.encodeMsg();
            if (jsonObject == null) jsonObject = new JSONObject();
            try {
                jsonObject.put("type", messageContent.type);
            } catch (JSONException e) {
                Log.e("消息类型错误", "-->");
            }
            boolean isUpdate = isUpdate(messageContent);
            if (isUpdate) {
                MSIM.getInstance().getMsgManager().updateMsgEdit(editMsg.messageID, channelId, channelType, jsonObject.toString());
            }
            deleteOperationMsg();
            return;
        }
        if (messageContent.type == MSContentType.MS_TEXT && replyMSMsg != null) {
            MSReply msReply = new MSReply();
            if (replyMSMsg.remoteExtra != null && replyMSMsg.remoteExtra.contentEditMsgModel != null) {
                msReply.payload = replyMSMsg.remoteExtra.contentEditMsgModel;
            } else {
                msReply.payload = replyMSMsg.baseContentMsgModel;
            }
            String showName = "";
            if (replyMSMsg.getFrom() != null) {
                showName = replyMSMsg.getFrom().channelName;
            } else {
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(replyMSMsg.fromUID, MSChannelType.PERSONAL);
                if (channel != null) showName = channel.channelName;
            }
            msReply.from_name = showName;
            msReply.from_uid = replyMSMsg.fromUID;
            msReply.message_id = replyMSMsg.messageID;
            msReply.message_seq = replyMSMsg.messageSeq;
            if (replyMSMsg.baseContentMsgModel.reply != null && !TextUtils.isEmpty(replyMSMsg.baseContentMsgModel.reply.root_mid)) {
                msReply.root_mid = replyMSMsg.baseContentMsgModel.reply.root_mid;
            } else {
                msReply.root_mid = msReply.message_id;
            }
            messageContent.reply = msReply;
        }
        sendMsg(messageContent);
        replyMSMsg = null;

    }

    private void sendMsg(MSMessageContent messageContent) {
        if (redDot > 0) {
            msVBinding.chatUnreadLayout.newMsgLayout.performClick();
        }
        MSMsg msMsg = new MSMsg();
        msMsg.channelID = channelId;
        msMsg.channelType = channelType;
        msMsg.type = messageContent.type;
        msMsg.baseContentMsgModel = messageContent;
        MSChannel channel = getChatChannelInfo();
        msMsg.setChannelInfo(channel);
        MSSendMsgUtils.getInstance().sendMessage(msMsg);
    }

    private boolean isUpdate(MSMessageContent messageContent) {
        boolean isUpdate = false;
        if (editMsg.remoteExtra != null && editMsg.remoteExtra.contentEditMsgModel != null) {
            if (!editMsg.remoteExtra.contentEditMsgModel.getDisplayContent().equals(messageContent.getDisplayContent())) {
                isUpdate = true;
            }
        }
        if (!editMsg.baseContentMsgModel.getDisplayContent().equals(messageContent.getDisplayContent())) {
            isUpdate = true;
        }
        return isUpdate;
    }

    private void setOnlineView(MSChannel channel) {
        if (channel.online == 1) {
            String device = getString(R.string.phone);
            if (channel.deviceFlag == UserOnlineStatus.Web) device = getString(R.string.web);
            else if (channel.deviceFlag == UserOnlineStatus.PC) device = getString(R.string.pc);
            String content = String.format("%s%s", device, getString(R.string.online));
            msVBinding.topLayout.subtitleTv.setText(content);
            msVBinding.topLayout.subtitleView.setVisibility(View.VISIBLE);
        } else {
            if (channel.lastOffline > 0) {
                String showTime = MSTimeUtils.getInstance().getOnlineTime(channel.lastOffline);
                if (TextUtils.isEmpty(showTime)) {
                    msVBinding.topLayout.subtitleView.setVisibility(View.VISIBLE);
                    String time = MSTimeUtils.getInstance().getShowDateAndMinute(channel.lastOffline * 1000L);
                    String content = String.format("%s%s", getString(R.string.last_seen_time), time);
                    msVBinding.topLayout.subtitleTv.setText(content);
                } else {
                    msVBinding.topLayout.subtitleTv.setText(showTime);
                    msVBinding.topLayout.subtitleView.setVisibility(View.VISIBLE);
                }
            } else msVBinding.topLayout.subtitleView.setVisibility(View.GONE);
        }
    }

    @Override
    public MSChannel getChatChannelInfo() {
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelId, channelType);
        if (channel == null) {
            channel = new MSChannel(channelId, channelType);
        }
        return channel;
    }

    @Override
    public void showMultipleChoice() {
        chatPanelManager.showMultipleChoice();
        CommonAnim.getInstance().rotateImage(msVBinding.topLayout.backIv, 180f, 360f, R.mipmap.ic_close_white);
        CommonAnim.getInstance().showOrHide(numberTextView, true, true);
        CommonAnim.getInstance().showOrHide(callIV, false, false);
        EndpointManager.getInstance().invoke("hide_pinned_view", null);
    }

    @Override
    public void setTitleRightText(String text) {
        int num = Integer.parseInt(text);
        chatPanelManager.updateForwardView(num);
        numberTextView.setNumber(num, true);
        CommonAnim.getInstance().showOrHide(numberTextView, true, true);
        CommonAnim.getInstance().showOrHide(callIV, false, false);
    }

    @Override
    public void showReply(MSMsg msMsg) {
        this.editMsg = null;
        boolean showDialog = false;
        MSChannelMember mChannelMember = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, loginUID);
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelId, channelType);
        if (channel != null && mChannelMember != null) {
            if ((channel.forbidden == 1 && mChannelMember.role == MSChannelMemberRole.normal) || mChannelMember.forbiddenExpirationTime > 0) {
                //普通成员
                showDialog = true;
            }
        }

        if (showDialog) {
            MSDialogUtils.getInstance().showSingleBtnDialog(this, "", getString(R.string.cannot_reply_msg), "", null);
            return;
        }

        if (channelType == MSChannelType.GROUP && !msMsg.fromUID.equals(loginUID)) {
            MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, msMsg.fromUID);
            if (member != null) {
                chatPanelManager.addSpan(member.memberName, member.memberUID);
            } else {
                MSChannel mChannel = MSIM.getInstance().getChannelManager().getChannel(msMsg.fromUID, MSChannelType.PERSONAL);
                if (mChannel != null) {
                    chatPanelManager.addSpan(mChannel.channelName, mChannel.channelID);
                }
            }
//            MSVBinding.toolbarView.editText.addAtSpan("@", member.memberName, member.memberUID);
        }
        this.replyMSMsg = msMsg;
        if (replyMSMsg != null) {
            chatPanelManager.showReplyLayout(replyMSMsg);
        }

    }

    @Override
    public void showEdit(MSMsg msMsg) {
        boolean showDialog = false;
        MSChannelMember mChannelMember = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, loginUID);
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelId, channelType);
        if (channel != null && mChannelMember != null) {
            if ((channel.forbidden == 1 && mChannelMember.role == MSChannelMemberRole.normal) || mChannelMember.forbiddenExpirationTime > 0) {
                //普通成员
                showDialog = true;
            }
        }

        if (showDialog) {
            MSDialogUtils.getInstance().showSingleBtnDialog(this, "", getString(R.string.cannot_edit_msg), "", null);
            return;
        }
        this.replyMSMsg = null;
        if (msMsg != null) {
            this.editMsg = msMsg;
            chatPanelManager.showEditLayout(msMsg);
        }

    }

    @Override
    public void tipsMsg(String clientMsgNo) {

        isTipMessage = true;
        int index = -1;
        for (int i = 0, size = chatAdapter.getData().size(); i < size; i++) {
            if (chatAdapter.getData().get(i).msMsg != null && chatAdapter.getData().get(i).msMsg.clientMsgNO.equals(clientMsgNo)) {
                chatAdapter.getData().get(i).isShowTips = true;
                index = i;
                break;
            }
        }
        if (index != -1) {
            int lastItemPosition = linearLayoutManager.findLastVisibleItemPosition();
            int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
            if (index < firstItemPosition || index > lastItemPosition) {
                linearLayoutManager.scrollToPositionWithOffset(index, AndroidUtilities.dp(70));
            }
            chatAdapter.notifyItemChanged(index);
        } else {
            MSMsg msg = MSIM.getInstance().getMsgManager().getWithClientMsgNO(clientMsgNo);
            if (msg != null && msg.isDeleted == 0) {
                unreadStartMsgOrderSeq = 0;
                tipsOrderSeq = msg.orderSeq;
                // keepMessageSeq = msg.orderSeq;
                getData(0, true, msg.orderSeq, true);
                isCanLoadMore = true;
            } else {
                showToast(R.string.cannot_tips_msg);
            }
        }

    }

    @Override
    public void setEditContent(String content) {

        int curPosition = chatPanelManager.getEditText().getSelectionStart();
        StringBuilder sb = new StringBuilder(Objects.requireNonNull(chatPanelManager.getEditText().getText()).toString());
        sb.insert(curPosition, content);
        chatPanelManager.getEditText().setText(MoonUtil.getEmotionContent(this, chatPanelManager.getEditText(), sb.toString()));
        // 将光标设置到新增完表情的右侧
        chatPanelManager.getEditText().setSelection(curPosition + content.length());

    }

    @Override
    public AppCompatActivity getChatActivity() {
        return this;
    }

    @Override
    public MSMsg getReplyMsg() {
        return replyMSMsg;
    }

    @Override
    public void hideSoftKeyboard() {
        mHelper.hookSystemBackByPanelSwitcher();
    }

    @Override
    public ChatAdapter getChatAdapter() {
        return chatAdapter;
    }

    @Override
    public void sendCardMsg() {

        Intent intent = new Intent(this, ChooseContactsActivity.class);
        intent.putExtra("chooseBack", true);
        intent.putExtra("singleChoose", true);
        if (channelType == MSChannelType.PERSONAL) {
            intent.putExtra("unVisibleUIDs", channelId);
        }
        chooseCardResultLac.launch(intent);
    }

    @Override
    public void chatRecyclerViewScrollToEnd() {
        if (isToEnd) {
            scrollToEnd();
        }

    }

    @Override
    public void deleteOperationMsg() {

        this.replyMSMsg = null;
        this.editMsg = null;
    }

    @Override
    public void onChatAvatarClick(String uid, boolean isLongClick) {
        chatPanelManager.chatAvatarClick(uid, isLongClick);
    }

    @Override
    public void onViewPicture(boolean isViewing) {
        isViewingPicture = isViewing;
    }

    @Override
    public void onMsgViewed(MSMsg msMsg, int position) {
        if (msMsg == null) return;
        if (!TextUtils.isEmpty(msMsg.messageID) && !isTipMessage) {
            EndpointManager.getInstance().invoke("tip_pinned_message", msMsg.messageID);
        }
        if (msMsg.flame == 1 && msMsg.viewed == 0 && msMsg.type != MSContentType.MS_IMAGE && msMsg.type != MSContentType.MS_VIDEO && msMsg.type != MSContentType.MS_VOICE) {

            msMsg.viewed = 1;
            msMsg.viewedAt = MSTimeUtils.getInstance().getCurrentMills();
            chatAdapter.updateDeleteTimer(position);
            MSIM.getInstance().getMsgManager().updateViewedAt(1, msMsg.viewedAt, msMsg.clientMsgNO);
        }
        if (msMsg.viewed == 0 && msMsg.type == MSContentType.MS_TEXT) {
            msMsg.viewed = 1;
        }

        if (msMsg.remoteExtra.readed == 0 && msMsg.setting != null && msMsg.setting.receipt == 1 && !TextUtils.isEmpty(msMsg.fromUID) && !msMsg.fromUID.equals(loginUID)) {
            boolean isAdd = true;
            for (int j = 0, size = readMsgIds.size(); j < size; j++) {
                if (readMsgIds.get(j).equals(msMsg.messageID)) {
                    isAdd = false;
                    break;
                }
            }
            if (isAdd) {
                readMsgIds.add(msMsg.messageID);
            }
        }
        boolean isResetRemind = false;
        if (MSReader.isNotEmpty(reminderList) && !TextUtils.isEmpty(msMsg.messageID)) {
            for (int j = 0; j < reminderList.size(); j++) {
                if (reminderList.get(j).messageID.equals(msMsg.messageID)) {
                    if (reminderList.get(j).done == 0) {
                        reminderIds.add(reminderList.get(j).reminderID);
                    }
                    reminderList.remove(j);
                    j = j - 1;
                    isResetRemind = true;
                }
            }
        }

        boolean isResetGroupApprove = false;
        if (MSReader.isNotEmpty(groupApproveList) && !TextUtils.isEmpty(msMsg.messageID)) {
            for (int j = 0, size = groupApproveList.size(); j < size; j++) {
                if (groupApproveList.get(j).messageID.equals(msMsg.messageID) && groupApproveList.get(j).done == 0) {
                    reminderIds.add(groupApproveList.get(j).reminderID);
                    groupApproveList.remove(j);
                    isResetGroupApprove = true;
                    break;
                }
            }
        }

        // 保存最新浏览到的位置
        if (msMsg.messageSeq > browseTo) {
            browseTo = msMsg.messageSeq;
        }
        boolean isResetUnread = false;
        if (msMsg.messageSeq > lastVisibleMsgSeq) {
            lastVisibleMsgSeq = msMsg.messageSeq;
        }
        if (lastVisibleMsgSeq != 0) {
            long lastVisibleMsgOrderSeq = MSIM.getInstance().getMsgManager().getMessageOrderSeq(lastVisibleMsgSeq, channelId, channelType);
            if (lastVisibleMsgOrderSeq < unreadStartMsgOrderSeq) {
                lastVisibleMsgSeq = (int) MSIM.getInstance().getMsgManager().getReliableMessageSeq(unreadStartMsgOrderSeq);
                lastVisibleMsgSeq = lastVisibleMsgSeq - 1;
            }
        }
        if (redDot > 0) {
            if (lastVisibleMsgSeq != 0) {
                redDot = maxMsgSeq - lastVisibleMsgSeq;
            }
            if (redDot < 0) redDot = 0;
            isResetUnread = true;

        }

        if (isResetGroupApprove) {
            resetGroupApproveView();
        }
        if (isResetUnread) {
            showUnReadCountView();
        }
        if (isResetRemind) {
            resetRemindView();
        }
    }

    @Override
    public View getRecyclerViewLayout() {
        return msVBinding.recyclerViewLayout;
    }

    @Override
    public boolean isShowChatActivity() {
        return isShowChatActivity;
    }

    @Override
    public void closeActivity() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);
        EndpointManager.getInstance().remove(channelId);
        EndpointManager.getInstance().invoke("stop_screen_shot", this);
        MSIM.getInstance().getMsgManager().removeDeleteMsgListener(channelId);
        MSIM.getInstance().getMsgManager().removeNewMsgListener(channelId);
        MSIM.getInstance().getMsgManager().removeRefreshMsgListener(channelId);
        MSIM.getInstance().getMsgManager().removeSendMsgCallBack(channelId);
        MSIM.getInstance().getChannelManager().removeRefreshChannelInfo(channelId);
        MSIM.getInstance().getChannelMembersManager().removeRefreshChannelMemberInfo(channelId);
        MSIM.getInstance().getChannelMembersManager().removeAddChannelMemberListener(channelId);
        MSIM.getInstance().getChannelMembersManager().removeRemoveChannelMemberListener(channelId);
        MSIM.getInstance().getCMDManager().removeCmdListener(channelId);
        MSIM.getInstance().getMsgManager().removeSendMsgAckListener(channelId);
        MSIM.getInstance().getMsgManager().removeClearMsg(channelId);
        MSIM.getInstance().getRobotManager().removeRefreshRobotMenu(channelId);
        MSIM.getInstance().getReminderManager().removeNewReminderListener(channelId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatPanelManager.onDestroy();
        ActManagerUtils.getInstance().removeActivity(this);
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        if (MSReader.isNotEmpty(readMsgIds)) {
            EndpointManager.getInstance().invoke("read_msg", new ReadMsgMenu(channelId, channelType, readMsgIds));
        }
        MsgModel.getInstance().startCheckFlameMsgTimer();
        saveEditContent();

    }

    private void saveEditContent() {
        if (MSReader.isEmpty(chatAdapter.getData())) {
            return;
        }
        //停止语音播放
        //AudioPlaybackManager.getInstance().stopAudio();
        int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int endItemPosition = linearLayoutManager.findLastVisibleItemPosition();
        long keepMsgSeq = 0;
        int offsetY = 0;
        if (endItemPosition != chatAdapter.getData().size() - 1) {
            MSMsg msg = chatAdapter.getFirstVisibleItem(firstItemPosition);
            if (msg != null) {
                keepMsgSeq = msg.messageSeq;
                int index = chatAdapter.getFirstVisibleItemIndex(firstItemPosition);
                View view = linearLayoutManager.findViewByPosition(index);
                if (view != null) {
                    offsetY = view.getTop();
                }
            }
        }
//        int unreadCount = msVBinding.chatUnreadLayout.msgCountTv.getCount();
        MsgModel.getInstance().clearUnread(channelId, channelType, redDot, null);
        String content = Objects.requireNonNull(chatPanelManager.getEditText().getText()).toString();
        MsgModel.getInstance().updateCoverExtra(channelId, channelType, browseTo, keepMsgSeq, offsetY, content);
        MsgModel.getInstance().deleteFlameMsg();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return setBackListener();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isShowChatActivity = false;
        MSUIKitApplication.getInstance().chattingChannelID = "";
        isUploadReadMsg = false;
        MSPlayVoiceUtils.getInstance().stopPlay();
        MsgModel.getInstance().doneReminder(reminderIds);
        EndpointManager.getInstance().invoke("stop_screen_shot", this);
    }


    ActivityResultLauncher<Intent> previewNewImgResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            String path = result.getData().getStringExtra("path");
            if (!TextUtils.isEmpty(path)) {
                sendMsg(new MSImageContent(path));
            }
        }
    });
    ActivityResultLauncher<Intent> chooseCardResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            String uid = result.getData().getStringExtra("uid");
            if (!TextUtils.isEmpty(uid)) {
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(uid, MSChannelType.PERSONAL);
                MSCardContent MSCardContent = new MSCardContent();
                MSCardContent.name = channel.channelName;
                MSCardContent.uid = channel.channelID;
                if (channel.remoteExtraMap != null && channel.remoteExtraMap.containsKey(MSChannelExtras.vercode))
                    MSCardContent.vercode = (String) channel.remoteExtraMap.get(MSChannelExtras.vercode);
                List<MSMessageContent> messageContentList = new ArrayList<>();
                messageContentList.add(MSCardContent);
                List<MSChannel> list = new ArrayList<>();
                list.add(MSIM.getInstance().getChannelManager().getChannel(channelId, channelType));
                MSUIKitApplication.getInstance().showChatConfirmDialog(ChatActivity.this, list, messageContentList, (list1, messageContentList1) -> sendMsg(MSCardContent));
            }
        }
    });

    private synchronized void sendMsgInserted(MSMsg msg) {
        if (msg.channelType == channelType && msg.channelID.equals(channelId) && msg.isDeleted == 0 && !msg.header.noPersist) {
            if (msg.orderSeq > maxMsgOrderSeq) {
                maxMsgOrderSeq = msg.orderSeq;
            }
            MSMsg timeMsg = addTimeMsg(msg.timestamp);
            //判断当前会话是否存在正在输入
            int index = chatAdapter.getData().size() - 1;
            if (chatAdapter.lastMsgIsTyping()) index--;
            if (index < 0) index = 0;
            MSUIChatMsgItemEntity itemEntity = MSIMUtils.getInstance().msg2UiMsg(this, msg, count, showNickName, chatAdapter.isShowChooseItem());
            if (timeMsg == null) {
                if (MSReader.isNotEmpty(chatAdapter.getData())) {
                    chatAdapter.getData().get(index).nextMsg = msg;
                    itemEntity.previousMsg = chatAdapter.getData().get(index).msMsg;
                }
            } else {
                chatAdapter.getData().get(index).nextMsg = timeMsg;
                itemEntity.previousMsg = timeMsg;
            }
            chatAdapter.addData(index + 1, itemEntity);
            int type = chatAdapter.getData().get(index).msMsg.type;
            if (MSContentType.isLocalMsg(type) || MSContentType.isSystemMsg(type)) {
                chatAdapter.notifyItemChanged(index);
            } else {
                chatAdapter.notifyBackground(index);
            }

            if (isToEnd) {
                scrollToEnd();
            }
            isToEnd = true;
        }
    }

    private synchronized void receivedMessages(List<MSMsg> list) {
        if (MSReader.isNotEmpty(list)) {
            for (MSMsg msg : list) {
                // 命令消息和撤回消息不显示在聊天
                if (msg.type == MSContentType.MS_INSIDE_MSG || msg.type == MSContentType.withdrawSystemInfo || msg.isDeleted == 1 || msg.header.noPersist)
                    continue;

                if (msg.remoteExtra.readedCount == 0) {
                    msg.remoteExtra.unreadCount = count - 1;
                }
                if (msg.channelID.equals(channelId) && msg.channelType == channelType) {
                    if (!chatAdapter.isExist(msg.clientMsgNO, msg.messageID)) {
                        if (!isCanLoadMore) {
                            //移除正在输入
                            if (chatAdapter.getItemCount() > 0 && chatAdapter.getData().get(chatAdapter.getItemCount() - 1).msMsg != null && chatAdapter.getData().get(chatAdapter.getItemCount() - 1).msMsg.type == MSContentType.typing) {
                                chatAdapter.removeAt(chatAdapter.getItemCount() - 1);
                            }
                            MSMsg timeMsg = addTimeMsg(msg.timestamp);
                            MSUIChatMsgItemEntity itemEntity = MSIMUtils.getInstance().msg2UiMsg(this, msg, count, showNickName, chatAdapter.isShowChooseItem());
                            if (timeMsg != null && chatAdapter.getData().size() > 1) {
                                chatAdapter.getData().get(chatAdapter.getData().size() - 2).nextMsg = timeMsg;
                            }
                            int previousMsgIndex = -1;
                            if (timeMsg == null) {
                                if (MSReader.isNotEmpty(chatAdapter.getData())) {
                                    itemEntity.previousMsg = chatAdapter.getData().get(chatAdapter.getData().size() - 1).msMsg;
                                    chatAdapter.getData().get(chatAdapter.getData().size() - 1).nextMsg = itemEntity.msMsg;
                                }
                            } else {
                                itemEntity.previousMsg = timeMsg;
                            }
                            if (MSReader.isNotEmpty(chatAdapter.getData())) {
                                previousMsgIndex = chatAdapter.getData().size() - 1;
                            }
                            if (!isShowHistory && redDot == 0 && itemEntity.msMsg.flame == 1 && itemEntity.msMsg.type != MSContentType.MS_VOICE && itemEntity.msMsg.type != MSContentType.MS_IMAGE && itemEntity.msMsg.type != MSContentType.MS_VIDEO) {
                                itemEntity.msMsg.viewed = 1;
                                itemEntity.msMsg.viewedAt = MSTimeUtils.getInstance().getCurrentMills();
                                MSIM.getInstance().getMsgManager().updateViewedAt(1, itemEntity.msMsg.viewedAt, itemEntity.msMsg.clientMsgNO);
                            }
                            MSPlaySound.getInstance().playInMsg(R.raw.sound_in);
                            chatAdapter.addData(itemEntity);
                            if (msg.messageSeq > maxMsgSeq) {
                                maxMsgSeq = msg.messageSeq;
                            }
                            if (msg.orderSeq > maxMsgOrderSeq) {
                                maxMsgOrderSeq = msg.orderSeq;
                            }
                            if (previousMsgIndex != -1) {
                                chatAdapter.notifyBackground(previousMsgIndex);
                            }
                        }
                        if (isShowHistory || redDot > 0) {
                            redDot += 1;
                            showUnReadCountView();
                            msVBinding.chatUnreadLayout.newMsgLayout.post(() -> CommonAnim.getInstance().showOrHide(msVBinding.chatUnreadLayout.newMsgLayout, redDot > 0, true, false));
                        } else {
                            scrollToEnd();
                            if (msg.setting.receipt == 1) readMsgIds.add(msg.messageID);
                        }
                    }
                }

            }
        }
    }

    private synchronized void typing(MSCMD msCmd) {

        if (redDot > 0) return;
        String channel_id = msCmd.paramJsonObject.optString("channel_id");
        byte channel_type = (byte) msCmd.paramJsonObject.optInt("channel_type");
        String from_uid = msCmd.paramJsonObject.optString("from_uid");
        String from_name = msCmd.paramJsonObject.optString("from_name");
        int isRobot;
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(from_uid, MSChannelType.PERSONAL);
        if (channel == null) {
            channel = new MSChannel(from_uid, MSChannelType.PERSONAL);
            channel.channelName = from_name;
        }
        isRobot = channel.robot;
        if (channelId.equals(channel_id) && channelType == channel_type && !TextUtils.equals(from_uid, loginUID)) {
            MSChannelMember mChannelMember = null;
            if (channelType == MSChannelType.GROUP && isRobot == 0) {
                // 没在群内的cmd不显示
                mChannelMember = MSIM.getInstance().getChannelMembersManager().getMember(channelId, channelType, from_uid);
                if (mChannelMember == null || mChannelMember.isDeleted == 1) return;
            }
            if (chatAdapter.getItemCount() > 0 && chatAdapter.getData().get(chatAdapter.getItemCount() - 1).msMsg.type == MSContentType.typing) {
                chatAdapter.getData().get(chatAdapter.getItemCount() - 1).msMsg.setFrom(channel);
                chatAdapter.getData().get(chatAdapter.getItemCount() - 1).msMsg.fromUID = from_uid;
                chatAdapter.getData().get(chatAdapter.getItemCount() - 1).msMsg.setMemberOfFrom(mChannelMember);
                chatAdapter.notifyItemChanged(chatAdapter.getItemCount() - 1);
            } else {
                addTimeMsg(MSTimeUtils.getInstance().getCurrentSeconds());
                int index = chatAdapter.getData().size() - 1;
                if (chatAdapter.lastMsgIsTyping()) index--;
                if (index < 0) index = 0;

                MSUIChatMsgItemEntity msgItemEntity = new MSUIChatMsgItemEntity(this, new MSMsg(), null);
                msgItemEntity.msMsg.channelType = channelType;
                msgItemEntity.msMsg.channelID = channelId;
                msgItemEntity.msMsg.type = MSContentType.typing;
                msgItemEntity.msMsg.setFrom(channel);
                msgItemEntity.showNickName = showNickName;
                msgItemEntity.msMsg.fromUID = channel.channelID;
                MSChannelMember member = new MSChannelMember();
                member.memberUID = channel.channelID;
                member.channelID = channelId;
                member.channelType = channelType;
                member.memberName = channel.channelName;
                member.memberRemark = channel.channelRemark;
                msgItemEntity.msMsg.setMemberOfFrom(member);
                msgItemEntity.previousMsg = chatAdapter.getLastMsg();
                chatAdapter.addData(msgItemEntity);
                chatAdapter.getData().get(index).nextMsg = msgItemEntity.msMsg;

                int type = chatAdapter.getData().get(index).msMsg.type;
                if (MSContentType.isLocalMsg(type) || MSContentType.isSystemMsg(type)) {
                    chatAdapter.notifyItemChanged(index);
                } else {
                    chatAdapter.notifyBackground(index);
                }

                if (!isShowHistory && !isCanLoadMore) {
                    scrollToEnd();
                }
            }
        }
    }

    private synchronized void refreshMsg(MSMsg msMsg) {
        MSIMUtils.getInstance().resetMsgProhibitWord(msMsg);
        List<MSUIChatMsgItemEntity> list = chatAdapter.getData();
        chatAdapter.refreshReplyMsg(msMsg);
        for (int i = 0, size = list.size(); i < size; i++) {
            if (list.get(i).msMsg == null) {
                continue;
            }
            boolean isNotify = false;
            if (list.get(i).msMsg.clientSeq == msMsg.clientSeq
                    || list.get(i).msMsg.clientMsgNO.equals(msMsg.clientMsgNO)
                    || (!TextUtils.isEmpty(list.get(i).msMsg.messageID) && !TextUtils.isEmpty(msMsg.messageID) && list.get(i).msMsg.messageID.equals(msMsg.messageID))) {
                if (msMsg.messageSeq > maxMsgSeq) {
                    maxMsgSeq = msMsg.messageSeq;
                }
                if (msMsg.messageSeq > lastVisibleMsgSeq) {
                    lastVisibleMsgSeq = msMsg.messageSeq;
                }
                if (list.get(i).msMsg.remoteExtra.revoke != msMsg.remoteExtra.revoke) {
                    isNotify = true;
                }
                // 消息撤回
                list.get(i).msMsg.remoteExtra.revoke = msMsg.remoteExtra.revoke;
                list.get(i).msMsg.remoteExtra.revoker = msMsg.remoteExtra.revoker;
                if (list.get(i).msMsg.status != MSSendMsgResult.send_success && msMsg.status == MSSendMsgResult.send_success) {
                    MSPlaySound.getInstance().playOutMsg(R.raw.sound_out);
                }
                boolean isResetStatus = false;
                boolean isResetListener = false;
                boolean isResetData = false;
                boolean isResetReaction = false;
                if (list.get(i).msMsg.status != msMsg.status
                        || (list.get(i).msMsg.remoteExtra.readedCount != msMsg.remoteExtra.readedCount && list.get(i).msMsg.remoteExtra.readedCount == 0)
                        || list.get(i).msMsg.remoteExtra.editedAt != msMsg.remoteExtra.editedAt
                ) {
                    list.get(i).isUpdateStatus = true;
                    isResetStatus = true;
                }
                if (list.get(i).msMsg.remoteExtra.isPinned != msMsg.remoteExtra.isPinned) {
                    isResetStatus = true;
                }
                list.get(i).msMsg.voiceStatus = msMsg.voiceStatus;

                if (hideChannelAllPinnedMessage == 0) {
                    list.get(i).isPinned = msMsg.remoteExtra.isPinned;
                } else {
                    list.get(i).isPinned = 0;
                }
                if (list.get(i).msMsg.remoteExtra.readedCount != msMsg.remoteExtra.readedCount && !isResetStatus) {
                    isResetListener = true;
                }
                list.get(i).msMsg.remoteExtra.isPinned = msMsg.remoteExtra.isPinned;
                list.get(i).msMsg.remoteExtra.readed = msMsg.remoteExtra.readed;
                list.get(i).msMsg.remoteExtra.readedCount = msMsg.remoteExtra.readedCount;
                list.get(i).msMsg.remoteExtra.needUpload = msMsg.remoteExtra.needUpload;
                if (list.get(i).msMsg.remoteExtra.readedCount == 0) {
                    list.get(i).msMsg.remoteExtra.unreadCount = count - 1;
                } else
                    list.get(i).msMsg.remoteExtra.unreadCount = msMsg.remoteExtra.unreadCount;
                if ((TextUtils.isEmpty(list.get(i).msMsg.remoteExtra.contentEdit) && !TextUtils.isEmpty(msMsg.remoteExtra.contentEdit)) || (!TextUtils.isEmpty(list.get(i).msMsg.remoteExtra.contentEdit) && !TextUtils.isEmpty(msMsg.remoteExtra.contentEdit) && !list.get(i).msMsg.remoteExtra.contentEdit.equals(msMsg.remoteExtra.contentEdit))) {
                    list.get(i).msMsg.remoteExtra.editedAt = msMsg.remoteExtra.editedAt;
                    list.get(i).msMsg.remoteExtra.contentEdit = msMsg.remoteExtra.contentEdit;
                    list.get(i).msMsg.remoteExtra.contentEditMsgModel = msMsg.remoteExtra.contentEditMsgModel;
                    list.get(i).isUpdateStatus = true;
                    list.get(i).formatSpans(ChatActivity.this, chatAdapter.getData().get(i).msMsg);
                    isResetData = true;
                }

                list.get(i).msMsg.isDeleted = msMsg.isDeleted;
                list.get(i).msMsg.messageID = msMsg.messageID;
                list.get(i).msMsg.messageSeq = msMsg.messageSeq;
                list.get(i).msMsg.orderSeq = msMsg.orderSeq;
                if ((msMsg.localExtraMap != null && !msMsg.localExtraMap.isEmpty())) {
                    isNotify = true;
                }
                if (isRefreshReaction(list.get(i).msMsg.reactionList, msMsg.reactionList)) {
                    isResetReaction = true;
                }
                list.get(i).msMsg.localExtraMap = msMsg.localExtraMap;
                list.get(i).msMsg.content = msMsg.content;
                list.get(i).msMsg.reactionList = msMsg.reactionList;
                list.get(i).msMsg.baseContentMsgModel = msMsg.baseContentMsgModel;
                list.get(i).msMsg.status = msMsg.status;
                if (isNotify) {
                    EndpointManager.getInstance().invoke("stop_reaction_animation", null);
                    chatAdapter.notifyItemChanged(i);
                } else {
                    if (isResetStatus) {
                        chatAdapter.notifyStatus(i);
                    }
                    if (isResetListener) {
                        chatAdapter.notifyListener(i);
                    }
                    if (isResetData) {
                        chatAdapter.notifyData(i);
                    }
                    if (isResetReaction) {
                        list.get(i).isRefreshReaction = true;
                        chatAdapter.notifyItemChanged(i, list.get(i));
                        //chatAdapter.notifyReaction(i, msMsg.reactionList);
                    }
                }

                if (list.get(i).msMsg.remoteExtra.revoke == 1) {
                    int finalI = i;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        int previousIndex = finalI - 1;
                        int nextIndex = finalI + 1;
                        if (previousIndex >= 0 && list.get(previousIndex).msMsg.remoteExtra.revoke == 0) {
                            chatAdapter.notifyItemChanged(previousIndex);
                        }
                        if (nextIndex <= chatAdapter.getData().size() - 1 && list.get(nextIndex).msMsg.remoteExtra.revoke == 0) {
                            chatAdapter.notifyItemChanged(nextIndex);
                        }
                    }, 200);
                }

                if ((msMsg.status == MSSendMsgResult.no_relation || msMsg.status == MSSendMsgResult.not_on_white_list) && channelType == MSChannelType.PERSONAL) {
                    if (UserUtils.getInstance().checkBlacklist(channelId)) {
                        return;
                    }
                    // 不是好友
                    MSMsg noRelationMsg = new MSMsg();
                    noRelationMsg.channelID = channelId;
                    noRelationMsg.channelType = channelType;
                    noRelationMsg.type = MSContentType.noRelation;
                    long tempOrderSeq = MSIM.getInstance().getMsgManager().getMessageOrderSeq(0, msMsg.channelID, msMsg.channelType);
                    noRelationMsg.orderSeq = tempOrderSeq + 1;
                    noRelationMsg.status = MSSendMsgResult.send_success;

                    int index = chatAdapter.getData().size() - 1;
                    if (chatAdapter.lastMsgIsTyping()) index--;
                    MSUIChatMsgItemEntity itemEntity = MSIMUtils.getInstance().msg2UiMsg(this, noRelationMsg, count, showNickName, chatAdapter.isShowChooseItem());
                    chatAdapter.getData().get(index).nextMsg = noRelationMsg;
                    itemEntity.previousMsg = chatAdapter.getData().get(index).msMsg;

                    chatAdapter.notifyItemChanged(index);
                    chatAdapter.addData(index + 1, itemEntity);
                    if (isToEnd) {
                        scrollToEnd();
                    }
                    MSIM.getInstance().getMsgManager().saveAndUpdateConversationMsg(noRelationMsg, false);
                }
                break;
            }
        }
    }

    private MSMsg getSpanEmptyMsg() {
        MSMsg msg = new MSMsg();
        msg.timestamp = 0;
        // 为了方便直接用该字段替换
        msg.messageSeq = getTopPinViewHeight();
        msg.type = MSContentType.spanEmptyView;
        return msg;
    }

    private boolean isAddedSpanEmptyView() {
        return MSReader.isNotEmpty(chatAdapter.getData()) && chatAdapter.getData().get(0).msMsg != null && chatAdapter.getData().get(0).msMsg.type == MSContentType.spanEmptyView;
    }
}
