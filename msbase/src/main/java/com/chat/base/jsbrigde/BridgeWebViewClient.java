package com.chat.base.jsbrigde;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chat.base.MSBaseApplication;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 如果要自定义WebViewClient必须要集成此类
 */
public class BridgeWebViewClient extends WebViewClient {
    private final BridgeWebView webView;

    public BridgeWebViewClient(BridgeWebView webView) {
        this.webView = webView;
    }

    @Override
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        sslErrorHandler.proceed();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            url = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            webView.handlerReturnData(url);
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            webView.flushMessageQueue();
            return true;
        } else {
            if (url.startsWith("alipays")) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MSBaseApplication.getInstance().getContext().startActivity(intent);
                    return true;
                } catch (Exception e) {
                    return this.onCustomShouldOverrideUrlLoading(url) || super.shouldOverrideUrlLoading(view, url);
                }
            } else {
                return this.onCustomShouldOverrideUrlLoading(url) || super.shouldOverrideUrlLoading(view, url);
            }
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);

        if (webView.getStartupMessage() != null) {
            for (Message m : webView.getStartupMessage()) {
                webView.dispatchMessage(m);
            }
            webView.setStartupMessage(null);
        }

        onCustomPageFinishd(view, url);
    }

    protected boolean onCustomShouldOverrideUrlLoading(String url) {
        return false;
    }


    protected void onCustomPageFinishd(WebView view, String url) {

    }
}