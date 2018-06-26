package com.chitchat.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.chitchat.Utilities.AppConstants;
import com.chitchat.fragments.ChatFragment;
import com.chitchat.fragments.ContactFragment;
import com.chitchat.fragments.SettingFragment;

public class HomePagerAdapter extends FragmentPagerAdapter {
    private final int tabs = 3;

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ContactFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new SettingFragment();
            default:
                return new ChatFragment();
        }
    }

    @Override
    public int getCount() {
        return tabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return AppConstants.CONTACTS;
            case 1:
                return AppConstants.CHATS;
            case 2:
                return AppConstants.SETTING;
            default:
                return AppConstants.DEFAULT;
        }
    }
}
