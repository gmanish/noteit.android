package com.geekjamboree.noteit;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geekjamboree.noteit.ItemListActivity.ItemType;
import com.geekjamboree.noteit.NoteItApplication.Category;

class CategoryListAdapter extends ArrayAdapterWithFontSize<Category> {

	NoteItApplication	mApp;
	boolean				mShowDragDropIcon = false;
	
	public CategoryListAdapter(
			Context context, 
			int resource,
			int textViewResourceId, 
			ArrayList<Category> objects,
			NoteItApplication app) {
		
		super(context, resource, textViewResourceId, objects, ItemType.BOLD);
		mApp = app;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = super.getView(position, convertView, parent);
		
		if (view != null) {
			
			NoteItApplication 	app = mApp;
			Category 			category = app.getCategory(position);
			TextView 			textView = (TextView) view.findViewById(mTextViewResId);
			
			if (textView != null && category != null) {
				
				textView.setCompoundDrawablesWithIntrinsicBounds(
						getCategoryDrawableId(getContext(), app, category), 
						0,
						ThemeUtils.getResourceIdFromAttribute(getContext(), R.attr.Hand_Small), 
						0);
			}
		}
		return view;
	}
	
	public static int getCategoryDrawableId(Context context, NoteItApplication app, Category category) {
		
		if (app != null && category != null) {
			if (category.mUserID != app.getUserID()) {
				return ThemeUtils.getResourceIdFromAttribute(
							context, 
							R.attr.Category_Shared_Small);
			} else { 
				return ThemeUtils.getResourceIdFromAttribute(
							context, 
							R.attr.Category_Small);
			}
		} else
			return 0;
	}
}
