package com.chitchat.beans;

public class ChatListBean {

    private String profilePic;
    private String name;
    private String lastMessage;
    private String receiverNum;
    private String receiverName;
    private String chatRoomID;
    private String messageID;
    private int messageType;
    private String image;
    private int messageSeen;
    private String senderID;

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public int getMessageSeen() {
        return messageSeen;
    }

    public void setMessageSeen(int messageSeen) {
        this.messageSeen = messageSeen;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getChatRoomID() {
        return chatRoomID;
    }

    public void setChatRoomID(String chatRoomID) {
        this.chatRoomID = chatRoomID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverNum() {
        return receiverNum;
    }


    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setReceiverNum(String receiverNum) {
        this.receiverNum = receiverNum;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getName() {
        return name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
