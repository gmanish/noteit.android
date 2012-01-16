package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Item;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.OnSuggestItemsListener;
import com.geekjamboree.noteit.NoteItApplication.SuggestedItem;
import com.geekjamboree.noteit.NoteItApplication.Unit;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;

class AddEditItemDialog extends Dialog {
	
	public interface baseListener{
		// Just a convenience, I need to have a single pointer
		// as a member that can hold both types of listeners
	}
	
	public interface addItemListener extends baseListener {
		
		void onAddItem(NoteItApplication.Item item);
	}
	
	public interface editItemListener extends baseListener {
		
		void onEditItem(
				NoteItApplication.Item oldItem, 
				NoteItApplication.Item newItem,
				int editItemBitMask);
	}
	
	public interface navigateItemsListener {

		long onPreviousItem();
		long onNextItem();
	}
	
	public AddEditItemDialog(
			Context context, 
			NoteItApplication application,
			addItemListener inListener) {   // opens the dialog in Add mode
		
		super(context, R.style.App_CustomDialog);
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
		
		super(context, R.style.App_CustomDialog);
		mApplication = application;
		mItemID = itemID;
		mIsAddItem = false;
		mListener = inListener;
		mNavigationListener = inNavigationListener;
	}

	static final int 		UNITS_GENERIC = 0;
	static final int 		UNITS_METRIC = 1;
	static final int		UNITS_IMPERIAL = 2;
	
	NoteItApplication			mApplication;
	baseListener				mListener;
	boolean						mIsAddItem = true;
	int							mIgnoreAutoComplete;
	long						mItemID = 0; // Holds only for edit mode
	Item 						mOriginalItem;
	navigateItemsListener 		mNavigationListener; 
	
	ArrayAdapter<SuggestedItem> mAutoCompleteAdapter;
	AutoCompleteTextView 		mItemName;

	// controls
	EditText				mEditName;
	EditText				mEditQuantity;
	EditText				mEditCost;
	TextView				mTextTotal;
	Spinner					mSpinCategories;
	Spinner					mSpinUnits;
	CheckBox				mAskLater;

	final AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			if (position >= 0 && position < mAutoCompleteAdapter.getCount()) {
				mApplication.fetchPurchasedInstances(
					mAutoCompleteAdapter.getItem(position).mItemId, 
					new NoteItApplication.OnFetchItemsListener() {
						
						public void onPostExecute(
							long resultCode, 
							ArrayList<Item> items,
							String message) {
							
							if (resultCode == 0 && items != null && items.size() > 0) {
								populateView(items.get(0), Item.ITEM_ALL & ~Item.ITEM_NAME);
							}
						}
					}
				);
			}
		}
	};
	
	final View.OnKeyListener mOnKeyListener = new View.OnKeyListener() {
		
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			
			String partialName = mEditName.getEditableText().toString();
			partialName = partialName.trim();
			if (!partialName.equals("") && mIgnoreAutoComplete <= 0){
				Log.i("AddEditItemDialog.OnKeyListener", "Requesting Suggestions: " + partialName);
				mApplication.suggestItems(partialName, new OnSuggestItemsListener() {
					
					public void onPostExecute(
							long resultCode, 
							ArrayList<SuggestedItem> suggestions,
							String message) {

						try {
							mAutoCompleteAdapter.clear();
							if (resultCode == 0 && suggestions != null) {
								for (SuggestedItem suggestion : suggestions) {
									mAutoCompleteAdapter.add(suggestion);
								}
								mAutoCompleteAdapter.notifyDataSetChanged();
							}
						} finally {
							mIgnoreAutoComplete--;
						}
					}
				});
				mIgnoreAutoComplete++;
			} else {
				Log.i("AddEditItemDialog.OnKeyListener", "Ignoring Keypress, autocomplete in progress.");
			}
			return false;
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

	// [NOTE] Since we have minSDKVersion set to 7 where we don't have showDialog(int, Bundle)
	// available we're using this method to pass parameters to the Dialog. Change when we can
	// move forward. This method is called from the Activity's onPrepareDialog and hence the  
	// dialog fields are hooked up to the data members already
	public void setItemID(long id){

		mItemID = id;
    	if (mIsAddItem == false && mItemID != 0){
    		doFetchAndDisplayItem(mItemID);
    	} 
	}
	
	// [NOTE] Since we have minSDKVersion set to 7 where we don't have showDialog(int, Bundle)
	// available we're using this method to pass parameters to the Dialog. Change when we can
	// move forward. This method is called from the Activity's onPrepareDialog and hence the  
	// dialog fields are hooked up to the data members already
	public void setItem(Item item) {
		mOriginalItem = mApplication.new Item(item);
		populateView(item, Item.ITEM_ALL);
	}
	
    /* (non-Javadoc)
     * @see android.app.Dialog#onCreate(android.os.Bundle)
     */
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_item_view);

        String title; 
        if (mIsAddItem) {
        	title = getContext().getResources().getString(R.string.addedit_Title);
        }
        else {
        	title = getContext().getResources().getString(R.string.addedit_EditTitle);
        }
        
    	((TextView) findViewById(R.id.addedit_textview_caption)).setText(title);
    	final ImageButton askLater = (ImageButton) findViewById(R.id.addedit_asklater_help);
    	if (askLater != null) {
    		askLater.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					FloatingPopup.MakePopup(
						getContext(), 
						askLater, 
						getContext().getResources().getString(R.string.addedit_asklaterhelp)).show();
				}
			});
    	}
    	
        mSpinCategories = (Spinner) findViewById(R.id.addedit_spinCategories);
        if (mSpinCategories != null) {
        	populateCategories();
        }
        
        mSpinUnits = (Spinner) findViewById(R.id.addedit_units);
        if (mSpinUnits != null) {
        	populateUnits();
        }
        
        mEditName = (EditText) findViewById(R.id.addedit_editName);
        mEditQuantity = (EditText) findViewById(R.id.addedit_txtQuantity);
        mEditCost = (EditText) findViewById(R.id.addedit_editprice);
        mAskLater = (CheckBox) findViewById(R.id.addedit_AskLater);
        mTextTotal = (TextView) findViewById(R.id.addedit_labelTotal);
        mEditCost.setOnKeyListener(new View.OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				doUpdateTotal();
				return false;
			}
		});
        mEditQuantity.setOnKeyListener(new View.OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				doUpdateTotal();
				return false;
			}
		});
        
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
        
        mAutoCompleteAdapter = new ArrayAdapter<SuggestedItem>(
        	getContext(), 
        	android.R.layout.simple_dropdown_item_1line, 
        	new ArrayList<SuggestedItem>());
        AutoCompleteTextView mItemName = (AutoCompleteTextView) findViewById(R.id.addedit_editName);
        mItemName.setAdapter(mAutoCompleteAdapter);
        if (showSuggestions) {
        	mItemName.setOnKeyListener(mOnKeyListener);
        	mItemName.setOnItemClickListener(mItemClickListener);
        }

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
        	nextBtn.setVisibility(View.GONE);
        	prevBtn.setVisibility(View.GONE);
        }
        
    	if (mIsAddItem == false && mItemID != 0){
    		doFetchAndDisplayItem(mItemID);
    	} else 
    		doUpdateTotal();
    	
    }
    
    protected void saveItem() throws DialogFieldException {

    	final Item item = getItemFromView();

    	if (mIsAddItem)	{
    		((addItemListener) mListener).onAddItem(item);
    	} else {
    		int bitFlags = 0; 

    		if (!item.mName.equals(mOriginalItem.mName)) {
    			bitFlags = bitFlags | Item.ITEM_NAME;
    			// Duh! Back end needs the category ID if we change name
    			bitFlags = bitFlags | Item.ITEM_CATEGORYID;
    		}
    		
    		if (item.mQuantity != mOriginalItem.mQuantity)
    			bitFlags = bitFlags | Item.ITEM_QUANTITY;
    		
    		if (item.mUnitID != mOriginalItem.mUnitID)
    			bitFlags = bitFlags | Item.ITEM_UNITID;
    		
    		if (item.mUnitPrice != mOriginalItem.mUnitPrice)
    			bitFlags = bitFlags | Item.ITEM_UNITCOST;
    		
    		if (item.mIsAskLater != mOriginalItem.mIsAskLater)
    			bitFlags = bitFlags | Item.ITEM_ISASKLATER;
    		
    		if (item.mCategoryID != mOriginalItem.mCategoryID)
    			bitFlags = bitFlags | Item.ITEM_CATEGORYID;
    		
    		final int finalBitFlags = bitFlags;
			((editItemListener) mListener).onEditItem(mOriginalItem, item, finalBitFlags);
	    }
    }
    
    public void initDialog() {
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
        int index = categories.indexOf(mApplication.new Category(1));
        if (index >= 0)
        	mSpinCategories.setSelection(index);        
    }
    
    protected void populateUnits() {
    	
    	ArrayList<Unit>	units = mApplication.getUnits();
        ArrayAdapter<Unit> adapter = new ArrayAdapter<Unit>(
                getContext(), 
                android.R.layout.simple_spinner_item,
                units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinUnits.setAdapter(adapter);
    }
    
    @SuppressWarnings("unchecked")
    protected void populateView(Item item, int itemFlags) {
    	
		if ((itemFlags & Item.ITEM_NAME) == Item.ITEM_NAME) {
			try {
				mIgnoreAutoComplete++;
				mEditName.setText(item.mName);
	    	} finally {
	    		mIgnoreAutoComplete--;
	    	}
		}
    	
    	mEditQuantity.setText(String.valueOf(item.mQuantity));
    	mEditCost.setText(String.valueOf(item.mUnitPrice));
    	mAskLater.setChecked(item.mIsAskLater > 0);
    	
    	ArrayAdapter<Category> adapter = (ArrayAdapter<Category>)mSpinCategories.getAdapter();
    	if (adapter != null){
    		int position = adapter.getPosition(mApplication.new Category(item.mCategoryID));
    		if (position >=0 && position < adapter.getCount())
    			mSpinCategories.setSelection(position);
    	}
    	
    	ArrayAdapter<Unit> unitAdapter = (ArrayAdapter<Unit>)mSpinUnits.getAdapter();
    	if (adapter != null) {
    		int position = unitAdapter.getPosition(mApplication.new Unit(item.mUnitID, "", "", 0));
    		if (position >= 0 && position < unitAdapter.getCount())
    			mSpinUnits.setSelection(position);
    	}
    	
    	doUpdateTotal();
    	mEditName.requestFocus();
    }
    
    protected void doFetchAndDisplayItem(long itemID) {
    	
    	mItemID = itemID;
		mApplication.getItem(itemID, new NoteItApplication.OnGetItemListener() {
			
			public void onPostExecute(long resultCode, Item item, String message) {
				// Populate the view with this data
				if (resultCode == 0) {
					// Make a copy and save for later
					mOriginalItem = mApplication.new Item(item);
					populateView(item, Item.ITEM_ALL);
				} else {
					Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
					mOriginalItem = null;
				}
			}
		});    	
    }
    
    
    protected Item getItemFromView() throws DialogFieldException {
    	
    	NoteItApplication 	app = mApplication;
    	Item 				item;
    	
    	if (mOriginalItem != null) {
    		// Copy all properties from the mOriginalItem as this dialog may not
    		// set all the properties of item and we don't want the original props
    		// to be changed if they are not set at all
    		item = mApplication.new Item(mOriginalItem);
    	}
    	else 
    		item = mApplication.new Item();
    	
    	int  position = mSpinCategories.getSelectedItemPosition();
    	if (position != Spinner.INVALID_POSITION) {
        	item.mCategoryID = ((Category)mSpinCategories.getItemAtPosition(position)).mID;
    	}
    	
    	position = mSpinUnits.getSelectedItemPosition();
    	if (position != Spinner.INVALID_POSITION) {
    		item.mUnitID = ((Unit) mSpinUnits.getItemAtPosition(position)).mID;
    	}
    	
    	//item.mClassID =
    	item.mID = mIsAddItem ? 0 : mItemID;
    	item.mListID = (app.getCurrentShoppingListID());
    	item.mName = mEditName.getEditableText().toString();
    	item.mIsAskLater = mAskLater.isChecked() ? 1 : 0;
    	if (item.mName.trim().matches(""))
    		throw new DialogFieldException(getContext().getResources().getString(R.string.addedit_nameblank));
   	
    	if (!mEditQuantity.getEditableText().toString().matches(""))
    		item.mQuantity = Float.valueOf(mEditQuantity.getEditableText().toString());
    	
    	if (!mEditCost.getEditableText().toString().matches(""))
    		item.mUnitPrice = Float.valueOf(mEditCost.getEditableText().toString());

    	return item;
    }
    
    public void clearDialogFields() {

    	mEditName.setText("");
    	mEditQuantity.setText("");
    	mEditCost.setText("");
    	mEditName.requestFocus();
    }
    
    void doUpdateTotal() {
		
    	String 			strcost = mEditCost.getEditableText().toString();
		String 			strquantity = mEditQuantity.getEditableText().toString();
		float 			newValue = 0;
		float 			quantity = 0;
		final String 	format = getContext().getResources().getString(R.string.addedit_total);
		
		try {
			newValue = Float.valueOf(strcost.trim());
			quantity = Float.valueOf(strquantity.trim());
		} catch (Exception e) {
			newValue = 0;
			quantity = 0;
		}
		
		if (newValue > 0 && quantity > 0) {
			String total = String.format(format, newValue * quantity);			
			mTextTotal.setText(total);
			mTextTotal.setVisibility(View.VISIBLE);
		} else {
			mTextTotal.setVisibility(View.INVISIBLE);
		}
    }
}
