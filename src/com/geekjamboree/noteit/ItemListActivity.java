 package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Item;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

public class ItemListActivity extends ExpandableListActivity implements NoteItApplication.OnFetchItemsListener {
	
	ExpandableListView	mListView;
	ProgressDialog		mProgressDialog = null;
	
	static final int ADD_ITEM_REQUEST = 0;
	
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);

        mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.progress_message));
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.itemlists);
        toolbar.SetTitle(getResources().getText(R.string.itemlistactivity_title));
        
        mListView = (ExpandableListView) findViewById(android.R.id.list);
        
        ImageButton btnAdd = (ImageButton) findViewById(R.id.itemlist_add);
        btnAdd.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(ItemListActivity.this, AddEditItemActivity.class);
				intent.putExtra("ADD", true);
				startActivityForResult(intent, ADD_ITEM_REQUEST);
			}
		});
        
 	   	((NoteItApplication)getApplication()).fetchItems(this);
    }

    public void onPostExecute(long retval, ArrayList<Item> items, String message) {
    	if (mProgressDialog != null) mProgressDialog.dismiss();
    	
    	if (retval == 0) {
        	
    		ItemsExpandableListAdapter adapter = new ItemsExpandableListAdapter(this);
        	
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
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.i("ItemListActivity.onActivityResult", "requestCode:" + requestCode + " resultCode: " + resultCode);
    	if (requestCode == ADD_ITEM_REQUEST && resultCode == RESULT_OK) {
    		// refresh our view
    		mListView.invalidateViews();
    	}
    }	     
}
