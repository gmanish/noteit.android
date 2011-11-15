package com.geekjamboree.noteit;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.geekjamboree.noteit.NoteItApplication.ShoppingListItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ListView;

public class ShoppingListActivity extends Activity implements AsyncInvokeURLTask.OnPostExecuteListener {

	ListView 	mListView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);

    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.shoppinglists);
        toolbar.SetTitle(getResources().getText(R.string.shoplistsactivity_title));
        
        mListView = (ListView) findViewById(android.R.id.list);
 //       mListView.setDividerHeight(2);
        
 	   	((NoteItApplication)getApplication()).fetchShoppingLists(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shoplists_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case R.id.noteit_prefs:
    			startActivity(new Intent(this, MainPreferenceActivity.class));
    			return true;
    	}
    	
    	return false;
    }
    
    public void onPostExecute(JSONObject json) {
		try {
        	long 						retval = json.getLong("JSONRetVal");
        	ArrayList<ShoppingListItem> shopList = new ArrayList<ShoppingListItem>();
			
        	if (retval == 0 && !json.isNull("arg1")){
	        	JSONArray jsonArr = json.getJSONArray("arg1");
	        	
	        	// [TODO]: This doesn't feel right, calling the app object
	        	// to read shopping list items and having to populate them
	        	// in the object from here. Figure out an elegant way to 
	        	// handle this.
	        	for (int index = 0; index < jsonArr.length(); index++){
	        		JSONObject thisObj = jsonArr.getJSONObject(index);
	        		ShoppingListItem thisItem = ((NoteItApplication)getApplication()).new ShoppingListItem(
	        				Long.parseLong(thisObj.getString("listID")),
							thisObj.getString("listName"));
	        		
	        		((NoteItApplication)getApplication()).addShoppingListItem(
	        				thisItem.mID,
	        				thisItem.mName);
	        		
	        		shopList.add(thisItem);
	        	}

	        	if (!shopList.isEmpty()){
	        		mListView.setAdapter(new ArrayAdapter<ShoppingListItem>(this, 
                			R.layout.shoppinglists_item, R.id.shoppinglist_name, shopList));
                	mListView.setTextFilterEnabled(true);
                	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                		
                		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                			String itemText = (String)mListView.getAdapter().getItem(position).toString();
                			
                			// When clicked, show a toast with the TextView text
                			Toast.makeText(getApplicationContext(), itemText, Toast.LENGTH_SHORT).show();
                			
                			// Invoke the category activity
                            Intent myIntent = new Intent(view.getContext(), CategoryListActivity.class);
                            startActivity(myIntent);
            			}
                	});
            	}
            	else {
            		// Display alert with error message
            		Toast.makeText(getApplicationContext(), "There are no lists. Please add a new list.", Toast.LENGTH_LONG).show();
            	}
	        	
        	} else {
        		String errMsg = json.getString("JSONRetMessage");
        		Toast.makeText(getApplicationContext(), "Error Occurred:" + errMsg, Toast.LENGTH_LONG).show();
        	}
		} catch (JSONException e){
			Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
			Log.e("NoteItApplication.loginUser", e.getMessage());
		}
	}

}
