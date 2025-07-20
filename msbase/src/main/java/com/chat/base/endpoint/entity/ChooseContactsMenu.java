package com.chat.base.endpoint.entity;

import com.mushanyux.mushanim.entity.MSChannel;

import java.util.List;

/**
 * 选择联系人
 */
public class ChooseContactsMenu {
    public int maxCount;
    public List<MSChannel> defaultSelected;
    public IChooseBack iChooseBack;
    public boolean isCanDeselect;//defaultSelected 默认选中的是否能取消选中
    public boolean isShowSaveLabelDialog; // 选择成员页面是否显示存为标签弹框

    public ChooseContactsMenu(int maxCount, boolean isCanDeselect, boolean isShowSaveLabelDialog, List<MSChannel> defaultSelected, IChooseBack iChooseBack) {
        this.defaultSelected = defaultSelected;
        this.isShowSaveLabelDialog = isShowSaveLabelDialog;
        this.iChooseBack = iChooseBack;
        this.maxCount = maxCount;
        this.isCanDeselect = isCanDeselect;
    }

    public interface IChooseBack {
        void onBack(List<MSChannel> selectedList);
    }
}
