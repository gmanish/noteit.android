/**
 * 
 */
package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.R;
import com.geekjamboree.noteit.NoteItApplication;
import com.geekjamboree.noteit.NoteItApplication.Category;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
	implements NoteItApplication.OnFetchCategoriesListener {
	
	ListView		mListView;
	ProgressDialog	mProgressDialog = null;
	
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
    	setContentView(R.layout.categories);
        toolbar.SetTitle(getResources().getText(R.string.categoriesactivity_title));
        
        mListView = (ListView) findViewById(android.R.id.list);
 //       mListView.setDividerHeight(2);

        mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.progress_message), true);
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
    
 	public void onPostExecute(long resultCode, ArrayList<Category> categories, String message) {
    	ArrayList<NoteItApplication.Category> 	categoryList = ((NoteItApplication)getApplication()).getCategories();
    	
    	if (mProgressDialog != null) mProgressDialog.dismiss();
    	 
    	if (resultCode == 0 && !categoryList.isEmpty()){
    		 
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
    	else if (resultCode != 0){
    		// Display alert with error message
    		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    	}
 	}
}
