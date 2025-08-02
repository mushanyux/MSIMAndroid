package com.chat.uikit.fragment;

import android.content.Intent;
import android.text.TextUtils;

import com.chat.base.base.MSBaseFragment;
import com.chat.base.common.MSCommonModel;
import com.chat.base.config.MSConfig;
import com.chat.base.endpoint.EndpointCategory;
import com.chat.base.endpoint.EndpointManager;
import com.chat.base.endpoint.entity.PersonalInfoMenu;
import com.chat.base.net.HttpResponseCode;
import com.chat.base.ui.Theme;
import com.chat.base.utils.MSLogUtils;
import com.chat.base.utils.MSReader;
import com.chat.base.utils.singleclick.SingleClickUtil;
import com.chat.uikit.R;
import com.chat.uikit.databinding.FragMyLayoutBinding;
import com.chat.uikit.user.MyInfoActivity;
import com.mushanyux.mushanim.entity.MSChannelType;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的
 */
public class MyFragment extends MSBaseFragment<FragMyLayoutBinding> {
    private PersonalItemAdapter adapter;

    @Override
    protected FragMyLayoutBinding getViewBinding() {
        return FragMyLayoutBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        msVBinding.recyclerView.setNestedScrollingEnabled(false);
        adapter = new PersonalItemAdapter(new ArrayList<>());
        initAdapter(msVBinding.recyclerView, adapter);
        //设置数据item
        List<PersonalInfoMenu> endpoints = EndpointManager.getInstance().invokes(EndpointCategory.personalCenter, null);
        for (int i = 0; i < endpoints.size(); i++) {
            if (!TextUtils.isEmpty(endpoints.get(i).sid)
                    && endpoints.get(i).sid.equals("invite_code")
                    && MSConfig.getInstance().getAppConfig().register_invite_on == 0) {
                endpoints.remove(i);
                break;
            }
        }
        adapter.setList(endpoints);
    }

    @Override
    protected void initPresenter() {
        msVBinding.avatarView.setSize(90);
        msVBinding.refreshLayout.setEnableOverScrollDrag(true);
        msVBinding.refreshLayout.setEnableLoadMore(false);
        msVBinding.refreshLayout.setEnableRefresh(false);
        Theme.setPressedBackground(msVBinding.qrIv);
    }

    @Override
    protected void initListener() {
        adapter.setOnItemClickListener((adapter1, view, position) -> SingleClickUtil.determineTriggerSingleClick(view, view1 -> {
            PersonalInfoMenu menu = (PersonalInfoMenu) adapter1.getItem(position);
            if (menu != null && menu.iPersonalInfoMenuClick != null) {
                menu.iPersonalInfoMenuClick.onClick();
            }
        }));
        SingleClickUtil.onSingleClick(msVBinding.avatarView, view -> gotoMyInfo());
        SingleClickUtil.onSingleClick(msVBinding.qrIv, view -> gotoMyInfo());
    }

    void gotoMyInfo() {
//        String str = MSDeviceUtils.getSignature(getActivity());
//        Log.e("签名",str+"");
        startActivity(new Intent(getActivity(), MyInfoActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        msVBinding.nameTv.setText(MSConfig.getInstance().getUserInfo().name);
        msVBinding.avatarView.showAvatar(MSConfig.getInstance().getUid(), MSChannelType.PERSONAL);
        if (null != adapter) {
            try {
                MSCommonModel.getInstance().getAppNewVersion(false, version -> {
                    int index = -1;
                    for (int i = 0; i < adapter.getData().size(); i++) {
                        if (getString(R.string.currency).equals(adapter.getData().get(i).text)) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        if (version != null && !TextUtils.isEmpty(version.download_url)) {
                            if (!adapter.getData().get(index).isNewVersionIv) {
                                adapter.getData().get(index).setIsNewVersionIv(true);
                                adapter.notifyItemChanged(index);
                            }
                        } else if (adapter.getData().get(index).isNewVersionIv) {
                            adapter.getData().get(index).setIsNewVersionIv(false);
                            adapter.notifyItemChanged(index);
                        }
                    }
                });
            } catch (Exception e) {
                MSLogUtils.w("检查新版本错误");
            }
        }
        MSCommonModel.getInstance().getAppConfig((code, msg, msappConfig) -> {
            if (code == HttpResponseCode.success) {
                if (adapter == null || MSReader.isEmpty(adapter.getData())) {
                    return;
                }
                if (msappConfig.register_invite_on == 0) {
                    for (int i = 0; i < adapter.getData().size(); i++) {
                        if (!TextUtils.isEmpty(adapter.getData().get(i).sid) && adapter.getData().get(i).sid.equals("invite_code")) {
                            adapter.removeAt(i);
                            break;
                        }
                    }
                } else {
                    List<PersonalInfoMenu> endpoints = EndpointManager.getInstance().invokes(EndpointCategory.personalCenter, null);
                    adapter.setList(endpoints);
                }
            }
        });
    }
}
