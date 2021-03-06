package com.geekjamboree.noteit;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.Timestamp;
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
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;

import com.geekjamboree.noteit.AsyncInvokeURLTask.OnPostExecuteListener;
import com.geekjamboree.noteit.AsyncInvokeURLTask.RequestMethod;
import com.geekjamboree.noteit.ItemListActivity.ItemType;
import com.geekjamboree.noteit.ItemListActivity.ProductSearchMethod;

public class NoteItApplication extends Application {

	public class Country {
		
		public int 		mCountryId 			= 0;
		public String 	mCountryCode 		= "";
		
		public Country(
			int countryId) {
			
			mCountryId = countryId;
		}
		
		public Country(
			int 	countryId,
			String 	countryCode) {
			
			mCountryId		= countryId;
			mCountryCode 	= countryCode;
		}
		
		public Country(JSONObject json) throws JSONException {
			mCountryId = json.getInt("countryId");
			mCountryCode = json.getString("countryCode");
		}

	}
	
	public class Currency {
	
		public int 		mCurrencyId 		= 0;
		public String 	mCurrencyCode 		= "";
		public String 	mCurrencySymbol 	= "";
		public String 	mCurrencyName 		= "";
		public int 		mCurrencyIsRight 	= 0;
		
		public static final int kDefaultCurrencyId = 135; // USD
		
		public Currency(int currencyId) {
			mCurrencyId = currencyId;
		}
		
		public Currency(JSONObject json) throws JSONException {
			
			mCurrencyId = json.getInt("currencyId");
			mCurrencyCode = json.getString("currencyCode");
			mCurrencySymbol = json.getString("currencySymbol");
			mCurrencyIsRight = json.getInt("currencyIsRight");
			mCurrencyName = json.getString("currencyName");
		}

		public boolean equals(Object obj){
			if (obj instanceof Currency)
				// As of now, we're only needing the currency code, so don't
				// compare against the country code
				return mCurrencyId == ((Currency)obj).mCurrencyId;
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
	
	static final int MIN_PASSWORD_LENGTH 		= 6;

	// PRODUCT_CODE_TYPES = list("UPC_A", "UPC_E", "EAN_8", "EAN_13", "RSS_14"
	static final int BARCODE_FORMAT_UNKNOWN 	= 1;
	static final int BARCODE_FORMAT_UPC_A		= 2;
	static final int BARCODE_FORMAT_UPC_E		= 3;
	static final int BARCODE_FORMAT_EAN_8		= 4;
	static final int BARCODE_FORMAT_EAN_13		= 5;
	static final int BARCODE_FORMAT_RSS_14 	 	= 6;
	
	static int barcodeFormatFromString(String format) {
		int barcodeFormat = BARCODE_FORMAT_UNKNOWN;
		if (format.equals("UPC_A"))
			barcodeFormat = BARCODE_FORMAT_UPC_A;
		else if (format.equals("UPC_E"))
			barcodeFormat = BARCODE_FORMAT_UPC_E;
		else if (format.equals("EAN_8"))
			barcodeFormat = BARCODE_FORMAT_EAN_8;
		else if (format.equals("EAN_13"))
			barcodeFormat = BARCODE_FORMAT_EAN_13;
		else if (format.equals("RSS_14"))
			barcodeFormat = BARCODE_FORMAT_RSS_14;
		else
			barcodeFormat = BARCODE_FORMAT_UNKNOWN;
		return barcodeFormat;
	}
	
	public class Preference {
		
		public int mCurrencyId = 0;
		
		public Preference(int currencyId) {
			mCurrencyId = currencyId;
		}
		
		public Preference(JSONObject json) throws JSONException {
			mCurrencyId = json.getInt("currencyId");
		}
		
		public JSONObject getJSON() throws JSONException {
			JSONObject json = new JSONObject();
			json.put("currencyId", mCurrencyId);
			return json;
		}
	}
	
	// Represents each shopping list that the user has
	public class ShoppingList {
		public String 	mName = "";
		public long		mID = 0;
		public long 	mItemCount = 0;
		public long 	mUserID = 0;
		
		public ShoppingList(long listID) {
			mID = listID;
		}
		
		public ShoppingList(long itemID, String itemName, long itemCount, long userID){
			mName = itemName;
			mID = itemID;
			mItemCount = itemCount;
			mUserID = userID;
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
		
		public void incrementCount() {
			mItemCount++;
		}
		
		public void decrementCount() {
			mItemCount--;
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
		public static final int ITEM_ALL			= ITEM_INSTANCEID | ITEM_USERID | ITEM_LISTID | ITEM_CATEGORYID |
									ITEM_NAME | ITEM_UNITCOST | ITEM_QUANTITY | ITEM_UNITID | 
									ITEM_DATEADDED | ITEM_DATEPURCHASED | ITEM_CLASSID | 
									ITEM_ISPURCHASED | ITEM_ISASKLATER;		
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
		public String 	mBarcode;
		public int		mBarcodeFormat;
		public int 		mLikeCount 		= 0;

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
			this.mBarcode = item.mBarcode;
			this.mBarcodeFormat = item.mBarcodeFormat;
			this.mLikeCount = item.mLikeCount;
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
			this.mBarcode = item.mBarcode;
			this.mBarcodeFormat = item.mBarcodeFormat;
			this.mLikeCount = item.mLikeCount;
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
		
		public Item(JSONObject json) throws JSONException {
			mCategoryID = json.getLong("categoryID_FK");
			mClassID = json.getLong("itemID_FK");
			mID = json.getLong("instanceID");
			mListID = json.getLong("listID_FK");
			mName = json.getString("itemName");
			mQuantity = (float) json.getDouble("quantity");
			mUnitID = json.getInt("unitID_FK");
			mUnitPrice = (float) json.getDouble("unitCost");
			mIsPurchased = json.getInt("isPurchased");
			mIsAskLater = json.getInt("isAskLater");
			mBarcode = json.getString("itemBarcode");
			mBarcodeFormat = json.getInt("itemBarcodeFormat");
			mLikeCount = json.getInt("voteCount");
			//mBarcodeFormat
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
	
	class ItemAndStats extends Item {
		
		public double mMean 					= 0.0f;
		public double mSampleDeviation 			= 0.0f;
		
		
		static public final int kNormalDeviation		= 0;
		static public final int kUp_OneStandardDev 		= 1;
		static public final int kUp_TwoStandardDev 		= 2;
		static public final int kUP_AlarmingDev			= 3;
		static public final int kDown_OneStandardDev	= -1;
		static public final int kDown_TwoStandardDev	= -2;
		static public final int kDown_AlarmingDev		= -3;
		
		public ItemAndStats(Item item, float mean, float deviation) {
			
			super(item);
			mMean = mean;
			mSampleDeviation = deviation;
		}
		
		public ItemAndStats(JSONObject json) throws JSONException {
		
			super(json);
			mMean = json.getDouble("mean");
			mSampleDeviation = json.getDouble("sampleDeviation");
		}
		
		public void copyFrom(Item item) {
			
			super.copyFrom(item);
			if (item instanceof ItemAndStats) {
				mMean = ((ItemAndStats) item).mMean;
				mSampleDeviation = ((ItemAndStats) item).mSampleDeviation;
			}
		}

		public int getDeviationRange() {
			
			double diff = mUnitPrice - mMean;
			
			if (mMean <= 0)
				return kNormalDeviation;
			else if (diff < -2 * mSampleDeviation) 
				return kDown_AlarmingDev;
			else if (diff < -1 * mSampleDeviation)
				return kDown_TwoStandardDev;
			else if (diff < 0)
				return kDown_OneStandardDev;
			else if (diff > 2 * mSampleDeviation)
				return kUP_AlarmingDev;
			else if (diff > 1 * mSampleDeviation)
				return kUp_TwoStandardDev;
			else if (diff > 0)
				return kUp_OneStandardDev;
			else
				return kNormalDeviation;
		}
	}
	
	class SuggestedItem {
		long 	mItemId = 0;
		String	mItemName = "";
		
		public SuggestedItem(long itemId, String itemName) {
			mItemId = itemId;
			mItemName = itemName;
		}

		public String toString(){
			return mItemName;
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
	
	class Message {
		
		long 		mMessageId		= 0;
		long 		mUserId 		= 0;
		long 		mFromUserId 	= 0;
		Timestamp	mDateReceived	= new Timestamp(Calendar.getInstance().getTimeInMillis());
		String		mSubject		= "";
		String		mText		 	= "";
		boolean 	mIsRead			= false;
		
		public Message(JSONObject json) throws JSONException {
			
			mMessageId 		= json.getLong("message_id");
			mUserID 		= json.getLong("user_id");
			mFromUserId 	= json.getLong("from_user_id");
			mDateReceived 	= java.sql.Timestamp.valueOf(json.getString("date_received"));
			mSubject 		= json.getString("subject");
			mText 			= json.getString("text");
			mIsRead	 		= json.getInt("is_read") > 0 ? true : false;
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
		void onPostExecute(long resultCode, ArrayList<SuggestedItem> suggestions, String message);
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
	
    public static interface OnSearchBarcodeListener {
    	public void onSearchResults(long retVal, Item item, String message); 
    }
    
    public static interface OnItemVoteListener {
    	public void onPostExecute(long retVal, int voteCount, String message); 
    }
    
    public static interface OnFetchMessagesListener {
    	public void onPostExecute(long retVal, ArrayList<Message> messages, String errMessage);
    }

    private long						mUserID = 0;
	private long						mCurrentShoppingListID = 0;
	private ArrayList<ShoppingList>		mShoppingLists = new ArrayList<ShoppingList>();
	private ArrayList<Category>			mCategories = new ArrayList<Category>();
	private ArrayList<Item>				mItems = new ArrayList<Item>();
	private ArrayList<Unit>				mUnits = new ArrayList<Unit>();
//	private ArrayList<Country>			mCountries = new ArrayList<Country>();
	private ArrayList<Currency>			mCurrencies = new ArrayList<Currency>();
//	private Country						mDefaultCountry;
//	private Currency					mDefaultCurrency = null;
	private Preference					mUserPrefs = new Preference(Currency.kDefaultCurrencyId);
	private int 						mItemsStartPos = 0;
	static final private int 			mItemsBatchSize = 25;
	private boolean						mItemsMorePending = true;
	
//	public ArrayList<Country> getCountries() {
//		return mCountries;
//	}
	
	public static int getItemBatchSize() {
		return mItemsBatchSize;
	}
	
	public ArrayList<Currency> getCurrencies() {
		return mCurrencies;
	}
	
//	public Country getDefaultCountry () {
//		return mDefaultCountry;
//	}
	
	public Preference getUserPrefs() {
		return mUserPrefs;
	}
	
	public void setUserPrefs(Preference prefs) {
		mUserPrefs = prefs;
	}
	 
	public void registerUser(
		String firstName, 
		String lastName, 
		String email, 
		String password,
		String confirmPassword,
		final OnMethodExecuteListerner listener) {
		
		try {

			if (email.trim().equals("")) {
				throw new Exception(getString(R.string.register_blank_email));
			} else if (password.trim().equals("") || confirmPassword.trim().equals("")) {
				throw new Exception(getString(R.string.register_blank_password));
			} else if (!password.trim().equals(confirmPassword.trim())) {
				throw new Exception(getString(R.string.register_password_not_match));
			} else if (password.length() < MIN_PASSWORD_LENGTH) {
       			throw new Exception(getString(R.string.login_password_tooshort));
        	}
			
			ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(5);
			args.add(new BasicNameValuePair("command", "do_register"));
			args.add(new BasicNameValuePair("first_name", firstName));
			args.add(new BasicNameValuePair("last_name", lastName));
			args.add(new BasicNameValuePair("email_ID", email));
			args.add(new BasicNameValuePair("password", password));
			
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(args, new AsyncInvokeURLTask.OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					long retVal;
					try {
						retVal = result.getLong("JSONRetVal");
						if (listener != null) { 
							listener.onPostExecute(retVal, result.getString("JSONRetMessage"));
						}
					} catch (JSONException e) {
						if (listener != null) listener.onPostExecute(-1, e.getMessage());
					}
				}
			});
			task.execute();
		} catch (Exception e) {
			if (listener != null) {
				listener.onPostExecute(-1, e.getMessage());
			}
		}
	}
	
	public void loginUser(
			String userEmail, 
			String password, 
			boolean isHashedPassword,
			final OnMethodExecuteListerner inPostExecute){
		try {
			
        	if (userEmail.equals("")) {
        		throw new Exception(getString(R.string.register_blank_email));
        	} else if (password.equals("")) {
        		throw new Exception(getString(R.string.register_blank_password));
        	} else if (password.length() < MIN_PASSWORD_LENGTH) {
    			throw new Exception(getString(R.string.login_password_tooshort));
        	}
   	
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_login_json"));
	        nameValuePairs.add(new BasicNameValuePair("email_ID", userEmail));
	        nameValuePairs.add(new BasicNameValuePair("password", password));
	        nameValuePairs.add(new BasicNameValuePair("isHashedPassword", String.valueOf(isHashedPassword ? 1 : 0)));
	        
	        Log.i("NoteItApplication.loginUser", "Email: " + userEmail);
	        Log.i("NoteItApplication.loginUser", "Password: " + password);
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(nameValuePairs, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					try {
						long retVal = result.getLong("JSONRetVal");
						if (retVal == 0) {
							long userID = result.getLong("arg1");
							
							setUserID(userID);
				    		if (!result.isNull("arg2")) {
				    			Preference prefs = new Preference(result.getJSONObject("arg2"));
				    			setUserPrefs(prefs);
				    		}
						}
						if (inPostExecute != null) {
							inPostExecute.onPostExecute(retVal, result.getString("JSONRetMessage"));
						}
					} catch (JSONException e) {
						if (inPostExecute != null) {
							inPostExecute.onPostExecute(-1, e.getMessage());
						}
						e.printStackTrace();
					}
				}
			});
			task.execute();
			
		} catch (Exception e) {
			Log.e("NoteItApplication.loginUser", e.getMessage());		
			if (inPostExecute != null) {
				inPostExecute.onPostExecute(-1, e.getMessage());
			}
		}
	}
	
	public void do_forgot_password(String emailID, final OnMethodExecuteListerner listener) {
		
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(2);
		args.add(new BasicNameValuePair("command", "do_forgot_password"));
		args.add(new BasicNameValuePair("arg1", emailID));
		try {
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					try {
						long retVal = result.getLong("JSONRetVal");
						if (listener != null)
							listener.onPostExecute(retVal, result.getString("JSONRetMessage"));
					} catch (JSONException e) {
						if (listener != null)
							listener.onPostExecute(-1, e.getMessage());
					}
				}
			});
			task.execute();
		} catch (Exception e) {
			if (listener != null)
				listener.onPostExecute(-1, e.getMessage());
		}
	}
	
	public long getUserID(){
		return mUserID;
	}
	
	public void setUserID(long userID){
		mUserID = userID;
	}
	
	protected void doInitialize(
			final String email, 
			final String password, 
			final boolean isHashedPassword, 
			final OnMethodExecuteListerner listener) {
		
		fetchCurrencies(new OnMethodExecuteListerner() {
			public void onPostExecute(long resultCode, String message) {
				
				if (resultCode == 0) {
					loginUser(email, password, isHashedPassword, new OnMethodExecuteListerner() {
						public void onPostExecute(long resultCode, String message) {

							if (resultCode == 0) {
								fetchUnits(Unit.METRIC, new NoteItApplication.OnMethodExecuteListerner() {
									public void onPostExecute(long resultCode, String message) {
										
										if (resultCode == 0) {
											fetchCategories(new OnFetchCategoriesListener() {
												public void onPostExecute(
														long resultCode, 
														ArrayList<Category> categories,
														String message) {
													
													if (listener != null) {
														listener.onPostExecute(resultCode, message);
													}
												}
											});
										} else if (listener != null) {
											listener.onPostExecute(resultCode, message);
										}
									}
								});
							} else if (listener != null) {
								listener.onPostExecute(resultCode, message);
							}
						}
					});
				}
				else if (listener != null) {
						listener.onPostExecute(resultCode, message);
				} 
			}
		});
	}
	
	public String getCurrencyForId(int currencyId) {
		
		String currencyCode = null;
		if (mCurrencies != null && mUserPrefs != null) {
			int index = mCurrencies.indexOf(new Currency(currencyId));
			if (index >= 0) {
				currencyCode = new String(mCurrencies.get(index).mCurrencyCode);
			}
		}
		
		return currencyCode != null ? currencyCode : "";
	}
	
	public String getCurrencyFormat(boolean formatString) {
		
		String currencyFormat = null;
        if (mCurrencies != null && mUserPrefs != null) {
        	int index = mCurrencies.indexOf(
        			new Currency(mUserPrefs.mCurrencyId));
        	if (index >= 0) {
        		if (mCurrencies.get(index).mCurrencyIsRight > 0) {
        			currencyFormat = new String(
        					(formatString ? "%1$s " : "%1$.2f ") + 
        					mCurrencies.get(index).mCurrencySymbol);
        		}
        		else { 
        			currencyFormat = new String(
        					mCurrencies.get(index).mCurrencySymbol + 
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
				args.add(new BasicNameValuePair("arg2", String.valueOf(mUserPrefs.mCurrencyId)));
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
	        	        	
	        	        	for (int index = 0; index < jsonArr.length(); index++){
	        	        		JSONObject thisObj = jsonArr.getJSONObject(index);
	        	        		ShoppingList thisItem = new ShoppingList(
        	        				Long.parseLong(thisObj.getString("listID")),
        							thisObj.getString("listName"),
        							Long.parseLong(thisObj.getString("itemCount")),
        							thisObj.getLong("userID_FK"));
	        	        		
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
        								0,
        								getUserID()));
        			
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
		       					listDetail.mItemCount,
		       					getUserID()); // Number of items wouldn't change
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
	
	public void shareShoppingList(long listID, String emailID, final OnMethodExecuteListerner listener) {
	
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(4);
		
		args.add(new BasicNameValuePair("command", "do_share_shop_list"));
		args.add(new BasicNameValuePair("arg1", String.valueOf(listID)));
		args.add(new BasicNameValuePair("arg2", emailID));
		args.add(new BasicNameValuePair("arg3", String.valueOf(getUserID())));
		
		try {
			AsyncInvokeURLTask	task = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					if (listener != null) {
						try {
							listener.onPostExecute(
									result.getLong("JSONRetVal"), 
									result.getString("JSONRetMessage"));
						} catch (JSONException e) {
							e.printStackTrace();
							if (listener != null) {
								listener.onPostExecute(-1, e.getMessage());
							}
						}
					}
				}
			});
			task.execute();
			
		} catch (Exception e) {
			if (listener != null) {
				listener.onPostExecute(-1, e.getMessage());
			}
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
		if (index < getShoppingListCount()) {
			ShoppingList thisList = getShoppingList(index);
			if (thisList != null){
				mItemsStartPos = 0;
				mItemsMorePending = true;
				mCurrentShoppingListID = thisList.mID;
				mItems.clear();
			}
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
										thisObj.getLong("userID_FK"),
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
	        nameValuePairs.add(new BasicNameValuePair("arg5", String.valueOf(targetCategory.mID)));
	        
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
	        	        		Item thisItem = new Item(thisObj);
	        	        		items.add(thisItem);
	        	        		mItems.add(thisItem);
	        	        	}
	        	        	
	        	        	//Collections.sort(mItems);
	        	        	mListener.onPostExecute(retval, items, message);
	                	} else 
	                		mListener.onPostExecute(retval, null, json.getString("JSONRetMessage"));
	        		} catch (JSONException e){
                		mListener.onPostExecute(-1, null, e.getMessage()	);
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
	
	public void fetchPurchasedInstances(long classId, final OnFetchItemsListener listener) {
		
		try {
			ArrayList<NameValuePair>	args = new ArrayList<NameValuePair>(4);
			args.add(new BasicNameValuePair("command", "do_get_instances"));
			args.add(new BasicNameValuePair("arg1", String.valueOf(classId)));
			args.add(new BasicNameValuePair("arg2", "1"));
			args.add(new BasicNameValuePair("arg3", String.valueOf(getUserID())));
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					
					long retVal = 0;
					try {
						retVal = result.getLong("JSONRetVal");
						if (retVal == 0) {
							ArrayList<Item> items = new ArrayList<Item>();
							JSONArray jsonItems = result.getJSONArray("arg1");
							for (int index = 0; index < jsonItems.length(); index++) {
								JSONObject jsonObj = jsonItems.getJSONObject(index);
								items.add(new Item(jsonObj));
							}
							if (listener != null) {
								listener.onPostExecute(retVal, items, "");
							}
						} else if (listener != null){
							listener.onPostExecute(retVal, null, result.getString("JSONRetMessage"));
						}
					} catch (JSONException e) {
						if (listener != null) 
							listener.onPostExecute(-1, null, e.getMessage());
					}
				}
			});
			task.execute();
		} catch (Exception e) {
			if (listener != null)
				listener.onPostExecute(-1, null, e.getMessage());
		}
	}

	public ArrayList<Item> getItems() {
		return mItems;
	}
	
	public void getItem(long instanceID, OnGetItemListener inListener) {
		
		int index = mItems != null ? mItems.indexOf(new Item(instanceID)) : - 1;
		if (index >= 0 && inListener != null) {
			inListener.onPostExecute(0, mItems.get(index), "");
		} else {
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
							
							Item item = new Item(itemObject);
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
						JSONObject 		object = json.getJSONArray("arg1").getJSONObject(0);
						ItemAndStats	newItem = new ItemAndStats(object);
						
						// Add to our internal list
						mItems.add(newItem);
						
						// Increase itemCount on the parent list
						int index = mShoppingLists.indexOf(new ShoppingList(newItem.mListID));
						if (index >= 0) {
							mShoppingLists.get(index).incrementCount();
						}
						
						Log.d("NoteItApplication.addItem()", "Item added: " + newItem.mName);
						
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
		nameValuePairs.add(new BasicNameValuePair("arg10", inItem.mBarcode));
		nameValuePairs.add(new BasicNameValuePair("arg11", String.valueOf(inItem.mBarcodeFormat)));
		
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
        			if (retVal == 0) {
						// Increase itemCount on the target list
						int index = mShoppingLists.indexOf(new ShoppingList(targetListId));
						if (index >= 0) {
							mShoppingLists.get(index).incrementCount();
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
	
	public void editItem(final int bitMask, final Item item, OnAddItemListener inListener) {
        
		class EditItemTask  implements AsyncInvokeURLTask.OnPostExecuteListener {

        	OnAddItemListener mListener;
        	
        	EditItemTask(OnAddItemListener inListener) {
        		mListener = inListener;
        	}
        	
        	public void onPostExecute(JSONObject json) {
        		try {
        			long retVal = json.getLong("JSONRetVal");
        			
        			if (retVal == 0) {
        				
						JSONObject 		object = json.getJSONArray("arg1").getJSONObject(0);
						ItemAndStats	editedItem = new ItemAndStats(object);
						
						int listIndex = mShoppingLists.indexOf(new ShoppingList(item.mListID));
        				if (listIndex >= 0 && (bitMask & Item.ITEM_ISPURCHASED) > 0) {

        					// Item is being marked purchased, decrease count
							if (item.mIsPurchased > 0)
								mShoppingLists.get(listIndex).decrementCount();
							else
								mShoppingLists.get(listIndex).incrementCount();
	        			} 
        			
	        			if (item.mListID != getCurrentShoppingListID()) {
	        				
	        				// Item is being moved, update the counts. The following assumes
	        				// the item being moved is currently in the current shopping list.
	        				int index = mShoppingLists.indexOf(new ShoppingList(getCurrentShoppingListID()));
	        				if (index >= 0) {
	        					mShoppingLists.get(index).decrementCount();
	        				}
	        				index = mShoppingLists.indexOf(new ShoppingList(item.mListID));
	        				if (index >= 0) {
	        					mShoppingLists.get(index).incrementCount();
	        				}
	        			}

	        			// Update item details in our internal list
	        			int itemIndex = mItems.indexOf(new Item(item.mID));
	        			if (itemIndex >= 0) {
	        				mItems.set(itemIndex, editedItem);
	        			}
	                	
	        			mListener.onPostExecute(retVal, editedItem, json.getString("JSONRetMessage"));
        			} else { 
        				mListener.onPostExecute(retVal, null, json.getString("JSONRetMessage"));
        			}
        		} catch (JSONException e){
        			mListener.onPostExecute(-1, null, e.getMessage());
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
        				int index = mItems.indexOf(new Item(itemID));
        				if (index != -1) {
        					Item item = mItems.remove(index);
    						// Decrease itemCount on the parent list
    						int listIndex = mShoppingLists.indexOf(new ShoppingList(item.mListID));
    						if (listIndex >= 0) {
    							mShoppingLists.get(listIndex).decrementCount();
    						}
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
	
	public void setItemMetadata(final long itemId, boolean like, final OnItemVoteListener inListener) {
		
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(2);
		args.add(new BasicNameValuePair("command", like == true ? "do_like_item" : "do_dislike_item"));
		args.add(new BasicNameValuePair("arg1", String.valueOf(itemId)));
		args.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
		try {
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					try {
						long retVal = result.getLong("JSONRetVal"); 
						if (inListener != null) {
							if (retVal == 0)
								inListener.onPostExecute(
									retVal, 
									result.getInt("voteCount"),
									result.getString("JSONRetMessage"));
							else
								inListener.onPostExecute(
									retVal, 
									0,
									result.getString("JSONRetMessage"));
						}
					} catch (JSONException e) {
						if (inListener != null)
							inListener.onPostExecute(-1, 0, e.getMessage());
					}
				}
			});
			task.execute();
		} catch (Exception e) {
			e.printStackTrace();
			if (inListener != null)
				inListener.onPostExecute(-1, 0, e.getMessage());
		}
	}
	
	public void markAllItemsDone(final long list_ID, boolean done, final OnMethodExecuteListerner listener) {
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
							long retVal = result.getLong("JSONRetVal");
							if (retVal == 0) {
	    						// Decrease itemCount on the parent list
	    						int listIndex = mShoppingLists.indexOf(new ShoppingList(list_ID));
	    						if (listIndex >= 0) {
	    							mShoppingLists.get(listIndex).mItemCount = 0;
	    						}
							}
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
	       		long 						retVal = -1;
	       		ArrayList<SuggestedItem> 	suggestions = new ArrayList<SuggestedItem>();
	       		
				try {
					if ((retVal = json.getLong("JSONRetVal")) == 0) {
						
						JSONArray jsonSuggestions = json.getJSONArray("arg1");
						for (int i = 0; i < jsonSuggestions.length(); i++) {
							JSONObject jsonObj = jsonSuggestions.getJSONObject(i);
							suggestions.add(
								new SuggestedItem(
									jsonObj.getLong("itemId"), 
									jsonObj.getString("itemName")));
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
	
//	public void fetchCountries(final OnMethodExecuteListerner listener) {
//		
//		ArrayList<NameValuePair>	nameValuePairs = new ArrayList<NameValuePair>(1);
//		nameValuePairs.add(new BasicNameValuePair("command", "do_get_countries"));
//
//		AsyncInvokeURLTask myTask;
//		try {
//			myTask = new AsyncInvokeURLTask(
//					nameValuePairs, 
//					new OnPostExecuteListener() {
//				
//				public void onPostExecute(JSONObject result) {
//					try {
//						long retVal = result.getLong("JSONRetVal");
//						if (retVal == 0){
//							JSONArray jsonCountries = result.getJSONArray("arg3");
//							for (int i = 0; i < jsonCountries.length(); i++) {
//								mCountries.add(new Country(jsonCountries.getJSONObject(i)));
//							}
//							
//							if (result.has("arg2"))
//								mDefaultCountry = new Country(result.getJSONObject("arg2"));
//						}
//						if (listener != null)
//							listener.onPostExecute(retVal, result.getString("JSONRetMessage"));
//					} catch (JSONException e) {
//						if (listener != null)
//							listener.onPostExecute(-1, e.getMessage());
//					}
//				}
//			});
//			myTask.execute();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	public void fetchCurrencies(final OnMethodExecuteListerner listener) {
		
		ArrayList<NameValuePair>	nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("command", "do_get_currencies"));

		AsyncInvokeURLTask myTask;
		try {
			myTask = new AsyncInvokeURLTask(
					nameValuePairs, 
					new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					try {
						long retVal = result.getLong("JSONRetVal");
						if (retVal == 0){
							JSONArray jsonCurrencies = result.getJSONArray("arg3");
							for (int i = 0; i < jsonCurrencies.length(); i++) {
								mCurrencies.add(new Currency(jsonCurrencies.getJSONObject(i)));
							}
							
//							if (result.has("arg2"))
//								mDefaultCurrency = new Currency(result.getJSONObject("arg2"));
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

	public void fetchInboxMessageHeaders(boolean ignoreRead, final OnFetchMessagesListener listener) {
	
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(3);
		args.add(new BasicNameValuePair("command", "do_get_msg_headers"));
		args.add(new BasicNameValuePair("arg1", String.valueOf(ignoreRead ? 1 : 0)));
		args.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
		
		try {
			AsyncInvokeURLTask 	myTask = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					
					long retVal = -1;
					try {
						ArrayList<Message> messages = null;
						retVal = result.getLong("JSONRetVal");
						if (retVal == 0) {
							messages = new ArrayList<Message>();
							JSONArray jsonMessages = result.getJSONArray("arg1");
							for (int index = 0; index < jsonMessages.length(); index++) {
								JSONObject obj = jsonMessages.getJSONObject(index);
								messages.add(new Message(obj));
							}
						} 
						if (listener != null) {
							listener.onPostExecute(retVal, messages, result.getString("JSONRetMessage"));
						}
					} catch (JSONException e) {
						if (listener != null)
							listener.onPostExecute(retVal, null, e.getMessage());
						e.printStackTrace();
					}
				}
			});
			myTask.execute();
		} catch (Exception e) {
			if (listener != null)
				listener.onPostExecute(-1, null, e.getMessage());
		}
	}
	
	public void getMessage(long messageID, final OnFetchMessagesListener listener) {
		
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(3);
		args.add(new BasicNameValuePair("command", "do_get_msg"));
		args.add(new BasicNameValuePair("arg1", String.valueOf(messageID)));
		args.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
		
		try {
			AsyncInvokeURLTask 	myTask = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					
					long retVal = -1;
					try {
						ArrayList<Message> messages = null;
						retVal = result.getLong("JSONRetVal");
						if (retVal == 0) {
							messages = new ArrayList<Message>();
							JSONObject jsonMessage = result.getJSONObject("arg1");
							messages.add(new Message(jsonMessage));
						} 
						if (listener != null) {
							listener.onPostExecute(retVal, messages, result.getString("JSONRetMessage"));
						}
					} catch (JSONException e) {
						if (listener != null)
							listener.onPostExecute(retVal, null, e.getMessage());
						e.printStackTrace();
					}
				}
			});
			myTask.execute();
		} catch (Exception e) {
			if (listener != null)
				listener.onPostExecute(-1, null, e.getMessage());
		}
	}
	
	public void setMessageRead(long messageID, final OnMethodExecuteListerner listener) {
		
		ArrayList<NameValuePair> args = new ArrayList<NameValuePair>(3);
		args.add(new BasicNameValuePair("command", "do_mark_msg_read"));
		args.add(new BasicNameValuePair("arg1", String.valueOf(messageID)));
		args.add(new BasicNameValuePair("arg2", String.valueOf(getUserID())));
		
		try {
			AsyncInvokeURLTask 	myTask = new AsyncInvokeURLTask(args, new OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					
					if (listener != null) {
						try {
							listener.onPostExecute(result.getLong("JSONRetVal"), result.getString("JSONRetMessage"));
						} catch (JSONException e) {
							e.printStackTrace();
							listener.onPostExecute(-1, e.getMessage());
						}
					}
				}
			});
			myTask.execute();
		} catch (Exception e) {
			if (listener != null)
				listener.onPostExecute(-1, e.getMessage());
		}
	}
	
    protected void searchItemByBarcode(
    	int format,
    	String contents, 
    	final OnSearchBarcodeListener listener) {
    	
    	ArrayList<NameValuePair> 	params = new ArrayList<NameValuePair>();
    	final ProductSearchMethod	searchMethod = ProductSearchMethod.NOTE_IT;
    	String 						searchURL = "";
    	RequestMethod				method = RequestMethod.GET;
    	
    	if (searchMethod == ProductSearchMethod.SEARCH_UPC) {
    		
    		final String searchUPCKey = "96C1B2C5-DB48-4D52-B8E4-50971BAC5F47";
    		searchURL = "http://www.searchupc.com/handlers/upcsearch.ashx";
	    	params.add(new BasicNameValuePair("request_type", "3"));
	    	params.add(new BasicNameValuePair("access_token", searchUPCKey));
	    	params.add(new BasicNameValuePair("upc", contents));
    	} else if (searchMethod == ProductSearchMethod.GOOGLE_SEARCH) {
        	
    		final String googleAPIKey = "AIzaSyA9IqL-QR5YezowBLgMIwwDvd_lDtWcSlo";
    		searchURL = "https://www.googleapis.com/shopping/search/v1/public/products";
    		params.add(new BasicNameValuePair("key", googleAPIKey));
    		params.add(new BasicNameValuePair("country", "US"));
    		params.add(new BasicNameValuePair("q", contents));
    	} else if (searchMethod == ProductSearchMethod.NOTE_IT) {
    		
    		method = RequestMethod.POST;
    		params.add(new BasicNameValuePair("command", "do_search_barcode"));
    		params.add(new BasicNameValuePair("arg1", contents));
    		params.add(new BasicNameValuePair("arg2", String.valueOf(format)));
    		params.add(new BasicNameValuePair("arg3", String.valueOf(getUserID())));
    	}
    	
    	try {
	    	AsyncInvokeURLTask task = new AsyncInvokeURLTask(
	    			method,
	    			searchURL, 
	    			params, 
	    			new AsyncInvokeURLTask.OnPostExecuteListener() {
				
				public void onPostExecute(JSONObject result) {
					try {
						if (result.isNull("JSONRetVal")) {
							if (searchMethod == ProductSearchMethod.GOOGLE_SEARCH) {
								if (result.isNull("error")) {
									Item item = parseItemFromGoogleJSON(result);
									listener.onSearchResults(0, item, "");
								} else if (listener != null){
									JSONObject errors = result.getJSONObject("error");
									listener.onSearchResults(
										errors.getLong("code"), 
										null, 
										errors.getString("message"));
								}
							}
						} else {
							long retVal = result.getLong("JSONRetVal");
							long count = result.getLong("arg2");
							Item item = null;
							if (retVal == 0 && count > 0) {
								item = new Item(result.getJSONObject("arg1"));
							}
							listener.onSearchResults(
								retVal, 
								item, 
								result.getString("JSONRetMessage"));
						}
					} catch (JSONException e) {
						e.printStackTrace();
						if (listener != null) listener.onSearchResults(-1, null, e.getMessage());
					}
				}
			});
	    	task.execute();
    	} catch (Exception e) {
			if (listener != null) listener.onSearchResults(-1, null, e.getMessage());
    	}
    }
    
    private Item parseItemFromGoogleJSON(JSONObject items) throws JSONException {
    	
    	long 				itemCount 	= items.getLong("totalItems");
    	Item				item 		= null;
		JSONArray 			array 		= items.getJSONArray("items");
    	
    	for (int index = items.getInt("startIndex"); index < itemCount; index++) {
    		
    		JSONObject 	itemJSON 	= array.getJSONObject(index);
    		JSONObject  product 	= itemJSON.getJSONObject("product");
    		JSONArray 	inventories = product.getJSONArray("inventories");
    		
    		if (inventories.length() > 0) {
	    		item = new Item(0);
	    		item.mQuantity = 1;
	    		item.mUnitID = 1; // unit
	    		item.mName = product.getString("title");
	    		String currency = inventories.getJSONObject(0).getString("currency");
	    		if (currency.equals(mUserPrefs.mCurrencyId)) {
    	    		item.mUnitPrice = (float) inventories.getJSONObject(0).getDouble("price");
	    		}
	    		break;
    		}
    	} 
    	return item;
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
    		byte[] saltedText;
			try {
				saltedText = sha1.digest((salt + clearText).getBytes("UTF-8"));
	    		Log.i("NoteItApplication.hashString", "Clear Text: " + salt + clearText + " Digest:" + sha1.digest((salt + clearText).getBytes()));
	    		StringBuffer hex = new StringBuffer(saltedText.length * 2);
	    		for (int i = 0; i < saltedText.length; i++) {
	    			char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	    			hex.append(hexArray[(0xF0 & (int) saltedText[i]) >> 4]);
	    			hex.append(hexArray[(0x0F & (int) saltedText[i])]);
	    		}
	        	return hex.toString();
			} catch (UnsupportedEncodingException e) {
				Log.e("NoteItApplication.hashString", e.getMessage());
				return clearText;
			}
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

    static int getPreferredTextAppearance(Context context, int fontSize, ItemType type) {
    	
    	int appearance = 0;
    	switch (type) {
    	
    	case PENDING:
        	if (fontSize == 3)
    			appearance = R.attr.TextAppearance_Item_Small;
        	else if (fontSize == 2)
    			appearance = R.attr.TextAppearance_Item_Medium;
        	else
    			appearance = R.attr.TextAppearance_Item_Large;
    		break;

    	case BOLD:
        	if (fontSize == 3)
    			appearance = R.attr.TextAppearance_Item_Bold_Small;
        	else if (fontSize == 2)
    			appearance = R.attr.TextAppearance_Item_Bold_Medium;
        	else
    			appearance = R.attr.TextAppearance_Item_Bold_Large;
    		break;
    		
    	case DONE:
        	if (fontSize == 3) {
       			appearance = R.attr.TextAppearance_Item_Done_Small;
        	}
        	else if (fontSize == 2) {
       			appearance = R.attr.TextAppearance_Item_Done_Medium;
        	}
        	else {
       			appearance = R.attr.TextAppearance_Item_Done_Large;
        	}
    		break;

    	case GROUP:
    		if (fontSize == 3) {
    			appearance = R.attr.TextAppearance_Group_Small;
    		}
    		else if (fontSize == 2) {
    			appearance = R.attr.TextAppearance_Group_Medium;
    		}
    		else {
       			appearance = R.attr.TextAppearance_Group_Large;
    		}
    		break;
    	}
    	
    	if (appearance > 0) {
    		Resources.Theme theme = context.getTheme();
    		TypedValue 		styleID = new TypedValue();
    		if (theme.resolveAttribute(appearance, styleID, true)) {
    		     return styleID.data;
    		}
    	}
    	return appearance;
    }
}
