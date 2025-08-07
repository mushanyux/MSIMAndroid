package com.chat.uikit.group.service;


import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.MSToastUtils;

import java.lang.ref.WeakReference;

/**
 * 群相关
 */
public class GroupPresenter implements GroupContract.GroupPresenter {

    private final WeakReference<GroupContract.GroupView> groupView;

    public GroupPresenter(GroupContract.GroupView _groupView) {
        groupView = new WeakReference<>(_groupView);
    }


    @Override
    public void getGroupInfo(String groupNo) {
        GroupModel.getInstance().getGroupInfo(groupNo, (code, msg, groupEntity) -> {
            if (code == HttpResponseCode.success) {
                if (groupView.get() != null) {
                    groupView.get().onGroupInfo(groupEntity);
                }
            } else MSToastUtils.getInstance().showToastNormal(msg);
        });
    }


    @Override
    public void updateGroupSetting(String groupNo, String key, int value) {
        GroupModel.getInstance().updateGroupSetting(groupNo, key, value, (code, msg) -> {
            if (code == HttpResponseCode.success) {
                if (groupView.get() != null) groupView.get().onRefreshGroupSetting(key, value);
            } else MSToastUtils.getInstance().showToastNormal(msg);
        });
    }

    @Override
    public void getQrData(String groupNo) {
        GroupModel.getInstance().getGroupQr(groupNo, (code, msg, day, qrCode, expire) -> {
            if (groupView.get() != null) {
                if (code == HttpResponseCode.success) {
                    groupView.get().setQrData(day, qrCode, expire);
                } else {
                    MSToastUtils.getInstance().showToastNormal(msg);
                }
            }
        });
    }

    @Override
    public void getMyGroups() {
        GroupModel.getInstance().getMyGroups((code, msg, list) -> {
            if (groupView.get() != null) {
                if (code == HttpResponseCode.success) {
                    groupView.get().setMyGroups(list);
                } else {
                    MSToastUtils.getInstance().showToastNormal(msg);
                }
            }
        });
    }

    @Override
    public void showLoading() {

    }
}
