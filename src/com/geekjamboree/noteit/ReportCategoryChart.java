package com.geekjamboree.noteit;

import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import com.geekjamboree.noteit.NoteItApplication.CategoryReportItem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ReportCategoryChart extends Activity {

	public static final String 	REPORT_TYPE = "REPORT_TYPE";

	public static final int REPORT_CATEGORY_PURCHASED_THISMONTH		= 5;
	public static final int REPORT_CATEGORY_PURCHASED_LASTMONTH		= 6;

	private static int[] 	COLORS = new int[] {Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.CYAN};
	private CategorySeries 	mSeries = new CategorySeries("");
	private DefaultRenderer mRenderer = new DefaultRenderer();
	private GraphicalView 	mChartView;
	TitleBar				mToolbar;
	View					mRoot;
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
		outState.putSerializable("current_series", mSeries);
		outState.putSerializable("current_renderer", mRenderer);
		outState.putString("chart_title", mToolbar.GetTitle());
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    
		super.onCreate(savedInstanceState);
    	TitleBar.RequestNoTitle(this);
	    setContentView(R.layout.report_categorychart);
        mToolbar = (TitleBar) findViewById(R.id.reportcategory_title);
	    mRoot = findViewById(R.id.report_category_chart_root);
	    
	    mRenderer.setApplyBackgroundColor(true);
	    mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
	    mRenderer.setChartTitleTextSize(20);
	    mRenderer.setLabelsTextSize(12);
	    mRenderer.setLegendTextSize(12);
	    mRenderer.setMargins(new int[] { 15, 15, 15, 30 });
	    mRenderer.setStartAngle(90);
	    
	    if (savedInstanceState == null) {
			Intent intent = getIntent();
			if (intent != null) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					int reportId = bundle.getInt(REPORT_TYPE);
					doReport(reportId);
				}
			}
	    } else {
		    mSeries = (CategorySeries) savedInstanceState.getSerializable("current_series");
		    mRenderer = (DefaultRenderer) savedInstanceState.getSerializable("current_renderer");
		    mToolbar.SetTitle(savedInstanceState.getString("chart_title"));
	    }
		if (mChartView != null)
			mChartView.repaint();
		doSetupToolbarButtons();
	}

	
	@Override
	protected void onResume() {
	    
		super.onResume();
	    if (mChartView == null) {
	    	LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
			if (layout != null) {
				mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
			    mRenderer.setClickEnabled(false);
			    layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
			    mChartView.repaint();
			}
	    } else {
	      mChartView.repaint();
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
					
					NumberFormat 		numberFormat = NumberFormat.getInstance();
					
					numberFormat.setMinimumFractionDigits(2);
					numberFormat.setMaximumFractionDigits(2);
					for (int index = 0; index < items.size(); index++) {
						CategoryReportItem item = items.get(index);
						if (item != null) {
							double percentage = 0f;
							if (total > 0)
								percentage = item.mPrice / total * 100;
							else
								percentage = 0f;

							mSeries.add(item.mCategoryName, percentage);
					        SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
					        renderer.setColor(COLORS[(mSeries.getItemCount() - 1) % COLORS.length]);
					        mRenderer.addSeriesRenderer(renderer);
						}
					}
					
					if (mChartView != null)
						mChartView.repaint();
				} else 
					CustomToast.makeText(ReportCategoryChart.this, mRoot, message).show(true);
			} finally {
			}
		}
	}
	
	protected void doReport(int reportId) {
		Date from = null;
		Date to = null;
		NoteItApplication app = (NoteItApplication) getApplication();

		switch (reportId) {
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
	
	protected void doSetupToolbarButtons() {

    	ImageButton homeButton = mToolbar.addLeftAlignedButton(R.drawable.home, true, true);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(ReportCategoryChart.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
    	
    	ImageButton settingsButton = mToolbar.addRightAlignedButton(R.drawable.settings, true, true);
    	settingsButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
    			startActivity(new Intent(ReportCategoryChart.this, MainPreferenceActivity.class));
			}
		});
    }
}

