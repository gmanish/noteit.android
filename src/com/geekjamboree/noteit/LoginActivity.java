package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.OnFetchCategoriesListener;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity 
	extends Activity 
	implements AsyncInvokeURLTask.OnPostExecuteListener {
	ProgressDialog		mProgressDialog = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.login);
        toolbar.SetTitle(getResources().getText(R.string.login_activity_title));
   
        // Read the email id from the preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String emailID = prefs.getString("email", "");
        if (emailID != "") {
	        EditText editTextEmail = (EditText)findViewById(R.id.editEmailID);
	        editTextEmail.setText(emailID);
        }
        
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
            	EditText emailID = (EditText)findViewById(R.id.editEmailID);
            	((NoteItApplication)getApplication()).loginUser(
    				emailID.getText().toString(), 
    				LoginActivity.this);
            }

        });
    }
    
	public void onPostExecute(JSONObject json) {
		try {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			if (json.has("JSONRetVal")) {
				// The server is up and running 
	        	long retval = json.getLong("JSONRetVal");
	        	if (retval == 0){
		        	// We're set to rock and roll
	            	if (!json.isNull("arg1")){
	            		
						long 				userID = json.getLong("arg1");
						NoteItApplication 	app = (NoteItApplication) getApplication();
						
						app.setUserID(userID);
	            		Toast.makeText(this, "You have been logged in.", Toast.LENGTH_SHORT).show();
	            		
	            		// Fetch the categories. This is a one time activity
	            		app.fetchCategories(new OnFetchCategoriesListener() {
							
							public void onPostExecute(long resultCode, ArrayList<Category> categories,
									String message) {

								if (resultCode == 0) {
									//Intent myIntent = new Intent(this, ShoppingListActivity.class);
				            		Intent myIntent = new Intent(LoginActivity.this, DashBoardActivity.class);
				                    startActivity(myIntent);
								}
							}
						});
	            	} else
	            		throw new Exception("Invalid email or password");
	        	} else 
	        		throw new Exception(json.getString("JSONRetMessage"));
			} else
				throw new Exception(getResources().getString(R.string.server_error));
		} catch (JSONException e){
			Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
			Log.e("NoteItApplication.loginUser", e.getMessage());
		} catch (Exception e){
    		Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.e("NoteItApplication.loginUser", e.getMessage());
		}
		
	}
}
