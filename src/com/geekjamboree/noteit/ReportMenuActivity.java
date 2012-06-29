package com.geekjamboree.noteit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.geekjamboree.noteit.ItemListActivity.ItemType;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

public class ReportMenuActivity extends ExpandableListActivity {

	final String GROUP_NAME 				= "GROUP_NAME";
	final String REPORT_NAME 				= "REPORT_NAME";
	final String REPORT_ID					= "REPORT_ID";
	final String REPORT_DESCRIPTION			= "REPORT_DESCRIPTION";
	final String REPORT_TYPE				= "REPORT_TYPE";
	
	final int REPORT_TYPE_ITEM				= 0;
	final int REPORT_TYPE_CATEGORY			= 1;
	
	// Report IDs
	protected ExpandableLVRightIndicator 	mListView;
	protected TitleBar 						mToolbar;
	protected SharedPreferences				mPrefs = null;
	protected int 							mFontSize = 3;
	
	
	class SimpleExpandaleAdapterWithFontSize extends SimpleExpandableListAdapter {
		
		protected int 		mGroupTo[];
		protected int 		mChildTo[];
		protected Context	mContext;
		
		public SimpleExpandaleAdapterWithFontSize(
				Context context,
				List<? extends Map<String, ?>> groupData, 
				int groupLayout,
				String[] groupFrom, 
				int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, 
				String[] childFrom, 
				int[] childTo) {
			super(
				context, 
				groupData, 
				groupLayout, 
				groupFrom, 
				groupTo, 
				childData,
				childLayout, 
				childFrom, 
				childTo);
			mGroupTo = groupTo;
			mChildTo = childTo;
			mContext = context;
		}

		@Override
		public View getChildView(
				int groupPosition, 
				int childPosition,
				boolean isLastChild, 
				View convertView, 
				ViewGroup parent) {
			View view = super.getChildView(groupPosition, childPosition, isLastChild,
					convertView, parent);
			if (view != null) {
				TextView textView = (TextView) view.findViewById(mChildTo[0]);
				if (textView != null) {
					textView.setTextAppearance(
						mContext, 
						NoteItApplication.getPreferredTextAppearance(
								mContext, mFontSize, ItemType.BOLD));
				}
			}
			return view;
		}

		@Override
		public View getGroupView(
				int groupPosition, 
				boolean isExpanded,
				View convertView, 
				ViewGroup parent) {
			View view = super.getGroupView(groupPosition, isExpanded, convertView, parent);
			if (view != null) {
				TextView textView = (TextView) view.findViewById(mGroupTo[0]);
				if (textView != null) {
					textView.setTextAppearance(
						mContext, 
						NoteItApplication.getPreferredTextAppearance(
							mContext, mFontSize, ItemType.GROUP));
				}
			}
			return view;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	TitleBar.RequestNoTitle(this);
    	ThemeUtils.onActivityCreateSetTheme(this);
		setContentView(R.layout.reportmenuactivity);
		mToolbar = (TitleBar) findViewById(R.id.reportmenu_title);

        mListView = (ExpandableLVRightIndicator) findViewById(android.R.id.list);
        if (mListView != null) {
        	mListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
				public boolean onChildClick(
						ExpandableListView parent, 
						View v,
						int groupPosition, 
						int childPosition, 
						long id) {
					@SuppressWarnings("unchecked")
					Map<String, String> child = (Map<String, String>) 
						mListView.getExpandableListAdapter().getChild(groupPosition, childPosition);
					if (child != null) {
						int reportId = Integer.valueOf(child.get(REPORT_ID));
						int reportType = Integer.valueOf(child.get(REPORT_TYPE));
						if (reportType == REPORT_TYPE_ITEM)
							doDisplayReport(reportId);
						else if (reportType == REPORT_TYPE_CATEGORY)
							doDisplayChart(reportId);
					}
					return false;
				}
			});
        }
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mToolbar.SetTitle(getString(R.string.reporting_title));
        doSetupToolbarButtons();
        populateReporingOptions();
	}

	@Override
	protected void onResume() {
        mFontSize = Integer.valueOf(mPrefs.getString("Item_Font_Size", "3"));
        mListView.invalidateViews();
		super.onResume();
	}

	void populateReporingOptions() {
		List<Map<String, String>> 		groups = new ArrayList<Map<String, String>>();
		
		Map<String, String>	 purchaseGroup = new HashMap<String, String>();
		purchaseGroup.put(GROUP_NAME, getString(R.string.reporting_purchased_group));
		groups.add(purchaseGroup);

		Map<String, String>  pendingGroup = new HashMap<String, String>();
		pendingGroup.put(GROUP_NAME, getString(R.string.reporting_pending_group));
		groups.add(pendingGroup);
		
		List<List<Map<String, String>>>	children = new ArrayList<List<Map<String, String>>>();
		List<Map<String, String>> 		purchasedReports = new ArrayList<Map<String, String>>();
		List<Map<String, String>>		pendingReports = new ArrayList<Map<String, String>>();
		
		Map<String, String> 			purchasedToday = new HashMap<String, String>();
		purchasedToday.put(REPORT_NAME, getString(R.string.reporting_putchased_today));
		purchasedToday.put(REPORT_ID, String.valueOf(ReportActivity.REPORT_ITEM_PURCHASED_TODAY));
		purchasedToday.put(REPORT_DESCRIPTION, getString(R.string.reporting_purchased_today_desc));
		purchasedToday.put(REPORT_TYPE, String.valueOf(REPORT_TYPE_ITEM));
		purchasedReports.add(purchasedToday);
		
		Map<String, String> 			purchasedYesterday = new HashMap<String, String>();
		purchasedYesterday.put(REPORT_NAME, getString(R.string.reporting_putchased_yesterday));
		purchasedYesterday.put(REPORT_ID, String.valueOf(ReportActivity.REPORT_ITEM_PURCHASED_YESTERDAY));
		purchasedYesterday.put(REPORT_DESCRIPTION, getString(R.string.reporting_purchased_yesterday_desc));
		purchasedYesterday.put(REPORT_TYPE, String.valueOf(REPORT_TYPE_ITEM));
		purchasedReports.add(purchasedYesterday);

		Map<String, String> 			purchasedThisWeek = new HashMap<String, String>();
		purchasedThisWeek.put(REPORT_NAME, getString(R.string.reporting_purchased_thisweek));
		purchasedThisWeek.put(REPORT_ID, String.valueOf(ReportActivity.REPORT_ITEM_PURCHASED_THISWEEK));
		purchasedThisWeek.put(REPORT_DESCRIPTION, getString(R.string.reporting_purchased_thisweek_desc));
		purchasedThisWeek.put(REPORT_TYPE, String.valueOf(REPORT_TYPE_ITEM));
		purchasedReports.add(purchasedThisWeek);
		
		Map<String, String> 			purchasedLastWeek = new HashMap<String, String>();
		purchasedLastWeek.put(REPORT_NAME, getString(R.string.reporting_purchased_lastweek));
		purchasedLastWeek.put(REPORT_ID, String.valueOf(ReportActivity.REPORT_ITEM_PURCHASED_LASTWEEK));
		purchasedLastWeek.put(REPORT_DESCRIPTION, getString(R.string.reporting_purchased_lastweek_desc));
		purchasedLastWeek.put(REPORT_TYPE, String.valueOf(REPORT_TYPE_ITEM));
		purchasedReports.add(purchasedLastWeek);

		Map<String, String> 			purchasedThisMonth = new HashMap<String, String>();
		purchasedThisMonth.put(REPORT_NAME, getString(R.string.reporting_purchased_thisMonth));
		purchasedThisMonth.put(REPORT_ID, String.valueOf(ReportActivity.REPORT_CATEGORY_PURCHASED_THISMONTH));
		purchasedThisMonth.put(REPORT_DESCRIPTION, getString(R.string.reporting_purchased_thisMonth_desc));
		purchasedThisMonth.put(REPORT_TYPE, String.valueOf(REPORT_TYPE_CATEGORY));
		purchasedReports.add(purchasedThisMonth);
		
		Map<String, String> 			purchasedLastMonth = new HashMap<String, String>();
		purchasedLastMonth.put(REPORT_NAME, getString(R.string.reporting_purchased_lastMonth));
		purchasedLastMonth.put(REPORT_ID, String.valueOf(ReportActivity.REPORT_CATEGORY_PURCHASED_LASTMONTH));
		purchasedLastMonth.put(REPORT_DESCRIPTION, getString(R.string.reporting_purchased_lastMonth_desc));
		purchasedLastMonth.put(REPORT_TYPE, String.valueOf(REPORT_TYPE_CATEGORY));
		purchasedReports.add(purchasedLastMonth);
		
//		Map<String, String> 			purchasedAll = new HashMap<String, String>();
//		purchasedAll.put(REPORT_NAME, getString(R.string.reporting_purchased_All));
//		purchasedAll.put(REPORT_ID, String.valueOf(ReportActivity.REPORT_PURCHASED_ALL));
//		purchasedReports.add(purchasedAll);

		Map<String, String>				pendingAll = new HashMap<String, String>();
		pendingAll.put(REPORT_NAME, getString(R.string.reporting_pending_all_menu));
		pendingAll.put(REPORT_ID, String.valueOf(ReportActivity.REPORT_ITEM_PENDING_ALL));
		pendingAll.put(REPORT_DESCRIPTION, getString(R.string.reporting_pending_all_desc));
		pendingAll.put(REPORT_TYPE, String.valueOf(REPORT_TYPE_ITEM));
		pendingReports.add(pendingAll);

		children.add(purchasedReports);
		children.add(pendingReports);
		
		SimpleExpandaleAdapterWithFontSize	adapter = new SimpleExpandaleAdapterWithFontSize(
			this, 
			groups, 
			R.layout.reportactivity_group,
			new String[] {GROUP_NAME}, 
			new int[] {R.id.reportactivity_group_name}, 
			children, 
			R.layout.reportactivity_item, 
			new String[] {REPORT_NAME, REPORT_DESCRIPTION}, 
			new int[] {R.id.reportactvity_item_name, R.id.reportactvity_item_description});
		mListView.setAdapter(adapter);
		
		doExpandAll();
	}

	void doDisplayReport(int reportId) {
		Intent intent = new Intent(this, ReportActivity.class);
		intent.putExtra(ReportActivity.REPORT_TYPE, reportId);
		startActivity(intent);
	}
	
	void doDisplayChart(int reportId) {
		Intent intent = new Intent(this, ReportCategoryChart.class);
		intent.putExtra(ReportCategoryChart.REPORT_TYPE, reportId);
		startActivity(intent);
	}

	protected void doSetupToolbarButtons() {

    	mToolbar.addVerticalSeparator(this, false);

    	ImageButton expandAll = mToolbar.addRightAlignedButton(R.drawable.expand, true, true);
    	expandAll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doExpandAll();
			}
		});
    	
    	ImageButton collapseAll = mToolbar.addRightAlignedButton(R.drawable.collapse, false, true);
    	collapseAll.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doCollapseAll();
			}
		});

    	ImageButton settingsButton = mToolbar.addRightAlignedButton(R.drawable.settings, false, true);
    	settingsButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
    			startActivity(new Intent(ReportMenuActivity.this, MainPreferenceActivity.class));
			}
		});

    	ImageButton homeButton = mToolbar.addLeftAlignedButton(R.drawable.home, true, true);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(ReportMenuActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

    	mToolbar.addVerticalSeparator(this, true);
    }

    protected void doExpandAll() {
    	SimpleExpandableListAdapter adapter = (SimpleExpandableListAdapter) mListView.getExpandableListAdapter();
    	for (int i = 0; i < adapter.getGroupCount(); i++){
    		mListView.expandGroup(i);
    	}        	
    }
    
    protected void doCollapseAll() {
    	SimpleExpandableListAdapter adapter = (SimpleExpandableListAdapter) mListView.getExpandableListAdapter();
    	for (int i = 0; i < adapter.getGroupCount(); i++){
    		mListView.collapseGroup(i);
    	}        	
    	
    }
}
