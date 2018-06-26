package com.chitchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.chitchat.R;
import com.chitchat.Utilities.AppUtils;
import com.chitchat.Utilities.FireBaseUtil;
import com.chitchat.Utilities.MySharedPref;
import com.chitchat.beans.UserBean;
import com.chitchat.interfaces.Loader;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, Loader {
    private Spinner spCountry;
    private EditText etNumber;
    private CallbackManager mCallbackManager;
    private com.facebook.login.LoginManager mFbLoginManager;
    private UserBean mUserBean;
    private ProgressBar pbLoader;
    private Button btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_verify_number);
        initViews();
        initVariables();
    }

    /**
     * Method ot initialise views
     */
    private void initViews() {
        mUserBean = new UserBean();
        TextView tvTitle = findViewById(R.id.tv_verify_title);
        etNumber = findViewById(R.id.et_number);
        spCountry = findViewById(R.id.sp_country);
        Spannable colorSpan = new SpannableString(getResources().getString(R.string.verify_number_title));
        colorSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.app_color)), 11, 20, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        tvTitle.setText(colorSpan);
        Spannable policySpan = new SpannableString(getResources().getString(R.string.terms_privacy_policy));
        policySpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.app_color)), 40, 57, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        policySpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.app_color)), 62, 76, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        TextView tvPolicies = findViewById(R.id.tv_privacy_policy);
        tvPolicies.setText(policySpan);
        btnVerify = findViewById(R.id.b_verify);
        btnVerify.setOnClickListener(this);
        findViewById(R.id.b_facebook_login).setOnClickListener(this);
        mFbLoginManager = com.facebook.login.LoginManager.getInstance();
        registerCallBackForFaceBook();
        pbLoader = findViewById(R.id.pb_loader);
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

    /**
     * Method to initialise Variables
     */
    private void initVariables() {
        ArrayAdapter<CharSequence> countryList = ArrayAdapter.createFromResource(this, R.array.country, android.R.layout.simple_spinner_item);
        spCountry.setAdapter(countryList);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_verify:
                hideKeyPad();
                if (AppUtils.checkConnection(this)) {
                    if (TextUtils.isEmpty(etNumber.getText()))
                        AppUtils.showSnackBar(findViewById(R.id.ll_sign_up), getResources().getString(R.string.enter_number));
                    else {
                        if (AppUtils.validateNumber(etNumber.getText().toString().trim())) {
                            showLoader();
                            FireBaseUtil.getInstance().VerifyNumber(SignUpActivity.this, etNumber.getText().toString());
                            //FireBaseDatabaseUtil.getDatabaseInstance().isUserExists(etNumber.getText().toString(), this);
                        } else
                            etNumber.setError(getResources().getString(R.string.txt_invalid));
                    }
                } else {
                    AppUtils.showToast(this, getResources().getString(R.string.no_connection));
                }
                break;
            case R.id.b_facebook_login:
                mFbLoginManager.logOut();
                mFbLoginManager.logInWithReadPermissions(SignUpActivity.this, Arrays.asList("email", "public_profile", "user_birthday"));
                break;
        }
    }

    /**
     * Method to get callback of anonymous Sign in
     */
    public void isVerified(boolean verified) {
        if (verified) {
            AppUtils.showToast(this, getResources().getString(R.string.number_verified));
            Intent intent = new Intent(this, UserDetailActivity.class);
            intent.putExtra(getResources().getString(R.string.phone_num), etNumber.getText().toString());
            if (mUserBean.getName() != null) {
                intent.putExtra(getResources().getString(R.string.first_name), mUserBean.getName());
                intent.putExtra(getResources().getString(R.string.last_name), mUserBean.getLastName());
                intent.putExtra(getResources().getString(R.string.profile_pic), mUserBean.getProfilePic());
                intent.putExtra(getResources().getString(R.string.isFbLogin), true);
            }
            hideLoader();
            startActivity(intent);
            etNumber.setText("");
        } else {
            AppUtils.showToast(this, getResources().getString(R.string.number_not_verified));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Method to Register CallBack for LoginManager for Result
     */
    private void registerCallBackForFaceBook() {
        mFbLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                if (Profile.getCurrentProfile() != null) {
                                    mUserBean.setName(Profile.getCurrentProfile().getFirstName());
                                    mUserBean.setLastName(Profile.getCurrentProfile().getLastName());
                                    mUserBean.setProfilePic(Profile.getCurrentProfile().getProfilePictureUri(500, 500).toString());
                                }
                            }
                        });
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                AppUtils.showToast(SignUpActivity.this, getResources().getString(R.string.txt_cancel));
            }

            @Override
            public void onError(FacebookException error) {
                AppUtils.showToast(SignUpActivity.this, getResources().getString(R.string.txt_error));
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * Method to take callback whether user exists or not
     */
    public void setIntent(UserBean user, boolean status) {
        if (status) {
            AppUtils.showToast(this, getResources().getString(R.string.user_already_exists));
            MySharedPref.getPreference(this).editor(user.getName(), user.getLastName(), user.getuId(), etNumber.getText().toString());
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(getResources().getString(R.string.phone_num), etNumber.getText().toString());
            etNumber.setText("");
            hideLoader();
            startActivity(intent);
        }
    }

    @Override
    public void showLoader() {
        btnVerify.setVisibility(View.GONE);
        pbLoader.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        btnVerify.setVisibility(View.VISIBLE);
        pbLoader.setVisibility(View.GONE);
    }

}
