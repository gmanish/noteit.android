package com.geekjamboree.noteit;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TitleBar extends LinearLayout {

	ViewFlipperHack			mFlipper;
	LinearLayout			mTitleRoot;
	TextView 				mTitleText;
	boolean					mProgressShowing = false;
	int						mIndex = 0;

	static float 			mScale = 0.0f;
	static int 				mButtonSize = 0;
	static int 				mButtonPadding = 0;
	static int 				mButtonMargin = 0;
	
    static final int BUTTON_DIMENSION 	= 32; //dip
    static final int BUTTON_PADDING 	= 8; //dip
    static final int BUTTON_MARGIN		= 8; //dip

    public TitleBar(Context context) {
		super(context);
		doInit();
	}
    
    public TitleBar(Context context, AttributeSet attr) {
    	super(context, attr);
    	doInit();
    }

	protected void doInit() {
		
		LayoutInflater.from(getContext()).inflate(
				R.layout.titlebar, 
				this, 
				true);
		
		mFlipper			= (ViewFlipperHack) findViewById(R.id.titlebar_flipper);
    	mTitleText 			= (TextView) findViewById(R.id.titlebar_title);
    	mTitleRoot			= (LinearLayout) findViewById(R.id.titlebar_root);
		
    	if (mFlipper == null || mTitleText == null || mTitleRoot == null)
			Log.e("TitleBar", "Could not inflate titlebar layout.");
		
		mScale 				= getResources().getDisplayMetrics().density;
		mButtonSize 		= (int) (BUTTON_DIMENSION * mScale + 0.5f);
		mButtonPadding 		= (int) (BUTTON_PADDING * mScale + 0.5f);
		mButtonMargin 		= (int) (BUTTON_MARGIN * mScale + 0.5f);
	}

    public void SetTitle(CharSequence charSequence){
        
        if (mTitleText != null) {
        	mTitleText.setText(charSequence);
         }
    }
    
    public String GetTitle() {
    	return mTitleText.getText().toString();
    }
    
    public void showInderminateProgress(String title) {
    	
    	if (!mProgressShowing && mFlipper != null) {
	    	TextView titleText = (TextView) findViewById(R.id.titlebar_progress_title);
	    	if (titleText != null) {
	    		titleText.setText(title);
	    	}
	    	
    		mFlipper.showNext();
	    	mProgressShowing = true;
    	}
    }
    
    public void hideIndeterminateProgress() {
    	
    	if (mFlipper != null && mProgressShowing) {
    		mFlipper.showPrevious();
    		mProgressShowing = false;
    	}
    }
    
    public void addCenterFillButton(
    		Button button) {
    	
    	LinearLayout 				root = (LinearLayout) findViewById(R.id.titlebar_root);
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(
    			LinearLayout.LayoutParams.FILL_PARENT, 
    			mButtonSize); // Fill Parent because this button needs to expand to fill available horizontal space 
    	
    	// When we display the center fill button, we don't have the title
    	mTitleText.setVisibility(View.GONE);
    	lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    	lp.weight = 1;
    	button.setLayoutParams(lp);
    	button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
    	button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_flyout, 0);
    	button.setTextAppearance(mTitleRoot.getContext(), android.R.style.TextAppearance_WindowTitle);
    	button.setTextColor(getResources().getColor(R.color.noteit_header_textcolor));
    	button.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
    	button.setSingleLine(true);
    	button.setEllipsize(TruncateAt.END);
    	button.setBackgroundResource(R.color.app_button_background);
    	root.addView(button);
    }
        
    public ImageButton addLeftAlignedButton(
    		int id, 
    		boolean separatorBefore, 
    		boolean separatorAfter) {

    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(mButtonSize, mButtonSize);
    	
    	lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    	lp.leftMargin = separatorBefore == true ? mButtonMargin : 0;
    	lp.rightMargin = separatorAfter == true ? mButtonMargin : 0;
    	ImageButton button = new ImageButton(getContext());
    	button.setLayoutParams(lp);
    	button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
    	button.setBackgroundResource(R.color.app_button_background);
    	button.setImageResource(id);
    	mTitleRoot.addView(button, mIndex++);
    	return button;
    }
    
    public ImageButton addRightAlignedButton(
    		int id, 
    		boolean separatorBefore, 
    		boolean separatorAfter) {
    	
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(mButtonSize, mButtonSize);

    	lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
    	lp.leftMargin = separatorBefore == true ? mButtonMargin : 0;
    	lp.rightMargin = separatorAfter == true ? mButtonMargin : 0;
    	ImageButton button = new ImageButton(getContext());
    	button.setLayoutParams(lp);
    	button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
    	button.setBackgroundResource(R.color.app_button_background);
    	button.setImageResource(id);
    	mTitleRoot.addView(button);
    	return button;
    }
    
    public static void RequestNoTitle(Activity parent) {
    	parent.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
}
