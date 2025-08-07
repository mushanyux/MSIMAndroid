package com.chat.uikit.group;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.entity.MSChannelCustomerExtras;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.chat.search.member.SearchWithMemberActivity;
import com.chat.uikit.databinding.ActAllMemberLayoutBinding;
import com.chat.uikit.enity.AllGroupMemberEntity;
import com.chat.uikit.enity.OnlineUser;
import com.chat.uikit.group.adapter.AllMembersAdapter;
import com.chat.uikit.user.UserDetailActivity;
import com.chat.uikit.user.service.UserModel;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelExtras;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有成员
 */
public class MSAllMembersActivity extends MSBaseActivity<ActAllMemberLayoutBinding> {

    private AllMembersAdapter adapter;
    private int page = 1;
    String channelID;
    byte channelType;
    private String searchKey;
    private TextView titleTv;
    private int groupType = 0;
    private boolean searchMessage;

    @Override
    protected ActAllMemberLayoutBinding getViewBinding() {
        return ActAllMemberLayoutBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("StringFormatMatches")
    @Override
    protected void setTitle(TextView titleTv) {
        this.titleTv = titleTv;
    }

    @Override
    protected void initView() {
        if (getIntent().hasExtra("searchMessage")) {
            searchMessage = getIntent().getBooleanExtra("searchMessage", false);
        }
        channelID = getIntent().getStringExtra("channelID");
        channelType = getIntent().getByteExtra("channelType", MSChannelType.GROUP);
        adapter = new AllMembersAdapter();
        initAdapter(msVBinding.recyclerView, adapter);


    }

    @Override
    protected void initListener() {

        msVBinding.refreshLayout.setEnableRefresh(false);
        msVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                page++;
                getData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                page = 1;
                getData();
            }
        });
        msVBinding.searchEt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        msVBinding.searchEt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                SoftKeyboardUtils.getInstance().hideSoftKeyboard(MSAllMembersActivity.this);
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
                searchKey = editable.toString();
                page = 1;
                adapter.setSearchKey(searchKey);
                getData();
            }
        });
        adapter.setOnItemClickListener((adapter1, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view2 -> {
            AllGroupMemberEntity entity = adapter.getItem(position);
            if (entity != null) {
                entity.getChannelMember();
                Intent intent;
                if (searchMessage) {
                    intent = new Intent(this, SearchWithMemberActivity.class);
                    intent.putExtra("channelID", channelID);
                    intent.putExtra("fromUID", entity.getChannelMember().memberUID);
                } else {
                    intent = new Intent(this, UserDetailActivity.class);
                    intent.putExtra("uid", entity.getChannelMember().memberUID);
                    intent.putExtra("groupID", entity.getChannelMember().channelID);
                }
                startActivity(intent);
            }
        }));
    }

    @Override
    protected void initData() {
        super.initData();
        int count = MSIM.getInstance().getChannelMembersManager().getMemberCount(channelID, channelType);
        if (searchMessage) {
            titleTv.setText(R.string.uikit_search_with_member);
        } else {
            titleTv.setText(String.format(getString(R.string.group_members), count + ""));
        }
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelID, channelType);
        if (channel != null && channel.remoteExtraMap != null) {
            if (channel.remoteExtraMap.containsKey(MSChannelExtras.groupType)) {
                Object groupTypeObject = channel.remoteExtraMap.get(MSChannelExtras.groupType);
                if (groupTypeObject instanceof Integer) {
                    groupType = (int) groupTypeObject;
                }
            }
            Object memberCountObject = channel.remoteExtraMap.get(MSChannelCustomerExtras.memberCount);
            if (memberCountObject instanceof Integer) {
                count = (int) memberCountObject;
                if (searchMessage) {
                    titleTv.setText(R.string.uikit_search_with_member);
                } else {
                    titleTv.setText(String.format(getString(R.string.group_members), count + ""));
                }
            }
        }
        getData();
    }

    private void getData() {
        MSIM.getInstance().getChannelMembersManager().getWithPageOrSearch(channelID, channelType, searchKey, page, 50, (list, b) -> {
            if (groupType == 0)
                resortData(list);
            else {
                if (b) {
                    resortData(list);
                }
            }
        });
    }

    private void resortData(List<MSChannelMember> list) {
        if (MSReader.isNotEmpty(list)) {
            msVBinding.refreshLayout.setEnableLoadMore(true);
            List<String> uidList = new ArrayList<>();
            for (MSChannelMember member : list) {
                uidList.add(member.memberUID);
            }
            UserModel.getInstance().getOnlineUsers(uidList, (code, msg, onlineUserList) -> {
                msVBinding.refreshLayout.finishLoadMore();
                if (MSReader.isNotEmpty(list)) {

                    List<AllGroupMemberEntity> allList = new ArrayList<>();
                    for (MSChannelMember member : list) {
                        int online = 0;
                        String lastOfflineTime = "";
                        String lastOnlineTime = "";
                        for (OnlineUser onlineUser : onlineUserList) {
                            if (onlineUser.getUid().equals(member.memberUID)) {
                                online = onlineUser.getOnline();
                                lastOnlineTime =
                                        MSTimeUtils.getInstance().getOnlineTime(onlineUser.getLast_offline());
                                lastOfflineTime = MSTimeUtils.getInstance()
                                        .getShowDateAndMinute(onlineUser.getLast_offline() * 1000L);
                            }
                        }
                        AllGroupMemberEntity entity = new AllGroupMemberEntity(member, online, lastOfflineTime, lastOnlineTime);
                        allList.add(entity);
                    }
                    if (page == 1) {
                        adapter.setList(allList);
                    } else {
                        adapter.addData(allList);
                    }

                }
            });
        } else {
            if (page == 1) {
                adapter.setList(new ArrayList<>());
            } else {
                msVBinding.refreshLayout.finishLoadMore();
                msVBinding.refreshLayout.setEnableLoadMore(false);
            }
        }
    }

}
