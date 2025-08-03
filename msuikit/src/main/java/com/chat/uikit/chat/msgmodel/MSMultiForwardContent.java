package com.chat.uikit.chat.msgmodel;

import android.os.Parcel;
import android.text.TextUtils;

import com.chat.base.msgitem.MSContentType;
import com.chat.base.utils.MSLogUtils;
import com.chat.base.utils.MSReader;
import com.chat.uikit.R;
import com.chat.uikit.MSUIKitApplication;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 合并转发消息
 */
public class MSMultiForwardContent extends MSMessageContent {
    public byte channelType;
    public List<MSChannel> userList;
    public List<MSMsg> msgList;

    public MSMultiForwardContent() {
        type = MSContentType.MS_MULTIPLE_FORWARD;
    }

    @Override
    public MSMessageContent decodeMsg(JSONObject jsonObject) {
        channelType = (byte) jsonObject.optInt("channel_type");
        JSONArray msgArr = jsonObject.optJSONArray("msgs");
        if (msgArr != null && msgArr.length() > 0) {
            msgList = new ArrayList<>();
            for (int i = 0, size = msgArr.length(); i < size; i++) {
                JSONObject msgJson = msgArr.optJSONObject(i);
                MSMsg msg = new MSMsg();
                JSONObject contentJson = msgJson.optJSONObject("payload");
                if (contentJson != null) {
                    msg.content = contentJson.toString();
                    msg.baseContentMsgModel = MSIM.getInstance().getMsgManager().getMsgContentModel(contentJson);
                    if (msg.baseContentMsgModel != null) {
                        msg.type = msg.baseContentMsgModel.type;
                    }
                } else {
                    msg.baseContentMsgModel = new MSMessageContent();
                    msg.type = MSContentType.unknown_msg;
                }
                msg.timestamp = msgJson.optLong("timestamp");
                msg.messageID = msgJson.optString("message_id");
                if (msgJson.has("from_uid")) {
                    msg.fromUID = msgJson.optString("from_uid");
                }
                msgList.add(msg);
            }
        }
        JSONArray userArr = jsonObject.optJSONArray("users");
        if (userArr != null && userArr.length() > 0) {
            userList = new ArrayList<>();
            for (int i = 0, size = userArr.length(); i < size; i++) {
                JSONObject userJson = userArr.optJSONObject(i);
                MSChannel channel = new MSChannel();
                if (userJson.has("uid"))
                    channel.channelID = userJson.optString("uid");
                if (userJson.has("name"))
                    channel.channelName = userJson.optString("name");
                if (userJson.has("avatar"))
                    channel.avatar = userJson.optString("avatar");
                userList.add(channel);
            }
        }
        return this;
    }

    @Override
    public JSONObject encodeMsg() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("channel_type", channelType);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0, size = msgList.size(); i < size; i++) {
                JSONObject json = new JSONObject();
                if (!TextUtils.isEmpty(msgList.get(i).content)) {
                    json.put("payload", new JSONObject(msgList.get(i).content));
                }
                json.put("timestamp", msgList.get(i).timestamp);
                json.put("message_id", msgList.get(i).messageID);
                json.put("from_uid", msgList.get(i).fromUID);
                jsonArray.put(json);
            }
            jsonObject.put("msgs", jsonArray);
            if (MSReader.isNotEmpty(userList)) {
                JSONArray userArr = new JSONArray();
                for (int i = 0, size = userList.size(); i < size; i++) {
                    JSONObject json = new JSONObject();
                    json.put("uid", userList.get(i).channelID);
                    json.put("name", userList.get(i).channelName);
                    json.put("avatar", userList.get(i).avatar);
                    userArr.put(json);
                }
                jsonObject.put("users", userArr);
            }
        } catch (JSONException e) {
            MSLogUtils.e("编码合并转发消息错误");
        }
        return jsonObject;
    }

    @Override
    public String getDisplayContent() {
        return MSUIKitApplication.getInstance().getContext().getString(R.string.last_msg_chat_record);
    }

    @Override
    public String getSearchableWord() {
        return MSUIKitApplication.getInstance().getContext().getString(R.string.last_msg_chat_record);
    }

    public MSMultiForwardContent(Parcel in) {
        super(in);
        channelType = in.readByte();
        userList = in.createTypedArrayList(MSChannel.CREATOR);
        msgList = in.createTypedArrayList(MSMsg.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(channelType);
        dest.writeTypedList(userList);
        dest.writeTypedList(msgList);
    }

    public static final Creator<MSMultiForwardContent> CREATOR = new Creator<MSMultiForwardContent>() {
        @Override
        public MSMultiForwardContent createFromParcel(Parcel in) {
            return new MSMultiForwardContent(in);
        }

        @Override
        public MSMultiForwardContent[] newArray(int size) {
            return new MSMultiForwardContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
