package com.chat.uikit.group.service;

import com.chat.base.base.MSBasePresenter;
import com.chat.base.base.MSBaseView;
import com.chat.base.entity.ChannelInfoEntity;
import com.chat.uikit.group.GroupEntity;

import java.util.List;

/**
 * 群相关
 */
public class GroupContract {
    public interface GroupPresenter extends MSBasePresenter {
        void getGroupInfo(String groupNo);
        void updateGroupSetting(String groupNo, String key, int value);
        void getQrData(String groupNo);
        void getMyGroups();

    }

    public interface GroupView extends MSBaseView {
        void onGroupInfo(ChannelInfoEntity channelInfoEntity);
        void onRefreshGroupSetting(String key, int value);
        void setQrData(int day, String qrCode, String expire);
        void setMyGroups(List<GroupEntity> list);
    }
}
