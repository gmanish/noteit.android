package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Country;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;
import com.geekjamboree.noteit.NoteItApplication.Preference;
import com.geekjamboree.noteit.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class MainPreferenceActivity extends PreferenceActivity {

	SharedPreferences 									mPrefs;
	SharedPreferences.OnSharedPreferenceChangeListener	mPrefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences,
				String key) {
			if (key.equals("currency")) {
				NoteItApplication app = (NoteItApplication) getApplication();
				if (app != null) {
					Preference appPrefs = app.getUserPrefs();
					appPrefs.mCurrencyCode = sharedPreferences.getString(key, "USD");
					app.saveUserPreferences(new OnMethodExecuteListerner() {
						
						public void onPostExecute(long resultCode, String message) {
							if (resultCode != 0)
								Toast.makeText(MainPreferenceActivity.this, message, Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		}
	};	
	
	public void onCreate(Bundle savedInstanceState){
        
		CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        super.onCreate(savedInstanceState);
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
		
		NoteItApplication 	app = (NoteItApplication) getApplication();
		ListPreference 		currenciesPref = (ListPreference) findPreference("currency");
		ArrayList<Country> 	countries = app.getCountries(); 
		
		if (currenciesPref != null &&  countries != null && countries.size() > 0){
			CharSequence[] currencies = new String[countries.size()];
			CharSequence[] currencyIds = new String[countries.size()];
			
			int selIndex = -1;
			Country defaultCountry = app.getDefaultCountry();
			for (int index = 0; index < countries.size(); index++) {
				currencies[index] = countries.get(index).mCurrencyName + " (" + countries.get(index).mCurrencySymbol + ")";
				currencyIds[index] = countries.get(index).mCurrencyCode;
				if (defaultCountry != null && currencyIds[index].equals(defaultCountry.mCurrencyCode))
					selIndex = index;
			}
			
			currenciesPref.setEntries(currencies);
			currenciesPref.setEntryValues(currencyIds);
			if (selIndex >= 0)
				currenciesPref.setValueIndex(selIndex);
		}
		mPrefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
		toolbar.SetTitle(getResources().getText(R.string.preference_activity_title));
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
}
