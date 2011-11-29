package com.geekjamboree.noteit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.geekjamboree.noteit.AsyncInvokeURLTask.OnPostExecuteListener;

public class NoteItApplication extends Application {

	// Represents each shopping list that the user has
	public class ShoppingList {
		public String 	mName = "";
		public long		mID = 0;
		
		public ShoppingList(long itemID, String itemName){
			mName = itemName;
			mID = itemID;
		}
		
		public String toString(){
			return mName;
		}
		
		public boolean equals(Object obj){
			if (obj instanceof ShoppingList)
				return (mID == ((ShoppingList)obj).mID);
			else
				return false;
		}
	}
	
	// Represents each category in the database
	public class Category {
		public String 	mName = "";
		public long 	mID = 0;
		public long 	mUserID = 0;
		
		public Category(long categoryID, String categoryName, long userID){
			mName = categoryName;
			mID = categoryID;
			mUserID = userID;
		}
		
		public String toString(){
			return mName;
		}

		public boolean equals(Object obj){
			if (obj instanceof Category)
				return (mID == ((Category)obj).mID);
			else
				return false;
		}
	}
	
	// Represents each item on the current shopping list
	public class Item implements Comparable<Item>{
		
		// [NOTE] If you add/remove members to the class don't forget to
		// update the parcelable overridden methods
		public String 	mName 			= "";
		public long		mID				= 0; // the instance id
		public long 	mClassID		= 0; // the id of the item in the catalog table
		public long 	mCategoryID 	= 0;	
		public long 	mListID			= 0;
		public float	mQuantity		= 0;
		public float	mUnitPrice		= 0;
		public int		mUnitID			= 1; // default to "unit"
		public int		mIsPurchased	= 0; // SMALLINT at the backend

		public Item() {
			
		}
		
		public Item(Item item){
			this.mCategoryID = item.mCategoryID;
			this.mClassID = item.mClassID;
			this.mID = item.mID;
			this.mListID = item.mListID;
			this.mName = item.mName;
			this.mQuantity = item.mQuantity;
			this.mUnitID = item.mUnitID;
			this.mUnitPrice = item.mUnitPrice;
			this.mIsPurchased = item.mIsPurchased;
		}
		
		public Item(long itemID) {
			mID = itemID;
		}
		
		public Item(long listID, long categoryID, String name) {
			mListID = listID;
			mCategoryID = categoryID;
			mName = name;
		}
		
		public Item(long id, String name, long categoryID){
			mID = id;
			mName = name;
			mCategoryID = categoryID;
		}
		
		public String toString(){
			return mName;
		}

		public boolean equals(Object obj){
			if (obj instanceof Item)
				return (mID == ((Item)obj).mID);
			else
				return false;
		}
		
		public String getCategoryName() {
			return getCategory(this.mCategoryID).mName;
		}
		
		public int compareTo(Item item){
			// Sort on category names, if they're same compare on item name
			int result = getCategoryName().compareTo(item.getCategoryName());
			return result == 0 ? mName.compareTo(item.mName) : result;
		}
	}
	
	public static interface OnMethodExecuteListerner {
		void onPostExecute(long resultCode, String message);
	}
	
	public static interface OnFetchShoppingListsListener {
		void onPostExecute(long resultCode, ArrayList<ShoppingList> categories, String message);
	}

	public static interface OnFetchCategoriesListener {
		void onPostExecute(long resultCode, ArrayList<Category> categories, String message);
	}
	
	public static interface OnFetchItemsListener {
		void onPostExecute(long resultCode, ArrayList<Item> items, String message);
	}
	
	public static interface OnSuggestItemsListener {
		void onPostExecute(long resultCode, ArrayList<String> suggestions, String message);
	}

	public static interface OnGetItemListener {
		void onPostExecute(long resultCode, Item item, String message);
	}
	
	public static interface OnAddItemListener {
		void onPostExecute(long resultCode, Item item, String message);
	}
	
	private long						mUserID = 0;
	private long						mCurrentShoppingListID = 0;
	private ArrayList<ShoppingList>		mShoppingLists = new ArrayList<ShoppingList>();
	private ArrayList<Category>			mCategories = new ArrayList<Category>();
	private ArrayList<Item>				mItems = new ArrayList<Item>();
	
	public void loginUser(String userEmail, AsyncInvokeURLTask.OnPostExecuteListener inPostExecute){
		try {
	    	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_login_json"));
	        nameValuePairs.add(new BasicNameValuePair("email_ID", userEmail));

			AsyncInvokeURLTask task = new AsyncInvokeURLTask(nameValuePairs, inPostExecute);
			task.execute();
			
		} catch (CancellationException e) {
			Log.e("NoteItApplication.loginUser", e.getMessage());
		} catch (ExecutionException e) {
			Log.e("NoteItApplication.loginUser", e.getMessage());
		} catch (InterruptedException e) {
			Log.e("NoteItApplication.loginUser", e.getMessage());
		} catch (Exception e) {
			Log.e("NoteItApplication.loginUser", e.getMessage());		
		}
	}
	
	public long getUserID(){
		return mUserID;
	}
	
	public void setUserID(long userID){
		mUserID = userID;
	}
	
	public void fetchShoppingLists(OnFetchShoppingListsListener inPostExecute){
		try {
			mShoppingLists.clear();
	    	
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_get_shop_list"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(getUserID())));

	        class FetchShoppingListsTask  implements AsyncInvokeURLTask.OnPostExecuteListener {
	        	
	        	OnFetchShoppingListsListener  mListener;
	        	
	        	FetchShoppingListsTask(OnFetchShoppingListsListener inPostExecute){
	        		mListener = inPostExecute;
	        	}
        		
	        	public void onPostExecute(JSONObject json) {
	        		try {
	                	long retval = json.getLong("JSONRetVal");
	        			
	                	if (retval == 0){
	        	        	JSONArray jsonArr = json.getJSONArray("arg1");
	        	        	
	        	        	// [TODO]: This doesn't feel right, calling the app object
	        	        	// to read shopping list items and having to populate them
	        	        	// in the object from here. Figure out an elegant way to 
	        	        	// handle this.
	        	        	for (int index = 0; index < jsonArr.length(); index++){
	        	        		JSONObject thisObj = jsonArr.getJSONObject(index);
	        	        		ShoppingList thisItem = new ShoppingList(
	        	        				Long.parseLong(thisObj.getString("listID")),
	        							thisObj.getString("listName"));
	        	        		
	        	        		mShoppingLists.add(thisItem);
	        	        	}
	        	        	
	        	        	mListener.onPostExecute(retval, mShoppingLists, "");
	                	}
	                	else {
	                		
	                		mListener.onPostExecute(retval, null, json.getString("JSONRetMessage"));
	                	}
	                		
	        		} catch (JSONException e){
                		mListener.onPostExecute(-1, null, getResources().getString(R.string.server_error));
	        			Log.e("NoteItApplication.fetchShoppingList", e.getMessage());
	        		}
	    		}
	        }
	        
	        FetchShoppingListsTask  myListener = new FetchShoppingListsTask(inPostExecute);
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(nameValuePairs, myListener);
			task.execute();
		} catch (CancellationException e) {
			Log.e("NoteItApplication.fetchShoppingLists", e.getMessage());
		} catch (ExecutionException e) {
			Log.e("NoteItApplication.fetchShoppingLists", e.getMessage());
		} catch (InterruptedException e) {
			Log.e("NoteItApplication.fetchShoppingLists", e.getMessage());
		} catch (Exception e) {
			Log.e("NoteItApplication.fetchShoppingLists", e.getMessage());		
		}
	}
	
	public void addShoppingList(String listName, OnMethodExecuteListerner inListener){
		
        class AddShoppingListTask  implements AsyncInvokeURLTask.OnPostExecuteListener {

        	OnMethodExecuteListerner mListener;
        	
        	AddShoppingListTask(OnMethodExecuteListerner inListener) {
        		mListener = inListener;
        	}
        	
        	public void onPostExecute(JSONObject json) {
        		try {
        			long retVal = json.getLong("JSONRetVal");
        			if (retVal == 0) {
        				// Success
        				
        				mShoppingLists.add(
        						new ShoppingList(
        								json.getLong("arg1"), 
        								json.getString("arg2")));
        			
        			}
        			
                	mListener.onPostExecute(retVal, json.getString("JSONRetMessage"));
        		} catch (JSONException e){
        			mListener.onPostExecute(-1, e.getMessage());
        		}
        	}
        }

        try {
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_add_shop_list"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", listName));
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
	        
	        AddShoppingListTask myAddShopListTask = new AddShoppingListTask(inListener);
	        AsyncInvokeURLTask 	task = new AsyncInvokeURLTask(nameValuePairs, myAddShopListTask);
	        task.execute();
        } catch (Exception e) {
        	Log.e("NoteItApplication.addShoppingList", e.getMessage());
        }
	}

	public void deleteShoppingList(final long listID, OnMethodExecuteListerner inListener) {
		
		class DeleteShoppingListTask implements OnPostExecuteListener {
       	

        	OnMethodExecuteListerner mListener;
        	
        	DeleteShoppingListTask(OnMethodExecuteListerner inListener) {
        		mListener = inListener;
        	}
        	
        	public void onPostExecute(JSONObject json) {
        		try {
        			long retVal = json.getLong("JSONRetVal");
        			if (retVal == 0){
        				// Delete it from our list as well
        				mShoppingLists.remove(new ShoppingList(listID, ""));
        			}
                	mListener.onPostExecute(retVal, json.getString("JSONRetMessage"));
        		} catch (JSONException e){
        			Toast.makeText(getApplicationContext(), "The server seems to be out of " +
        							"its mind. Please try later.", Toast.LENGTH_SHORT).show();
        			mListener.onPostExecute(-1, e.getMessage());
        		}
        	}
		}
		
        try {
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_delete_shop_list"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(listID)));
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
	        
	        DeleteShoppingListTask myDeleteTask = new DeleteShoppingListTask(inListener);
	        AsyncInvokeURLTask 	task = new AsyncInvokeURLTask(nameValuePairs, myDeleteTask);
	        task.execute();
        } catch (Exception e) {
        	Log.e("NoteItApplication.addShoppingList", e.getMessage());
        	inListener.onPostExecute(-1, e.getMessage());
        }
 	}
	
	// edit the item with id = listDetail.mID
	public void editShoppingList(final ShoppingList listDetail, OnMethodExecuteListerner inListener){
		
		class EditShoppingListTask implements OnPostExecuteListener {

			OnMethodExecuteListerner mListener;
			
			EditShoppingListTask(OnMethodExecuteListerner inListener){
				mListener = inListener;
			}
			
	       	public void onPostExecute(JSONObject json) {
	       		long retVal;
				try {
					retVal = json.getLong("JSONRetVal");
		       		if (retVal == 0) {
		       			ShoppingList newList = new ShoppingList(json.getLong("arg1"), json.getString("arg2"));
		       			mShoppingLists.set(mShoppingLists.indexOf(newList), newList);
		       		}

                	mListener.onPostExecute(retVal, json.getString("JSONRetMessage"));
				} catch (JSONException e) {
					Log.e("NoteItApplication.editShoppingList", e.getMessage());
                	mListener.onPostExecute(-1, e.getMessage());
				}
	       	}
		}
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("command", "do_edit_shop_list"));
		nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(listDetail.mID)));
		nameValuePairs.add(new BasicNameValuePair("arg2", listDetail.mName));
		nameValuePairs.add(new BasicNameValuePair("arg3", String.valueOf(getUserID())));

		EditShoppingListTask myEditTask = new EditShoppingListTask(inListener);
        AsyncInvokeURLTask task;
		try {
			task = new AsyncInvokeURLTask(nameValuePairs, myEditTask);
	        task.execute();
		} catch (Exception e) {
			Log.e("NoteItApplication.editShoppingList", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public int getShoppingListCount(){
		return mShoppingLists.size();
	}
	
	public ShoppingList getShoppingList(int index){
		return mShoppingLists.get(index);
	}
	
	public void setCurrentShoppingListIndex(int index){
		ShoppingList thisList = getShoppingList(index);
		if (thisList != null){
			mCurrentShoppingListID = thisList.mID;
		}
	}
	
	public long getCurrentShoppingListID() {
		return mCurrentShoppingListID;
	}
	
	public ArrayList<ShoppingList> getShoppingList() {
		return mShoppingLists;
	}
	
	public void fetchCategories(OnFetchCategoriesListener inPostExecute){
		try {
			if (mCategories == null) {
				// There are no cached results
				mCategories = new ArrayList<Category>();
			}
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_get_categories"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(getUserID())));
	        
        	class GetCategoriesTask implements AsyncInvokeURLTask.OnPostExecuteListener {
        		
        		OnFetchCategoriesListener mListener;
        		
        		GetCategoriesTask(OnFetchCategoriesListener inListener){
        			mListener = inListener;
        			if (mListener == null)
        				throw new NullPointerException();
        		}
				
        		public void onPostExecute(JSONObject json) {

					try {
			        	long retval = json.getLong("JSONRetVal");
						
			        	if (retval == 0){
				        	JSONArray jsonArr = json.getJSONArray("arg1");
				        	
				        	// Special Casing for "Uncategorized" (Category ID = 0)
				        	addCategory(new Category(0, getResources().getString(R.string.category_uncategorized), 1));
				        	
				        	// [TODO]: This doesn't feel right, calling the app object
				        	// to read shopping list items and having to populate them
				        	// in the object from here. Figure out an elegant way to 
				        	// handle this.
				        	for (int index = 0; index < jsonArr.length(); index++){
				        		
				        		JSONObject thisObj = jsonArr.getJSONObject(index);
				        		
				        		Category thisCategory = new Category(
				        				Long.parseLong(thisObj.getString("listID")),
										thisObj.getString("listName"),
										0L);
				        		
				        		addCategory(thisCategory);
				        	}
				        	
				        	// Call the invoker
				        	mListener.onPostExecute(retval, mCategories, "");
			        	} else 
			        		mListener.onPostExecute(retval, null, json.getString("JSONRetMessage"));
					} catch (JSONException e) {
						Log.e("NoteItApplication.fetchCategories: JSON Exception", e.getMessage());
						mListener.onPostExecute(-1L, mCategories, e.getMessage());
					}
				}
			}
        	
        	GetCategoriesTask  myListener = new GetCategoriesTask(inPostExecute);
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(nameValuePairs, myListener);
			task.execute();
		} catch (CancellationException e) {
			Log.e("NoteItApplication.fetchCategories", e.getMessage());
		} catch (ExecutionException e) {
			Log.e("NoteItApplication.fetchCategories", e.getMessage());
		} catch (InterruptedException e) {
			Log.e("NoteItApplication.fetchCategories", e.getMessage());
		} catch (Exception e) {
			Log.e("NoteItApplication.fetchCategories", e.getMessage());		
		}
	}

	public void addCategory(long itemID, String itemName, long userID){
		Log.i("NoteItApplication.addCategory", 
    			"Item ID: " + itemID + " Item Name: " + itemName);
		Category category = new Category(itemID, itemName, userID);
		addCategory(category);
	}
	
	public void addCategory(Category category){
		if (!mCategories.contains(category)){
			mCategories.add(category);
		}
	}
	
	public int getCategoryCount(){
		return mCategories.size();
	}
	
	public Category getCategory(int index){
		return mCategories.get(index);
	}
	
	public Category getCategory(long categoryID){
		Category category = new Category(categoryID, "", 0); // dummy created to use indexOf
		int index = mCategories.indexOf(category);
		if (index < mCategories.size() && index >= 0)
			return mCategories.get(index);
		return null;
	}

	public ArrayList<Category> getCategories(){
		return mCategories;
	}
	
	public void fetchItems(OnFetchItemsListener inPostExecute){
		try {
			
			mItems.clear();
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_list_shop_items"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", "Y")); // Show Purchased Items
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(mCurrentShoppingListID))); // Shopping List ID
	        nameValuePairs.add(new BasicNameValuePair("arg3", "0")); // Start @ index
	        nameValuePairs.add(new BasicNameValuePair("arg4", String.valueOf(getUserID())));
	        
	        class FetchItemsTask implements AsyncInvokeURLTask.OnPostExecuteListener {
	        	
	        	OnFetchItemsListener	mListener;
	        	
	        	public FetchItemsTask(OnFetchItemsListener inListener) {
	        		mListener = inListener;
	        	} 
	        	
	        	public void onPostExecute(JSONObject json){
	        		try {
	                	long 	retval = json.getLong("JSONRetVal");
	         			String 	message = json.getString("JSONRetMessage");
	         			
	                	if (retval == 0){
	        	        	JSONArray jsonArr = json.getJSONArray("arg1");
	        	        	
	        	        	for (int index = 0; index < jsonArr.length(); index++){
	        	        		JSONObject thisObj = jsonArr.getJSONObject(index);
	        	        		
	        	        		// construct the Item from JSON
	        	        		Item thisItem = new Item(
	        	        				Long.parseLong(thisObj.getString("instanceID")),
	        							thisObj.getString("itemName"),
	        							Long.parseLong(thisObj.getString("categoryID_FK")));
	        	        		
	        	        		thisItem.mClassID = thisObj.getLong("itemID_FK");
	        	        		thisItem.mListID = thisObj.getLong("listID_FK");
	        	        		thisItem.mUnitID = thisObj.getInt("unitID_FK");
	        	        		thisItem.mCategoryID = thisObj.getLong("categoryID_FK");
	        	        		thisItem.mUnitPrice = (float)thisObj.getDouble("unitCost");
	        	        		thisItem.mQuantity = (float)thisObj.getDouble("quantity");
	        	        		thisItem.mIsPurchased = thisObj.getInt("isPurchased");
	        	        		
	        	        		mItems.add(thisItem);
	        	        	}
	        	        	
	        	        	Collections.sort(mItems);
	        	        	mListener.onPostExecute(retval, mItems, message);
	                	} else 
	                		mListener.onPostExecute(retval, null, json.getString("JSONRetMessage"));
	        		} catch (JSONException e){
	        			Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
	        			Log.e("NoteItApplication.fetchItems", e.getMessage());
	        		}
	        		
	        	}
	        }
	        
	        FetchItemsTask myTask = new FetchItemsTask(inPostExecute);
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(nameValuePairs, myTask);
			task.execute();
			
		} catch (CancellationException e) {
			Log.e("NoteItApplication.fetchItems", e.getMessage());
		} catch (ExecutionException e) {
			Log.e("NoteItApplication.fetchItems", e.getMessage());
		} catch (InterruptedException e) {
			Log.e("NoteItApplication.fetchItems", e.getMessage());
		} catch (Exception e) {
			Log.e("NoteItApplication.fetchItems", e.getMessage());		
		}
	}

	public void getItem(long instanceID, OnGetItemListener inListener) {
		
		class GetItemTask implements OnPostExecuteListener {

			OnGetItemListener mListener;
			
			GetItemTask(OnGetItemListener inListener){
				mListener = inListener;
			}
			
	       	public void onPostExecute(JSONObject json) {
	       		long retVal;
				try {
					retVal = json.getLong("JSONRetVal");
					
					if (retVal == 0){
						
						JSONArray itemArray = json.getJSONArray("arg1");
						JSONObject itemObject = itemArray.getJSONObject(0);
						
						Item item = new Item(
							itemObject.getLong("instanceID"),
							itemObject.getString("itemName"),
							itemObject.getLong("categoryID_FK"));
						
						item.mClassID = itemObject.getLong("itemID_FK");
						item.mListID = itemObject.getLong("listID_FK");
						item.mUnitID = itemObject.getInt("unitID_FK");
						item.mCategoryID = itemObject.getLong("categoryID_FK");
						item.mUnitPrice = (float)itemObject.getDouble("unitCost");
						item.mQuantity = (float)itemObject.getDouble("quantity");
    	        		item.mIsPurchased = itemObject.getInt("isPurchased");
						
						mListener.onPostExecute(retVal, item, json.getString("JSONRetMessage"));
					} else {
						mListener.onPostExecute(retVal, null, json.getString("JSONRetMessage"));
					}
				} catch (JSONException e) {
					Log.e("NoteItApplication.editShoppingList", e.getMessage());
                	mListener.onPostExecute(-1, null, e.getMessage());
				}
	       	}
		}
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("command", "do_get_shop_item"));
		nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(instanceID)));
		nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
		
		GetItemTask myEditTask = new GetItemTask(inListener);
        AsyncInvokeURLTask task;
		try {
			task = new AsyncInvokeURLTask(nameValuePairs, myEditTask);
	        task.execute();
		} catch (Exception e) {
			Log.e("NoteItApplication.getItem", e.getMessage());
			e.printStackTrace();
		}		
	}
	
	public void addItem(Item inItem, OnAddItemListener inListener) {
		
		class AddItemTask implements OnPostExecuteListener {

			OnAddItemListener mListener;
			
			AddItemTask(OnAddItemListener inListener){
				mListener = inListener;
			}
			
	       	public void onPostExecute(JSONObject json) {
	       		
	       		long retVal;
	       		String message;
				try {
					
					retVal = json.getLong("JSONRetVal");
					message = json.getString("JSONRetMessage");
					if (retVal == 0) {
						JSONObject 	object = json.getJSONArray("arg1").getJSONObject(0);
						Item 		newItem = new Item(
										object.getLong("listID_FK"), 
										object.getLong("categoryID_FK"), 
										object.getString("itemName"));
						
						newItem.mID = object.getLong("instanceID");
						newItem.mClassID = object.getLong("itemID_FK");
						newItem.mListID = object.getLong("listID_FK");
						newItem.mUnitPrice = (float)object.getDouble("unitCost");
						newItem.mQuantity = (float)object.getDouble("quantity");
						newItem.mUnitID = object.getInt("unitID_FK");
						newItem.mCategoryID = object.getLong("categoryID_FK");
						newItem.mIsPurchased = object.getInt("isPurchased");
						
						// Add to our internal list
						mItems.add(newItem);
						
						// Invoke the callback
						mListener.onPostExecute(retVal, newItem, message);
					} else 
						mListener.onPostExecute(retVal, null, message);
				} catch (JSONException e) {
					Log.e("NoteItApplication.addItem", e.getMessage());
                	mListener.onPostExecute(-1, null, e.getMessage());
				}
	       	}
		}
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("command", "do_add_item"));
		nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(inItem.mListID)));
		nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(inItem.mCategoryID)));
		nameValuePairs.add(new BasicNameValuePair("arg3", inItem.mName));
		nameValuePairs.add(new BasicNameValuePair("arg4", String.valueOf(inItem.mQuantity)));
		nameValuePairs.add(new BasicNameValuePair("arg5", String.valueOf(inItem.mUnitPrice)));
		nameValuePairs.add(new BasicNameValuePair("arg6", String.valueOf(inItem.mUnitID)));
		nameValuePairs.add(new BasicNameValuePair("arg7", String.valueOf(getUserID())));
		nameValuePairs.add(new BasicNameValuePair("arg8", String.valueOf(inItem.mIsPurchased)));
		
		AddItemTask myEditTask = new AddItemTask(inListener);
        AsyncInvokeURLTask task;
		try {
			task = new AsyncInvokeURLTask(nameValuePairs, myEditTask);
	        task.execute();
		} catch (Exception e) {
			Log.e("NoteItApplication.addItem", e.getMessage());
			e.printStackTrace();
		}		
	}
	
	public void editItem(final long itemID, final Item item, OnMethodExecuteListerner inListener) {
        
		class EditItemTask  implements AsyncInvokeURLTask.OnPostExecuteListener {

        	OnMethodExecuteListerner mListener;
        	
        	EditItemTask(OnMethodExecuteListerner inListener) {
        		mListener = inListener;
        	}
        	
        	public void onPostExecute(JSONObject json) {
        		try {
        			long retVal = json.getLong("JSONRetVal");
                	mListener.onPostExecute(retVal, json.getString("JSONRetMessage"));
        		} catch (JSONException e){
        			mListener.onPostExecute(-1, e.getMessage());
        		}
        	}
        }

        try {
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_edit_shop_item"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(itemID)));
//	        nameValuePairs.add(new BasicNameValuePair("arg2", value))  // List ID
	        nameValuePairs.add(new BasicNameValuePair("arg3", String.valueOf(item.mCategoryID)));
	        nameValuePairs.add(new BasicNameValuePair("arg4", item.mName));
	        nameValuePairs.add(new BasicNameValuePair("arg5", String.valueOf(item.mQuantity)));
	        nameValuePairs.add(new BasicNameValuePair("arg6", String.valueOf(item.mUnitPrice)));
	        nameValuePairs.add(new BasicNameValuePair("arg7", String.valueOf(item.mUnitID)));
	        nameValuePairs.add(new BasicNameValuePair("arg8", String.valueOf(getUserID())));
	        nameValuePairs.add(new BasicNameValuePair("arg9", String.valueOf(item.mIsPurchased)));
	        
	        EditItemTask myTask = new EditItemTask(inListener);
	        AsyncInvokeURLTask 	task = new AsyncInvokeURLTask(nameValuePairs, myTask);
	        task.execute();
        } catch (Exception e) {
        	Log.e("NoteItApplication.editItem", e.getMessage());
        }
    }

	public void deleteItem(final long itemID, OnMethodExecuteListerner inListener) {
        
		class DeleteItemTask  implements AsyncInvokeURLTask.OnPostExecuteListener {

        	OnMethodExecuteListerner mListener;
        	
        	DeleteItemTask(OnMethodExecuteListerner inListener) {
        		mListener = inListener;
        	}
        	
        	public void onPostExecute(JSONObject json) {
        		try {
        			long retVal = json.getLong("JSONRetVal");
        			if (retVal == 0) {
        				// Success
        				mItems.remove(new Item(itemID));
        			}
                	mListener.onPostExecute(retVal, json.getString("JSONRetMessage"));
        		} catch (JSONException e){
        			mListener.onPostExecute(-1, e.getMessage());
        		}
        	}
        }

        try {
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_delete_item"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(itemID)));
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
	        
	        DeleteItemTask myTask = new DeleteItemTask(inListener);
	        AsyncInvokeURLTask 	task = new AsyncInvokeURLTask(nameValuePairs, myTask);
	        task.execute();
        } catch (Exception e) {
        	Log.e("NoteItApplication.deleteItem", e.getMessage());
        }
    }
	
	public void suggestItems(String subString, OnSuggestItemsListener inListener) {
		
		class SuggestItemsTask implements OnPostExecuteListener {

			OnSuggestItemsListener mListener;
			
			SuggestItemsTask(OnSuggestItemsListener inListener){
				mListener = inListener;
			}
			
	       	public void onPostExecute(JSONObject json) {
	       		long 				retVal = -1;
	       		ArrayList<String> 	suggestions = new ArrayList<String>();
	       		
				try {
					if ((retVal = json.getLong("JSONRetVal")) == 0) {
						
						JSONArray jsonSuggestions = json.getJSONArray("arg1");
						for (int i = 0; i < jsonSuggestions.length(); i++) {
							suggestions.add(jsonSuggestions.getString(i));
						}
					}
					mListener.onPostExecute(retVal, suggestions, json.getString("JSONRetMessage"));
				} catch (JSONException e) {
					Log.e("NoteItApplication.editShoppingList", e.getMessage());
                	mListener.onPostExecute(-1, null, e.getMessage());
				}
	       	}
		}
		
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
		nameValuePairs.add(new BasicNameValuePair("command", "do_suggest_items"));
		nameValuePairs.add(new BasicNameValuePair("arg1", subString));
		nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(4)));
		nameValuePairs.add(new BasicNameValuePair("arg3", String.valueOf(getUserID())));
		
		SuggestItemsTask myEditTask = new SuggestItemsTask(inListener);
        AsyncInvokeURLTask task;
		try {
			task = new AsyncInvokeURLTask(nameValuePairs, myEditTask);
	        task.execute();
		} catch (Exception e) {
			Log.e("NoteItApplication.suggestItems", e.getMessage());
			e.printStackTrace();
		}		
	}
}
