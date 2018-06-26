package com.chitchat.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPref {
    private static SharedPreferences pref;
    private static MySharedPref mMyPref;
    private static SharedPreferences.Editor edit;


    MySharedPref(Context context) {
        pref = context.getSharedPreferences(AppConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Method to get Shared Preferences User First Name
     */
    public String getUserFirstName() {
        return pref.getString(AppConstants.SHARED_PREF_FIRST_NAME, "GUEST");
    }

    /**
     * Method to get Shared Preferences User Last Name
     */
    public String getUserLastName() {
        return pref.getString(AppConstants.SHARED_PREF_LAST_NAME, "GUEST");
    }


    /**
     * Method to get Shared Preferences User Id
     */
    public String getUserID() {
        return pref.getString(AppConstants.SHARED_PREF_USER_ID, "GUEST");
    }

    /**
     * Method to get Shared Preferences User Id
     */
    public String getUserPhone() {
        return pref.getString(AppConstants.SHARED_PREF_USER_PHONE, "GUEST");
    }

    /**
     * Method to get references Shared Preferences
     */
    public static MySharedPref getPreference(Context context) {
        if (mMyPref == null)
            mMyPref = new MySharedPref(context);
        return mMyPref;
    }

    /**
     * Method to edit Shared Preferences data
     */
    public void editor(String userFirstName, String userLastName, String userID, String phoneNo) {
        edit = pref.edit();
        edit.putString(AppConstants.SHARED_PREF_FIRST_NAME, userFirstName);
        edit.putString(AppConstants.SHARED_PREF_LAST_NAME, userLastName);
        edit.putString(AppConstants.SHARED_PREF_USER_ID, userID);
        edit.putString(AppConstants.SHARED_PREF_USER_PHONE, phoneNo);
        edit.putBoolean(AppConstants.SHARED_PREF_LOGIN_STATUS, true);
        edit.apply();
    }

    /**
     * Method to edit Shared Preferences phone number
     */
    /*public void addPhone(String phoneNo) {
        edit = pref.edit();
        edit.putString(AppConstants.SHARED_PREF_USER_PHONE, phoneNo);
        edit.apply();
    }*/

    /**
     * Method to clear Shared Preferences on logout
     */
    public void clearPreferences() {
        edit = pref.edit();
        edit.putBoolean(AppConstants.SHARED_PREF_LOGIN_STATUS, false);
        edit.putString(AppConstants.SHARED_PREF_FIRST_NAME, "");
        edit.putString(AppConstants.SHARED_PREF_LAST_NAME, "");
        edit.putString(AppConstants.SHARED_PREF_USER_ID, "");
        edit.putString(AppConstants.SHARED_PREF_USER_PHONE, "");
        edit.apply();
    }


    /**
     * Method to check whether user in logged in or not
     */
    public boolean checkLogin() {
        return pref.getBoolean(AppConstants.SHARED_PREF_LOGIN_STATUS, false);
    }
}
