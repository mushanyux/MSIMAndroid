package com.chat.base.net;

/**
 * 请求返还监听
 */
public interface IRequestResultListener<T> {
    void onSuccess(T result);

    void onFail(int code, String msg);
}
