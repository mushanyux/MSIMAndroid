package com.chat.scan;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.chat.base.act.MSWebViewActivity;
import com.chat.base.glide.ChooseMimeType;
import com.chat.base.glide.ChooseResult;
import com.chat.base.glide.GlideUtils;
import com.chat.base.ui.Theme;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSToastUtils;
import com.chat.base.utils.systembar.MSStatusBarUtils;
import com.google.zxing.Result;
import com.king.camera.scan.AnalyzeResult;
import com.king.camera.scan.CameraScan;
import com.king.camera.scan.analyze.Analyzer;
import com.king.zxing.BarcodeCameraScanActivity;
import com.king.zxing.DecodeConfig;
import com.king.zxing.DecodeFormatManager;
import com.king.zxing.analyze.MultiFormatAnalyzer;
import com.king.zxing.util.CodeUtils;

import java.util.List;

/**
 * 扫描页面
 */
public class MSScanActivity extends BarcodeCameraScanActivity {
    private AppCompatImageView ivFlash;

    protected void toggleTorchState() {
        if (getCameraScan() != null) {
            boolean isTorch = getCameraScan().isTorchEnabled();
            getCameraScan().enableTorch(!isTorch);
            ivFlash.setSelected(!isTorch);
        }
    }

    private void handleResult(String result) {
        ScanUtils.getInstance().handleScanResult(MSScanActivity.this, result, new ScanUtils.IHandleScanResult() {
            @Override
            public void showOtherContent(String content) {
                Intent intent = new Intent(MSScanActivity.this, MSScanOtherResultActivity.class);
                intent.putExtra("result", content);
                startActivity(intent);
                finish();
            }

            @Override
            public void showWebView(String url) {
                Intent intent = new Intent(MSScanActivity.this, MSWebViewActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
                finish();
            }

            @Override
            public void dismissView() {
                finish();
            }
        });
    }

    private void chooseIMG() {
        GlideUtils.getInstance().chooseIMG(this, 1, false, ChooseMimeType.img, false, new GlideUtils.ISelectBack() {
            @Override
            public void onBack(List<ChooseResult> paths) {
                if (MSReader.isNotEmpty(paths)) {
                    Bitmap bitmap = BitmapFactory.decodeFile(paths.get(0).path);
                    new Thread(() -> {
                        final String result = CodeUtils.parseCode(bitmap);
                        runOnUiThread(() -> {
                            if (TextUtils.isEmpty(result)) {
                                MSToastUtils.getInstance().showToast(getString(R.string.ms_scan_module_scan_no_result));
                                return;
                            }
                            handleResult(result);
                        });
                    }).start();
                }
            }

            @Override
            public void onCancel() {

            }
        });
    }


    @Override
    public void initCameraScan(@NonNull CameraScan<Result> cameraScan) {
        super.initCameraScan(cameraScan);
        // 根据需要设置CameraScan相关配置
        cameraScan.setPlayBeep(true);
    }

    @Nullable
    @Override
    public Analyzer<Result> createAnalyzer() {
        //初始化解码配置
        DecodeConfig decodeConfig = new DecodeConfig();
        decodeConfig.setHints(DecodeFormatManager.QR_CODE_HINTS)//如果只有识别二维码的需求，这样设置效率会更高，不设置默认为DecodeFormatManager.DEFAULT_HINTS
                .setFullAreaScan(false)//设置是否全区域识别，默认false
                .setAreaRectRatio(0.8f)//设置识别区域比例，默认0.8，设置的比例最终会在预览区域裁剪基于此比例的一个矩形进行扫码识别
                .setAreaRectVerticalOffset(0)//设置识别区域垂直方向偏移量，默认为0，为0表示居中，可以为负数
                .setAreaRectHorizontalOffset(0);//设置识别区域水平方向偏移量，默认为0，为0表示居中，可以为负数
        // BarcodeCameraScanActivity默认使用的MultiFormatAnalyzer，这里也可以改为使用QRCodeAnalyzer
        return new MultiFormatAnalyzer(decodeConfig);
    }

    /**
     * 布局ID；通过覆写此方法可以自定义布局
     *
     * @return 布局ID
     */
    @Override
    public int getLayoutId() {
        toggleStatusBarMode();
        setStatusBarColor(R.color.black);
        return R.layout.act_scan_layout;
    }

    @Override
    public void initUI() {
        super.initUI();
        ivFlash = findViewById(R.id.ivFlash);
        initListener();
    }

    @Override
    public void onScanResultCallback(@NonNull AnalyzeResult<Result> result) {
        // 停止分析
        getCameraScan().setAnalyzeImage(false);
        // 返回结果
        handleResult(result.getResult().getText());
    }

    protected void setStatusBarColor(int color) {
        Window window = getWindow();
        if (window == null || color == -1) return;
        MSStatusBarUtils.transparentStatusBar(window);
        MSStatusBarUtils.setStatusBarColor(window, ContextCompat.getColor(this, color), 0);
        if (!Theme.getDarkModeStatus(this))
            MSStatusBarUtils.setDarkMode(window);
        else MSStatusBarUtils.setLightMode(window);
    }

    protected void toggleStatusBarMode() {
        Window window = getWindow();
        if (window == null) return;
        MSStatusBarUtils.transparentStatusBar(window);
        if (!Theme.getDarkModeStatus(this))
            MSStatusBarUtils.setDarkMode(window);
        else MSStatusBarUtils.setLightMode(window);
    }

    private void initListener() {
        ivFlash.setOnClickListener(v -> toggleTorchState());
        findViewById(R.id.backIv).setOnClickListener(v -> finish());
        findViewById(R.id.rightIV).setOnClickListener(v -> chooseIMG());
    }
}
