package com.chitchat.comparators;

import com.chitchat.beans.ContactBean;

import java.util.Comparator;


public class NameComparator implements Comparator<ContactBean> {
    @Override
    public int compare(ContactBean post1, ContactBean post2) {
        return post1.getmName().compareTo(post2.getmName());
    }
}
