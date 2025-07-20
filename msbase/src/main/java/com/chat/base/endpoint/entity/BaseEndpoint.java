package com.chat.base.endpoint.entity;

public class BaseEndpoint {
    public String sid;
    public int imgResourceID;
    public String text;
    public IMenuClick iMenuClick;

    public interface IMenuClick {
        void onClick();
    }
}
