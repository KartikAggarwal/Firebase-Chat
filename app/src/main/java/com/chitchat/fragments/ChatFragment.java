package com.chitchat.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.FireBaseDatabaseUtil;
import com.chitchat.Utilities.MySharedPref;
import com.chitchat.activities.HomeActivity;
import com.chitchat.adapters.ChatListRecyclerAdapter;
import com.chitchat.beans.ChatListBean;
import com.chitchat.interfaces.AllChatList;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements AllChatList {
    private TextView tvDontSee;
    private ImageView ivAddFriendIcon;
    private Button btnAddFriend;
    private RecyclerView rvChatList;
    private List<ChatListBean> mAllChats;
    private ChatListRecyclerAdapter mChatListAdapter;
    private Activity mActivity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        initViews(view);
        initVariables();
        return view;
    }

    /**
     * Method to initialise the views
     */
    private void initViews(View view) {
        Toolbar tbToolbar = view.findViewById(R.id.tb_toolbar);
        view.findViewById(R.id.iv_toolbar_left).setVisibility(View.GONE);
        view.findViewById(R.id.tv_last_seen).setVisibility(View.GONE);
        ((HomeActivity) mActivity).setSupportActionBar(tbToolbar);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(getResources().getString(R.string.chat_text));
        ImageView ivToolbarRight = view.findViewById(R.id.iv_toolbar_right);
        ivToolbarRight.setImageResource(R.drawable.ic_chat_edit);
        tvDontSee = view.findViewById(R.id.tv_dont_see);
        ivAddFriendIcon = view.findViewById(R.id.iv_add_friend);
        btnAddFriend = view.findViewById(R.id.b_add_friend);
        rvChatList = view.findViewById(R.id.rv_chat_list);
        rvChatList.setLayoutManager(new LinearLayoutManager(mActivity));

    }

    /**
     * Method to initialise the variables
     */
    private void initVariables() {
        mAllChats = new ArrayList<>();
        mChatListAdapter = new ChatListRecyclerAdapter(mAllChats, mActivity);
        rvChatList.setAdapter(mChatListAdapter);
        FireBaseDatabaseUtil.getDatabaseInstance().getAllChatList(MySharedPref.getPreference(mActivity).getUserID(), mActivity);
    }

    @Override
    public void allChatList(List<ChatListBean> chatList) {
        if (chatList != null && chatList.size() > 0) {
            ivAddFriendIcon.setVisibility(View.GONE);
            btnAddFriend.setVisibility(View.GONE);
            tvDontSee.setVisibility(View.GONE);
            rvChatList.setVisibility(View.VISIBLE);
            mAllChats.clear();
            mAllChats.addAll(chatList);
            mChatListAdapter.notifyDataSetChanged();
        } else {
            tvDontSee.setVisibility(View.VISIBLE);
            ivAddFriendIcon.setVisibility(View.VISIBLE);
            btnAddFriend.setVisibility(View.VISIBLE);
            rvChatList.setVisibility(View.GONE);
        }
    }
}
