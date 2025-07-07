package com.chat.base.config;

import android.text.TextUtils;

import com.chat.base.entity.UserInfoEntity;
import com.chat.base.entity.UserInfoSetting;
import com.chat.base.entity.MSAPPConfig;
import com.google.gson.Gson;

public class MSConfig {
    private MSConfig() {
    }

    private static class ConfigBinder {
        private static final MSConfig MS_CONFIG = new MSConfig();
    }

    public static MSConfig getInstance() {
        return ConfigBinder.MS_CONFIG;
    }

    public void setUid(String uid) {
        MSSharedPreferencesUtil.getInstance().putSP("ms_uid", uid);
    }

    public String getUid() {
        return MSSharedPreferencesUtil.getInstance().getSP("ms_uid");
    }

    public void setToken(String token) {
        MSSharedPreferencesUtil.getInstance().putSP("ms_token", token);
    }

    public String getToken() {
        return MSSharedPreferencesUtil.getInstance().getSP("ms_token");
    }

    public void setImToken(String imToken) {
        MSSharedPreferencesUtil.getInstance().putSP("ms_im_token", imToken);
    }

    public String getImToken() {
        return MSSharedPreferencesUtil.getInstance().getSP("ms_im_token");
    }

    public void setUserName(String name) {
        MSSharedPreferencesUtil.getInstance().putSP("ms_name", name);
    }

    public String getUserName() {
        return MSSharedPreferencesUtil.getInstance().getSP("ms_name");
    }

    public void clearInfo() {
        setUid("");
        setToken("");
        setImToken("");
        UserInfoEntity userInfoEntity = MSConfig.getInstance().getUserInfo();
        userInfoEntity.token = "";
        userInfoEntity.im_token = "";
        MSConfig.getInstance().saveUserInfo(userInfoEntity);
    }

    public void saveAppConfig(MSAPPConfig MSAPPConfig) {
        String json = new Gson().toJson(MSAPPConfig);
        MSSharedPreferencesUtil.getInstance().putSP("app_config", json);
    }

    public MSAPPConfig getAppConfig() {
        String json = MSSharedPreferencesUtil.getInstance().getSP("app_config");
        MSAPPConfig MSAPPConfig = null;
        if (!TextUtils.isEmpty(json)) {
            MSAPPConfig = new Gson().fromJson(json, MSAPPConfig.class);
        }
        if (MSAPPConfig == null) {
            MSAPPConfig = new MSAPPConfig();
        }
        return MSAPPConfig;
    }

    public void saveUserInfo(UserInfoEntity userInfoEntity) {
        String json = new Gson().toJson(userInfoEntity);
        MSSharedPreferencesUtil.getInstance().putSP("user_info", json);
    }

    public UserInfoEntity getUserInfo() {
        String json = MSSharedPreferencesUtil.getInstance().getSP("user_info");
        UserInfoEntity userInfoEntity = null;
        if (!TextUtils.isEmpty(json)) {
            userInfoEntity = new Gson().fromJson(json, UserInfoEntity.class);
        }
        if (userInfoEntity == null) {
            userInfoEntity = new UserInfoEntity();
        }
        if (userInfoEntity.setting == null)
            userInfoEntity.setting = new UserInfoSetting();
        return userInfoEntity;
    }
}
