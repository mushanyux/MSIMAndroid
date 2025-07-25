package com.chat.base.net;

import android.os.Build;
import android.text.TextUtils;

import com.chat.base.MSBaseApplication;
import com.chat.base.config.MSConfig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 公共请求参数
 */
public class CommonRequestParamInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        Map<String, String> commonParams = getCommonParams();
        for (Map.Entry<String, String> entry : commonParams.entrySet()) {
            if (!TextUtils.isEmpty(entry.getValue())) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        request = builder.build();
        return chain.proceed(request);
    }

    private Map<String, String> getCommonParams() {
        Map<String, String> mCommonParams = new HashMap<>();
        mCommonParams.put("token", MSConfig.getInstance().getToken());
        mCommonParams.put("model", Build.MODEL);
        mCommonParams.put("os", "Android");
        mCommonParams.put("appid", MSBaseApplication.getInstance().appID);
        mCommonParams.put("version", MSBaseApplication.getInstance().versionName);
        mCommonParams.put("package", MSBaseApplication.getInstance().packageName);
        return mCommonParams;
    }
}
