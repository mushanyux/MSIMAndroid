package com.chat.scan;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chat.base.act.MSWebViewActivity;
import com.chat.base.base.MSBaseModel;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSConfig;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.endpoint.entity.ScanResultMenu;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSToastUtils;
import com.chat.scan.entity.ScanResult;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

/**
 * 扫描处理
 */
class ScanUtils extends MSBaseModel {

    private IHandleScanResult iHandleScanResult;

    private ScanUtils() {

    }

    private static class ScanUtilsBinder {
        static final ScanUtils scanUtils = new ScanUtils();
    }

    static ScanUtils getInstance() {
        return ScanUtilsBinder.scanUtils;
    }

    void handleScanResult(AppCompatActivity activity, String result, @NonNull final IHandleScanResult iHandleScanResult) {
        this.iHandleScanResult = iHandleScanResult;
        try {
            if (result.startsWith("HTTP") || result.startsWith("http") || result.startsWith("www") || result.startsWith("WWW")) {
                URL resultURL = new URL(result);
                URL baseURL = new URL(MSApiConfig.baseUrl + "qrcode/");
                if (resultURL.getHost().equals(baseURL.getHost()) && resultURL.getPath().contains(baseURL.getPath())) {
                    requestScanResult(activity, result);
                } else {
                    iHandleScanResult.showWebView(result);
                }
            } else if (result.startsWith("mtp://")) {
            } else {
                iHandleScanResult.showOtherContent(result);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void requestScanResult(AppCompatActivity activity, String url) {
        request(createService(ScanService.class).getScanResult(url), new IRequestResultListener<>() {
            @Override
            public void onSuccess(ScanResult result) {
                handleResult(activity, result);
            }

            @Override
            public void onFail(int code, String msg) {
                MSToastUtils.getInstance().showToast(msg);
            }
        });
    }

    interface IHandleScanResult {
        void showOtherContent(String content);

        void showWebView(String url);

        void dismissView();
    }

    private void handleResult(AppCompatActivity activity, ScanResult result) {
        if (result.forward.equals("h5")) {
            Intent intent = new Intent(MSScanApplication.getInstance().mContext.get(), MSWebViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("url", String.valueOf(result.data.get("url")));
            MSScanApplication.getInstance().mContext.get().startActivity(intent);
            iHandleScanResult.dismissView();
        } else {
            String type = result.type;
            JSONObject dataJson = new JSONObject(result.data);
            if (type.equals("group")) {
                if (dataJson.has("group_no")) {
                    String group_no = dataJson.optString("group_no");
                    MSChannelMember mChannelMember = MSIM.getInstance().getChannelMembersManager().getMember(group_no, MSChannelType.GROUP, MSConfig.getInstance().getUid());
                    if (mChannelMember != null) {
                        if (mChannelMember.isDeleted == 0) {
                            ChatViewMenu chatViewMenu = new ChatViewMenu(activity, group_no, MSChannelType.GROUP, 0, true);
                            EndpointManager.getInstance().invoke(EndpointSID.chatView, chatViewMenu);
                            iHandleScanResult.dismissView();
                        } else {
                            MSToastUtils.getInstance().showToast(activity
                                    .getString(R.string.scan_remove_group));
                        }
                    } else {
                        // 加入群聊
                    }
                }

            } else {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("type", type);
                hashMap.put("data", dataJson);
                List<ScanResultMenu> list = EndpointManager.getInstance().invokes(EndpointCategory.msScan, hashMap);
                if (MSReader.isNotEmpty(list)) {
                    for (int i = 0, size = list.size(); i < size; i++) {
                        boolean canHandle = list.get(i).iResultClick.invoke(hashMap);
                        if (canHandle) {
                            iHandleScanResult.dismissView();
                            break;
                        }
                    }
                }
            }
        }
    }
}
