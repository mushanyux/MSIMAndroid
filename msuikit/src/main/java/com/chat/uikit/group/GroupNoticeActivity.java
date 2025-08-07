package com.chat.uikit.group;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.entity.PopupMenuItem;
import com.chat.base.msgitem.MSChannelMemberRole;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.StringUtils;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSToastUtils;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActGroupNoticeLayoutBinding;
import com.chat.uikit.group.service.GroupModel;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 群公告
 */
public class GroupNoticeActivity extends MSBaseActivity<ActGroupNoticeLayoutBinding> {

    private String groupNo, oldNotice;
    private TextView titleRightTv;

    @Override
    protected ActGroupNoticeLayoutBinding getViewBinding() {
        return ActGroupNoticeLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.group_announcement);
    }

    @Override
    protected String getRightTvText(TextView textView) {
        titleRightTv = textView;
        return getString(R.string.save);
    }

    @Override
    protected boolean hideStatusBar() {
        return true;
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        String content = Objects.requireNonNull(msVBinding.contentEt.getText()).toString();
//        content = StringUtils.replaceBlank(content);
        if ((!TextUtils.isEmpty(oldNotice) && content.equals(oldNotice))) {
            return;
        }
        showTitleRightLoading();
        GroupModel.getInstance().updateGroupInfo(groupNo, "notice", content, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                finish();
            } else {
                hideTitleRightLoading();
                showToast(msg);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initView() {
        List<PopupMenuItem> list = new ArrayList<>();
        list.add(new PopupMenuItem(getString(R.string.copy), R.mipmap.msg_copy, () -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", Objects.requireNonNull(msVBinding.contentEt.getText()).toString());
            assert cm != null;
            cm.setPrimaryClip(mClipData);
            MSToastUtils.getInstance().showToastNormal(getString(R.string.copyed));
        }));
        MSDialogUtils.getInstance().setViewLongClickPopup(msVBinding.contentEt,list);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        super.initData();

        groupNo = getIntent().getStringExtra("groupNo");
        oldNotice = getIntent().getStringExtra("oldNotice");

        MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(groupNo, MSChannelType.GROUP, MSConfig.getInstance().getUid());
        msVBinding.contentEt.setText(oldNotice);
        if (!TextUtils.isEmpty(oldNotice))
            msVBinding.contentEt.setSelection(oldNotice.length());

        if (member != null && member.role != MSChannelMemberRole.normal) {
            msVBinding.contentEt.setEnabled(true);
            titleRightTv.setVisibility(View.VISIBLE);
            msVBinding.contentEt.requestFocus();
            SoftKeyboardUtils.getInstance().showSoftKeyBoard(this, msVBinding.contentEt);
        } else {
            msVBinding.contentEt.setFocusableInTouchMode(false);
            msVBinding.contentEt.setEnabled(true);
            titleRightTv.setVisibility(View.GONE);
        }
        if (member != null) {
            msVBinding.bottomView.setVisibility(member.role == MSChannelMemberRole.normal ? View.VISIBLE : View.GONE);
        }
        msVBinding.contentEt.setFilters(new InputFilter[]{StringUtils.getInputFilter(300)});
    }

    @Override
    public void finish() {
        super.finish();
        SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);
    }
}
