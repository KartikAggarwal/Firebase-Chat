package com.chitchat.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.AppConstants;
import com.chitchat.Utilities.AppUtils;
import com.chitchat.Utilities.FireBaseDatabaseUtil;
import com.chitchat.Utilities.MySharedPref;
import com.chitchat.interfaces.Loader;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

public class UserDetailActivity extends AppCompatActivity implements View.OnClickListener, Loader, SimpleDraweeView.OnLongClickListener {
    private EditText etFirstName, etLastName;
    private Uri mProfilePic;
    private String mPhone;
    private SimpleDraweeView sdvProfile;
    private ProgressBar pbLoader;
    private boolean isFbLogin;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_user_detail);
        initViews();
    }

    /**
     * Method to Initialise Views
     */
    private void initViews() {
        Toolbar tbToolbar = findViewById(R.id.tb_toolbar);
        setSupportActionBar(tbToolbar);
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(getResources().getString(R.string.sign_up_title));
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        sdvProfile = findViewById(R.id.sdv_profile_pic);
        pbLoader = findViewById(R.id.pb_loader_user_details);
        findViewById(R.id.iv_edit_profile_pic).setOnClickListener(this);

        btnDone = findViewById(R.id.b_done);

        if (getIntent() != null) {
            mPhone = getIntent().getStringExtra(getResources().getString(R.string.phone_num));
            if (getIntent().getStringExtra(getResources().getString(R.string.first_name)) != null) {
                mProfilePic = Uri.parse(getIntent().getStringExtra(getResources().getString(R.string.profile_pic)));
                sdvProfile.setImageURI(getIntent().getStringExtra(getResources().getString(R.string.profile_pic)));
                etFirstName.setText(getIntent().getStringExtra(getResources().getString(R.string.first_name)));
                etLastName.setText(getIntent().getStringExtra(getResources().getString(R.string.last_name)));
                isFbLogin = getIntent().getBooleanExtra(getResources().getString(R.string.isFbLogin), false);
            }
        }
        btnDone.setOnClickListener(this);
    }

    /**
     * Method to Hide Keypad on View Click
     */
    private void hideKeyPad() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_done:
                hideKeyPad();
                if (TextUtils.isEmpty(etFirstName.getText()) && TextUtils.isEmpty(etLastName.getText())) {
                    AppUtils.showToast(this, getResources().getString(R.string.enter_name));
                } else if (mProfilePic != null) {
                    showLoader();
                    if (isFbLogin)
                        FireBaseDatabaseUtil.getDatabaseInstance().createUser(etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(), mProfilePic.toString(), mPhone, this);
                    else
                        FireBaseDatabaseUtil.getDatabaseInstance().UploadProfilePic(mProfilePic, etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(), this);

                } else {
                    showLoader();
                    FireBaseDatabaseUtil.getDatabaseInstance().createUser(etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(), null, mPhone, this);
                }
                break;

            case R.id.iv_edit_profile_pic:
                requestReadPermission();
                break;
        }
    }

    /**
     * Method to open Gallery
     */
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, AppConstants.GALLERY_REQUEST);
    }

    /**
     * Method to request permission for camera
     */
    private void requestReadPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, AppConstants.REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConstants.REQUEST_READ_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openGallery();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstants.GALLERY_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                mProfilePic = data.getData();
                sdvProfile.setImageURI(data.getData());
            }
        }
    }

    /**
     * Method to upload User details
     */
    public void uploadDetails(String profilePic) {
        FireBaseDatabaseUtil.getDatabaseInstance().createUser(etFirstName.getText().toString(), etLastName.getText().toString(), profilePic, mPhone, this);
    }

    @Override
    public void showLoader() {
        btnDone.setVisibility(View.GONE);
        pbLoader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        btnDone.setVisibility(View.VISIBLE);
        pbLoader.setVisibility(View.GONE);
    }

    /**
     * Method to get Callback after registering user
     */
    public void userRegistered(String uId) {
        hideLoader();
        MySharedPref.getPreference(this).editor(etFirstName.getText().toString().trim(), etLastName.getText().toString().trim(), uId, mPhone);
        etFirstName.setText("");
        etLastName.setText("");
        AppUtils.showToast(this, getResources().getString(R.string.user_registered));
        Intent intent = new Intent(this, HomeActivity.class);
        if (mPhone != null)
            intent.putExtra(getResources().getString(R.string.phone_num), mPhone);
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View view) {
        sdvProfile.setImageURI("");
        return true;
    }
}
