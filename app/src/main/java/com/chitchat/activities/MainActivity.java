package com.chitchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.chitchat.R;
import com.chitchat.Utilities.MySharedPref;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        decideIntent();
    }

    /**
     * Method to Decide on which Activity should intent
     */
    private void decideIntent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MySharedPref.getPreference(getApplicationContext()).checkLogin()) {
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(MainActivity.this, SignUpActivity.class));
                    finish();
                }
            }
        }, 2000);

    }
}
