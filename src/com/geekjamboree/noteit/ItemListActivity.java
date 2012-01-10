
package com.geekjamboree.noteit;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.geekjamboree.noteit.ActionItem;
import com.geekjamboree.noteit.NoteItApplication.OnFetchItemsListener;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;
import com.geekjamboree.noteit.QuickAction;

import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.Item;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class ItemListActivity extends ExpandableListActivity implements NoteItApplication.OnFetchItemsListener {
	
	ExpandableLVRightIndicator		mListView;
	QuickAction 					mQuickAction = null;
	AtomicInteger					mSelectedGroup = new AtomicInteger();
	AtomicInteger					mSelectedChild = new AtomicInteger();
	long							mSelectedItemID = 0;
	boolean							mDisplayExtras = true;
	boolean							mDisplayCategoryExtras = true;
	Integer							mFontSize = 3;
	boolean							mHideDoneItems = false;
	CustomTitlebarWrapper			mToolbar;
	Button							mShoppingListButton;
	boolean							mIsItemListFetched = false;
	SharedPreferences				mPrefs;
	String							mCurrencyFormat = new String();
	boolean							mLoadingMore = false;
	float							mPendingTotal = 0f;
	ViewFlipper						mLoadMoreFlipper;
	LayoutInflater					mLayoutInflater;
	AlertDialog 					mInstallScanAppDialog = null;
	Item							mTempItemToPassToDialog = null;
	
	static final int ADD_ITEM_REQUEST = 0;	
	
	static final int QA_ID_EDIT 	= 0;
	static final int QA_ID_DELETE	= 1;
	static final int QA_ID_BOUGHT	= 2;
	static final int QA_ID_COPY		= 3;
	static final int QA_ID_MOVE 	= 4;
	
	static final int ITEM_FONT_LARGE 	= 0;
	static final int ITEM_FONT_MEDIUM	= 1;
	static final int ITEM_FONT_SMALL	= 2;
	
	static final int DIALOG_ADD_ITEM 	= 99;
	static final int DIALOG_EDIT_ITEM	= 100;
	
	static final String SELECTED_GROUP 			= "selGroup";
	static final String SELECTED_CHILD 			= "selChild";
	static final String SELECTED_ITEM_ID		= "selItemID";
	static final String IS_ITEMLIST_FETCHED 	= "IS_ITEM_LIST_FETCHED";
	
    protected enum ItemType {
    	PENDING, 
    	DONE, 
    	GROUP,
    	BOLD
    }
    
    protected enum ProductSearchMethod {
    	GOOGLE_SEARCH,
    	SEARCH_UPC
    }
    
	// Custom adapter for my shopping items
	public class ItemsExpandableListAdapter extends BaseExpandableListAdapter {

		private ArrayList<Category>				mCategories;

		/* mItems
		  -------------------------------------------
	  	  | Category 0   		| ArrayList<Item>	|
		  ------------------------------------------
		  | Category n 			| ArrayList<Item>	|
		  -------------------------------------------*/
		private ArrayList<ArrayList<Item>>		mItems;
		private Context							mContext = null;
		private String							mUnitPriceFormat;
		
		ItemsExpandableListAdapter(Context context){
			
			mContext = context;
			mCategories = new ArrayList<Category>();
			mItems = new ArrayList<ArrayList<Item>>();
			mUnitPriceFormat = getResources().getString(R.string.itemlist_unitpriceformat);
			for (int i = 0; i < mCategories.size(); i++){
				// Initialize the mItems ArrayList
				mItems.add(new ArrayList<Item>());
			}
		}
		
		public void AddCategory(Category category){
			
			if (!mCategories.contains(category)) {
				assert(category.mName != "");
				mCategories.add(category);
			}
		}
		
		public void AddItem(Item item, Category category){
			
			if (category != null){
				int index = mCategories.indexOf(category);
				if (index < 0){
					// Category has not been added, add it.
					mCategories.add(category);
					index = mCategories.indexOf(category);
				}
				if (index > mItems.size() - 1){
					mItems.add(new ArrayList<Item>());
				}
				mItems.get(index).add(item);
			}
		}
		
		public void DeleteItem(final Item item) {
			
			if (item != null) {
				Category category = ((NoteItApplication) getApplication()).getCategory(item.mCategoryID);
				if (category != null) {
					int categoryIndex = mCategories.indexOf(category);
					if (categoryIndex >= 0 && categoryIndex < mItems.size()) {
						mItems.get(categoryIndex).remove(item);
						// if this is the last item remove the category as well
						if (mItems.get(categoryIndex).size() == 0) { 
							mItems.remove(categoryIndex);
							mCategories.remove(category);
						}
					}
				}
			}
		}
		
		public Object getChild(int groupPosition, int childPosition) {
			
			return mItems.get(groupPosition).get(childPosition);
		}

		public long getChildId(int groupPosition, int childPosition) {
			
			return childPosition;
		}

		public Object getNextChild(int groupPosition, int childPosition) {
			
			if (childPosition < getChildrenCount(groupPosition) - 1) {
				// There are more next items in the same group
				return getChild(groupPosition, childPosition + 1);
			} else  if (groupPosition < getGroupCount() - 1)
				return getChild(groupPosition + 1, 0);
			
			return null;
		}
		
		public Object getPrevChild(int groupPosition, int childPosition) {
			
			if (childPosition > 0) {
				// There are more next items in the same group
				return getChild(groupPosition, childPosition -1);
			} else if (groupPosition > 0) {
				return getChild(
					groupPosition - 1, 	// The previous group
					getChildrenCount(groupPosition - 1) -1); // Last child
			}
			
			return null;
		}
		
		public boolean getNextChildPosition(
				AtomicInteger groupPosition, //[IN/OUT] 
				AtomicInteger childPosition) //[IN/OUT]
		{
			
			if (childPosition.get() < getChildrenCount(groupPosition.get()) - 1) {

				childPosition.set(childPosition.get() + 1);
				return true;
			} else if (groupPosition.get() < getGroupCount() - 1) {
				
				groupPosition.set(groupPosition.get() + 1);
				childPosition.set(0);
				return true;
			}
			
			return false;
		}

		public boolean getPrevChildPosition(
				AtomicInteger groupPosition, //[IN/OUT] 
				AtomicInteger childPosition) //[IN/OUT]
		{
			
			if (childPosition.get() > 0) {

				childPosition.set(childPosition.get() - 1);
				return true;
			} else if (groupPosition.get() > 0) {
				
				groupPosition.set(groupPosition.get() - 1);
				childPosition.set(getChildrenCount(groupPosition.get()) - 1);
				return true;
			}
			
			return false;
		}

		public int getTotalChildrenCount() {
			int count = 0;
			for (int index = 0; index < mCategories.size(); index++)
				count += mItems.get(index).size();
			return count;
		}
		
		public int getChildrenCount(int groupPosition) {
			
			return mItems.get(groupPosition).size();
		}

		public Object getGroup(int groupPosition) {
			
			return mCategories.get(groupPosition);
		}

		public int getGroupCount() {
			
			return mCategories.size();
		}

		public long getGroupId(int groupPosition) {
			
			return groupPosition;
		}

		public boolean hasStableIds() {
			
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			
			return true;
		}

	    public View getView(ViewGroup parent) {
	    	
	    	if (mLayoutInflater != null) {
	    		return mLayoutInflater.inflate(R.layout.listitems_item, parent, false);
	    	} else
	    		return null;
	    }
	    
		/* From Android Documentation: Makes life interesting
		 * 
		 * @param convertView the old view to reuse, if possible. You should check
		 *            that this view is non-null and of an appropriate type before
		 *            using. If it is not possible to convert this view to display
		 *            the correct data, this method can create a new view. It is not
		 *            guaranteed that the convertView will have been previously
		 *            created by
		 *            {@link #getChildView(int, int, boolean, View, ViewGroup)}.
		 */
	    public void setViewParams(View view, int height) {

	    	 ViewGroup.LayoutParams params = new AbsListView.LayoutParams(
		                ViewGroup.LayoutParams.FILL_PARENT, 
		                height);
	        
	        view.setLayoutParams(params);
	        view.setPadding(height + 10, 0, 0, 0);
	    }
	    
	    public View getChildView(
	    		int groupPosition, 
	    		int childPosition,
				boolean isLastChild, 
				View convertView, 
				ViewGroup parent) {

	        Item thisItem = (Item) getChild(groupPosition, childPosition);;
	        View view;
	        
	        if (convertView == null) {
		        view = getView(parent);
	        } else {
	        	view = convertView;
	        }
	        
	        if (view != null) {
    	        TextView				textView = (TextView) view.findViewById(R.id.itemlist_name);
    	        TextView 				quantity = (TextView) view.findViewById(R.id.itemlist_quantity);
    	        TextView 				price = (TextView) view.findViewById(R.id.itemlist_price);
    	        TextView				total = (TextView) view.findViewById(R.id.itemlist_Total);
    	        ViewGroup.LayoutParams 	params = (ViewGroup.LayoutParams) parent.getLayoutParams();
    	        
    	        if (params != null) {  
    	            params.width = ViewGroup.LayoutParams.FILL_PARENT;
    	            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
    	        } else {
    	        	params = new AbsListView.LayoutParams(
    		                ViewGroup.LayoutParams.FILL_PARENT, 
    		                ViewGroup.LayoutParams.WRAP_CONTENT);
    	        }	
    	        
    	        if (textView != null) {

			        if (thisItem.mIsPurchased > 0) {
			        	textView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			        	textView.setTextAppearance(
			        			mContext, 
			        			NoteItApplication.getPreferredTextAppearance(mContext, mFontSize, ItemType.DONE));
			        }
			        else { 
			        	textView.setTextAppearance(
			        			mContext, 
			        			NoteItApplication.getPreferredTextAppearance(mContext, mFontSize, ItemType.PENDING));
			        	textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			        }
			        
			        textView.setText(thisItem.mName.toString());
			        textView.setBackgroundColor(android.R.color.transparent);
	        	}
	        	
	        	if (mDisplayExtras && quantity != null && thisItem.mQuantity > 0 ) {
	        		NoteItApplication 	app = (NoteItApplication) getApplication();
	        		String 				unit = app.getUnitFromID(thisItem.mUnitID).mAbbreviation; 
	        		quantity.setText(String.valueOf(thisItem.mQuantity) + " " + unit); 
	        		quantity.setVisibility(View.VISIBLE);
	        		quantity.setPaintFlags(
	        			thisItem.mIsPurchased > 0 ?
	        					Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG :
	        					textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);	
		        	if (price != null && thisItem.mUnitPrice > 0){
		        		String strPrice = String.format(
		        				mUnitPriceFormat, 
		        				String.format(mCurrencyFormat, thisItem.mUnitPrice),
		        				unit);
		        		String strTotal = String.format(mCurrencyFormat, thisItem.mUnitPrice * thisItem.mQuantity);
			        	price.setPaintFlags(
			        			thisItem.mIsPurchased > 0 ?
	        					Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG :
		        				textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);	
			        	total.setPaintFlags(	        			
			        			thisItem.mIsPurchased > 0 ?
	        					Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG :
		        				textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);	
		        		price.setText(strPrice);
		        		total.setText(strTotal);
		        		price.setVisibility(View.VISIBLE);
		        		total.setVisibility(View.VISIBLE);
		        	} else {
		        		price.setVisibility(View.GONE);
		        		total.setVisibility(View.GONE);
		        	}
	        	} else {
	        		quantity.setVisibility(View.GONE);
	        		price.setVisibility(View.GONE);
	        		total.setVisibility(View.GONE);
	        	}
	        }
	        return view;
		}

	    public View getGroupView(
	    		int groupPosition, 
	    		boolean isExpanded,
				View convertView, 
				ViewGroup parent) {
	    	
	    	ViewGroup viewGroup = null;
	    	if (convertView == null) {
		    	if (mLayoutInflater != null) {
		    		viewGroup = (ViewGroup) mLayoutInflater.inflate(R.layout.listitems_group, parent, false);
		    	}
	    	} else {
	    		viewGroup = (ViewGroup) convertView;
	    	}
	    	
    		if (viewGroup != null) {
		    	TextView  	textView = (TextView) viewGroup.findViewById(R.id.itemslist_categoryName);
		    	TextView  	totals = (TextView) viewGroup.findViewById(R.id.itemslist_categoryTotals);
		    	
		        textView.setText(getGroup(groupPosition).toString());
		        textView.setTextAppearance(
		        	mContext, 
		        	NoteItApplication.getPreferredTextAppearance(
		        			mContext, mFontSize, ItemType.GROUP));

		        if (mDisplayCategoryExtras) {
			    	String 		totalsText;
			    	totalsText = "(" + getUnpurchasedChildrenCount(groupPosition) + ")";
			        totals.setText(totalsText);
			        totals.setTextAppearance(
			        	mContext, 
			        	NoteItApplication.getPreferredTextAppearance(
			        		mContext, mFontSize, ItemType.GROUP));
			        totals.setPadding(0, 0, mListView.getRightMargin(), 0);
			        totals.setVisibility(View.VISIBLE);
		        } else {
			        totals.setVisibility(View.GONE);
		        }
    		}
    		return viewGroup;
		}
	    
	    public int getUnpurchasedChildrenCount(int groupPosition) {
	    	int count = 0;
	    	for (Item item : mItems.get(groupPosition)) {
	    		count += (item.mIsPurchased <= 0 ? 1 : 0);
	    	}
	    	return count;
	    }
	}
	
	protected AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
		
		public void onScrollStateChanged(
				AbsListView view, 
				int scrollState) {
		}
		
		public void onScroll(
			AbsListView view, 
			int firstVisibleItem,
			int visibleItemCount, 
			int totalItemCount) {
		
			NoteItApplication app = (NoteItApplication) getApplication();
			if (firstVisibleItem + visibleItemCount >= totalItemCount && 
				app != null &&
				!mLoadingMore) {
				
				Log.i("ItemsListView.onScrollListener", 
						"FirstVisible: " + firstVisibleItem + 
						" VisibleItemCount: " + visibleItemCount + 
						" TotalItemsCount:" + totalItemCount);

				ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
				Log.i("ItemsListView.onScrollListener", "App Items: " + app.getItems().size());
				if (totalItemCount - mListView.getGroupCount() >= adapter.getTotalChildrenCount()) {
					if (app.isMoreItemsPending()) {
						fetchItems(ItemListActivity.this);
					}
					else
						Log.i("ItemListActivity.onScroll", "NOP: No More Pending");
				} else {
					Log.i("ItemListActivity.onScroll", "NOP: Already Have Sufficient Data");
					//doDisplayItems(app.getItems());
				}
			}
		}
	};
	
	protected SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener = 
			new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences,
					String key) {
				if (key.equals("Display_Price_Quantity") || 
						key.equals("Item_Font_Size")) {
					Log.i("ItemListActivity.onSharedPreferenceChanged", "Display_Price_Quantity preference changed");
					mDisplayExtras = sharedPreferences.getBoolean("Display_Price_Quantity", true);
					mDisplayCategoryExtras = sharedPreferences.getBoolean("Display_Category_Totals", true);
			        mFontSize = Integer.valueOf(sharedPreferences.getString("Item_Font_Size", "3"));
			        mHideDoneItems = sharedPreferences.getBoolean("Delete_Bought_Items", false);
					mListView.invalidateViews();
				}
				
			}
		};
		
	public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);

    	if (savedInstanceState != null) {
    		Log.i("ItemListActivity.onCreate", "Got a valid savedInstanceState");
    		mSelectedGroup.set(savedInstanceState.getInt(SELECTED_GROUP));
    		mSelectedChild.set(savedInstanceState.getInt(SELECTED_CHILD));
    		mSelectedItemID = savedInstanceState.getLong(SELECTED_ITEM_ID);
    		mIsItemListFetched = savedInstanceState.getBoolean(IS_ITEMLIST_FETCHED);
    	}
    	
        NoteItApplication app = (NoteItApplication) getApplication();
        if (app.getShoppingListCount() <= 0) {
        	// Send user back to the ShoppingListActivity where a list can be created
        	Intent intent = new Intent(this, ShoppingListActivity.class);
        	startActivity(intent);
        	finish();
        	return;
        }
        
        mToolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.itemlists);

        mToolbar.SetTitle(app.getShoppingList().get(app.getCurrentShoppingListIndex()).mName);
        doSetupToolbarButtons(app.getShoppingList().get(app.getCurrentShoppingListIndex()).mName);
                
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Set up Quick Actions
        ActionItem boughtItem 	= new ActionItem(
					        		QA_ID_BOUGHT, 
					        		getResources().getString(R.string.itemlistqe_bought), 
					        		getResources().getDrawable(R.drawable.tick));
        ActionItem editItem 	= new ActionItem(
									QA_ID_EDIT,
									getResources().getString(R.string.itemlistqe_edit),
									getResources().getDrawable(R.drawable.edit)); 
        ActionItem deleteItem 	= new ActionItem(
									QA_ID_DELETE,
									getResources().getString(R.string.itemlistqe_delete),
									getResources().getDrawable(R.drawable.delete));
        ActionItem copyItem 	= new ActionItem(
									QA_ID_COPY,
									getResources().getString(R.string.itemlistqe_copy),
									getResources().getDrawable(R.drawable.copy));
        ActionItem moveItem 	= new ActionItem(
									QA_ID_MOVE,
									getResources().getString(R.string.itemlistqe_move),
									getResources().getDrawable(R.drawable.move));
        

		mQuickAction = new QuickAction(this);
		mQuickAction.addActionItem(boughtItem);
		mQuickAction.addActionItem(editItem);
		mQuickAction.addActionItem(deleteItem);
		mQuickAction.addActionItem(copyItem);
		mQuickAction.addActionItem(moveItem);
		
		//setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			
			public void onItemClick(QuickAction quickAction, int pos, int actionId) {

				switch(actionId){
					case QA_ID_BOUGHT:
						doToggleMarkDone();
						break;
					case QA_ID_EDIT:
						doEditItem();
						break;
					case QA_ID_DELETE:
						doDeleteItem();
						break;
					case QA_ID_COPY:
						doCopyItem();
						break;
					case QA_ID_MOVE:
						doMoveItem();
						break;
				}
			}
		});
		
		mQuickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {			
			public void onDismiss() {
			}
		});    
		
		
        ItemsExpandableListAdapter adapter = new ItemsExpandableListAdapter(this);
		mListView = (ExpandableLVRightIndicator) getExpandableListView();
		mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE); 
		mLoadMoreFlipper = (ViewFlipper) mLayoutInflater.inflate(
			R.layout.itemlist_moreswitcher, 
			(ViewGroup) findViewById(R.id.itemlist_more_root), 
			false);
		mListView.setTextFilterEnabled(true);
//		mListView.setOnScrollListener(mScrollListener);
		if (mLoadMoreFlipper != null) {
			// Note: There seems to be a bug in android. If I don't call
			// addFooterView before setting the adapter, the footer view
			// is never added. Go Figure!
			mListView.addFooterView(mLoadMoreFlipper);
			Button moreButton = (Button) mLoadMoreFlipper.findViewById(R.id.itemlist_morebuttom);
			if (moreButton != null) {
				moreButton.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						fetchItems(ItemListActivity.this);
					}
				});
			}
		}
		mListView.setAdapter(adapter);
    	mListView.setOnChildClickListener(new OnChildClickListener() {
			
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				if (v.getId() == R.id.itemlist_name) {
					doToggleMarkDone();
				} else {
    				mQuickAction.show(v);
				}
				mSelectedGroup.set(groupPosition);
				mSelectedChild.set(childPosition);
				mSelectedItemID = ((Item) mListView.getExpandableListAdapter().getChild(groupPosition, childPosition)).mID;
				return false;
			}
		});
        
    	// Populating of the list with items is handled in OnScrollListener for the list view
		if (app.getShoppingListCount() > 0 && !mIsItemListFetched) {
			fetchItems(this);
		} else {
			Log.i("ItemListActivity.onCreate", "Skipping fetchItems");
			doDisplayItems(app.getItems());
		}
		
    	doFetchAndDisplayPendingTotal();
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.itemlist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
    public void fetchItems(OnFetchItemsListener listener) {
    	NoteItApplication app = (NoteItApplication) getApplication();
    	mToolbar.showInderminateProgress(getString(R.string.progress_message));
    	mLoadMoreFlipper.showNext();
		mLoadingMore = true;
		app.fetchItems(
			!mPrefs.getBoolean("Delete_Bought_Items", true),
				mPrefs.getBoolean("Shuffle_Done_Items", true),
				listener);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemlist_add:
			doAddItem();
			break;
		case R.id.itemlist_home:
			Intent intent = new Intent(ItemListActivity.this, DashBoardActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			break;
		case R.id.itemlist_expandall:
			doExpandAll();
			break;
		case R.id.itemlist_collapseall:
			doCollapseAll();
			break;
		case R.id.itemlist_email:
			doEmail();
			break;
		case R.id.itemlist_alldone:
			doAllDone();
			break;
		case R.id.itemlist_settings:
			startActivity(new Intent(this, MainPreferenceActivity.class));
			break;
		case R.id.itemlist_scan:
			doScanBarcode();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_GROUP, mSelectedGroup.get());
		outState.putInt(SELECTED_CHILD, mSelectedChild.get());
		outState.putBoolean(IS_ITEMLIST_FETCHED, mIsItemListFetched);
		Item selItem = null;
		ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
		if (mSelectedGroup.get() < adapter.getGroupCount() && 
			mSelectedChild.get() < adapter.getChildrenCount(mSelectedGroup.get())) {
			selItem = (Item) mListView.getExpandableListAdapter().getChild(
									mSelectedGroup.get(), mSelectedChild.get());
		}
		if (selItem != null) {
			outState.putLong(SELECTED_ITEM_ID, selItem.mID);
		} else {
			outState.putLong(SELECTED_ITEM_ID, 0);
		}
	}

	@Override
	protected void onPause() {
		SharedPreferences.Editor editor = mPrefs.edit(); 
		editor.putLong("LastUsedShoppingListID", ((NoteItApplication)getApplication()).getCurrentShoppingListID());
		editor.commit();
		mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
		if (mInstallScanAppDialog != null && mInstallScanAppDialog.isShowing())
			mInstallScanAppDialog.dismiss();
		super.onPause();
	}

	@Override
	protected void onResume() {

		mPrefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
		mDisplayExtras = mPrefs.getBoolean("Display_Price_Quantity", true);
		mDisplayCategoryExtras = mPrefs.getBoolean("Display_Category_Totals", true);
		mHideDoneItems = mPrefs.getBoolean("Delete_Bought_Items", false);
        mFontSize = Integer.valueOf(mPrefs.getString("Item_Font_Size", "3"));
		mListView.invalidateViews();
		mCurrencyFormat = ((NoteItApplication) getApplication()).getCurrencyFormat(false);
		doDisplayPendingTotal();
		super.onResume();
	}

	public void onPostExecute(long retval, ArrayList<Item> items, String message) {
    	
    	try {
    		mToolbar.hideIndeterminateProgress();
			mLoadMoreFlipper.showPrevious();
	    	if (retval == 0) {
	        	mIsItemListFetched = true;
	        	doDisplayItems(items);
				NoteItApplication app = (NoteItApplication) getApplication();
				if (app != null) { 
					if (mListView.getFooterViewsCount() > 0) {
						if  (!app.isMoreItemsPending()) {
							mListView.removeFooterView(mLoadMoreFlipper);
						}
					} else {
						if (app.isMoreItemsPending()){
							mListView.addFooterView(mLoadMoreFlipper);
						}
					}
				}
	    	}
	    	else {
				Toast.makeText(
					getApplicationContext(), 
					"The server seems to be out of its mind. Please try later.", 
					Toast.LENGTH_SHORT).show();
	    	}
    	} catch (Exception e) {
    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
    	}
		if (mLoadingMore) 
			mLoadingMore = false;
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	Log.i("ItemListActivity.onActivityResult", "requestCode:" + requestCode + " resultCode: " + resultCode);
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    	if (scanResult != null) {
    		// handle scan result
    		NoteItApplication app = (NoteItApplication) getApplication();
    		app.searchItemByBarcode(scanResult.getFormatName(), scanResult.getContents(), null);
    	} else if (requestCode == ADD_ITEM_REQUEST && resultCode == RESULT_OK) {
    		
    		// refresh our view
    		ArrayList<Item> addedItems = new ArrayList<Item>();
    		if (addedItems.size() > 0) {
    	
    			// New items were added by the called activity, we need to add them to our view
    			ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)mListView.getExpandableListAdapter();
    			for (Item item : addedItems) {
    				Category category = ((NoteItApplication) getApplication()).getCategory(item.mCategoryID);
    				adapter.AddItem(item, category);
    			}
				adapter.notifyDataSetChanged();
    		}
    	}
    }
    
    protected void setUpQuickActions() {
	}
    
    protected void doAddItem() {
    	showDialog(DIALOG_ADD_ITEM);
    }
    
    protected void doEditItem() {
    	showDialog(DIALOG_EDIT_ITEM);
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {
    	
    	switch (id) {
    	case DIALOG_ADD_ITEM:
    		Log.i("ItemListActivity.onCreateDailog", "DIALOG_EDIT_ITEM");
        	AddEditItemDialog addDialog = new AddEditItemDialog(
    				this,
    				(NoteItApplication)getApplication(),
    				new AddEditItemDialog.addItemListener() {
    					
    					public void onAddItem(Item item) {
    		    			// New items were added by the called activity, we need to add them to our view
    		    			ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)mListView.getExpandableListAdapter();
    	    				Category category = ((NoteItApplication) getApplication()).getCategory(item.mCategoryID);
    	    				adapter.AddItem(item, category);
    						adapter.notifyDataSetChanged();
    						doAddToPendingTotal(item.mUnitID * item.mUnitPrice);
    						doDisplayPendingTotal();
    					}
    				});
    		return addDialog;
    		
    	case DIALOG_EDIT_ITEM:
    		Log.i("ItemListActivity.onCreateDailog", "DIALOG_EDIT_ITEM");
    		AddEditItemDialog editDialog = new AddEditItemDialog(
    			this, 
    			(NoteItApplication)getApplication(),
    			new AddEditItemDialog.editItemListener() {
    				
    				public void onEditItem(Item oldItem, Item newItem, int bitMask) {
    	    			
    	    			ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)mListView.getExpandableListAdapter();
        				Category category = ((NoteItApplication) getApplication()).getCategory(newItem.mCategoryID);
        				if ((bitMask & Item.ITEM_CATEGORYID) > 0 || (bitMask & Item.ITEM_NAME) > 0) {
    	    				adapter.DeleteItem(oldItem);
    	    				adapter.AddItem(newItem, category);
    	    				doAddToPendingTotal(newItem.mUnitPrice * newItem.mQuantity - oldItem.mUnitPrice * oldItem.mQuantity);
    	    				doDisplayPendingTotal();
        				} else {
        					Item selItem = (Item) mListView.getExpandableListAdapter().getChild(
        							mSelectedGroup.get(), 
        							mSelectedChild.get());
        					selItem.copyFrom(newItem);
        				}
    					adapter.notifyDataSetChanged();
    				}
    			},
    			new AddEditItemDialog.navigateItemsListener() {
    				
    				public long onPreviousItem() {
    					
    					ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
    					if (adapter.getPrevChildPosition(mSelectedGroup, mSelectedChild)) {
    						
    						Item prevItem = (Item) adapter.getChild(mSelectedGroup.get(), mSelectedChild.get());
    						if (prevItem != null) {
    							return mSelectedItemID = prevItem.mID;
    						}
    					}

    					return 0;
    				}
    				
    				public long onNextItem() {
    					
    					ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
    					if (adapter.getNextChildPosition(mSelectedGroup, mSelectedChild)) {
    						
    						Item nextItem = (Item) adapter.getChild(mSelectedGroup.get(), mSelectedChild.get());
    						if (nextItem != null) {
    							return mSelectedItemID = nextItem.mID;
    						}
    					}
    					
    					return 0;
    				}
    			},
    			mSelectedItemID);
    		
    		return editDialog;
    		
    	default:
    		return super.onCreateDialog(id);
    	}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case DIALOG_ADD_ITEM:
			AddEditItemDialog addDialog = (AddEditItemDialog) dialog;
			addDialog.clearDialogFields();
			if (mTempItemToPassToDialog != null) {
				addDialog.setItem(mTempItemToPassToDialog);
			}
			break;
		case DIALOG_EDIT_ITEM:
        	AddEditItemDialog editDialog = (AddEditItemDialog) dialog;
        	editDialog.setItemID(mSelectedItemID);
			break;
		}
	}

	protected void doDeleteItem() {
    	
    	final Item selItem = (Item) mListView.getExpandableListAdapter().getChild(mSelectedGroup.get(), mSelectedChild.get());
    	if (selItem != null) {
    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
    		((NoteItApplication) getApplication()).deleteItem(selItem.mID, new OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					try {
						if (resultCode == 0) {
							// Remove the item from our adapter
							ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)mListView.getExpandableListAdapter();
							adapter.DeleteItem(selItem);
							adapter.notifyDataSetChanged();
							doDeductFromPendingTotal(selItem.mUnitPrice * selItem.mQuantity);
							doDisplayPendingTotal();
						} else
							Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_LONG).show();
					} finally {
						mToolbar.hideIndeterminateProgress();
					}
				}
			});
    	}
    }

	protected void doCopyItem() {
		
		final ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
		if (mSelectedGroup.get() < adapter.getGroupCount() &&
		    mSelectedGroup.get() >= 0 &&
		    mSelectedChild.get() < adapter.getChildrenCount(mSelectedGroup.get()) &&
		    mSelectedChild.get() >= 0) {
			
	    	final NoteItApplication 	app = (NoteItApplication) getApplication();
	    	ArrayList<ShoppingList> 	shoppingList = app.getShoppingList();
	    	ArrayAdapter<ShoppingList> 	shopListAdapter = new ArrayAdapter<ShoppingList>(
				this, 
				android.R.layout.simple_dropdown_item_1line,
				shoppingList);
	    	AlertDialog shoppingLists = new AlertDialog.Builder(this)
	    		.setTitle(getResources().getString(R.string.itemlist_copytolist))
	    		.setAdapter(shopListAdapter, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {

						final Item selItem = (Item) adapter.getChild(
								mSelectedGroup.get(), 
								mSelectedChild.get());
				    	ShoppingList targetList = app.getShoppingList().get(which);
				    	
						if (selItem != null && 
				    			targetList.mID != selItem.mListID) {
				    		
				    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
				    		app.copyItem(
			    				selItem.mID,
			    				targetList.mID,
			        			new OnMethodExecuteListerner() {
			    					public void onPostExecute(long resultCode, String message) {
			    						try {
				    						if (resultCode == 0) {
				    							Toast.makeText(
				    								ItemListActivity.this, 
				    								getResources().getString(R.string.itemlist_copytolistsuccess), 
				    								Toast.LENGTH_SHORT).show();	
				    						}
				    						else
				    							Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_SHORT).show();
			    						} finally {
			    							mToolbar.hideIndeterminateProgress();
			    						}
			    					}
			    				});
				    	}
					}
				})
				.create();
	    	shoppingLists.show();
		}
	}
	
	protected void doMoveItem() {
		
		final ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter )mListView.getExpandableListAdapter();
		if (mSelectedGroup.get() < adapter.getGroupCount() &&
		    mSelectedGroup.get() >= 0 &&
		    mSelectedChild.get() < adapter.getChildrenCount(mSelectedGroup.get()) &&
		    mSelectedChild.get() >= 0) {
			
	    	final NoteItApplication 	app = (NoteItApplication) getApplication();
	    	ArrayList<ShoppingList> 	shoppingList = app.getShoppingList();
	    	ArrayAdapter<ShoppingList> 	shopListadapter = new ArrayAdapter<ShoppingList>(
				this, 
				android.R.layout.simple_dropdown_item_1line,
				shoppingList);
	    	AlertDialog shoppingLists = new AlertDialog.Builder(this)
	    		.setTitle(getResources().getString(R.string.itemlist_movetolist))
	    		.setAdapter(shopListadapter, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {

						final Item selItem = (Item) adapter.getChild(
								mSelectedGroup.get(), 
								mSelectedChild.get());
				    	ShoppingList targetList = app.getShoppingList().get(which);
				    	
						if (selItem != null && 
				    			targetList.mID != selItem.mListID) {
				    		
				    		final int	editBitmask = Item.ITEM_LISTID;
				        	final Item	newItem = app.new Item(selItem);
				        	newItem.mListID = targetList.mID;
				    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
				    		app.editItem(
				    				editBitmask, 
				    				newItem, 
				        			new OnMethodExecuteListerner() {
				    					public void onPostExecute(long resultCode, String message) {
				    						try {
					    						if (resultCode == 0) {
					    							adapter.DeleteItem(selItem);
					    							adapter.notifyDataSetChanged();
					    						}
					    						else
					    							Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_LONG).show();
				    						} finally {
				    				    		mToolbar.hideIndeterminateProgress();
				    						}
				    					}
				    			});
				    	}
					}
				})
				.create();
	    	shoppingLists.show();
		}
	}
	
    void doToggleMarkDone() {
    	
    	final Item selItem = (Item) mListView.getExpandableListAdapter().getChild(mSelectedGroup.get(), mSelectedChild.get());
    	if (selItem != null){
    		
    		if (selItem.mIsPurchased <= 0 && selItem.mIsAskLater > 0) {
    			// Item is being marked done and "Ask Later" is checked
    			doAskForPriceAndSave(selItem);
    		} else {
    			doCommitToggleItemDone(selItem, false, 0);
    		}
    	}
    }
    
    protected void doExpandPending() {
    	ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
    	for (int i = 0; i < adapter.getGroupCount(); i++){
    		if (adapter.getUnpurchasedChildrenCount(i) > 0)
    			mListView.expandGroup(i);
    	}        	
    }
    
    protected void doExpandAll() {
    	ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
    	for (int i = 0; i < adapter.getGroupCount(); i++){
    		mListView.expandGroup(i);
    	}        	
    }
    
    protected void doCollapseAll() {
    	ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
    	for (int i = 0; i < adapter.getGroupCount(); i++){
    		mListView.collapseGroup(i);
    	}        	
    	
    }
    
    protected void doSetupToolbarButtons(String listName) {

    	ImageButton homeButton = new ImageButton(this);
    	homeButton.setImageResource(R.drawable.home);
    	mToolbar.addLeftAlignedButton(homeButton, false, true);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(ItemListActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
    	
    	mShoppingListButton = new Button(this);
    	mShoppingListButton.setText(listName);
    	mToolbar.addCenterFillButton(mShoppingListButton);
    	mShoppingListButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doDisplayShoppingLists();
			}
		});
		
    	ImageButton addButton = new ImageButton(this);
    	addButton.setImageResource(R.drawable.add);
    	mToolbar.addRightAlignedButton(addButton, true, false);
    	addButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doAddItem();
			}
		});
    	
    	ImageButton expandAll = new ImageButton(this);
    	expandAll.setImageResource(R.drawable.down);
    	mToolbar.addRightAlignedButton(expandAll, true, false);
    	expandAll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doExpandAll();
			}
		});
    	
    	ImageButton collapseAll = new ImageButton(this);
    	collapseAll.setImageResource(R.drawable.up);
    	mToolbar.addRightAlignedButton(collapseAll, true, false);
    	collapseAll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doCollapseAll();
			}
		});
    }
    
    protected float doAskForPriceAndSave(final Item item) {

		// inflate the view from resource layout
		final View dialogView = mLayoutInflater.inflate(R.layout.dialog_addshoppinglist, (ViewGroup) findViewById(R.id.dialog_addshoppinglist_root));
		String text = String.format(
				getResources().getString(R.string.addedit_askLaterPrompt), 
				item.mQuantity,
				((NoteItApplication) getApplication()).getUnitFromID(item.mUnitID).mAbbreviation, 
				item.mName);
		
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setView(dialogView)
			.setTitle(getResources().getString(R.string.addedit_asklater_price))
			.setCancelable(false)
			.create();
    	
    	dialog.setMessage(text);
		dialog.setOwnerActivity(this);
		final EditText editPrice = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
		dialog.setButton(
				DialogInterface.BUTTON1, 
				getResources().getString(R.string.OK), 
				new DialogInterface.OnClickListener() {
		
			public void onClick(DialogInterface dialog, int which) {
				if (editPrice != null) {
					doCommitToggleItemDone(item, true, Float.valueOf(editPrice.getText().toString()));
				} else {
					doCommitToggleItemDone(item, true, 0);
				}
			}
		});
	
		dialog.setButton(
				DialogInterface.BUTTON2, 
				getResources().getString(R.string.Skip), 
				new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				// Even if the user cancels the Price dialog, we still need to toggle the done state
				doCommitToggleItemDone(item, false, 0);
			}
		});
		if (editPrice != null) {
			editPrice.setSelectAllOnFocus(true);
			editPrice.setText(String.valueOf(item.mUnitPrice));
			editPrice.requestFocus();
		}
		dialog.show();
		return 0;
	}
    
	// Send in the item as is without any changes
    protected void doCommitToggleItemDone(final Item item, final boolean resetAskLater, final float newPrice) {
    	
    	final NoteItApplication 	app = (NoteItApplication) getApplication();
    	final Item 					newItem = app.new Item(item);
    	int							editBitmask = Item.ITEM_ISPURCHASED;
    	
    	newItem.mIsPurchased = newItem.mIsPurchased > 0 ? 0 : 1;

    	if (resetAskLater) {
			newItem.mIsAskLater = 0;
			editBitmask = editBitmask | Item.ITEM_ISASKLATER;
    	}
    	
    	if (newPrice > 0) {
    		editBitmask = editBitmask | Item.ITEM_UNITCOST;
    		if (newItem.mQuantity > 0)
    			newItem.mUnitPrice = newPrice / newItem.mQuantity;
    		else
    			newItem.mUnitPrice = newPrice;
    	}
    	
    	mToolbar.showInderminateProgress(getString(R.string.progress_message));
		app.editItem(
				editBitmask, 
    			newItem, 
    			new OnMethodExecuteListerner() {
				
					public void onPostExecute(long resultCode, String message) {
						
						try {
							if (resultCode == 0) {
								item.mIsPurchased = newItem.mIsPurchased;
								item.mIsAskLater = newItem.mIsAskLater;
								item.mUnitPrice = newItem.mUnitPrice; 
								ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)mListView.getExpandableListAdapter();
								if (item.mIsPurchased > 0 && mPrefs.getBoolean("Shuffle_Done_Items", true)) {
									adapter.DeleteItem(item);
									adapter.AddItem(item, app.getCategory(item.mCategoryID));
								}
								if (item.mIsPurchased > 0)
									doDeductFromPendingTotal(item.mUnitPrice * item.mQuantity);
								else
									doAddToPendingTotal(item.mUnitPrice * item.mQuantity);
								doDisplayPendingTotal();
								adapter.notifyDataSetChanged();
							}
							else
								Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_LONG).show();
						} finally {
							mToolbar.hideIndeterminateProgress();
						}
					}
			});
    }
    
    protected void doDisplayShoppingLists() {
    	
    	final NoteItApplication app = (NoteItApplication) getApplication();
    	
    	ArrayList<ShoppingList> shoppingList = app.getShoppingList();
    	ArrayAdapter<ShoppingList> adapter = new ArrayAdapter<ShoppingList>(
    			this, 
    			android.R.layout.simple_dropdown_item_1line,
    			shoppingList);
    	AlertDialog shoppingLists = new AlertDialog.Builder(this)
    		.setTitle(R.string.itemlist_select_shoppinglist)
    		.setAdapter(adapter, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {

					ShoppingList shoppingList = app.getShoppingList(which);
					if (shoppingList != null) {
						mShoppingListButton.setText(shoppingList.mName);
					}
					app.setCurrentShoppingListIndex(which);
					mIsItemListFetched = false;
					ItemsExpandableListAdapter adapter;
					if ((adapter = new ItemsExpandableListAdapter(ItemListActivity.this)) != null) {
						// Note: There seems to be a bug in android. If I don't call
						// addFooterView before setting the adapter, the footer view
						// is never added. Go Figure!
						mListView.addFooterView(mLoadMoreFlipper);
						mListView.setAdapter(adapter);
					}
					fetchItems(ItemListActivity.this);
					doFetchAndDisplayPendingTotal();
				}
			})
			.create();
    	shoppingLists.show();
    }
    
    protected void doDisplayItems(ArrayList<Item> items) {
		
    	ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
    	for (int index = 0; index < items.size(); index++){
    		Item 	thisItem = items.get(index);
    		if (thisItem.mIsPurchased <= 0 || ((thisItem.mIsPurchased > 0) && !mHideDoneItems)) {
	    		NoteItApplication.Category category = ((NoteItApplication)getApplication()).getCategory(thisItem.mCategoryID);
	    		adapter.AddItem(thisItem, category);
    		}
    	}
    	adapter.notifyDataSetChanged();
    	Log.i("NoteItApplication.doDisplayItems", "Notified Adapter of change");
		doExpandPending();
    }
    
    void doFetchAndDisplayPendingTotal() {
    	NoteItApplication app = (NoteItApplication) getApplication();
    	app.getPendingTotal(
    		app.getCurrentShoppingListID(), 
    		new NoteItApplication.OnGetPendingTotalListener() {
			
			public void onPostExecute(long resultCode, float total, String message) {
				if (resultCode == 0) {
					mPendingTotal = total;
					doDisplayPendingTotal();
				}
				else {
					mPendingTotal = 0f;
					doDisplayPendingTotal();
				}
			}
		});
    }
    
    void doAddToPendingTotal(float add) {
    	mPendingTotal += add;
    }
    
    void doDeductFromPendingTotal(float deduct) {
    	mPendingTotal -= deduct;
    }
    
    void doDisplayPendingTotal() {
		final String 	remainingFormat = getResources().getString(R.string.itemlist_remaining);
		LinearLayout 	statusBar = (LinearLayout) findViewById(R.id.bottom_bar);

		if (statusBar != null && mPendingTotal > 0) {
			statusBar.setVisibility(View.VISIBLE);

			float taxes = 0f;
			try {
				taxes = Float.valueOf(mPrefs.getString("taxes", "0"));
			} catch (Exception e) {
			}
			
			float total = mPendingTotal;
			if (taxes > 0) {
				total += (taxes / 100f) * total;
			}
			
			String strRemaining = String.format(remainingFormat, String.format(mCurrencyFormat, total));
			TextView textViewRemaining = (TextView) statusBar.findViewById(R.id.bottom_remaining);
			if (textViewRemaining != null) 
				textViewRemaining.setText(strRemaining);
			
		} else if (statusBar != null && mPendingTotal <= 0) {
			statusBar.setVisibility(View.GONE);
		}
    }
        
    protected void doEmail() {
    	NoteItApplication	app = (NoteItApplication) getApplication();
    	final Intent 		emailIntent = new Intent(Intent.ACTION_SEND);
    	emailIntent.setType("text/plain");
    	emailIntent.putExtra(
    			Intent.EXTRA_SUBJECT, 
    			app.getShoppingList().get(app.getCurrentShoppingListIndex()).mName);
    	emailIntent.putExtra(Intent.EXTRA_TEXT, formatItemsList());
    	startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.itemlist_emailPrompt)));
    }

    protected void doAllDone() {
    	NoteItApplication app = (NoteItApplication) getApplication();
    	if (app != null) {
    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
    		app.markAllItemsDone(app.getCurrentShoppingListID(), true, new OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					try {
						if (resultCode == 0) {
							ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) mListView.getExpandableListAdapter();
							for (int group = 0; group < adapter.getGroupCount(); group++)
								for (int child = 0; child < adapter.getChildrenCount(group); child++)
								{
									Item item = (Item) adapter.getChild(group, child);
									if (item != null && item.mIsPurchased <= 0) {
										item.mIsPurchased = 1;
									}
								}
							adapter.notifyDataSetChanged();
						} else
							Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_LONG).show();
					} finally {
						mToolbar.hideIndeterminateProgress();
					}
				}
			});
    	}
    }
    
    protected String formatItemsList() {
    	NoteItApplication app = (NoteItApplication) getApplication();
    	String str = getResources().getString(R.string.itemlist_emailIntro) + "\n\n";
    	for (Item item : app.getItems()) {
    		str += item.mName;
    		if (item.mQuantity > 0) {
    			str += ", " + item.mQuantity;
    			str += " " + app.getUnitFromID(item.mUnitID);
    		}
    		if (item.mUnitPrice > 0)
    			str += ", " + item.mUnitPrice;
    		str += "\n";
    	}

    	str += "\n\n";
    	str += getResources().getString(R.string.itemlist_emailsig);		
    	return str;
    }
    
    protected void doScanBarcode() {
    	
    	final NoteItApplication app = (NoteItApplication) getApplication();
    	app.searchItemByBarcode("", "602527246949", new NoteItApplication.OnSearchBarcodeListener() {
			
			public void onSearchResults(long retVal, Item item, String message) {
				if (retVal == 0) {
					// Item found
				} else {
					// Not Found
				}
				mTempItemToPassToDialog = app.new Item(item);
				mTempItemToPassToDialog.mBarcode = "602527246949";
				mTempItemToPassToDialog.mBarcodeFormat = "";
				showDialog(DIALOG_ADD_ITEM);
			}
		});
//    	IntentIntegrator integrator = new IntentIntegrator(this);
//    	mInstallScanAppDialog = integrator.initiateScan();
    }
}
