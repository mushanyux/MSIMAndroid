package com.chat.uikit.crypto;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.chat.base.base.MSBaseModel;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.IRequestResultListener;
import com.chat.uikit.enity.MSSignalData;
import com.mushanyux.mushanim.entity.MSChannelType;

public class MSCryptoModel extends MSBaseModel {
    private MSCryptoModel() {
    }

    private static class CryptoModelBinder {
        final static MSCryptoModel model = new MSCryptoModel();
    }

    public static MSCryptoModel getInstance() {
        return CryptoModelBinder.model;
    }

    public void getUserKey(String uid, final @NonNull ISignalData iSignalData) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("channel_id", uid);
        jsonObject.put("channel_type", MSChannelType.PERSONAL);
        request(createService(MSCryptoService.class).getUserSignalData(jsonObject), new IRequestResultListener<MSSignalData>() {
            @Override
            public void onSuccess(MSSignalData result) {
                iSignalData.onResult(HttpResponseCode.success, "", result);
            }

            @Override
            public void onFail(int code, String msg) {
                iSignalData.onResult(code, msg, null);
            }
        });
    }

   public interface ISignalData {
        void onResult(int code, String msg, MSSignalData data);
    }
}
