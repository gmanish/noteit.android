package com.geekjamboree.noteit;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
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

	public class Country {
		public String 	mCountryCode = "";
		public String 	mCurrencyCode = "";
		public String 	mCurrencySymbol = "";
		public String 	mCurrencyName = "";
		public int 		mCurrencyIsRight = 0;
		
		public Country(
			String countryCode,
			String currencyCode) {
			
			mCountryCode = countryCode;
			mCurrencyCode = currencyCode;
		}
		
		public Country(
			String countryCode, 
			String currencyCode, 
			String currencySymbol, 
			int currencyIsRight, 
			String currencyName) {
			
			mCountryCode = countryCode;
			mCurrencyCode = currencyCode;
			mCurrencySymbol = currencySymbol;
			mCurrencyName = currencyName;
			mCurrencyIsRight = currencyIsRight;
		}
		
		public Country(JSONObject json) throws JSONException {
			mCountryCode = json.getString("countryCode");
			mCurrencyCode = json.getString("currencyCode"); 
			mCurrencySymbol = json.getString("currencySymbol");
			mCurrencyIsRight = json.getInt("currencyIsRight");
			mCurrencyName = json.getString("currencyName");
		}

		public boolean equals(Object obj){
			if (obj instanceof Country)
				return (mCountryCode.equals(((Country) obj).mCountryCode) &&
						mCurrencyCode.equals(((Country) obj).mCurrencyCode));
			else
				return false;
		}
	}
	
	public class Unit {
		static final int METRIC = 1;
		static final int IMPERIAL = 2;
			
		public String 	mName = "";
		public String 	mAbbreviation = "";
		public int 		mID = 0;
		public int 		mType = METRIC;
		
		public Unit(int id, String name, String abbreviation, int type) {
			mID = id;
			mAbbreviation = abbreviation;
			mName = name;
			mType = type;
		}
	
		public String toString(){
			return mName;
		}

		public boolean equals(Object obj){
			if (obj instanceof Unit)
				return (mID == ((Unit)obj).mID);
			else
				return false;
		}
	}
	
	public class Preference {
		public String 	mCountryCode = "";
		public String 	mCurrencyCode = "";
		
		public Preference(String countryCode, String currencyCode) {
			mCountryCode = countryCode;
			mCurrencyCode = currencyCode;
		}
		
		public Preference(JSONObject json) throws JSONException {
			mCountryCode = json.getString("countryCode");
			mCurrencyCode = json.getString("currencyCode");
		}
		
		public JSONObject getJSON() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("countryCode", mCountryCode);
			json.put("currencyCode", mCurrencyCode);
			return json;
		}
	}
	
	// Represents each shopping list that the user has
	public class ShoppingList {
		public String 	mName = "";
		public long		mID = 0;
		public long 	mItemCount = 0;
		
		public ShoppingList(long listID) {
			mID = listID;
		}
		
		public ShoppingList(long itemID, String itemName, long itemCount){
			mName = itemName;
			mID = itemID;
			mItemCount = itemCount;
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
		public static final int CATEGORY_ID		= 1 << 0;
		public static final int CATEGORY_NAME 	= 1 << 1;

		public String 	mName = "";
		public long 	mID = 0;
		public long 	mUserID = 0;
		public long 	mRank = 0;
		
		public Category(long categoryID) {
			mID = categoryID;
		}
		
		public Category(long categoryID, String categoryName, long userID, long rank){
			mName = categoryName;
			mID = categoryID;
			mUserID = userID;
			mRank = rank;
		}

		public Category(Category category) {
			this.mID = category.mID;
			this.mName = category.mName;
			this.mUserID = category.mUserID;
			this.mRank = category.mRank;
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
		
		public static final int ITEM_INSTANCEID		= 1 << 0;
		public static final int ITEM_USERID			= 1 << 1;
		public static final int ITEM_LISTID			= 1 << 2;
		public static final int ITEM_CATEGORYID     = 1 << 3;
		public static final int ITEM_NAME       	= 1 << 4;
		public static final int ITEM_UNITCOST       = 1 << 5;
		public static final int ITEM_QUANTITY       = 1 << 6;
		public static final int ITEM_UNITID         = 1 << 7;
		public static final int ITEM_DATEADDED      = 1 << 8;
		public static final int ITEM_DATEPURCHASED  = 1 << 9;
		public static final int ITEM_CLASSID		= 1 << 10;
		public static final int ITEM_ISPURCHASED	= 1 << 11;
		public static final int ITEM_ISASKLATER		= 1 << 12;
		
		public String 	mName 			= "";
		public long		mID				= 0; // the instance id
		public long 	mClassID		= 0; // the id of the item in the catalog table
		public long 	mCategoryID 	= 0;	
		public long 	mListID			= 0;
		public float	mQuantity		= 0;
		public float	mUnitPrice		= 0;
		public int		mUnitID			= 1; // default to "unit"
		public int		mIsPurchased	= 0; // SMALLINT at the backend
		public int 		mIsAskLater 	= 0; // SMALLINT at the backend
		public Date		mDateAdded;

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
			this.mIsAskLater = item.mIsAskLater;
		}
		
		public void copyFrom(Item item) {
			this.mCategoryID = item.mCategoryID;
			this.mClassID = item.mClassID;
			this.mID = item.mID;
			this.mListID = item.mListID;
			this.mName = item.mName;
			this.mQuantity = item.mQuantity;
			this.mUnitID = item.mUnitID;
			this.mUnitPrice = item.mUnitPrice;
			this.mIsPurchased = item.mIsPurchased;
			this.mIsAskLater = item.mIsAskLater;
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
	
	class CategoryReportItem {
		long 			mCategoryId = 0;
		String			mCategoryName = "";
		float			mPrice = 0f;
		
		public CategoryReportItem(long id, String name, float price) {
			mCategoryId = id;
			mCategoryName = name;
			mPrice = price;
		}
	}

	class ItemReportItem {
		long 			mItemID = 0;
		String			mItemName = "";
		float			mPrice = 0f;
		
		public ItemReportItem(long id, String name, float price) {
			mItemID = id;
			mItemName = name;
			mPrice = price;
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
	
	public static interface OnAddCategoryListener {
		void onPostExecute(long result, Category category, String message);
	}
	
	public static interface OnAddItemListener {
		void onPostExecute(long resultCode, Item item, String message);
	}
	
	public static interface OnGetPendingTotalListener {
		void onPostExecute(long resultCode, float total, String message);
	}
	
	public static interface OnGetCategoryReportListener {
		void onPostExecute(long resultCode, ArrayList<CategoryReportItem> items, String message);
	}
	
	public static interface OnGetItemReportListener {
		void onPostExecute(long resultCode, ArrayList<ItemReportItem> items, String message);
	}
	
	private long						mUserID = 0;
	private long						mCurrentShoppingListID = 0;
	private ArrayList<ShoppingList>		mShoppingLists = new ArrayList<ShoppingList>();
	private ArrayList<Category>			mCategories = new ArrayList<Category>();
	private ArrayList<Item>				mItems = new ArrayList<Item>();
	private ArrayList<Unit>				mUnits = new ArrayList<Unit>();
	private ArrayList<Country>			mCountries = new ArrayList<Country>();
	private Country						mDefaultCountry = new Country("US", "USD","$", 0, "US Dollar");
	private Preference					mUserPrefs = new Preference("US", "USD");
	private int 						mItemsStartPos = 0;
	private int 						mItemsBatchSize = 20;
	private boolean						mItemsMorePending = true;
	
	@Override
	public void onCreate() {
		fetchCountries(null);
		super.onCreate();
	}

	public ArrayList<Country> getCountries() {
		return mCountries;
	}
	
	public Country getDefaultCountry () {
		return mDefaultCountry;
	}
	
	public Preference getUserPrefs() {
		return mUserPrefs;
	}
	
	public void setUserPrefs(Preference prefs) {
		mUserPrefs = prefs;
	}
	     
	public void loginUser(
			String userEmail, 
			String password, 
			boolean isHashedPassword,
			AsyncInvokeURLTask.OnPostExecuteListener inPostExecute){
		try {
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_login_json"));
	        nameValuePairs.add(new BasicNameValuePair("email_ID", userEmail));
	        nameValuePairs.add(new BasicNameValuePair("password", password));
	        nameValuePairs.add(new BasicNameValuePair("isHashedPassword", String.valueOf(isHashedPassword ? 1 : 0)));

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
	
	public String getCurrencyFormat(boolean formatString) {
		
		String currencyFormat = null;
        if (mCountries != null && mUserPrefs != null) {
        	int index = mCountries.indexOf(
        			new Country(mUserPrefs.mCountryCode, mUserPrefs.mCurrencyCode));
        	if (index >= 0) {
        		if (mCountries.get(index).mCurrencyIsRight > 0) {
        			currencyFormat = new String(
        					(formatString ? "%1$s " : "%1$.2f ") + 
        					mCountries.get(index).mCurrencySymbol);
        		}
        		else { 
        			currencyFormat = new String(
        					mCountries.get(index).mCurrencySymbol + 
        					(formatString ? " %1$s " : " %1$.2f"));
        		}
        	}
        }
        
        return currencyFormat != null ? currencyFormat : (formatString ? "%1$s" : "%1$.2f");
	}
	
	public void saveUserPreferences(final OnMethodExecuteListerner listener) {
		
		if (mUserPrefs != null) {
			ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(3);
			
			try {
				args.add(new BasicNameValuePair("command", "do_save_prefs"));
				args.add(new BasicNameValuePair("arg1", mUserPrefs.mCountryCode));
				args.add(new BasicNameValuePair("arg2", mUserPrefs.mCurrencyCode));
				args.add(new BasicNameValuePair("arg3", String.valueOf(mUserID)));
				
				AsyncInvokeURLTask task = new AsyncInvokeURLTask(args, 
						new OnPostExecuteListener() {
							public void onPostExecute(JSONObject result) {
								if (listener != null) {
									try {
										listener.onPostExecute(
											result.getLong("JSONRetVal"), 
											result.getString("JSONRetMessage"));
									} catch (JSONException e) {
										listener.onPostExecute(-1, e.getMessage());
									}
								}
							}
						});
				task.execute();
				
			} catch (JSONException e) {
				if (listener != null)
					listener.onPostExecute(-1, "Unexpected Error");
				e.printStackTrace();
			} catch (Exception e) {
				if (listener != null)
					listener.onPostExecute(-1, "Unexpected Error");
				e.printStackTrace();
			}
		}
	}
	
	public void fetchShoppingLists(boolean isFetchCount, OnFetchShoppingListsListener inPostExecute){
		try {
			mShoppingLists.clear();
	    	
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_get_shop_list"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(getUserID())));
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(isFetchCount ? 1 : 0)));

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
	        							thisObj.getString("listName"),
	        							Long.parseLong(thisObj.getString("itemCount")));
	        	        		
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
        								json.getString("arg2"),
        								0));
        			
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
        				mShoppingLists.remove(new ShoppingList(listID));
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
		       			ShoppingList newList = new ShoppingList(
		       					json.getLong("arg1"), 
		       					json.getString("arg2"),
		       					listDetail.mItemCount); // Number of items wouldn't change
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
	
	public int getUnitsCount() {
		return mUnits.size();
	}
	
	public ArrayList<Unit> getUnits() {
		return mUnits;
	}
	
	public Unit getUnitFromID(int id) {
		int index = mUnits.indexOf(new Unit(id, "", "", 0));
		if (index >= 0)
			return mUnits.get(index);
		else
			return null;
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
			mItemsStartPos = 0;
			mItemsMorePending = true;
			mCurrentShoppingListID = thisList.mID;
			mItems.clear();
		}
	}
	
	public long getCurrentShoppingListID() {
		return mCurrentShoppingListID;
	}
	
	public int getCurrentShoppingListIndex() {
		return mShoppingLists.indexOf(new ShoppingList(mCurrentShoppingListID));
	}
	
	public ArrayList<ShoppingList> getShoppingList() {
		return mShoppingLists;
	}
	
	public void fetchCategories(OnFetchCategoriesListener inPostExecute){
		try {
			mCategories.clear();
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
				        	
				        	for (int index = 0; index < jsonArr.length(); index++){
				        		
				        		JSONObject thisObj = jsonArr.getJSONObject(index);
				        		
				        		Category thisCategory = new Category(
				        				Long.parseLong(thisObj.getString("listID")),
										thisObj.getString("listName"),
										thisObj.getLong("listID"),
										thisObj.getLong("categoryRank"));
				        		
				        		mCategories.add(thisCategory);
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

	public void addCategory(Category category, OnAddCategoryListener inListener){
		
		try {
			if (!mCategories.contains(category)){
	
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("command", "do_add_category"));
		        nameValuePairs.add(new BasicNameValuePair("arg1", category.mName));
		        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
	
		        class AddCategoryTask implements AsyncInvokeURLTask.OnPostExecuteListener {
	
					OnAddCategoryListener mListener;
					
					public AddCategoryTask(OnAddCategoryListener listener) {
						mListener = listener;
					}
					
					public void onPostExecute(JSONObject json) {
						
						try {
							long retVal = json.getLong("JSONRetVal");
							if (retVal == 0) {
								long newRank = 0;
								for (Category category : mCategories) {
									if (category.mRank > newRank)
										newRank = category.mRank;
								}
								Category category = new Category(
										json.getLong("arg1"), 
										json.getString("arg2"), 
										getUserID(),
										newRank + 1);
								mCategories.add(category);
								mListener.onPostExecute(retVal, category, "");
							} else 
								mListener.onPostExecute(
									retVal, 
									null, 
									json.getString("JSONRetMessage"));
						} catch (JSONException e) {
							Log.e("NoteItApplication.addCategory: JSON Exception", e.getMessage());
							mListener.onPostExecute(-1L, null, e.getMessage());
						}
					}
				}
		        
		        AddCategoryTask listener = new AddCategoryTask(inListener);
		        AsyncInvokeURLTask task = new AsyncInvokeURLTask(nameValuePairs, listener);
		        task.execute();
			}
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
	
	public void editCategory(int bitMask, Category category, final OnMethodExecuteListerner listener) {
		
		class EditCategoryListener implements AsyncInvokeURLTask.OnPostExecuteListener {
			
			public void onPostExecute(JSONObject json) {
				
				long retVal = -1;
				try {
					 retVal = json.getLong("JSONRetVal");
					 listener.onPostExecute(retVal, json.getString("JSONRetMessage"));
				} catch (JSONException e) {
					Log.e("NoteItApplication.editCategory", e.getMessage());
					listener.onPostExecute(retVal, e.getMessage());
				}
			}
		}
		
		try {
			ArrayList<NameValuePair> 	nameValuePairs = new ArrayList<NameValuePair>(3);
			nameValuePairs.add(new BasicNameValuePair("command", "do_edit_category"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(category.mID)));
	        nameValuePairs.add(new BasicNameValuePair("arg2", category.mName));
	        nameValuePairs.add(new BasicNameValuePair("arg3", String.valueOf(getUserID())));
	        
	        EditCategoryListener editListener = new EditCategoryListener();
	        AsyncInvokeURLTask task;
			task = new AsyncInvokeURLTask(nameValuePairs, editListener);
	        task.execute();
		} catch (Exception e) {
			Log.e("NoteItApplication.editCategory", e.getMessage());
		}
	}
	
	public void reorderCategory(final int source, final int target, final OnMethodExecuteListerner listener) {
		
		final Category sourceCategory = mCategories.get(source);
		final Category targetCategory = mCategories.get(target);

		class ReorderCategoryListener implements AsyncInvokeURLTask.OnPostExecuteListener {
			
			public void onPostExecute(JSONObject json) {
				
				long retVal = -1;
				try {
					 retVal = json.getLong("JSONRetVal");
					 if (retVal == 0) {
						// [TODO] The backend would have adjusted the ranking of all
						// items between (dragSource, dropTarget). In order to
						// obtain the adjusted ranks, we can refresh our category
						// list. However, that seems far from ideal. Even if the back
						// end were to implement this functionality, we'd need to
						// make two queries still. We're reproducing the logic 
						// used in the backend below.
						doReAdjustRanks(source, target);
						// Now adjust what's visible to the user
						mCategories.remove(sourceCategory);
						mCategories.add(target, sourceCategory);
					 }
					 listener.onPostExecute(retVal, json.getString("JSONRetMessage"));
				} catch (JSONException e) {
					Log.e("NoteItApplication.reorderCategory", e.getMessage());
					listener.onPostExecute(retVal, e.getMessage());
				}
			}
		}
		
		try {
			ArrayList<NameValuePair> 	nameValuePairs = new ArrayList<NameValuePair>(5);
			nameValuePairs.add(new BasicNameValuePair("command", "do_reorder_category"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(sourceCategory.mID)));
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(sourceCategory.mRank)));
	        nameValuePairs.add(new BasicNameValuePair("arg3", String.valueOf(targetCategory.mRank)));
	        nameValuePairs.add(new BasicNameValuePair("arg4", String.valueOf(getUserID())));
	        
	        ReorderCategoryListener reorderListener = new ReorderCategoryListener();
	        AsyncInvokeURLTask task;
			task = new AsyncInvokeURLTask(nameValuePairs, reorderListener );
	        task.execute();
		} catch (Exception e) {
			Log.e("NoteItApplication.reorderCategory", e.getMessage());
		}
	}

	
	public void deleteCategory(final int index, OnMethodExecuteListerner listener) {

		class DeleteCategoryListener implements AsyncInvokeURLTask.OnPostExecuteListener {
			
			OnMethodExecuteListerner mListener;
			
			public DeleteCategoryListener(OnMethodExecuteListerner listener) {
				
				mListener = listener;
			}
			
			public void onPostExecute(JSONObject json) {
				
				long retVal = -1;
				try {
					retVal = json.getLong("JSONRetVal");
					if (retVal == 0)
						mCategories.remove(index);
					mListener.onPostExecute(retVal, json.getString("JSONRetMessage"));
				} catch (JSONException e) {
					mListener.onPostExecute(retVal, e.getMessage());
				}
			}
		}
		
		try {
			Category 					category = mCategories.get(index);
			ArrayList<NameValuePair> 	nameValuePairs = new ArrayList<NameValuePair>(3);

			nameValuePairs.add(new BasicNameValuePair("command", "do_delete_category"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(category.mID)));
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
	        
	        DeleteCategoryListener deleteListener = new DeleteCategoryListener(listener);
	        AsyncInvokeURLTask task = new AsyncInvokeURLTask(nameValuePairs, deleteListener);
	        task.execute();
		} catch (Exception e) {
			Log.e("NoteItApplication.deleteCategory", e.getMessage());
		}
	}
	
	public int getCategoryCount(){
		return mCategories.size();
	}
	
	public Category getCategory(int index){
		return mCategories.get(index);
	}
	
	public Category getCategory(long categoryID){
		Category category = new Category(categoryID); // dummy created to use indexOf
		int index = mCategories.indexOf(category);
		if (index < mCategories.size() && index >= 0)
			return mCategories.get(index);
		return null;
	}

	public ArrayList<Category> getCategories(){
		return mCategories;
	}
	
	public boolean isMoreItemsPending() {
		return mItemsMorePending;
	}
	
	public void fetchItems(
			boolean showPurchased, 
			boolean movePurchasedToBottom, 
			OnFetchItemsListener inPostExecute){
		try {
			
			final ArrayList<Item> items = new ArrayList<Item>();
			
			if (mItemsStartPos < 0) {
				if (inPostExecute != null)
					inPostExecute.onPostExecute(0, items, "");
				return;
			}
			else if (mItemsStartPos == 0) {
				mItems.clear();
			}
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_list_shop_items"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", showPurchased ? "1" : "0")); // Show Purchased Items
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(mCurrentShoppingListID))); // Shopping List ID
	        nameValuePairs.add(new BasicNameValuePair("arg3", String.valueOf(mItemsStartPos)));
	        nameValuePairs.add(new BasicNameValuePair("arg4", String.valueOf(getUserID())));
	        nameValuePairs.add(new BasicNameValuePair("arg5", movePurchasedToBottom ? "1" : "0"));
	        nameValuePairs.add(new BasicNameValuePair("arg6", String.valueOf(mItemsBatchSize)));
	        
	        Log.i("NoteItApplication.fetchItems", "StartPos=" + mItemsStartPos + " Batch: " + mItemsBatchSize);
	        // We might get lesser than we asked for, will account for this in onPostExecute below
	        mItemsStartPos += mItemsBatchSize;
	        class FetchItemsTask implements AsyncInvokeURLTask.OnPostExecuteListener {
	        	
	        	OnFetchItemsListener	mListener;
	        	
	        	public FetchItemsTask(OnFetchItemsListener inListener) {
	        		mListener = inListener;
	        	} 
	        	
	        	public void onPostExecute(JSONObject json){
	        		try {
	                	long 	retval = json.getLong("JSONRetVal");
	         			String 	message = json.getString("JSONRetMessage");
	         			
	         			// to prevent re-entrance
	         			mItemsStartPos -= mItemsBatchSize;
	                	if (retval == 0){
	        	        	JSONArray jsonArr = json.getJSONArray("arg1");
	        	        	
	        	        	if (jsonArr.length() >= 0)
	        	        		mItemsStartPos += jsonArr.length(); // There may be more
	        	        	else
	        	        		mItemsStartPos = -1; // We're done fetching items
	        	        	
	        	        	if (jsonArr.length() < mItemsBatchSize)
	        	        		mItemsMorePending = false;
	        	        	else 
	        	        		mItemsMorePending = true;
	        	        	
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
	        	        		thisItem.mIsAskLater = thisObj.getInt("isAskLater");
	        	        		
	        	        		items.add(thisItem);
	        	        		mItems.add(thisItem);
	        	        	}
	        	        	
	        	        	//Collections.sort(mItems);
	        	        	mListener.onPostExecute(retval, items, message);
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

	public ArrayList<Item> getItems() {
		return mItems;
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
    	        		item.mIsAskLater = itemObject.getInt("isAskLater");
						
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
						newItem.mIsAskLater = object.getInt("isAskLater");
						
						// Add to our internal list
						mItems.add(newItem);
						
						// Increase itemCount on the parent list
						int index = mShoppingLists.indexOf(new ShoppingList(newItem.mListID));
						if (index >= 0) {
							mShoppingLists.get(index).mItemCount++;
						}
						
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
		nameValuePairs.add(new BasicNameValuePair("arg9", String.valueOf(inItem.mIsAskLater)));
		
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
	
	public void copyItem(
			final long instanceID, 
			final long targetListId, 
			OnMethodExecuteListerner listener) {
		
		class CopyItemTask  implements AsyncInvokeURLTask.OnPostExecuteListener {
        	OnMethodExecuteListerner mListener;

        	public CopyItemTask(OnMethodExecuteListerner inListener) {
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
	        nameValuePairs.add(new BasicNameValuePair("command", "do_copy_item"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(instanceID)));
	        nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(targetListId)));
	        nameValuePairs.add(new BasicNameValuePair("arg3", String.valueOf(getUserID())));
	        
	        CopyItemTask myTask = new CopyItemTask(listener);
	        AsyncInvokeURLTask 	task = new AsyncInvokeURLTask(nameValuePairs, myTask);
	        task.execute();
        } catch (Exception e) {
        	Log.e("NoteItApplication.copyItem", e.getMessage());
        }
	}
	
	public void editItem(final int bitMask, final Item item, OnMethodExecuteListerner inListener) {
        
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
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(item.mID)));
	        
	        if ((bitMask & Item.ITEM_LISTID) > 0)
	        	nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(item.mListID)));  // List ID
	        	
	        if ((bitMask & Item.ITEM_CATEGORYID) > 0)
	        	nameValuePairs.add(new BasicNameValuePair("arg3", String.valueOf(item.mCategoryID)));

	        if ((bitMask & Item.ITEM_NAME) > 0)
	        	nameValuePairs.add(new BasicNameValuePair("arg4", item.mName));
	        
	        if ((bitMask & Item.ITEM_QUANTITY) > 0)
	        	nameValuePairs.add(new BasicNameValuePair("arg5", String.valueOf(item.mQuantity)));
	        
	        if ((bitMask & Item.ITEM_UNITCOST) > 0)
	        	nameValuePairs.add(new BasicNameValuePair("arg6", String.valueOf(item.mUnitPrice)));
	        
	        if ((bitMask & Item.ITEM_UNITID) > 0)
	        	nameValuePairs.add(new BasicNameValuePair("arg7", String.valueOf(item.mUnitID)));
	        
	        nameValuePairs.add(new BasicNameValuePair("arg8", String.valueOf(getUserID())));
	        
	        if ((bitMask & Item.ITEM_ISPURCHASED) > 0) {
	        	nameValuePairs.add(new BasicNameValuePair("arg9", String.valueOf(item.mIsPurchased)));
	        	nameValuePairs.add(new BasicNameValuePair("arg11", new Date(Calendar.getInstance().getTimeInMillis()).toString()));
	        }
	        
	        if ((bitMask & Item.ITEM_ISASKLATER) > 0)
	        	nameValuePairs.add(new BasicNameValuePair("arg10", String.valueOf(item.mIsAskLater)));
	        
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
        				
						// Decrease itemCount on the parent list
						int index = mShoppingLists.indexOf(new ShoppingList(itemID));
						if (index >= 0) {
							mShoppingLists.get(index).mItemCount--;
						}
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
	
	public void markAllItemsDone(long list_ID, boolean done, final OnMethodExecuteListerner listener) {
		try {
			ArrayList<NameValuePair> 	args = new ArrayList<NameValuePair>(4);

			args.add(new BasicNameValuePair("command", "do_mark_all_done"));
			args.add(new BasicNameValuePair("arg1", String.valueOf(list_ID)));
			args.add(new BasicNameValuePair("arg2", String.valueOf(done ? 1 : 0)));
			args.add(new BasicNameValuePair("arg3", String.valueOf(getUserID())));
			
			AsyncInvokeURLTask setItemsDone = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					if (listener != null)
						try {
							listener.onPostExecute(
								result.getLong("JSONRetVal"), 
								result.getString("JSONRetMessage"));
						} catch (JSONException e) {
							listener.onPostExecute(-1, e.getMessage());
						}
				}
			});
			
			setItemsDone.execute();
		} catch (Exception e) {
			listener.onPostExecute(-1, e.getMessage());
		}
	}
	
	public void getPendingTotal(long listId, final OnGetPendingTotalListener listener) {
		
		try {
			ArrayList<NameValuePair> 	args = new ArrayList<NameValuePair>(3);
			
			args.add(new BasicNameValuePair("command", "do_get_pending_cost"));
			args.add(new BasicNameValuePair("arg1", String.valueOf(listId)));
			args.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));

			AsyncInvokeURLTask getPendingTotal = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					if (listener != null) {
						try {
							long retVal = result.getLong("JSONRetVal");
							if (retVal == 0) {
								listener.onPostExecute(
									result.getLong("JSONRetVal"), 
									result.getLong("arg1"), 
									"");
							} else 
								listener.onPostExecute(retVal, 0, result.getString("JSONRetMessage"));
						} catch (JSONException e) {
							listener.onPostExecute(-1, 0, e.getMessage());
						}
					}
				}
			});
			getPendingTotal.execute();
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null)
				listener.onPostExecute(-1, 0, e.getMessage());
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
	
	public void fetchCountries(final OnMethodExecuteListerner listener) {
		
		ArrayList<NameValuePair>	nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("command", "do_get_countries"));

		AsyncInvokeURLTask myTask;
		try {
			myTask = new AsyncInvokeURLTask(
					nameValuePairs, 
					new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					try {
						long retVal = result.getLong("JSONRetVal");
						if (retVal == 0){
							JSONArray jsonCountries = result.getJSONArray("arg3");
							for (int i = 0; i < jsonCountries.length(); i++) {
								mCountries.add(new Country(jsonCountries.getJSONObject(i)));
							}
							
							if (result.has("arg2"))
								mDefaultCountry = new Country(result.getJSONObject("arg2"));
						}
						if (listener != null)
							listener.onPostExecute(retVal, result.getString("JSONRetMessage"));
					} catch (JSONException e) {
						if (listener != null)
							listener.onPostExecute(-1, e.getMessage());
					}
				}
			});
			myTask.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void fetchUnits(int unitType, OnMethodExecuteListerner listener) {
		
		class getUnitsTask implements OnPostExecuteListener {

			OnMethodExecuteListerner mListener;
			
			getUnitsTask(OnMethodExecuteListerner inListener){
				mListener = inListener;
			}
			
	       	public void onPostExecute(JSONObject json) {
	       		long 				retVal = -1;
	       		
				try {
					if ((retVal = json.getLong("JSONRetVal")) == 0) {
						JSONArray jsonUnits = json.getJSONArray("arg1");
						for (int index = 0; index < jsonUnits.length(); index++) {
							JSONObject obj = jsonUnits.getJSONObject(index);
							mUnits.add(new Unit(
									obj.getInt("unitID"),
									obj.getString("unitName"),
									obj.getString("unitAbbreviation"),
									obj.getInt("unitType")));
						}
					}
					mListener.onPostExecute(retVal, json.getString("JSONRetMessage"));
				} catch (JSONException e) {
					Log.e("NoteItApplication.editShoppingList", e.getMessage());
                	mListener.onPostExecute(-1, e.getMessage());
				}
	       	}
		}
		
		mUnits.clear();
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("command", "do_get_units"));
		nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(unitType)));
		nameValuePairs.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
		
		getUnitsTask 		myTask = new getUnitsTask(listener);
        AsyncInvokeURLTask 	task;
		try {
			task = new AsyncInvokeURLTask(nameValuePairs, myTask);
	        task.execute();
		} catch (Exception e) {
			Log.e("NoteItApplication.suggestItems", e.getMessage());
			e.printStackTrace();
		}		
	}

	void doReAdjustRanks(int dragSource , int dropTarget) {
		Category sourceObj = mCategories.get(dragSource);
		Category targetObj = mCategories.get(dropTarget); 
		if (sourceObj.mRank < targetObj.mRank) {
			sourceObj.mRank = targetObj.mRank;	
			for (int i = dragSource + 1; i <= dropTarget; i++) {
				Category category = mCategories.get(i);
				category.mRank -= 1;
			}
		} else if (sourceObj.mRank > targetObj.mRank) {
			sourceObj.mRank = targetObj.mRank;	
			for (int i = dropTarget; i < dragSource; i++) {
				Category category = mCategories.get(i);
				category.mRank += 1;
			}
		}
	}
	
	public static String hashString(String clearText) throws NoSuchAlgorithmException {
    	String salt = "G3480BFA037EE";
    	MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    	if (sha1 != null) {
    		byte[] saltedText = sha1.digest((salt + clearText).getBytes());
    		StringBuffer hex = new StringBuffer(saltedText.length * 2);
    		for (int i = 0; i < saltedText.length; i++) {
    			char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    			hex.append(hexArray[(0xF0 & (int) saltedText[i]) >> 4]);
    			hex.append(hexArray[(0x0F & (int) saltedText[i])]);
    		}
        	return hex.toString();
    	} else
    		return clearText;
	}
	
	public void getReport(
			boolean isPurchased,
			Date from,
			Date to,
			final OnGetCategoryReportListener listener) {
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(5);
		args.add(new BasicNameValuePair("command", "do_category_report"));
		if (from != null)
			args.add(new BasicNameValuePair("arg1", from.toString()));
		if (to != null)
			args.add(new BasicNameValuePair("arg2", to.toString()));
		args.add(new BasicNameValuePair("arg3", String.valueOf(isPurchased ? 1 : 0)));
		args.add(new BasicNameValuePair("arg4", String.valueOf(getUserID())));
		try {
			AsyncInvokeURLTask getReportTask = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					try {
						long retVal = result.getLong("JSONRetVal");
						if (retVal == 0) {
							JSONArray jsonItems = result.getJSONArray("arg1");
							ArrayList<CategoryReportItem> items = new ArrayList<CategoryReportItem>();
							for (int index = 0; index < jsonItems.length(); index++) {
								JSONObject jsonItem = jsonItems.getJSONObject(index);
								CategoryReportItem item = new CategoryReportItem(
										jsonItem.getLong("categoryID"), 
										jsonItem.getString("categoryName"), 
										(float)jsonItem.getDouble("price"));
								items.add(item);
							}
							if (listener != null)
								listener.onPostExecute(retVal, items, "");
						} else if (listener != null)
							listener.onPostExecute(-1, null, result.getString("JSONRetMessage"));
					} catch (JSONException e) {
						e.printStackTrace();
						if (listener != null)
							listener.onPostExecute(-1, null, e.getMessage());
					}
				}
			});
			getReportTask.execute();
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onPostExecute(-1,null, e.getMessage());
			}
		}
	}

	public void getReport(
			boolean isPurchased,
			Date from,
			Date to,
			final OnGetItemReportListener listener) {
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(5);
		args.add(new BasicNameValuePair("command", "do_item_report"));
		if (from != null)
			args.add(new BasicNameValuePair("arg1", from.toString()));
		if (to != null)
			args.add(new BasicNameValuePair("arg2", to.toString()));
		args.add(new BasicNameValuePair("arg3", String.valueOf(isPurchased ? 1 : 0)));
		args.add(new BasicNameValuePair("arg4", String.valueOf(getUserID())));
		try {
			AsyncInvokeURLTask getReportTask = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					try {
						long retVal = result.getLong("JSONRetVal");
						if (retVal == 0) {
							JSONArray jsonItems = result.getJSONArray("arg1");
							ArrayList<ItemReportItem> items = new ArrayList<ItemReportItem>();
							for (int index = 0; index < jsonItems.length(); index++) {
								JSONObject jsonItem = jsonItems.getJSONObject(index);
								ItemReportItem item = new ItemReportItem(
										jsonItem.getLong("itemID"), 
										jsonItem.getString("itemName"), 
										(float)jsonItem.getDouble("price"));
								items.add(item);
							}
							if (listener != null)
								listener.onPostExecute(retVal, items, "");
						} else if (listener != null)
							listener.onPostExecute(-1, null, result.getString("JSONRetMessage"));
					} catch (JSONException e) {
						e.printStackTrace();
						if (listener != null)
							listener.onPostExecute(-1, null, e.getMessage());
					}
				}
			});
			getReportTask.execute();
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onPostExecute(-1,null, e.getMessage());
			}
		}
	}
}
