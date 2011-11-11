package com.geekjamboree.noteit;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class NoteItApplication extends Application {

	public class ShoppingListItem {
		public String 	mName = "";
		public long		mID = 0;
		
		public ShoppingListItem(long itemID, String itemName){
			mName = itemName;
			mID = itemID;
		}
		
		public String toString(){
			return mName;
		}
	}
	
	public class Category {
		public String 	mName = "";
		public long 	mID = 0;
		
		public Category(long categoryID, String categoryName){
			mName = categoryName;
			mID = categoryID;
		}
		
		public String toString(){
			return mName;
		}
	}
	
	private long						mUserID = 0;
	private ArrayList<ShoppingListItem>	mShoppingList = new ArrayList<NoteItApplication.ShoppingListItem>();
	private ArrayList<Category>			mCategories = new ArrayList<NoteItApplication.Category>();
	
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
			mShoppingList.clear();
	    	
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
	
	public void addShoppingListItem(long itemID, String itemName){
		Log.i("NoteItApplication.addShoppingListItem", 
    			"Item ID: " + itemID + " Item Name: " + itemName);
		mShoppingList.add(new ShoppingListItem(itemID, itemName));
	}
	
	public int getShoppingListItemCount(){
		return mShoppingList.size();
	}
	
	public ShoppingListItem getShoppingListItem(int index){
		return mShoppingList.get(index);
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

	public void addCategory(long itemID, String itemName){
		Log.i("NoteItApplication.addCategory", 
    			"Item ID: " + itemID + " Item Name: " + itemName);
		mCategories.add(new Category(itemID, itemName));
	}
}
