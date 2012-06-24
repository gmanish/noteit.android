package com.geekjamboree.noteit;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;
import com.geekjamboree.noteit.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoginActivity 
	extends Activity 
	implements OnMethodExecuteListerner {
	
	static final String		DONT_LOGIN = "DONT_LOGIN";
	static final String		DISPLAY_UNREAD_MESSAGES = "DISPLAY_UNREAD_MESSAGES";
	
	SharedPreferences		mPrefs;
	boolean					mIsHashedPassword = false;
	View					mContentView = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
    	super.onCreate(savedInstanceState);
    	
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);
        hideIndeterminateProgress();
   
        mContentView = findViewById(R.id.Login_Root);

    	// Read the email id from the preference
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRememberMe = mPrefs.getBoolean("Remember_Me", true);

        String emailID = mPrefs.getString("email", "");
        if (!emailID.equals("") && isRememberMe) {
	        EditText editTextEmail = (EditText) findViewById(R.id.editEmailID);
	        editTextEmail.setText(emailID);
	        editTextEmail.selectAll();
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
        
        Button forgotPassword = (Button) findViewById(R.id.buttonForgotPassword);
        if (forgotPassword != null) {
        	forgotPassword.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					doForgotPassword();
				}
			});
        }
        
        Intent 	intent = getIntent();
        Bundle 	bundle = intent.getExtras();
        boolean	dontLogin = false;
        if (bundle != null) {
        	dontLogin = bundle.getBoolean(DONT_LOGIN);
        }
        if (!dontLogin && isRememberMe && !password.equals("") && !emailID.equals("")) {
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
			editor.putString("currency", String.valueOf(app.getUserPrefs().mCurrencyId));
		}

    	editor.commit();
		super.onPause();
	}

	public void onPostExecute(long resultCode, String message) {

		try {
			if (resultCode != 0) {
				throw new Exception(message);
			} else {
				boolean startDashboard = mPrefs.getBoolean("Start_Dashboard", true);
				if (startDashboard) {
            		Intent myIntent = new Intent(LoginActivity.this, DashBoardActivity.class);
            		myIntent.putExtra(DISPLAY_UNREAD_MESSAGES, 1);
                    startActivity(myIntent);
                    finish();
				} else {
					boolean					fetchCount = mPrefs.getBoolean("Display_Pending_Item_Count", true);
					final NoteItApplication	app = (NoteItApplication) getApplication();
					
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
				                myIntent.putExtra(DISPLAY_UNREAD_MESSAGES, 1);
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
		} catch (Exception e) {
			hideIndeterminateProgress();
    		CustomToast.makeText(
    				getApplicationContext(),
    				mContentView,
    				e.getMessage()).show(true);
			Log.e("NoteItApplication.loginUser", e.getMessage());
		}
	}
	
    void doForgotPassword() {
		// inflate the view from resource layout
		LayoutInflater	inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View 		dialogView = inflater.inflate(R.layout.dialog_addshoppinglist, null, false);
		final EditText 	editListName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
		
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setView(dialogView)
			.setTitle(getResources().getString(R.string.login_forgot_password_title))
			.create();

		editListName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		dialog.setButton(DialogInterface.BUTTON1, "OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				final String emailID = editListName.getText().toString();

				if (!emailID.trim().matches("")) {
					dialog.dismiss();
				
					NoteItApplication app = (NoteItApplication) getApplication();
					app.do_forgot_password(emailID, new OnMethodExecuteListerner() {
						
						public void onPostExecute(long resultCode, String message) {
							
							if (resultCode != 0) {
								CustomToast.makeText(
										LoginActivity.this,
										mContentView,
										message).show(true);
							} else {
								CustomToast.makeText(
										LoginActivity.this, 
										mContentView, 
										String.format(
												getResources().getString(R.string.login_forgot_password_success), 
												emailID)).show(true);
							}
						}
					});
				}
			}
		});
	
		dialog.setButton(DialogInterface.BUTTON2, "Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Log.e("AddShoppingList", "Cancel");
				dialog.dismiss();
			}
		});

		dialog.show();    
	}
    
    protected void showIndeterminateProgress() {
    	RelativeLayout progressPanel = (RelativeLayout) findViewById(R.id.login_bottom_root);
		if (progressPanel != null) {
			progressPanel.setVisibility(View.VISIBLE);
		}
	}
	
	protected void hideIndeterminateProgress() {
		RelativeLayout progressPanel = (RelativeLayout) findViewById(R.id.login_bottom_root);
		if (progressPanel != null) {
			progressPanel.setVisibility(View.GONE);
		}
	}
	
	private void doLogin() {
    	EditText emailID = (EditText) findViewById(R.id.editEmailID);
    	EditText password = (EditText) findViewById(R.id.editPassword);
    	String strEmail = emailID.getEditableText().toString().trim();
    	String strPassword = password.getEditableText().toString();
    	
    	try {
        	showIndeterminateProgress();

        	((NoteItApplication) getApplication()).doInitialize(
				strEmail, 
				strPassword,
				mIsHashedPassword,
				this);
    	} catch (Exception e) {
    		hideIndeterminateProgress();    		
    		Log.e("doLogin", e.getMessage());
    		AlertDialog dialog = MessageBox.createMessageBox(
        			this, 
        			getString(R.string.login_failed),
        			e.getMessage());
			dialog.show();
    	}
	}
}
