package com.chat.uikit.robot.service;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chat.base.base.MSBaseModel;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.net.IRequestResultListener;
import com.chat.base.utils.MSReader;
import com.chat.uikit.robot.entity.MSRobotEntity;
import com.chat.uikit.robot.entity.MSRobotInlineQueryResult;
import com.chat.uikit.robot.entity.MSRobotMenuEntity;
import com.chat.uikit.robot.entity.MSSyncRobotEntity;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSRobot;
import com.mushanyux.mushanim.entity.MSRobotMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MSRobotModel extends MSBaseModel {
    private MSRobotModel() {
    }

    private static class MSRobotModelBinder {
        final static MSRobotModel model = new MSRobotModel();
    }

    public static MSRobotModel getInstance() {
        return MSRobotModelBinder.model;
    }

    public void syncRobotData(MSChannel channel) {
        new Thread(() -> sync(channel)).start();
    }

    private void sync(MSChannel channel) {

        boolean isSync = false;
        List<MSRobotEntity> list = new ArrayList<>();
        if (channel.robot == 1) {
            isSync = true;
            MSRobotEntity entity = new MSRobotEntity();
            entity.robot_id = channel.channelID;
            MSRobot robot = MSIM.getInstance().getRobotManager().getWithRobotID(channel.channelID);
            if (robot != null) {
                entity.version = robot.version;
            } else {
                entity.version = 0;
            }
            list.add(entity);
        }
        if (channel.channelType == MSChannelType.GROUP) {
            List<MSChannelMember> memberList = MSIM.getInstance().getChannelMembersManager().getRobotMembers(channel.channelID, channel.channelType);
            if (MSReader.isNotEmpty(memberList)) {
                List<String> robotIds = new ArrayList<>();
                for (MSChannelMember member : memberList) {
                    robotIds.add(member.memberUID);
                }
                List<MSRobot> robotList = MSIM.getInstance().getRobotManager().getWithRobotIds(robotIds);
                if (MSReader.isNotEmpty(robotList)) {
                    for (String robotID : robotIds) {
                        long version = 0;
                        for (MSRobot robot : robotList) {
                            if (robotID.equals(robot.robotID)) {
                                version = robot.version;
                                break;
                            }
                        }
                        list.add(new MSRobotEntity(robotID, version));
                    }
                } else {
                    for (String robotID : robotIds) {
                        list.add(new MSRobotEntity(robotID, 0));
                    }
                }
                isSync = true;
            }
        }
        if (isSync && MSReader.isNotEmpty(list)) {
            MSRobotModel.getInstance().syncRobot(1, list);
        }
    }

    public void syncRobot(int syncType, List<MSRobotEntity> list) {
        JSONArray jsonArray = new JSONArray();
        for (MSRobotEntity entity : list) {
            JSONObject jsonObject = new JSONObject();
            if (syncType == 1) {
                jsonObject.put("robot_id", entity.robot_id);
            } else
                jsonObject.put("username", entity.username);
            jsonObject.put("version", entity.version);
            jsonArray.add(jsonObject);
        }
        request(createService(MSRobotService.class).syncRobot(jsonArray), new IRequestResultListener<List<MSSyncRobotEntity>>() {
            @Override
            public void onSuccess(List<MSSyncRobotEntity> result) {
                List<MSRobot> robotList = new ArrayList<>();
                List<MSRobotMenu> menuList = new ArrayList<>();
                if (MSReader.isNotEmpty(result)) {
                    for (MSSyncRobotEntity entity : result) {
                        MSRobot robot = new MSRobot();
                        robot.username = entity.username;
                        robot.placeholder = entity.placeholder;
                        robot.inlineOn = entity.inline_on;
                        robot.robotID = entity.robot_id;
                        robot.status = entity.status;
                        robot.version = entity.version;
                        robot.updatedAT = entity.updated_at;
                        robot.createdAT = entity.created_at;
                        robotList.add(robot);

                        if (MSReader.isNotEmpty(entity.menus)) {
                            for (MSRobotMenuEntity mRobotMenuEntity : entity.menus) {
                                MSRobotMenu menu = new MSRobotMenu();
                                menu.cmd = mRobotMenuEntity.cmd;
                                menu.type = mRobotMenuEntity.type;
                                menu.remark = mRobotMenuEntity.remark;
                                menu.robotID = mRobotMenuEntity.robot_id;
                                menu.createdAT = mRobotMenuEntity.created_at;
                                menu.updatedAT = mRobotMenuEntity.updated_at;
                                menuList.add(menu);
                            }
                        }
                    }
                }
                // 无数据也调用sdk保存是为了让页面刷新出robot menus
                MSIM.getInstance().getRobotManager().saveOrUpdateRobotMenus(menuList);
                MSIM.getInstance().getRobotManager().saveOrUpdateRobots(robotList);
            }

            @Override
            public void onFail(int code, String msg) {

            }
        });
    }

    public List<MSRobotMenuEntity> getRobotMenus(String channelID, byte channelType) {
        List<MSRobotMenuEntity> list = new ArrayList<>();
        if (channelType == MSChannelType.PERSONAL) {
            MSRobot robot = MSIM.getInstance().getRobotManager().getWithRobotID(channelID);
            if (robot != null && !TextUtils.isEmpty(robot.robotID) && robot.status == 1) {
                List<MSRobotMenu> menus = MSIM.getInstance().getRobotManager().getRobotMenus(robot.robotID);
                for (MSRobotMenu menu : menus) {
                    MSRobotMenuEntity entity = new MSRobotMenuEntity();
                    entity.robot_id = menu.robotID;
                    entity.cmd = menu.cmd;
                    entity.remark = menu.remark;
                    entity.type = menu.type;
                    list.add(entity);
                }
            }
        } else {
            List<MSChannelMember> memberList = MSIM.getInstance().getChannelMembersManager().getRobotMembers(channelID, channelType);
            if (MSReader.isNotEmpty(memberList)) {
                List<String> robotIds = new ArrayList<>();
                for (MSChannelMember member : memberList) {
                    if (!TextUtils.isEmpty(member.memberUID) && member.robot == 1) {
                        robotIds.add(member.memberUID);
                    }
                }
                if (MSReader.isNotEmpty(robotIds)) {
                    HashMap<String, List<MSRobotMenuEntity>> hashMap = new HashMap<>();
                    List<MSRobot> robotList = MSIM.getInstance().getRobotManager().getWithRobotIds(robotIds);
                    List<MSRobotMenu> menuList = MSIM.getInstance().getRobotManager().getRobotMenus(robotIds);
                    for (MSRobotMenu menu : menuList) {
                        boolean isAddMenu = true;
                        if (MSReader.isNotEmpty(robotList)) {
                            for (MSRobot robot : robotList) {
                                if (menu.robotID.equals(robot.robotID)) {
                                    if (robot.status == 0) {
                                        isAddMenu = false;
                                    }
                                    break;
                                }
                            }
                        }
                        if (!isAddMenu) continue;
                        MSRobotMenuEntity entity = new MSRobotMenuEntity();
                        entity.cmd = menu.cmd;
                        entity.robot_id = menu.robotID;
                        entity.remark = menu.remark;
                        entity.type = menu.type;
                        List<MSRobotMenuEntity> tempList;
                        if (hashMap.containsKey(menu.robotID)) {
                            tempList = hashMap.get(menu.robotID);
                        } else {
                            tempList = new ArrayList<>();
                        }
                        tempList.add(entity);
                        hashMap.put(menu.robotID, tempList);
                    }

                    for (String key : hashMap.keySet()) {
                        list.addAll(hashMap.get(key));
                    }
                }
            }
        }
        return list;
    }

    public void inlineQuery(String offset, String username, String searchContent, String channelID, byte channelType, final InlineQueryListener inlineQueryListener) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("query", searchContent);
        jsonObject.put("username", username);
        jsonObject.put("channel_id", channelID);
        jsonObject.put("offset", offset);
        jsonObject.put("channel_type", channelType);
        request(createService(MSRobotService.class).inlineQuery(jsonObject), new IRequestResultListener<MSRobotInlineQueryResult>() {
            @Override
            public void onSuccess(MSRobotInlineQueryResult result) {
                inlineQueryListener.onResult(HttpResponseCode.success, "", result);
            }

            @Override
            public void onFail(int code, String msg) {
                inlineQueryListener.onResult(code, msg, null);
            }
        });
    }

    public interface InlineQueryListener {
        void onResult(int code, String msg, MSRobotInlineQueryResult result);
    }
}
