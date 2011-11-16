package com.geekjamboree.noteit;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

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
	}
	
	// Represents each item on the current shopping list
	public class Item {
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
	
	public void fetchCategories(AsyncInvokeURLTask.OnPostExecuteListener inPostExecute){
		try {
			mCategories.clear();
			
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("command", "do_get_categories"));
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
		if (index < mCategories.size())
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
	        nameValuePairs.add(new BasicNameValuePair("arg1", "Y"));
	        nameValuePairs.add(new BasicNameValuePair("arg2", "0"));
	        nameValuePairs.add(new BasicNameValuePair("arg3", "0"));
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
