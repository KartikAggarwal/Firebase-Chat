package com.chitchat.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.AppConstants;
import com.chitchat.Utilities.AppUtils;
import com.chitchat.Utilities.FireBaseDatabaseUtil;
import com.chitchat.adapters.ChatRecyclerAdapter;
import com.chitchat.adapters.GalleryRecyclerAdapter;
import com.chitchat.beans.MediaBean;
import com.chitchat.beans.RetrieveMessageBean;
import com.chitchat.beans.UserBean;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener, View.OnTouchListener, EditText.OnFocusChangeListener {
    private ImageView ivMedia, ivSeparator, ivLocation, ivEmoji;
    private EditText etMessage;
    private TextView btn_send, tvLastSeen;
    private String mUID, mReceiverName;
    private String mReceiverPhone;
    private RecyclerView rvMessages;
    private String mChatRoomID;
    private ChatRecyclerAdapter mChatAdapter;
    private List<RetrieveMessageBean> mAllMessages;
    private UserBean mReceiver;
    private Handler mTimeoutHandler;
    private Runnable mTimeOut;
    private boolean mIsTyping;
    private RecyclerView rvGallery;
    private List<MediaBean> mGalleryList;
    private GalleryRecyclerAdapter mGalleryAdapter;
    private LinearLayout llGallery;
    private Dialog mDialog;
    private double mLatitude;
    private double mLongitude;
    private ImageView ivCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initViews();
        initVariables();
    }

    /**
     * Method to initialise views
     */
    private void initViews() {
        if (getIntent() != null) {
            mUID = getIntent().getStringExtra(getResources().getString(R.string.user_id));
            mReceiverPhone = getIntent().getStringExtra(getResources().getString(R.string.receiver_no));
            mReceiverName = getIntent().getStringExtra(getResources().getString(R.string.receiver_name));
        }

        Toolbar tbToolbar = (Toolbar) findViewById(R.id.tb_toolbar);
        ImageView ivBack = (ImageView) findViewById(R.id.iv_toolbar_left);
        ivBack.setImageResource(R.drawable.ic_otp_back);
        tvLastSeen = (TextView) findViewById(R.id.tv_last_seen);
        setSupportActionBar(tbToolbar);
        TextView tvName = (TextView) findViewById(R.id.tv_person_name);
        tvName.setText(mReceiverName);
        ivCall = (ImageView) findViewById(R.id.iv_toolbar_right);
        ivBack.setOnClickListener(this);
        ivCall.setImageResource(R.drawable.ic_usermessage_phone);
        ivCall.setOnClickListener(this);
        rvMessages = (RecyclerView) findViewById(R.id.rv_messages);
        rvGallery = (RecyclerView) findViewById(R.id.rv_gallery);
        tvLastSeen = (TextView) findViewById(R.id.tv_last_seen);
        etMessage = (EditText) findViewById(R.id.et_message);
        etMessage.setOnFocusChangeListener(this);
        etMessage.setOnClickListener(this);
        ivMedia = (ImageView) findViewById(R.id.iv_media);
        ivSeparator = (ImageView) findViewById(R.id.iv_seperator);
        ivLocation = (ImageView) findViewById(R.id.iv_location);
        ivLocation.setOnClickListener(this);
        btn_send = (TextView) findViewById(R.id.tv_send);
        ivEmoji = (ImageView) findViewById(R.id.iv_emoji);
        ivEmoji.setOnClickListener(this);
        etMessage.addTextChangedListener(this);
        ivMedia.setOnClickListener(this);
        btn_send.setOnClickListener(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvMessages.setLayoutManager(layoutManager);
        llGallery = (LinearLayout) findViewById(R.id.ll_gallery);
        mTimeoutHandler = new Handler();
        mTimeOut = new Runnable() {
            public void run() {
                mIsTyping = false;
                FireBaseDatabaseUtil.getDatabaseInstance().changeOnlineStatus(mUID);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mUID != null)
            FireBaseDatabaseUtil.getDatabaseInstance().changeOnlineStatus(mUID);
    }

    /**
     * Method To initials Variables
     */
    private void initVariables() {
        mAllMessages = new ArrayList<>();
        FireBaseDatabaseUtil.getDatabaseInstance().changeOnlineStatus(mUID);
        FireBaseDatabaseUtil.getDatabaseInstance().getReceiverDetails(mReceiverPhone, this, 0, 0);
        mChatAdapter = new ChatRecyclerAdapter(mAllMessages, mUID, this);
        rvMessages.setAdapter(mChatAdapter);
        mGalleryList = new ArrayList<>();
        mGalleryAdapter = new GalleryRecyclerAdapter(mGalleryList, this);
        rvGallery.setLayoutManager(new GridLayoutManager(this, 3));
        rvGallery.setAdapter(mGalleryAdapter);
    }

    @Override
    public void onBackPressed() {
        if (llGallery.getVisibility() == View.VISIBLE) {
            llGallery.setVisibility(View.GONE);
            mGalleryAdapter.mSelectedMedia.clear();
            hideSendButton();
        } else
            super.onBackPressed();
    }

    /**
     * Method To change the last Seen status
     */
    public void changeLastSeen(UserBean receiver) {
        mReceiver = receiver;
        if (mReceiver != null) {
            if (mReceiver.getOnlineStatus() == 0) {
                String lastSeen = getResources().getString(R.string.last_seen) + DateFormat.format("dd-MMM-yy hh:mm a", (Long) mReceiver.getLastSeenStatus());
                tvLastSeen.setText(lastSeen);
            } else if (mReceiver.getOnlineStatus() == 1) {
                tvLastSeen.setText(getResources().getString(R.string.online));
            } else {
                tvLastSeen.setText(getResources().getString(R.string.typing));
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    /**
     * Method to hide Send button
     */
    public void hideSendButton() {
        btn_send.setVisibility(View.GONE);
        ivMedia.setVisibility(View.VISIBLE);
        ivSeparator.setVisibility(View.VISIBLE);
        ivLocation.setVisibility(View.VISIBLE);
    }

    /**
     * Method to show Send button
     */
    public void showSendButton() {
        btn_send.setVisibility(View.VISIBLE);
        ivMedia.setVisibility(View.GONE);
        ivSeparator.setVisibility(View.GONE);
        ivLocation.setVisibility(View.GONE);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (TextUtils.isEmpty(charSequence)) {
            hideSendButton();
        } else {
            showSendButton();
        }
        mTimeoutHandler.removeCallbacks(mTimeOut);
        if (etMessage.getText().toString().trim().length() > 0) {
            mTimeoutHandler.postDelayed(mTimeOut, AppConstants.TYPING_TIMEOUT);

            if (!mIsTyping) {
                mIsTyping = true;
                FireBaseDatabaseUtil.getDatabaseInstance().changeTypingStatus(mUID);
            }
        } else {
            mIsTyping = false;
            FireBaseDatabaseUtil.getDatabaseInstance().changeOnlineStatus(mUID);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatRoomID != null && mUID != null) {
            FireBaseDatabaseUtil.getDatabaseInstance().removeChatListener(mChatRoomID);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FireBaseDatabaseUtil.getDatabaseInstance().changeLastSeen(mUID);
    }

    /**
     * Method to load gallery
     */
    private void loadGallery() {
        hideKeyPad();
        if (llGallery.getVisibility() == View.VISIBLE)
            llGallery.setVisibility(View.GONE);
        else {
            llGallery.setVisibility(View.VISIBLE);
            String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
            String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            ContentResolver cr = getContentResolver();
            Cursor imagesCursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
            if (imagesCursor != null) {
                mGalleryList.clear();
                for (int i = 0; i < imagesCursor.getCount(); i++) {
                    imagesCursor.moveToPosition(i);
                    int dataColumnIndex = imagesCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    MediaBean media = new MediaBean();
                    media.setImage(new File(imagesCursor.getString(dataColumnIndex)));
                    mGalleryList.add(media);
                }
                imagesCursor.close();
            }
            if (mGalleryList.size() > 0) {
                if (mGalleryAdapter != null)
                    mGalleryAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_media:
                if (mGalleryAdapter != null)
                    if (mGalleryAdapter.mSelectedMedia != null && mGalleryAdapter.mSelectedMedia.size() > 0)
                        mGalleryAdapter.mSelectedMedia.clear();
                requestReadPermission();
                break;
            case R.id.iv_emoji:
                break;
            case R.id.tv_send:
                llGallery.setVisibility(View.GONE);
                if (!etMessage.getText().toString().trim().equals(""))
                    FireBaseDatabaseUtil.getDatabaseInstance().getReceiverDetails(mReceiverPhone, this, 1, 0);
                else {
                    if (mGalleryAdapter != null && mGalleryAdapter.mSelectedMedia != null && mGalleryAdapter.mSelectedMedia.size() > 0) {
                        FireBaseDatabaseUtil.getDatabaseInstance().getReceiverDetails(mReceiverPhone, this, 1, 1);
                    } else
                        AppUtils.showToast(this, getResources().getString(R.string.enter_something));
                }
                break;
            case R.id.iv_location:
                mDialog = new Dialog(this);
                mDialog.setContentView(R.layout.location_dialog);
                if (mDialog.getWindow() != null)
                    mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView textView = mDialog.findViewById(R.id.tv_share_status);
                textView.setText(getString(R.string.confirm_share_location_text) + " " + mReceiverName + "?");
                Button btnYes = mDialog.findViewById(R.id.b_yes);
                Button btnNo = mDialog.findViewById(R.id.b_no);
                cornerCurves(btnYes, R.color.app_color, new float[]{0, 0, 0, 0, 25, 25, 0, 0});
                cornerCurves(btnNo, R.color.light_grey, new float[]{0, 0, 0, 0, 0, 0, 25, 25});
                mDialog.show();
                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestLocationPermission();
                    }
                });
                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });
                break;
            case R.id.iv_toolbar_left: {
                hideKeyPad();
                onBackPressed();
            }
                break;
            case R.id.et_message:
                llGallery.setVisibility(View.GONE);
                break;
            case R.id.iv_toolbar_right:
                if(ivCall.getDrawable().getConstantState()!=getResources().getDrawable(R.drawable.ic_usermessage_phone).getConstantState())
                {
                    if(mChatAdapter!=null)
                        mChatAdapter.showDialog(mChatRoomID);
                }
                break;
        }
    }

    /**
     * Method to curve the corners of buttons in Location dialog
     */
    public void cornerCurves(View view, int color, float[] floats) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(ContextCompat.getColor(this, color));
        shape.setCornerRadii(floats);
        if (Build.VERSION.SDK_INT >= 19) {
            view.setBackground(shape);
        } else view.setBackgroundDrawable(shape);
    }


    /**
     * Method to request user for ACCESS FINE Location
     */
    private void requestLocationPermission() {
        if ((ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            showPlacePicker();
        else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, AppConstants.ACCESS_FINE_LOCATION_REQUEST);
    }


    /**
     * Method to show Place Picker to User
     */
    private void showPlacePicker() {
        mDialog.dismiss();
        PlacePicker.IntentBuilder locationBuilder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(locationBuilder.build(this), AppConstants.MAP_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.MAP_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                LatLng latLng = place.getLatLng();
                mLatitude = latLng.latitude;
                mLongitude = latLng.longitude;
                FireBaseDatabaseUtil.getDatabaseInstance().getReceiverDetails(mReceiverPhone, this, 1, 2);
            }
        }
    }

    /**
     * Method get ChatRoom
     */
    public void getChatRoom(String chatRoomID, boolean firstTimeStatus, int messageType) {

        if (messageType == 0) {
            if (!TextUtils.isEmpty(etMessage.getText()))
                FireBaseDatabaseUtil.getDatabaseInstance().createMessageNode(chatRoomID, etMessage.getText().toString(), mUID, firstTimeStatus, null, 0, 0, 0, this,mReceiver.getuId());
        } else if (messageType == 1) {
            if (mGalleryAdapter.mSelectedMedia.size() > 0) {
                for (MediaBean media : mGalleryAdapter.mSelectedMedia) {
                    RetrieveMessageBean message = new RetrieveMessageBean();
                    message.setSeenStatus(3);
                    message.setMedia(media.getImage().toString());
                    message.setSenderId(mUID);
                    message.setTimeStamp(System.currentTimeMillis());
                    message.setMessageType(1);
                    setImageMessages(message);
                    FireBaseDatabaseUtil.getDatabaseInstance().uploadMedia(media.getImage(), chatRoomID, firstTimeStatus, this);
                }
               /*for (MediaBean media : mGalleryAdapter.mSelectedMedia)
                   FireBaseDatabaseUtil.getDatabaseInstance().uploadMedia(media.getImage(), chatRoomID, firstTimeStatus, this);*/
                mGalleryAdapter.mSelectedMedia.clear();
            }
        } else if (messageType == 2) {
            if (mLatitude != 0.0 && mLongitude != 0.0) {
                FireBaseDatabaseUtil.getDatabaseInstance().createMessageNode(chatRoomID, null, mUID, firstTimeStatus, null, 2, mLatitude, mLongitude, this,mReceiver.getuId());
                mLatitude = 0.0;
                mLongitude = 0.0;
            }
        }
        etMessage.setText("");
    }

    /**
     * Method get ChatRoom for Messages
     */
    public void getChatRoomForMessage(String chatRoomID, boolean status) {
        if (status)
            if (chatRoomID != null) {
                mChatRoomID = chatRoomID;
                FireBaseDatabaseUtil.getDatabaseInstance().getAllMessages(mChatRoomID, this, mUID,mReceiver.getuId());
            }
    }

    /**
     * Method to get callback and details of current Receiver
     */
    public void getCurrentReceiver(UserBean currentReceiver, int callStatus, int messageType) {
        if (currentReceiver != null) {
            mReceiver = currentReceiver;
            changeLastSeen(mReceiver);
            if (callStatus == 0)
                FireBaseDatabaseUtil.getDatabaseInstance().isReceiverExistsForMessage(mUID, currentReceiver.getuId(), this);
            else
                FireBaseDatabaseUtil.getDatabaseInstance().isUserExistsInbox(mUID, currentReceiver.getuId(), this, messageType);
        }
    }


    /**
     * Method to put image Messages in list
     */
    public void setImageMessages(RetrieveMessageBean message) {
        if (message != null) {
            mAllMessages.add(message);
            rvMessages.scrollToPosition(mAllMessages.size() - 1);
            mChatAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Method to get All Messages
     */
    public void getAllMessages(RetrieveMessageBean message) {
        int flag = 0;
        if (message != null) {
            if (message.getMedia() != null) {
                for (int l = 0; l < mAllMessages.size(); l++) {
                    flag = 0;
                    if (mAllMessages.get(l).getSeenStatus() == 3) {
                        mAllMessages.remove(l);
                        mAllMessages.add(l, message);
                        flag = 1;
                        break;
                    }
                }
            }
            if (flag == 0)
                mAllMessages.add(message);
            rvMessages.scrollToPosition(mAllMessages.size() - 1);
            mChatAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Method to update Seen Status
     */
    public void updateSeenStatus(RetrieveMessageBean message) {
        if (message != null) {
            for (int l = 0; l < mAllMessages.size(); l++) {
                if (message.getMessageId().equals(mAllMessages.get(l).getMessageId())) {
                    mAllMessages.get(l).setSeenStatus(message.getSeenStatus());
                    mChatAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }



    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        hideKeyPad();
        return true;
    }

    /**
     * Method to Hide Keypad on View Click
     */
    private void hideKeyPad() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Method to request permission for camera
     */
    private void requestReadPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, AppConstants.REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
        } else {
            loadGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConstants.REQUEST_READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    loadGallery();
                break;
            case AppConstants.ACCESS_FINE_LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showPlacePicker();
                }
                break;
        }
    }

    /**
     * Method to get download url of uploaded media
     */
    public void uploadDetails(String media, String chatRoom, boolean firstTimeStatus) {
        if (media != null) {
            FireBaseDatabaseUtil.getDatabaseInstance().createMessageNode(chatRoom, null, mUID, firstTimeStatus, media, 1, 0, 0, this,mReceiver.getuId());
        }
    }

    /**
     * Method to change the Call icon to Delete icon
     */
    public void changeIconToDelete() {
        ivCall.setImageResource(R.drawable.ic_delete);
    }

    /**
     * Method to change the Delete icon to Call icon
     */
    public void changeIconToCall() {
        ivCall.setImageResource(R.drawable.ic_usermessage_phone);
        rvMessages.scrollToPosition(mAllMessages.size()-1);
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        llGallery.setVisibility(View.GONE);
    }
}
