package com.geekjamboree.noteit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckableView extends LinearLayout implements Checkable {

	TextView		mTextView;
	boolean			mIsChecked = false;
	int				mId = 0;
	
	public CheckableView(Context context) throws Exception {
        this(context, null);
	}

	public CheckableView(Context context, AttributeSet attrs) throws Exception {
        this(context, attrs, 0);
	}

	public CheckableView(
			Context context, 
			AttributeSet attrs, 
			int defStyle) throws Exception {
		this(context, attrs, defStyle, android.R.layout.simple_list_item_1, android.R.id.text1);
	}

	public CheckableView(
			Context context, 
			AttributeSet attrs, 
			int defStyle,
			int resource, 
			int textViewResourceId) throws Exception {
		
		super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(resource, this, true);
		mTextView = (TextView) findViewById(textViewResourceId);
		if (mTextView == null)
			throw new Exception("The specified TextView resource Id was not found.");
	}

	public boolean isChecked() {
		return mIsChecked;
	}

	public void setChecked(boolean checked) {
		
		mIsChecked = checked;
		if (mTextView != null) {
			mTextView.setCompoundDrawablesWithIntrinsicBounds(
				getLeftDrawable(), 
				0, 
				mIsChecked ? ThemeUtils.getResourceIdFromAttribute(getContext(), R.attr.Check) : 0, 
				0);
		}
	}

	public void toggle() {
		setChecked(!mIsChecked);
	}
	
	public int getLeftDrawable() {
		return 0;
	}
	
	public void setId(int id) {
		mId = id;
	}
	
	public int getId() {
		return mId;
	}
}
