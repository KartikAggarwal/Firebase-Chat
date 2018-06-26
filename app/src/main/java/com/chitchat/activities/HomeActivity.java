package com.chitchat.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.chitchat.R;
import com.chitchat.Utilities.AppConstants;
import com.chitchat.Utilities.AppUtils;
import com.chitchat.Utilities.FireBaseDatabaseUtil;
import com.chitchat.Utilities.MySharedPref;
import com.chitchat.adapters.HomePagerAdapter;
import com.chitchat.beans.ChatListBean;
import com.chitchat.beans.ContactBean;
import com.chitchat.beans.RetrieveMessageBean;
import com.chitchat.beans.UserBean;
import com.chitchat.comparators.NameComparator;
import com.chitchat.fragments.ChatFragment;
import com.chitchat.fragments.ContactFragment;
import com.chitchat.interfaces.AllChatList;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private TabLayout tlTabs;
    private String mUid;
    private List<UserBean> mAllUsers;
    private List<ContactBean> mContacts;
    private List<ChatListBean> mChatList;
    private AllChatList mAllChats;
    private HashMap<String, String> mContactMap;
    private int mChatCounter = 0;
    private boolean mDoubleTapOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activit_home);
        initViews();
        initVariables();
    }

    /**
     * Method to initialise the views
     */
    private void initViews() {
        tlTabs = findViewById(R.id.tl_home);
    }

    /**
     * Method to initialise the Variables
     */
    private void initVariables() {
        mUid = MySharedPref.getPreference(this).getUserID();
        HomePagerAdapter pagerAdapter = new HomePagerAdapter(getSupportFragmentManager());
        ViewPager pager = findViewById(R.id.vp_home);
        pager.setAdapter(pagerAdapter);
        pager.setCurrentItem(1);
        pager.addOnPageChangeListener(this);
        tlTabs.setupWithViewPager(pager);
        addTabs();
        mChatList = new ArrayList<>();
        mAllUsers = new ArrayList<>();
        FireBaseDatabaseUtil.getDatabaseInstance().getAllUsers(this);
        //checkContactPermission();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ChatFragment) {
            mAllChats = (AllChatList) fragment;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FireBaseDatabaseUtil.getDatabaseInstance().changeOnlineStatus(MySharedPref.getPreference(this).getUserID());
    }

    @Override
    protected void onPause() {
        super.onPause();
        FireBaseDatabaseUtil.getDatabaseInstance().changeLastSeen(mUid);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FireBaseDatabaseUtil.getDatabaseInstance().removeUserListener();
    }

    /**
     * Method to add tabs Dynamically to tab layout
     */
    private void addTabs() {
        int tabIcons[] = {R.drawable.contact_selector, R.drawable.chat_selector, R.drawable.setting_selector};
        for (int l = 0; l < tabIcons.length; l++) {
            if (tlTabs != null) {
                TabLayout.Tab tab = tlTabs.getTabAt(l);
                if (tab != null)
                    tab.setIcon(tabIcons[l]);
            }
        }
    }

    /**
     * Method to Check Permission To Read the phone Contacts
     */
    public void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, AppConstants.CONTACT_PERMISSION);
        } else {
            fetchContacts();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConstants.CONTACT_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchContacts();
                }
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.vp_home + ":" + tlTabs.getSelectedTabPosition());
        if (fragment != null)
            if (fragment instanceof ContactFragment) {
                if (mContacts != null && mContacts.size() > 0)
                    ((ContactFragment) fragment).getSyncList(mContacts);
            }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * Method to get all the users from database
     */
    public void allUsers(List<UserBean> allUsers) {
        if (allUsers != null) {
            mAllUsers.addAll(allUsers);
        }
        checkContactPermission();
    }

    /**
     * Method to sync contacts from database
     */
    private void syncContacts() {
        int counter;
        List<ContactBean> appUserList = new ArrayList<>();
        List<ContactBean> nonAppUser = new ArrayList<>();
        //mAppUserDB = new ArrayList<>();
        if (mContacts != null && mContacts.size() > 0 && mAllUsers != null) {
            for (ContactBean contactBean : mContacts) {
                counter = 0;
                for (UserBean users : mAllUsers) {
                    if (users.getPhoneNumber().equals(formatNumber(contactBean.getmNumber()))) {
                        if (!MySharedPref.getPreference(this).getUserPhone().equals(users.getPhoneNumber())) {
                            counter = 1;
                            //mAppUserDB.add(users);
                            break;
                        }
                    }
                }

                if (counter == 1) {
                    contactBean.setAppUser(true);
                    appUserList.add(contactBean);
                } else {
                    nonAppUser.add(contactBean);
                }
            }
        }
        if (mContacts != null) {
            mContacts.clear();
            Collections.sort(appUserList, new NameComparator());
            mContacts.addAll(appUserList);
            mContacts.addAll(nonAppUser);
        }
    }

    /**
     * Method to load chat Activity
     */
    public void loadChatActivity(String receiverNum, String receiverName) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(getResources().getString(R.string.user_id), mUid);
        intent.putExtra(getResources().getString(R.string.receiver_no), receiverNum);
        intent.putExtra(getResources().getString(R.string.receiver_name), receiverName);
        startActivity(intent);
    }

    /**
     * Method to load chat List
     */
    public void loadChatList(String receiverID, String chatRoom) {
        if (receiverID != null && chatRoom != null) {
            if (mContacts != null) {
                if (++mChatCounter <= mContacts.size()) {
                    for (UserBean receiver : mAllUsers) {
                        if (receiver.getuId().equals(receiverID)) {
                            if (mContactMap.containsKey(receiver.getPhoneNumber()))
                                FireBaseDatabaseUtil.getDatabaseInstance().getLastMessage(chatRoom, mContactMap.get(receiver.getPhoneNumber()), receiver.getProfilePic(), receiver.getPhoneNumber(), this, mUid);
                            else
                                FireBaseDatabaseUtil.getDatabaseInstance().getLastMessage(chatRoom, receiver.getPhoneNumber(), receiver.getProfilePic(), receiver.getPhoneNumber(), this, mUid);
                        }
                    }
                } else {
                    if (mAllUsers != null && mAllUsers.size() > 0) {
                        for (UserBean receiver : mAllUsers) {
                            if (receiver.getuId().equals(receiverID)) {
                                FireBaseDatabaseUtil.getDatabaseInstance().getLastMessage(chatRoom, receiver.getPhoneNumber(), receiver.getProfilePic(), receiver.getPhoneNumber(), this, mUid);
                            }
                        }
                    }
                }
            } else {
                if (mAllUsers != null && mAllUsers.size() > 0) {
                    for (UserBean receiver : mAllUsers) {
                        if (receiver.getuId().equals(receiverID)) {
                            FireBaseDatabaseUtil.getDatabaseInstance().getLastMessage(chatRoom, receiver.getPhoneNumber(), receiver.getProfilePic(), receiver.getPhoneNumber(), this, mUid);
                        }
                    }
                }
            }
        }
    }


    /**
     * Method to get Last Message
     */
    public void getLastMessage(ChatListBean chat) {
        if (chat != null) {
            mChatList.add(chat);
            mAllChats.allChatList(mChatList);
        }
    }

    /**
     * Method to update Last Message
     */
    public void updateLastMessage(RetrieveMessageBean message, String chatRoom) {
        if (message != null) {
            for (int l = 0; l < mChatList.size(); l++) {
                if (chatRoom.equals(mChatList.get(l).getChatRoomID())) {
                    ChatListBean chat = mChatList.get(l);
                    chat.setLastMessage(message.getMessage());
                    chat.setMessageType(message.getMessageType());
                    chat.setMessageSeen(message.getSeenStatus());
                    chat.setSenderID(message.getSenderId());
                    mChatList.remove(l);
                    mChatList.add(0, chat);
                    mAllChats.allChatList(mChatList);
                    break;
                }
            }
        }
    }

    /**
     * Method to filter contact number
     */
    private String formatNumber(String number) {
        if (number.contains(" "))
            number = number.replaceAll(" ", "");
        if (number.contains("-"))
            number = number.replaceAll("-", "");
        if (number.length() == 13)
            number = number.substring(3);
        if (number.length() == 11)
            number = number.substring(1);
        if (number.contains("("))
            number = number.replaceAll("\\(", "");
        if (number.contains(")"))
            number = number.replaceAll("\\)", "");
        return number;
    }

    /**
     * Method to fetch contact List from Phone Contacts
     */
    private void fetchContacts() {
        List<ContactBean> contactList = new ArrayList<>();
        mContactMap = new HashMap<>();
        Cursor contacts = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (contacts != null) {
            while (contacts.moveToNext()) {
                String id = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null, null);
                ContactBean contact = new ContactBean();
                if (phone != null) {
                    if (phone.getCount() > 0) {
                        phone.moveToNext();
                        contact.setmName(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                        contact.setmNumber(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        mContactMap.put(formatNumber(contact.getmNumber()), contact.getmName());
                    }
                }
                if (contact.getmName() != null && contact.getmNumber() != null)
                    contactList.add(contact);
                if (phone != null)
                    phone.close();
            }
        }
        mContacts = new ArrayList<>();
        mContacts.addAll(contactList);
        syncContacts();
        if (contacts != null)
            contacts.close();
    }

    @Override
    public void onBackPressed() {
        if (mDoubleTapOnce) {
            super.onBackPressed();
            return;
        }
        mDoubleTapOnce = true;
        AppUtils.showToast(this, getResources().getString(R.string.press_again));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDoubleTapOnce = false;
            }
        }, 2000);
    }
}
