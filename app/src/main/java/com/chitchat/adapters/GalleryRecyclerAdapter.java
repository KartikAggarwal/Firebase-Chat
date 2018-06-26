package com.chitchat.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.chitchat.R;
import com.chitchat.Utilities.AppUtils;
import com.chitchat.activities.ChatActivity;
import com.chitchat.beans.MediaBean;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import java.util.ArrayList;
import java.util.List;

public class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryRecyclerAdapter.GalleryViewHolder> {
    private List<MediaBean> mImageList;
    public List<MediaBean> mSelectedMedia;
    private Context mContext;

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_view, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        compressPicture(holder.sdvImage, Uri.fromFile(mImageList.get(position).getImage()));
        holder.cbSelect.setChecked(mImageList.get(position).isSelected());
    }

    public GalleryRecyclerAdapter(List<MediaBean> imageList, Context context) {
        mImageList = imageList;
        mSelectedMedia = new ArrayList<>();
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView sdvImage;
        CheckBox cbSelect;

        GalleryViewHolder(View itemView) {
            super(itemView);
            sdvImage = itemView.findViewById(R.id.sdv_gallery_image);
            cbSelect = itemView.findViewById(R.id.cb_media_select);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImage(!mImageList.get(getAdapterPosition()).isSelected());
                }
            });
        }

        /**
         * Method to select image on Click
         */
        private void selectImage(boolean check) {
            if (mSelectedMedia.size() != 5) {
                cbSelect.setClickable(true);
                mImageList.get(getAdapterPosition()).setSelected(check);
                if (mImageList.get(getAdapterPosition()).isSelected()) {
                    if (!mSelectedMedia.contains(mImageList.get(getAdapterPosition())))
                        mSelectedMedia.add(mImageList.get(getAdapterPosition()));
                } else {
                    if (mSelectedMedia.contains(mImageList.get(getAdapterPosition())))
                        mSelectedMedia.remove(mImageList.get(getAdapterPosition()));
                }
            } else {
                cbSelect.setClickable(false);
                if (mImageList.get(getAdapterPosition()).isSelected()) {
                    mImageList.get(getAdapterPosition()).setSelected(check);
                    if (mSelectedMedia.contains(mImageList.get(getAdapterPosition())))
                        mSelectedMedia.remove(mImageList.get(getAdapterPosition()));
                } else
                    AppUtils.showToast(mContext, mContext.getResources().getString(R.string.maximum_media));
            }
            notifyDataSetChanged();

            if (mSelectedMedia.size() == 0)
                ((ChatActivity) mContext).hideSendButton();
            else if (mSelectedMedia.size() > 0 && mSelectedMedia.size() <= 5)
                ((ChatActivity) mContext).showSendButton();
        }
    }


    /**
     * Method to compress profile picture
     */
    private void compressPicture(SimpleDraweeView pic, Uri uri) {
        ImageRequest userPicRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(300, 300))
                .build();
        pic.setController(
                Fresco.newDraweeControllerBuilder()
                        .setOldController(pic.getController())
                        .setImageRequest(userPicRequest)
                        .build());
    }
}
