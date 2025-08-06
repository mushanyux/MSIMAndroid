package com.chat.uikit.user;

import android.widget.ImageView;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSSystemAccount;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSTimeUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.chat.manager.MSIMUtils;
import com.chat.uikit.databinding.ActFileHelperLayoutBinding;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统文件助手详情
 */
public class MSFileHelperActivity extends MSBaseActivity<ActFileHelperLayoutBinding> {
    @Override
    protected ActFileHelperLayoutBinding getViewBinding() {
        return ActFileHelperLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText("");
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView() {
        msVBinding.appIdNumLeftTv.setText(String.format(getString(R.string.app_idnum), getString(R.string.app_name)));
        msVBinding.appIdNumTv.setText(MSSystemAccount.system_file_helper_short_no);
        msVBinding.avatarView.setSize(70);
        msVBinding.avatarView.showAvatar(MSSystemAccount.system_file_helper, MSChannelType.PERSONAL);
        SingleClickUtil.onSingleClick(msVBinding.sendMsgBtn, v -> MSIMUtils.getInstance().startChatActivity(new ChatViewMenu(this, MSSystemAccount.system_file_helper, MSChannelType.PERSONAL, 0, true)));
    }

    @Override
    protected void initListener() {
        msVBinding.avatarView.setOnClickListener(v -> showImg());
    }

    private void showImg() {
        String uri = MSApiConfig.getAvatarUrl(MSSystemAccount.system_file_helper) + "?key=" + MSTimeUtils.getInstance().getCurrentMills();
        List<Object> tempImgList = new ArrayList<>();
        List<ImageView> imageViewList = new ArrayList<>();
        imageViewList.add(msVBinding.avatarView.imageView);
        tempImgList.add(MSApiConfig.getShowUrl(uri));
        int index = 0;
        MSDialogUtils.getInstance().showImagePopup(this, tempImgList, imageViewList, msVBinding.avatarView.imageView, index, new ArrayList<>(), null, null);

    }
}
