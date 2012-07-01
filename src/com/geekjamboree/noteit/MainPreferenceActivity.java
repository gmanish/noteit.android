package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Currency;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;
import com.geekjamboree.noteit.NoteItApplication.Preference;
import com.geekjamboree.noteit.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class MainPreferenceActivity extends PreferenceActivity {

	public static final String 		kPref_HideDoneItems 		= "Delete_Bought_Items";
	public static final boolean		kPref_HideDoneDefault		= true;
	TitleBar						mToolbar;

	SharedPreferences 									mPrefs;
	SharedPreferences.OnSharedPreferenceChangeListener	mPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences,
				String key) {
			if (key.equals("currency")) {
				NoteItApplication app = (NoteItApplication) getApplication();
				if (app != null) {
					Preference appPrefs = app.getUserPrefs();
					appPrefs.mCurrencyId = Integer.valueOf(sharedPreferences.getString(key, String.valueOf(Currency.kDefaultCurrencyId)));
					app.saveUserPreferences(new OnMethodExecuteListerner() {
						
						public void onPostExecute(long resultCode, String message) {
							if (resultCode != 0)
					    		CustomToast.makeText(
					    				MainPreferenceActivity.this,
					    				findViewById(android.R.id.content),
					    				message).show(true);
						}
					});
				}
			}
		}
	};	

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState){
        
		TitleBar.RequestNoTitle(this);
    	ThemeUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpreference);
        mToolbar = (TitleBar) findViewById(R.id.prefs_title);
        doSetupToolbarButtons();
        
		try {
			addPreferencesFromResource(R.xml.prefs);
			mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			ListPreference measurementUnits = (ListPreference)findPreference("MeasurementUnits");
			if (measurementUnits != null) {
				CharSequence[] measurementsystem = {"US", "Imperial", "Metric/SI"};
				CharSequence[] measurementsystemIDs = {"1", "2", "3" };
				
				measurementUnits.setEntries(measurementsystem);
				measurementUnits.setEntryValues(measurementsystemIDs);
			}
			
			ListPreference itemTextSize = (ListPreference)findPreference("Item_Font_Size");
			if (itemTextSize != null) {
				CharSequence[] fontSizeIDs = {"1", "2", "3"}; // Large, Medium, Small
				itemTextSize.setEntryValues(fontSizeIDs);
				itemTextSize.setDefaultValue(fontSizeIDs[2]); // default small
			}
			
			ListPreference	themePref = (ListPreference) findPreference("app_theme");
			if (themePref != null) {
				CharSequence[] themeIDs = {"1", "2"}; // Note Book, Brushed Metal
				themePref.setEntryValues(themeIDs);
				themePref.setDefaultValue(themeIDs[0]);
			}
			
			NoteItApplication 	app = (NoteItApplication) getApplication();
			ListPreference 		currenciesPref = (ListPreference) findPreference("currency");
			ArrayList<Currency> currencies = app.getCurrencies();
			if (currenciesPref != null &&  currencies != null && currencies.size() > 0){
				CharSequence[] currencyNames = new String[currencies.size()];
				CharSequence[] currencyIds = new String[currencies.size()];
				
				int selIndex = -1;
				Preference prefs = app.getUserPrefs();
				for (int index = 0; index < currencies.size(); index++) {
					currencyNames[index] = currencies.get(index).mCurrencyName + 
							" (" + currencies.get(index).mCurrencySymbol + ")";
					currencyIds[index] = String.valueOf(currencies.get(index).mCurrencyId);
					if (prefs != null && currencyIds[index].equals(String.valueOf(prefs.mCurrencyId)))
						selIndex = index;
				}
				
				currenciesPref.setEntries(currencyNames);
				currenciesPref.setEntryValues(currencyIds);
				if (selIndex >= 0)
					currenciesPref.setValueIndex(selIndex);
			} else {
				throw new NullPointerException("Server Installation Corrupt");
			}
			
			mPrefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
		} finally {
		}
	}

	@Override
	protected void onPause() {
		mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
		super.onPause();
	}

	@Override
	protected void onResume() {
		mPrefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
		super.onResume();
	}
	
    protected void doSetupToolbarButtons() {

    	ImageButton homeButton = mToolbar.addLeftAlignedButton(R.drawable.home, true, true);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(MainPreferenceActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
    	
    	mToolbar.addVerticalSeparator(this, true);
    }
	
}
