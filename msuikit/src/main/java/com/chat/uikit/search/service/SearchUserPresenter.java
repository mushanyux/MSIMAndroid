package com.chat.uikit.search.service;


import com.chat.base.net.HttpResponseCode;
import com.chat.base.utils.MSToastUtils;

import java.lang.ref.WeakReference;

public class SearchUserPresenter implements SearchContract.SearchUserPresenter {
    private final WeakReference<SearchContract.SearchUserView> userViewWeakReference;

    public SearchUserPresenter(SearchContract.SearchUserView searchUserView) {
        userViewWeakReference = new WeakReference<>(searchUserView);
    }

    @Override
    public void searchUser(String keyword) {
        SearchModel.getInstance().searchUser(keyword, (code, msg, searchUserEntity) -> {
            if (code == HttpResponseCode.success) {
                if (userViewWeakReference.get() != null)
                    userViewWeakReference.get().setSearchUser(searchUserEntity);
            } else MSToastUtils.getInstance().showToastFail(msg);
        });
    }

    @Override
    public void showLoading() {

    }
}
