package com.chitchat.beans;

public class ChatRoomBean {
    String senderID;
    String receiverID;

    public ChatRoomBean(String senderId, String receiverId) {
        this.senderID = senderId;
        this.receiverID = receiverId;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }
}
