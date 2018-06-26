package com.chitchat.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;
import com.chitchat.R;

public class AppUtils {

    /**
     * Method to Show Snackbar
     */
    public static void showSnackBar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Method to Show Toast
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to check connection
     */
    public static boolean checkConnection(Context context) {
        boolean mob_data = false, wifi_test = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.getTypeName().equalsIgnoreCase("WIFI"))
                if (info.isConnected())
                    wifi_test = true;

            if (info.getTypeName().equalsIgnoreCase("MOBILE"))
                if (info.isConnected())
                    mob_data = true;
        }
        return (mob_data || wifi_test);
    }

    /**
     * Method to make location preview
     */
    public static String makeLocationPreview(double latitude, double longitude, int zoomLevel, Context context) {
        return "https://maps.googleapis.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=" + zoomLevel + "&size=200x200&markers=color:red%7C" + latitude + "," + longitude + "&key=" + context.getResources().getString(R.string.API_KEY);
    }

    /**
     * Method to Validate Number
     */
    public static boolean validateNumber(String number) {
        if (number != null) {
            if (number.length() == 10)
                if (number.startsWith("9") || number.startsWith("8") || number.startsWith("7"))
                    return true;
                else
                    return false;
            else
                return false;
        } else {
            return false;
        }
    }


    /**
     * Method to filter contact number
     */
    public static String formatNumber(String number) {
        if (number.contains(" "))
            number = number.replaceAll(" ", "");
        if (number.contains("-"))
            number = number.replaceAll("-", "");
        if (number.length() == 13)
            number = number.substring(3);
        if (number.length() == 11)
            number = number.substring(1);
        if (number.contains("("))
            number = number.replaceAll("\\(", "");
        if (number.contains(")"))
            number = number.replaceAll("\\)", "");
        return number;
    }

}
