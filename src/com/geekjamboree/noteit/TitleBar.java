package com.geekjamboree.noteit;

import com.geekjamboree.noteit.R;

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
import android.widget.ImageView;
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
	static int				mButtonSeparatorWidth = 0;
	static int				mButtonSeparatorHeight = 0;
	
    static final int BUTTON_DIMENSION 		= 32; //dip
    static final int BUTTON_PADDING 		= 8; //dip
    static final int BUTTON_MARGIN			= 0; //dip, spacing between adjacent buttons
    static final int BUTTON_SEPARATOR_WIDTH	= 2; //dip
    static final int BUTTON_SEPARATOR_HEIGHT= 36; //dip

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
		
		mScale 					= getResources().getDisplayMetrics().density;
		mButtonSize 			= (int) (BUTTON_DIMENSION * mScale + 0.5f);
		mButtonPadding 			= (int) (BUTTON_PADDING * mScale + 0.5f);
		mButtonMargin 			= (int) (BUTTON_MARGIN * mScale + 0.5f);
		mButtonSeparatorWidth	= (int) (BUTTON_SEPARATOR_WIDTH * mScale + 0.5f);
		mButtonSeparatorHeight	= (int) (BUTTON_SEPARATOR_HEIGHT * mScale + 0.5f);
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
    			LinearLayout.LayoutParams.MATCH_PARENT, 
    			getButtonDim()); // Fill Parent because this button needs to expand to fill available horizontal space 
    	
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
        
    public ImageButton addLeftAlignedButton(int id) {

    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(getButtonDim(), getButtonDim());
    	
    	lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    	ImageButton button = new ImageButton(getContext(), null, R.attr.NI_ButtonStyle);
    	button.setLayoutParams(lp);
    	button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
    	button.setImageResource(id);
    	mTitleRoot.addView(button, mIndex++);
    	return button;
    }
    
    public ImageButton addRightAlignedButton(int id) {
    	
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(getButtonDim(), getButtonDim());

    	lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
    	ImageButton button = new ImageButton(getContext(), null, R.attr.NI_ButtonStyle);
    	button.setLayoutParams(lp);
    	button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
    	button.setImageResource(id);
    	mTitleRoot.addView(button);
    	return button;
    }
    
    public void addVerticalSeparator(Context context, boolean leftAlligned) {
    	
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(mButtonSeparatorWidth, mButtonSeparatorHeight);

    	lp.gravity = (leftAlligned ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
    	ImageView sep = new ImageView(getContext());
    	sep.setLayoutParams(lp);
    	sep.setBackgroundResource(ThemeUtils.getResourceIdFromAttribute(context, R.attr.NI_VerticalSeparator));
    	if (leftAlligned)
    		mTitleRoot.addView(sep, mIndex++);
    	else 
    		mTitleRoot.addView(sep);
    }
    
    public static void RequestNoTitle(Activity parent) {
    	parent.requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    
    protected int getButtonDim() {
    	return mButtonSize + 2 * mButtonPadding;
    }
}
