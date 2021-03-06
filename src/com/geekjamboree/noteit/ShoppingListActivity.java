 package com.geekjamboree.noteit;

import java.util.ArrayList;
 
import com.geekjamboree.noteit.R;
import com.geekjamboree.noteit.NoteItApplication;
import com.geekjamboree.noteit.ItemListActivity.ItemType;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;

import android.app.AlertDialog;
import android.app.ListActivity;
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
import android.widget.ListView;

public class ShoppingListActivity 
	extends ListActivity 
	implements NoteItApplication.OnFetchShoppingListsListener {

	ListView 				mListView;
	ShoppingListAdapter		mAdapter;
	TitleBar 				mToolbar;
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
			super(context, resource, textViewResourceId, objects, ItemType.BOLD);
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			View view = super.getView(position, convertView, parent);
			
			ShoppingList 	item = getItem(position);
			TextView 		itemCount = (TextView) view.findViewById(R.id.shoppinglist_itemCount);
			if (itemCount != null) {
				if (mIsDisplayCount) {
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

			TextView listName = (TextView) view.findViewById(R.id.shoppinglist_name);
			if (listName != null) {
				if (item.mUserID != ((NoteItApplication) getApplication()).getUserID())
					listName.setCompoundDrawablesWithIntrinsicBounds(
							ThemeUtils.getResourceIdFromAttribute(
									ShoppingListActivity.this, 
									R.attr.SharedShoppingList), 0, 0, 0);
				else 
					listName.setCompoundDrawablesWithIntrinsicBounds(
							ThemeUtils.getResourceIdFromAttribute(
									ShoppingListActivity.this, 
									R.attr.ShoppingList_Cart), 0, 0, 0);
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
    	
    	TitleBar.RequestNoTitle(this);
    	ThemeUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.shoppinglists);
        mToolbar = (TitleBar) findViewById(R.id.shoppinglist_title);
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
			mToolbar.showInderminateProgress(getString(R.string.progress_message));
        	((NoteItApplication) getApplication()).fetchShoppingLists(true, this);
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
		mToolbar.hideIndeterminateProgress();
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
    		CustomToast.makeText(
    				getApplicationContext(),
    				getListView(),
    				"Error Occurred:" + errMsg).show(true);
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

			dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					String 	listName = editListName.getText().toString();
	
					if (!listName.trim().matches("")) {
						dialog.dismiss();
					
						NoteItApplication app = (NoteItApplication)getApplication();
						ShoppingList	  list = (ShoppingList) mListView.getItemAtPosition(index);
						// Create a new list with the name
						mToolbar.showInderminateProgress(getString(R.string.progress_message));
						app.editShoppingList(
								app.new ShoppingList(
									list.mID, 
									listName,
									list.mItemCount,
									list.mUserID),
							new NoteItApplication.OnMethodExecuteListerner() {
								
								public void onPostExecute(long resultCode, String message) {
									try {
										if (resultCode != 0) {
								    		CustomToast.makeText(
								    				getApplicationContext(),
								    				getListView(),
								    				message).show(true);
										} else {
											// refresh the listView
											mAdapter.notifyDataSetChanged();
										}
									} finally {
										mToolbar.hideIndeterminateProgress();
									}
								}
							}
						);
					} else 
			    		CustomToast.makeText(
			    				getApplicationContext(),
			    				getListView(),
			    				getResources().getString(R.string.shoppinglist_name_blank)).show(true);
				}});
				
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
				
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
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {

					dialog.dismiss();
				
					Log.i("deleteShoppingList", "Called with index=" + index);
					mToolbar.showInderminateProgress(getString(R.string.progress_message));
					((NoteItApplication)getApplication()).deleteShoppingList(
						((ShoppingList)mListView.getItemAtPosition(index)).mID,
						new NoteItApplication.OnMethodExecuteListerner() {
						
							public void onPostExecute(long resultCode, String message) {
								try {
									if (resultCode != 0) {
										CustomToast.makeText(
												getApplicationContext(),
												getListView(),
												message).show(true);
									} else {
										// Delete this item from the view
										mAdapter.notifyDataSetChanged();
									}
								} finally {
									mToolbar.hideIndeterminateProgress();
								}
						}
					});
				}
			});
	
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					Log.e("AddShoppingList", "Cancel");
					dialog.dismiss();
				}
			});

			dialog.show();
		}    	
    }
    
    protected void doSetupToolbarButtons() {

    	ImageButton homeButton = mToolbar.addLeftAlignedButton(R.drawable.home);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(ShoppingListActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

    	mToolbar.addVerticalSeparator(this, true);
    	mToolbar.addVerticalSeparator(this, false);

    	ImageButton addButton = mToolbar.addRightAlignedButton(R.drawable.add);
    	addButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doAddShoppingList();
			}
    	});
		
    	ImageButton settingsButton = mToolbar.addRightAlignedButton(R.drawable.settings);
    	settingsButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
    			startActivity(new Intent(ShoppingListActivity.this, MainPreferenceActivity.class));
			}
		});
    }
    
    void doAddShoppingList() {
		// inflate the view from resource layout
		LayoutInflater	inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dialogView = inflater.inflate(R.layout.dialog_addshoppinglist, null, false);
		
		AlertDialog dialog = new AlertDialog.Builder(ShoppingListActivity.this)
			.setView(dialogView)
			.setTitle(getResources().getString(R.string.shoppinglist_add_title))
			.create();
		
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			
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
									CustomToast.makeText(
											getApplicationContext(),
											getListView(),
											message).show(true);
								} else {
									// refresh the listView
									mAdapter.notifyDataSetChanged();
								}
							}
						});
					} else 
						CustomToast.makeText(
								getApplicationContext(),
								getListView(),
								getResources().getString(R.string.shoppinglist_name_blank)).show(true);
				}
			}
		);
	
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Log.e("AddShoppingList", "Cancel");
				dialog.dismiss();
			}
		});

		dialog.show();    
	}
}