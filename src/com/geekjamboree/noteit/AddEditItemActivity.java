package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Item;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.OnAddItemListener;
import com.geekjamboree.noteit.NoteItApplication.OnSuggestItemsListener;
import com.geekjamboree.noteit.NoteItApplication.OnGetItemListener;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class AddEditItemActivity extends Activity {

	static final int 	UNITS_GENERIC = 0;
	static final int 	UNITS_METRIC = 1;
	static final int	UNITS_IMPERIAL = 2;
	Item 				thisItem;
	
	boolean				mIsAddItem = true;
	
	// controls
	EditText					mEditName;
	EditText					mEditQuantity;
	EditText					mEditCost;
	Spinner						mSpinCategories;
	ArrayList<String>			mSuggestions = new ArrayList<String>();
	ArrayAdapter<String>		mAutoCompleteAdapter;
	AutoCompleteTextView 		mItemName;
	ArrayList<Item>				mNewItems = new ArrayList<Item>();
	long						mItemID = 0; // Holds only for edit mode

	// This one's for the AutoTextComplete
	final TextWatcher			mTextChecker = new TextWatcher() {
		
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			((NoteItApplication) getApplication()).suggestItems(s.toString(), new OnSuggestItemsListener() {
				
				public void onPostExecute(long resultCode, ArrayList<String> suggestions,
						String message) {
					
					mAutoCompleteAdapter.clear();
					mAutoCompleteAdapter.addAll(suggestions);
//					for (String suggestion : suggestions){
//						mAutoCompleteAdapter.add(suggestion);
//					}
					mAutoCompleteAdapter.notifyDataSetChanged();
				}
			});
		}
		
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
			
		}
		
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub
			
		}
	};
	
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);
    	
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.add_edit_item_view);
        toolbar.SetTitle(getResources().getText(R.string.addedit_Title));
        
        mSpinCategories = (Spinner) findViewById(R.id.addedit_spinCategories);
        if (mSpinCategories != null) {
        	populateCategories();
        }
        
        mEditName = (EditText) findViewById(R.id.addedit_editName);
        mEditQuantity = (EditText) findViewById(R.id.addedit_txtQuantity);
        mEditCost = (EditText) findViewById(R.id.addedit_editprice);
        
        Button doneBtn = (Button) findViewById(R.id.addedit_btnDone);
        doneBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				saveItem();
			}
		});

        mAutoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, mSuggestions);
        AutoCompleteTextView mItemName = (AutoCompleteTextView) findViewById(R.id.addedit_editName);
        mItemName.addTextChangedListener(mTextChecker);
        mItemName.setAdapter(mAutoCompleteAdapter);
        
        Button continueBtn = (Button) findViewById(R.id.addedit_btnContinue);
        continueBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				saveItem();
			}
		});
        
        Button cancelBtn = (Button) findViewById(R.id.addedit_btnCancel);
        cancelBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});

        // Are we opening in the add or edit mode
        final Intent intent = getIntent();
        mIsAddItem = intent.getBooleanExtra("ADD", true);
        if (!mIsAddItem) {
        	// read details of this item from the back-end
        	mItemID = intent.getLongExtra("ITEMID", 0);
        	if (mItemID != 0){
        		((NoteItApplication)getApplication()).getItem(mItemID, new OnGetItemListener() {
					
					public void onPostExecute(long resultCode, Item item, String message) {
						// Populate the view with this data
						if (resultCode == 0) {
							populateView(item);
						} else {
							Toast.makeText(AddEditItemActivity.this, message, Toast.LENGTH_LONG).show();
						}
					}
				});
        	}
        }
    }

    protected void saveItem() {
    	try {
	    	Item item = getItemFromView();

	    	if (mIsAddItem)	{
	    		
		    	((NoteItApplication) getApplication()).addItem(item, new OnAddItemListener() {
					
					public void onPostExecute(long resultCode, Item item, String message) {
						if (resultCode == 0){
							Toast.makeText(AddEditItemActivity.this, "Item has been added", Toast.LENGTH_LONG).show();
							// Add this item to our internal list so it can be returned to the calling activity
							AddEditItemActivity.this.mNewItems.add(item);
							Intent resultIntent = new Intent();
							resultIntent.putExtra("RESULT", true);
//							resultIntent.putParcelableArrayListExtra("com.geekjamboree.noteit.items", AddEditItemActivity.this.mNewItems);
							AddEditItemActivity.this.setResult(RESULT_OK, resultIntent);
							finish();
						}
						else
							Toast.makeText(AddEditItemActivity.this, message, Toast.LENGTH_LONG).show();
					}
					});
	    	} else {
	    		
		    	((NoteItApplication) getApplication()).editItem(item.mID, item, new OnMethodExecuteListerner() {
					
					public void onPostExecute(long resultCode, String message) {
						if (resultCode == 0) {
							Toast.makeText(
								AddEditItemActivity.this, 
								getResources().getString(R.string.addedit_itemupdated), 
								Toast.LENGTH_LONG).show();
							finish();
						}
						else
							Toast.makeText(AddEditItemActivity.this, message, Toast.LENGTH_LONG).show();
					}
				});
	    	}
    	} catch (Exception e) {
    		Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
    	}
    }
    
    protected void populateCategories() {
    	ArrayList<Category> categories = ((NoteItApplication)getApplication()).getCategories();
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                this, 
                android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinCategories.setAdapter(adapter);
        mSpinCategories.setOnItemSelectedListener(
            new OnItemSelectedListener() {
                public void onItemSelected(
                        AdapterView<?> parent, View view, int position, long id) {
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        
        // Set the selection to the "Uncategorized" category  
        // which has a hard coded id = 1
		NoteItApplication app = (NoteItApplication) getApplication();
        int index = categories.indexOf(app.new Category(1, "", app.getUserID()));
        if (index >= 0)
        	mSpinCategories.setSelection(index);
    }
    
    @SuppressWarnings("unchecked")
    protected void populateView(Item item) {
    	mEditName.setText(item.mName);
    	mEditQuantity.setText(String.valueOf(item.mQuantity));
    	mEditCost.setText(String.valueOf(item.mUnitPrice));
    	ArrayAdapter<Category> adapter = (ArrayAdapter<Category>)mSpinCategories.getAdapter();
    	if (adapter != null){
    		NoteItApplication app = (NoteItApplication) getApplication();
    		int position = adapter.getPosition(app.new Category(item.mCategoryID, "", 0));
    		if (position >=0 && position < adapter.getCount())
    			mSpinCategories.setSelection(position);
    	}
    }
    
    protected Item getItemFromView() throws Exception {
    	
    	NoteItApplication app = ((NoteItApplication) getApplication());
    	Item item = app.new Item();
    	
    	int  position = mSpinCategories.getSelectedItemPosition();
    	if (position != Spinner.INVALID_POSITION) {
        	item.mCategoryID = ((Category)mSpinCategories.getItemAtPosition(position)).mID;
    	}
    	//item.mClassID =
    	item.mID = mIsAddItem ? 0 : mItemID;
    	item.mListID = (app.getCurrentShoppingListID());
    	item.mName = mEditName.getEditableText().toString();
    	if (item.mName.isEmpty())
    		throw new Exception(getResources().getString(R.string.addedit_nameblank));
   	
    	if (!mEditQuantity.getEditableText().toString().isEmpty())
    		item.mQuantity = Float.valueOf(mEditQuantity.getEditableText().toString());
    	
    	item.mUnitID = 1; // default to one unit
    	if (!mEditCost.getEditableText().toString().isEmpty())
    		item.mUnitPrice = Float.valueOf(mEditCost.getEditableText().toString());

    	return item;
    }

}
