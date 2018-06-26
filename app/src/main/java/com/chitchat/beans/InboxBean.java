package com.chitchat.beans;

public class InboxBean {
    private String chatRoomID;

    public void setChatRoomID(String chatRoomID) {
        this.chatRoomID = chatRoomID;
    }

    public String getChatRoomID() {
        return chatRoomID;
    }


    public InboxBean(String chatRoomID) {
        this.chatRoomID = chatRoomID;
    }
}
