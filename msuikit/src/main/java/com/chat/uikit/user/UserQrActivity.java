package com.chat.uikit.user;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.ImageUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActUserQrLayoutBinding;
import com.chat.uikit.user.service.UserModel;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人二维码
 */
public class UserQrActivity extends MSBaseActivity<ActUserQrLayoutBinding> {

    @Override
    protected ActUserQrLayoutBinding getViewBinding() {
        return ActUserQrLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.my_qr);
    }

    @Override
    protected int getRightIvResourceId(ImageView imageView) {
        return R.mipmap.ic_ab_other;
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        List<PopupMenuItem> list = new ArrayList<>();
        list.add(new PopupMenuItem(getString(R.string.save_img), R.mipmap.msg_download, () -> {
            Bitmap bitmap = ImageUtils.getInstance().loadBitmapFromView(msVBinding.shadowLayout);
            ImageUtils.getInstance().saveBitmap(UserQrActivity.this, bitmap, true, path -> showToast(R.string.saved_album));
        }));
        ImageView rightIV = findViewById(R.id.titleRightIv);
        MSDialogUtils.getInstance().showScreenPopup(rightIV,  list);

    }


    @Override
    protected void initView() {
        msVBinding.qrDescTv.setText(String.format(getString(R.string.qr_desc), getString(R.string.app_name)));
        msVBinding.nameTv.setText(MSConfig.getInstance().getUserName());
        msVBinding.avatarView.showAvatar(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL);
        UserModel.getInstance().userQr((code, msg, userQr) -> {
            if (code == HttpResponseCode.success) {
                String qrCode = userQr.data;
                Bitmap mBitmap = (Bitmap) EndpointManager.getInstance().invoke("create_qrcode", qrCode);
                msVBinding.qrIv.setImageBitmap(mBitmap);
            } else showToast(msg);
        });
    }

    @Override
    protected void initListener() {

    }
}
