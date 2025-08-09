package com.chat.uikit.search;

import android.widget.TextView;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.chat.manager.MSIMUtils;
import com.chat.uikit.databinding.ActCommonListLayoutBinding;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSMessageSearchResult;
import com.mushanyux.mushanim.entity.MSMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索消息结果
 */
public class SearchMsgResultActivity extends MSBaseActivity<ActCommonListLayoutBinding> {

    MSMessageSearchResult result;
    SearchMsgResultAdapter adapter;

    @Override
    protected ActCommonListLayoutBinding getViewBinding() {
        return ActCommonListLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(result.msChannel.channelName);
    }

    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView() {
        result = getIntent().getParcelableExtra("result");
        String searchKey = getIntent().getStringExtra("searchKey");
        adapter = new SearchMsgResultAdapter(searchKey, new ArrayList<>());
        initAdapter(msVBinding.recyclerView, adapter);
        List<MSMsg> msgList = MSIM.getInstance().getMsgManager().searchWithChannel(searchKey,result.msChannel.channelID, result.msChannel.channelType);
        adapter.setList(msgList);
    }

    @Override
    protected void initListener() {
        adapter.setOnItemClickListener((adapter1, view1, position) -> SingleClickUtil.determineTriggerSingleClick(view1, view2 -> {
            MSMsg msg = (MSMsg) adapter1.getItem(position);
            if (msg != null) {
                MSIMUtils.getInstance().startChatActivity(new ChatViewMenu(this, msg.channelID, msg.channelType, msg.orderSeq, false));
            }
        }));
    }
}
