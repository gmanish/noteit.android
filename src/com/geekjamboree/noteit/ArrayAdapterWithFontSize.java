package com.geekjamboree.noteit;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrayAdapterWithFontSize<T> extends ArrayAdapter<T> {

	protected int mFontSize = 3;
	protected int mTextViewResId = 0;
	
	public ArrayAdapterWithFontSize(
			Context context, 
			int resource, 
			int textViewResourceId, 
			ArrayList<T> objects) {

		super(context, resource, textViewResourceId, objects);
		mTextViewResId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View group = super.getView(position, convertView, parent);
		if (group != null) {
			TextView textView = (TextView) group.findViewById(mTextViewResId);
			if (textView != null) {
				textView.setTextAppearance(getContext(), getPreferredTextAppearance());
			}
		}
		return group;
	}


   protected int getPreferredTextAppearance() {
    	
    	int 				appearance = 0;
		SharedPreferences 	prefs = PreferenceManager.getDefaultSharedPreferences(getContext()); 
        
		mFontSize = Integer.valueOf(prefs.getString("Item_Font_Size", "3"));
    	if (mFontSize == 1)
    		appearance = R.style.ItemList_TextAppearance_PendingItem_Large;
    	else if (mFontSize == 2)
    		appearance = R.style.ItemList_TextAppearance_PendingItem_Medium;
    	else
    		appearance = R.style.ItemList_TextAppearance_PendingItem_Small;
    	
    	return appearance;
    }
}
