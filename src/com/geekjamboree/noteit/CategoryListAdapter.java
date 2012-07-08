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
			NoteItApplication app,
			boolean showDragDropIcon) {
		
		super(context, resource, textViewResourceId, objects, ItemType.BOLD);
		mApp = app;
		mShowDragDropIcon = showDragDropIcon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = super.getView(position, convertView, parent);
		
		if (view != null) {
			
			NoteItApplication 	app = mApp;
			Category 			category = app.getCategory(position);
			TextView 			textView = (TextView) view.findViewById(mTextViewResId);
			
			if (category != null && category.mUserID != app.getUserID()) {
				textView.setCompoundDrawablesWithIntrinsicBounds(
						ThemeUtils.getResourceIdFromAttribute(
								getContext(), 
								R.attr.Category_Shared_Small), 
						0,
						mShowDragDropIcon? ThemeUtils.getResourceIdFromAttribute(
								getContext(),
								R.attr.Hand_Small) : 0, 
						0);
			} else { 
				textView.setCompoundDrawablesWithIntrinsicBounds(
						ThemeUtils.getResourceIdFromAttribute(
								getContext(), 
								R.attr.Category_Small),
						0,
						mShowDragDropIcon ? ThemeUtils.getResourceIdFromAttribute(
								getContext(), 
								R.attr.Hand_Small) : 0, 
						0);
			}
		}
		return view;
	}
}
