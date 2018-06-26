package com.chitchat.beans;

public class ContactBean {

    private String mName;
    private String mNumber;
    private boolean isAppUser;

    public String getmName() {
        return mName;
    }

    public String getmNumber() {
        return mNumber;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public void setmNumber(String mNumber) {
        this.mNumber = mNumber;
    }

    public boolean isAppUser() {
        return isAppUser;
    }

    public void setAppUser(boolean appUser) {
        isAppUser = appUser;
    }
}
