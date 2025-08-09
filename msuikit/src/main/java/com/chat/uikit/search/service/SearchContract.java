package com.chat.uikit.search.service;

import com.chat.base.base.MSBasePresenter;
import com.chat.base.base.MSBaseView;
import com.chat.uikit.search.SearchUserEntity;

/**
 * 搜索
 */
public class SearchContract {
    public interface SearchUserPresenter extends MSBasePresenter {
        void searchUser(String keyword);
    }

    public interface SearchUserView extends MSBaseView {
        void setSearchUser(SearchUserEntity searchUser);
    }
}
