package com.chat.uikit.search;

import android.content.Intent;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.ui.Theme;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActAddFriendsLayoutBinding;
import com.chat.uikit.user.UserQrActivity;

/**
 * 添加好友
 */
public class AddFriendsActivity extends MSBaseActivity<ActAddFriendsLayoutBinding> {
    @Override
    protected ActAddFriendsLayoutBinding getViewBinding() {
        return ActAddFriendsLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.add_friends);
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView() {
        Theme.setPressedBackground(msVBinding.qrIv);
        msVBinding.searchTitleTv.setText(String.format(getString(R.string.my_app_id), getString(R.string.app_name)));
        msVBinding.identityTv.setText(MSConfig.getInstance().getUserInfo().short_no);
    }

    @Override
    protected void initListener() {
        SingleClickUtil.onSingleClick(msVBinding.qrIv, v -> startActivity(new Intent(this, UserQrActivity.class)));
        SingleClickUtil.onSingleClick(msVBinding.searchLayout, v -> startActivity(new Intent(this, SearchUserActivity.class)));
        SingleClickUtil.onSingleClick(msVBinding.scanLayout, v -> EndpointManager.getInstance().invoke("ms_scan_show", null));
        SingleClickUtil.onSingleClick(msVBinding.mailListLayout, v -> startActivity(new Intent(this, MailListActivity.class)));
    }

}
