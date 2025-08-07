package com.chat.uikit.group;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.entity.ChannelInfoEntity;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.utils.ImageUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActGroupQrLayoutBinding;
import com.chat.uikit.group.service.GroupContract;
import com.chat.uikit.group.service.GroupPresenter;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 群聊二维码名片
 */
public class GroupQrActivity extends MSBaseActivity<ActGroupQrLayoutBinding> implements GroupContract.GroupView {

    private GroupPresenter presenter;
    String groupId;

    @Override
    protected ActGroupQrLayoutBinding getViewBinding() {
        return ActGroupQrLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.group_qr);
    }

    @Override
    protected void initPresenter() {
        presenter = new GroupPresenter(this);
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
            ImageUtils.getInstance().saveBitmap(this, bitmap, true, path -> showToast(R.string.saved_album));
        }));
        ImageView rightIV = findViewById(R.id.titleRightIv);
        MSDialogUtils.getInstance().showScreenPopup(rightIV,  list);
    }

    @Override
    protected void initView() {
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);
        msVBinding.avatarView.setSize(45);
    }

    @Override
    protected void initListener() {
        MSIM.getInstance().getChannelManager().addOnRefreshChannelInfo("group_qr_channel_refresh", (channel, isEnd) -> getGroupInfo());
    }

    @Override
    protected void initData() {
        super.initData();
        groupId = getIntent().getStringExtra("groupId");
        getGroupInfo();
    }

    private void getGroupInfo() {
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(groupId, MSChannelType.GROUP);
        if (channel != null) {
            msVBinding.nameTv.setText(channel.channelName);
            msVBinding.avatarView.showAvatar(groupId, channel.channelType, channel.avatarCacheKey);
            if (channel.invite == 1) {
                msVBinding.qrTv.setVisibility(View.INVISIBLE);
                msVBinding.unusedTv.setVisibility(View.VISIBLE);
                msVBinding.qrIv.setImageResource(R.mipmap.icon_no_qr);
            } else {
                msVBinding.unusedTv.setVisibility(View.INVISIBLE);
                msVBinding.qrTv.setVisibility(View.VISIBLE);
                presenter.getQrData(groupId);
            }
        } else {
            presenter.getQrData(groupId);
        }
    }

    @Override
    public void onGroupInfo(ChannelInfoEntity groupEntity) {

    }

    @Override
    public void onRefreshGroupSetting(String key, int value) {

    }

    @Override
    public void setQrData(int day, String qrcode, String expire) {
        if (TextUtils.isEmpty(qrcode)) {
            msVBinding.qrIv.setImageResource(R.mipmap.icon_no_qr);
        } else {
            Bitmap mBitmap = (Bitmap) EndpointManager.getInstance().invoke("create_qrcode", qrcode);
//            Bitmap mBitmap = CodeUtils.createQRCode(qrcode, 400, null);
            msVBinding.qrIv.setImageBitmap(mBitmap);
            String content = String.format(getString(R.string.group_qr_desc), day, expire);
            msVBinding.qrTv.setText(content);
        }
    }

    @Override
    public void setMyGroups(List<GroupEntity> list) {

    }

    @Override
    public void showError(String msg) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MSIM.getInstance().getChannelManager().removeRefreshChannelInfo("group_qr_channel_refresh");
    }
}
