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
import com.chat.uikit.databinding.ActSystemTeamLayoutBinding;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统团队
 */
public class MSSystemTeamActivity extends MSBaseActivity<ActSystemTeamLayoutBinding> {
    @Override
    protected ActSystemTeamLayoutBinding getViewBinding() {
        return ActSystemTeamLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText("");
    }


    @Override
    protected void initView() {
        msVBinding.appIdNumLeftTv.setText(String.format(getString(R.string.app_idnum), getString(R.string.app_name)));
        msVBinding.functionNameTv.setText(String.format(getString(R.string.function_system_team_tips), getString(R.string.app_name)));
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(MSSystemAccount.system_team, MSChannelType.PERSONAL);
        if (channel != null) {
            msVBinding.nameTv.setText(channel.channelName);
            msVBinding.appIdNumTv.setText(MSSystemAccount.system_team_short_no);
        }
        msVBinding.nameTv.setText(R.string.ms_system_notice);
        msVBinding.avatarView.setSize(70);
        msVBinding.avatarView.showAvatar(channel);
        SingleClickUtil.onSingleClick(msVBinding.sendMsgBtn, v -> MSIMUtils.getInstance().startChatActivity(new ChatViewMenu(this, MSSystemAccount.system_team, MSChannelType.PERSONAL, 0, true)));
    }

    @Override
    protected void initListener() {
        msVBinding.avatarView.setOnClickListener(v -> showImg());
    }

    private void showImg() {
        String uri = MSApiConfig.getAvatarUrl(MSSystemAccount.system_team) + "?key=" + MSTimeUtils.getInstance().getCurrentMills();
        //查看大图
        List<Object> tempImgList = new ArrayList<>();
        List<ImageView> imageViewList = new ArrayList<>();
        imageViewList.add(msVBinding.avatarView.imageView);
        tempImgList.add(MSApiConfig.getShowUrl(uri));
        int index = 0;
        MSDialogUtils.getInstance().showImagePopup(this, tempImgList, imageViewList, msVBinding.avatarView.imageView, index, new ArrayList<>(), null, null);

    }
}
