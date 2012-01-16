package com.geekjamboree.noteit;

import java.security.NoSuchAlgorithmException;

import com.geekjamboree.noteit.Eula.OnEulaAgreedTo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnEulaAgreedTo {

    private static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
    private static final String PREFERENCES_EULA = "eula";

    CustomTitlebarWrapper 		mToolbar;
    boolean						mEulaAccepted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		mToolbar = new CustomTitlebarWrapper(this);
		setContentView(R.layout.register);
		mToolbar.SetTitle(getString(R.string.register));
		
        final SharedPreferences 	prefs = getSharedPreferences(PREFERENCES_EULA, Activity.MODE_PRIVATE);
        SharedPreferences.Editor 	editor = prefs.edit();
        // We'll force the user to accept the Eula every time they come to "Registration"
        editor.putBoolean(PREFERENCE_EULA_ACCEPTED, false);
        editor.commit();
        
		mEulaAccepted = prefs.getBoolean(PREFERENCE_EULA_ACCEPTED, false);
		Button eula = (Button) findViewById(R.id.buttonEula);
		if (eula != null) {
			eula.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Eula.show(RegisterActivity.this);
				}
			});
		}
		
		Button buttonRegister = (Button) findViewById(R.id.buttonRegister);
		if (buttonRegister != null) {
			buttonRegister.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					
					EditText 		editFirstName 		= (EditText) findViewById(R.id.editFirstName);
					EditText 		editLastName 		= (EditText) findViewById(R.id.editLastName);
					EditText 		editEmail 			= (EditText) findViewById(R.id.editEmail);
					EditText 		editPassword 		= (EditText) findViewById(R.id.editPassword);
					EditText 		editConfirmPassword = (EditText) findViewById(R.id.editConfirmPassword);
					final String 	email				= editEmail.getEditableText().toString();
					final String 	password 			= editPassword.getEditableText().toString();
					final String 	confirmPassword 	= editConfirmPassword.getEditableText().toString();

					if (!mEulaAccepted) {
						AlertDialog dialog = MessageBox.createMessageBox(
							RegisterActivity.this, 
							getString(R.string.app_name), 
							getString(R.string.eula_agree));
						dialog.show();
						return;
					}
					
					if (email.trim().equals("")) {
						
						AlertDialog dialog = MessageBox.createMessageBox(
							RegisterActivity.this, 
							getString(R.string.register_error_title), 
							getString(R.string.register_blank_email));

						dialog.show();
						return;
					} else if (password.trim().equals("") || confirmPassword.trim().equals("")) {
						
						AlertDialog dialog = MessageBox.createMessageBox(
							RegisterActivity.this, 
							getString(R.string.register_error_title), 
							getString(R.string.register_blank_password));

						dialog.show();
						return;
					} else if (!password.trim().equals(confirmPassword.trim())) {
						
						AlertDialog dialog = MessageBox.createMessageBox(
								RegisterActivity.this, 
								getString(R.string.register_error_title), 
								getString(R.string.register_password_not_match));
						
						dialog.show();
						return;
					} else if (password.length() < LoginActivity.MIN_PASSWORD_LENGTH) {
			    		AlertDialog dialog = MessageBox.createMessageBox(
			        			RegisterActivity.this, 
			        			getString(R.string.register_error_title), 
			        			getString(R.string.login_password_tooshort));
			    		dialog.show();
			    		return;
		        	}

					
					NoteItApplication app = (NoteItApplication) getApplication();
					if (app != null) {
						showIndeterminateProgress();
						app.registerUser(
							editFirstName.getEditableText().toString(), 
							editLastName.getEditableText().toString(), 
							editEmail.getEditableText().toString(), 
							editPassword.getEditableText().toString(), 
							new NoteItApplication.OnMethodExecuteListerner() {
								
								public void onPostExecute(long resultCode, String message) {
									try {
										if (resultCode == 0) {
											doSaveLoginDetails(email, password);
											startActivity(
												new Intent(RegisterActivity.this, LoginActivity.class));
											finish();
										} else {
											Toast.makeText(
												RegisterActivity.this, 
												message, 
												Toast.LENGTH_LONG).show();
										}
									} finally {
										hideIndeterminateProgress();
									}
								}
							});
					}
				}
			});
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
	
	protected void doSaveLoginDetails(String email, String password) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = prefs.edit();
		String hashedPassword = "";
		
		try {
			hashedPassword = NoteItApplication.hashString(password);
		} catch (NoSuchAlgorithmException e) {
		}

   		editor.putString("email", email);
		editor.putString("password", hashedPassword);
		editor.putBoolean("Remember_Me", true);
    	editor.commit();
	}
	
    public void onEulaAgreedTo() {
    	mEulaAccepted = true;
    }
}
