package com.chitchat.beans;

import java.util.Map;

public class MessageBean {
    private String message;
    private Map<String, String> timeStamp;
    private String messageId;
    private int seenStatus;
    private String senderId;
    private String media;
    private double latitude;
    private double longitude;
    private int messageType;

    public MessageBean(String messageID, Map<String, String> timeStamp, String message, int seenStatus, String senderId,int messageType) {
        this.messageId = messageID;
        this.timeStamp = timeStamp;
        this.message = message;
        this.seenStatus = seenStatus;
        this.senderId = senderId;
        this.messageType=messageType;
    }

    public MessageBean(String messageID, Map<String, String> timeStamp, int messageType, int seenStatus, String senderId,String media) {
        this.messageId = messageID;
        this.timeStamp = timeStamp;
        this.media=media;
        this.seenStatus = seenStatus;
        this.senderId = senderId;
        this.messageType = messageType;
    }

    public MessageBean (String messageID,Map<String,String> timeStamp,int seenStatus,String senderId,double latitude,double longitude,int messageType)
    {
        this.messageId=messageID;
        this.timeStamp=timeStamp;
        this.seenStatus=seenStatus;
        this.senderId=senderId;
        this.latitude=latitude;
        this.longitude=longitude;
        this.messageType=messageType;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setTimeStamp(Map<String, String> timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getSeenStatus() {
        return seenStatus;
    }

    public Map<String, String> getTimeStamp() {
        return timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSeenStatus(int seenStatus) {
        this.seenStatus = seenStatus;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
