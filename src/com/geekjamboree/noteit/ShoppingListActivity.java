package com.geekjamboree.noteit;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.geekjamboree.noteit.R;
import com.geekjamboree.noteit.NoteItApplication;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;
import com.geekjamboree.noteit.AsyncInvokeURLTask;

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

public class ShoppingListActivity 
	extends Activity 
	implements AsyncInvokeURLTask.OnPostExecuteListener {

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
        	long 										retval = json.getLong("JSONRetVal");
        	ArrayList<NoteItApplication.ShoppingList> 	shopList = new ArrayList<NoteItApplication.ShoppingList>();
			
        	if (retval == 0 && !json.isNull("arg1")){
	        	JSONArray jsonArr = json.getJSONArray("arg1");
	        	
	        	// [TODO]: This doesn't feel right, calling the app object
	        	// to read shopping list items and having to populate them
	        	// in the object from here. Figure out an elegant way to 
	        	// handle this.
	        	for (int index = 0; index < jsonArr.length(); index++){
	        		JSONObject thisObj = jsonArr.getJSONObject(index);
	        		ShoppingList thisItem = new ShoppingList(
	        				Long.parseLong(thisObj.getString("listID")),
							thisObj.getString("listName"));
	        		
	        		((NoteItApplication)getApplication()).addShoppingList(thisItem.mID, thisItem.mName);
	        		
	        		shopList.add(thisItem);
	        	}

	        	if (!shopList.isEmpty()){
	        		mListView.setAdapter(new ArrayAdapter<NoteItApplication.ShoppingList>(this, 
                			R.layout.shoppinglists_item, R.id.shoppinglist_name, shopList));
                	mListView.setTextFilterEnabled(true);
                	
                	class ItemClickAndPostExecuteListener 
                		implements AdapterView.OnItemClickListener, NoteItApplication.OnFetchCategoriesListener {
                		
                		View mView = null;
                		
                		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                			String itemText = (String)mListView.getAdapter().getItem(position).toString();
                			
                			mView = view;
                			
                			// When clicked, show a toast with the TextView text
                			Toast.makeText(getApplicationContext(), itemText, Toast.LENGTH_SHORT).show();
                			
                			// Now fetch the categories in the dbase
                			((NoteItApplication)getApplication()).fetchCategories(this);
            			}
                		
                	    public void onPostExecute(long resultCode, ArrayList<Category> categories, String message) {
                			// Invoke the category activity
                	    	assert(mView != null);
                	    	if (mView != null && resultCode == 0){
	                            Intent myIntent = new Intent(mView.getContext(), ItemListActivity.class);
	                            startActivity(myIntent);
                	    	}
                	    	else
                	    		// Display alert with error message
                	    		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                	    }
                	}
                	
                	mListView.setOnItemClickListener(new ItemClickAndPostExecuteListener());
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
