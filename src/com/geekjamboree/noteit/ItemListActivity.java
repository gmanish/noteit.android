 package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.ActionItem;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;
import com.geekjamboree.noteit.QuickAction;
//import net.londatiga.android.R;

import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.Item;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class ItemListActivity extends ExpandableListActivity implements NoteItApplication.OnFetchItemsListener {
	
	ExpandableListView			mListView;
	ItemsExpandableListAdapter 	mAdapter;
	ProgressDialog				mProgressDialog = null;
	QuickAction 				mQuickAction = null;
	int							mSelectedGroup = 0;
	int 						mSelectedChild = 0;
	
	static final int ADD_ITEM_REQUEST = 0;	
	
	static final int QA_ID_EDIT 	= 0;
	static final int QA_ID_DELETE	= 1;
	static final int QA_ID_BOUGHT	= 2;
	
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
		
		ItemsExpandableListAdapter(Context context){
			
			mContext = context;
			mCategories = new ArrayList<Category>();
			mItems = new ArrayList<ArrayList<Item>>();
			
			for (int i = 0; i < mCategories.size(); i++){
				// Initialize the mItems arraylist
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

	    public TextView getGenericView(final int itemHeight) {

	        TextView textView = new TextView(mContext);
	        return textView;
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
	    public void setViewParams(TextView view, int height) {

	        // Layout parameters for the ExpandableListView
	        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
	                ViewGroup.LayoutParams.MATCH_PARENT, 	// Width 
	                height); 									// Height
	        view.setLayoutParams(lp);
	        view.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
	        view.setPadding(height + 10, 0, 0, 0);
	    }
	    
	    public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

	    	TextView 	textView;
	        final int 	childHeight = 32;
	        Item 		thisItem = (Item) getChild(groupPosition, childPosition);

	        if (convertView != null)
	        	textView = (TextView) convertView;
	        else
	        	textView = getGenericView(childHeight);
	        setViewParams(textView, childHeight);
	        if (thisItem.mIsPurchased > 0)
	        	textView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
	        else 
	        	textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
	        
	        textView.setText(getChild(groupPosition, childPosition).toString());
	        // Set the background to transparent
	        textView.setBackgroundColor(android.R.color.transparent);
	        textView.setTextAppearance(mContext, R.style.ListView_GroupTextAppearance);
	        
	        return textView;
		}

	    public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
	    	
	    	TextView	textView;	
	    	final int 	groupHeight = 32;
	    	
	    	if (convertView != null)
	    		textView = (TextView) convertView;
	    	else
	    		textView = getGenericView(groupHeight);
	    	
	    	setViewParams(textView, groupHeight);
	        textView.setText(getGroup(groupPosition).toString());
	        // Set the background to transparent
	        textView.setBackgroundResource(R.color.listitem_group_background);
	        textView.setTextAppearance(mContext, R.style.ListView_GroupTextAppearance);
	        return textView;
		}
	}
	
	public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);

        mProgressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.progress_message));
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.itemlists);
        toolbar.SetTitle(getResources().getText(R.string.itemlistactivity_title));
        
        mListView = (ExpandableListView) findViewById(android.R.id.list);
        
        // The add button in the toolbar
        ImageButton btnAdd = (ImageButton) findViewById(R.id.itemlist_add);
        btnAdd.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doAddItem(true);
			}
		});
        
        ImageButton expandAll = (ImageButton) findViewById(R.id.itemlist_expandall);
        expandAll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doExpandAll();
			}
		});
        
        ImageButton collapseAll = (ImageButton) findViewById(R.id.itemlist_collapseall);
        collapseAll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doCollapseAll();
			}
		});
        
        // Set up Quick Actions
        ActionItem boughtItem 	= new ActionItem(
						        		QA_ID_BOUGHT, 
						        		getResources().getString(R.string.itemlistqe_bought), 
						        		getResources().getDrawable(R.drawable.ok));
		ActionItem editItem		= new ActionItem(
										QA_ID_EDIT,
										getResources().getString(R.string.itemlistqe_edit),
										getResources().getDrawable(R.drawable.edit)); 
		ActionItem deleteItem	= new ActionItem(
										QA_ID_DELETE,
										getResources().getString(R.string.itemlistqe_delete),
										getResources().getDrawable(R.drawable.delete));
		
        mQuickAction = new QuickAction(this);
		mQuickAction.addActionItem(boughtItem);
		mQuickAction.addActionItem(editItem);
		mQuickAction.addActionItem(deleteItem);
		
		//setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			public void onItemClick(QuickAction quickAction, int pos, int actionId) {
//				ActionItem actionItem = quickAction.getActionItem(pos);
				switch(actionId){
					case QA_ID_BOUGHT:
						doToggleMarkDone();
						break;
					case QA_ID_EDIT:
						doEditItem(true);
						break;
					case QA_ID_DELETE:
						doDeleteItem();
						break;
				}
				if (actionId == QA_ID_BOUGHT) { 
				} else if (actionId == QA_ID_EDIT ) {
				}
			}
		});
		
		//setup on dismiss listener, set the icon back to normal
		mQuickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {			
			public void onDismiss() {
//				mMoreIv.setImageResource(R.drawable.ic_list_more);
			}
		});    
        	
 	   	((NoteItApplication)getApplication()).fetchItems(this);
    }

    public void onPostExecute(long retval, ArrayList<Item> items, String message) {
    	
    	if (mProgressDialog != null) mProgressDialog.dismiss();
    	
    	if (retval == 0) {
        	
    		mAdapter = new ItemsExpandableListAdapter(this);
        	
        	for (int index = 0; index < items.size(); index++){
        		
        		Item thisItem = items.get(index);
        		
        		// Get a reference to the parent Category
        		NoteItApplication.Category category = ((NoteItApplication)getApplication()).getCategory(thisItem.mCategoryID);
        		
        		mAdapter.AddItem(thisItem, category);
        	}

    		((ExpandableListView)mListView).setAdapter(mAdapter);
    		mListView.setTextFilterEnabled(true);
        	
        	mListView.setOnChildClickListener(new OnChildClickListener() {
				
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					
					mSelectedGroup = groupPosition;
					mSelectedChild = childPosition;
//					String itemText = (String)mListView.getExpandableListAdapter().getChild(groupPosition, childPosition).toString();
    				mQuickAction.show(v);
//        			Toast.makeText(getApplicationContext(), itemText, Toast.LENGTH_SHORT).show();
					return false;
				}
			});        	
    	}
    	else {
			Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
    	}
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	Log.i("ItemListActivity.onActivityResult", "requestCode:" + requestCode + " resultCode: " + resultCode);
    	if (requestCode == ADD_ITEM_REQUEST && resultCode == RESULT_OK) {
    		
    		// refresh our view
    		ArrayList<Item> addedItems = new ArrayList<Item>();
//    		addedItems = data.getParcelableArrayListExtra("com.geekjamboree.noteit.items");
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
    
    protected void doAddItem(boolean doDialog) {
    	
    	if (doDialog == false) {
			Intent intent = new Intent(ItemListActivity.this, AddEditItemActivity.class);
			intent.putExtra("ADD", true);
			startActivityForResult(intent, ADD_ITEM_REQUEST);
    	} else {
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
					}
				});
	    	addDialog.show();
    	}
    }
    
    protected void doEditItem(boolean doDialog) {
    	
    	if (doDialog == true) {
	    	Item selItem = (Item) mListView.getExpandableListAdapter().getChild(mSelectedGroup, mSelectedChild);
			AddEditItemDialog addDialog = new AddEditItemDialog(
					this, 
					(NoteItApplication)getApplication(),
    				new AddEditItemDialog.editItemListener() {
						
						public void onEditItem(Item oldItem, Item newItem) {
			    			// An item has been updated, it's category may also have changed, hence we delete
							// the old item from the adapter and insert a fresh one to ensure consistency
			    			ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)mListView.getExpandableListAdapter();
		    				Category category = ((NoteItApplication) getApplication()).getCategory(newItem.mCategoryID);
		    				adapter.DeleteItem(oldItem);
		    				adapter.AddItem(newItem, category);
							adapter.notifyDataSetChanged();
						}
					},
					selItem.mID);
	    	addDialog.show();
    	} else {
	    	Item selItem = (Item) mListView.getExpandableListAdapter().getChild(mSelectedGroup, mSelectedChild);
	    	Intent intent = new Intent(ItemListActivity.this, AddEditItemActivity.class);
			intent.putExtra("ADD", false);
			intent.putExtra("ITEMID", selItem.mID);
			startActivityForResult(intent, ADD_ITEM_REQUEST);
   	 	}
    }
    
    protected void doDeleteItem() {
    	
    	final Item selItem = (Item) mListView.getExpandableListAdapter().getChild(mSelectedGroup, mSelectedChild);
    	if (selItem != null) {
    		((NoteItApplication) getApplication()).deleteItem(selItem.mID, new OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					if (resultCode == 0) {
						// Remove the item from our adapter
						ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)mListView.getExpandableListAdapter();
						adapter.DeleteItem(selItem);
						adapter.notifyDataSetChanged();
					} else
						Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_LONG).show();
				}
			});
    	}
    }

    void doToggleMarkDone() {
    	
    	final Item selItem = (Item) mListView.getExpandableListAdapter().getChild(mSelectedGroup, mSelectedChild);
    	if (selItem != null){
    		// Toggling an item done/undone essentially involved marking
    		// setting the current date to <now>/<null>
    		NoteItApplication app = (NoteItApplication) getApplication();
    		Item newItem = app.new Item(selItem);
    		newItem.mIsPurchased = newItem.mIsPurchased > 0 ? 0 : 1; // How do you do !(integer) in java?
    		app.editItem(newItem.mID, newItem, new OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					if (resultCode == 0) {
						selItem.mIsPurchased = selItem.mIsPurchased > 0 ? 0 : 1;
						ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)mListView.getExpandableListAdapter();
						adapter.notifyDataSetChanged();
					}
					else
						Toast.makeText(ItemListActivity.this, message, Toast.LENGTH_LONG).show();
				}
			});
    	}
    }
    
    protected void doExpandAll() {
    	
    	for (int i = 0; i < mAdapter.getGroupCount(); i++){
    		mListView.expandGroup(i);
    	}        	
    }
    
    protected void doCollapseAll() {
    	
    	for (int i = 0; i < mAdapter.getGroupCount(); i++){
    		mListView.collapseGroup(i);
    	}        	
    	
    }
    
    protected ArrayList<Item> sortItems(ArrayList<Item> list) {
    	return list;
    }
}
