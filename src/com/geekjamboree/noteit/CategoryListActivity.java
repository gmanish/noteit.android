/**
 * 
 */
package com.geekjamboree.noteit;

import java.util.ArrayList;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * @author mgupta
 *
 */
public class CategoryListActivity 
	extends ListActivity implements DragDropListener {
	
	ArrayAdapterWithFontSize<Category> 	mAdapter;
	CategoryListView					mListView;
	TitleBar 							mToolbar;
	QuickAction							mQuickAction;
	int									mSelectedCategory = 0;
	
	class CategoryListAdapterDragDrop extends CategoryListAdapter implements DragDropListener {

		public CategoryListAdapterDragDrop(
				Context context, 
				int resource,
				int textViewResourceId, 
				ArrayList<Category> objects) {
			
			super(
				context, 
				resource, 
				textViewResourceId, 
				objects, 
				(NoteItApplication) getApplication(), 
				true);
		}

		public void onDrag(int dragSource, int dropTarget) {
			// I don't want to do anything here
		}

		public void onDrop(final int dragSource, final int dropTarget) {
			Log.i("CategoryListAdapter.onDrop", "Dropping: " + dragSource + " @ " + dropTarget);
			
			NoteItApplication app = (NoteItApplication) getApplication();
			app.reorderCategory(dragSource, dropTarget, new OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					if (resultCode == 0) {
						notifyDataSetChanged();
					} else
						CustomToast.makeText(
								getContext(), 
								getListView(), 
								message).show(true);
				}
			});
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View view = super.getView(position, convertView, parent);
			
			if (view != null) {
				
				NoteItApplication 	app = (NoteItApplication) getApplication();
				Category 			category = app.getCategory(position);
				TextView 			textView = (TextView) view.findViewById(mTextViewResId);
				
				if (category != null && category.mUserID != app.getUserID()) {
					textView.setCompoundDrawablesWithIntrinsicBounds(
							ThemeUtils.getResourceIdFromAttribute(
									getContext(), 
									R.attr.Category_Shared_Small), 
							0,
							ThemeUtils.getResourceIdFromAttribute(
									getContext(),
									R.attr.Hand_Small), 
							0);
				} else { 
					textView.setCompoundDrawablesWithIntrinsicBounds(
							ThemeUtils.getResourceIdFromAttribute(
									getContext(), 
									R.attr.Category_Small),
							0,
							ThemeUtils.getResourceIdFromAttribute(
									getContext(), 
									R.attr.Hand_Small), 
							0);
				}
			}
			return view;
		}
	}

	static final int QA_ID_EDIT 	= 0;
	static final int QA_ID_DELETE	= 1;
	static final int QA_ID_BOUGHT	= 2;
	static final String SELECTED_CATEGORY = "SELECTED_CATEGORY";
	
	protected SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener = 
			new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				if (key.equals("Item_Font_Size")) {
					mListView.invalidateViews();
				}
				
			}
		};

	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	if (savedInstanceState != null) {
    		mSelectedCategory = savedInstanceState.getInt(SELECTED_CATEGORY);
    	}
    	
    	TitleBar.RequestNoTitle(this);
    	ThemeUtils.onActivityCreateSetTheme(this);
    	setContentView(R.layout.categories);
    	mToolbar = (TitleBar) findViewById(R.id.categories_title);
    	mToolbar.SetTitle(getResources().getText(R.string.categoriesactivity_title));
        doSetupToolbarButtons();
        
        mListView = (CategoryListView) findViewById(android.R.id.list);
        mAdapter = new CategoryListAdapter(
    			this, 
    			R.layout.categorieslist_item, 
    			R.id.categorylists_item_name, 
    			((NoteItApplication) getApplication()).getCategories(),
    			(NoteItApplication) getApplication(),
    			true);
    	mListView.setAdapter(mAdapter);
    	mListView.setDragDropListener(this);
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
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.categorylist_menu, menu);
    	return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.categorylist_home:
			Intent intent = new Intent(CategoryListActivity.this, DashBoardActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			break;
		case R.id.categorylist_add:
			doAddCategory();
			break;
		case R.id.categorylist_settings:
			startActivity(new Intent(CategoryListActivity.this, MainPreferenceActivity.class));
			break;
		}
		
		return super.onOptionsItemSelected(item);
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

	public void onDrag(int dragSource, int dropTarget) {
		// I don't want to do anything here
	}

	public void onDrop(final int dragSource, final int dropTarget) {
		Log.i("CategoryListAdapter.onDrop", "Dropping: " + dragSource + " @ " + dropTarget);
		
		NoteItApplication app = (NoteItApplication) getApplication();
		mToolbar.showInderminateProgress(getString(R.string.progress_message));
		try {
			app.reorderCategory(dragSource, dropTarget, new OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					try {
						if (resultCode == 0) {
							mAdapter.notifyDataSetChanged();
						} else
							CustomToast.makeText(
									CategoryListActivity.this, 
									CategoryListActivity.this.getListView(), 
									message).show(true);
					} finally {
						mToolbar.hideIndeterminateProgress();
					}
				}
			});
		} catch (Exception e) {
			mToolbar.hideIndeterminateProgress();
		}
	}

	protected void doSetupToolbarButtons() {

    	ImageButton homeButton = mToolbar.addLeftAlignedButton(R.drawable.home, true, true);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(CategoryListActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
    	
    	mToolbar.addVerticalSeparator(this, true);
    	mToolbar.addVerticalSeparator(this, false);

    	ImageButton addButton = mToolbar.addRightAlignedButton(R.drawable.add, false, true);
    	addButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				doAddCategory();
			}
    	});
		
    	ImageButton settingsButton = mToolbar.addRightAlignedButton(R.drawable.settings, false, true);
    	settingsButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
    			startActivity(
    				new Intent(CategoryListActivity.this, MainPreferenceActivity.class));
			}
		});
    }
    
    protected void doEditCategory(final int position) {
    	
    	doShowCategoryNameDialog(false, position, new CategoryNameDialogListener() {
			
			public void onDialogOK(final String categoryName) {
		    	
				NoteItApplication 	app = (NoteItApplication) getApplication();
		    	Category 			category = app.new Category(app.getCategories().get(position));
		    	
		    	category.mName = categoryName;
		    	mToolbar.showInderminateProgress(getString(R.string.progress_message));
		    	try {
			    	app.editCategory(Category.CATEGORY_NAME, category, new OnMethodExecuteListerner() {
						
						public void onPostExecute(long resultCode, String message) {
							try {
								if (resultCode != 0)
									CustomToast.makeText(
											CategoryListActivity.this, 
											CategoryListActivity.this.getListView(), 
											message).show(true);
								else {
									mAdapter.getItem(position).mName = categoryName;
									mAdapter.notifyDataSetChanged();
								}
							} finally {
								mToolbar.hideIndeterminateProgress();
							}
						}
					});
		    	} catch (Exception e){
		    		mToolbar.hideIndeterminateProgress();
		    	}
			}
		});
    	
    }
    
    protected void doDeleteCategory(int position) {
    	
    	NoteItApplication app = (NoteItApplication) getApplication();
    	mToolbar.showInderminateProgress(getString(R.string.progress_message));
    	try {
	    	app.deleteCategory(position, new NoteItApplication.OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					try {
						if (resultCode != 0)
							CustomToast.makeText(
									CategoryListActivity.this, 
									CategoryListActivity.this.getListView(), 
									message).show(true);
						else
							mAdapter.notifyDataSetChanged();
					} finally {
						mToolbar.hideIndeterminateProgress();
					}
				}
			});
    	} catch (Exception e) {
    		mToolbar.hideIndeterminateProgress();
    	}
    }
    
    static interface CategoryNameDialogListener {
    	void onDialogOK(String categoryName);
    }
    
    protected void doShowCategoryNameDialog(
    		boolean isAdd, 
    		int selPosition,
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
		else {
			dialog.setTitle(getResources().getString(R.string.categorylists_edit));
			EditText  editName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
			if (editName != null) {
				Category category = ((NoteItApplication) getApplication()).getCategory(selPosition);
				if (category != null) {
					editName.setText(category.mName);
					editName.selectAll();
				}
			}
		}
			
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {

				EditText 	editListName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
				String 		categoryName = editListName.getText().toString();

				if (!categoryName.trim().equals("")) {
					dialog.dismiss();
					listener.onDialogOK(categoryName);
				}
			}
		});
		
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		dialog.show();
	}
    
    void doAddCategory() {
		doShowCategoryNameDialog(true, 0, // Position not relevant in add case 
			new CategoryNameDialogListener() {
				public void onDialogOK(String categoryName) {
					
					NoteItApplication 	app = (NoteItApplication) getApplication();
					Category 			category = app.new Category(0, categoryName, app.getUserID(), 0);
					
					mToolbar.showInderminateProgress(getString(R.string.progress_message));
					try {
						((NoteItApplication) getApplication()).addCategory(
							category,
							new NoteItApplication.OnAddCategoryListener() {
								
								public void onPostExecute(long result, Category category, String message) {
									try {
										if (result != 0) {
											CustomToast.makeText(
													CategoryListActivity.this, 
													CategoryListActivity.this.getListView(), 
													message).show(true);
										} else {
								    		mAdapter.notifyDataSetChanged();
										}
									} finally {
										mToolbar.hideIndeterminateProgress();
									}
								}
							});
					} catch (Exception e) {
						mToolbar.hideIndeterminateProgress();
					}
				}
		});
    }
}
