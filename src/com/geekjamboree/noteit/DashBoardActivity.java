package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.ItemListActivity.ItemType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class DashBoardActivity extends Activity {

	protected GridView 		mGridView;
	TitleBar				mToolbar;
	
	protected final int DASHBOARD_SHOPPINGLISTS = 0;
	protected final int DASHBOARD_CATEGORIES = 1;
	protected final int DASHBOARD_REPORTS = 2;
	
	public class DashboardItem {
		public String 		mText;
		public int			mImageResourceID;
		public int			mID = 0;
	}

	public class DashBoardAdapter extends BaseAdapter {
	    
		private LayoutInflater				mLayoutInflater;
		private ArrayList<DashboardItem> 	mItems = new ArrayList<DashboardItem>();
		private Context						mContext;
		
	    public DashBoardAdapter(Context c) {
	    	mContext = c;
	    	mLayoutInflater = (LayoutInflater) getApplication().getSystemService(LAYOUT_INFLATER_SERVICE);
	    }

	    public void addItem(String text, int resourceID, int itemID) {
	    	DashboardItem item = new DashboardItem();
	    	item.mText = text;
	    	item.mImageResourceID = resourceID;
	    	item.mID = itemID;
	    	mItems.add(item);
	    }
	    
	    public int getCount() {
	    	
	        return mItems.size();
	    }

	    public Object getItem(int position) {
	    	
	        return mItems.get(position);
	    }

	    public long getItemId(int position) {
	    	
	        return mItems.get(position).mID;
	    }

	    // create a new ImageView for each item referenced by the Adapter
	    public View getView(int position, View convertView, ViewGroup parent) {
	        
	    	ViewGroup view;
	        if (convertView == null) {  // if it's not recycled, initialize some attributes
	        	view = (ViewGroup) mLayoutInflater.inflate(R.layout.dashboard_item, parent, false);
	        } else {
	            view = (ViewGroup) convertView;
	        }

        	TextView 	text = (TextView) view.findViewById(R.id.dashboard_item_text);
	        
	        text.setText(mItems.get(position).mText);
	        text.setTextAppearance(mContext, NoteItApplication.getPreferredTextAppearance(mContext, 3, ItemType.GROUP));
	        text.setCompoundDrawablesWithIntrinsicBounds(0, mItems.get(position).mImageResourceID, 0, 0);
	        final float scale = getResources().getDisplayMetrics().density; 
	        GridView.LayoutParams lp = new GridView.LayoutParams((int)(128 * scale + 0.5), (int)(128 * scale + 0.5));
	        view.setLayoutParams(lp);
        	view.setPadding(10, 10, 10, 10);
	        return view;
	    }
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        
		super.onCreate(savedInstanceState);
    	TitleBar.RequestNoTitle(this);
    	ThemeUtils.onActivityCreateSetTheme(this);
        setContentView(R.layout.dashboard);
        
        mToolbar = (TitleBar) findViewById(R.id.dashboard_title);
        mToolbar.SetTitle(getResources().getText(R.string.app_name));
		doSetupToolbarButton();
		
        DashBoardAdapter adapter = new DashBoardAdapter(this);
        
        adapter.addItem(
        	"Shopping Lists", 
        	ThemeUtils.getResourceIdFromAttribute(this, R.attr.Dashboard_ShoppingList), 
        	DASHBOARD_SHOPPINGLISTS);
        
        adapter.addItem(
        	"Categories", 
        	ThemeUtils.getResourceIdFromAttribute(this, R.attr.Dashboard_Category), 
        	DASHBOARD_CATEGORIES);
        
        adapter.addItem(
        	"Reports", 
        	ThemeUtils.getResourceIdFromAttribute(this, R.attr.Dashboard_Reports), 
        	DASHBOARD_REPORTS);
        
        mGridView = (GridView) findViewById(R.id.category_gridview);
        mGridView.setAdapter(adapter);
        
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		
        		DashboardItem item = (DashboardItem) mGridView.getAdapter().getItem(position);
        		switch(item.mID) {
        		case DASHBOARD_SHOPPINGLISTS:
        			doDashboardShoppingLists();
        			break;
        		case DASHBOARD_CATEGORIES:
        			doDashboardCategories();
        			break;
        		case DASHBOARD_REPORTS:
        			doDashboardReports();
        			break;
        		}
        	}
		});
        
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getInt(LoginActivity.DISPLAY_UNREAD_MESSAGES, 0) > 0) {
	        // Read the messages in inbox
	        final NoteItApplication app = (NoteItApplication) getApplication();
	        if (app != null) {
				InboxMessages inboxMessages = new InboxMessages(app, DashBoardActivity.this, mGridView);
				if (inboxMessages != null) {
					inboxMessages.doDisplayUnreadMessages();
				}
	        }
        }
	}


	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.dashboard_menu, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch(item.getItemId()){
    		case R.id.dashboard_settings:
    			startActivity(new Intent(this, MainPreferenceActivity.class));
    			return true;
    		case R.id.dashboard_signout:
    			doSignOut();
    			return true;
    	}
    	
    	return false;
    }
        	
	protected void doDashboardShoppingLists() {
        
		Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
		
	}
	
	protected void doDashboardCategories() {
		
        Intent intent = new Intent(this, CategoryListActivity.class);
        startActivity(intent);
	}

	protected void doDashboardReports() {
		Intent intent = new Intent(this, ReportMenuActivity.class);
		startActivity(intent);
	}
	
	protected void doSetupToolbarButton() {
		
		ImageButton logOut = mToolbar.addRightAlignedButton(R.drawable.sign_out);
		logOut.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				doSignOut();
			}
		});
		
        ImageButton settings = mToolbar.addRightAlignedButton(R.drawable.settings);
        settings.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(DashBoardActivity.this, MainPreferenceActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private void doSignOut() {
		Intent intent = new Intent(DashBoardActivity.this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(LoginActivity.DONT_LOGIN, true);
		startActivity(intent);
		finish();
	}
}
