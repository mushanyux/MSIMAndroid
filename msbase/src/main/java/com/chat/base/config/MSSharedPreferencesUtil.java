package com.chat.base.config;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.chat.base.MSBaseApplication;

public class MSSharedPreferencesUtil {
    // 创建一个写入器
    private final SharedPreferences mPreferences;
    private final SharedPreferences.Editor mEditor;

    @SuppressLint("CommitPrefEdits")
    private MSSharedPreferencesUtil(Context context) {
        String mTAG = "msSharedPreferences";
        mPreferences = context.getSharedPreferences(mTAG, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    enum SingletonEnum {
        INSTANCE;
        private final MSSharedPreferencesUtil util;

        SingletonEnum() {
            util = new MSSharedPreferencesUtil(MSBaseApplication.getInstance().application);
        }

        public MSSharedPreferencesUtil getInstance() {
            return util;
        }
    }


    public static MSSharedPreferencesUtil getInstance() {
        return SingletonEnum.INSTANCE.getInstance();
    }

    public void putSPWithUID(String key, String value) {
        this.putSP(MSConfig.getInstance().getUid() + "_" + key, value);
    }

    public String getSPWithUID(String key) {
        return getSP(MSConfig.getInstance().getUid() + "_" + key);
    }

    // 存入数据
    public void putSP(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    // 获取数据
    public String getSP(String key) {
        return mPreferences.getString(key, "");
    }

    public String getSP(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    public void putBooleanWithUID(String key, boolean value) {
        this.putBoolean(MSConfig.getInstance().getUid() + "_" + key, value);
    }

    public boolean getBooleanWithUID(String key) {
        return this.getBoolean(MSConfig.getInstance().getUid() + "_" + key);
    }

    public void putBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    // 获取数据
    public boolean getBoolean(String key) {
        return getBoolean(key, true);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mPreferences.getBoolean(key, defValue);
    }

    public void putIntWithUID(String key, int value) {
        this.putInt(MSConfig.getInstance().getUid() + "_" + key, value);
    }

    public int getIntWithUID(String key) {
        return getInt(MSConfig.getInstance().getUid() + "_" + key);
    }

    public void putInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    public int getInt(String key) {
        return mPreferences.getInt(key, 0);
    }

    public float getFloat(String key) {
        return mPreferences.getFloat(key, 0.0f);
    }

    public void putFloat(String key, float value) {
        mEditor.putFloat(key, value);
        mEditor.commit();
    }

    public float getFloat(String key, float defValue) {
        return mPreferences.getFloat(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return mPreferences.getInt(key, defValue);
    }

    public void putLongWithUID(String key, long value) {
        this.putLong(MSConfig.getInstance().getUid() + "_" + key, value);
    }

    public long getLongWithUID(String key) {
        return this.getLong(MSConfig.getInstance().getUid() + "_" + key);
    }

    public void putLong(String key, long value) {
        mEditor.putLong(key, value);
        mEditor.commit();
    }

    public long getLong(String key) {
        return mPreferences.getLong(key, 0);
    }
}
