package com.geekjamboree.noteit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DashBoardActivity extends Activity {

	protected GridView mGridView;

	protected final int DASHBOARD_SHOPPINGLISTS = 0;
	protected final int DASHBOARD_CATEGORIES = 1;
	
	public class DashboardItem {
		public String 		mText;
		public int			mImageResourceID;
		public int			mID = 0;
	}

	public class DashBoardAdapter extends BaseAdapter {
	    
//		private Context 					mContext;
		private ArrayList<DashboardItem> 	mItems = new ArrayList<DashboardItem>();
		
	    public DashBoardAdapter(/*Context c*/) {
	        //mContext = c;
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
	        	LayoutInflater li = (LayoutInflater) getApplication().getSystemService(LAYOUT_INFLATER_SERVICE);
	        	view = (ViewGroup) li.inflate(R.layout.dashboard_item, parent, false);
	        } else {
	            view = (ViewGroup) convertView;
	        }

        	ImageView 	image = (ImageView) view.findViewById(R.id.dashboard_item_image);
        	TextView 	text = (TextView) view.findViewById(R.id.dashboard_item_text);
	        
	        text.setText(mItems.get(position).mText);
	        image.setImageResource(mItems.get(position).mImageResourceID);
	        final float scale = getResources().getDisplayMetrics().density; 
	        view.setLayoutParams(new GridView.LayoutParams((int)(128 * scale + 0.5), (int)(128 * scale + 0.5)));
        	view.setPadding(10, 10, 10, 10);
        	image.setScaleType(ImageView.ScaleType.CENTER);
	        return view;
	    }
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        
		super.onCreate(savedInstanceState);
        CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.dashboard);
        toolbar.SetTitle(getResources().getText(R.string.dashboard_title));
        
        DashBoardAdapter adapter = new DashBoardAdapter(/*this*/);
        adapter.addItem("Shopping Lists", R.drawable.cart_big, DASHBOARD_SHOPPINGLISTS);
        adapter.addItem("Categories", R.drawable.category_large, DASHBOARD_CATEGORIES);
        
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
        		}
        	}
		});
	}

	protected void doDashboardShoppingLists() {
        
		Intent intent = new Intent(this, ShoppingListActivity.class);
        startActivity(intent);
		
	}
	
	protected void doDashboardCategories() {
		
        Intent intent = new Intent(this, CategoryListActivity.class);
        startActivity(intent);
	}
}
