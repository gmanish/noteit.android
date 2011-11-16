package com.geekjamboree.noteit;

import com.geekjamboree.noteit.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MainPreferenceActivity extends PreferenceActivity {

	public void onCreate(Bundle savedInstanceState){
        CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
        toolbar.SetTitle(getResources().getText(R.string.preference_activity_title));
	}
}
