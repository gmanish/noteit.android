package com.geekjamboree.noteit;

import android.app.Service;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

class FloatingPopup extends PopupWindow {
	
	View 				mAnchor = null;
	View 				mContentView = null;
	OnTouchListener		mTouchListener = new OnTouchListener() {
		
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
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
				setAnimationStyle(R.style.Animations_PopDownMenu_Reflect);
				TextView textView = (TextView) mContentView.findViewById(R.id.textInfo);
				if (textView != null) {
					textView.setText(text);
					WindowManager wm = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
					if (wm != null) {
						Paint paint = textView.getPaint();
						int textWidth = (int)(paint.measureText(text) + 0.5);
						int displayWidth = wm.getDefaultDisplay().getWidth(); 
						if (textWidth > (displayWidth - 50)) {
							textWidth = displayWidth - 100;
							textView.setWidth(textWidth);
						}
					}
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
	
	public void show(boolean bCenterOnAnchorView) {
		if (!bCenterOnAnchorView) {
			showAsDropDown(mAnchor);
		} else {
			// center in the parent
			showAtLocation(mAnchor, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
		}
	}
}
