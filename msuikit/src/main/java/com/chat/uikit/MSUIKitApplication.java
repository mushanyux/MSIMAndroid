package com.chat.uikit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.Manifest;
import android.app.Application;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.chat.base.MSBaseApplication;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSBinder;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.config.MSSystemAccount;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatChooseContacts;
import com.chat.base.endpoint.entity.ChatFunctionMenu;
import com.chat.base.endpoint.entity.ChatItemPopupMenu;
import com.chat.base.endpoint.entity.ChatToolBarMenu;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.endpoint.entity.ChooseChatMenu;
import com.chat.base.endpoint.entity.ChooseContactsMenu;
import com.chat.base.endpoint.entity.ContactsMenu;
import com.chat.base.endpoint.entity.DBMenu;
import com.chat.base.endpoint.entity.LoginMenu;
import com.chat.base.endpoint.entity.MsgConfig;
import com.chat.base.endpoint.entity.PersonalInfoMenu;
import com.chat.base.endpoint.entity.ScanResultMenu;
import com.chat.base.endpoint.entity.SearchChatContentMenu;
import com.chat.base.endpoint.entity.UserDetailMenu;
import com.chat.base.endpoint.entity.MSMsg2UiMsgMenu;
import com.chat.base.endpoint.entity.WithdrawMsgMenu;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.glide.ChooseMimeType;
import com.chat.base.glide.ChooseResult;
import com.chat.base.glide.ChooseResultModel;
import com.chat.base.glide.GlideUtils;
import com.chat.base.msg.IConversationContext;
import com.chat.base.msg.model.MSGifContent;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.msgitem.MSMsgItemViewManager;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.components.AlertDialog;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.utils.ActManagerUtils;
import com.chat.base.utils.ImageUtils;
import com.chat.base.utils.LayoutHelper;
import com.chat.base.utils.MSDeviceUtils;
import com.chat.base.utils.MSFileUtils;
import com.chat.base.utils.MSMediaFileUtils;
import com.chat.base.utils.MSPermissions;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSToastUtils;
import com.chat.uikit.chat.ChooseChatActivity;
import com.chat.uikit.chat.face.MSVoiceViewManager;
import com.chat.uikit.chat.manager.FaceManger;
import com.chat.uikit.chat.manager.MSIMUtils;
import com.chat.uikit.chat.msgmodel.MSCardContent;
import com.chat.uikit.chat.msgmodel.MSMultiForwardContent;
import com.chat.uikit.chat.provider.LoadingProvider;
import com.chat.uikit.chat.provider.MSCardProvider;
import com.chat.uikit.chat.provider.MSEmptyProvider;
import com.chat.uikit.chat.provider.MSImageProvider;
import com.chat.uikit.chat.provider.MSMultiForwardProvider;
import com.chat.uikit.chat.provider.MSNoRelationProvider;
import com.chat.uikit.chat.provider.MSPromptNewMsgProvider;
import com.chat.uikit.chat.provider.MSSensitiveWordsProvider;
import com.chat.uikit.chat.provider.MSSpanEmptyProvider;
import com.chat.uikit.chat.provider.MSTextProvider;
import com.chat.uikit.chat.provider.MSVoiceProvider;
import com.chat.uikit.chat.search.date.SearchWithDateActivity;
import com.chat.uikit.chat.search.image.SearchWithImgActivity;
import com.chat.uikit.contacts.ChooseContactsActivity;
import com.chat.uikit.contacts.NewFriendsActivity;
import com.chat.uikit.enity.SensitiveWords;
import com.chat.uikit.group.SavedGroupsActivity;
import com.chat.uikit.group.MSAllMembersActivity;
import com.chat.uikit.message.MsgModel;
import com.chat.uikit.message.ProhibitWordModel;
import com.chat.uikit.search.AddFriendsActivity;
import com.chat.uikit.setting.MsgNoticesSettingActivity;
import com.chat.uikit.setting.SettingActivity;
import com.chat.uikit.user.UserDetailActivity;
import com.tencent.bugly.crashreport.CrashReport;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.msgmodel.MSImageContent;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;
import com.mushanyux.mushanim.msgmodel.MSTextContent;
import com.mushanyux.mushanim.msgmodel.MSVideoContent;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ui kit
 */
public class MSUIKitApplication {
    int totalMsgCount = 0;
    public String chattingChannelID;
    public SensitiveWords sensitiveWords;
    public boolean isRefreshChatActivityMessage = false;

    private MSUIKitApplication() {
    }

    private static class KitApplicationBinder {
        private static final MSUIKitApplication uikit = new MSUIKitApplication();
    }

    public static MSUIKitApplication getInstance() {
        return KitApplicationBinder.uikit;
    }

    private WeakReference<Application> mContext;

    public void init(Application mContext) {
        this.mContext = new WeakReference<>(mContext);
        initIM();
        //初始化im事件及监听
        MSIMUtils.getInstance().initIMListener();
        initKitModuleListener();
        String json = MSSharedPreferencesUtil.getInstance().getSP("ms_sensitive_words");
        if (!TextUtils.isEmpty(json)) {
            sensitiveWords = JSON.parseObject(json, SensitiveWords.class);
        }
        MsgModel.getInstance().syncSensitiveWords();
        ProhibitWordModel.Companion.getInstance().sync();
        MsgModel.getInstance().deleteFlameMsg();
    }

    public Context getContext() {
        return mContext.get();
    }


    public void initIM() {
        if (!TextUtils.isEmpty(MSConfig.getInstance().getToken())) {
            //设置开发模式
            MSIM.getInstance().setDebug(MSBinder.isDebug);
            MSIM.getInstance().setFileCacheDir("msIMFile");

            String imToken = MSConfig.getInstance().getImToken();
            String uid = MSConfig.getInstance().getUid();
            MSIM.getInstance().init(mContext.get(), uid, imToken);

            CrashReport.initCrashReport(getContext(), "b8bf09f25f", false);
            CrashReport.setUserId(MSConfig.getInstance().getUid());
            CrashReport.setDeviceModel(getContext(), MSDeviceUtils.getInstance().getSystemModel());

        }
    }

    public void startChat() {
        if (!TextUtils.isEmpty(MSConfig.getInstance().getToken())) {
            Log.e("去连接", "-->");
            MSIM.getInstance().getConnectionManager().connection();
        }
    }

    public void stopConn() {
        EndpointManager.getInstance().invoke("push_update_device_badge", totalMsgCount);
        MSIM.getInstance().getConnectionManager().disconnect(false);
    }

    private void initKitModuleListener() {
        // 注册消息model到sdk
        MSIM.getInstance().getMsgManager().registerContentMsg(MSCardContent.class);
        MSIM.getInstance().getMsgManager().registerContentMsg(MSMultiForwardContent.class);
        //添加消息item
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.sensitiveWordsTips, new MSSensitiveWordsProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.noRelation, new MSNoRelationProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.msgPromptNewMsg, new MSPromptNewMsgProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.MS_TEXT, new MSTextProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.MS_IMAGE, new MSImageProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.emptyView, new MSEmptyProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.spanEmptyView, new MSSpanEmptyProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.MS_VOICE, new MSVoiceProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.MS_CARD, new MSCardProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.MS_MULTIPLE_FORWARD, new MSMultiForwardProvider());
        MSMsgItemViewManager.getInstance().addChatItemViewProvider(MSContentType.loading, new LoadingProvider());
        // 设置消息长按选项
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + MSContentType.MS_TEXT, object -> new MsgConfig(true));
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + MSContentType.MS_IMAGE, object -> new MsgConfig(true));
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + MSContentType.MS_CARD, object -> new MsgConfig(true));
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + MSContentType.MS_VOICE, object -> new MsgConfig(true));
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + MSContentType.MS_MULTIPLE_FORWARD, object -> new MsgConfig(true));
        EndpointManager.getInstance().setMethod("uikit_sql", EndpointCategory.msDBMenus, object -> new DBMenu("uikit_sql"));
        //注册消息长按菜单配置
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + MSContentType.MS_VOICE, object -> new MsgConfig(false, true, true, false, false, false));
        EndpointManager.getInstance().setMethod(EndpointCategory.msgConfig + MSContentType.typing, object -> new MsgConfig(false));
        EndpointManager.getInstance().setMethod("", EndpointCategory.msChatPopupItem, 90, object -> {
            MSMsg msMsg = (MSMsg) object;
            if (msMsg.type == MSContentType.MS_TEXT) {
                return new ChatItemPopupMenu(R.mipmap.msg_copy, getContext().getString(R.string.copy), (msg, iConversationContext) -> {
                    MSTextContent textContent = (MSTextContent) msg.baseContentMsgModel;
                    String content = textContent.content;
                    if (msg.remoteExtra.contentEditMsgModel != null) {
                        content = msg.remoteExtra.contentEditMsgModel.getDisplayContent();
                    }
                    ClipboardManager cm = (ClipboardManager) iConversationContext.getChatActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("Label", content);
                    assert cm != null;
                    cm.setPrimaryClip(mClipData);
                    MSToastUtils.getInstance().showToastNormal(iConversationContext.getChatActivity().getString(R.string.copyed));
                });
            }
            return null;
        });

        //添加个人中心
        EndpointManager.getInstance().setMethod("personal_center_currency", EndpointCategory.personalCenter, 2, object -> new PersonalInfoMenu(R.mipmap.icon_setting, mContext.get().getString(R.string.currency), () -> {
            Intent intent = new Intent(mContext.get(), SettingActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
        }));
        EndpointManager.getInstance().setMethod("personal_center_new_msg_notice", EndpointCategory.personalCenter, 3, object -> new PersonalInfoMenu(R.mipmap.icon_notice, mContext.get().getString(R.string.new_msg_notice), () -> {
            Intent intent = new Intent(mContext.get(), MsgNoticesSettingActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
        }));
        EndpointManager.getInstance().setMethod("personal_center_web_login", EndpointCategory.personalCenter, 1000, object -> new PersonalInfoMenu(R.mipmap.icon_web_login, mContext.get().getString(R.string.web_login), () -> EndpointManager.getInstance().invoke("show_web_login_desc", mContext.get())));

        //添加通讯录
        EndpointManager.getInstance().setMethod(EndpointCategory.mailList + "_friends", EndpointCategory.mailList, 100, object -> new ContactsMenu("friend", R.mipmap.icon_new_friend, mContext.get().getString(R.string.new_friends), () -> {
            Intent intent = new Intent(mContext.get(), NewFriendsActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
        }));
        EndpointManager.getInstance().setMethod(EndpointCategory.mailList + "_groups", EndpointCategory.mailList, 90, object -> new ContactsMenu("group", R.mipmap.icon_groups, mContext.get().getString(R.string.saved_groups), () -> {
            Intent intent = new Intent(mContext.get(), SavedGroupsActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
        }));

        // 添加聊天工具栏菜单语音
        EndpointManager.getInstance().setMethod(EndpointCategory.msChatToolBar + "_voice", EndpointCategory.msChatToolBar, 97, object -> {
            IConversationContext iConversationContext = (IConversationContext) object;
            View voiceView = MSVoiceViewManager.getInstance().getVoiceView(iConversationContext);
            return new ChatToolBarMenu("ms_chat_toolbar_voice", R.mipmap.icon_chat_toolbar_voice, R.mipmap.icon_chat_toolbar_voice, voiceView, (isSelected, iConversationContext14) -> {
            });
        });
        //聊天工具栏相册
        EndpointManager.getInstance().setMethod(EndpointCategory.msChatToolBar + "_album", EndpointCategory.msChatToolBar, 99, object -> new ChatToolBarMenu("ms_chat_toolbar_album", R.mipmap.icon_chat_toolbar_album, -1, null, (isSelected, iConversationContext1) -> {
            if (isSelected) {
                chooseIMG(iConversationContext1);
            }
        }));
        //聊天工具栏@
        EndpointManager.getInstance().setMethod(EndpointCategory.msChatToolBar + "_remind", EndpointCategory.msChatToolBar, 96, object
                -> {
            IConversationContext iConversationContext = (IConversationContext) object;
            if (iConversationContext.getChatChannelInfo().channelType == MSChannelType.PERSONAL)
                return null;
            return new ChatToolBarMenu("ms_chat_toolbar_remind", R.mipmap.icon_chat_toolbar_aite, -1, null, (isSelected, iConversationContext12) -> {

            });
        });

        // 添加聊天工具栏菜单
        EndpointManager.getInstance().setMethod(EndpointCategory.msChatToolBar + "_more", EndpointCategory.msChatToolBar, 40, object -> {
            IConversationContext iConversationContext = (IConversationContext) object;
            View moreView = FaceManger.getInstance().getFunctionView(iConversationContext, chatFunctionMenu -> chatFunctionMenu.iChatFunctionCLick.onClick(iConversationContext));

            return new ChatToolBarMenu("ms_chat_toolbar_more", R.mipmap.icon_chat_toolbar_more, R.mipmap.icon_chat_toolbar_more, moreView, (isSelected, iConversationContext13) -> {

            });
        });
        //添加聊天功能面板
        EndpointManager.getInstance().setMethod(EndpointCategory.chatFunction + "_chooseImg", EndpointCategory.chatFunction, 100, object -> new ChatFunctionMenu("chooseImg", R.mipmap.icon_func_album, mContext.get().getString(R.string.image), this::chooseIMG));
        EndpointManager.getInstance().setMethod(EndpointCategory.chatFunction + "_chooseCard", EndpointCategory.chatFunction, 95, object -> new ChatFunctionMenu("chooseCard", R.mipmap.icon_func_card, mContext.get().getString(R.string.card), IConversationContext::sendCardMsg));

        //添加tab页
        EndpointManager.getInstance().setMethod(EndpointCategory.tabMenus + "_start_chat", EndpointCategory.tabMenus, 200, object -> new PopupMenuItem(mContext.get().getString(R.string.start_group_chat), R.mipmap.menu_chats, () -> {
            Intent intent = new Intent(mContext.get(), ChooseContactsActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
        }));
        EndpointManager.getInstance().setMethod(EndpointCategory.tabMenus + "_add_friends", EndpointCategory.tabMenus, 99, object -> new PopupMenuItem(mContext.get().getString(R.string.add_friends), R.mipmap.menu_invite, () -> {
            Intent intent = new Intent(mContext.get(), AddFriendsActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
        }));

        //显示聊天页面
        EndpointManager.getInstance().setMethod(EndpointSID.chatView, object -> {
            if (object instanceof ChatViewMenu chatViewMenu) {
                if (!TextUtils.isEmpty(chatViewMenu.channelID)) {
                    MSIMUtils.getInstance().startChatActivity(chatViewMenu);
                }
            }
            return null;
        });

        //撤回消息
        EndpointManager.getInstance().setMethod("chat_withdraw_msg", object -> {
            final WithdrawMsgMenu withdrawMsgMenu = (WithdrawMsgMenu) object;
            if (withdrawMsgMenu != null) {
                MsgModel.getInstance().revokeMsg(withdrawMsgMenu.message_id, withdrawMsgMenu.channel_id, withdrawMsgMenu.channel_type, withdrawMsgMenu.client_msg_no, (code, msg) -> {
                    if (code != HttpResponseCode.success) {
                        MSToastUtils.getInstance().showToastNormal(msg);
                        //  MSIM.getInstance().getMsgManager().updateMsgRevokeWithMessageID(withdrawMsgMenu.message_id, 1);
//                        MSIM.getInstance().getMessageManager().deleteMsgByClientMsgNo(client_msg_no);
                    }
                });
            }
            return null;
        });
        EndpointManager.getInstance().setMethod("str_delete_msg", object -> {
            MSMsg msg = (MSMsg) object;
            if (msg != null) {
                List<MSMsg> list = new ArrayList<>();
                list.add(msg);
                MsgModel.getInstance().deleteMsg(list, null);
            }
            return null;
        });
        //选择会话
        EndpointManager.getInstance().setMethod(EndpointSID.showChooseChatView, object -> {
            ChooseChatMenu messageContent = (ChooseChatMenu) object;
            Intent intent = new Intent(mContext.get(), ChooseChatActivity.class);
            intent.putExtra("isChoose", true);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
            MSUIKitApplication.this.messageContentList = messageContent.list;
            MSUIKitApplication.this.chooseChatCallBack = messageContent.mChatChooseContacts;
            return null;
        });

        //处理扫一扫结果
        EndpointManager.getInstance().setMethod("", EndpointCategory.msScan, object -> new ScanResultMenu(hashMap -> {
            String type = Objects.requireNonNull(hashMap.get("type")).toString();
            if (type.equals("userInfo")) {
                JSONObject dataJson = (JSONObject) hashMap.get("data");
                if (dataJson != null && dataJson.has("uid")) {
                    String uid = dataJson.optString("uid");
                    String verCode = dataJson.optString("vercode");
                    if (!TextUtils.isEmpty(uid)) {
                        Intent intent = new Intent(mContext.get(), UserDetailActivity.class);
                        intent.putExtra("uid", uid);
                        intent.putExtra("vercode", verCode);
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        mContext.get().startActivity(intent);
                    }
                }
                return true;
            } else return false;

        }));
        //选择联系人
        EndpointManager.getInstance().setMethod("choose_contacts", object -> {
            Intent intent = new Intent(mContext.get(), ChooseContactsActivity.class);
            intent.putExtra("type", 2);
            this.contactsMenu = (ChooseContactsMenu) object;
            if (contactsMenu != null) {
                intent.putParcelableArrayListExtra("defaultSelected", (ArrayList<? extends Parcelable>) contactsMenu.defaultSelected);
                intent.putExtra("isShowSaveLabelDialog", contactsMenu.isShowSaveLabelDialog);
                if (MSReader.isNotEmpty(contactsMenu.defaultSelected) && !contactsMenu.isCanDeselect) {
                    String unSelectUids = "";
                    for (int i = 0, size = contactsMenu.defaultSelected.size(); i < size; i++) {
                        if (TextUtils.isEmpty(unSelectUids)) {
                            unSelectUids = contactsMenu.defaultSelected.get(i).channelID;
                        } else
                            unSelectUids = unSelectUids + "," + contactsMenu.defaultSelected.get(i).channelID;
                    }
                    intent.putExtra("unSelectUids", unSelectUids);
                }
            }
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
            return null;
        });
        EndpointManager.getInstance().setMethod("exit_login", object -> {
            exitLogin(0);
            return null;
        });
        //查看用户详情
        EndpointManager.getInstance().setMethod(EndpointSID.userDetailView, object -> {
            UserDetailMenu msUserDetailMenu = (UserDetailMenu) object;
            if (msUserDetailMenu != null) {
                if (!TextUtils.isEmpty(msUserDetailMenu.uid)) {
                    Intent intent = new Intent(mContext.get(), UserDetailActivity.class);
                    intent.putExtra("uid", msUserDetailMenu.uid);
                    if (!TextUtils.isEmpty(msUserDetailMenu.groupID)) {
                        intent.putExtra("groupID", msUserDetailMenu.groupID);
                    }
                    msUserDetailMenu.context.startActivity(intent);
                }

            }
            return null;
        });

        EndpointManager.getInstance().setMethod("show_tab_main", object -> {
            Intent intent = new Intent(mContext.get(), TabActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
            return null;
        });
        //监听登录状态
        EndpointManager.getInstance().setMethod("", EndpointCategory.loginMenus, object -> new LoginMenu(() -> {
            Log.e("接受登录", "-->3");
            MSSharedPreferencesUtil.getInstance().putInt("ms_lock_screen_pwd_count", 5);
            MSSharedPreferencesUtil.getInstance().putBoolean("sync_friend", true);
            //初始化im
            MSUIKitApplication.getInstance().initIM();
            //初始化密钥
//            MSIM.getInstance().getSignalProtocolManager().init();
            UserInfoEntity userInfo = MSConfig.getInstance().getUserInfo();
            if (userInfo != null) {
                MSIM.getInstance().getCMDManager().setRSAPublicKey(userInfo.rsa_public_key);
                MSIM.getInstance().getChannelManager().updateAvatarCacheKey(userInfo.uid, MSChannelType.PERSONAL, UUID.randomUUID().toString().replaceAll("-", ""));
            }
            Intent intent = new Intent(mContext.get(), TabActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.get().startActivity(intent);
            startChat();
            ProhibitWordModel.Companion.getInstance().sync();
            MsgModel.getInstance().deleteFlameMsg();
        }));

        EndpointManager.getInstance().setMethod("syncExtraMsg", object -> {
            if (object != null) {
                MSChannel channel = (MSChannel) object;
                MsgModel.getInstance().syncExtraMsg(channel.channelID, channel.channelType);
            }
            return null;
        });

        EndpointManager.getInstance().setMethod("deleteRemoteMsg", object -> {
            if (object instanceof String clientMsgNo) {
                MSMsg msg = MSIM.getInstance().getMsgManager().getWithClientMsgNO(clientMsgNo);
                if (msg != null) {
                    List<MSMsg> list = new ArrayList<>();
                    list.add(msg);
                    MsgModel.getInstance().deleteMsg(list, null);
                }
            }
            return null;
        });

        EndpointManager.getInstance().setMethod("get_chat_uid_msg", object -> {
            if (object instanceof MSMsg2UiMsgMenu msMsg2UiMsgMenu) {
                return MSIMUtils.getInstance().msg2UiMsg(msMsg2UiMsgMenu.getIConversationContext(), msMsg2UiMsgMenu.getMsMsg(), msMsg2UiMsgMenu.getMemberCount(), msMsg2UiMsgMenu.getShowNickName(), msMsg2UiMsgMenu.isChoose());
            }
            return null;
        });

        // 搜索消息按群成员搜索
        EndpointManager.getInstance().setMethod("search_message_with_member", EndpointCategory.msSearchChatContent, 101, object -> {
            if (object instanceof MSChannel) {
                if (((MSChannel) object).channelType == MSChannelType.GROUP) {
                    return new SearchChatContentMenu(MSBaseApplication.getInstance().getContext().getString(R.string.uikit_search_member), (channelID, channelType) -> {
                        Intent intent = new Intent(MSBaseApplication.getInstance().getContext(), MSAllMembersActivity.class);
                        intent.putExtra("channelID", ((MSChannel) object).channelID);
                        intent.putExtra("channelType", MSChannelType.GROUP);
                        intent.putExtra("searchMessage", true);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        MSBaseApplication.getInstance().getContext().startActivity(intent);
                    });
                }
            }
            return null;
        });

        // 搜索消息按日期搜索
        EndpointManager.getInstance().setMethod("search_message_with_date", EndpointCategory.msSearchChatContent, 96, object -> {
            if (object instanceof MSChannel) {
                return new SearchChatContentMenu(MSBaseApplication.getInstance().getContext().getString(R.string.uikit_search_for_date), (channelID, channelType) -> {
                    Intent intent = new Intent(MSBaseApplication.getInstance().getContext(), SearchWithDateActivity.class);
                    intent.putExtra("channel_id", ((MSChannel) object).channelID);
                    intent.putExtra("channel_type", ((MSChannel) object).channelType);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MSBaseApplication.getInstance().getContext().startActivity(intent);
                });
            }
            return null;
        });


        // 搜索消息按图片搜索
        EndpointManager.getInstance().setMethod("search_message_with_img", EndpointCategory.msSearchChatContent, 98, object -> {
            if (object instanceof MSChannel) {
                return new SearchChatContentMenu(MSBaseApplication.getInstance().getContext().getString(R.string.uikit_search_for_image), (channelID, channelType) -> {
                    Intent intent = new Intent(MSBaseApplication.getInstance().getContext(), SearchWithImgActivity.class);
                    intent.putExtra("channel_id", ((MSChannel) object).channelID);
                    intent.putExtra("channel_type", ((MSChannel) object).channelType);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MSBaseApplication.getInstance().getContext().startActivity(intent);
                });
            }
            return null;
        });

    }

    public void sendChooseChatBack(List<MSChannel> list) {
        if (chooseChatCallBack != null) {
            chooseChatCallBack.iChoose.onResult(list);
            chooseChatCallBack = null;
        }
    }

    public List<MSMessageContent> getMessageContentList() {
        return messageContentList;
    }

    public void setChooseContactsBack(List<MSChannel> list) {
        if (contactsMenu != null) {
            contactsMenu.iChooseBack.onBack(list);
            contactsMenu = null;
        }
    }

    private ChatChooseContacts chooseChatCallBack;
    private ChooseContactsMenu contactsMenu;
    private List<MSMessageContent> messageContentList;

    public void exitLogin(int from) {
        MsgModel.getInstance().stopTimer();
        EndpointManager.getInstance().invoke("ms_logout", null);
        MSConfig.getInstance().clearInfo();
        MSIM.getInstance().getConnectionManager().disconnect(true);
        ActManagerUtils.getInstance().clearAllActivity();
        EndpointManager.getInstance().invoke("main_show_home_view", from);
        //关闭UI层数据库
        MSBaseApplication.getInstance().closeDbHelper();

    }

    private void chooseIMG(IConversationContext iConversationContext) {
        String[] permissionStr = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
//        String permissionStr = Manifest.permission.READ_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= 33) {
//            permissionStr = Manifest.permission.READ_MEDIA_IMAGES;
            permissionStr = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO};
        }
        String desc = String.format(iConversationContext.getChatActivity().getString(R.string.album_permissions_desc), iConversationContext.getChatActivity().getString(R.string.app_name));
        MSPermissions.getInstance().checkPermissions(new MSPermissions.IPermissionResult() {
            @Override
            public void onResult(boolean result) {
                ChooseMimeType mimeType = ChooseMimeType.img;
                if (result) {
                    Object isRegisterVideo = EndpointManager.getInstance().invoke("is_register_video", null);
                    if (isRegisterVideo instanceof Boolean) {
                        boolean isRegister = (boolean) isRegisterVideo;
                        if (isRegister) {
                            mimeType = ChooseMimeType.all;
                        }
                    }
                    GlideUtils.getInstance().chooseIMG(iConversationContext.getChatActivity(), 9, true, mimeType, true, new GlideUtils.ISelectBack() {
                        @Override
                        public void onBack(List<ChooseResult> paths) {
                            if (paths.size() == 1 && paths.get(0).model == ChooseResultModel.video) {
//                                EndpointManager.getInstance().invoke("videoCompress",paths.get(0).path);
                                MSVideoContent videoContent = new MSVideoContent();
                                videoContent.coverLocalPath = MSMediaFileUtils.getInstance().getVideoCover(paths.get(0).path);
                                videoContent.localPath = paths.get(0).path;
                                videoContent.second = MSMediaFileUtils.getInstance().getVideoTime(paths.get(0).path) / 1000;
                                videoContent.size = MSFileUtils.getInstance().getFileSize(paths.get(0).path);
                                iConversationContext.sendMessage(videoContent);
                                return;
                            }

                            for (int i = 0, size = paths.size(); i < size; i++) {
                                String path = paths.get(i).path;
                                if (paths.get(i).model == ChooseResultModel.video) {
                                    MSVideoContent videoContent = new MSVideoContent();
                                    videoContent.coverLocalPath = MSMediaFileUtils.getInstance().getVideoCover(path);
                                    videoContent.localPath = path;
                                    videoContent.second = MSMediaFileUtils.getInstance().getVideoTime(path) / 1000;
                                    videoContent.size = MSFileUtils.getInstance().getFileSize(path);
                                    iConversationContext.sendMessage(videoContent);
                                } else {
                                    if (MSFileUtils.getInstance().isGif(path)) {
                                        Object isRegisterSticker = EndpointManager.getInstance().invoke("is_register_sticker", null);
                                        if (isRegisterSticker instanceof Boolean) {
                                            MSGifContent mGifContent = new MSGifContent();
                                            mGifContent.format = "gif";
                                            mGifContent.localPath = path;
                                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                                            if (bitmap != null) {
                                                mGifContent.height = bitmap.getHeight();
                                                mGifContent.width = bitmap.getWidth();
                                            }
                                            iConversationContext.sendMessage(mGifContent);
                                            return;
                                        }
                                    }
                                    MSImageContent imageContent = new MSImageContent(path);
                                    iConversationContext.sendMessage(imageContent);

                                }
                            }
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                }
            }

            @Override
            public void clickResult(boolean isCancel) {
            }
        }, iConversationContext.getChatActivity(), desc, permissionStr);
    }

    public interface IShowChatConfirm {
        void onBack(@NonNull List<MSChannel> list, @NonNull List<MSMessageContent> messageContentList);
    }

    public void showChatConfirmDialog(@NonNull Context context, @NonNull List<MSChannel> list, @NonNull List<MSMessageContent> messageContentList, final IShowChatConfirm iShowChatConfirm) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_confirm_dialog_view, null, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        AvatarView avatarView = view.findViewById(R.id.avatarView);
        TextView nameTv = view.findViewById(R.id.nameTv);
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView contentTv = view.findViewById(R.id.contentTv);
        if (list.size() == 1) {
            avatarView.showAvatar(list.get(0));
            String showName = list.get(0).channelRemark;
            if (TextUtils.isEmpty(showName)) showName = list.get(0).channelName;
            if (list.get(0).channelID.equals(MSSystemAccount.system_file_helper)) {
                showName = context.getString(R.string.ms_file_helper);
            }
            if (list.get(0).channelID.equals(MSSystemAccount.system_team)) {
                showName = context.getString(R.string.ms_system_notice);
            }
            nameTv.setText(showName);
            recyclerView.setVisibility(View.GONE);
            avatarView.setVisibility(View.VISIBLE);
            nameTv.setVisibility(View.VISIBLE);
        } else {
            class AvatarViewHolder extends RecyclerView.ViewHolder {
                final AvatarView avatarView;

                public AvatarViewHolder(@NonNull View itemView) {
                    super(itemView);
                    avatarView = itemView.findViewWithTag("avatar");
                }
            }
            recyclerView.setLayoutManager(new GridLayoutManager(context, 5));
            recyclerView.setAdapter(new RecyclerView.Adapter<AvatarViewHolder>() {
                @NonNull
                @Override
                public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    LinearLayout view1 = new LinearLayout(parent.getContext());
                    AvatarView avatarView1 = new AvatarView(parent.getContext());
                    avatarView1.setTag("avatar");
                    view1.addView(avatarView1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 5, 5, 5, 5));
                    return new AvatarViewHolder(view1);
                }

                @Override
                public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
                    holder.avatarView.setSize(40);
                    holder.avatarView.showAvatar(list.get(position));
                }

                @Override
                public int getItemCount() {
                    return list.size();
                }
            });
            nameTv.setVisibility(View.GONE);
            avatarView.setVisibility(View.GONE);
            contentTv.setVisibility(View.GONE);
        }

        if (messageContentList.size() == 1) {
            MSMessageContent messageContent = messageContentList.get(0);
            if (messageContent.type == MSContentType.MS_IMAGE) {
                MSImageContent imgMsgModel = (MSImageContent) messageContent;
                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                int[] ints = ImageUtils.getInstance().getImageWidthAndHeightToTalk(imgMsgModel.width, imgMsgModel.height);
                layoutParams.height = ints[1];
                layoutParams.width = ints[0];
                imageView.setLayoutParams(layoutParams);
                String showUrl;
                if (!TextUtils.isEmpty(imgMsgModel.localPath)) {
                    showUrl = imgMsgModel.localPath;
                    File file = new File(showUrl);
                    if (!file.exists()) {
                        //如果本地文件被删除就显示网络图片
                        showUrl = MSApiConfig.getShowUrl(imgMsgModel.url);
                    }
                } else {
                    showUrl = MSApiConfig.getShowUrl(imgMsgModel.url);
                }
                GlideUtils.getInstance().showImg(context, showUrl, ints[0], ints[1], imageView);
                imageView.setVisibility(View.VISIBLE);
                contentTv.setVisibility(View.GONE);
            } else {
                String content = messageContent.getDisplayContent();
                if (messageContent.type == MSContentType.MS_CARD) {
                    MSCardContent MSCardContent = (MSCardContent) messageContent;
                    content = content + MSCardContent.name;
                }
                contentTv.setText(content);
                imageView.setVisibility(View.GONE);
                contentTv.setVisibility(View.VISIBLE);
            }
        } else {
            imageView.setVisibility(View.GONE);
            contentTv.setVisibility(View.VISIBLE);
            contentTv.setText(String.format(context.getString(R.string.item_forward_count), messageContentList.size()));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.send_to));

        builder.setView(view);
        builder.setPositiveButton(context.getString(R.string.sure), (dialog, which) -> iShowChatConfirm.onBack(list, messageContentList));
        builder.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> {

        });

        AlertDialog dialog = builder.create();
        dialog.setBlurParams(1f, true, true);
        dialog.show();
        TextView sureTv = (TextView) dialog.getButton(Dialog.BUTTON_POSITIVE);
        sureTv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

    }
}
