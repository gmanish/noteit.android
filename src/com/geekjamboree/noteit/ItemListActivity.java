package com.geekjamboree.noteit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.geekjamboree.noteit.NoteItApplication.Item;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

public class ItemListActivity extends ExpandableListActivity implements AsyncInvokeURLTask.OnPostExecuteListener {
	
	ListView	mListView;
	
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);

    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.itemlists);
        toolbar.SetTitle(getResources().getText(R.string.itemlistactivity_title));
        
        mListView = (ExpandableListView) findViewById(android.R.id.list);
        
 	   	((NoteItApplication)getApplication()).fetchItems(this);
    }

    public void onPostExecute(JSONObject json) {
		try {
        	long 			retval = json.getLong("JSONRetVal");
 			
        	if (retval == 0 && !json.isNull("arg1")){
	        	JSONArray jsonArr = json.getJSONArray("arg1");
	        	
	        	ItemsExpandableListAdapter adapter = new ItemsExpandableListAdapter(
	        			this);
	        	// [TODO]: This doesn't feel right, calling the app object
	        	// to read shopping list items and having to populate them
	        	// in the object from here. Figure out an elegant way to 
	        	// handle this.
	        	for (int index = 0; index < jsonArr.length(); index++){
	        		JSONObject thisObj = jsonArr.getJSONObject(index);
	        		
	        		// construct the Item from JSON
	        		Item thisItem = new Item(
	        				Long.parseLong(thisObj.getString("instanceID")),
							thisObj.getString("itemName"),
							Long.parseLong(thisObj.getString("categoryID_FK")));
	        		
	        		// Get a reference to the parent Category
	        		NoteItApplication.Category category = ((NoteItApplication)getApplication()).getCategory(thisItem.mCategoryID);
	        		
        			//[TODO:] Send this data back to application
	        		//((NoteItApplication)getApplication()).add(thisItem.mID, thisItem.mName);
	        		
	        		adapter.AddItem(thisItem, category);
	        	}

        		((ExpandableListView)mListView).setAdapter(adapter);
            	mListView.setTextFilterEnabled(true);
            	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            		
	        		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        			String itemText = (String)mListView.getAdapter().getItem(position).toString();
	        			
	        			// When clicked, show a toast with the TextView text
	        			Toast.makeText(getApplicationContext(), itemText, Toast.LENGTH_SHORT).show();
        			}
            	});
        	}
		}catch (JSONException e){
			Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
			Log.e("NoteItApplication.loginUser", e.getMessage());
		}
	}
}
