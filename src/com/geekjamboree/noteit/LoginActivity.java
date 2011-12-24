package com.geekjamboree.noteit;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.OnFetchCategoriesListener;
import com.geekjamboree.noteit.NoteItApplication.Preference;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;
import com.geekjamboree.noteit.NoteItApplication.Unit;
import com.geekjamboree.noteit.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity 
	extends Activity 
	implements AsyncInvokeURLTask.OnPostExecuteListener {
	
	ProgressDialog		mProgressDialog = null;
	SharedPreferences	mPrefs;
	boolean				mIsHashedPassword = false;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.login);
        toolbar.SetTitle(getResources().getText(R.string.login_activity_title));
   
        // Read the email id from the preference
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRememberMe = mPrefs.getBoolean("Remember_Me", true);

        String emailID = mPrefs.getString("email", "");
        if (emailID != "" && isRememberMe) {
	        EditText editTextEmail = (EditText) findViewById(R.id.editEmailID);
	        editTextEmail.setText(emailID);
        }
        
        String 		password = mPrefs.getString("password", "");
    	EditText 	editPassword = (EditText) findViewById(R.id.editPassword);
        if (password != "" && isRememberMe) {
    		editPassword.setText(password);
    		mIsHashedPassword = true;
        }
        editPassword.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// Perhaps we should ignore non-alpha numeric key presses?
				mIsHashedPassword = false;
				return false;
			}
		});
        
        Button next = (Button) findViewById(R.id.buttonLogin);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	// show a progress dialog
                mProgressDialog = ProgressDialog.show(
                		LoginActivity.this, 
                		"", 
                		getResources().getString(R.string.Login_Authenticating_message), 
                		true);

            	// Try to authenticate the user with the supplied credentials.
            	// If authentication succeedes, switch to the shopping list view
            	EditText emailID = (EditText) findViewById(R.id.editEmailID);
            	EditText password = (EditText) findViewById(R.id.editPassword); 
            	((NoteItApplication) getApplication()).loginUser(
    				emailID.getText().toString(), 
    				password.getText().toString(),
    				mIsHashedPassword,
    				LoginActivity.this);
            }

        });
/*        
        android.telephony.TelephonyManager telephonyManager = (android.telephony.TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String nativeCountry = telephonyManager.getSimCountryIso();
        Locale india = new Locale("en", "in");
        Log.i("LoginActivity.onCreate", "Native Currency Code:" + Currency.getInstance(india).getCurrencyCode());
        Log.i("LoginActivity.onCreate", "Native Currency Symbol: " + Currency.getInstance(india).getSymbol());
        Currency currency = Currency.getInstance(india);
*/
    }
    
	@Override
	protected void onPause() {
        boolean isRememberMe = mPrefs.getBoolean("Remember_Me", true);
		SharedPreferences.Editor editor = mPrefs.edit();
    	
		EditText emailID = (EditText) findViewById(R.id.editEmailID);
    	if (emailID != null && isRememberMe) {
    		String email = emailID.getText().toString();
    		editor.putString("email", email);
    	} else
    		editor.remove("email");
    	
    	EditText password = (EditText) findViewById(R.id.editPassword);
    	if (password != null && isRememberMe) {
			if (!mIsHashedPassword) {
				try {
					editor.putString("password", NoteItApplication.hashString(password.getText().toString()));
				} catch (NoSuchAlgorithmException e) {
				}
			} else {
				editor.putString("password", password.getText().toString());
			}
    	} else 
    		editor.remove("password");
    	
		NoteItApplication app = (NoteItApplication) getApplication();
    	if (app.getUserPrefs() != null) {
			editor.putString("currency", app.getUserPrefs().mCurrencyCode);
		}

    	editor.commit();
		super.onPause();
	}

	public void onPostExecute(JSONObject json) {
		try {
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				Log.i("LoginActivity.onPostExecute", "Destroying Progressbar");
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			if (json.has("JSONRetVal")) {
				// The server is up and running 
	        	long retval = json.getLong("JSONRetVal");
	        	if (retval == 0){
		        	// We're set to rock and roll
	            	if (!json.isNull("arg1")) {
	            		
						long 					userID = json.getLong("arg1");
						final NoteItApplication app = (NoteItApplication) getApplication();
						
						app.setUserID(userID);
	            		Toast.makeText(this, "You are logged in.", Toast.LENGTH_SHORT).show();
	            		
	            		// Read the user preferences
	            		if (!json.isNull("arg2")) {
	            			Preference prefs = app.new Preference(json.getJSONObject("arg2"));
	            			app.setUserPrefs(prefs);
	            		}
	            		
	            		app.fetchUnits(Unit.METRIC, new NoteItApplication.OnMethodExecuteListerner() {
	            			
	            			public void onPostExecute(long resultCode, String message) {
	    	            		
	            				if (resultCode == 0) {
		            				// Fetch the categories. This is a one time activity
		    	            		app.fetchCategories(new OnFetchCategoriesListener() {
		    							
		    							public void onPostExecute(long resultCode, ArrayList<Category> categories,
		    									String message) {
	
		    								if (resultCode == 0) {
		    									final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
		    									boolean 				startDashboard = prefs.getBoolean("Start_Dashboard", true);
		    									
		    									if (startDashboard) {
		    										//Intent myIntent = new Intent(this, ShoppingListActivity.class);
		    					            		Intent myIntent = new Intent(LoginActivity.this, DashBoardActivity.class);
		    					                    startActivity(myIntent);
		    					                    finish();
		    									} else {
		    										final NoteItApplication app = (NoteItApplication) getApplication();
		    										boolean					fetchCount = prefs.getBoolean("Display_Pending_Item_Count", true);
		    										app.fetchShoppingLists(fetchCount, new NoteItApplication.OnFetchShoppingListsListener() {
		    											
		    											public void onPostExecute(long resultCode,
		    													ArrayList<ShoppingList> categories, 
		    													String message) {
		    												
		    												if (resultCode == 0) {
		    			  										
		    													long lastUsedShoppingListID = prefs.getLong("LastUsedShoppingListID", 0);
		    													if (app.getShoppingListCount() > 0 && lastUsedShoppingListID != 0) {
		    														int index = app.getShoppingList().indexOf((app.new ShoppingList(lastUsedShoppingListID)));
		    														if (index >= 0)
		    															app.setCurrentShoppingListIndex(index);
		    														else
		    															app.setCurrentShoppingListIndex(0);
		    													} else if (app.getShoppingListCount() > 0) {
		    														app.setCurrentShoppingListIndex(0);
		    													}
		    									                Intent myIntent = new Intent(LoginActivity.this, ItemListActivity.class);
		    									                startActivity(myIntent);
		    									                finish();
		    												} else
		    													Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
		    											}
		    										});
		    									}
		    								} else {
		    				            		Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
		    								}
		    							}
		    						});
								} else {
				            		Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
								}
	    	            	}
	            		});
	            	} else
	            		throw new Exception("Invalid email or password");
	        	} else 
	        		throw new Exception(json.getString("JSONRetMessage"));
			} else
				throw new Exception(getResources().getString(R.string.server_error));
		} catch (JSONException e) {
			Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
			Log.e("NoteItApplication.loginUser", e.getMessage());
		} catch (Exception e) {
    		Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.e("NoteItApplication.loginUser", e.getMessage());
		}
		
	}
}
