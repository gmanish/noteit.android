package com.geekjamboree.noteit;

import com.geekjamboree.noteit.R;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class MainPreferenceActivity extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState){
        
		CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);

		ListPreference currency = (ListPreference)findPreference("currency");
		if (currency != null) {
			CharSequence[] currencies = {"INR", "USD", "GBP", "Euro"};
			CharSequence[] currencyIDs = {"1", "2", "3", "4" };
			
			currency.setEntries(currencies);
			currency.setEntryValues(currencyIDs);
		}
		
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
	
		toolbar.SetTitle(getResources().getText(R.string.preference_activity_title));
	}
}
