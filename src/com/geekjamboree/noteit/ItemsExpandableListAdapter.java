package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.Item;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ItemsExpandableListAdapter extends BaseExpandableListAdapter {

	private ArrayList<Category>				mCategories;
	private ArrayList<ArrayList<Item>>		mItems;
	private Context							mContext = null;
	
	ItemsExpandableListAdapter(Context context, 
			ArrayList<Category> categories){
		mCategories = categories;
		mContext = context;
	}
	
	public void AddCategory(Category category){
		if (!mCategories.contains(category)) {
			assert(category.mName != "");
			mCategories.add(category);
		}
	}
	
	public void AddItem(Item item){
		// This category should have been added before AddItem is called	
		assert(mCategories.contains(item.mCategoryID));
		
		int index = mCategories.indexOf(item.mCategoryID);
		if (mItems.size() < index + 1) {
		    mItems.add(new ArrayList<NoteItApplication.Item>());
		}
		
		mItems.get(index).add(item);
	}
	
	public Object getChild(int groupPosition, int childPosition) {
		return mItems.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		return mItems.get(groupPosition).size();
	}

	public Object getGroup(int groupPosition) {
		return mCategories.get(groupPosition);
	}

	public int getGroupCount() {
		return mCategories.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public boolean hasStableIds() {
		return true;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

    public TextView getGenericView() {
        // Layout parameters for the ExpandableListView
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 	// Width 
                64); 									// Height

        TextView textView = new TextView(mContext);
        textView.setLayoutParams(lp);
        // Center the text vertically
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        // Set the text starting position
        textView.setPadding(36, 0, 0, 0);
        return textView;
    }

    public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
        TextView textView = getGenericView();
        textView.setText(getChild(groupPosition, childPosition).toString());
        return textView;
	}

    public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
        TextView textView = getGenericView();
        textView.setText(getGroup(groupPosition).toString());
        return textView;
	}
}
