 package com.geekjamboree.noteit;

import java.util.ArrayList;
 
import com.geekjamboree.noteit.R;
import com.geekjamboree.noteit.NoteItApplication;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ListView;

public class ShoppingListActivity 
	extends ListActivity 
	implements NoteItApplication.OnFetchShoppingListsListener {

	ListView 				mListView;
	ProgressDialog			mProgressDialog = null;
	ShoppingListAdapter		mAdapter;
	CustomTitlebarWrapper 	mToolbar;
	int						mFontSize = 3;
	boolean					mIsShoppingListFetched = false;
	boolean					mIsDisplayCount	= true;
	static final String		IS_SHOPPINGLIST_FETCHED = "IS_SHOPPINGLIST_FETCHED";
	

	class ShoppingListAdapter extends ArrayAdapterWithFontSize<ShoppingList> {
		public ShoppingListAdapter(
				Context context, 
				int resource, 
				int textViewResourceId, 
				ArrayList<ShoppingList> objects) {
			super(context, resource, textViewResourceId, objects);
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			View view = super.getView(position, convertView, parent);
			
			TextView itemCount = (TextView) view.findViewById(R.id.shoppinglist_itemCount);
			if (itemCount != null) {
				if (mIsDisplayCount) {
					ShoppingList item = getItem(position);
					if (item != null) {
						itemCount.setText(" (" + String.valueOf(item.mItemCount) + ")");
						itemCount.setTextAppearance(parent.getContext(), super.getPreferredTextAppearance());
						itemCount.setVisibility(View.VISIBLE);
					} else { 
						itemCount.setVisibility(View.GONE);
					}
				} else {
					itemCount.setVisibility(View.GONE);
				}
			}
			return view;
		}
		
	}
		
	protected SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener = 
			new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if (key.equals("Item_Font_Size")) {
					mListView.invalidateViews();
				}
			}
		};

	@Override
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);
    	
    	if (savedInstanceState != null) {
    		mIsShoppingListFetched = savedInstanceState.getBoolean(IS_SHOPPINGLIST_FETCHED);
    	}
    	
        mToolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.shoppinglists);
        mToolbar.SetTitle(getResources().getText(R.string.shoplistsactivity_title));
        doSetupToolbarButtons();
        
        mListView = (ListView) findViewById(android.R.id.list);
    	mListView.setTextFilterEnabled(true);
		mAdapter = new ShoppingListAdapter(
				this, 
    			R.layout.shoppinglists_item, 
    			R.id.shoppinglist_name, 
    			((NoteItApplication)getApplication()).getShoppingList());
		mListView.setAdapter(mAdapter);
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	mIsDisplayCount = prefs.getBoolean("Display_Pending_Item_Count", true);
		
		class ItemClickAndPostExecuteListener 
    		implements AdapterView.OnItemClickListener {
    		
    		View mView = null;
    		
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    			mView = view;
    			// Now fetch the categories in the dbase
    			((NoteItApplication)getApplication()).setCurrentShoppingListIndex(position);
                Intent myIntent = new Intent(mView.getContext(), ItemListActivity.class);
                startActivity(myIntent);
			}
    	}
    	
    	mListView.setOnItemClickListener(new ItemClickAndPostExecuteListener());
            
        // register for context menus
        registerForContextMenu(mListView);
        
		Log.i("ShoppingListActivity.onCreate", "onCreate called");
		if (!mIsShoppingListFetched) {
        	((NoteItApplication) getApplication()).fetchShoppingLists(mIsDisplayCount, this);
            mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.progress_message), true);
		}
        else {
        	Log.i("ShoppingListActivity:onCreate", "Skipping fetchShoppingLists()");
        }
    }
     
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(IS_SHOPPINGLIST_FETCHED, mIsShoppingListFetched);
		super.onSaveInstanceState(outState);
	}

	protected void onPause() {
		Log.i("ShoppingListActivity.onPause", "onPause called");
		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing()) {
				Log.i("ShoppingListActivity.onPause", "onPause called while progress is showing");
				mProgressDialog.dismiss();
			}
			mProgressDialog = null;
		}
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
		super.onPause();
	}
	
    @Override
	protected void onResume() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	mIsDisplayCount = prefs.getBoolean("Display_Pending_Item_Count", true);
		prefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
		mListView.invalidateViews();
		super.onResume();
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
    	case R.id.add_shoplist:
    		doAddShoppingList();
    		return true;
    	case R.id.noteit_prefs:
			startActivity(new Intent(this, MainPreferenceActivity.class));
			return true;
    	case R.id.shoppinglist_home:
			Intent intent = new Intent(this, DashBoardActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
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
    	
		Log.i("ShoppingListActivity.onPostExecute", "onPostExecute called");
    	if (mProgressDialog != null && mProgressDialog.isShowing()) {
    		Log.i("ShoppingListActivity.onPostExecute", "Destroyed the progress bar");
    		mProgressDialog.dismiss();
    		mProgressDialog = null;
    	}

    	if (retval == 0){ // success
    		// [TODO]: Since we're directly passing a reference to the NoteItApplication.getShoppingList
    		// to the ListView as it's adapter and since NoteItApplication takes care of add/deleting items
    		// from the mShoppingList member. We don't have to clear the adapter. It is automatically taken
    		// care of. Got to improve this though.
    		// mAdapter.clear();
        	// if (!shopList.isEmpty()){
        	//	 mAdapter.addAll(shopList);
        	// }
    		mIsShoppingListFetched = true;
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
			
			AlertDialog dialog;
			dialog = new AlertDialog.Builder(ShoppingListActivity.this)
				.setView(dialogView)
				.setTitle(getResources().getString(R.string.shoppinglist_edit_title))
				.create();

			final EditText 	editListName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
			editListName.setText(((ShoppingList)mListView.getItemAtPosition(index)).mName);

			dialog.setButton(DialogInterface.BUTTON1, "OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					String 	listName = editListName.getText().toString();
	
					if (!listName.trim().matches("")) {
						dialog.dismiss();
					
						NoteItApplication app = (NoteItApplication)getApplication();
						ShoppingList	  list = (ShoppingList) mListView.getItemAtPosition(index);
						// Create a new list with the name
						app.editShoppingList(
								app.new ShoppingList(
									list.mID, 
									listName,
									list.mItemCount),
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
					} else 
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.shoppinglist_name_blank), Toast.LENGTH_SHORT).show();
				}});
				
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
    
    protected void doSetupToolbarButtons() {

    	ImageButton addButton = new ImageButton(this);
    	addButton.setImageResource(R.drawable.add);
    	addButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doAddShoppingList();
			}
    	});
		
    	ImageButton settingsButton = new ImageButton(this);
    	settingsButton.setImageResource(R.drawable.settings);
    	settingsButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
    			startActivity(new Intent(ShoppingListActivity.this, MainPreferenceActivity.class));
			}
		});
    	
    	ImageButton homeButton = new ImageButton(this);
    	homeButton.setImageResource(R.drawable.home);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(ShoppingListActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

    	mToolbar.addLeftAlignedButton(homeButton, false, true);
    	mToolbar.addRightAlignedButton(addButton, true, false);
    	mToolbar.addRightAlignedButton(settingsButton, true, false);
    }
    
    void doAddShoppingList() {
		// inflate the view from resource layout
		LayoutInflater	inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dialogView = inflater.inflate(R.layout.dialog_addshoppinglist, null, false);
		
		AlertDialog dialog = new AlertDialog.Builder(ShoppingListActivity.this)
			.setView(dialogView)
			.setTitle(getResources().getString(R.string.shoppinglist_add_title))
			.create();
		
		dialog.setButton(DialogInterface.BUTTON1, "OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				EditText 	editListName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
				String 		listName = editListName.getText().toString();

				if (!listName.trim().matches("")) {
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
					} else 
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.shoppinglist_name_blank), Toast.LENGTH_SHORT).show();
				}
			}
		);
	
		dialog.setButton(DialogInterface.BUTTON2, "Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Log.e("AddShoppingList", "Cancel");
				dialog.dismiss();
			}
		});

		dialog.show();    }
}