package com.chitchat.Utilities;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import com.chitchat.activities.ChatActivity;
import com.chitchat.activities.HomeActivity;
import com.chitchat.activities.SignUpActivity;
import com.chitchat.activities.UserDetailActivity;
import com.chitchat.beans.ChatListBean;
import com.chitchat.beans.ChatRoomBean;
import com.chitchat.beans.MessageBean;
import com.chitchat.beans.RetrieveMessageBean;
import com.chitchat.beans.UserBean;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FireBaseDatabaseUtil {
    private static FireBaseDatabaseUtil mDatabaseUtil;
    private DatabaseReference mDbReference;
    private StorageReference mStorageRef;
    private FirebaseDatabase mDatabase;
    private ChildEventListener mMessageListener, mReceiverListener, mChatListener;

    private FireBaseDatabaseUtil() {
        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.setPersistenceEnabled(true);
        mDbReference = mDatabase.getReference();
        mDbReference.keepSynced(true);
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    public static FireBaseDatabaseUtil getDatabaseInstance() {
        if (mDatabaseUtil == null)
            mDatabaseUtil = new FireBaseDatabaseUtil();
        return mDatabaseUtil;
    }

    /**
     * Method to create User node
     */
    public void createUser(String name, String lastName, String profilePic, String phoneNumber, Context context) {
        mDbReference = mDatabase.getReference(AppConstants.USER_NODE);
        String uId = mDbReference.push().getKey();
        MySharedPref.getPreference(context).editor(name, lastName, uId, phoneNumber);
        mDbReference.child(uId).setValue(new UserBean(name, lastName, uId, ServerValue.TIMESTAMP, profilePic, phoneNumber, 0));
        ((UserDetailActivity) context).userRegistered(uId);
    }

    /**
     * Method to upload Profile Picture
     */
    public void UploadProfilePic(Uri image, String name, String lastName, final Context context) {
        UploadTask uploadTask = mStorageRef.child(AppConstants.USER_NODE).child(AppConstants.STORAGE_PATH).child(name + " " + lastName + "/").putFile(image);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getDownloadUrl() != null)
                    ((UserDetailActivity) context).uploadDetails(taskSnapshot.getDownloadUrl().toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AppUtils.showToast(context, AppConstants.FAIL);
            }
        });
    }


    /**
     * Method to upload media
     */
    public void uploadMedia(File image, final String chatRoom, final boolean isFirstTime, final Context context) {
        String mediaID = mDbReference.push().getKey();
        UploadTask uploadTask = mStorageRef.child(AppConstants.MESSAGE_NODE).child(AppConstants.MEDIA + "/").child(mediaID + "/").putFile(Uri.fromFile(image));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getDownloadUrl() != null) {
                    ((ChatActivity) context).uploadDetails(taskSnapshot.getDownloadUrl().toString(), chatRoom, isFirstTime);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AppUtils.showToast(context, AppConstants.FAIL);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }

    /**
     * Method to check if user exists or not
     */
    public void isUserExists(String phone, final Context context) {
        mDbReference = mDatabase.getReference();
        Query checkUser = mDbReference.child(AppConstants.USER_NODE).orderByChild(AppConstants.USER_NUMBER).equalTo(phone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    UserBean currentUser = new UserBean();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        currentUser.setPhoneNumber((String) snapshot.child(AppConstants.USER_NUMBER).getValue());
                        currentUser.setName((String) snapshot.child(AppConstants.USER_FIRST_NAME).getValue());
                        currentUser.setLastName((String) snapshot.child(AppConstants.USER_LAST_NAME).getValue());
                        currentUser.setuId((String) snapshot.child(AppConstants.USER_ID).getValue());
                    }
                    ((SignUpActivity) context).setIntent(currentUser, true);
                } else
                    ((SignUpActivity) context).isVerified(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                AppUtils.showToast(context, AppConstants.FAIL);
            }
        });
    }

    /**
     * Method to change the Message Seen Status
     */
    private void setMessageSeen(String chatRoom, String messageID) {
        if (chatRoom != null && messageID != null) {
            mDbReference = mDatabase.getReference();
            mDbReference.child(AppConstants.MESSAGE_NODE).child(chatRoom).child(messageID).child(AppConstants.MESSAGE_STATUS).setValue(1);
        }
    }

    /**
     * Method to change the Last Message Seen Status
     */
    private void setLastMessageSeen(String chatRoom, String userId, String receiverId) {
        if (chatRoom != null) {
            mDbReference = mDatabase.getReference();
            mDbReference.child(AppConstants.LAST_MESSAGE_NODE).child(chatRoom).child(userId).child(AppConstants.MESSAGE_STATUS).setValue(1);
            mDbReference.child(AppConstants.LAST_MESSAGE_NODE).child(chatRoom).child(receiverId).child(AppConstants.MESSAGE_STATUS).setValue(1);
        }
    }

    /**
     * Method to change the Message Deliverd Status
     */
    private void setMessageDelivered(String chatRoom, String messageID) {
        if (chatRoom != null && messageID != null) {
            mDbReference = mDatabase.getReference();
            mDbReference.child(AppConstants.MESSAGE_NODE).child(chatRoom).child(messageID).child(AppConstants.MESSAGE_STATUS).setValue(2);
        }
    }

    /**
     * Method to check if user exists in Inbox or not
     */
    public void isUserExistsInbox(final String userID, final String receiverID, final Context context, final int messageType) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.INBOX_NODE).child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    isReceiverExists(userID, receiverID, context, messageType);
                } else {
                    createChatRoom(userID, receiverID, context, messageType);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                AppUtils.showToast(context, AppConstants.FAIL);
            }
        });
    }

    /**
     * Method to check if receiver exists under sender in inbox
     */
    private void isReceiverExists(final String userID, final String receiverID, final Context context, final int messageType) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.INBOX_NODE).child(userID).child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null)
                    ((ChatActivity) context).getChatRoom((String) dataSnapshot.getValue(), false, messageType);
                else
                    createChatRoom(userID, receiverID, context, messageType);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                AppUtils.showToast(context, AppConstants.FAIL);
            }
        });
    }

    /**
     * Method to check if receiver exists under sender in inbox for Messages
     */
    public void isReceiverExistsForMessage(final String userID, final String receiverID, final Context context) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.INBOX_NODE).child(userID).child(receiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null)
                    ((ChatActivity) context).getChatRoomForMessage((String) dataSnapshot.getValue(), true);
                else
                    ((ChatActivity) context).getChatRoomForMessage(null, false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                AppUtils.showToast(context, AppConstants.FAIL);
            }
        });
    }

    /**
     * Method to get all the messages from database for particular user
     */
    public void getAllMessages(final String chatRoomID, final Context context, final String userID, final String receiverId) {
        mDbReference = mDatabase.getReference();
        mMessageListener = mDbReference.child(AppConstants.MESSAGE_NODE).child(chatRoomID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (!dataSnapshot.hasChild(userID)) {
                            RetrieveMessageBean message = dataSnapshot.getValue(RetrieveMessageBean.class);
                            ((ChatActivity) context).getAllMessages(message);
                            if (message != null)
                                if (!message.getSenderId().equals(userID)) {
                                    setMessageSeen(chatRoomID, message.getMessageId());
                                    setLastMessageSeen(chatRoomID, userID, receiverId);
                                }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot != null) {
                            RetrieveMessageBean message = dataSnapshot.getValue(RetrieveMessageBean.class);
                            ((ChatActivity) context).updateSeenStatus(message);
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Method to create user node in Inbox
     */
    private void createUserNodeInbox(String senderID, String receiverID, String chatRoomID, Context context, int messageType) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.INBOX_NODE).child(senderID).child(receiverID).setValue(chatRoomID);
        mDbReference.child(AppConstants.INBOX_NODE).child(receiverID).child(senderID).setValue(chatRoomID);
        ((ChatActivity) context).getChatRoom(chatRoomID, true, messageType);
    }

    /**
     * Method to create receiver in Inbox
     */
    /*private void createReceiverInbox(String senderID, String receiverID, String chatRoomID, Context context) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.INBOX_NODE).child(senderID).child(receiverID).setValue(chatRoomID);
        mDbReference.child(AppConstants.INBOX_NODE).child(receiverID).child(senderID).setValue(chatRoomID);
        ((ChatActivity) context).getChatRoom(chatRoomID);
    }*/


    /**
     * Method to create Last Message in node
     */
    private void createLastMessage(String chatRoomID, MessageBean lastMessage, String userId) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.LAST_MESSAGE_NODE).child(chatRoomID).child(userId).setValue(lastMessage);
        mDbReference.child(AppConstants.LAST_MESSAGE_NODE).child(chatRoomID).child(userId).child(userId).setValue(ServerValue.TIMESTAMP);
    }

    /**
     * Method to create Message node in database
     */
    public void createMessageNode(String chatRoomID, String message, String senderID, boolean firstTimeStatus, String media, int messageType, double latitude, double longitude, Context context, String receiverId) {
        String messageID = mDbReference.push().getKey();
        MessageBean messageBean;

        if (messageType == 0)
            messageBean = new MessageBean(messageID, ServerValue.TIMESTAMP, message.trim(), 0, senderID, messageType);
        else if (messageType == 1)
            messageBean = new MessageBean(messageID, ServerValue.TIMESTAMP, messageType, 0, senderID, media);
        else
            messageBean = new MessageBean(messageID, ServerValue.TIMESTAMP, 0, senderID, latitude, longitude, messageType);

        mDbReference.child(AppConstants.MESSAGE_NODE).child(chatRoomID).child(messageID).setValue(messageBean);
        if (firstTimeStatus)
            getAllMessages(chatRoomID, context, senderID, null);
        createLastMessage(chatRoomID, messageBean, receiverId);
        createLastMessage(chatRoomID, messageBean, senderID);
    }

    /**
     * Method to create Chat Room in database
     */
    private void createChatRoom(String senderId, String receiverId, Context context, int messageType) {
        mDbReference = mDatabase.getReference(AppConstants.CHAT_ROOM_NODE);
        String chatRoomId = mDbReference.push().getKey();
        mDbReference.child(chatRoomId).setValue(new ChatRoomBean(senderId, receiverId));
        createUserNodeInbox(senderId, receiverId, chatRoomId, context, messageType);
    }

    /**
     * Method to get last Message
     */
    public void getLastMessage(final String chatRoom, final String name, final String receiverProfile, final String receiverNum, final Context context, final String userId) {
        if (chatRoom != null) {
            mDbReference = mDatabase.getReference();
            mDbReference.child(AppConstants.LAST_MESSAGE_NODE).child(chatRoom)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (dataSnapshot.getValue() != null) {
                                {
                                    if (dataSnapshot.getKey().equals(userId)) {
                                        RetrieveMessageBean message = dataSnapshot.getValue(RetrieveMessageBean.class);
                                        if (message != null) {
                                            ChatListBean chat = new ChatListBean();
                                            chat.setName(name);
                                            chat.setChatRoomID(chatRoom);
                                            chat.setLastMessage(message.getMessage());
                                            chat.setMessageID(message.getMessageId());
                                            chat.setProfilePic(receiverProfile);
                                            chat.setReceiverNum(receiverNum);
                                            chat.setMessageType(message.getMessageType());
                                            chat.setImage(message.getMedia());
                                            chat.setSenderID(message.getSenderId());
                                            chat.setMessageSeen(message.getSeenStatus());
                                            ((HomeActivity) context).getLastMessage(chat);
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            if (dataSnapshot.getValue() != null) {
                                if (dataSnapshot.hasChild(userId))
                                    ((HomeActivity) context).updateLastMessage(dataSnapshot.getValue(RetrieveMessageBean.class), chatRoom);
                            }
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }
    }

    /**
     * Method to get all users from database
     */
    public void getAllUsers(final Context context) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.USER_NODE).orderByKey()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<UserBean> allUser = new ArrayList<>();
                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot != null) {
                                    allUser.add(snapshot.getValue(UserBean.class));
                                   /* UserBean user = new UserBean();
                                    user.setuId((String) snapshot.child(AppConstants.USER_ID).getValue());
                                    user.setName((String) snapshot.child(AppConstants.USER_FIRST_NAME).getValue());
                                    user.setLastName((String) snapshot.child(AppConstants.USER_LAST_NAME).getValue());
                                    user.setPhoneNumber((String) snapshot.child(AppConstants.USER_NUMBER).getValue());
                                    user.setOnlineStatus((Integer) snapshot.child(AppConstants.ONLINE_STATUS).getValue());
                                    user.setLastSeenStatus((Long)snapshot.child(AppConstants.LAST_SEEN).getValue());
                                    user.setProfilePic((String) snapshot.child(AppConstants.PROFILE_PIC).getValue());
                                    allUser.add(user);*/
                                }
                            }
                            ((HomeActivity) context).allUsers(allUser);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                /*.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null) {
                            UserBean user = dataSnapshot.getValue(UserBean.class);
                            ((HomeActivity) context).allUsers(user);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });*/

    }

    /**
     * Method to remove listener from user
     */
    public void removeUserListener() {
        if (mDbReference != null && mChatListener != null) {
            mDbReference.child(AppConstants.USER_NODE).removeEventListener(mChatListener);
        }
    }


    /**
     * Method to remove listener from chat
     */
    public void removeChatListener(String chatRoom) {
        mDbReference.child(AppConstants.MESSAGE_NODE).child(chatRoom).removeEventListener(mMessageListener);
        mDbReference.child(AppConstants.USER_NODE).child(AppConstants.USER_NUMBER).removeEventListener(mReceiverListener);
    }

    /**
     * Method to get current user details
     */
    /*public void getUserDetails(String phone, final Context context) {
        mDbReference = mDatabase.getReference();
        Query checkUser = mDbReference.child(AppConstants.USER_NODE).orderByChild(AppConstants.USER_NUMBER).equalTo(phone);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserBean currentUser = new UserBean();
                        currentUser.setPhoneNumber((String) snapshot.child(AppConstants.USER_NUMBER).getValue());
                        currentUser.setName((String) snapshot.child(AppConstants.USER_FIRST_NAME).getValue());
                        currentUser.setLastName((String) snapshot.child(AppConstants.USER_LAST_NAME).getValue());
                        currentUser.setuId((String) snapshot.child(AppConstants.USER_ID).getValue());
                        ((HomeActivity) context).getCurrentUser(currentUser);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/


    /**
     * Method to change online status of user
     */
    public void changeOnlineStatus(String userID) {
        if (userID != null) {
            mDbReference = mDatabase.getReference();
            mDbReference.child(AppConstants.USER_NODE).child(userID).child(AppConstants.ONLINE_STATUS).setValue(1);
        }
    }

    /**
     * Method to change typing status of user
     */
    public void changeTypingStatus(String userID) {
        if (userID != null) {
            mDbReference = mDatabase.getReference();
            mDbReference.child(AppConstants.USER_NODE).child(userID).child(AppConstants.ONLINE_STATUS).setValue(2);
        }
    }

    /**
     * Method to change last seen status of user
     */
    public void changeLastSeen(String userID) {
        if (userID != null) {
            mDbReference = mDatabase.getReference();
            mDbReference.child(AppConstants.USER_NODE).child(userID).child(AppConstants.ONLINE_STATUS).setValue(0);
            mDbReference.child(AppConstants.USER_NODE).child(userID).child(AppConstants.LAST_SEEN).setValue(ServerValue.TIMESTAMP);
        }
    }

    /**
     * Method to get receiver details
     */
    public void getReceiverDetails(String phone, final Context context, final int callStatus, final int messageType) {
        if (phone.contains(" "))
            phone = phone.replaceAll(" ", "");
        if (phone.contains("-"))
            phone = phone.replaceAll("-", "");
        if (phone.length() == 13)
            phone = phone.substring(3);
        if (phone.length() == 11)
            phone = phone.substring(1);
        mDbReference = mDatabase.getReference();
        mReceiverListener = mDbReference.child(AppConstants.USER_NODE).orderByChild(AppConstants.USER_NUMBER).equalTo(phone)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null) {
                            UserBean currentReceiver = dataSnapshot.getValue(UserBean.class);
                            ((ChatActivity) context).getCurrentReceiver(currentReceiver, callStatus, messageType);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null)
                            ((ChatActivity) context).changeLastSeen(dataSnapshot.getValue(UserBean.class));
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    /**
     * Method to get All chats for a user
     */
    public void getAllChatList(String userID, final Context context) {
        mDbReference = mDatabase.getReference();
        mChatListener = mDbReference.child(AppConstants.INBOX_NODE).child(userID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (dataSnapshot.getValue() != null)
                            ((HomeActivity) context).loadChatList(dataSnapshot.getKey(), dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    /**
     * Method to delete Message
     */
    public void deleteMessage(String messageId, String userId, String chatRoomId) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.MESSAGE_NODE).child(chatRoomId).child(messageId).child(userId).setValue(ServerValue.TIMESTAMP);
    }

    /**
     * Method to update Last Message after Deleting Messages
     */
    public void updateLastMessage(RetrieveMessageBean message, String chatRoomId, String userId) {
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.LAST_MESSAGE_NODE).child(chatRoomId).child(userId).setValue(message);
        mDbReference.child(AppConstants.LAST_MESSAGE_NODE).child(chatRoomId).child(userId).child(userId).setValue(ServerValue.TIMESTAMP);
    }

    /**
     * Method to get Last Message
     */
    private RetrieveMessageBean getMessage(String chatRoom) {
        final RetrieveMessageBean messageBean = new RetrieveMessageBean();
        mDbReference = mDatabase.getReference();
        mDbReference.child(AppConstants.LAST_MESSAGE_NODE).child(chatRoom).child(AppConstants.LAST_MESSAGE)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            RetrieveMessageBean message = dataSnapshot.getValue(RetrieveMessageBean.class);
                            messageBean.setMessageType(message.getMessageType());
                            messageBean.setMessage(message.getMessage());
                            messageBean.setSenderId(message.getSenderId());
                            messageBean.setMessageId(message.getMessageId());
                            messageBean.setMedia(message.getMedia());
                            messageBean.setSeenStatus(message.getSeenStatus());
                            messageBean.setTimeStamp(message.getTimeStamp());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        return messageBean;
    }

}
