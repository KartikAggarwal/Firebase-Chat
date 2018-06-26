package com.chitchat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.AppUtils;
import com.chitchat.activities.HomeActivity;
import com.chitchat.beans.ContactBean;

import java.util.ArrayList;
import java.util.List;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private final int USER_VIEW = 0;
    private final int CONTACT_HEADING = 1;
    private final int INVITE = 2;
    private final int CONTACT = 3;
    private List<ContactBean> mContacts;
    private List<ContactBean> mParentList;
    private Filter mFilterSearch;
    private String mUserName, mPhoneNumber;
    private Context mContext;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case USER_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_user_row_view, parent, false);
                return new UserViewHolder(view);
            case CONTACT_HEADING:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_heading_row_view, parent, false);
                return new ContactHeadingViewHolder(view);
            case INVITE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invite_friends_row_view, parent, false);
                return new InviteFriendViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_row_view, parent, false);
                return new ContactViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)) {
            case USER_VIEW:
                UserViewHolder userViewHolder = (UserViewHolder) holder;
                if (mPhoneNumber != null && mUserName != null) {
                    userViewHolder.tvUserName.setText(mUserName);
                    userViewHolder.tvNumber.setText(AppUtils.formatNumber(mPhoneNumber));
                    userViewHolder.tvInitials.setText(getInitials(mUserName));
                }
                break;
            case CONTACT_HEADING:
                break;
            case INVITE:
                break;
            default:
                ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
                int listPosition = position - 3;
                contactViewHolder.tvName.setText(mContacts.get(listPosition).getmName());
                contactViewHolder.tvNumber.setText(AppUtils.formatNumber(mContacts.get(listPosition).getmNumber()));
                contactViewHolder.tvInitials.setText(getInitials(mContacts.get(listPosition).getmName()));
                if (!mContacts.get(listPosition).isAppUser())
                    contactViewHolder.btn_invite.setVisibility(View.VISIBLE);
                else {
                    contactViewHolder.btn_invite.setVisibility(View.GONE);
                }
                break;
        }
    }

    public ContactRecyclerAdapter(List<ContactBean> contacts, Context context) {
        mContacts = contacts;
        mParentList = new ArrayList<>();
        mParentList.addAll(contacts);
        mFilterSearch = new FilterSearch();
        mContext = context;
    }


    @Override
    public int getItemCount() {
        return mContacts.size() + 3;
    }

    @Override
    public Filter getFilter() {
        return mFilterSearch;
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvNumber, tvInitials;

        UserViewHolder(View itemView) {
            super(itemView);
            tvNumber = itemView.findViewById(R.id.tv_user_number);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvInitials = itemView.findViewById(R.id.tv_user_pic_contact);
        }
    }

    private class ContactHeadingViewHolder extends RecyclerView.ViewHolder {
        ContactHeadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvName, tvNumber, tvInitials;
        Button btn_invite;

        ContactViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvNumber = itemView.findViewById(R.id.tv_contact_number);
            tvInitials = itemView.findViewById(R.id.tv_contact_image);
            btn_invite = itemView.findViewById(R.id.b_invite);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mContacts.get(getAdapterPosition() - 3).isAppUser())
                if (mContacts != null) {
                    ((HomeActivity) mContext).loadChatActivity(AppUtils.formatNumber(mContacts.get(getAdapterPosition() - 3).getmNumber()), mContacts.get(getAdapterPosition() - 3).getmName());
                }
        }
    }

    private class InviteFriendViewHolder extends RecyclerView.ViewHolder {
        InviteFriendViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {

        switch (position) {
            case 0:
                return USER_VIEW;
            case 1:
                return CONTACT_HEADING;
            case 2:
                return INVITE;
            default:
                return CONTACT;
        }
    }

    private class FilterSearch extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            mContacts.clear();
            if (charSequence.length() == 0)
                mContacts.addAll(mParentList);
            else {
                for (ContactBean contact : mParentList)
                    if (contact.getmName().toLowerCase().contains(charSequence.toString().trim().toLowerCase()))
                        mContacts.add(contact);
            }
            FilterResults filterResult = new FilterResults();
            filterResult.values = mContacts;
            filterResult.count = mContacts.size();
            return filterResult;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            mContacts = (List<ContactBean>) filterResults.values;
            notifyDataSetChanged();
        }
    }

    /**
     * Method to get User name and phone number
     */
    public void getUser(String number, String name) {
        mUserName = name;
        mPhoneNumber = number;
        notifyDataSetChanged();
    }

    /**
     * Method to draw contact Image
     */
    private String getInitials(String name) {
        String initials = "";
        String nameSplit[] = name.split(" ");
        for (String firstLetter : nameSplit) {
            if(initials.length() < 2)
                initials = initials + firstLetter.charAt(0);
            else
                break;
        }
        return initials;
    }

    /**
     * Method to getSynchronised List
     */
    public void getSyncedList(List<ContactBean> filteredList) {
        if (filteredList != null && filteredList.size() > 0) {
            mParentList.clear();
            mParentList.addAll(filteredList);
        }
    }
}
