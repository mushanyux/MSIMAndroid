package com.chat.uikit.chat;

import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.utils.MSTimeUtils;
import com.chat.uikit.R;
import com.chat.uikit.chat.adapter.ChatMultiForwardDetailAdapter;
import com.chat.uikit.chat.msgmodel.MSMultiForwardContent;
import com.chat.uikit.databinding.ActCommonListLayoutWhiteBinding;
import com.chat.uikit.enity.ChatMultiForwardEntity;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSCMDKeys;
import com.mushanyux.mushanim.entity.MSMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * 合并转发消息详情
 */
public class ChatMultiForwardDetailActivity extends MSBaseActivity<ActCommonListLayoutWhiteBinding> {

    MSMultiForwardContent MSMultiForwardContent;
    String clientMsgNo = "";

    @Override
    protected ActCommonListLayoutWhiteBinding getViewBinding() {
        return ActCommonListLayoutWhiteBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        String title;
        if (MSMultiForwardContent.channelType == 1) {
            if (MSMultiForwardContent.userList.size() > 1) {
                StringBuilder sBuilder = new StringBuilder();
                for (int i = 0; i < MSMultiForwardContent.userList.size(); i++) {
                    if (!TextUtils.isEmpty(sBuilder))
                        sBuilder.append("、");
                    sBuilder.append(MSMultiForwardContent.userList.get(i).channelName);
                }
                title = sBuilder.toString();
            } else title = MSMultiForwardContent.userList.get(0).channelName;
        } else {
            title = getString(R.string.group_chat);
        }
        titleTv.setText(String.format(getString(R.string.chat_title_records), title));
    }

    @Override
    protected void initPresenter() {
        clientMsgNo = getIntent().getStringExtra("client_msg_no");
        MSMsg msg = MSIM.getInstance().getMsgManager().getWithClientMsgNO(clientMsgNo);
        MSMultiForwardContent = (MSMultiForwardContent) msg.baseContentMsgModel;
        if (MSMultiForwardContent == null) {
            showToast("传入数据有误！");
            finish();
        }
        long minTime = 0;
        long maxTime = 0;
        for (int i = 0, size = MSMultiForwardContent.msgList.size(); i < size; i++) {
            if (MSMultiForwardContent.msgList.get(i).timestamp > maxTime || maxTime == 0)
                maxTime = MSMultiForwardContent.msgList.get(i).timestamp;
            if (MSMultiForwardContent.msgList.get(i).timestamp < minTime || minTime == 0)
                minTime = MSMultiForwardContent.msgList.get(i).timestamp;
        }
        String time;
        boolean showDetailTime;
        if (!MSTimeUtils.getInstance().isSameDayOfMillis(minTime * 1000, maxTime * 1000)) {
            showDetailTime = true;
            String tempTime1 = MSTimeUtils.getInstance().time2DataDay1(minTime * 1000);
            String tempTime2 = MSTimeUtils.getInstance().time2DataDay1(maxTime * 1000);
            time = String.format(getString(R.string.time_section), tempTime1, tempTime2);
        } else {
            showDetailTime = false;
            time = MSTimeUtils.getInstance().time2DataDay1(minTime * 1000);
        }
        List<ChatMultiForwardEntity> list = new ArrayList<>();
        ChatMultiForwardEntity entity = new ChatMultiForwardEntity();
        entity.itemType = 1;
        entity.title = time;
        list.add(entity);
        for (int i = 0, size = MSMultiForwardContent.msgList.size(); i < size; i++) {
            ChatMultiForwardEntity temp = new ChatMultiForwardEntity();
            temp.msg = MSMultiForwardContent.msgList.get(i);
//            if (temp.msg.type != 0)
            list.add(temp);
        }
        ChatMultiForwardEntity view = new ChatMultiForwardEntity();
        view.itemType = 2;
        list.add(view);
        ChatMultiForwardDetailAdapter adapter = new ChatMultiForwardDetailAdapter(showDetailTime, list);
        initAdapter(msVBinding.recyclerView, adapter);
    }

    @Override
    protected void initListener() {
        MSIM.getInstance().getCMDManager().addCmdListener("chat_multi_forward_detail", cmd -> {
            if (!TextUtils.isEmpty(cmd.cmdKey)) {
                if (cmd.cmdKey.equals(MSCMDKeys.ms_messageRevoke)) {
                    if (cmd.paramJsonObject != null && cmd.paramJsonObject.has("message_id")) {
                        String msgID = cmd.paramJsonObject.optString("message_id");
                        MSMsg msg = MSIM.getInstance().getMsgManager().getWithMessageID(msgID);
                        if (msg != null) {
                            if (msg.clientMsgNO.equals(clientMsgNo)) {
                                showToast(getString(R.string.msg_revoked));
                                finish();
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MSIM.getInstance().getMsgManager().removeRefreshMsgListener("chat_multi_forward_detail");
    }
}
