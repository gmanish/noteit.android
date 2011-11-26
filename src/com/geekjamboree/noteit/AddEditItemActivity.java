package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Item;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.OnAddItemListener;
import com.geekjamboree.noteit.NoteItApplication.OnSuggestItemsListener;
import com.geekjamboree.noteit.NoteItApplication.OnGetItemListener;

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
	
	// Entry mode is true when the user adds items details 
	// and clicks on "Continue" instead of "Done"
	boolean				mEntryMode = false;
	
	// controls
	EditText					mEditName;
	EditText					mEditQuantity;
	EditText					mEditCost;
	Spinner						spinCategories;
	ArrayList<String>			mSuggestions = new ArrayList<String>();
	ArrayAdapter<String>		mAutoCompleteAdapter;
	AutoCompleteTextView 		mItemName;
	ArrayList<Item>				mNewItems = new ArrayList<Item>();

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
        
        spinCategories = (Spinner) findViewById(R.id.addedit_spinCategories);
        if (spinCategories != null) {
        	populateCategories();
        }
        
        mEditName = (EditText) findViewById(R.id.addedit_editName);
        mEditQuantity = (EditText) findViewById(R.id.addedit_txtQuantity);
        mEditCost = (EditText) findViewById(R.id.addedit_editprice);
        
        Button doneBtn = (Button) findViewById(R.id.addedit_btnDone);
        doneBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mEntryMode = false;
				addItem();
			}
		});

        mAutoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, mSuggestions);
        AutoCompleteTextView mItemName = (AutoCompleteTextView) findViewById(R.id.addedit_editName);
        mItemName.addTextChangedListener(mTextChecker);
        mItemName.setAdapter(mAutoCompleteAdapter);
        
        Button continueBtn = (Button) findViewById(R.id.addedit_btnContinue);
        continueBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mEntryMode = true;
				addItem();
			}
		});

        // Are we opening in the add or edit mode
        final Intent intent = getIntent();
        boolean add = intent.getBooleanExtra("ADD", true);
        if (!add) {
        	// read details of this item from the back-end
        	long itemID = intent.getLongExtra("ITEMID", 0);
        	if (itemID != 0){
        		((NoteItApplication)getApplication()).getItem(itemID, new OnGetItemListener() {
					
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

    protected void addItem() {
    	Category	category = (Category)spinCategories.getSelectedItem();
    	String		itemName = mEditName.getEditableText().toString(); 
    	final Item 	newItem = new Item(
    			((NoteItApplication) getApplication()).getCurrentShoppingList(),
    			category.mID,
    			itemName);
    	
    	if (!itemName.isEmpty()){
	    	// Set the other attributes
	    	newItem.mQuantity = Float.valueOf(mEditQuantity.getEditableText().toString());
	    	newItem.mUnitPrice = Float.valueOf(mEditCost.getEditableText().toString());
	    	
	    	((NoteItApplication) getApplication()).addItem(newItem, new OnAddItemListener() {
				
				public void onPostExecute(long resultCode, Item item, String message) {
					if (resultCode == 0){
						// Add this item to our internal list so it can be returned to the calling activity
						AddEditItemActivity.this.mNewItems.add(item);
						Toast.makeText(AddEditItemActivity.this, "Item has been added", Toast.LENGTH_LONG).show();
						if (!mEntryMode){
							// We'll dismiss the Activity now
							Intent resultIntent = new Intent();
							resultIntent.putExtra("RESULT", true);
							resultIntent.putParcelableArrayListExtra("com.geekjamboree.noteit.items", AddEditItemActivity.this.mNewItems);
							AddEditItemActivity.this.setResult(RESULT_OK, resultIntent);
							finish();
						}
						else {
							// Clear the controls and continue adding
						}
					}
					else
						Toast.makeText(AddEditItemActivity.this, message, Toast.LENGTH_LONG).show();
				}
			});
    	} else {
    		// Item name cannot be left blank
    		Toast.makeText(this, getResources().getString(R.string.addedit_nameblank), Toast.LENGTH_LONG).show();
    	}
    }
    
    protected void populateCategories() {
    	ArrayList<Category> categories = ((NoteItApplication)getApplication()).getCategories();
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                this, 
                android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategories.setAdapter(adapter);
        spinCategories.setOnItemSelectedListener(
            new OnItemSelectedListener() {
                public void onItemSelected(
                        AdapterView<?> parent, View view, int position, long id) {
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
    }
    
    void populateView(Item item) {
    	mEditName.setText(item.mName);
    	mEditQuantity.setText(String.valueOf(item.mQuantity));
    	mEditCost.setText(String.valueOf(item.mUnitPrice));
    }
}
