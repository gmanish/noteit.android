package com.geekjamboree.noteit;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        setTitle(getResources().getText(R.string.login_activity_title));
        
        Button next = (Button) findViewById(R.id.buttonLogin);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	// Try to authenticate the user with the supplied credentials.
            	// If authentication succeedes, switch to the shopping list view
            	EditText emailID = (EditText)findViewById(R.id.editEmailID);
            	((NoteItApplication)getApplication()).loginUser(
    				emailID.getText().toString(), new LoginActivityOnPostExecuteListener(view));
            }

        });
    }
    
	private class LoginActivityOnPostExecuteListener implements AsyncInvokeURLTask.OnPostExecuteListener {
		
		private View mView;
		
		LoginActivityOnPostExecuteListener(View view){
			mView = view;
		}
		
		public void onPostExecute(JSONObject json) {
			try {
	        	long retval = json.getLong("JSONRetVal");
	        	if (retval == 0){
		        	// We're set to rock and roll
		        	Log.i("NoteItApplication.loginUser", "Login successful. Let's get rocked!");
	            	if (!json.isNull("arg1")){
						long userID = json.getLong("arg1");
						((NoteItApplication)getApplication()).setUserID(userID);
	            		Toast.makeText(getApplicationContext(), "You have been logged in.", Toast.LENGTH_SHORT).show();
	                    Intent myIntent = new Intent(mView.getContext(), ShoppingListActivity.class);
	                    startActivity(myIntent);
	            	} else
	            		throw new Exception("Invalid email or password");
	        	} else 
            		throw new Exception("Invalid email or password");
			} catch (JSONException e){
				Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
				Log.e("NoteItApplication.loginUser", e.getMessage());
			} catch (Exception e){
        		Toast.makeText(getApplicationContext(), "Invalid email or password.", Toast.LENGTH_SHORT).show();
				Log.e("NoteItApplication.loginUser", e.getMessage());
			}
			
		}
	}    
}
