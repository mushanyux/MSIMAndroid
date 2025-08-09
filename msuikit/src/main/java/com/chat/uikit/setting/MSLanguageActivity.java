package com.chat.uikit.setting;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.utils.language.MSLanguageType;
import com.chat.base.utils.language.MSMultiLanguageUtil;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActLanguageLayoutBinding;

/**
 * 多语言
 */
public class MSLanguageActivity extends MSBaseActivity<ActLanguageLayoutBinding> {

    int selectedLanguage = 0;

    @Override
    protected ActLanguageLayoutBinding getViewBinding() {
        return ActLanguageLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.language);
    }

    @Override
    protected String getRightBtnText(Button titleRightBtn) {
        return getString(R.string.str_save);
    }

    @Override
    protected void rightButtonClick() {
        super.rightButtonClick();
        MSMultiLanguageUtil.getInstance().updateLanguage(selectedLanguage);
        EndpointManager.getInstance().invoke("main_show_home_view",0);
        finish();
    }

    @Override
    protected void initView() {
        selectedLanguage = MSMultiLanguageUtil.getInstance().getLanguageType();
        setSelectedLanguage();
    }

    @Override
    protected void initListener() {
        msVBinding.autoLayout.setOnClickListener(v -> {
            selectedLanguage = MSLanguageType.LANGUAGE_FOLLOW_SYSTEM;
            setSelectedLanguage();
        });
        msVBinding.simplifiedChineseLayout.setOnClickListener(v -> {
            selectedLanguage = MSLanguageType.LANGUAGE_CHINESE_SIMPLIFIED;
            setSelectedLanguage();
        });
        msVBinding.englishLayout.setOnClickListener(v -> {
            selectedLanguage = MSLanguageType.LANGUAGE_EN;
            setSelectedLanguage();
        });
    }


    private void setSelectedLanguage() {
        if (selectedLanguage == MSLanguageType.LANGUAGE_FOLLOW_SYSTEM) {
            msVBinding.autoIv.setVisibility(View.VISIBLE);
            msVBinding.englishIv.setVisibility(View.INVISIBLE);
            msVBinding.simplifiedChineseIv.setVisibility(View.INVISIBLE);
        } else if (selectedLanguage == MSLanguageType.LANGUAGE_EN) {
            msVBinding.autoIv.setVisibility(View.INVISIBLE);
            msVBinding.englishIv.setVisibility(View.VISIBLE);
            msVBinding.simplifiedChineseIv.setVisibility(View.INVISIBLE);
        } else if (selectedLanguage == MSLanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
            msVBinding.autoIv.setVisibility(View.INVISIBLE);
            msVBinding.englishIv.setVisibility(View.INVISIBLE);
            msVBinding.simplifiedChineseIv.setVisibility(View.VISIBLE);
        }
    }
}
