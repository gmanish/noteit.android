package com.geekjamboree.noteit;

import android.content.Context;
import android.view.View;

public class CustomToast {
	
	Context 	mContext;
	View		mAnchor;
	String		mMessage;
	
	private CustomToast(Context context, View anchor, String message) {
		super();
		mContext = context;
		mAnchor = anchor;
		mMessage = message;
	}
	
	public void show(boolean bCenterInAnchor) {
		FloatingPopup.MakePopup(mContext, mAnchor, mMessage).show(bCenterInAnchor);
	}
	
	public static CustomToast makeText(Context context, View anchor, String message) {
		return new CustomToast(context, anchor, message);
	}
}
