package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Item;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.OnAddItemListener;
import com.geekjamboree.noteit.NoteItApplication.OnSuggestItemsListener;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class AddEditItemDialog extends Dialog {
	
	public interface baseListener{
		// Just a convenience, I need to have a single pointer
		// as a member that can hold both types of listeners
	}
	
	public interface addItemListener extends baseListener {
		void onAddItem(NoteItApplication.Item item);
	}
	
	public interface editItemListener extends baseListener {
		void onEditItem(NoteItApplication.Item oldItem, NoteItApplication.Item newItem);
	}
	
	public AddEditItemDialog(
			Context context, 
			NoteItApplication application,
			addItemListener inListener) { // opens the dialog in Add mode
		
		super(context);
		mApplication = application;
		mItemID = 0;
		mIsAddItem = true;
		mListener = inListener;
	}

	public AddEditItemDialog(
			Context context, 
			NoteItApplication application,
			editItemListener inListener, // Opens the dialog in Edit mode
			long itemID) {
		
		super(context);
		mApplication = application;
		mItemID = itemID;
		mIsAddItem = false;
		mListener = inListener;
	}

	static final int 		UNITS_GENERIC = 0;
	static final int 		UNITS_METRIC = 1;
	static final int		UNITS_IMPERIAL = 2;
	
	NoteItApplication		mApplication;
	baseListener			mListener;
	boolean					mIsAddItem = true;
	long					mItemID = 0; // Holds only for edit mode

	Item 					mOriginalItem;
	

	// controls
	EditText				mEditName;
	EditText				mEditQuantity;
	EditText				mEditCost;
	Spinner					mSpinCategories;
	ArrayList<String>		mSuggestions = new ArrayList<String>();
	ArrayAdapter<String>	mAutoCompleteAdapter;
	AutoCompleteTextView 	mItemName;

	// This one's for the AutoTextComplete
	final TextWatcher		mTextChecker = new TextWatcher() {
		
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mApplication.suggestItems(s.toString(), new OnSuggestItemsListener() {
				
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
	
    /* (non-Javadoc)
     * @see android.app.Dialog#onCreate(android.os.Bundle)
     */
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_item_view);
        
        mSpinCategories = (Spinner) findViewById(R.id.addedit_spinCategories);
        if (mSpinCategories != null) {
        	populateCategories();
        }
        
        mEditName = (EditText) findViewById(R.id.addedit_editName);
        mEditQuantity = (EditText) findViewById(R.id.addedit_txtQuantity);
        mEditCost = (EditText) findViewById(R.id.addedit_editprice);
        
        Button doneBtn = (Button) findViewById(R.id.addedit_btnDone);
        doneBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				saveItem();
				dismiss();
			}
		});

        mAutoCompleteAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, mSuggestions);
        AutoCompleteTextView mItemName = (AutoCompleteTextView) findViewById(R.id.addedit_editName);
        mItemName.addTextChangedListener(mTextChecker);
        mItemName.setAdapter(mAutoCompleteAdapter);
        
        Button continueBtn = (Button) findViewById(R.id.addedit_btnContinue);
        continueBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				saveItem();
				// clear contents here
			}
		});

    	if (mIsAddItem == false && mItemID != 0){
    		mApplication.getItem(mItemID, new NoteItApplication.OnGetItemListener() {
				
				public void onPostExecute(long resultCode, Item item, String message) {
					// Populate the view with this data
					if (resultCode == 0) {
						// Make a copy and save for later
						mOriginalItem = new Item(item);
						populateView(item);
					} else {
						Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
					}
				}
			});
    	}
    }
    
    protected void saveItem() {
    	try {
	    	final Item item = getItemFromView();

	    	if (mIsAddItem)	{
	    		
		    	mApplication.addItem(item, new OnAddItemListener() {
					
					public void onPostExecute(long resultCode, Item item, String message) {
						if (resultCode == 0){
							Toast.makeText(getContext(), "Item has been added", Toast.LENGTH_LONG).show();
							((addItemListener)mListener).onAddItem(item);
						}
						else
							Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
					}
					});
	    	} else {
	    		// In this case, we need to send old item details to the caller, so copy it
		    	mApplication.editItem(item.mID, item, new OnMethodExecuteListerner() {
					
					public void onPostExecute(long resultCode, String message) {
						if (resultCode == 0) {
							Toast.makeText(
								getContext(), 
								mApplication.getResources().getString(R.string.addedit_itemupdated), 
								Toast.LENGTH_LONG).show();
							((editItemListener)mListener).onEditItem(mOriginalItem, item);
						}
						else
							Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
					}
				});
	    	}
    	} catch (Exception e) {
    		Toast.makeText(getContext(), mApplication.getResources().getString(R.string.server_error), Toast.LENGTH_LONG).show();
    	}
    }
    
    protected void populateCategories() {
    	ArrayList<Category> categories = mApplication.getCategories();
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                getContext(), 
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
    }
    
    @SuppressWarnings("unchecked")
    protected void populateView(Item item) {
    	mEditName.setText(item.mName);
    	mEditQuantity.setText(String.valueOf(item.mQuantity));
    	mEditCost.setText(String.valueOf(item.mUnitPrice));
    	ArrayAdapter<Category> adapter = (ArrayAdapter<Category>)mSpinCategories.getAdapter();
    	if (adapter != null){
    		int position = adapter.getPosition(new Category(item.mCategoryID, "", 0));
    		if (position >=0 && position < adapter.getCount())
    			mSpinCategories.setSelection(position);
    	}
    }
    
    protected Item getItemFromView() throws Exception {
    	
    	NoteItApplication app = mApplication;
    	Item item = new Item();
    	
    	int  position = mSpinCategories.getSelectedItemPosition();
    	if (position != Spinner.INVALID_POSITION) {
        	item.mCategoryID = ((Category)mSpinCategories.getItemAtPosition(position)).mID;
    	}
    	//item.mClassID =
    	item.mID = mIsAddItem ? 0 : mItemID;
    	item.mListID = (app.getCurrentShoppingList());
    	item.mName = mEditName.getEditableText().toString();
    	if (item.mName.isEmpty())
    		throw new Exception(mApplication.getResources().getString(R.string.addedit_nameblank));
   	
    	if (!mEditQuantity.getEditableText().toString().isEmpty())
    		item.mQuantity = Float.valueOf(mEditQuantity.getEditableText().toString());
    	
    	item.mUnitID = 1; // default to one unit
    	if (!mEditCost.getEditableText().toString().isEmpty())
    		item.mUnitPrice = Float.valueOf(mEditCost.getEditableText().toString());

    	return item;
    }
}
