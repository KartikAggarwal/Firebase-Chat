package com.chitchat.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.AppConstants;
import com.chitchat.Utilities.AppUtils;
import com.chitchat.Utilities.FireBaseDatabaseUtil;
import com.chitchat.activities.ChatActivity;
import com.chitchat.beans.RetrieveMessageBean;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<RetrieveMessageBean> mAllMessages;
    private Context mContext;
    private final int SENDER = 0;
    private final int RECEIVER = 1;
    private String mUserID;
    private boolean mMultipleSelect;
    private List<String> mSelectedList;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case SENDER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sender_message_view, parent, false);
                return new SenderViewHolder(view);
            case RECEIVER:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiver_message_view, parent, false);
                return new ReceiverViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case SENDER:
                SenderViewHolder senderViewHolder = (SenderViewHolder) holder;
                if (mAllMessages.get(position).getMessageType() == 0) {
                    if (mAllMessages.get(position).getMessage() != null) {
                        senderViewHolder.tvMessage.setVisibility(View.VISIBLE);
                        senderViewHolder.sdvLocation.setVisibility(View.GONE);
                        senderViewHolder.pbLoader.setVisibility(View.GONE);
                        senderViewHolder.tvMessage.setText(mAllMessages.get(position).getMessage());
                        senderViewHolder.sdvMedia.setVisibility(View.GONE);
                    } else
                        senderViewHolder.tvMessage.setVisibility(View.GONE);
                } else if (mAllMessages.get(position).getMessageType() == 1) {
                    if (mAllMessages.get(position).getMedia() != null) {
                        senderViewHolder.sdvMedia.setVisibility(View.VISIBLE);
                        senderViewHolder.tvMessage.setVisibility(View.GONE);
                        senderViewHolder.sdvLocation.setVisibility(View.GONE);
                        senderViewHolder.pbLoader.setVisibility(View.VISIBLE);

                        if (mAllMessages.get(position).getSeenStatus() == 3) {
                            ImageRequest userPicRequest = ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(mAllMessages.get(position).getMedia())))
                                    .setResizeOptions(new ResizeOptions(500, 500))
                                    .build();
                            senderViewHolder.sdvMedia.setController(
                                    Fresco.newDraweeControllerBuilder()
                                            .setOldController(senderViewHolder.sdvMedia.getController())
                                            .setImageRequest(userPicRequest)
                                            .build());
                            //senderViewHolder.tvMessageStatus.setText(AppConstants.SENDING);
                            //compressPicture(senderViewHolder.sdvMedia, Uri.fromFile(new File(mAllMessages.get(position).getMedia())), senderViewHolder.pbLoader, 0);
                            if (!AppUtils.checkConnection(mContext)) {
                                senderViewHolder.pbLoader.setVisibility(View.GONE);
                                AppUtils.showToast(mContext, mContext.getResources().getString(R.string.no_internet));
                            }
                        } else
                            compressPicture(senderViewHolder.sdvMedia, Uri.parse(mAllMessages.get(position).getMedia()), senderViewHolder.pbLoader, 1);
                    } else {
                        senderViewHolder.sdvMedia.setVisibility(View.GONE);
                    }
                } else {
                    if (mAllMessages.get(position).getLatitude() != 0.0 && mAllMessages.get(position).getLongitude() != 0.0) {
                        senderViewHolder.sdvMedia.setVisibility(View.GONE);
                        senderViewHolder.tvMessage.setVisibility(View.GONE);
                        senderViewHolder.sdvLocation.setVisibility(View.VISIBLE);
                        senderViewHolder.pbLoader.setVisibility(View.GONE);
                        senderViewHolder.sdvLocation.setImageURI(AppUtils.makeLocationPreview(mAllMessages.get(position).getLatitude(), mAllMessages.get(position).getLongitude(), 17, mContext));
                    }
                }
                senderViewHolder.tvTime.setText(convertTimeStamp(mAllMessages.get(position).getTimeStamp()));
                if (mAllMessages.get(position).getSeenStatus() == 0)
                    senderViewHolder.tvMessageStatus.setText(AppConstants.SENT);
                else if (mAllMessages.get(position).getSeenStatus() == 1)
                    senderViewHolder.tvMessageStatus.setText(AppConstants.SEEN);
                else if (mAllMessages.get(position).getSeenStatus() == 2)
                    senderViewHolder.tvMessageStatus.setText(AppConstants.DELIVERED);
                else if (mAllMessages.get(position).getSeenStatus() == 3)
                    senderViewHolder.tvMessageStatus.setText(AppConstants.SENDING);

                if (mAllMessages.get(position).isSelected())
                    senderViewHolder.llSenderView.setBackgroundResource(R.drawable.selection_highlight);
                else
                    senderViewHolder.llSenderView.setBackgroundResource(0);

                /*if (mAllMessages.get(position).getSeenStatus() == 3) {
                    senderViewHolder.pbLoader.setVisibility(View.VISIBLE);
                    if (!AppUtils.checkConnection(mContext)) {
                        senderViewHolder.pbLoader.setVisibility(View.GONE);
                        AppUtils.showToast(mContext, mContext.getResources().getString(R.string.no_internet));
                    }
                } else if(mAllMessages.get(position).getSeenStatus()==0)
                    senderViewHolder.pbLoader.setVisibility(View.VISIBLE);
                else
                    senderViewHolder.pbLoader.setVisibility(View.GONE);*/
                break;

            case RECEIVER:
                ReceiverViewHolder receiverViewHolder = (ReceiverViewHolder) holder;
                if (mAllMessages.get(position).getMessageType() == 0) {
                    if (mAllMessages.get(position).getMessage() != null) {
                        receiverViewHolder.tvMessage.setVisibility(View.VISIBLE);
                        receiverViewHolder.sdvMedia.setVisibility(View.GONE);
                        receiverViewHolder.sdvLocation.setVisibility(View.GONE);
                        receiverViewHolder.pbLoader.setVisibility(View.GONE);
                        receiverViewHolder.tvMessage.setText(mAllMessages.get(position).getMessage());
                    } else {
                        receiverViewHolder.tvMessage.setVisibility(View.GONE);
                    }
                } else if (mAllMessages.get(position).getMessageType() == 1) {
                    if (mAllMessages.get(position).getMedia() != null) {
                        receiverViewHolder.tvMessage.setVisibility(View.GONE);
                        receiverViewHolder.sdvMedia.setVisibility(View.VISIBLE);
                        receiverViewHolder.sdvLocation.setVisibility(View.GONE);
                        receiverViewHolder.pbLoader.setVisibility(View.VISIBLE);
                        compressPicture(receiverViewHolder.sdvMedia, Uri.parse(mAllMessages.get(position).getMedia()), receiverViewHolder.pbLoader, 1);
                    } else {
                        receiverViewHolder.sdvMedia.setVisibility(View.GONE);
                    }
                } else {
                    if (mAllMessages.get(position).getLatitude() != 0.0 && mAllMessages.get(position).getLongitude() != 0.0) {
                        receiverViewHolder.sdvMedia.setVisibility(View.GONE);
                        receiverViewHolder.tvMessage.setVisibility(View.GONE);
                        receiverViewHolder.sdvLocation.setVisibility(View.VISIBLE);
                        receiverViewHolder.pbLoader.setVisibility(View.GONE);
                        receiverViewHolder.sdvLocation.setImageURI(AppUtils.makeLocationPreview(mAllMessages.get(position).getLatitude(), mAllMessages.get(position).getLongitude(), 17, mContext));
                    }
                }
                receiverViewHolder.tvTime.setText(convertTimeStamp(mAllMessages.get(position).getTimeStamp()));

                if (mAllMessages.get(position).isSelected())
                    receiverViewHolder.llReceiverView.setBackgroundResource(R.drawable.selection_highlight);
                else
                    receiverViewHolder.llReceiverView.setBackgroundResource(0);

                break;
        }
    }

    public ChatRecyclerAdapter(List<RetrieveMessageBean> allMessages, String userID, Context context) {
        mAllMessages = allMessages;
        mUserID = userID;
        mContext = context;
        mSelectedList = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return mAllMessages.size();
    }

    private class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvMessageStatus;
        SimpleDraweeView sdvMedia, sdvLocation;
        ProgressBar pbLoader;
        LinearLayout llSenderView;

        SenderViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_sender);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMessageStatus = itemView.findViewById(R.id.tv_message_status);
            sdvMedia = itemView.findViewById(R.id.sdv_sender);
            pbLoader = itemView.findViewById(R.id.pb_image_loader_sender);
            sdvLocation = itemView.findViewById(R.id.sdv_location_sender);
            llSenderView = itemView.findViewById(R.id.ll_sender);

            sdvMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMultipleSelect)
                        selectOnCLick();
                    else {
                        List<String> list = new ArrayList<>();
                        list.add(mAllMessages.get(getAdapterPosition()).getMedia());
                        new ImageViewer.Builder(mContext, list).show();
                    }
                }
            });
            sdvLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMultipleSelect)
                        selectOnCLick();
                    else {
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=%d&q=%f,%f", mAllMessages.get(getAdapterPosition()).getLatitude(), mAllMessages.get(getAdapterPosition()).getLongitude(), 20, mAllMessages.get(getAdapterPosition()).getLatitude(), mAllMessages.get(getAdapterPosition()).getLongitude());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        mContext.startActivity(intent);
                    }
                }
            });

            sdvMedia.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectOnLongClick();
                    return true;
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectOnLongClick();
                    return true;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMultipleSelect)
                        selectOnCLick();
                }
            });

            sdvLocation.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectOnLongClick();
                    return true;
                }
            });
        }

        /**
         * Method to select message to delete on Single Click
         */
        private void selectOnCLick() {
            if (!mAllMessages.get(getAdapterPosition()).isSelected()) {
                mAllMessages.get(getAdapterPosition()).setSelected(true);
                mSelectedList.add(mAllMessages.get(getAdapterPosition()).getMessageId());
            } else {
                if (mSelectedList.contains(mAllMessages.get(getAdapterPosition()).getMessageId())) {
                    mSelectedList.remove(mAllMessages.get(getAdapterPosition()).getMessageId());
                    mAllMessages.get(getAdapterPosition()).setSelected(false);
                }
            }
            if (mSelectedList.size() == 0) {
                mMultipleSelect = false;
                ((ChatActivity) mContext).changeIconToCall();
            }
            notifyDataSetChanged();
        }

        /**
         * Method to select message on Long Click
         */
        private void selectOnLongClick() {
            if (mSelectedList.size() == 0) {
                mMultipleSelect = true;
                mAllMessages.get(getAdapterPosition()).setSelected(true);
                mSelectedList.add(mAllMessages.get(getAdapterPosition()).getMessageId());
                ((ChatActivity) mContext).changeIconToDelete();
                notifyDataSetChanged();
            }
        }
    }

    private class FrescoListener extends BaseControllerListener<ImageInfo> {
        private ProgressBar pbLoader;
        private int mFlag;
        private SimpleDraweeView sdvImage;

        FrescoListener(ProgressBar pbLoader, int flag, SimpleDraweeView view) {
            this.pbLoader = pbLoader;
            mFlag = flag;
            sdvImage = view;
        }

        @Override
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
            super.onFinalImageSet(id, imageInfo, animatable);
            if (mFlag == 1)
                pbLoader.setVisibility(View.GONE);
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            super.onFailure(id, throwable);
            sdvImage.setActualImageResource(R.drawable.ic_broken);
            pbLoader.setVisibility(View.GONE);
        }
    }

    private class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        SimpleDraweeView sdvMedia, sdvLocation;
        ProgressBar pbLoader;
        LinearLayout llReceiverView;

        ReceiverViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_receiver_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            sdvMedia = itemView.findViewById(R.id.sdv_receiver);
            sdvLocation = itemView.findViewById(R.id.sdv_location_receiver);
            pbLoader = itemView.findViewById(R.id.pb_image_loader_receiver);
            llReceiverView = itemView.findViewById(R.id.ll_receiver);

            sdvMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMultipleSelect)
                        selectOnCLick();
                    else {
                        List<String> list = new ArrayList<>();
                        list.add(mAllMessages.get(getAdapterPosition()).getMedia());
                        new ImageViewer.Builder(mContext, list).show();
                    }
                }
            });

            sdvMedia.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectOnLongClick();
                    return true;
                }
            });

            sdvLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMultipleSelect)
                        selectOnCLick();
                    else {
                        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?z=%d&q=%f,%f", mAllMessages.get(getAdapterPosition()).getLatitude(), mAllMessages.get(getAdapterPosition()).getLongitude(), 20, mAllMessages.get(getAdapterPosition()).getLatitude(), mAllMessages.get(getAdapterPosition()).getLongitude());
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        mContext.startActivity(intent);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMultipleSelect)
                        selectOnCLick();
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectOnLongClick();
                    return true;
                }
            });

            sdvLocation.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectOnLongClick();
                    return true;
                }
            });
        }

        /**
         * Method to select message to delete on Single Click
         */
        private void selectOnCLick() {
            if (!mAllMessages.get(getAdapterPosition()).isSelected()) {
                mAllMessages.get(getAdapterPosition()).setSelected(true);
                mSelectedList.add(mAllMessages.get(getAdapterPosition()).getMessageId());
            } else {
                if (mSelectedList.contains(mAllMessages.get(getAdapterPosition()).getMessageId())) {
                    mSelectedList.remove(mAllMessages.get(getAdapterPosition()).getMessageId());
                    mAllMessages.get(getAdapterPosition()).setSelected(false);
                }
            }
            if (mSelectedList.size() == 0) {
                mMultipleSelect = false;
                ((ChatActivity) mContext).changeIconToCall();
            }
            notifyDataSetChanged();
        }

        /**
         * Method to select message on Long Click
         */
        private void selectOnLongClick() {
            if (mSelectedList.size() == 0) {
                mMultipleSelect = true;
                mAllMessages.get(getAdapterPosition()).setSelected(true);
                mSelectedList.add(mAllMessages.get(getAdapterPosition()).getMessageId());
                ((ChatActivity) mContext).changeIconToDelete();
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (mAllMessages.get(position).getSenderId().equals(mUserID)) ? SENDER : RECEIVER;
    }

    /**
     * Method to convert timeStamp to time
     */
    private String convertTimeStamp(long timeStamp) {
        long currentDateInMS = System.currentTimeMillis();
        long difference = currentDateInMS - timeStamp;
        if (difference == 0)
            return null;
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        long elapsedDays = difference / daysInMilli;

        if (elapsedDays == 1)
            return android.text.format.DateFormat.format("dd-MMM-yyyy", timeStamp).toString();
        else
            return android.text.format.DateFormat.format("hh:mm a", timeStamp).toString();
    }

    /**
     * Method to compress profile picture
     */
    private void compressPicture(SimpleDraweeView pic, Uri uri, ProgressBar pbLoader, int flag) {
        ImageRequest userPicRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(500, 500))
                .build();
        pic.setController(
                Fresco.newDraweeControllerBuilder()
                        .setOldController(pic.getController())
                        .setImageRequest(userPicRequest)
                        .setControllerListener(new FrescoListener(pbLoader, flag, pic))
                        .build());
    }

    /**
     * Method to show Alert Dialog before deleting
     */
    private void showAlert(final String chatRoom) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
       if(mSelectedList.size()==1)
       {
           builder.setMessage("Delete Message")
                   .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            deleteMessages(chatRoom);
                       }
                   })
                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                       }
                   });
       }
       else
       {
           builder.setMessage("Delete " + mSelectedList.size() + " Messages")
                   .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                        deleteMessages(chatRoom);
                       }
                   })
                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                       }
                   });
       }
        builder.show();
    }

    /**
     * Method show dialog on delete button press
     */
    public void showDialog(String chatRoomId) {
        if (mSelectedList.size() > 0) {
            showAlert(chatRoomId);
        }
    }

    /**
     * Method to delete Message
     */
    private void deleteMessages(String chatRoomId)
    {
        for (String item : mSelectedList) {
            for (RetrieveMessageBean message : mAllMessages) {
                if (message.getMessageId().equals(item)) {
                    mAllMessages.remove(message);
                    FireBaseDatabaseUtil.getDatabaseInstance().deleteMessage(item, mUserID, chatRoomId);
                    notifyDataSetChanged();
                    break;
                }
            }
        }
        mSelectedList.clear();
        mMultipleSelect = false;
        ((ChatActivity) mContext).changeIconToCall();
        if (mAllMessages.size() > 0)
            FireBaseDatabaseUtil.getDatabaseInstance().updateLastMessage(mAllMessages.get(mAllMessages.size() - 1), chatRoomId, mUserID);
        else {
            RetrieveMessageBean lastMessage = new RetrieveMessageBean();
            lastMessage.setMessage(null);
            lastMessage.setMessageId(null);
            lastMessage.setMessageType(0);
            lastMessage.setSeenStatus(0);
            lastMessage.setSenderId(null);
            FireBaseDatabaseUtil.getDatabaseInstance().updateLastMessage(lastMessage, chatRoomId, mUserID);
        }
    }
}
