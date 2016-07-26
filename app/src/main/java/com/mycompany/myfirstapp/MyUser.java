package com.mycompany.myfirstapp;

/**
 * Created by lenovo on 2016/7/4.
 */
public class MyUser {
    private String userName;
    private int imageID;
    private String recentMsg;
    private int messageSeq;
    public MyUser(String userName, int imageID) {
        this.userName = userName;
        this.imageID=imageID;
        recentMsg="";
        messageSeq=0;
    }

    public MyUser(String recentMsg, String userName, int imageID) {
        this.recentMsg = recentMsg;
        this.userName = userName;
        this.imageID = imageID;
        messageSeq=0;
    }

    public int getMessageSeq() {
        return messageSeq;
    }

    public void setMessageSeq(int messageSeq) {
        this.messageSeq = messageSeq;
    }

    public String getRecentMsg() {
        return recentMsg;
    }

    public void setRecentMsg(String recentMsg) {
        this.recentMsg = recentMsg;
    }

    public int getImageID() {
        return imageID;
    }

    public String getUserName() {
        return userName;
    }
}
