package com.geekjamboree.noteit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.TypedValue;

public class ThemeUtils {

	private static int sTheme = -1; // Not initialized

	public final static int THEME_DEFAULT		= 1;
	public final static int THEME_NOTEBOOK 		= 1;
	public final static int THEME_BRUSHED_METAL = 2;

	public static void initializeThemes() {
		sTheme = -1; // Force read from preferences
	}
	
	public static void changeToTheme(Activity activity, int theme) {
		sTheme = theme;
		activity.finish();
		activity.startActivity(new Intent(activity, activity.getClass()));
	}

	public static void onActivityCreateSetTheme(Activity activity) {
		
		if (sTheme > 0) {
			activity.setTheme(sTheme);
		} else {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
			if (prefs != null) {
				int themePref = Integer.valueOf(prefs.getString("app_theme", String.valueOf(THEME_DEFAULT)));
				switch (themePref)
				{
				default:
				case THEME_NOTEBOOK:
					activity.setTheme(sTheme = R.style.NI_AppTheme_Trademark);
					break;
				case THEME_BRUSHED_METAL:
					activity.setTheme(sTheme = R.style.NI_AppTheme_Dark);
					break;
				}
				activity.getApplication().setTheme(sTheme);
			}
		}
		
	}
	
	public static int getResourceIdFromAttribute(Context context, int attribId) {
		
		return getResourceIdFromAttribute(context, attribId, false);
	}
	
	public static int getResourceIdFromAttribute(Context context, int attribId, boolean resolveRefs) {
		
		Resources.Theme theme = context.getTheme();
		TypedValue 		resID = new TypedValue();
		theme.resolveAttribute(attribId, resID, resolveRefs);
		return resID.data;
	}
	
	public static int getPlatformVersion() {
	    try {
	        java.lang.reflect.Field verField = Class.forName("android.os.Build$VERSION").getField("SDK_INT");
	        int ver = verField.getInt(verField);
	        return ver;
	    } catch (Exception e) {
	        try {
	            java.lang.reflect.Field verField = Class.forName("android.os.Build$VERSION").getField("SDK");
	            String verString = (String) verField.get(verField);
	            return Integer.parseInt(verString);
	        } catch(Exception err) {
	            return -1;
	        }
	    }
	}
}
