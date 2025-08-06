package com.chat.uikit.user;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSConfig;
import com.chat.base.entity.BottomSheetItem;
import com.chat.base.entity.UserInfoEntity;
import com.chat.base.entity.MSAPPConfig;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActMyInfoLayoutBinding;
import com.chat.uikit.user.service.UserModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录用户个人信息
 */
public class MyInfoActivity extends MSBaseActivity<ActMyInfoLayoutBinding> {

    @Override
    protected ActMyInfoLayoutBinding getViewBinding() {
        return ActMyInfoLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.personal_info);
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView() {
        msVBinding.idLeftTv.setText(String.format(getString(R.string.identity), getString(R.string.app_name)));
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);
        UserInfoEntity userInfoEntity = MSConfig.getInstance().getUserInfo();
        MSAPPConfig appConfig = MSConfig.getInstance().getAppConfig();
        if (userInfoEntity.short_status == 1 || appConfig.shortno_edit_off == 1) {
            msVBinding.identityLayout.setEnabled(false);
            msVBinding.identityIv.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL);
        if (channel != null && !TextUtils.isEmpty(channel.channelID)) {
            msVBinding.avatarView.showAvatar(channel);
        } else
            msVBinding.avatarView.showAvatar(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL);
    }

    @Override
    protected void initListener() {
        MSCommonModel.getInstance().getChannel(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL, (code, msg, entity) -> {
            if (entity != null && entity.extra != null) {
                Object sexObject = entity.extra.get("sex");
                if (sexObject != null) {
                    int sex = (int) sexObject;
                    msVBinding.sexTv.setText(sex == 1 ? R.string.male : R.string.female);
                }
                Object shortNoObject = entity.extra.get("short_no");
                if (shortNoObject != null) {
                    String shortNo = (String) shortNoObject;
                    msVBinding.identityTv.setText(shortNo);
                    msVBinding.nameTv.setText(entity.name);
                }
            }
        });
        SingleClickUtil.onSingleClick(msVBinding.headLayout, view -> startActivity(new Intent(MyInfoActivity.this, MyHeadPortraitActivity.class)));
        SingleClickUtil.onSingleClick(msVBinding.nameLayout, view1 -> {
            Intent intent = new Intent(this, UpdateUserInfoActivity.class);
            intent.putExtra("oldStr", msVBinding.nameTv.getText().toString());
            intent.putExtra("updateType", 1);
            chooseResultLac.launch(intent);
        });
        SingleClickUtil.onSingleClick(msVBinding.identityLayout, view1 -> {
            if (MSConfig.getInstance().getAppConfig().shortno_edit_off == 0) {
                Intent intent = new Intent(this, UpdateUserInfoActivity.class);
                intent.putExtra("oldStr", msVBinding.identityTv.getText().toString());
                intent.putExtra("updateType", 2);
                chooseResultLac.launch(intent);
            }
        });
        SingleClickUtil.onSingleClick(msVBinding.qrLayout, view1 -> startActivity(new Intent(this, UserQrActivity.class)));
        msVBinding.sexLayout.setOnClickListener(v -> {
            List<BottomSheetItem> list = new ArrayList<>();
            list.add(new BottomSheetItem(getString(R.string.male), 0, () -> updateSex(1)));
            list.add(new BottomSheetItem(getString(R.string.female), 0, () -> updateSex(0)));
            MSDialogUtils.getInstance().showBottomSheet(this,getString(R.string.sex),false,list);
        });
    }
    private void updateSex(int value){
        UserModel.getInstance().updateUserInfo("sex", String.valueOf(value), (code, msg) -> {
            if (code == HttpResponseCode.success)
                msVBinding.sexTv.setText(value == 1 ? R.string.male : R.string.female);
            else showToast(msg);
        });
    }
    ActivityResultLauncher<Intent> chooseResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            String resultStr = result.getData().getStringExtra("result");
            int updateType = result.getData().getIntExtra("updateType", 1);
            if (updateType == 1) {
                msVBinding.nameTv.setText(resultStr);
                MSConfig.getInstance().setUserName(resultStr);
            } else if (updateType == 2) {
                msVBinding.identityTv.setText(resultStr);
                msVBinding.identityIv.setVisibility(View.GONE);
            }
        }
    });
}
