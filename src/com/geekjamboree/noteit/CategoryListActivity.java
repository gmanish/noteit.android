/**
 * 
 */
package com.geekjamboree.noteit;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.geekjamboree.noteit.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
        
        ImageButton textView = (ImageButton)findViewById(R.id.button_categories_preferences);
        textView.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
                Intent myIntent = new Intent(CategoryListActivity.this, ItemListActivity.class);
                startActivity(myIntent);
			}
		});
	}
    
 	public void onPostExecute(JSONObject json) {
		try {
        	long 									retval = json.getLong("JSONRetVal");
        	ArrayList<NoteItApplication.Category> 	categoryList = new ArrayList<NoteItApplication.Category>();
			
        	if (retval == 0 && !json.isNull("arg1")){
	        	JSONArray jsonArr = json.getJSONArray("arg1");
	        	
	        	// [TODO]: This doesn't feel right, calling the app object
	        	// to read shopping list items and having to populate them
	        	// in the object from here. Figure out an elegant way to 
	        	// handle this.
	        	for (int index = 0; index < jsonArr.length(); index++){
	        		JSONObject thisObj = jsonArr.getJSONObject(index);
	        		NoteItApplication.Category thisCategory = ((NoteItApplication)getApplication()).new Category(
	        				Long.parseLong(thisObj.getString("listID")),
							thisObj.getString("listName"),
							0);
	        		
	        		((NoteItApplication)getApplication()).addCategory(thisCategory);
	        		
	        		categoryList.add(thisCategory);
	        	}

	        	if (!categoryList.isEmpty()){
	        		
                	mListView.setAdapter(new ArrayAdapter<NoteItApplication.Category>(
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
