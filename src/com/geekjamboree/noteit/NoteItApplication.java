package com.geekjamboree.noteit;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NoteItApplication extends Application {

	// Represents each shopping list that the user has
	public static class ShoppingList {
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
	public static class Category {
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
	public static class Item {
		public String 	mName 			= "";
		public long		mID				= 0;
		public long 	mCategoryID 	= 0;	
	
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
	}
	
	public static interface OnFetchCategoriesListener {
		void onPostExecute(long resultCode, ArrayList<Category> categories, String message);
	}
	
	private long						mUserID = 0;
	private long						mCurrentShoppingList = 0;
	private ArrayList<ShoppingList>		mShoppingLists = new ArrayList<ShoppingList>();
	private ArrayList<Category>			mCategories = new ArrayList<Category>();
	
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
	
	public void fetchShoppingLists(AsyncInvokeURLTask.OnPostExecuteListener inPostExecute){
		try {
			mShoppingLists.clear();
	    	
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_get_shop_list"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", String.valueOf(getUserID())));

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
	
	public void addShoppingList(long listID, String listName){
		Log.i("NoteItApplication.addShoppingList", 
    			"Item ID: " + listID + " Item Name: " + listName);
		mShoppingLists.add(new ShoppingList(listID, listName));
	}
	
	public int getShoppingListCount(){
		return mShoppingLists.size();
	}
	
	public ShoppingList getShoppingList(int index){
		return mShoppingLists.get(index);
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
						
			        	if (retval == 0 && !json.isNull("arg1")){
				        	JSONArray jsonArr = json.getJSONArray("arg1");
				        	
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
			        	}
					} catch (JSONException e) {
						Log.e("NoteItApplication.loginUser: JSON Exception", e.getMessage());
						mListener.onPostExecute(-1L, mCategories, e.getMessage());
					}
				}
			}
        	
        	GetCategoriesTask  myListener = new GetCategoriesTask(inPostExecute);
			AsyncInvokeURLTask task = new AsyncInvokeURLTask(nameValuePairs, myListener);
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
	
	public void fetchItems(AsyncInvokeURLTask.OnPostExecuteListener inPostExecute){
		try {

			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_list_shop_items"));
	        nameValuePairs.add(new BasicNameValuePair("arg1", "Y")); // Show Purchased Items
	        nameValuePairs.add(new BasicNameValuePair("arg2", "1")); // Shopping List ID
	        nameValuePairs.add(new BasicNameValuePair("arg3", "0")); // Start @ index
	        nameValuePairs.add(new BasicNameValuePair("arg4", String.valueOf(getUserID())));
	        
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
}
