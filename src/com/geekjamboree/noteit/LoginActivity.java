package com.geekjamboree.noteit;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Preference;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;
import com.geekjamboree.noteit.R;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity 
	extends Activity 
	implements AsyncInvokeURLTask.OnPostExecuteListener {
	
	SharedPreferences		mPrefs;
	boolean					mIsHashedPassword = false;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	
    	if (((NoteItApplication) getApplication()).doesSanityPrevail() == false) {
    		Toast.makeText(this, getString(R.string.app_critical_error), Toast.LENGTH_LONG).show();
    		finish();
    		return;
    	}
    	
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        hideIndeterminateProgress();
   
        // Read the email id from the preference
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRememberMe = mPrefs.getBoolean("Remember_Me", true);

        String emailID = mPrefs.getString("email", "");
        if (!emailID.equals("") && isRememberMe) {
	        EditText editTextEmail = (EditText) findViewById(R.id.editEmailID);
	        editTextEmail.setText(emailID);
        }
        
        String 		password = mPrefs.getString("password", "");
    	EditText 	editPassword = (EditText) findViewById(R.id.editPassword);
        if (!password.equals("") && isRememberMe) {
    		editPassword.setText(password);
    		mIsHashedPassword = true;
        }
        editPassword.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// Perhaps we should ignore non-alpha numeric key presses?
				mIsHashedPassword = false;
				Log.i("editPassword.onKey", "Setting Password Hashed to: " + mIsHashedPassword);
				return false;
			}
		});
        
        Button login = (Button) findViewById(R.id.buttonLogin);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	doLogin();
            }

        });
        
        Button register = (Button) findViewById(R.id.buttonRegister);
        if (register != null) {
        	register.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
				}
			});
        }
        
        if (isRememberMe && !password.equals("") && !emailID.equals("")) {
        	doLogin();
        }
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
					String clearPassword = password.getText().toString();
					String hashedPassword = NoteItApplication.hashString(clearPassword);
					editor.putString("password", hashedPassword);
				} catch (NoSuchAlgorithmException e) {
				}
			} else {
				String hashedPassword = password.getText().toString();
				editor.putString("password", hashedPassword);
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
			long retVal = json.getLong("JSONRetVal");
			if (retVal != 0)
				throw new Exception(json.getString("JSONRetMessage"));
			
			long 					userID = json.getLong("arg1");
			final NoteItApplication app = (NoteItApplication) getApplication();
			
			app.setUserID(userID);
    		if (!json.isNull("arg2")) {
    			Preference prefs = app.new Preference(json.getJSONObject("arg2"));
    			app.setUserPrefs(prefs);
    		}
    		
    		app.doInitialize(new NoteItApplication.OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					
					if (resultCode == 0) {
						boolean startDashboard = mPrefs.getBoolean("Start_Dashboard", true);
						if (startDashboard) {
		            		Intent myIntent = new Intent(LoginActivity.this, DashBoardActivity.class);
		                    startActivity(myIntent);
		                    finish();
						} else {
							boolean	fetchCount = mPrefs.getBoolean("Display_Pending_Item_Count", true);
							app.fetchShoppingLists(fetchCount, new NoteItApplication.OnFetchShoppingListsListener() {
								
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
						                Intent myIntent = new Intent(LoginActivity.this, ItemListActivity.class);
						                startActivity(myIntent);
						                finish();
									} else {
										Toast.makeText(
											getApplicationContext(), 
											message, 
											Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
					} else {
						Toast.makeText(
								getApplicationContext(), 
								message, 
								Toast.LENGTH_SHORT).show();
					}
				}
			});
		} catch (JSONException e) {
			Toast.makeText(
				getApplicationContext(), 
				getString(R.string.server_error), 
				Toast.LENGTH_SHORT).show();
			Log.e("NoteItApplication.loginUser", e.getMessage());
			hideIndeterminateProgress();
		} catch (Exception e) {
    		Toast.makeText(
    			getApplicationContext(), 
    			e.getMessage(), 
    			Toast.LENGTH_SHORT).show();
			Log.e("NoteItApplication.loginUser", e.getMessage());
			hideIndeterminateProgress();
		}
	}
	
	protected void showIndeterminateProgress() {
		LinearLayout progressLayout = (LinearLayout) findViewById(R.id.login_progress);
		if (progressLayout != null) {
			progressLayout.setVisibility(View.VISIBLE);
		}
	}
	
	protected void hideIndeterminateProgress() {
		LinearLayout progressLayout = (LinearLayout) findViewById(R.id.login_progress);
		if (progressLayout != null) {
			progressLayout.setVisibility(View.GONE);
		}
	}
	
	private void doLogin() {
    	showIndeterminateProgress();
    	// Try to authenticate the user with the supplied credentials.
    	// If authentication succeeds, switch to the shopping list view
    	EditText emailID = (EditText) findViewById(R.id.editEmailID);
    	EditText password = (EditText) findViewById(R.id.editPassword);
    	try {
        	((NoteItApplication) getApplication()).loginUser(
				emailID.getText().toString(), 
				password.getText().toString(),
				mIsHashedPassword,
				LoginActivity.this);
    	} catch (Exception e) {
    		hideIndeterminateProgress();
    	}
	}
}
