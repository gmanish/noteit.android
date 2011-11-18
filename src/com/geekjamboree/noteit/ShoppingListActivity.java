package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.R;
import com.geekjamboree.noteit.NoteItApplication;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ListView;

public class ShoppingListActivity 
	extends ListActivity 
	implements NoteItApplication.OnFetchShoppingListsListener {

	ListView 			mListView;
	ProgressDialog		mProgressDialog = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);

    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.shoppinglists);
        toolbar.SetTitle(getResources().getText(R.string.shoplistsactivity_title));
        
        mListView = (ListView) findViewById(android.R.id.list);
 //       mListView.setDividerHeight(2);
        
        // Show a spinning wheel dialog
        mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.progress_message), true);
        
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
    
    public void onPostExecute(long retval, ArrayList<ShoppingList> shopList, String errMsg) {
    	
    	if (mProgressDialog != null) mProgressDialog.dismiss();
    	
    	if (retval == 0){ // success
        	if (!shopList.isEmpty()){
        		mListView.setAdapter(new ArrayAdapter<NoteItApplication.ShoppingList>(this, 
            			R.layout.shoppinglists_item, R.id.shoppinglist_name, shopList));
            	mListView.setTextFilterEnabled(true);
            	
            	class ItemClickAndPostExecuteListener 
            		implements AdapterView.OnItemClickListener, NoteItApplication.OnFetchCategoriesListener {
            		
            		View mView = null;
            		
            		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            			mView = view;
            			// Now fetch the categories in the dbase
            			((NoteItApplication)getApplication()).setCurrentShoppingList(position);
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
    		Toast.makeText(getApplicationContext(), "Error Occurred:" + errMsg, Toast.LENGTH_LONG).show();
    	}
    }
}
