package com.chat.uikit.search;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.core.view.ViewCompat;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.ui.Theme;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.chat.ChatActivity;
import com.chat.uikit.chat.manager.MSIMUtils;
import com.chat.uikit.databinding.ActSearchAllLayoutBinding;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelSearchResult;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSMessageSearchResult;
import com.mushanyux.mushanim.entity.MSMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 搜索所有内容
 */
public class SearchAllActivity extends MSBaseActivity<ActSearchAllLayoutBinding> {
    private SearchChannelAdapter userAdapter;
    private SearchChannelAdapter groupAdapter;
    private SearchMsgAdapter msgAdapter;

    @Override
    protected ActSearchAllLayoutBinding getViewBinding() {
        return ActSearchAllLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initPresenter() {
        Theme.setColorFilter(this, msVBinding.searchIv, R.color.popupTextColor);
        ViewCompat.setTransitionName(msVBinding.searchIv, "searchView");
    }

    @Override
    protected void initView() {
        msVBinding.searchKeyTv.setTextColor(Theme.colorAccount);
        Theme.setPressedBackground(msVBinding.cancelTv);
        userAdapter = new SearchChannelAdapter();
        groupAdapter = new SearchChannelAdapter();
        msgAdapter = new SearchMsgAdapter();
        initAdapter(msVBinding.userRecyclerView, userAdapter);
        initAdapter(msVBinding.groupRecyclerView, groupAdapter);
        initAdapter(msVBinding.msgRecyclerView, msgAdapter);
        msVBinding.userRecyclerView.setNestedScrollingEnabled(false);
        msVBinding.groupRecyclerView.setNestedScrollingEnabled(false);
        msVBinding.msgRecyclerView.setNestedScrollingEnabled(false);
        SoftKeyboardUtils.getInstance().showSoftKeyBoard(SearchAllActivity.this, msVBinding.searchEt);
    }

    @Override
    protected void initListener() {
        msVBinding.searchEt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        msVBinding.searchEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                SoftKeyboardUtils.getInstance().hideSoftKeyboard(SearchAllActivity.this);
                return true;
            }

            return false;
        });
        msgAdapter.setOnItemClickListener((adapter, view1, position) -> {
            MSMessageSearchResult result = (MSMessageSearchResult) adapter.getItem(position);
            if (result != null) {
                if (result.messageCount > 1) {
                    Intent intent = new Intent(this, SearchMsgResultActivity.class);
                    intent.putExtra("result", result);
                    intent.putExtra("searchKey", Objects.requireNonNull(msVBinding.searchEt.getText()).toString());
                    startActivity(intent);
                } else {
                    List<MSMsg> msgList = MSIM.getInstance().getMsgManager().searchWithChannel(Objects.requireNonNull(msVBinding.searchEt.getText()).toString(),result.msChannel.channelID, result.msChannel.channelType);
                    if (MSReader.isNotEmpty(msgList)) {
                        MSIMUtils.getInstance().startChatActivity(new ChatViewMenu(this, msgList.get(0).channelID, msgList.get(0).channelType, msgList.get(0).orderSeq, false));
                    }

                }
                SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.searchEt);
            }
        });
        userAdapter.setOnItemClickListener((adapter, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view2 -> {
            MSChannelSearchResult result = (MSChannelSearchResult) adapter.getItem(position);
            if (result != null) {
                Intent intent = new Intent(SearchAllActivity.this, ChatActivity.class);
                intent.putExtra("channelId", result.msChannel.channelID);
                intent.putExtra("channelType", result.msChannel.channelType);
                startActivity(intent);
                SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.searchEt);
            }
        }));
        groupAdapter.setOnItemClickListener((adapter, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view2 -> {
            MSChannelSearchResult result = (MSChannelSearchResult) adapter.getItem(position);
            if (result != null) {
                SoftKeyboardUtils.getInstance().hideInput(this, msVBinding.searchEt);
                MSIMUtils.getInstance().startChatActivity(new ChatViewMenu(this, result.msChannel.channelID, result.msChannel.channelType, 0, false));
            }
        }));
        msVBinding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String key = editable.toString();
                if (TextUtils.isEmpty(key)) {
                    msVBinding.resultView.setVisibility(View.GONE);
                } else {
                    searchChannel(key);
                    msVBinding.resultView.setVisibility(View.VISIBLE);
                }
            }
        });
        msVBinding.cancelTv.setOnClickListener(v -> finish());
        SingleClickUtil.onSingleClick(msVBinding.findUserLayout, v -> {
            SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);
            String searchKey = Objects.requireNonNull(msVBinding.searchEt.getText()).toString();
            Intent intent = new Intent(this, SearchUserActivity.class);
            intent.putExtra("searchKey", searchKey);
            startActivity(intent);
        });
    }

    private void searchChannel(String key) {
        List<MSChannelSearchResult> groupList = MSIM.getInstance().getChannelManager().search(key);
        List<MSChannel> tempList = MSIM.getInstance().getChannelManager().searchWithChannelTypeAndFollow(key, MSChannelType.PERSONAL, 1);
        List<MSChannelSearchResult> userList = new ArrayList<>();
        if (MSReader.isNotEmpty(tempList)) {
            for (int i = 0, size = tempList.size(); i < size; i++) {
                MSChannelSearchResult result = new MSChannelSearchResult();
                result.msChannel = tempList.get(i);
                userList.add(result);
            }
        }
        List<MSMessageSearchResult> msgList = MSIM.getInstance().getMsgManager().search(key);
        userAdapter.setSearchKey(key);
        groupAdapter.setSearchKey(key);
        msgAdapter.setSearchKey(key);
        if (MSReader.isNotEmpty(userList)) {
            msVBinding.userLayout.setVisibility(View.VISIBLE);
        } else {
            msVBinding.userLayout.setVisibility(View.GONE);
        }
        userAdapter.setList(userList);
        if (MSReader.isNotEmpty(groupList)) {
            msVBinding.groupLayout.setVisibility(View.VISIBLE);
        } else {
            msVBinding.groupLayout.setVisibility(View.GONE);
        }
        groupAdapter.setList(groupList);
        if (MSReader.isNotEmpty(msgList)) {
            msVBinding.msgLayout.setVisibility(View.VISIBLE);
        } else {
            msVBinding.msgLayout.setVisibility(View.GONE);
        }
        msgAdapter.setList(msgList);
        msVBinding.searchKeyTv.setText(key);
    }

    @Override
    public void finish() {
        super.finish();
        SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);
    }
}
