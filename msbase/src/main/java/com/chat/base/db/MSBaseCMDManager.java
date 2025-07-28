package com.chat.base.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.chat.base.MSBaseApplication;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.utils.MSLogUtils;
import com.chat.base.utils.MSReader;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSCMD;
import com.mushanyux.mushanim.entity.MSCMDKeys;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelExtras;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * cmd管理
 */
public class MSBaseCMDManager {
    private MSBaseCMDManager() {
    }

    private static class BaseCMDManagerBinder {
        final static MSBaseCMDManager cmdManager = new MSBaseCMDManager();
    }

    public static MSBaseCMDManager getInstance() {
        return BaseCMDManagerBinder.cmdManager;
    }

    //添加
    public void addCmd(List<MSBaseCMD> list) {
        if (MSReader.isEmpty(list)) return;
        try {
            List<MSBaseCMD> tempList = new ArrayList<>();
            List<ContentValues> cvList = new ArrayList<>();
            List<String> clientMsgNos = new ArrayList<>();
            List<String> msgIds = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                clientMsgNos.add(list.get(i).client_msg_no);
                msgIds.add(list.get(i).message_id);
                // if (!isExistWithClientMsgNo(list.get(i).client_msg_no) && !isExistWithMessageID(list.get(i).message_id))
//                cvList.add(getContentValues(list.get(i)));
            }
            tempList.addAll(queryWithClientMsgNos(clientMsgNos));
            tempList.addAll(queryWithMsgIds(msgIds));
            boolean isCheck = MSReader.isNotEmpty(tempList);

            for (int i = 0; i < list.size(); i++) {
                boolean isAdd = true;
                if (isCheck) {
                    for (MSBaseCMD cmd : tempList) {
                        if (cmd.client_msg_no.equals(list.get(i).client_msg_no) || cmd.message_id.equals(list.get(i).message_id)) {
                            isAdd = false;
                            break;
                        }
                    }
                }
                if (isAdd) {
                    cvList.add(getContentValues(list.get(i)));
                }
            }
            MSBaseApplication.getInstance().getDbHelper().getDB()
                    .beginTransaction();
            for (ContentValues cv : cvList) {
                MSBaseApplication.getInstance().getDbHelper()
                        .insert("cmd", cv);
            }
            MSBaseApplication.getInstance().getDbHelper().getDB()
                    .setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            if (MSBaseApplication.getInstance().getDbHelper().getDB().inTransaction()) {
                MSBaseApplication.getInstance().getDbHelper().getDB()
                        .endTransaction();
            }
        }
    }

    private List<MSBaseCMD> queryWithClientMsgNos(List<String> clientMsgNos) {
        List<MSBaseCMD> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("select * from cmd where client_msg_no in (");
        for (int i = 0, size = clientMsgNos.size(); i < size; i++) {
            if (i != 0) sb.append(",");
            sb.append("'").append(clientMsgNos.get(i)).append("'");
        }
        sb.append(")");
        try (Cursor cursor = MSBaseApplication.getInstance().getDbHelper().rawQuery(sb.toString())) {
            if (cursor == null) {
                return list;
            }
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                MSBaseCMD cmd = serializeCmd(cursor);
                list.add(cmd);
            }
        }
        return list;
    }

    private List<MSBaseCMD> queryWithMsgIds(List<String> clientMsgNos) {
        List<MSBaseCMD> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("select * from cmd where message_id in (");
        for (int i = 0, size = clientMsgNos.size(); i < size; i++) {
            if (i != 0) sb.append(",");
            sb.append("'").append(clientMsgNos.get(i)).append("'");
        }
        sb.append(")");
        try (Cursor cursor = MSBaseApplication.getInstance().getDbHelper().rawQuery(sb.toString())) {
            if (cursor == null) {
                return list;
            }
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                MSBaseCMD cmd = serializeCmd(cursor);
                list.add(cmd);
            }
        }
        return list;
    }

    //是否存在某条cmd
    private boolean isExistWithClientMsgNo(String clientMsgNo) {
        String sql = "select * from cmd where client_msg_no = " + "\"" + clientMsgNo + "\"";
        Cursor cursor = MSBaseApplication.getInstance().getDbHelper().rawQuery(sql, null);
        boolean isExist;
        if (cursor == null) {
            isExist = false;
        } else {
            isExist = cursor.moveToLast();
            cursor.close();
        }
        return isExist;
    }

    //是否存在某条cmd
    private boolean isExistWithMessageID(String messageID) {
        String sql = "select * from cmd where message_id = " + "\"" + messageID + "\"";
        Cursor cursor = MSBaseApplication.getInstance().getDbHelper().rawQuery(sql, null);
        boolean isExist;
        if (cursor == null) {
            isExist = false;
        } else {
            isExist = cursor.moveToLast();
            cursor.close();
        }
        return isExist;
    }


    //删除某条cmd
    public void deleteCmd(String client_msg_no) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("is_deleted", 1);
        String[] update = new String[1];
        update[0] = client_msg_no;
        MSBaseApplication.getInstance().getDbHelper().update("cmd", contentValues, "client_msg_no=?", update);
    }

    //查询所有cmd
    private List<MSBaseCMD> queryAllCmd() {
        List<MSBaseCMD> list = new ArrayList<>();
        String sql = "select * from cmd where is_deleted=0";
        Cursor cursor = MSBaseApplication.getInstance().getDbHelper().rawQuery(sql, null);
        if (cursor == null) {
            return list;
        }
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            list.add(serializeCmd(cursor));
        }
        cursor.close();
        return list;
    }

    private ContentValues getContentValues(MSBaseCMD MSBaseCmd) {
        ContentValues contentValues = new ContentValues();
        if (MSBaseCmd == null) {
            return contentValues;
        }
        contentValues.put("client_msg_no", MSBaseCmd.client_msg_no);
        contentValues.put("cmd", MSBaseCmd.cmd);
        contentValues.put("sign", MSBaseCmd.sign);
        contentValues.put("created_at", MSBaseCmd.created_at);
        contentValues.put("message_id", MSBaseCmd.message_id);
        contentValues.put("message_seq", MSBaseCmd.message_seq);
        contentValues.put("param", MSBaseCmd.param);
        contentValues.put("timestamp", MSBaseCmd.timestamp);
        return contentValues;
    }

    @SuppressLint("Range")
    private MSBaseCMD serializeCmd(Cursor cursor) {
        MSBaseCMD MSBaseCmd = new MSBaseCMD();
        MSBaseCmd.client_msg_no = MSCursor.readString(cursor, "client_msg_no");
        MSBaseCmd.cmd = MSCursor.readString(cursor, "cmd");
        MSBaseCmd.created_at = MSCursor.readString(cursor, "created_at");
        MSBaseCmd.message_id = MSCursor.readString(cursor, "message_id");
        MSBaseCmd.message_seq = MSCursor.readLong(cursor, "message_seq");
        MSBaseCmd.param = MSCursor.readString(cursor, "param");
        MSBaseCmd.sign = MSCursor.readString(cursor, "sign");
        MSBaseCmd.timestamp = MSCursor.readLong(cursor, "timestamp");
        return MSBaseCmd;
    }

    private void handleRevokeCmd(List<MSBaseCMD> list) {
        final Timer[] timer = {new Timer()};
        final int[] i = {0};
        timer[0].schedule(new TimerTask() {
            @Override
            public void run() {
                if (i[0] == list.size() - 1) {
                    timer[0].cancel();
                    timer[0] = null;
                }
                MSIM.getInstance().getCMDManager().handleCMD(list.get(i[0]).cmd, list.get(i[0]).param, list.get(i[0]).sign);
                i[0]++;
            }
        }, 0, 100);
    }

    //处理cmd
    public void handleCmd() {
        List<MSCMD> rtcList = new ArrayList<>();
        List<MSBaseCMD> cmdList = queryAllCmd();
        if (MSReader.isEmpty(cmdList)) return;
        HashMap<String, List<MSBaseCMD>> revokeMap = new HashMap<>();
        for (MSBaseCMD MSBaseCmd : cmdList) {
            if (MSBaseCmd.is_deleted == 0 && !TextUtils.isEmpty(MSBaseCmd.cmd)) {
                if (MSBaseCmd.cmd.equals(MSCMDKeys.ms_messageRevoke)) {
                    if (!TextUtils.isEmpty(MSBaseCmd.param)) {
                        try {
                            String channelID = "";
                            byte channelType = 0;
                            JSONObject jsonObject = new JSONObject(MSBaseCmd.param);
                            if (jsonObject.has("channel_id")) {
                                channelID = jsonObject.optString("channel_id");
                            }
                            if (jsonObject.has("channel_type")) {
                                channelType = (byte) jsonObject.optInt("channel_type");
                            }
                            if (!TextUtils.isEmpty(channelID)) {
                                List<MSBaseCMD> list;
                                String key = String.format("%s,%s", channelID, channelType);
                                if (revokeMap.containsKey(key)) {
                                    list = revokeMap.get(key);
                                    if (list == null) list = new ArrayList<>();
                                } else {
                                    list = new ArrayList<>();
                                }
                                list.add(MSBaseCmd);
                                revokeMap.put(key, list);
                            }
                        } catch (JSONException e) {
                            MSLogUtils.e("处理cmd错误");
                        }
                    }
                } else if (MSBaseCmd.cmd.startsWith("rtc.p2p")) {
                    try {
                        JSONObject jsonObject = new JSONObject(MSBaseCmd.param);
                        rtcList.add(new MSCMD(MSBaseCmd.cmd, jsonObject));
                    } catch (JSONException e) {
                        MSLogUtils.e("解析cmd错误");
                    }
                } else
                    MSIM.getInstance().getCMDManager().handleCMD(MSBaseCmd.cmd, MSBaseCmd.param, MSBaseCmd.sign);
            }
        }
        if (MSReader.isNotEmpty(rtcList)) {
            EndpointManager.getInstance().invoke("rtc_offline_data", rtcList);
        }
        if (!revokeMap.isEmpty()) {
            List<MSBaseCMD> tempList = new ArrayList<>();
            for (String key : revokeMap.keySet()) {
                String channelID = key.split(",")[0];
                byte channelType = Byte.parseByte(key.split(",")[1]);
                MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(channelID, channelType);
                //是否撤回提醒
                int revokeRemind = 0;
                if (channel != null && channel.localExtra != null && channel.localExtra.containsKey(MSChannelExtras.revokeRemind)) {
                    Object object = channel.localExtra.get(MSChannelExtras.revokeRemind);
                    if (object != null) {
                        revokeRemind = (int) object;
                    }
                }
                if (revokeRemind == 1) {
                    EndpointManager.getInstance().invoke("syncExtraMsg", new MSChannel(channelID, channelType));
                } else {
                    List<MSBaseCMD> list = revokeMap.get(key);
                    if (MSReader.isNotEmpty(list))
                        tempList.addAll(list);
                }
                if (MSReader.isNotEmpty(tempList)) {
                    new Thread(() -> handleRevokeCmd(tempList)).start();
                }
            }
        }
        try {
            MSBaseApplication.getInstance().getDbHelper().getDB()
                    .beginTransaction();
            for (int i = 0; i < cmdList.size(); i++) {
                deleteCmd(cmdList.get(i).client_msg_no);
            }
            MSBaseApplication.getInstance().getDbHelper().getDB()
                    .setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            if (MSBaseApplication.getInstance().getDbHelper() != null && MSBaseApplication.getInstance().getDbHelper().getDB().inTransaction()) {
                MSBaseApplication.getInstance().getDbHelper().getDB()
                        .endTransaction();
            }
        }
    }
}
