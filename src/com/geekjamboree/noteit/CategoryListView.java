package com.geekjamboree.noteit;

import android.content.Context;
import android.util.AttributeSet;

class CategoryListView extends DragDropListView {
	
	int 	mDragDropIndicatorWidth = 0;
	
	public CategoryListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mDragDropIndicatorWidth = getResources().getDrawable(R.drawable.hand).getMinimumWidth(); 
	}

	public CategoryListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDragDropIndicatorWidth = getResources().getDrawable(R.drawable.hand).getMinimumWidth(); 
	}

	public CategoryListView(Context context) {
		super(context);
		mDragDropIndicatorWidth = getResources().getDrawable(R.drawable.hand).getMinimumWidth(); 
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
		int paddingRight = (int)(getResources().getDisplayMetrics().density * 10 + 0.5); // dip to px
        if (oldw != w) {
        	setDragDropIndicatorBounds(
        		w - mDragDropIndicatorWidth - paddingRight, 
        		w - paddingRight);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
}

