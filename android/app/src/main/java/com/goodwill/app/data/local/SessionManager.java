package com.goodwill.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.goodwill.app.data.remote.AuthInterceptor;
import com.goodwill.app.data.remote.dto.UserDto;

public class SessionManager implements AuthInterceptor.TokenProvider {

    private static final String PREF_NAME = "goodwill_session";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_LOGGED_IN = "is_logged_in";
    private static final String KEY_UID = "user_uid";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_HOSTEL_BLOCK = "user_hostel_block";
    private static final String KEY_ROOM_NO = "user_room_no";
    private static final String KEY_PHONE = "user_phone";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_VERIFIED = "user_verified";
    private static final String KEY_VERIFIED_VIA_CODE = "user_verified_via_code";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, UserDto user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_LOGGED_IN, true);
        if (user != null) {
            editor.putString(KEY_UID, user.getUid());
            editor.putString(KEY_NAME, user.getName());
            editor.putString(KEY_EMAIL, user.getEmail());
            editor.putString(KEY_HOSTEL_BLOCK, user.getHostelBlock());
            editor.putString(KEY_ROOM_NO, user.getRoomNo());
            editor.putString(KEY_PHONE, user.getPhone());
            editor.putString(KEY_ROLE, user.getRole());
            editor.putBoolean(KEY_VERIFIED, user.isVerified());
            editor.putString(KEY_VERIFIED_VIA_CODE, user.getVerifiedViaCode());
        }
        editor.apply();
    }

    public void setVerified(boolean verified, String code) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_VERIFIED, verified);
        if (code != null) {
            editor.putString(KEY_VERIFIED_VIA_CODE, code);
        }
        editor.apply();
    }

    @Override
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false) && getToken() != null;
    }

    public boolean isVerified() {
        return prefs.getBoolean(KEY_VERIFIED, false);
    }

    public UserDto getUser() {
        if (!isLoggedIn()) {
            return null;
        }
        return new UserDto(
                prefs.getString(KEY_UID, ""),
                prefs.getString(KEY_NAME, ""),
                prefs.getString(KEY_EMAIL, ""),
                prefs.getString(KEY_HOSTEL_BLOCK, ""),
                prefs.getString(KEY_ROOM_NO, ""),
                prefs.getString(KEY_PHONE, ""),
                prefs.getString(KEY_ROLE, "student"),
                prefs.getBoolean(KEY_VERIFIED, false),
                prefs.getString(KEY_VERIFIED_VIA_CODE, "")
        );
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
