package com.geekjamboree.noteit;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.geekjamboree.noteit.NoteItApplication.ShoppingListItem;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ListView;

public class ShoppingListActivity extends ListActivity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) { 
    	super.onCreate(savedInstanceState);
        setTitle(getResources().getText(R.string.shoplistsactivity_title));
        getListView().setDividerHeight(2);
    	((NoteItApplication)getApplication()).fetchShoppingLists(new ShoppingListActivityOnPostExecuteListener(this));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shoplists_menu, menu);
        return true;
    }
    
   	class ShoppingListActivityOnPostExecuteListener implements AsyncInvokeURLTask.OnPostExecuteListener {
    	Context mContext;
    	
    	ShoppingListActivityOnPostExecuteListener(Context context){
    		mContext = context;
    	}
		
    	public void onPostExecute(JSONObject json) {
			try {
	        	long 						retval = json.getLong("JSONRetVal");
	        	ArrayList<ShoppingListItem> shopList = new ArrayList<ShoppingListItem>();
    			
	        	if (retval == 0 && !json.isNull("arg1")){
		        	JSONArray jsonArr = json.getJSONArray("arg1");
		        	
		        	// [TODO]: This doesn't feel right, calling the app object
		        	// to read shopping list items and having to populate them
		        	// in the object from here. Figure out an elegant way to 
		        	// handle this.
		        	for (int index = 0; index < jsonArr.length(); index++){
		        		JSONObject thisObj = jsonArr.getJSONObject(index);
		        		ShoppingListItem thisItem = ((NoteItApplication)getApplication()).new ShoppingListItem(
		        				Long.parseLong(thisObj.getString("listID")),
    							thisObj.getString("listName"));
		        		
		        		((NoteItApplication)getApplication()).addShoppingListItem(
		        				thisItem.mID,
		        				thisItem.mName);
		        		
		        		shopList.add(thisItem);
		        	}

		        	ListView lv = getListView();
		        	if (!shopList.isEmpty()){
	                	setListAdapter(new ArrayAdapter<ShoppingListItem>(mContext, R.layout.list_item, shopList));
	                	lv.setTextFilterEnabled(true);
	                	lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	                		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	                			// When clicked, show a toast with the TextView text
	                			Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
	                			
	                			// Invoke the category activity
	                            Intent myIntent = new Intent(view.getContext(), CategoryListActivity.class);
	                            startActivity(myIntent);
	            			}
	                	});
	            	}
	            	else {
	            		// Display alert with error message
	            		Toast.makeText(getApplicationContext(), "There are no lists. Please add a new list.", Toast.LENGTH_LONG).show();
	            	}
		        	
	        	} else {
	        		String errMsg = json.getString("JSONRetMessage");
	        		Toast.makeText(getApplicationContext(), "Error Occurred:" + errMsg, Toast.LENGTH_LONG).show();
	        	}
			} catch (JSONException e){
				Toast.makeText(getApplicationContext(), "The server seems to be out of its mind. Please try later.", Toast.LENGTH_SHORT).show();
				Log.e("NoteItApplication.loginUser", e.getMessage());
			}
		}
	}
    
    // Just for testing
    static final String[] COUNTRIES = new String[] {
        "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
        "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina",
        "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan",
        "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",
        "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
        "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory",
        "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
        "Cote d'Ivoire", "Cambodia", "Cameroon", "Canada", "Cape Verde",
        "Cayman Islands", "Central African Republic", "Chad", "Chile", "China",
        "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo",
        "Cook Islands", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic",
        "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
        "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
        "Estonia", "Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji", "Finland",
        "Former Yugoslav Republic of Macedonia", "France", "French Guiana", "French Polynesia",
        "French Southern Territories", "Gabon", "Georgia", "Germany", "Ghana", "Gibraltar",
        "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau",
        "Guyana", "Haiti", "Heard Island and McDonald Islands", "Honduras", "Hong Kong", "Hungary",
        "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica",
        "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos",
        "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg",
        "Macau", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands",
        "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia", "Moldova",
        "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia",
        "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand",
        "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea", "Northern Marianas",
        "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru",
        "Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar",
        "Reunion", "Romania", "Russia", "Rwanda", "Sqo Tome and Principe", "Saint Helena",
        "Saint Kitts and Nevis", "Saint Lucia", "Saint Pierre and Miquelon",
        "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Saudi Arabia", "Senegal",
        "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands",
        "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "South Korea",
        "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard and Jan Mayen", "Swaziland", "Sweden",
        "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "The Bahamas",
        "The Gambia", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey",
        "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Virgin Islands", "Uganda",
        "Ukraine", "United Arab Emirates", "United Kingdom",
        "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan",
        "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Wallis and Futuna", "Western Sahara",
        "Yemen", "Yugoslavia", "Zambia", "Zimbabwe"
      };    
}
