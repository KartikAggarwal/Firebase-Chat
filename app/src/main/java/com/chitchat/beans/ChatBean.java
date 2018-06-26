package com.chitchat.beans;

public class ChatBean {
    private String sender;
    private String receiver;
    private String senderUid;
    private String receiverUid;
    private String message;
    private long timestamp;

    public ChatBean() {
    }

    public ChatBean(String sender, String receiver, String senderUid, String receiverUid, String message, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.message = message;
        this.timestamp = timestamp;
    }
}
