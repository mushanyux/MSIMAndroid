package com.chat.base.utils.language;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.core.os.ConfigurationCompat;
import androidx.core.os.LocaleListCompat;

import com.chat.base.R;
import com.chat.base.config.MSSharedPreferencesUtil;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * 多语言切换的帮助类
 */
public class MSMultiLanguageUtil {

    private static final String TAG = "MultiLanguageUtil";

    private WeakReference<Context> mContext;
    private static final String SAVE_LANGUAGE = "save_language";

    private static class MultiLanguageUtilBinder {
        private final static MSMultiLanguageUtil util = new MSMultiLanguageUtil();
    }

    public static MSMultiLanguageUtil getInstance() {
        return MultiLanguageUtilBinder.util;
    }

    public void init(Context context) {
        mContext = new WeakReference<>(context);
    }

    private MSMultiLanguageUtil() {
    }

    /**
     * 设置语言
     */
    public void setConfiguration() {
        if (mContext != null) {
            Locale targetLocale = getLanguageLocale();
            Configuration configuration = mContext.get().getResources().getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(targetLocale);
            } else {
                configuration.locale = targetLocale;
            }
            Resources resources = mContext.get().getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            resources.updateConfiguration(configuration, dm);//语言更换生效的代码!
        }

    }

    //如果不是英文、简体中文、繁体中文，默认返回简体中文
    private Locale getLanguageLocale() {
        if (mContext == null || mContext.get() == null) return getSysLocale();
        int languageType = MSSharedPreferencesUtil.getInstance().getInt(MSMultiLanguageUtil.SAVE_LANGUAGE, 0);
        if (languageType == MSLanguageType.LANGUAGE_FOLLOW_SYSTEM) {
            return getSysLocale();
        } else if (languageType == MSLanguageType.LANGUAGE_EN) {
            return Locale.ENGLISH;
        } else if (languageType == MSLanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
            return Locale.SIMPLIFIED_CHINESE;
        } else if (languageType == MSLanguageType.LANGUAGE_CHINESE_TRADITIONAL) {
            return Locale.TRADITIONAL_CHINESE;
        }
        getSystemLanguage(getSysLocale());
        Log.e(TAG, "getLanguageLocale" + languageType + languageType);
        return Locale.SIMPLIFIED_CHINESE;
    }

    private String getSystemLanguage(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    //以上获取方式需要特殊处理一下
    public Locale getSysLocale() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                LocaleListCompat listCompat= ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration());
                locale= listCompat.get(0);
            }catch (Exception e){
                locale=LocaleList.getDefault().get(0);
            }
        } else {
            locale = Locale.getDefault();
        }
        return locale;
    }

    /**
     * 更新语言
     *
     * @param languageType
     */
    public void updateLanguage(int languageType) {
        MSSharedPreferencesUtil.getInstance().putInt(MSMultiLanguageUtil.SAVE_LANGUAGE, languageType);
        MSMultiLanguageUtil.getInstance().setConfiguration();
    }

    public String getLanguageName(Context context) {
        int languageType = MSSharedPreferencesUtil.getInstance().getInt(MSMultiLanguageUtil.SAVE_LANGUAGE, MSLanguageType.LANGUAGE_FOLLOW_SYSTEM);
        if (languageType == MSLanguageType.LANGUAGE_EN) {
            return mContext.get().getString(R.string.setting_language_english);
        } else if (languageType == MSLanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
            return mContext.get().getString(R.string.setting_simplified_chinese);
        } else if (languageType == MSLanguageType.LANGUAGE_CHINESE_TRADITIONAL) {
            return mContext.get().getString(R.string.setting_traditional_chinese);
        }
        return mContext.get().getString(R.string.setting_language_auto);
    }

    /**
     * 获取到用户保存的语言类型
     *
     * @return
     */
    public int getLanguageType() {
        int languageType = MSSharedPreferencesUtil.getInstance().getInt(MSMultiLanguageUtil.SAVE_LANGUAGE, MSLanguageType.LANGUAGE_FOLLOW_SYSTEM);
        if (languageType == MSLanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
            return MSLanguageType.LANGUAGE_CHINESE_SIMPLIFIED;
        } else if (languageType == MSLanguageType.LANGUAGE_CHINESE_TRADITIONAL) {
            return MSLanguageType.LANGUAGE_CHINESE_TRADITIONAL;
        } else if (languageType == MSLanguageType.LANGUAGE_FOLLOW_SYSTEM) {
            return MSLanguageType.LANGUAGE_FOLLOW_SYSTEM;
        }
        Log.e(TAG, "getLanguageType" + languageType);
        return languageType;
    }

    public Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createConfigurationResources(context);
        } else {
            MSMultiLanguageUtil.getInstance().setConfiguration();
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getInstance().getLanguageLocale();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }
}
