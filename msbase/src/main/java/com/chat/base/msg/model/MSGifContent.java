package com.chat.base.msg.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.chat.base.MSBaseApplication;
import com.chat.base.R;
import com.mushanyux.mushanim.message.type.MSMsgContentType;
import com.mushanyux.mushanim.msgmodel.MSMediaMessageContent;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * gif消息
 */
public class MSGifContent extends MSMediaMessageContent implements Parcelable {
    public int width;
    public int height;
    public String category;
    public String placeholder;
    public String format;
    public String title;

    public MSGifContent() {
        type = MSMsgContentType.MS_GIF;
    }

    @Override
    public JSONObject encodeMsg() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("width", width);
            jsonObject.put("height", height);
            jsonObject.put("url", url);
            jsonObject.put("category", category);
            jsonObject.put("title", title);
            jsonObject.put("placeholder", placeholder);
            jsonObject.put("format", format);
            jsonObject.put("localPath", localPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public MSMessageContent decodeMsg(JSONObject jsonObject) {
        this.width = jsonObject.optInt("width");
        this.height = jsonObject.optInt("height");
        this.url = jsonObject.optString("url");
        this.category = jsonObject.optString("category");
        this.title = jsonObject.optString("title");
        this.localPath = jsonObject.optString("localPath");
        this.placeholder = jsonObject.optString("placeholder");
        this.format = jsonObject.optString("format");
        return this;
    }

    protected MSGifContent(Parcel in) {
        super(in);
        width = in.readInt();
        height = in.readInt();
        url = in.readString();
        category = in.readString();
        title = in.readString();
        placeholder = in.readString();
        format = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(url);
        dest.writeString(category);
        dest.writeString(title);
        dest.writeString(placeholder);
        dest.writeString(format);
    }


    public static final Creator<MSGifContent> CREATOR = new Creator<MSGifContent>() {
        @Override
        public MSGifContent createFromParcel(Parcel in) {
            return new MSGifContent(in);
        }

        @Override
        public MSGifContent[] newArray(int size) {
            return new MSGifContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getDisplayContent() {
        return "[GIF]";
    }
}
