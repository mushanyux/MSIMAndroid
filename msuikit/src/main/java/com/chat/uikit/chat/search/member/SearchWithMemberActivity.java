package com.chat.uikit.chat.search.member;

import android.text.TextUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chat.base.base.MSBaseActivity;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.entity.GlobalMessage;
import com.chat.base.entity.GlobalSearchReq;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.search.GlobalSearchModel;
import com.chat.base.utils.MSReader;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActCommonRefreshListLayoutBinding;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelMember;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;


public class SearchWithMemberActivity extends MSBaseActivity<ActCommonRefreshListLayoutBinding> {
    SearchWithMemberAdapter adapter;
    private String channelID;
    private String fromUID;
    private int page = 1;

    @Override
    protected ActCommonRefreshListLayoutBinding getViewBinding() {
        return ActCommonRefreshListLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.uikit_search_with_member);
    }

    @Override
    protected void initPresenter() {
        channelID = getIntent().getStringExtra("channelID");
        fromUID = getIntent().getStringExtra("fromUID");
    }

    @Override
    protected void initView() {
        String name = "";
        String avatarKey = "";
        MSChannelMember member = MSIM.getInstance().getChannelMembersManager().getMember(channelID, MSChannelType.GROUP, fromUID);
        if (member != null) {
            name = TextUtils.isEmpty(member.memberRemark) ? member.memberName : member.memberRemark;
        }
        MSChannel channel = MSIM.getInstance().getChannelManager().getChannel(fromUID, MSChannelType.PERSONAL);
        if (channel != null) {
            if (!TextUtils.isEmpty(channel.channelRemark))
                name = channel.channelRemark;
            avatarKey = channel.avatarCacheKey;
        }
        adapter = new SearchWithMemberAdapter(name, avatarKey);
        initAdapter(msVBinding.recyclerView, adapter);
    }

    @Override
    protected void initListener() {
        msVBinding.refreshLayout.setEnableRefresh(false);
        msVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                page ++;
                getData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
        adapter.setOnItemClickListener((adapter, view, position) -> {
            GlobalMessage msg = (GlobalMessage) adapter.getData().get(position);
            if (msg != null) {
                long orderSeq = MSIM.getInstance().getMsgManager().getMessageOrderSeq(
                        msg.getMessage_seq(),
                        msg.getChannel().getChannel_id(),
                        msg.getChannel().getChannel_type()
                );
                EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(SearchWithMemberActivity.this, channelID, MSChannelType.GROUP, orderSeq, false));
            }
        });
        getData();
    }

    private void getData() {
        ArrayList<Integer> contentType = new ArrayList<>();
        contentType.add(MSContentType.MS_TEXT);
        contentType.add(MSContentType.MS_FILE);
        GlobalSearchReq req = new GlobalSearchReq(1, "", channelID, MSChannelType.GROUP, fromUID, "", contentType, page, 20, 0, 0);
        GlobalSearchModel.INSTANCE.search(req, (code, s, globalSearch) -> {
            msVBinding.refreshLayout.finishLoadMore();
            msVBinding.refreshLayout.finishRefresh();
            if (code != HttpResponseCode.success) {
                showToast(s);
                return null;
            }
            if (globalSearch == null || MSReader.isEmpty(globalSearch.messages)) {
                if (page != 1) {
                    msVBinding.refreshLayout.setEnableLoadMore(false);
                }
                return null;
            }
            if (page == 1) {
                adapter.setList(globalSearch.messages);
            } else {
                adapter.addData(globalSearch.messages);
            }
            return null;
        });
    }
}
