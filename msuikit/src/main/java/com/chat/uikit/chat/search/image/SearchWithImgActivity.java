package com.chat.uikit.chat.search.image;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.chat.base.base.MSBaseActivity;
import com.chat.base.config.MSApiConfig;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.EndpointSID;
import com.chat.base.endpoint.entity.ChatChooseContacts;
import com.chat.base.endpoint.entity.ChatViewMenu;
import com.chat.base.endpoint.entity.ChooseChatMenu;
import com.chat.base.entity.GlobalMessage;
import com.chat.base.entity.GlobalSearchReq;
import com.chat.base.entity.ImagePopupBottomSheetItem;
import com.chat.base.msgitem.MSContentType;
import com.chat.base.search.GlobalSearchModel;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.MSDialogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.MSTimeUtils;
import com.chat.base.views.CustomImageViewerPopup;
import com.chat.base.views.FullyGridLayoutManager;
import com.chat.base.views.pinnedsectionitemdecoration.PinnedHeaderItemDecoration;
import com.chat.uikit.R;
import com.chat.uikit.databinding.ActSearchMsgImgLayoutBinding;
import com.google.android.material.snackbar.Snackbar;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;
import com.mushanyux.mushanim.MSIM;
import com.mushanyux.mushanim.entity.MSChannel;
import com.mushanyux.mushanim.entity.MSChannelType;
import com.mushanyux.mushanim.entity.MSMsg;
import com.mushanyux.mushanim.msgmodel.MSImageContent;
import com.mushanyux.mushanim.msgmodel.MSMessageContent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 搜索聊天图片
 */
public class SearchWithImgActivity extends MSBaseActivity<ActSearchMsgImgLayoutBinding> {
    private String channelID;
    private byte channelType;
    private SearchWithImgAdapter adapter;
    private int page = 1;

    @Override
    protected ActSearchMsgImgLayoutBinding getViewBinding() {
        return ActSearchMsgImgLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void setTitle(TextView titleTv) {
        titleTv.setText(R.string.image);
    }

    @Override
    protected void initPresenter() {
        channelID = getIntent().getStringExtra("channel_id");
        channelType = getIntent().getByteExtra("channel_type", MSChannelType.PERSONAL);
    }

    @Override
    protected void initView() {
        PinnedHeaderItemDecoration mHeaderItemDecoration = new PinnedHeaderItemDecoration.Builder(1).enableDivider(false).create();
        int wH = (AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(6)) / 4;
        FullyGridLayoutManager layoutManager = new FullyGridLayoutManager(this, 4);
        msVBinding.recyclerView.setLayoutManager(layoutManager);
        adapter = new SearchWithImgAdapter(wH, new SearchWithImgAdapter.ICLick() {
            @Override
            public void onClick(SearchImgEntity entity) {
                showInChat(entity.message);
            }

            @Override
            public void onForward(SearchImgEntity entity) {
                forward(entity);
            }
        });
        msVBinding.recyclerView.setAdapter(adapter);
        msVBinding.recyclerView.addItemDecoration(mHeaderItemDecoration);
    }

    @Override
    protected void initListener() {
        getData();
        msVBinding.spinKit.setColor(Theme.colorAccount);
        msVBinding.refreshLayout.setEnableRefresh(false);
        msVBinding.refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                //  oldestOrderSeq = adapter.getData().get(adapter.getData().size() - 1).oldestOrderSeq;
                page++;
                getData();
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

            }
        });
        adapter.addChildClickViewIds(R.id.imageView);
        adapter.setOnItemChildClickListener((adapter1, view1, position) -> {
            SearchImgEntity entity = (SearchImgEntity) adapter1.getData().get(position);
            if (entity != null && entity.getItemType() == 0) {
                showImg(entity.url, (ImageView) view1);
            }
        });
    }

    private void showImg(String uri, ImageView imageView) {
        //查看大图
        List<Object> tempImgList = new ArrayList<>();
        List<Object> urlList = new ArrayList<>();
        List<ImageView> imgList = new ArrayList<>();
        for (int i = 0, size = adapter.getData().size(); i < size; i++) {
            if (adapter.getData().get(i).getItemType() == 0) {
                tempImgList.add(adapter.getData().get(i));
                urlList.add(adapter.getData().get(i).url);
                ImageView imageView1 = (ImageView) adapter.getViewByPosition(i, R.id.imageView);
                imgList.add(imageView1);
            }
        }
        int index = 0;
        for (int i = 0; i < tempImgList.size(); i++) {
            SearchImgEntity entity = (SearchImgEntity) tempImgList.get(i);
            if (entity.url.equals(uri)) {
                index = i;
                break;
            }
        }

        List<ImagePopupBottomSheetItem> bottomEntityArrayList = new ArrayList<>();
        bottomEntityArrayList.add(new ImagePopupBottomSheetItem(getString(R.string.forward), R.mipmap.msg_forward, position -> {
            SearchImgEntity entity = (SearchImgEntity) tempImgList.get(position);
            if (entity == null || entity.message.getMessageModel() == null) return;
            forward(entity);
        }));
        bottomEntityArrayList.add(new ImagePopupBottomSheetItem(getString(R.string.uikit_go_to_chat_item), R.mipmap.msg_message, position -> {
            SearchImgEntity entity = (SearchImgEntity) tempImgList.get(position);
            showInChat(entity.message);
        }));
        MSDialogUtils.getInstance().showImagePopup(this, urlList, imgList, imageView, index, bottomEntityArrayList, new CustomImageViewerPopup.IImgPopupMenu() {
            @Override
            public void onForward(int position) {
            }

            @Override
            public void onFavorite(int position) {
                SearchImgEntity entity = (SearchImgEntity) tempImgList.get(position);
                MSMsg msg = MSIM.getInstance().getMsgManager().getWithClientMsgNO(entity.message.client_msg_no);
                if (msg != null) {
                    collect(msg);
                }
            }

            @Override
            public void onShowInChat(int position) {
                SearchImgEntity entity = (SearchImgEntity) tempImgList.get(position);
                showInChat(entity.message);
            }
        }, null);

    }

    private void showInChat(GlobalMessage msg) {
        long orderSeq = MSIM.getInstance().getMsgManager().getMessageOrderSeq(
                msg.getMessage_seq(),
                msg.getChannel().getChannel_id(),
                msg.getChannel().getChannel_type()
        );
        EndpointManager.getInstance().invoke(EndpointSID.chatView, new ChatViewMenu(SearchWithImgActivity.this, channelID, MSChannelType.GROUP, orderSeq, false));
    }


    private void collect(MSMsg msg) {
        JSONObject jsonObject = new JSONObject();
        MSImageContent msgModel = (MSImageContent) msg.baseContentMsgModel;
        jsonObject.put("content", MSApiConfig.getShowUrl(msgModel.url));
        jsonObject.put("width", msgModel.width);
        jsonObject.put("height", msgModel.height);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("type", msg.type);
        String unique_key = msg.messageID;
        if (TextUtils.isEmpty(unique_key))
            unique_key = msg.clientMsgNO;
        hashMap.put("unique_key", unique_key);
        if (msg.getFrom() != null) {
            hashMap.put("author_uid", msg.getFrom().channelID);
            hashMap.put("author_name", msg.getFrom().channelName);
        }
        hashMap.put("payload", jsonObject);
        hashMap.put("activity", this);
        EndpointManager.getInstance().invoke("favorite_add", hashMap);
    }

    private void forward(SearchImgEntity entity) {
        MSMessageContent finalMSMessageContent = entity.message.getMessageModel();
        if (finalMSMessageContent == null) {
            return;
        }
        EndpointManager.getInstance().invoke(EndpointSID.showChooseChatView, new ChooseChatMenu(new ChatChooseContacts(list1 -> {
            if (MSReader.isNotEmpty(list1)) {
                for (MSChannel channel : list1) {
                    MSIM.getInstance().getMsgManager().send(finalMSMessageContent, channel);
                }
                ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();
                Snackbar.make(viewGroup, getString(R.string.is_forward), 1000)
                        .setAction("", v1 -> {
                        })
                        .show();
            }
        }), finalMSMessageContent));
    }

    private void getData() {
        ArrayList<Integer> contentType = new ArrayList<>();
        contentType.add(MSContentType.MS_IMAGE);
        GlobalSearchReq req = new GlobalSearchReq(1, "", channelID, channelType, "", "", contentType, page, 20, 0, 0);
        GlobalSearchModel.INSTANCE.search(req, (code, s, globalSearch) -> {
            msVBinding.refreshLayout.finishLoadMore();
            msVBinding.refreshLayout.finishRefresh();
            if (MSReader.isNotEmpty(globalSearch.messages)) {
                List<SearchImgEntity> fileEntityList = new ArrayList<>();
                // 构造数据
                for (GlobalMessage msg : globalSearch.messages) {
                    MSMessageContent content = msg.getMessageModel();
                    if (content == null) {
                        continue;
                    }
                    MSImageContent msgModel = null;
                    if (content instanceof MSImageContent) {
                        msgModel = (MSImageContent) content;
                    }
                    if (msgModel == null) {
                        continue;
                    }
                    String date = MSTimeUtils.getInstance().time2YearMonth(msg.getTimestamp() * 1000);
                    if (MSReader.isNotEmpty(fileEntityList)) {
                        if (!fileEntityList.get(fileEntityList.size() - 1).date.equals(date)) {
                            SearchImgEntity entity = new SearchImgEntity();
                            entity.date = date;
                            entity.itemType = 1;
                            fileEntityList.add(entity);
                        }
                    } else {
                        SearchImgEntity entity = new SearchImgEntity();
                        entity.date = date;
                        entity.itemType = 1;
                        fileEntityList.add(entity);
                    }
                    SearchImgEntity entity = new SearchImgEntity();
                    entity.date = date;
                    entity.message = msg;

                    String showUrl = "";
                    if (!TextUtils.isEmpty(msgModel.localPath)) {
                        File file = new File(msgModel.localPath);
                        if (file.exists()) {
                            showUrl = msgModel.localPath;
                        }
                    }
                    if (TextUtils.isEmpty(showUrl)) {
                        showUrl = MSApiConfig.getShowUrl(msgModel.url);
                    }
                    entity.url = showUrl;
                    fileEntityList.add(entity);
                }
                if (MSReader.isNotEmpty(adapter.getData())) {
                    SearchImgEntity entity = adapter.getData().get(adapter.getData().size() - 1);
                    if (entity.date.equals(fileEntityList.get(0).date)) {
                        fileEntityList.remove(0);
                    }
                }
                adapter.addData(fileEntityList);
            } else {
                msVBinding.refreshLayout.finishLoadMoreWithNoMoreData();
                if (page == 1) {
                    msVBinding.refreshLayout.setEnableLoadMore(false);
                    msVBinding.nodataTv.setVisibility(View.VISIBLE);
                }
            }
            return null;
        });
    }
}
