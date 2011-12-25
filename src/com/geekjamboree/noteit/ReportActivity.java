package com.geekjamboree.noteit;

import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.geekjamboree.noteit.NoteItApplication.CategoryReportItem;
import com.geekjamboree.noteit.NoteItApplication.ItemReportItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.Toast;

public class ReportActivity extends Activity {

	public static final String REPORT_TYPE 				= "REPORT_TYPE";
	
//	public static final int REPORT_PURCHASED_TODAY 					= 1;
//	public static final int REPORT_PURCHASED_YESTERDAY 				= 2;
//	public static final int REPORT_PURCHASED_THISWEEK 				= 3;
//	public static final int REPORT_PURCHASED_LASTWEEK				= 4;
	public static final int REPORT_CATEGORY_PURCHASED_THISMONTH				= 5;
	public static final int REPORT_CATEGORY_PURCHASED_LASTMONTH		= 6;
	public static final int REPORT_PURCHASED_ALL					= 7;
	
	// We don't want to support monthly items report 
	// as this will potentially return a lot for items
	public static final int REPORT_ITEM_PURCHASED_TODAY		= 8;
	public static final int REPORT_ITEM_PURCHASED_YESTERDAY	= 9;
	public static final int REPORT_ITEM_PURCHASED_THISWEEK	= 10;
	public static final int REPORT_ITEM_PURCHASED_LASTWEEK	= 11;
	
	class ToolbarWrapper extends CustomTitlebarWrapper {

		public ToolbarWrapper(Activity parent) {
			super(parent);
		}

		public void SetTitle(CharSequence title) {
			super.SetTitle(title);
			doSetupToolbarButtons();
		}
	}

	ToolbarWrapper			mToolbar;
	WebView					mTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mToolbar = new ToolbarWrapper(this);
		setContentView(R.layout.reportactvity);
		mTextView = (WebView) findViewById(R.id.webview);
		mTextView.setBackgroundColor(0);
		
		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				int reportId = bundle.getInt(REPORT_TYPE);
				doReport(reportId);
			}
		}
	}

	class onDisplayItemReportListener implements NoteItApplication.OnGetItemReportListener {
		public void onPostExecute(long resultCode, ArrayList<ItemReportItem> items, String message) {
			if (resultCode == 0) {
				float total = 0f;
				for (int index = 0; index < items.size(); index++) {
					ItemReportItem item = items.get(index);
					total += item.mPrice;
				}
				String text = getString(R.string.reporting_category_head); 
				text += "<tr>";
				text += "<th class='la'>" + getString(R.string.reporting_item_name) + "</th>";
				text += "<th class='ra'>" + getString(R.string.reporting_category_price) + "</th>";
				text += "<th class='ra'>" + getString(R.string.reporting_category_percentage) + "</th>";
				text += "</tr>";
				String categoryItemFormat = getString(R.string.reporting_category_report_item);
				NoteItApplication app = (NoteItApplication) getApplication();
				NumberFormat numberFormat = NumberFormat.getInstance();
				String currencyFormat = app.getCurrencyFormat(true);
				numberFormat.setMinimumFractionDigits(2);
				numberFormat.setMaximumFractionDigits(2);
				for (int index = 0; index < items.size(); index++) {
					ItemReportItem item = items.get(index);
					if (item != null) {
						String price = String.format(currencyFormat, numberFormat.format(item.mPrice));
						String percentage = null;
						if (total > 0)
							percentage = numberFormat.format(item.mPrice / total * 100);
						else
							percentage = "";
						String row = String.format(
								categoryItemFormat.toString(), 
								item.mItemName, 
								price,
								percentage);
						text += row;
					}
				}
				String categoryFooter = getString(R.string.reporting_category_footer);
				String strTotal = String.format(currencyFormat, numberFormat.format(total));
				text += String.format(categoryFooter, strTotal);
				mTextView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
			} else 
				Toast.makeText(ReportActivity.this, message, Toast.LENGTH_LONG).show();		}
	}
	
	class onDisplayCategoryReportListener implements NoteItApplication.OnGetCategoryReportListener {
		
		public void onPostExecute(long resultCode,
				ArrayList<CategoryReportItem> items, String message) {
			if (resultCode == 0) {
				float total = 0f;
				for (int index = 0; index < items.size(); index++) {
					CategoryReportItem item = items.get(index);
					total += item.mPrice;
				}
				String text = getString(R.string.reporting_category_head); 
				text += "<tr>";
				text += "<th class='la'>" + getString(R.string.reporting_category_category) + "</th>";
				text += "<th class='ra'>" + getString(R.string.reporting_category_price) + "</th>";
				text += "<th class='ra'>" + getString(R.string.reporting_category_percentage) + "</th>";
				text += "</tr>";
				String categoryItemFormat = getString(R.string.reporting_category_report_item);
				NoteItApplication app = (NoteItApplication) getApplication();
				NumberFormat numberFormat = NumberFormat.getInstance();
				String currencyFormat = app.getCurrencyFormat(true);
				numberFormat.setMinimumFractionDigits(2);
				numberFormat.setMaximumFractionDigits(2);
				for (int index = 0; index < items.size(); index++) {
					CategoryReportItem item = items.get(index);
					if (item != null) {
						String price = String.format(currencyFormat, numberFormat.format(item.mPrice));
						String percentage = null;
						if (total > 0)
							percentage = numberFormat.format(item.mPrice / total * 100);
						else
							percentage = "";
						String row = String.format(
								categoryItemFormat.toString(), 
								item.mCategoryName, 
								price,
								percentage);
						text += row;
					}
				}
				String categoryFooter = getString(R.string.reporting_category_footer);
				String strTotal = String.format(currencyFormat, numberFormat.format(total));
				text += String.format(categoryFooter, strTotal);
				mTextView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
			} else 
				Toast.makeText(ReportActivity.this, message, Toast.LENGTH_LONG).show();		
		}
	}
		
	protected void doReport(int reportId) {
		Date from = null;
		Date to = null;
		NoteItApplication app = (NoteItApplication) getApplication();
		
		switch (reportId) {
		case REPORT_ITEM_PURCHASED_TODAY:
			from = new Date(System.currentTimeMillis());
			to = new Date(System.currentTimeMillis());
			mToolbar.SetTitle(
					getString(R.string.reporting_putchased_today) + " (" +
					from.toString() + ")");
			app.getReport(true, from, to, new onDisplayItemReportListener());
			break;
		case REPORT_ITEM_PURCHASED_YESTERDAY:
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -1);
			from = new Date(calendar.getTimeInMillis());
			to = new Date(calendar.getTimeInMillis());
			mToolbar.SetTitle(
					getString(R.string.reporting_putchased_yesterday) + " (" +
					from.toString() + ")");
			app.getReport(true, from, to, new onDisplayItemReportListener());
			break;
		case REPORT_ITEM_PURCHASED_THISWEEK:
			Calendar thisWeekCalendar = Calendar.getInstance();
			to = new Date(thisWeekCalendar.getTimeInMillis());
			thisWeekCalendar.add(Calendar.DATE, -thisWeekCalendar.get((Calendar.DAY_OF_WEEK)) + 1);
			from = new Date(thisWeekCalendar.getTimeInMillis());
			mToolbar.SetTitle(
					getString(R.string.reporting_purchased_thisweek) + " (" +
					from.toString() + " " +
					to.toString() + ")");
			app.getReport(true, from, to, new onDisplayItemReportListener());
			break;
		case REPORT_ITEM_PURCHASED_LASTWEEK:
			Calendar lastWeekCalendar = Calendar.getInstance();
			lastWeekCalendar.add(Calendar.DATE, -lastWeekCalendar.get(Calendar.DAY_OF_WEEK));
			to = new Date(lastWeekCalendar.getTimeInMillis());
			lastWeekCalendar.add(Calendar.DATE, -6);
			from = new Date(lastWeekCalendar.getTimeInMillis());
			mToolbar.SetTitle(getString(R.string.reporting_purchased_lastweek) + " (" +
					from.toString() + " " +
					to.toString() + ")");
			app.getReport(true, from, to, new onDisplayItemReportListener());
			break;
		case REPORT_CATEGORY_PURCHASED_THISMONTH:
			Calendar thisMonth = Calendar.getInstance();
			to = new Date(thisMonth.getTimeInMillis());
			thisMonth.set(Calendar.DATE, 1);
			from = new Date(thisMonth.getTimeInMillis());
			mToolbar.SetTitle(
					getString(R.string.reporting_purchased_thisMonth) + " (" +
					from.toString() + " " + to.toString() + ")");
			app.getReport(true, from, to, new onDisplayCategoryReportListener());
			break;
		case REPORT_CATEGORY_PURCHASED_LASTMONTH:
			Calendar lastMonthCalendar = Calendar.getInstance();
			lastMonthCalendar.set(Calendar.DATE, 1);
			lastMonthCalendar.add(Calendar.DATE, -1);
			to = new Date(lastMonthCalendar.getTimeInMillis());
			lastMonthCalendar.set(Calendar.DATE, 1);
			from = new Date(lastMonthCalendar.getTimeInMillis());
			mToolbar.SetTitle(
					getString(R.string.reporting_purchased_lastMonth) + " (" +
					from.toString() + " " + to.toString() + ")");
			app.getReport(true, from, to, new onDisplayCategoryReportListener());
			break;
		}

	}
	
	protected String getTitle(int reportType) {
		String title = "";
		switch (reportType) {
		case REPORT_ITEM_PURCHASED_TODAY:
			break;
		}
		
		return title;
	}

	protected void doSetupToolbarButtons() {

    	ImageButton settingsButton = new ImageButton(this);
    	settingsButton.setImageResource(R.drawable.settings);
    	settingsButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
    			startActivity(new Intent(ReportActivity.this, MainPreferenceActivity.class));
			}
		});

    	ImageButton homeButton = new ImageButton(this);
    	homeButton.setImageResource(R.drawable.home);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(ReportActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});

    	mToolbar.addLeftAlignedButton(homeButton, false, true);
    	mToolbar.addRightAlignedButton(settingsButton, true, false);
    }
	
}
