package com.chat.scan;

import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.scan.databinding.ActScanOtherResultLayoutBinding;

/**
 * 扫描其他内容
 */
public class MSScanOtherResultActivity extends MSBaseActivity<ActScanOtherResultLayoutBinding> {
    @Override
    protected ActScanOtherResultLayoutBinding getViewBinding() {
        return ActScanOtherResultLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.ms_scan_module_other_result);
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView() {
        String result = getIntent().getStringExtra("result");
        msVBinding.resultTv.setText(result);
    }

    @Override
    protected void initListener() {

    }
}
