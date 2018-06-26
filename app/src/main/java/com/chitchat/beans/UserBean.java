package com.chitchat.beans;

import java.util.Map;

public class UserBean {

    private String name;
    private String lastName;
    private String uId;
    private Object lastSeenStatus;
    private String profilePic;
    private String phoneNumber;
    private int onlineStatus;

    public UserBean() {
    }

    public int getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getLastName() {
        return lastName;
    }

    public Object getLastSeenStatus() {
        return lastSeenStatus;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLastSeenStatus(Object lastSeenStatus) {
        this.lastSeenStatus = lastSeenStatus;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuId() {
        return uId;
    }

    public UserBean(String name, String lastName, String uId, Map<String,String> lastSeenStatus, String profilePic, String phoneNumber, int onlineStatus) {
        this.name = name;
        this.lastName = lastName;
        this.uId = uId;
        this.lastSeenStatus = lastSeenStatus;
        this.profilePic = profilePic;
        this.phoneNumber = phoneNumber;
        this.onlineStatus = onlineStatus;
    }
}
