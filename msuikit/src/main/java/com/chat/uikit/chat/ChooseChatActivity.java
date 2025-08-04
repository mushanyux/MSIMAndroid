package com.chat.uikit.chat;

import android.content.Intent;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.msgitem.MSChannelMemberRole;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.MSReader;
import com.chat.uikit.MSUIKitApplication;
import com.chat.uikit.R;
import com.chat.uikit.chat.adapter.ChooseChatAdapter;
import com.chat.uikit.contacts.ChooseContactsActivity;
import com.chat.uikit.databinding.ActChooseChatLayoutBinding;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelStatus;
import com.mushanyux.mushanim.entity.MSUIConversationMsg;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 选择会话页面
 */
public class ChooseChatActivity extends MSBaseActivity<ActChooseChatLayoutBinding> {
    ChooseChatAdapter chooseChatAdapter;
    Button rightBtn;
    private boolean isChoose;
    List<ChooseChatEntity> allList;

    @Override
    protected ActChooseChatLayoutBinding getViewBinding() {
        return ActChooseChatLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.choose_chat);
    }

    @Override
    protected String getRightBtnText(Button titleRightBtn) {
        rightBtn = titleRightBtn;
        return getString(R.string.sure);
    }

    @Override
    protected void rightButtonClick() {
        super.rightButtonClick();

        List<MSUIConversationMsg> selectedList = new ArrayList<>();
        for (int i = 0, size = chooseChatAdapter.getData().size(); i < size; i++) {
            if (chooseChatAdapter.getData().get(i).isCheck)
                selectedList.add(chooseChatAdapter.getData().get(i).uiConveursationMsg);
        }
        List<MSChannel> list = new ArrayList<>();
        if (MSReader.isNotEmpty(selectedList)) {
            for (int i = 0; i < selectedList.size(); i++) {
                list.add(selectedList.get(i).getMsChannel());
            }
            if (isChoose) {
                if (MSUIKitApplication.getInstance().getMessageContentList() != null) {
                    MSUIKitApplication.getInstance().showChatConfirmDialog(this, list, MSUIKitApplication.getInstance().getMessageContentList(), new MSUIKitApplication.IShowChatConfirm() {
                        @Override
                        public void onBack(@NonNull List<MSChannel> list, @NonNull List<MSMessageContent> messageContentList) {
                            MSUIKitApplication.getInstance().sendChooseChatBack(list);
                            finish();
                        }
                    });
                } else {
                    MSUIKitApplication.getInstance().sendChooseChatBack(list);
                    finish();
                }
            } else {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("list", (ArrayList<? extends Parcelable>) list);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    protected void initPresenter() {
        isChoose = getIntent().getBooleanExtra("isChoose", false);
    }

    @Override
    protected void initView() {
        chooseChatAdapter = new ChooseChatAdapter(new ArrayList<>());
        initAdapter(msVBinding.recyclerView, chooseChatAdapter);
        chooseChatAdapter.addHeaderView(getHeader());
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
    }

    @Override
    protected void initListener() {
        chooseChatAdapter.setOnItemClickListener((adapter, view1, position) -> {
            ChooseChatEntity chooseChatEntity = (ChooseChatEntity) adapter.getItem(position);
            if (chooseChatEntity != null) {
                boolean isSelect = !chooseChatEntity.isBan && !chooseChatEntity.isForbidden;
                if (isSelect) {
                    chooseChatEntity.isCheck = !chooseChatEntity.isCheck;
                    int selectCount = 0;
                    for (int i = 0, size = allList.size(); i < size; i++) {
                        if (allList.get(i).isCheck)
                            selectCount++;
                    }
                    if (chooseChatEntity.isCheck && selectCount == 10) {
                        chooseChatEntity.isCheck = false;
                        showSingleBtnDialog(String.format(getString(R.string.max_select_count_chat), 9));
                        adapter.notifyItemChanged(position + adapter.getHeaderLayoutCount());
                        return;
                    }
                    adapter.notifyItemChanged(position + adapter.getHeaderLayoutCount(),chooseChatEntity);


                    int count = 0;
                    for (int i = 0, size = allList.size(); i < size; i++) {
                        if (allList.get(i).isCheck)
                            count++;
                    }
                    if (count > 0) {
                        rightBtn.setVisibility(View.VISIBLE);
                        rightBtn.setText(String.format("%s(%s)", getString(R.string.sure), count));
                    } else {
                        rightBtn.setText(R.string.sure);
                        rightBtn.setVisibility(View.INVISIBLE);
                    }
                }
            }

        });

        msVBinding.searchEt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        msVBinding.searchEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                SoftKeyboardUtils.getInstance().hideSoftKeyboard(ChooseChatActivity.this);
                return true;
            }
            return false;
        });
        msVBinding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchUser(editable.toString());
            }
        });
    }

    private void searchUser(String content) {
        if (TextUtils.isEmpty(content)) {
            chooseChatAdapter.setList(allList);
            return;
        }
        List<ChooseChatEntity> tempList = new ArrayList<>();
        for (int i = 0, size = allList.size(); i < size; i++) {
            if ((!TextUtils.isEmpty(allList.get(i).uiConveursationMsg.getMsChannel().channelName) && allList.get(i).uiConveursationMsg.getMsChannel().channelName.toLowerCase(Locale.getDefault())
                    .contains(content.toLowerCase(Locale.getDefault())))
                    || (!TextUtils.isEmpty(allList.get(i).uiConveursationMsg.getMsChannel().channelRemark) && allList.get(i).uiConveursationMsg.getMsChannel().channelRemark.toLowerCase(Locale.getDefault())
                    .contains(content.toLowerCase(Locale.getDefault())))) {
                tempList.add(allList.get(i));
            }
        }
        chooseChatAdapter.setList(tempList);
    }

    @Override
    protected void initData() {
        super.initData();
        List<MSUIConversationMsg> list = MSIM.getInstance().getConversationManager().getAll();
        allList = new ArrayList<>();
        for (int i = 0, size = list.size(); i < size; i++) {
            ChooseChatEntity chooseChatEntity = new ChooseChatEntity(list.get(i));
            if (list.get(i).getMsChannel() != null) {
                MSChannelMember mChannelMember = MSIM.getInstance().getChannelMembersManager().getMember(list.get(i).getMsChannel().channelID, list.get(i).getMsChannel().channelType, MSConfig.getInstance().getUid());
                if (list.get(i).getMsChannel().forbidden == 1) {
                    // 禁言中
                    if (mChannelMember != null) {
                        chooseChatEntity.isForbidden = mChannelMember.role == MSChannelMemberRole.normal;
                    }
                } else {
                    if (mChannelMember != null)
                        chooseChatEntity.isForbidden = mChannelMember.forbiddenExpirationTime > 0;
                    else chooseChatEntity.isForbidden = false;
                }
                chooseChatEntity.isBan = list.get(i).getMsChannel().status == MSChannelStatus.statusDisabled;
            }
            allList.add(chooseChatEntity);
        }

        chooseChatAdapter.setList(allList);
        rightBtn.setVisibility(View.GONE);
    }

    public static class ChooseChatEntity {
        ChooseChatEntity(MSUIConversationMsg uiConveursationMsg) {
            this.uiConveursationMsg = uiConveursationMsg;
        }

        public MSUIConversationMsg uiConveursationMsg;
        public boolean isCheck;
        // 禁言中
        public boolean isForbidden;
        // 禁用中
        public boolean isBan;
    }

    private View getHeader() {
        View view = LayoutInflater.from(this).inflate(R.layout.choose_chat_header_layout, msVBinding.recyclerView, false);
        View headerView = view.findViewById(R.id.createTv);
        headerView.setOnClickListener(view1 -> {
            Intent intent = new Intent(this, ChooseContactsActivity.class);
            if (MSUIKitApplication.getInstance().getMessageContentList() != null)
                intent.putParcelableArrayListExtra("msgContentList", (ArrayList<? extends Parcelable>) MSUIKitApplication.getInstance().getMessageContentList());
            startActivity(intent);
        });
        return view;
    }
}
