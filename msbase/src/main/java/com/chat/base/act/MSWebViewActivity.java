package com.chat.base.act;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chat.base.R;
import com.chat.base.app.MSAppModel;
import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSBinder;
import com.chat.base.config.MSConfig;
import com.chat.base.databinding.ActWebvieiwLayoutBinding;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatChooseContacts;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.endpoint.entity.ChooseChatMenu;
import com.chat.base.entity.AppInfo;
import com.chat.base.entity.AuthInfo;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.glide.GlideUtils;
import com.chat.base.jsbrigde.CallBackFunction;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.ui.components.AvatarView;
import com.chat.base.ui.components.BottomSheet;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSLogUtils;
import com.chat.base.utils.MSToastUtils;
import com.google.gson.JsonObject;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSSendOptions;
import com.mushanyux.mushanim.msgmodel.MSTextContent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("JavascriptInterface")
public class MSWebViewActivity extends MSBaseActivity<ActWebvieiwLayoutBinding> {
    TextView titleTv;
    private final int FILE_CHOOSER_RESULT_CODE = 101;
    ValueCallback<Uri> mUploadMessage;
    ValueCallback<Uri[]> mUploadCallbackAboveL;
    private String channelID;
    private byte channelType;

    @Override
    protected ActWebvieiwLayoutBinding getViewBinding() {
        return ActWebvieiwLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        this.titleTv = titleTv;
    }

    @Override
    protected void initPresenter() {
        if (getIntent().hasExtra("channelID"))
            channelID = getIntent().getStringExtra("channelID");
        if (getIntent().hasExtra("channelType"))
            channelType = getIntent().getByteExtra("channelType", (byte) 0);
    }

    @Override
    protected int getBackResourceID(ImageView backIv) {
        return R.mipmap.ic_close_white;
    }

    @Override
    protected int getRightIvResourceId(ImageView imageView) {
        return R.mipmap.ic_ab_other;
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();

        List<PopupMenuItem> list = new ArrayList<>();
        list.add(new PopupMenuItem(getString(R.string.copy_url), R.mipmap.search_links, () -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", msVBinding.webView.getUrl());
            assert cm != null;
            cm.setPrimaryClip(mClipData);
            MSToastUtils.getInstance().showToastNormal(getString(R.string.copyed));
        }));
        list.add(new PopupMenuItem(getString(R.string.forward), R.mipmap.msg_forward, () -> {
            MSTextContent textContent = new MSTextContent(msVBinding.webView.getUrl());
            EndpointManager.getInstance().invoke(EndpointSID.showChooseChatView, new ChooseChatMenu(new ChatChooseContacts(new ChatChooseContacts.IChoose() {
                @Override
                public void onResult(List<MSChannel> list) {
                    for (MSChannel channel : list) {
                        MSSendOptions options = new MSSendOptions();
                        options.setting.receipt = channel.receipt;
                        MSIM.getInstance().getMsgManager().sendWithOptions(textContent, channel,options);
                    }
                }
            }), textContent));
        }));

        list.add(new PopupMenuItem(getString(R.string.refresh), R.mipmap.tool_rotate, () -> {
            msVBinding.webView.reload();
        }));
        list.add(new PopupMenuItem(getString(R.string.open_system_browser), R.mipmap.msg_openin, () -> {
            Uri uri = Uri.parse(msVBinding.webView.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }));
        ImageView rightIV = findViewById(R.id.titleRightIv);
        MSDialogUtils.getInstance().showScreenPopup(rightIV, list);
    }

    @Override
    protected void initView() {
        initWebViewSetting();
        String url = getIntent().getStringExtra("url");
        assert url != null;
        if (!url.startsWith("http") && !url.startsWith("HTTP") && !url.startsWith("file"))
            url = "http://" + url;
//        msVBinding.webView.loadUrl("file:///android_asset/web/report.html");
        if (url.equals(MSApiConfig.baseWebUrl + "report.html")) {
            String ms_theme_pref = Theme.getTheme();
            url = String.format("%s?uid=%s&token=%s&mode=%s", url, MSConfig.getInstance().getUid(), MSConfig.getInstance().getToken(), ms_theme_pref);
        }
        Log.e("加载的URL", url);
        msVBinding.webView.loadUrl(url);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebViewSetting() {
        WebSettings webSettings = msVBinding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 设置支持javascript脚本
        webSettings.setUseWideViewPort(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//        webSettings.setAppCacheEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false); // 禁止保存表单
        webSettings.setDomStorageEnabled(true);
//        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        //webSettings.setAllowFileAccess(true);
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        webSettings.setAllowFileAccessFromFileURLs(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(0);
        }
        if (MSBinder.isDebug && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        //支持屏幕缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        msVBinding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // msVBinding.webView.setBackgroundColor(ContextCompat.getColor(this, R.color.homeColor));
    }

    @Override
    protected boolean supportSlideBack() {
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (msVBinding.webView.canGoBack()) {
                msVBinding.webView.goBack();
                return true;
            } else return super.onKeyDown(keyCode, event);
        } else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void initListener() {
        msVBinding.webView.registerHandler("quit", (var1, var2) -> {
            finish();
        });
        msVBinding.webView.registerHandler("auth", (data, function) -> {
            if (!TextUtils.isEmpty(data)) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String appId = jsonObject.optString("app_id");
                    if (!TextUtils.isEmpty(appId)) {
                        getAppInfo(appId, function);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
            Log.e("需要授权的信息", data);
        });
        msVBinding.webView.registerHandler("getChannel", (data, function) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("channelID", channelID);
            jsonObject.addProperty("channelType", channelType);
            function.onCallBack(jsonObject.toString());
        });
        msVBinding.webView.registerHandler("showConversation", (data, function) -> {
            if (!TextUtils.isEmpty(data)) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    String channelID = jsonObject.optString("channel_id");
                    byte channelType = (byte) jsonObject.optInt("channel_type");
                    EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(MSWebViewActivity.this, channelID, channelType, 0, true));
                    finish();
                } catch (JSONException e) {
                    MSLogUtils.e("显示最近会话页面错误");
                }
            }
        });

        msVBinding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView webView, String s) {
                super.onReceivedTitle(webView, s);
                if (!TextUtils.isEmpty(s) && !"about:blank".equals(s)) {
                    titleTv.setText(s);
                }
            }

            @Override
            public void onProgressChanged(WebView webView, int i) {
                super.onProgressChanged(webView, i);
                if (i > 99) {
                    msVBinding.progress.setVisibility(View.GONE);
//                    hideLoadingDialog();
                } else {
                    msVBinding.progress.setVisibility(View.VISIBLE);
                    msVBinding.progress.setProgress(i);
                }
            }

//            @Override
//            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
//                mUploadMessage = uploadMsg;
//                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                i.addCategory(Intent.CATEGORY_OPENABLE);
//                i.setType("*/*");
//                MSWebViewActivity.this.startActivityForResult(Intent.createChooser(i, "File Browser"), FILE_CHOOSER_RESULT_CODE);
//            }

            // For Android 5.0+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILE_CHOOSER_RESULT_CODE);
                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE
                || mUploadCallbackAboveL == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
    }


    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        msVBinding.webView.onPause();
        super.onPause();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        msVBinding.webView.onResume();
        super.onResume();
    }

    private void getAppInfo(String appId, CallBackFunction function) {
        MSAppModel.Companion.getInstance().getAppInfo(appId, (code, msg, appInfo) -> {
            if (code == HttpResponseCode.success) {
                authDialog(appInfo, function);
            } else {
                if (!TextUtils.isEmpty(msg)) {
                    showToast(msg);
                }
            }
        });
    }

    private void authDialog(AppInfo appInfo, CallBackFunction function) {
        View authView = LayoutInflater.from(this).inflate(R.layout.auth_dialog_layout, getViewBinding().webView, false);
        TextView appName = authView.findViewById(R.id.appNameTv);
        AvatarView appIV = authView.findViewById(R.id.appIV);
        TextView nameTv = authView.findViewById(R.id.nameTv);
        TextView descTv = authView.findViewById(R.id.descTv);
        AvatarView avatarView = authView.findViewById(R.id.avatarView);
        descTv.setText(String.format(getString(R.string.str_request_desc), getString(R.string.app_name)));
        appIV.setSize(30f);
        appName.setText(appInfo.getApp_name());
        GlideUtils.getInstance().showImg(this, MSApiConfig.getShowUrl(appInfo.getApp_logo()), appIV.imageView);
        avatarView.setSize(40f);
        MSChannel loginChannel = MSIM.getInstance().getChannelManager().getChannel(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL);
        avatarView.showAvatar(loginChannel);
        nameTv.setText(loginChannel.channelName);
        BottomSheet bottomSheet = new BottomSheet(this, true);
        bottomSheet.setCustomView(authView);
        authView.findViewById(R.id.cancelBtn).setOnClickListener(v -> {
            bottomSheet.setDelegate(null);
            bottomSheet.dismiss();
        });
        authView.findViewById(R.id.sureBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MSAppModel.Companion.getInstance().getAuthCode(appInfo.getApp_id(), new MSAppModel.IAuth() {
                    @Override
                    public void onResult(int code, @Nullable String msg, @Nullable AuthInfo authInfo) {
                        if (authInfo != null) {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("code", authInfo.getAuthcode());
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            function.onCallBack(json.toString());
                            bottomSheet.setDelegate(null);
                            bottomSheet.dismiss();
                        }
                    }
                });
            }
        });
        bottomSheet.setOpenNoDelay(false);
        bottomSheet.setDelegate(new BottomSheet.BottomSheetDelegateInterface() {

            @Override
            public void onOpenAnimationStart() {

            }

            @Override
            public void onOpenAnimationEnd() {

            }

            @Override
            public boolean canDismiss() {
                return false;
            }
        });
        bottomSheet.show();
    }
}
