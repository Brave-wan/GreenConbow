package com.ijourney.ani.sample.bean;

public class ChatMsgBean {
    private String msg;
    private long time;

    public ChatMsgBean(String msg, long time) {
        this.msg = msg;
        this.time = time * 1000;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }
}
