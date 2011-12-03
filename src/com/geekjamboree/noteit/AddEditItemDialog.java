package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Item;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.OnAddItemListener;
import com.geekjamboree.noteit.NoteItApplication.OnSuggestItemsListener;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class AddEditItemDialog extends AlertDialog {
	
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
	
	public interface navigateItemsListener {

		long onPreviousItem();
		long onNextItem();
	}
	
	public AddEditItemDialog(
			Context context, 
			NoteItApplication application,
			addItemListener inListener) {   // opens the dialog in Add mode
		
		super(context, R.style.AppCustomDialog);
		mApplication = application;
		mItemID = 0;
		mIsAddItem = true;
		mListener = inListener;
		mNavigationListener = null;
	}

	public AddEditItemDialog(
			Context context, 
			NoteItApplication application,
			editItemListener inListener, // Opens the dialog in Edit mode
			navigateItemsListener inNavigationListener,
			long itemID) {
		
		super(context, R.style.AppCustomDialog);
		mApplication = application;
		mItemID = itemID;
		mIsAddItem = false;
		mListener = inListener;
		mNavigationListener = inNavigationListener;
	}

	static final int 		UNITS_GENERIC = 0;
	static final int 		UNITS_METRIC = 1;
	static final int		UNITS_IMPERIAL = 2;
	
	NoteItApplication		mApplication;
	baseListener			mListener;
	boolean					mIsAddItem = true;
	long					mItemID = 0; // Holds only for edit mode
	Item 					mOriginalItem;
	navigateItemsListener 	mNavigationListener; 

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
	
	protected class DialogFieldException extends Exception {

		private static final long serialVersionUID = 0x84586262L; // Completely Random to suppress warning

		DialogFieldException(){
		}
		
		DialogFieldException(String message){
			
			super(message);
		}
	}
	
    /* (non-Javadoc)
     * @see android.app.Dialog#onCreate(android.os.Bundle)
     */
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_item_view);
        
//        setTitle(getContext().getResources().getString(R.string.addedit_Title));
        
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
				try {
					saveItem();
					dismiss();
		    	}catch (DialogFieldException dialogException){
		    		
		    		Toast.makeText(getContext(), 
		    				dialogException.getMessage(), 
		    				Toast.LENGTH_LONG).show();
		    	}
		    	catch (Exception e) {
		    		Toast.makeText(getContext(), 
		    				getContext().getResources().getString(R.string.server_error), 
		    				Toast.LENGTH_LONG).show();
		    	}
			}
		});

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean showSuggestions = prefs.getBoolean("Show_Suggestions", true);
        
        mAutoCompleteAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_dropdown_item_1line, mSuggestions);
        AutoCompleteTextView mItemName = (AutoCompleteTextView) findViewById(R.id.addedit_editName);
        mItemName.setAdapter(mAutoCompleteAdapter);
        
        if (showSuggestions)
        	mItemName.addTextChangedListener(mTextChecker);

        Button continueBtn = (Button) findViewById(R.id.addedit_btnContinue);
        continueBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				try {
					saveItem();
					// clear contents here
					clearDialogFields();
		    	}catch (DialogFieldException dialogException){
		    		
		    		Toast.makeText(getContext(), 
		    				dialogException.getMessage(), 
		    				Toast.LENGTH_LONG).show();
		    		
		    	}
		    	catch (Exception e) {
		    		Toast.makeText(getContext(), 
		    				getContext().getResources().getString(R.string.server_error), 
		    				Toast.LENGTH_LONG).show();
	    	}
			}
		});
        
        Button cancelBtn = (Button) findViewById(R.id.addedit_btnCancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				dismiss();
			}
		});    	        
        
        ImageButton prevBtn = (ImageButton) findViewById(R.id.addedit_btnPrev);
        ImageButton nextBtn = (ImageButton) findViewById(R.id.addedit_btnNext);

        if (!mIsAddItem) {
        	
        	prevBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					
					long prevItemID = mNavigationListener.onPreviousItem(); 
					if (prevItemID > 0) {
						mItemID = prevItemID;
						doFetchAndDisplayItem(prevItemID);
					}
				}
			});
        	
        	nextBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					
					long nextItemID = mNavigationListener.onNextItem(); 
					if (nextItemID > 0) {
						mItemID = nextItemID;
						doFetchAndDisplayItem(nextItemID);
					}
				}
			});
        }
        
        if (!mIsAddItem) {
        	// In the edit mode "Add More" button does not make sense
        	continueBtn.setVisibility(View.INVISIBLE);
        } else {
        	// In the Add mode, next and prev don't make sense
        	nextBtn.setVisibility(View.INVISIBLE);
        	prevBtn.setVisibility(View.INVISIBLE);
        }
        
    	if (mIsAddItem == false && mItemID != 0){
    		doFetchAndDisplayItem(mItemID);
    	}    	
    }
    
    protected void saveItem() throws DialogFieldException {

    	final Item item = getItemFromView();

    	if (mIsAddItem)	{
    		
	    	mApplication.addItem(item, new OnAddItemListener() {
				
				public void onPostExecute(long resultCode, Item item, String message) {
					if (resultCode == 0){
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
						((editItemListener)mListener).onEditItem(mOriginalItem, item);
					}
					else
						Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
				}
			});
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
        
        // Set the selection to the "Uncategorized" category  
        // which has a hard coded id = 1
        int index = categories.indexOf(mApplication.new Category(1, "", mApplication.getUserID()));
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
    		int position = adapter.getPosition(mApplication.new Category(item.mCategoryID, "", 0));
    		if (position >=0 && position < adapter.getCount())
    			mSpinCategories.setSelection(position);
    	}
    	mEditCost.setActivated(true);
    }
    
    protected void doFetchAndDisplayItem(long itemID) {
    	
    	mItemID = itemID;
		mApplication.getItem(itemID, new NoteItApplication.OnGetItemListener() {
			
			public void onPostExecute(long resultCode, Item item, String message) {
				// Populate the view with this data
				if (resultCode == 0) {
					// Make a copy and save for later
					mOriginalItem = mApplication.new Item(item);
					populateView(item);
				} else {
					Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
				}
			}
		});    	
    }
    
    
    protected Item getItemFromView() throws DialogFieldException {
    	
    	NoteItApplication app = mApplication;
    	Item item = mApplication.new Item();
    	
    	int  position = mSpinCategories.getSelectedItemPosition();
    	if (position != Spinner.INVALID_POSITION) {
        	item.mCategoryID = ((Category)mSpinCategories.getItemAtPosition(position)).mID;
    	}
    	//item.mClassID =
    	item.mID = mIsAddItem ? 0 : mItemID;
    	item.mListID = (app.getCurrentShoppingListID());
    	item.mName = mEditName.getEditableText().toString();
    	if (item.mName.isEmpty())
    		throw new DialogFieldException(getContext().getResources().getString(R.string.addedit_nameblank));
   	
    	if (!mEditQuantity.getEditableText().toString().isEmpty())
    		item.mQuantity = Float.valueOf(mEditQuantity.getEditableText().toString());
    	
    	item.mUnitID = 1; // default to one unit
    	if (!mEditCost.getEditableText().toString().isEmpty())
    		item.mUnitPrice = Float.valueOf(mEditCost.getEditableText().toString());

    	return item;
    }
    
    void clearDialogFields() {

    	mEditName.setText("");
    	mEditQuantity.setText("");
    	mEditCost.setText("");
    	mEditName.setActivated(true);
    	mEditName.requestFocus();
		//mSpinCategories.setSelection(position);
    }
}
