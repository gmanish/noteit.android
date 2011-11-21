 package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Item;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class ItemListActivity extends ExpandableListActivity implements NoteItApplication.OnFetchItemsListener {
	
	ExpandableListView	mListView;
	ProgressDialog		mProgressDialog = null;
	 
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);

        mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.progress_message));
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.itemlists);
        toolbar.SetTitle(getResources().getText(R.string.itemlistactivity_title));
        
        mListView = (ExpandableListView) findViewById(android.R.id.list);
        
 	   	((NoteItApplication)getApplication()).fetchItems(this);
    }

    public void onPostExecute(long retval, ArrayList<Item> items, String message) {
    	if (mProgressDialog != null) mProgressDialog.dismiss();
    	
    	if (retval == 0) {
        	
    		ItemsExpandableListAdapter adapter = new ItemsExpandableListAdapter(this);
        	
        	// [TODO]: This doesn't feel right, calling the app object
        	// to read shopping list items and having to populate them
        	// in the object from here. Figure out an elegant way to 
        	// handle this.
        	for (int index = 0; index < items.size(); index++){
        		
        		Item thisItem = items.get(index);
        		
        		// Get a reference to the parent Category
        		NoteItApplication.Category category = ((NoteItApplication)getApplication()).getCategory(thisItem.mCategoryID);
        		
        		adapter.AddItem(thisItem, category);
        	}

    		((ExpandableListView)mListView).setAdapter(adapter);
        	
    		mListView.setTextFilterEnabled(true);
        	for (int i = 0; i < adapter.getGroupCount(); i++){
        		mListView.expandGroup(i);
        	}

        	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        		
        		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        			String itemText = (String)mListView.getAdapter().getItem(position).toString();
        			
        			// When clicked, show a toast with the TextView text
        			Toast.makeText(getApplicationContext(), itemText, Toast.LENGTH_SHORT).show();
    			}
        	});
    	}
    	else {
			Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
    	}
    		
	}
}
