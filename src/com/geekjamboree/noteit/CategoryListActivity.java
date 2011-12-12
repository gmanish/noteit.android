/**
 * 
 */
package com.geekjamboree.noteit;

import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;
import com.geekjamboree.noteit.R;
import com.geekjamboree.noteit.NoteItApplication;
import com.geekjamboree.noteit.NoteItApplication.Category;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * @author mgupta
 *
 */
public class CategoryListActivity 
	extends ListActivity {
	
	ArrayAdapter<Category> 	mAdapter;
	ListView				mListView;
	CustomTitlebarWrapper 	mToolbar;
	QuickAction				mQuickAction;
	int						mSelectedCategory = 0;
	
	static final int QA_ID_EDIT 	= 0;
	static final int QA_ID_DELETE	= 1;
	static final int QA_ID_BOUGHT	= 2;
	static final String SELECTED_CATEGORY = "SELECTED_CATEGORY";
	
	protected SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener = 
			new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if (key == "Item_Font_Size") {
					mListView.invalidateViews();
				}
				
			}
		};

	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	if (savedInstanceState != null) {
    		mSelectedCategory = savedInstanceState.getInt(SELECTED_CATEGORY);
    	}
    	
    	mToolbar = new CustomTitlebarWrapper(this);
    	setContentView(R.layout.categories);
    	mToolbar.SetTitle(getResources().getText(R.string.categoriesactivity_title));
        doSetupToolbarButtons();
        
        mListView = (ListView) findViewById(android.R.id.list);
        mAdapter = new ArrayAdapterWithFontSize<Category>(
    			getBaseContext(), 
    			R.layout.categorieslist_item, 
    			R.id.categorylists_item_name, 
    			((NoteItApplication) getApplication()).getCategories());
    	mListView.setAdapter(mAdapter);
    	mListView.setTextFilterEnabled(true);
    	mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       			
    			mSelectedCategory = position;
    			mQuickAction.show(view);
    		}
    	});

        // Set up Quick Actions
         ActionItem editItem 	= new ActionItem(
									QA_ID_EDIT,
									getResources().getString(R.string.itemlistqe_edit),
									getResources().getDrawable(R.drawable.edit)); 
        ActionItem deleteItem 	= new ActionItem(
									QA_ID_DELETE,
									getResources().getString(R.string.itemlistqe_delete),
									getResources().getDrawable(R.drawable.delete));

		mQuickAction = new QuickAction(this);
		mQuickAction.addActionItem(editItem);
		mQuickAction.addActionItem(deleteItem);
		
		//setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			
			public void onItemClick(QuickAction quickAction, int pos, int actionId) {

				switch(actionId){
					case QA_ID_EDIT:
						doEditCategory(mSelectedCategory);
						break;
					case QA_ID_DELETE:
						doDeleteCategory(mSelectedCategory);
						break;
				}
			}
		});
		
		mQuickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {			
			
			public void onDismiss() {
			}
		});    
    	
    	// [NOTE] This activity assumes the categories have already been fetched
    	// Categories should have been fetched by now, we don't need to do anything
	}
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
    	outState.putInt(SELECTED_CATEGORY, mSelectedCategory);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this); 
		prefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
		mListView.invalidateViews();
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i("CategoryListActivity.onPause", "onPause called");
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
		super.onPause();
	}

	protected void doSetupToolbarButtons() {

    	ImageButton addButton = new ImageButton(this);
    	addButton.setImageResource(R.drawable.add);
    	addButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				doShowCategoryNameDialog(true, 
					new CategoryNameDialogListener() {
						
						public void onDialogOK(String categoryName) {
							
							NoteItApplication 	app = (NoteItApplication) getApplication();
							Category 			category = app.new Category(0, categoryName, app.getUserID());
							
							((NoteItApplication) getApplication()).addCategory(
								category,
								new NoteItApplication.OnAddCategoryListener() {
									
									public void onPostExecute(long result, Category category, String message) {
										if (result != 0) {
											Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
										} else {
								    		mAdapter.notifyDataSetChanged();
										}
									}
								});
						}
				});
			}
    	});
		
    	ImageButton settingsButton = new ImageButton(this);
    	settingsButton.setImageResource(R.drawable.settings);
    	settingsButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
    			startActivity(new Intent(CategoryListActivity.this, MainPreferenceActivity.class));
			}
		});

    	ImageButton homeButton = new ImageButton(this);
    	homeButton.setImageResource(R.drawable.home);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(CategoryListActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

    	mToolbar.addLeftAlignedButton(homeButton, false, true);
    	mToolbar.addRightAlignedButton(addButton, true, false);
    	mToolbar.addRightAlignedButton(settingsButton, true, false);
    }
    
    protected void doEditCategory(final int position) {
    	
    	doShowCategoryNameDialog(false, new CategoryNameDialogListener() {
			
			public void onDialogOK(final String categoryName) {
		    	
				NoteItApplication 	app = (NoteItApplication) getApplication();
		    	Category 			category = app.new Category(app.getCategories().get(position));
		    	
		    	category.mName = categoryName;
		    	app.editCategory(Category.CATEGORY_NAME, category, new OnMethodExecuteListerner() {
					
					public void onPostExecute(long resultCode, String message) {
						if (resultCode != 0)
							Toast.makeText(CategoryListActivity.this, message, Toast.LENGTH_LONG).show();
						else {
							mAdapter.getItem(position).mName = categoryName;
							mAdapter.notifyDataSetChanged();
						}
					}
				});
			}
		});
    	
    }
    
    protected void doDeleteCategory(int position) {
    	
    	NoteItApplication app = (NoteItApplication) getApplication();
    	app.deleteCategory(position, new NoteItApplication.OnMethodExecuteListerner() {
			
			public void onPostExecute(long resultCode, String message) {
				if (resultCode != 0)
					Toast.makeText(CategoryListActivity.this, message, Toast.LENGTH_LONG).show();
				else
					mAdapter.notifyDataSetChanged();
			}
		});
    }
    
    static interface CategoryNameDialogListener {
    	void onDialogOK(String categoryName);
    }
    
    protected void doShowCategoryNameDialog(
    		boolean isAdd, 
    		final CategoryNameDialogListener listener) {
		
    	// inflate the view from resource layout
		LayoutInflater	inflater = (LayoutInflater) CategoryListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View dialogView = inflater.inflate(
				R.layout.dialog_addshoppinglist, 
				(ViewGroup) findViewById(R.id.dialog_addshoppinglist_root));
		
		AlertDialog dialog = new AlertDialog.Builder(CategoryListActivity.this)
			.setView(dialogView)
			.create();

		if (isAdd)
			dialog.setTitle(getResources().getString(R.string.categorylists_add));
		else
			dialog.setTitle(getResources().getString(R.string.categorylists_edit));
			
		dialog.setButton(DialogInterface.BUTTON1, "OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {

				EditText 	editListName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
				String 		categoryName = editListName.getText().toString();

				if (!categoryName.trim().equals("")) {
					dialog.dismiss();
					listener.onDialogOK(categoryName);
				}
			}
		});
		
		dialog.setButton(DialogInterface.BUTTON2, "Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		dialog.show();
	}
}
