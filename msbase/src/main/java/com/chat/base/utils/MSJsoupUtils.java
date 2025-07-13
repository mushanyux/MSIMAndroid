package com.chat.base.utils;

import android.text.TextUtils;

import com.chat.base.config.MSSharedPreferencesUtil;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSMsg;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MSJsoupUtils {
    private MSJsoupUtils() {
    }

    private static class JsoupUtilsBinder {
        final static MSJsoupUtils jsoup = new MSJsoupUtils();
    }

    public static MSJsoupUtils getInstance() {
        return JsoupUtilsBinder.jsoup;
    }

    public void getURLContent(String url, String clientMsgNo) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(clientMsgNo)) return;

        Observable.create((ObservableOnSubscribe<MSURLContent>) emitter -> {
            String tempURL = url;
            if (url.startsWith("www") || url.startsWith("WWW")) {
                tempURL = "http://" + url;
            }
            Document document = Jsoup.connect(tempURL).get();
            if (document != null) {
                String title = document.head().getElementsByTag("title").text();
                Elements elements = document.head().getElementsByTag("meta");
                String htmlContent = "";
                String coverURL = "";
                for (Element element : elements) {
                    String name = element.attr("name");
                    String content = element.attr("content");
                    if (name.equals("description")) {
                        htmlContent = content;
                        if (!TextUtils.isEmpty(coverURL))
                            break;
                    }
                    String property = element.attr("property");
                    if (property.equals("og:image")) {
                        coverURL = content;
                        if (!TextUtils.isEmpty(htmlContent)) {
                            break;
                        }
                    }
                }
                MSURLContent msurlContent = new MSURLContent();
                msurlContent.content = htmlContent;
                msurlContent.title = title;
                msurlContent.url = url;
                msurlContent.coverURL = coverURL;
                emitter.onNext(msurlContent);
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe(new Observer<MSURLContent>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull MSJsoupUtils.MSURLContent msUrlContent) {
                if (!TextUtils.isEmpty(msUrlContent.title) && !TextUtils.isEmpty(msUrlContent.content)) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("title", msUrlContent.title);
                        jsonObject.put("content", msUrlContent.content);
                        jsonObject.put("coverURL", msUrlContent.coverURL);
                        jsonObject.put("logo", msUrlContent.url + "/favicon.ico");
                        jsonObject.put("expirationTime", MSTimeUtils.getInstance().getCurrentSeconds());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MSMsg msMsg = MSIM.getInstance().getMsgManager().getWithClientMsgNO(clientMsgNo);
                    if (msMsg != null) {
                        MSIM.getInstance().getMsgManager().setRefreshMsg(msMsg, true);
                    }
                    MSSharedPreferencesUtil.getInstance().putSP(msUrlContent.url, jsonObject.toString());
                }

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private static class MSURLContent {
        public String title;
        public String url;
        public String content;
        public String coverURL;
    }
}
