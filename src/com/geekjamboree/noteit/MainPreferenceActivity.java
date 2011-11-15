package com.geekjamboree.noteit;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainPreferenceActivity extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
	}
}
