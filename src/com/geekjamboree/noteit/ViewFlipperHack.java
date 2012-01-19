/*
 * 	This is a HACK for the ViewFlipper problem
 * 	See http://stackoverflow.com/questions/8923935/view-flipper-throws-exception-in-viewflipper-ondetachedfromwindow
 */
package com.geekjamboree.noteit;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class ViewFlipperHack extends ViewFlipper {

	public ViewFlipperHack(Context context) {
		super(context);
	}

	public ViewFlipperHack(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override 
	protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        }
        catch (IllegalArgumentException e) {
            stopFlipping();
        }
    }
}
