package com.geekjamboree.noteit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.geekjamboree.noteit.NoteItApplication.CategoryReportItem;
import com.geekjamboree.noteit.NoteItApplication.ItemReportItem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
	public static final int REPORT_CATEGORY_PURCHASED_THISMONTH		= 5;
	public static final int REPORT_CATEGORY_PURCHASED_LASTMONTH		= 6;
	public static final int REPORT_PURCHASED_ALL					= 7;
	
	// We don't want to support monthly items report 
	// as this will potentially return a lot for items
	public static final int REPORT_ITEM_PURCHASED_TODAY		= 8;
	public static final int REPORT_ITEM_PURCHASED_YESTERDAY	= 9;
	public static final int REPORT_ITEM_PURCHASED_THISWEEK	= 10;
	public static final int REPORT_ITEM_PURCHASED_LASTWEEK	= 11;
	
	public static final int REPORT_ITEM_PENDING_ALL			= 12;
	
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
	String					mCurrentReportText = "";
	String 					mCurrentReportHtml = "";
	boolean					mAttachHTML = true;
	
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

	@Override
	protected void onResume() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mAttachHTML = prefs.getBoolean("Attach_html_reports", true);
		super.onResume();
	}

	class onDisplayItemReportListener implements NoteItApplication.OnGetItemReportListener {
		public void onPostExecute(long resultCode, ArrayList<ItemReportItem> items, String message) {
			
			try {
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
					mCurrentReportText = getString(R.string.reporting_emailIntro);
					mCurrentReportText += "\n" + mToolbar.GetTitle().toString() + ":\n\n";
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
								percentage = numberFormat.format(0f);
							String row = String.format(
									categoryItemFormat.toString(), 
									item.mItemName, 
									price,
									percentage);
							mCurrentReportText += item.mItemName.trim() + ", \t\t" + price + ",  \t\t" + percentage + "\n";
							text += row;
						}
					}
					String categoryFooter = getString(R.string.reporting_category_footer);
					String strTotal = String.format(currencyFormat, numberFormat.format(total));
					text += String.format(categoryFooter, strTotal);
					mCurrentReportText += "\n\n\n" + getString(R.string.itemlist_emailsig);
					mCurrentReportHtml = text;
					mTextView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
				} else 
					Toast.makeText(ReportActivity.this, message, Toast.LENGTH_LONG).show();		
			} finally {
				mToolbar.hideIndeterminateProgress();
			}
		}
	}
	
	class onDisplayCategoryReportListener implements NoteItApplication.OnGetCategoryReportListener {
		
		public void onPostExecute(long resultCode,
				ArrayList<CategoryReportItem> items, String message) {
			
			try {
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
					mCurrentReportText = getString(R.string.reporting_emailIntro);
					mCurrentReportText += "\n" + mToolbar.GetTitle().toString() + ":\n\n";
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
								percentage = numberFormat.format(0f);
							String row = String.format(
									categoryItemFormat.toString(), 
									item.mCategoryName, 
									price,
									percentage);
							mCurrentReportText += item.mCategoryName.trim() + ",  \t\t" + price + ",  \t\t" + percentage + "\n";
							text += row;
						}
					}
					String categoryFooter = getString(R.string.reporting_category_footer);
					String strTotal = String.format(currencyFormat, numberFormat.format(total));
					mCurrentReportText += getString(R.string.itemlist_emailsig);
					text += "\n\n\n" + String.format(categoryFooter, strTotal);
					mCurrentReportHtml = text;
					mTextView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
				} else 
					Toast.makeText(ReportActivity.this, message, Toast.LENGTH_LONG).show();
			} finally {
				mToolbar.hideIndeterminateProgress();
			}
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
		case REPORT_ITEM_PENDING_ALL:
			Calendar pendingTillToday = Calendar.getInstance();
			to = new Date(pendingTillToday.getTimeInMillis());
			mToolbar.SetTitle(
					getString(R.string.reporting_pending_all_title) + " (" +
					to.toString() + ")");
			app.getReport(false, null, to, new onDisplayItemReportListener());
			break;
		default:
			return;
		}
		mToolbar.showInderminateProgress(getString(R.string.progress_message));
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
    	
    	ImageButton shareButton = new ImageButton(this);
    	shareButton.setImageResource(R.drawable.email);
    	shareButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doEmail();
			}
		});
    	
    	mToolbar.addLeftAlignedButton(homeButton, false, true);
    	mToolbar.addRightAlignedButton(shareButton, true, false);
    	mToolbar.addRightAlignedButton(settingsButton, true, false);
    }

    protected void doEmail() {
    	final Intent emailIntent = new Intent(Intent.ACTION_SEND);
//    	emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	emailIntent.setType("text/plain");
    	emailIntent.putExtra(Intent.EXTRA_SUBJECT, mToolbar.GetTitle());
    	emailIntent.putExtra(Intent.EXTRA_TEXT, mCurrentReportText);
    	Uri attachment = null;
    	if (mAttachHTML && (attachment = writeToExternalStoragePublic()) != null) {
    		emailIntent.putExtra(Intent.EXTRA_STREAM, attachment);
     	}
    	startActivity(Intent.createChooser(emailIntent, getString(R.string.reporting_sharemessage)));
    }

    public Uri writeToExternalStoragePublic() {
    	final String 		filename 		= mToolbar.GetTitle() + ".html"; 
        final String 		packageName 	= this.getPackageName();
        final String 		folderpath 		= Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + packageName + "/files/";
        File 				folder			= new File(folderpath);
        File 			 	file			= null;
        FileOutputStream 	fOut 			= null;
        
        try {
            try {
            	if (folder != null) {
	                boolean exists = folder.exists();
	                if (!exists) 
	                	folder.mkdirs();	                
	                file = new File(folder.toString(), filename);
	                if (file != null) {
	                	fOut = new FileOutputStream(file, false);
		                if (fOut != null) {
			                fOut.write(mCurrentReportHtml.getBytes());
		                }
	                }
            	}
            } catch (IOException e) {
	    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return Uri.fromFile(file);
        } finally {
        	if (fOut != null) {
	            try {
					fOut.flush();
		            fOut.close();
				} catch (IOException e) {
		    		Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				}
        	}
        }
    }
}	
