package com.geekjamboree.noteit;

import android.content.Context;
import android.view.View;

public class CustomToast {
	
	Context 							mContext;
	View								mAnchor;
	String								mMessage;
	FloatingPopup.OnDismissListener		mDismissListener = null;
	
	private CustomToast(Context context, View anchor, String message, FloatingPopup.OnDismissListener listener) {
		super();
		mContext = context;
		mAnchor = anchor;
		mMessage = message;
		mDismissListener = listener;
	}
	
	public void show(boolean bCenterInAnchor) {
		FloatingPopup.MakePopup(mContext, mAnchor, mMessage, mDismissListener).show(bCenterInAnchor);
	}
	
	public static CustomToast makeText(Context context, View anchor, String message) {
		return new CustomToast(context, anchor, message, null);
	}

	public static CustomToast makeText(Context context, View anchor, String message, FloatingPopup.OnDismissListener listener) {
		return new CustomToast(context, anchor, message, listener);
	}
}
