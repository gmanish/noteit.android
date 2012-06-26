package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class SplashScreen extends Activity implements OnMethodExecuteListerner {

	SharedPreferences		mPrefs = null;
	View					mContentView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);
		mContentView = findViewById(R.id.splash_root);
		
    	// Read the email id from the preference
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRememberMe 	= mPrefs.getBoolean("Remember_Me", true);
        String 	emailID 		= mPrefs.getString("email", "");
        String 	password 		= mPrefs.getString("password", "");
        
        if (isRememberMe && !emailID.isEmpty() && !password.isEmpty())
        	doLogin(emailID, password);
	}

	private void doLogin(String emailID, String password) {
		
    	try {
        	((NoteItApplication) getApplication()).doInitialize(
				emailID, 
				password,
				true,
				this);
    	} catch (Exception e) {
    		Log.e("doLogin", e.getMessage());
    		AlertDialog dialog = MessageBox.createMessageBox(
        			this, 
        			getString(R.string.login_failed),
        			e.getMessage());
			dialog.show();
    	}
	}

	public void onPostExecute(long resultCode, String message) {
		
		if (resultCode != 0) {
			// Automatic Login failed, show the login screen
			doLoginActivity();
		} else {
			// Login succeeded, proceed
			doPostLoginActivity();
		}
	}
	
	private void doLoginActivity() {
		
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(LoginActivity.DONT_LOGIN, true);
		startActivity(intent);
		finish();
	}
	
	private void doPostLoginActivity() {
		
		boolean startDashboard = mPrefs.getBoolean("Start_Dashboard", true);
		if (startDashboard) {
    		Intent myIntent = new Intent(this, DashBoardActivity.class);
    		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		myIntent.putExtra(LoginActivity.DISPLAY_UNREAD_MESSAGES, 1);
            startActivity(myIntent);
            finish();
		} else {
			final NoteItApplication	app = (NoteItApplication) getApplication();
			app.fetchShoppingLists(true, new NoteItApplication.OnFetchShoppingListsListener() {
				
				public void onPostExecute(
						long resultCode,
						ArrayList<ShoppingList> categories, 
						String message) {
					
					if (resultCode == 0) {
						int 	index = 0;
						long 	lastUsedShoppingListID = mPrefs.getLong("LastUsedShoppingListID", 0);
						
						if (app.getShoppingListCount() > 0 && lastUsedShoppingListID != 0) {
							index = app.getShoppingList().indexOf(
									app.new ShoppingList(lastUsedShoppingListID));
						}
						app.setCurrentShoppingListIndex(index);
		                Intent myIntent = new Intent(SplashScreen.this, ItemListActivity.class);
		        		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		                myIntent.putExtra(LoginActivity.DISPLAY_UNREAD_MESSAGES, 1);
		                startActivity(myIntent);
		                finish();
					} else {
			    		CustomToast.makeText(
			    				getApplicationContext(),
			    				mContentView,
			    				message).show(true);
					}
				}
			});
		}
	}
}
