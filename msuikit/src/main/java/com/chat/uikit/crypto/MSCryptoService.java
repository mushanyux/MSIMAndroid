package com.chat.uikit.crypto;

import com.alibaba.fastjson.JSONObject;
import com.chat.uikit.enity.MSSignalData;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MSCryptoService {
    @POST("user/signal/getkey")
    Observable<MSSignalData> getUserSignalData(@Body JSONObject jsonObject);
}
