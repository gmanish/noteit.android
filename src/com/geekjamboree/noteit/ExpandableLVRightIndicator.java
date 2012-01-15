package com.geekjamboree.noteit;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;


class ExpandableLVRightIndicator extends ExpandableListView {

	protected int groupIndicatorWidth = 0;
	protected int mRightMargin;
	
	public ExpandableLVRightIndicator(Context context) {
		super(context);
		groupIndicatorWidth = getResources().getDrawable(R.drawable.up).getMinimumWidth(); 
	}

	public ExpandableLVRightIndicator(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		groupIndicatorWidth = getResources().getDrawable(R.drawable.up).getMinimumWidth(); 
	}

	public ExpandableLVRightIndicator(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		groupIndicatorWidth = getResources().getDrawable(R.drawable.up).getMinimumWidth(); 
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		int paddingRight = (int)(getResources().getDisplayMetrics().density * 5 + 0.5); // dip to px
        if (oldw != w) {
        	mRightMargin = (groupIndicatorWidth + paddingRight);
			setIndicatorBounds(w - mRightMargin, w);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	/*
	 * Indicates the rightMargin any view in the List should have in order to not overlap with the groupIndicator
	 */
	public int getRightMargin() {
		return mRightMargin;
	}
	
	public int getGroupCount() {
		int groupCount = 0;
		for (int index = getFirstVisiblePosition(); index <= getLastVisiblePosition(); index++) {
			long packedPosition = super.getExpandableListPosition(index);
			if (ExpandableListView.getPackedPositionType(packedPosition) == PACKED_POSITION_TYPE_GROUP)
				groupCount++;
		}
		return groupCount;
	}
}
