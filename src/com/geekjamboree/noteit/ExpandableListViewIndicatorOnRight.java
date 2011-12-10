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
		
		int paddingRight = (int)(getResources().getDisplayMetrics().density * 5 + 0.5); // dip to px
        if (oldw != w) {
			setIndicatorBounds(w - (groupIndicatorWidth + paddingRight), w);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
}
