package com.chat.base.endpoint.entity;

import java.util.List;

/**
 * 选择标签
 */
public class ChooseLabelMenu {
    public IChooseLabel iChooseLabel;

    public ChooseLabelMenu(IChooseLabel iChooseLabel) {
        this.iChooseLabel = iChooseLabel;
    }

    public interface IChooseLabel {
        void onResult(List<ChooseLabelEntity> list);
    }
}
