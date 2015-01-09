package com.meg7.soas.accounts;

import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.meg7.soas.R;
import com.meg7.soas.ui.PhotosListActivity;

/**
 * Created by laaptu on 1/7/15.
 */
public class AuthActivity extends AccountAuthenticatorActivity {

    public static final String SHARED_PREFS_NAME = "SoasPrefs";
    private SoasAccountManager soasAccountManager;
    private boolean calledFromAddAccount = false;
    public static final String CALL_FROM_ADD_ACCOUNT = "callFromAddButton";

    private EditText txtName, txtEmail, txtPassword, txtUname;


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        soasAccountManager = new SoasAccountManager(AccountManager.get(this));
        /**
         * Just a simple variable to know whether this
         * activity is called from AccountViewActivity or Fragment
         * so that we won't navigate to HomeDisplay after login i.e
         * we just simply finish this activity*/
        calledFromAddAccount = getIntent().hasExtra(CALL_FROM_ADD_ACCOUNT);
        if (isUserLoggedIn(this)) {
            showSuccessLogin();
            return;
        }

        setContentView(R.layout.activity_auth);
        initViews();
    }

    private void initViews() {
        txtName = (EditText) findViewById(R.id.txt_name);
        txtEmail = (EditText) findViewById(R.id.txt_email);
        txtUname = (EditText) findViewById(R.id.txt_uname);
        txtPassword = (EditText) findViewById(R.id.txt_pwd);

    }

    /**
     * Method which validates and submits
     * the inputs from Sign In or Sign Up form
     */
    public void validateAndSubmit(View view) {
        if (isTextEmpty(txtName.getText().toString())) {
            setError(txtName, "Name can't be empty");
            return;
        }

        if (isTextEmpty(txtUname.getText().toString())) {
            setError(txtUname, "Uname can't be empty");
            return;
        }

        if (isTextEmpty(txtPassword.getText().toString())) {
            setError(txtPassword, "Password can't be empty");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(txtEmail.getText().toString()).matches()) {
            setError(txtEmail, "Add valid email");
            return;
        }

        /**
         * Here you call your server
         * make authentication and receive authentication token
         * or any other form of server calls
         * for user authentication
         * Here We are just assuming that server calls are made and
         * we have received valid auth token and key
         */

        final String authKey = "bad18eba1ff45jk7858b8ae88a77fa30";
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                authenticationSuccess(authKey);

            }
        });
    }


    /**
     * This method is called when there is login or sign up
     * success and user has the necessary authToken
     * which needs to be stored
     */
    private void authenticationSuccess(String authToken) {
        User user = new User();
        user.uname = txtUname.getText().toString();
        user.name = txtName.getText().toString();
        user.email = txtEmail.getText().toString();
        user.authToken = authToken;

        /**
         * We are now creating an account
         * on Android Account Manager
         */
        soasAccountManager.addAccount(user);

        /**
         * This method is crucials as it will
         * notify the calling intent that
         * account creation has been successful
         * and passes AUTHTOKEN on Bundle which can
         * be recieved by calling Intent*/
        Bundle callbackResult = new Bundle();
        callbackResult.putString(AccountManager.KEY_ACCOUNT_NAME, user.email);
        callbackResult.putString(AccountManager.KEY_ACCOUNT_TYPE, SoasAccountManager.ACCOUNT_TYPE);
        callbackResult.putString(AccountManager.KEY_AUTHTOKEN, authToken);
        setAccountAuthenticatorResult(callbackResult);

        showSuccessLogin();

    }

    private void setError(EditText txt, String errorMsg) {
        txt.requestFocus();
        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();

    }

    private void showSuccessLogin() {
        if (!calledFromAddAccount) {
            Intent intent = new Intent(this, PhotosListActivity.class);
            startActivity(intent);
        }
        Toast.makeText(this, "You are logged in", Toast.LENGTH_LONG).show();
        this.finish();
    }


    /**
     * We normally store info on SharedPreferences
     * and on app update, there is chance that your shared preferences
     * get cleared. So when the shared preferences get cleared
     * user has to login again. It is a hassle. So, here first we check
     * on shared preferences, for stored data.
     * If not found on shared preferences, we check Account under
     * AccountManager and if there isn't any stored account , then
     * we are sure that user hasn't logged in.
     */
    public static boolean isUserLoggedIn(Context context) {
        if (isUserInfoStoredOnSharedPreferences(context)) {
            return true;
        }
        User user = new SoasAccountManager(AccountManager.get(context)).getCurrentUser();
        if (user != null) {
            saveUserInfoToSharedPreferences(user, context);
            return true;
        }
        return false;

    }


    public static boolean isTextEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }

    /**
     * ####### ALL SHARED PREFERENCES RELATED ACTION ##############
     * you can declare a separate class and do the necessary action*
     */
    public static void saveUserInfoToSharedPreferences(User user, Context context) {
        if (user == null)
            return;
        SharedPreferences mPref = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(SoasAccountManager.USER_EMAIL, user.email);
        editor.putString(SoasAccountManager.USER_UNAME, user.uname);
        editor.putString(SoasAccountManager.USER_NAME, user.name);
        editor.putString(SoasAccountManager.USER_AUTH, user.authToken);
        editor.commit();
    }


    public static boolean isUserInfoStoredOnSharedPreferences(Context context) {
        /**Here just checking the email, you all can
         * implement your own logic to find whether user is logged in
         * or not, by checking out the shared preferences stored value*/
        SharedPreferences mPref = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return !TextUtils.isEmpty(mPref.getString(SoasAccountManager.USER_EMAIL, null));
    }

    /**
     * Method to clear all the  saved Shared Preferences Value
     */
    public static boolean clear(Context context) {
        SharedPreferences mPref = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        return mPref.edit()
                .clear()
                .commit();
    }
}
