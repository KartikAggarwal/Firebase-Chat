package com.chitchat.interfaces;

import com.chitchat.beans.ContactBean;

import java.util.List;

/**
 * Created by appinventiv on 10/10/17.
 */

public interface ContactSyncList {

    void getSyncList(List<ContactBean> syncList);
}
