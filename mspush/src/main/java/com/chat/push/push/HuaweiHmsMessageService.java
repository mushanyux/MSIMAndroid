package com.chat.push.push;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.chat.base.MSBaseApplication;
import com.chat.base.utils.MSDeviceUtils;
import com.chat.push.MSPushApplication;
import com.chat.push.service.PushModel;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

/**
 * 华为推送服务
 */
public class HuaweiHmsMessageService extends HmsMessageService {
    @Override
    public void onNewToken(String s, Bundle bundle) {
        super.onNewToken(s, bundle);
        if (!TextUtils.isEmpty(s)) {
            PushModel.getInstance().registerDeviceToken(s, MSPushApplication.getInstance().pushBundleID,"");
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e("收到透传消息",remoteMessage.getData());
    }
}
