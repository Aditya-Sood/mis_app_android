package com.android.mis.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.util.ArraySet;

import com.android.mis.javac.Login.LoginActivity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SessionManagement {
    SharedPreferences pref;
	
	// Editor for Shared preferences
	Editor editor;
	
	// Context
	Context _context;
	
	// Shared pref mode
	int PRIVATE_MODE = 0;

	// Sharedpref file name
	private static final String PREF_NAME = "MISPref";

	// All Shared Preferences Keys
	private static final String IS_LOGIN = "IsLoggedIn";
	
	// User name (make variable public to access from outside)
	public static final String KEY_TOKEN = "access_token";

	// User name
	public static final String KEY_NAME = "user_name";

	// User emails
	public static final String KEY_EMAIL = "user_email";

	// User picPath
	public static final String KEY_PIC_PATH = "user_profile_pic_path";

	// App version
	public static final String KEY_APP_VERSION = "app_version";

	// cache request keys
	public static  final String KEY_CACHE_REQ_KEYS = "cache_keys";


	// Constructor
	public SessionManagement(Context context){
		this._context = context;
		pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		editor = pref.edit();
	}
	
	/**
	 * Create login session
	 * */
	public void createLoginSession(String token,String name,String email,String pic_path){
		// Storing login value as TRUE
		editor.putBoolean(IS_LOGIN, true);

		// Storing the token
		editor.putString(KEY_TOKEN,token);

		// Storing the name
		editor.putString(KEY_NAME,name);

		// Storing the email
		editor.putString(KEY_EMAIL,email);

		// Storing the pic path
		editor.putString(KEY_PIC_PATH,pic_path);

		Set<String> cached_keys = new ArraySet<>();
		editor.putStringSet(KEY_CACHE_REQ_KEYS,cached_keys);
		// commit changes
		editor.commit();
	}

	public void insertCachedKeys(String key){
		Set<String> cached_keys = pref.getStringSet(KEY_CACHE_REQ_KEYS,new ArraySet<String>());
		cached_keys.add(key);
		editor.putStringSet(KEY_CACHE_REQ_KEYS,cached_keys);
		editor.commit();
	}
	
	/**
	 * Check login method will check user login status
	 * If false it will redirect user to login page
	 * Else won't do anything
	 * */
	public void checkLogin(){
		// Check login status
		if(!this.isLoggedIn()) {
			// user is not logged in redirect him to Login Activity
			Intent i = new Intent(_context, LoginActivity.class);
			// Closing all the Activities
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			// Add new Flag to start new Activity
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// Staring Login Activity
			_context.startActivity(i);
		}
	}

	/**
	 * Get stored session data
	 * */
	public HashMap<String, String> getTokenDetails(){
		HashMap<String, String> user = new HashMap<String, String>();
		// user name
		user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));

        // return user
		return user;
	}

    public HashMap<String,String> getSessionDetails(){
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_TOKEN, pref.getString(KEY_TOKEN, null));
        user.put(KEY_NAME,pref.getString(KEY_NAME,null));
        user.put(KEY_PIC_PATH,pref.getString(KEY_PIC_PATH,null));
        user.put(KEY_EMAIL,pref.getString(KEY_EMAIL,null));
        // return user
        return user;
    }

	/**
	 * Clear session details
	 * */
	public void logoutUser(){
		//Set<String> cached_keys = pref.getStringSet(KEY_CACHE_REQ_KEYS,new ArraySet<String>());

		// Clearing all data from Shared Preferences
		editor.clear();
		editor.commit();
		
		// After logout redirect user to Loing Activity
		Intent i = new Intent(_context, LoginActivity.class);
		// Closing all the Activities
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		// Add new Flag to start new Activity
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		// Staring Login Activity
		_context.startActivity(i);
	}

	/**
	 * Quick check for login
	 * **/
	// Get Login State
	public boolean isLoggedIn(){
		return pref.getBoolean(IS_LOGIN, false);
	}

}
