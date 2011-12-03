package com.geekjamboree.noteit;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;


public class ExpandableListViewIndicatorOnRight extends ExpandableListView {

	protected int groupIndicatorWidth = 0;
	
	public ExpandableListViewIndicatorOnRight(Context context) {
		super(context);
		groupIndicatorWidth = getResources().getDrawable(R.drawable.up).getMinimumWidth(); 
	}

	public ExpandableListViewIndicatorOnRight(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		groupIndicatorWidth = getResources().getDrawable(R.drawable.up).getMinimumWidth(); 
	}

	public ExpandableListViewIndicatorOnRight(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		groupIndicatorWidth = getResources().getDrawable(R.drawable.up).getMinimumWidth(); 
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
        if (oldw != w) {
			setIndicatorBounds(w - groupIndicatorWidth, w);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
}
