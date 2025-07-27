package com.chat.base.common;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.chat.base.R;
import com.chat.base.MSBaseApplication;
import com.chat.base.base.MSBaseModel;
import com.chat.base.config.MSConfig;
import com.chat.base.config.MSConstants;
import com.chat.base.config.MSSharedPreferencesUtil;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.entity.AppModule;
import com.chat.base.entity.AppVersion;
import com.chat.base.entity.ChannelInfoEntity;
import com.chat.base.entity.MSAPPConfig;
import com.chat.base.entity.MSChannelState;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.DispatchQueuePool;
import com.chat.base.utils.MSDeviceUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSToastUtils;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelExtras;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MSCommonModel extends MSBaseModel {
    private final DispatchQueuePool dispatchQueuePool = new DispatchQueuePool(3);

    private MSCommonModel() {
    }

    private static class CommonModelBinder {
        final static MSCommonModel model = new MSCommonModel();
    }

    public static MSCommonModel getInstance() {
        return CommonModelBinder.model;
    }

    public void getAppNewVersion(boolean isShowToast, final IAppNewVersion iAppNewVersion) {
        String v = MSDeviceUtils.getInstance().getVersionName(MSBaseApplication.getInstance().getContext());
        request(createService(MSCommonService.class).getAppNewVersion(v), new IRequestResultListener<AppVersion>() {
            @Override
            public void onSuccess(AppVersion result) {
                if ((result == null || TextUtils.isEmpty(result.download_url)) && isShowToast) {
                    MSToastUtils.getInstance().showToastNormal(MSBaseApplication.getInstance().getContext().getString(R.string.is_new_version));
                } else {
                    iAppNewVersion.onNewVersion(result);
                }
            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    public interface IAppNewVersion {
        void onNewVersion(AppVersion version);
    }

    public interface IAppConfig {
        void onResult(int code, String msg, MSAPPConfig msappConfig);
    }

    public void getAppConfig(IAppConfig iAppConfig) {
        request(createService(MSCommonService.class).getAppConfig(), new IRequestResultListener<>() {
            @Override
            public void onSuccess(MSAPPConfig result) {
                MSConfig.getInstance().saveAppConfig(result);
                if (iAppConfig != null) {
                    iAppConfig.onResult(HttpResponseCode.success, "", result);
                }
            }

            @Override
            public void onFail(int code, String msg) {
                if (iAppConfig != null) {
                    iAppConfig.onResult(code, msg, null);
                }
            }
        });
    }

    public void getChannelState(String channelID, byte channelType, final IChannelState iChannelState) {
        request(createService(MSCommonService.class).getChannelState(channelID, channelType), new IRequestResultListener<MSChannelState>() {
            @Override
            public void onSuccess(MSChannelState result) {
                iChannelState.onResult(result);
            }

            @Override
            public void onFail(int code, String msg) {
                iChannelState.onResult(null);
            }
        });
    }

    public interface IChannelState {
        void onResult(MSChannelState channelState);
    }


    public void getChannel(String channelID, byte channelType, IGetChannel iGetChannel) {
        dispatchQueuePool.execute(() -> request(createService(MSCommonService.class).getChannel(channelID, channelType), new IRequestResultListener<ChannelInfoEntity>() {
            @Override
            public void onSuccess(ChannelInfoEntity result) {
                saveChannel(result);
                if (iGetChannel != null) {
                    AndroidUtilities.runOnUIThread(() -> iGetChannel.onResult(HttpResponseCode.success, "", result));
                }
            }

            @Override
            public void onFail(int code, String msg) {
                if (iGetChannel != null) {
                    AndroidUtilities.runOnUIThread(() -> iGetChannel.onResult(code, msg, null));

                }
            }
        }));

    }

    private void saveChannel(ChannelInfoEntity entity) {
        HashMap<String, Object> hashMap = null;
        MSChannel msChannel = new MSChannel(entity.channel.channel_id, entity.channel.channel_type);
        MSChannel localChannel = MSIM.getInstance().getChannelManager().getChannel(entity.channel.channel_id, entity.channel.channel_type);
        boolean isRefreshContacts = false;
        if (localChannel != null && !TextUtils.isEmpty(localChannel.channelID)) {
            msChannel.avatarCacheKey = localChannel.avatarCacheKey;
            hashMap = localChannel.localExtra;

            if (msChannel.follow != entity.follow || msChannel.status != entity.status)
                isRefreshContacts = true;
        }
        if (hashMap == null)
            hashMap = new HashMap<>();

        msChannel.channelName = entity.name;
        msChannel.avatar = entity.logo;
        msChannel.channelRemark = entity.remark;
        msChannel.status = entity.status;
        msChannel.online = entity.online;
        msChannel.lastOffline = entity.last_offline;
        msChannel.receipt = entity.receipt;
        msChannel.robot = entity.robot;
        msChannel.category = entity.category;
        msChannel.top = entity.stick;
        msChannel.mute = entity.mute;
        msChannel.showNick = entity.show_nick;
        msChannel.follow = entity.follow;
        msChannel.save = entity.save;
        msChannel.forbidden = entity.forbidden;
        msChannel.invite = entity.invite;
        msChannel.flame = entity.flame;
        msChannel.flameSecond = entity.flame_second;
        msChannel.deviceFlag = entity.device_flag;
        if (entity.parent_channel != null) {
            msChannel.parentChannelID = entity.parent_channel.channel_id;
            msChannel.parentChannelType = entity.parent_channel.channel_type;
        }
        msChannel.remoteExtraMap = (HashMap) entity.extra;
        hashMap.put(MSChannelExtras.beDeleted, entity.be_deleted);
        hashMap.put(MSChannelExtras.beBlacklist, entity.be_blacklist);
        hashMap.put(MSChannelExtras.notice, entity.notice);
        msChannel.localExtra = hashMap;
        MSIM.getInstance().getChannelManager().saveOrUpdateChannel(msChannel);
        if (isRefreshContacts) {
            EndpointManager.getInstance().invoke(MSConstants.refreshContacts, null);
        }
    }

    public interface IGetChannel {
        void onResult(int code, String msg, ChannelInfoEntity entity);
    }

    public void getAppModule(@NotNull final IAppModule iAppModule) {
        request(createService(MSCommonService.class).getAppModule(), new IRequestResultListener<List<AppModule>>() {
            @Override
            public void onSuccess(List<AppModule> result) {
                String text = MSSharedPreferencesUtil.getInstance().getSPWithUID("app_module");
                List<AppModule> localSavedAppModule = new ArrayList<>();
                if (!TextUtils.isEmpty(text)) {
                    localSavedAppModule = JSON.parseArray(text, AppModule.class);
                }
                List<AppModule> tempList = new ArrayList<>();
                if (MSReader.isNotEmpty(result)) {
                    for (AppModule item : result) {
                        AppModule m = new AppModule();
                        m.setName(item.getName());
                        m.setDesc(item.getDesc());
                        m.setSid(item.getSid());
                        m.setStatus(item.getStatus());
                        if (item.getStatus() == 2) {
                            m.setChecked(true);
                        } else if (item.getStatus() == 0) {
                            m.setChecked(false);
                        } else {
                            if (MSReader.isNotEmpty(localSavedAppModule)) {
                                for (AppModule temp : localSavedAppModule) {
                                    if (temp.getSid().equals(item.getSid())) {
                                        m.setChecked(temp.getChecked());
                                    }
                                }
                            } else {
                                m.setChecked(false);
                            }
                        }
                        tempList.add(m);
                    }
                }
                String json = JSON.toJSONString(tempList);
                MSSharedPreferencesUtil.getInstance().putSPWithUID("app_module", json);

                iAppModule.onResult(HttpResponseCode.success, "", tempList);
            }

            @Override
            public void onFail(int code, String msg) {
                iAppModule.onResult(code, msg, null);
            }
        });
    }

    public interface IAppModule {
        void onResult(int code, String msg, List<AppModule> list);
    }
}
