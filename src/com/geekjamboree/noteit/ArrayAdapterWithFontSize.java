package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.ItemListActivity.ItemType;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class ArrayAdapterWithFontSize<T> extends ArrayAdapter<T> {

	protected int 		mFontSize 		= 3;
	protected int 		mTextViewResId 	= 0;
	protected Context	mContext;
	protected ItemType	mItemType;
	
	public ArrayAdapterWithFontSize(
			Context context, 
			int resource, 
			int textViewResourceId, 
			ArrayList<T> objects,
			ItemType itemType) {

		super(context, resource, textViewResourceId, objects);
		mTextViewResId = textViewResourceId;
		mContext = context;
		mItemType = itemType;
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
		appearance = NoteItApplication.getPreferredTextAppearance(
				mContext, mFontSize, mItemType);
    	return appearance;
    }
}
