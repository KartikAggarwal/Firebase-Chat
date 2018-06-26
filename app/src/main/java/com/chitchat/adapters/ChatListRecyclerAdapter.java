package com.chitchat.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.AppConstants;
import com.chitchat.Utilities.MySharedPref;
import com.chitchat.activities.HomeActivity;
import com.chitchat.beans.ChatListBean;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

public class ChatListRecyclerAdapter extends RecyclerView.Adapter<ChatListRecyclerAdapter.ChatListViewHolder> {
    private List<ChatListBean> mAllChatList;
    private Context mContext;

    @Override
    public ChatListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_row_view, parent, false);
        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatListViewHolder holder, int position) {
        if (mAllChatList.get(position).getMessageType() == 0) {
            if (mAllChatList.get(position).getLastMessage() != null) {
                /*if (mAllChatList.get(position).getLastMessage().length() > 30) {
                    String message = mAllChatList.get(position).getLastMessage().substring(0, 30) + "...";
                    holder.tvLastMessage.setText(message);
                } else*/
                    holder.tvLastMessage.setText(mAllChatList.get(position).getLastMessage());
            } else
                holder.tvLastMessage.setText("");
        } else if (mAllChatList.get(position).getMessageType() == 1)
            holder.tvLastMessage.setText(AppConstants.IMAGE);
        else
            holder.tvLastMessage.setText(AppConstants.LOCATION);

        if (mAllChatList.get(position).getSenderID() != null) {
            if (!mAllChatList.get(position).getSenderID().equals(MySharedPref.getPreference(mContext).getUserID())) {
                if (mAllChatList.get(position).getMessageSeen() == 1)
                    holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
                else
                    holder.tvLastMessage.setTypeface(null, Typeface.BOLD);
            } else
                holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
        }

        holder.tvName.setText(mAllChatList.get(position).getName());
        if (mAllChatList.get(position).getProfilePic() != null) {
            compressPicture(holder.sdvProfile, Uri.parse(mAllChatList.get(position).getProfilePic()));
        } else
            holder.sdvProfile.setActualImageResource(R.drawable.ic_signup_avatar);
    }

    public ChatListRecyclerAdapter(List<ChatListBean> chatList, Context context) {
        if (chatList != null) {
            mAllChatList = chatList;
            mContext = context;
        }
    }


    @Override
    public int getItemCount() {
        return mAllChatList.size();
    }

    class ChatListViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView sdvProfile;
        TextView tvName, tvLastMessage;

        ChatListViewHolder(View itemView) {
            super(itemView);
            sdvProfile = itemView.findViewById(R.id.sdv_chat_image);
            tvName = itemView.findViewById(R.id.tv_chat_contact_name);
            tvLastMessage = itemView.findViewById(R.id.tv_chat_last_msg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAllChatList != null) {
                        ((HomeActivity) mContext).loadChatActivity(mAllChatList.get(getAdapterPosition()).getReceiverNum(), mAllChatList.get(getAdapterPosition()).getName());
                    }
                }
            });
        }
    }

    /**
     * Method to compress profile picture
     */
    private void compressPicture(SimpleDraweeView pic, Uri uri) {
        ImageRequest userPicRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(50, 50))
                .build();
        pic.setController(
                Fresco.newDraweeControllerBuilder()
                        .setOldController(pic.getController())
                        .setImageRequest(userPicRequest)
                        .build());
    }
}
