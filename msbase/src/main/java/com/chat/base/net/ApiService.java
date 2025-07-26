package com.chat.base.net;

import com.chat.base.net.entity.UploadFileUrl;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ApiService {

    @GET
    Observable<UploadFileUrl> getUploadFileUrl(@Url String url);
}
