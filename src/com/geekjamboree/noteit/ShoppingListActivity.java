 package com.geekjamboree.noteit;

import java.util.ArrayList;
 
import com.geekjamboree.noteit.R;
import com.geekjamboree.noteit.NoteItApplication;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem; 
import android.content.DialogInterface;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ListView;

public class ShoppingListActivity 
	extends ListActivity 
	implements NoteItApplication.OnFetchShoppingListsListener {

	ListView 										mListView;
	ProgressDialog									mProgressDialog = null;
	ArrayAdapter<NoteItApplication.ShoppingList>	mAdapter;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);
    	
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.shoppinglists);
        toolbar.SetTitle(getResources().getText(R.string.shoplistsactivity_title));
        
        mListView = (ListView) findViewById(android.R.id.list);
    	mListView.setTextFilterEnabled(true);
		mAdapter = new ArrayAdapter<NoteItApplication.ShoppingList>(
				this, 
    			R.layout.shoppinglists_item, 
    			R.id.shoppinglist_name, 
    			((NoteItApplication)getApplication()).getShoppingList());
		mListView.setAdapter(mAdapter);
    	
    	class ItemClickAndPostExecuteListener 
    		implements AdapterView.OnItemClickListener, NoteItApplication.OnFetchCategoriesListener {
    		
    		View mView = null;
    		
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			mView = view;
    			// Now fetch the categories in the dbase
    			((NoteItApplication)getApplication()).setCurrentShoppingListIndex(position);
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
        
        // Show a spinning wheel dialog
        mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.progress_message), true);
    
        // register for context menus
        registerForContextMenu(mListView);
        
        ImageButton btnAdd = (ImageButton) findViewById(R.id.button_shoppinglists_add);
        btnAdd.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// inflate the view from resource layout
				LayoutInflater	inflater = (LayoutInflater) ShoppingListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View dialogView = inflater.inflate(R.layout.dialog_addshoppinglist, (ViewGroup) findViewById(R.id.dialog_addshoppinglist_root));
				
				AlertDialog dialog = new AlertDialog.Builder(ShoppingListActivity.this)
					.setView(dialogView)
					.setTitle(getResources().getString(R.string.shoppinglist_add_title))
					.create();
				
				dialog.setButton(DialogInterface.BUTTON1, "OK", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						EditText 	editListName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
						String 		listName = editListName.getText().toString();

						dialog.dismiss();
					
						// Create a new list with the name
						((NoteItApplication)getApplication()).addShoppingList(
								listName,
								new NoteItApplication.OnMethodExecuteListerner() {
									
								public void onPostExecute(long resultCode, String message) {
									if (resultCode != 0) {
										Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
									} else {
										// refresh the listView
										mAdapter.notifyDataSetChanged();
									}
								}
							});
						}
					}
				);
			
				dialog.setButton(DialogInterface.BUTTON2, "Cancel", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						Log.e("AddShoppingList", "Cancel");
						dialog.dismiss();
					}
				});

				dialog.show();
			}
		});

        // Hook up the preference button
        ImageButton prefButton = (ImageButton)findViewById(R.id.button_shoppinglists_preferences);
        prefButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
    			startActivity(new Intent(ShoppingListActivity.this, MainPreferenceActivity.class));
			}
		});

		((NoteItApplication)getApplication()).fetchShoppingLists(this);
    }
     
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shoplists_menu, menu);
        
        return true;
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.shoplists_menu_context, menu);
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch(item.getItemId()){
    		case R.id.noteit_prefs:
    			startActivity(new Intent(this, MainPreferenceActivity.class));
    			return true;
    	}
    	
    	return false;
    }
    
    public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.menu_context_shoplist_edit:
			editShoppingList(info.position);
			return true;
		case R.id.menu_context_shoplist_delete:
			deleteShoppingList(info.position);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
    
    public void onPostExecute(long retval, ArrayList<ShoppingList> shopList, String errMsg) {
    	
    	if (mProgressDialog != null) mProgressDialog.dismiss();
    	

    	if (retval == 0){ // success
    		// [TODO]: Since we're directly passing a reference to the NoteItApplication.getShoppingList
    		// to the ListView as it's adapter and since NoteItApplication takes care of add/deleting items
    		// from the mShoppingList member. We don't have to clear the adapter. It is automatically taken
    		// care of. Got to improve this though.
    		// mAdapter.clear();
        	// if (!shopList.isEmpty()){
        	//	 mAdapter.addAll(shopList);
        	// }
    		
    		mAdapter.notifyDataSetChanged();
    	} else {
    		Toast.makeText(getApplicationContext(), "Error Occurred:" + errMsg, Toast.LENGTH_LONG).show();
    	}
    }
    
    protected void editShoppingList(final int index) {
		
    	if (index >= 0 && index < mListView.getCount()) {
			// inflate the view from resource layout
			LayoutInflater	inflater = (LayoutInflater) ShoppingListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View dialogView = inflater.inflate(R.layout.dialog_addshoppinglist, (ViewGroup) findViewById(R.id.dialog_addshoppinglist_root));
			
			AlertDialog dialog = new AlertDialog.Builder(ShoppingListActivity.this)
				.setView(dialogView)
				.setTitle(getResources().getString(R.string.shoppinglist_edit_title))
				.create();
			
			final EditText 	editListName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
			editListName.setText(((ShoppingList)mListView.getItemAtPosition(index)).mName);
			dialog.setButton(DialogInterface.BUTTON1, "OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					String 	listName = editListName.getText().toString();
	
					dialog.dismiss();
				
					// Create a new list with the name
					((NoteItApplication)getApplication()).editShoppingList(
						new ShoppingList(
								((ShoppingList)mListView.getItemAtPosition(index)).mID, 
								listName),
						new NoteItApplication.OnMethodExecuteListerner() {
							
							public void onPostExecute(long resultCode, String message) {
								if (resultCode != 0) {
									Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
								} else {
									// refresh the listView
									mAdapter.notifyDataSetChanged();
								}
							}
						}
					);
				}
			});
			
			dialog.setButton(DialogInterface.BUTTON2, "No", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					Log.e("AddShoppingList", "Cancel");
					dialog.dismiss();
				}
			});

			dialog.show();
		}
    }
    
    protected void deleteShoppingList(final int index){

    	if (index >= 0 && index < mListView.getCount()) {
			final AlertDialog dialog = new AlertDialog.Builder(ShoppingListActivity.this).create();
			
			dialog.setTitle(getResources().getString(R.string.shoplistsactivity_title));
			dialog.setMessage(getResources().getString(R.string.shopping_list_confirm_delte));
			dialog.setButton(DialogInterface.BUTTON1, "Yes", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {

					dialog.dismiss();
				
					Log.i("deleteShoppingList", "Called with index=" + index);
					((NoteItApplication)getApplication()).deleteShoppingList(
						((ShoppingList)mListView.getItemAtPosition(index)).mID,
						new NoteItApplication.OnMethodExecuteListerner() {
							
							public void onPostExecute(long resultCode, String message) {
								if (resultCode != 0) {
									Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
								} else {
									// Delete this item from the view
									mAdapter.notifyDataSetChanged();
								}
						}
					});
				}
			});
	
			dialog.setButton(DialogInterface.BUTTON2, "No", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					Log.e("AddShoppingList", "Cancel");
					dialog.dismiss();
				}
			});

			dialog.show();
		}    	
    }
}