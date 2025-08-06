package com.chat.uikit.user;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.chat.base.MSBaseApplication;
import com.chat.base.act.MSCropImageActivity;
import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSApiConfig;
import com.chat.base.config.MSConfig;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.glide.ChooseMimeType;
import com.chat.base.glide.ChooseResult;
import com.chat.base.glide.GlideUtils;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.ImageUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSPermissions;
import com.chat.base.utils.MSReader;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActMyHeadPortraitLayoutBinding;
import com.chat.uikit.user.service.UserModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 我的头像
 */
public class MyHeadPortraitActivity extends MSBaseActivity<ActMyHeadPortraitLayoutBinding> {
    @Override
    protected ActMyHeadPortraitLayoutBinding getViewBinding() {
        return ActMyHeadPortraitLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.head_portrait);
    }

    @Override
    protected int getRightIvResourceId(ImageView imageView) {
        return R.mipmap.ic_ab_other;
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        showBottomDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MSBaseApplication.getInstance().disconnect = true;
    }

    @Override
    protected void initView() {
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL);
        String url = MSApiConfig.getAvatarUrl(MSConfig.getInstance().getUid());

        if (channel != null && !TextUtils.isEmpty(channel.channelID)) {
            GlideUtils.getInstance().showAvatarImg(this, channel.channelID, channel.channelType, channel.avatarCacheKey, msVBinding.avatarIv);
        } else {
            GlideUtils.getInstance().showImg(this, url + "?width=500&height=500", msVBinding.avatarIv);
        }
    }

    @Override
    protected void initListener() {
        msVBinding.avatarIv.setOnLongClickListener(view1 -> {
            showBottomDialog();
            return true;
        });
    }

    private void showBottomDialog() {
        List<PopupMenuItem> list = new ArrayList<>();
        list.add(new PopupMenuItem(getString(R.string.update_avatar), R.mipmap.msg_edit, () -> {
            MSBaseApplication.getInstance().disconnect = false;
            chooseIMG();
        }));

        list.add(new PopupMenuItem(getString(R.string.save_img), R.mipmap.msg_download, () -> {
            String avatarURL = MSApiConfig.getAvatarUrl(MSConfig.getInstance().getUid());
            avatarURL = avatarURL + "?key=" + UUID.randomUUID().toString().replaceAll("-","");
            ImageUtils.getInstance().downloadImg(this, avatarURL, bitmap -> {
                if (bitmap != null) {
                    ImageUtils.getInstance().saveBitmap(MyHeadPortraitActivity.this, bitmap, true, path -> showToast(R.string.saved_album));
                }
            });
        }));
        ImageView rightIV = findViewById(R.id.titleRightIv);
        MSDialogUtils.getInstance().showScreenPopup(rightIV, list);
    }

    private void chooseIMG() {
        String desc = String.format(getString(R.string.file_permissions_des), getString(R.string.app_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            MSPermissions.getInstance().checkPermissions(new MSPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        success();
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {
                }
            }, this, desc, Manifest.permission.CAMERA);
        } else {
            MSPermissions.getInstance().checkPermissions(new MSPermissions.IPermissionResult() {
                @Override
                public void onResult(boolean result) {
                    if (result) {
                        success();
                    }
                }

                @Override
                public void clickResult(boolean isCancel) {
                }
            }, this, desc, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE);
        }


    }

    private void success() {

        GlideUtils.getInstance().chooseIMG(MyHeadPortraitActivity.this, 1, true, ChooseMimeType.img, false,false, new GlideUtils.ISelectBack() {
            @Override
            public void onBack(List<ChooseResult> paths) {
                if (MSReader.isNotEmpty(paths)) {
                    String path = paths.get(0).path;
                    if (!TextUtils.isEmpty(path)) {
                        Intent intent = new Intent(MyHeadPortraitActivity.this, MSCropImageActivity.class);
                        intent.putExtra("path", path);
                        chooseResultLac.launch(intent);
                    }
                }
            }

            @Override
            public void onCancel() {

            }
        });

    }

    ActivityResultLauncher<Intent> chooseResultLac = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            String path = result.getData().getStringExtra("path");
            UserModel.getInstance().uploadAvatar(path, code -> {
                if (code == HttpResponseCode.success) {
                    MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL);
                    if (channel == null || TextUtils.isEmpty(channel.channelID)) {
                        channel = new MSChannel();
                        channel.channelType = MSChannelType.PERSONAL;
                        channel.channelID = MSConfig.getInstance().getUid();
                        MSIM.getInstance().getChannelManager().saveOrUpdateChannel(channel);
                    }
                    channel.avatarCacheKey = UUID.randomUUID().toString().replace("-", "");
                    MSIM.getInstance().getChannelManager().updateAvatarCacheKey(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL, channel.avatarCacheKey);
                    GlideUtils.getInstance().showAvatarImg(this, channel.channelID, MSChannelType.PERSONAL, channel.avatarCacheKey, msVBinding.avatarIv);
                    String avatarURL = MSApiConfig.getAvatarUrl(MSConfig.getInstance().getUid());
                    avatarURL = avatarURL + "?key=" + channel.avatarCacheKey;
                    EndpointManager.getInstance().invoke("updateRtcAvatarUrl", avatarURL);
                }
            });
        }
    });
}
