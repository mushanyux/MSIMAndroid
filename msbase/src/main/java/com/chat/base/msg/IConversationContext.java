package com.chat.base.msg;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;


public interface IConversationContext {
    //发送消息到当前会话
    void sendMessage(MSMessageContent msMessageContent);

    //获取当前会话到频道信息
    MSChannel getChatChannelInfo();

    //显示多选状态
    void showMultipleChoice();

    //显示标题栏右边内容
    void setTitleRightText(String text);

    //显示回复效果
    void showReply(MSMsg msMsg);

    //显示编辑效果
    void showEdit(MSMsg msMsg);

    //提醒某条消息
    void tipsMsg(String clientMsgNo);

    //设置输入框内容
    void setEditContent(String content);

    // 当前聊天页面
    AppCompatActivity getChatActivity();

    // 获取回复消息
    MSMsg getReplyMsg();

    // 隐藏软键盘
    void hideSoftKeyboard();

    ChatAdapter getChatAdapter();

    // 发送名片
    void sendCardMsg();

    // 消息列表滚动到底部
    void chatRecyclerViewScrollToEnd();

    void deleteOperationMsg();

    // 头像点击事件
    void onChatAvatarClick(String uid, boolean isLongClick);

    // 正在查看大图
    void onViewPicture(boolean isViewing);

    // 消息已查看
    void onMsgViewed(MSMsg msMsg, int position);

    View getRecyclerViewLayout();
    boolean isShowChatActivity();
    void closeActivity();
}
