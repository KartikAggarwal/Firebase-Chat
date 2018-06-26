package com.chitchat.Utilities;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.chitchat.activities.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class FireBaseUtil {
    private FirebaseAuth mAuth;
    private static FireBaseUtil mFireBaseUtil;


    private FireBaseUtil() {
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Method to get single object of FireBaseUtil class
     */
    public static FireBaseUtil getInstance() {
        if (mFireBaseUtil == null)
            mFireBaseUtil = new FireBaseUtil();
        return mFireBaseUtil;
    }

    /**
     * Method to Verify Number
     */
    public void VerifyNumber(final Activity activity, final String phone) {
        mAuth.signInAnonymously().addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FireBaseDatabaseUtil.getDatabaseInstance().isUserExists(phone,activity);
            }
        });
    }

    /**
     * Method to SignOut current user
     */
    public void signOut()
    {
        mAuth.signOut();
    }

    /*public void CurrentUserDetail() {
        UserBean currentUser = new UserBean();
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().isAnonymous())
                Log.d("Name", "uuf7uyf" + mAuth.getCurrentUser().getPhoneNumber());
        }
    }*/
}
