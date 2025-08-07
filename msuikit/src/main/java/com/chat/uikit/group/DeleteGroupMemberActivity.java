package com.chat.uikit.group;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSConfig;
import com.chat.base.msgitem.MSChannelMemberRole;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.SoftKeyboardUtils;
import com.chat.base.utils.MSReader;
import com.chat.uikit.R;
import com.chat.uikit.contacts.ChooseUserSelectedAdapter;
import com.chat.uikit.contacts.FriendUIEntity;
import com.chat.uikit.databinding.ActDeleteMemberLayoutBinding;
import com.chat.uikit.group.adapter.DeleteGroupMemberAdapter;
import com.chat.uikit.group.service.GroupModel;
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
 * 删除群成员
 */
public class DeleteGroupMemberActivity extends MSBaseActivity<ActDeleteMemberLayoutBinding> {
    DeleteGroupMemberAdapter groupMemberAdapter;
    ChooseUserSelectedAdapter selectedAdapter;
    private String groupId;
    private TextView textView;
    private String searchKey;
    private int page = 1;
    private int groupType = 0;

    @Override
    protected ActDeleteMemberLayoutBinding getViewBinding() {
        return ActDeleteMemberLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.delete_group_members);
    }

    @Override
    protected void rightLeftLayoutClick() {
        super.rightLeftLayoutClick();
    }

    @Override
    protected String getRightTvText(TextView textView) {
        this.textView = textView;
        return getString(R.string.delete);
    }

    @Override
    protected void rightLayoutClick() {
        super.rightLayoutClick();
        List<GroupMemberEntity> selectedList = new ArrayList<>();
        for (int i = 0, size = groupMemberAdapter.getData().size(); i < size; i++) {
            if (groupMemberAdapter.getData().get(i).checked == 1)
                selectedList.add(groupMemberAdapter.getData().get(i));
        }

        if (MSReader.isNotEmpty(selectedList)) {
            List<String> uids = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (int i = 0, size = selectedList.size(); i < size; i++) {
                uids.add(selectedList.get(i).member.memberUID);
                names.add(selectedList.get(i).member.memberName);
            }
            showTitleRightLoading();
            GroupModel.getInstance().deleteGroupMembers(groupId, uids, names, (code, msg) -> {
                if (code == HttpResponseCode.success) {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        SoftKeyboardUtils.getInstance().hideSoftKeyboard(DeleteGroupMemberActivity.this);
                        setResult(RESULT_OK);
                        finish();
                    }, 500);
                } else {
                    hideTitleRightLoading();
                    showToast(msg);
                }
            });
        }
    }

    @Override
    protected void initView() {
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(groupId, MSChannelType.GROUP);
        if (channel != null && channel.remoteExtraMap != null && channel.remoteExtraMap.containsKey(MSChannelExtras.groupType)) {
            Object groupTypeObject = channel.remoteExtraMap.get(MSChannelExtras.groupType);
            if (groupTypeObject instanceof Integer) {
                groupType = (int) groupTypeObject;
            }
        }
        groupMemberAdapter = new DeleteGroupMemberAdapter(new ArrayList<>());
        initAdapter(msVBinding.recyclerView, groupMemberAdapter);

        selectedAdapter = new ChooseUserSelectedAdapter(new ChooseUserSelectedAdapter.IGetEdit() {
            @Override
            public void onDeleted(String uid) {
                for (int i = 0, size = groupMemberAdapter.getData().size(); i < size; i++) {
                    if (groupMemberAdapter.getData().get(i).member.memberUID.equals(uid)) {
                        groupMemberAdapter.getData().get(i).checked = 0;
                        groupMemberAdapter.notifyItemChanged(i, groupMemberAdapter.getData().get(i));
                        break;
                    }
                }
                new Handler().postDelayed(() -> setRightTv(), 300);
            }

            @Override
            public void searchUser(String key) {
                page = 1;
                searchKey = key;
                groupMemberAdapter.setSearch(searchKey);
                msVBinding.refreshLayout.setEnableLoadMore(true);
                getData();
            }
        });
        FriendUIEntity ui = new FriendUIEntity(new MSChannel("", MSChannelType.PERSONAL));
        ui.itemType = 1;
        selectedAdapter.addData(ui);
        msVBinding.selectUserRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        msVBinding.selectUserRecyclerView.setAdapter(selectedAdapter);
    }

    private void setRightTv() {
        int count = selectedAdapter.getData().size() - 1;
        if (count > 0) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.format("%s(%s)", getString(R.string.delete), count));
            showTitleRightView();
        } else {
            textView.setText(R.string.delete);
            textView.setVisibility(View.INVISIBLE);
            hideTitleRightView();
        }

    }

    @SuppressLint("ClickableViewAccessibility")
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

            }
        });
        selectedAdapter.setOnItemClickListener((adapter, view, position) -> {
            FriendUIEntity userEntity = selectedAdapter.getItem(position);
            if (userEntity != null && userEntity.itemType == 0) {
                if (!userEntity.isSetDelete) {
                    userEntity.isSetDelete = true;
                    selectedAdapter.notifyItemChanged(position, userEntity);
                    return;
                }
                boolean isRemove = false;
                for (int i = 0, size = groupMemberAdapter.getData().size(); i < size; i++) {
                    if (groupMemberAdapter.getData().get(i).member.memberUID.equalsIgnoreCase(userEntity.channel.channelID) && groupMemberAdapter.getData().get(i).isCanCheck == 1) {
                        groupMemberAdapter.getData().get(i).checked = groupMemberAdapter.getData().get(i).checked == 1 ? 0 : 1;
                        groupMemberAdapter.notifyItemChanged(i, groupMemberAdapter.getData().get(i));
                        isRemove = true;
                        break;
                    }
                }
                if (isRemove) {
                    for (int i = 0, size = selectedAdapter.getData().size(); i < size; i++) {
                        selectedAdapter.getData().get(i).isSetDelete = false;
                    }
                    selectedAdapter.removeAt(position);
                    setRightTv();
                }
            }
        });
        groupMemberAdapter.setOnItemClickListener((adapter, view1, position) -> {
            GroupMemberEntity groupMemberEntity = (GroupMemberEntity) adapter.getItem(position);
            if (groupMemberEntity != null) {
                groupMemberEntity.checked = groupMemberEntity.checked == 1 ? 0 : 1;

                if (groupMemberEntity.checked == 1) {
                    groupMemberEntity.isSetDelete = false;
                    MSChannel channel = new MSChannel();
                    channel.channelName = groupMemberEntity.member.memberName;
                    channel.channelRemark = groupMemberEntity.member.memberRemark;
                    channel.channelID = groupMemberEntity.member.memberUID;
                    channel.channelType = MSChannelType.PERSONAL;
                    channel.avatar = groupMemberEntity.member.memberAvatar;
                    channel.avatarCacheKey = groupMemberEntity.member.memberAvatarCacheKey;
                    FriendUIEntity uiEntity = new FriendUIEntity(channel);
                    uiEntity.isSetDelete = false;
                    selectedAdapter.addData(selectedAdapter.getData().size() - 1, uiEntity);
                    msVBinding.selectUserRecyclerView.scrollToPosition(selectedAdapter.getData().size() - 1);
                } else {
                    for (int i = 0, size = selectedAdapter.getData().size(); i < size; i++) {
                        if (selectedAdapter.getData().get(i).channel.channelID.equalsIgnoreCase(groupMemberEntity.member.memberUID)) {
                            selectedAdapter.removeAt(i);
                            break;
                        }
                    }
                }

            }
            selectedAdapter.notifyItemChanged(selectedAdapter.getData().size() - 1, selectedAdapter.getData().get(selectedAdapter.getData().size() - 1));
            SoftKeyboardUtils.getInstance().hideSoftKeyboard(this);

            adapter.notifyItemChanged(position, groupMemberEntity);
            setRightTv();
        });


        msVBinding.selectUserRecyclerView.setOnTouchListener((view, motionEvent) -> {
            View childView = msVBinding.selectUserRecyclerView.getChildAt(selectedAdapter.getData().size() - 1);
            if (childView != null) {
                EditText editText = childView.findViewById(R.id.searchEt);
                SoftKeyboardUtils.getInstance().showSoftKeyBoard(DeleteGroupMemberActivity.this, editText);
            }
            return false;
        });
    }

    @Override
    protected void initData() {
        super.initData();
        groupId = getIntent().getStringExtra("groupId");
        getData();
    }


    private void getData() {
        MSIM.getInstance().getChannelMembersManager().getWithPageOrSearch(groupId, MSChannelType.GROUP, searchKey, page, 20, (list, b) -> {
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
        List<GroupMemberEntity> tempList = new ArrayList<>();
        String loginUID = MSConfig.getInstance().getUid();
        int loginMemberRole = 0;
        MSChannelMember loginUserMember = MSIM.getInstance().getChannelMembersManager().getMember(groupId, MSChannelType.GROUP, loginUID);
        if (loginUserMember != null) {
            loginMemberRole = loginUserMember.role;
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            if (loginUID.equals(list.get(i).memberUID)) continue;
            if (loginMemberRole == MSChannelMemberRole.manager && list.get(i).role != MSChannelMemberRole.normal){
                continue;
            }
            GroupMemberEntity entity = new GroupMemberEntity(list.get(i));
            for (int j = 0, len = selectedAdapter.getData().size(); j < len; j++) {
                if (list.get(i).memberUID.equals(selectedAdapter.getData().get(j).channel.channelID)) {
                    entity.checked = 1;
                    break;
                }
            }
            tempList.add(entity);

        }
        msVBinding.refreshLayout.finishLoadMore();
        if (page == 1) {
            groupMemberAdapter.setList(tempList);
        } else {
            groupMemberAdapter.addData(tempList);
        }
        if (MSReader.isEmpty(tempList)) {
            msVBinding.refreshLayout.finishLoadMoreWithNoMoreData();
        }
    }
}
