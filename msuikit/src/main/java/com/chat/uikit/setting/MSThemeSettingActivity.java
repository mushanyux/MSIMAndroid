package com.chat.uikit.setting;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.ui.Theme;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActDarkSettingLayoutBinding;

/**
 * 深色模式
 */
public class MSThemeSettingActivity extends MSBaseActivity<ActDarkSettingLayoutBinding> {

    private int type = 0;

    @Override
    protected ActDarkSettingLayoutBinding getViewBinding() {
        return ActDarkSettingLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.dark_night);
    }

    @Override
    protected void rightButtonClick() {
        super.rightButtonClick();
        showDialog(String.format(getString(R.string.dark_save_tips), getString(R.string.app_name)), index -> {
            if (index == 1) {
                saveType();
            }
        });
    }

    @Override
    protected String getRightBtnText(Button titleRightBtn) {
        return getString(R.string.sure);
    }

    @Override
    protected void initView() {
        String sp = Theme.getTheme();
        if (sp.equals(Theme.DARK_MODE)) {
            msVBinding.followSystemSwitch.setChecked(false);
            msVBinding.nightIv.setVisibility(View.VISIBLE);
            msVBinding.bottomView.setVisibility(View.VISIBLE);
            msVBinding.normalIv.setVisibility(View.INVISIBLE);
        } else if (sp.equals(Theme.LIGHT_MODE)) {
            msVBinding.followSystemSwitch.setChecked(false);
            msVBinding.nightIv.setVisibility(View.INVISIBLE);
            msVBinding.normalIv.setVisibility(View.VISIBLE);
            msVBinding.bottomView.setVisibility(View.VISIBLE);
        } else {
            msVBinding.followSystemSwitch.setChecked(true);
            msVBinding.nightIv.setVisibility(View.INVISIBLE);
            msVBinding.normalIv.setVisibility(View.VISIBLE);
            msVBinding.bottomView.setVisibility(View.GONE);
        }

    }

    @Override
    protected void initListener() {
        msVBinding.followSystemSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                msVBinding.bottomView.setVisibility(View.GONE);
                type = 0;
            } else {
                type = 1;
                msVBinding.bottomView.setVisibility(View.VISIBLE);
            }
        });
        msVBinding.darkLayout.setOnClickListener(v -> {
            type = 2;
            msVBinding.nightIv.setVisibility(View.VISIBLE);
            msVBinding.normalIv.setVisibility(View.INVISIBLE);
        });
        msVBinding.normalLayout.setOnClickListener(v -> {
            type = 1;
            msVBinding.nightIv.setVisibility(View.INVISIBLE);
            msVBinding.normalIv.setVisibility(View.VISIBLE);
        });
    }

    private void saveType() {
        String s = Theme.DEFAULT_MODE;
        if (type == 0) {
            s = Theme.DEFAULT_MODE;
        } else if (type == 1) {
            s = Theme.LIGHT_MODE;
        } else if (type == 2){
            s = Theme.DARK_MODE;
        }
        Theme.setTheme(s);
        finish();
    }

    @Override
    protected void resetTheme(boolean isDark) {
        super.resetTheme(isDark);
        msVBinding.contentLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.homeColor));
        msVBinding.topLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.layoutColor));
        msVBinding.normalLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.layoutColor));
        msVBinding.darkLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.layoutColor));
        msVBinding.normalTv.setTextColor(ContextCompat.getColor(this, R.color.colorDark));
        msVBinding.darkTv.setTextColor(ContextCompat.getColor(this, R.color.colorDark));
        msVBinding.systemTv.setTextColor(ContextCompat.getColor(this, R.color.colorDark));
        msVBinding.followSystemSwitch.invalidate();
    }
}
