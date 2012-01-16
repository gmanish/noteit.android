package com.geekjamboree.noteit;

import android.app.Service;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

class FloatingPopup extends PopupWindow {
	
	View 				mAnchor = null;
	View 				mContentView = null;
	OnDismissListener 	mDismissListener = new OnDismissListener() {
		
		public void onDismiss() {
			Log.i("FloatingPopup.onDismiss", "onDismiss Called");
		}
	};
	
	OnTouchListener		mTouchListener = new OnTouchListener() {
		
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				Log.i("FloatingPopup.onTouch", "onTouch Called");
				dismiss();
				return true;
			}
			return false;
		}
	};
	
	private FloatingPopup(Context context, View anchor, String text) {
		super(context);
		mAnchor = anchor;
		LayoutInflater 	li = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
		if (li != null) {
			mContentView = li.inflate(R.layout.tipsdialog, null);
			if (mContentView != null) {
				setContentView(mContentView);
				setWidth(LayoutParams.WRAP_CONTENT);
				setHeight(LayoutParams.WRAP_CONTENT);
				setTouchable(true);
				setFocusable(true);
				setOutsideTouchable(true);
				setTouchInterceptor(mTouchListener);
				setOnDismissListener(mDismissListener);
				setAnimationStyle(R.style.Animations_PopDownMenu_Reflect);
				TextView textView = (TextView) mContentView.findViewById(R.id.textInfo);
				if (textView != null) {
					textView.setText(text);
				}
				ImageButton close = (ImageButton) mContentView.findViewById(R.id.btnClose);
				if (close != null) {
					close.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							dismiss();
						}
					});
				}
			}
		}
	}
	
	public static FloatingPopup MakePopup(Context context, View anchor, String text) {
		return new FloatingPopup(context, anchor, text);
	}
	
	public void show() {
		showAsDropDown(mAnchor);
	}
}
