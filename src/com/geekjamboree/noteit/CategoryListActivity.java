/**
 * 
 */
package com.geekjamboree.noteit;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.CustomTitlebarWrapper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author mgupta
 *
 */
public class CategoryListActivity 
	extends Activity 
	implements AsyncInvokeURLTask.OnPostExecuteListener {
	
	ListView		mListView;
	
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
    	setContentView(R.layout.categories);
        toolbar.SetTitle(getResources().getText(R.string.categoriesactivity_title));
        
        mListView = (ListView) findViewById(android.R.id.list);
 //       mListView.setDividerHeight(2);

        ((NoteItApplication)getApplication()).fetchCategories(this);
 
	}
    
 	public void onPostExecute(JSONObject json) {
		try {
        	long 					retval = json.getLong("JSONRetVal");
        	ArrayList<Category> 	categoryList = new ArrayList<Category>();
			
        	if (retval == 0 && !json.isNull("arg1")){
	        	JSONArray jsonArr = json.getJSONArray("arg1");
	        	
	        	// [TODO]: This doesn't feel right, calling the app object
	        	// to read shopping list items and having to populate them
	        	// in the object from here. Figure out an elegant way to 
	        	// handle this.
	        	for (int index = 0; index < jsonArr.length(); index++){
	        		JSONObject thisObj = jsonArr.getJSONObject(index);
	        		Category thisItem = ((NoteItApplication)getApplication()).new Category(
	        				Long.parseLong(thisObj.getString("listID")),
							thisObj.getString("listName"));
	        		
	        		((NoteItApplication)getApplication()).addCategory(
	        				thisItem.mID,
	        				thisItem.mName);
	        		
	        		categoryList.add(thisItem);
	        	}

	        	if (!categoryList.isEmpty()){
	        		
                	mListView.setAdapter(new ArrayAdapter<Category>(
                			getBaseContext(), 
                			R.layout.shoppinglists_item, 
                			R.id.shoppinglist_name, 
                			categoryList));
                	mListView.setTextFilterEnabled(true);
                	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                   			String itemText = (String)mListView.getAdapter().getItem(position).toString();

                   			// When clicked, show a toast with the TextView text
                			Toast.makeText(getApplicationContext(), itemText, Toast.LENGTH_SHORT).show();
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
