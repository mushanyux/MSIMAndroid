package com.chat.uikit.chat.msgmodel;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.chat.uikit.MSUIKitApplication;
import com.chat.uikit.R;
import com.mushanyux.mushanim.message.type.MSMsgContentType;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 名片消息
 */
public class MSCardContent extends MSMessageContent {
    public MSCardContent() {
        type = MSMsgContentType.MS_CARD;
    }

    public String uid;
    public String name;
    public String vercode;
    public String avatar;

    @NonNull
    @Override
    public JSONObject encodeMsg() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", uid);
            jsonObject.put("name", name);
            jsonObject.put("avatar", avatar);
            jsonObject.put("vercode", vercode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public MSMessageContent decodeMsg(JSONObject jsonObject) {
        uid = jsonObject.optString("uid");
        name = jsonObject.optString("name");
        avatar = jsonObject.optString("avatar");
        vercode = jsonObject.optString("vercode");
        return this;
    }

    protected MSCardContent(Parcel in) {
        super(in);
        uid = in.readString();
        name = in.readString();
        avatar = in.readString();
        vercode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(avatar);
        dest.writeString(vercode);
    }


    public static final Creator<MSCardContent> CREATOR = new Creator<MSCardContent>() {
        @Override
        public MSCardContent createFromParcel(Parcel in) {
            return new MSCardContent(in);
        }

        @Override
        public MSCardContent[] newArray(int size) {
            return new MSCardContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getDisplayContent() {
        return MSUIKitApplication.getInstance().getContext().getString(R.string.last_msg_card);
    }

}
