package com.chitchat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.MySharedPref;
import com.chitchat.activities.HomeActivity;
import com.chitchat.adapters.ContactRecyclerAdapter;
import com.chitchat.beans.ContactBean;
import com.chitchat.beans.UserBean;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends Fragment implements TextWatcher {
    private RecyclerView rvContacts;
    private ContactRecyclerAdapter mContactAdapter;
    private List<ContactBean> mContacts;

    public ContactFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        initViews(view);
        initVariables();
        return view;
    }

    /**
     * Method to initialise Views
     */
    private void initViews(View view) {
        rvContacts = view.findViewById(R.id.rv_contacts);
        Toolbar tbToolbar = view.findViewById(R.id.tb_toolbar);
        view.findViewById(R.id.iv_toolbar_left).setVisibility(View.GONE);
        view.findViewById(R.id.tv_last_seen).setVisibility(View.GONE);
        ((HomeActivity) getActivity()).setSupportActionBar(tbToolbar);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(getResources().getString(R.string.contact_text));
        ImageView ivToolbarRight = view.findViewById(R.id.iv_toolbar_right);
        ivToolbarRight.setImageResource(R.drawable.ic_invite);
        EditText etSearch = view.findViewById(R.id.et_search_contact);
        etSearch.addTextChangedListener(this);
    }

    /**
     * Method to initialise Variables
     */
    private void initVariables() {
            mContacts = new ArrayList<>();
            loadContacts(mContacts);
            String currentUserName = MySharedPref.getPreference(getContext()).getUserFirstName() + " " + MySharedPref.getPreference(getContext()).getUserLastName();
            String currentUserNumber = MySharedPref.getPreference(getContext()).getUserPhone();
            if (mContactAdapter != null)
                mContactAdapter.getUser(currentUserNumber, currentUserName);
        }

    /**
     * Method to load Contact list in Recycler View
     */
    private void loadContacts(List<ContactBean> list) {//,String currentUserName,String currentUserNumber) {
        if (list != null) {
            mContactAdapter = new ContactRecyclerAdapter(list, getActivity());
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            rvContacts.setLayoutManager(layoutManager);
            rvContacts.setAdapter(mContactAdapter);
        }
    }

    /**
     * Method to get sync contacts from database
     */
    public void getSyncList(List<ContactBean> syncContactList) {
        if (syncContactList != null && syncContactList.size() > 0) {
            if (mContactAdapter != null) {
                mContacts.clear();
                mContacts.addAll(syncContactList);
                mContactAdapter.notifyDataSetChanged();
                mContactAdapter.getSyncedList(mContacts);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (mContactAdapter != null)
            mContactAdapter.getFilter().filter(charSequence);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

   /* @Override
    public void getSyncList(List<ContactBean> syncList) {
        if (syncList != null && syncList.size() > 0) {
            if (mContactAdapter != null) {
                mContacts.clear();
                mContacts.addAll(syncList);
                mContactAdapter.notifyDataSetChanged();
            }
        }
    }*/


    /**
     * Method to sync contacts from database
     */
    /*private void syncContacts(List<ContactBean> contactList) {
        int counter;
        List<ContactBean> appUserList = new ArrayList<>();
        List<ContactBean> nonAppUser = new ArrayList<>();
        if (contactList != null && contactList.size() > 0 && mAllUsers != null) {

            for (ContactBean contactBean : contactList) {
                counter = 0;
                String number = contactBean.getmNumber();
                if (number.contains(" "))
                    number = number.replaceAll(" ", "");
                if (number.contains("-"))
                    number = number.replaceAll("-", "");
                if (number.length() == 13)
                    number = number.substring(3);
                if (number.length() == 11)
                    number = number.substring(1);
                for (UserBean users : mAllUsers) {
                    if (users.getPhoneNumber().equals(number)) {
                        counter = 1;
                        break;
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
        mContacts.clear();
        mContacts.addAll(appUserList);
        mContacts.addAll(nonAppUser);
        mContactAdapter.notifyDataSetChanged();
        mContactAdapter.getSyncedList(mContacts);
    }*/

    /**
     * Method to fetch contact List from Phone Contacts
     */
   /* private void fetchContacts() {
        List<ContactBean> contactList = new ArrayList<>();
        Cursor contacts = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (contacts != null) {
            while (contacts.moveToNext()) {
                String id = contacts.getString(contacts.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor phone = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null, null);
                ContactBean contact = new ContactBean();
                if (phone != null) {
                    if (phone.getCount() > 0) {
                        phone.moveToNext();
                        contact.setmName(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                        contact.setmNumber(phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }
                }
                if (contact.getmName() != null && contact.getmNumber() != null)
                    contactList.add(contact);
                phone.close();
            }
        }
        mContacts = new ArrayList<>();
        mContacts.addAll(contactList);
        FireBaseDatabaseUtil.getDatabaseInstance().getAllUsers(getActivity());
        loadContacts(mContacts);
        contacts.close();
    }*/


    /**
     * Method to get all contacts from phone and current user details
     */
    /*public void getPhoneContacts(String currentUserPhone, String currentUserName, List<ContactBean> contacts) {
        mCurrentUserNumber = currentUserPhone;
        mCurrentUserName = currentUserName;
        *//*if (mContactAdapter != null)
            mContactAdapter.getUser(mCurrentUserNumber, mCurrentUserName);*//*
        //loadContacts(contacts,currentUserName,currentUserPhone);
    }*/

    /**
     * Method to get all contacts from phone and current user details
     */
    /*public void getCurrentUser(String currentUserPhone, String currentUserName, String currentUID) {
        mCurrentUserNumber = currentUserPhone;
        mCurrentUserName = currentUserName;
        mCurrentUserUID = currentUID;
        if (mContactAdapter != null)
            mContactAdapter.getUser(mCurrentUserNumber, mCurrentUserName);
    }*/

}
