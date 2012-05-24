package com.geekjamboree.noteit;

import android.app.Activity;
import android.content.res.Resources;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 *  A wrapper over customizing the toolbar in an activity. Note that the order of 
 *  construction is important. A instance should be created before setContentView
 *  is called in the parent activity and after super.OnCreate.
 */
class CustomTitlebarWrapper {

    boolean 			mCustomTitleSupported = false;
	Activity			mParent;
	ViewFlipperHack		mFlipper;
	TextView 			mTitleText;
	int					index = 0;
	boolean				mProgressShowing = false;
	static float 		mScale = 0.0f;
	static int 			mButtonSize = 0;
	static int 			mButtonPadding = 0;
	static int 			mButtonMargin = 0;
	
    static final int BUTTON_DIMENSION 	= 32; //dip
    static final int BUTTON_PADDING 	= 8; //dip
    static final int BUTTON_MARGIN		= 8; //dip

    public CustomTitlebarWrapper(Activity parent) {
    	mParent = parent;
		mCustomTitleSupported = parent.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		mScale = mParent.getResources().getDisplayMetrics().density;
		mButtonSize = (int) (BUTTON_DIMENSION * mScale + 0.5f);
		mButtonPadding = (int) (BUTTON_PADDING * mScale + 0.5f);
		mButtonMargin = (int) (BUTTON_MARGIN * mScale + 0.5f);
	}
    
    public void SetTitle(CharSequence charSequence){
        
    	if (mCustomTitleSupported) {
        	mParent.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        }

    	mFlipper = (ViewFlipperHack) mParent.findViewById(R.id.titlebar_flipper);
    	mTitleText = (TextView) mFlipper.findViewById(R.id.titlebar_title);
        if (mTitleText != null) {
        	mTitleText.setText(charSequence);
         }
    }
    
    public String GetTitle() {
    	return mTitleText.getText().toString();
    }
    
    public void showInderminateProgress(String title) {
    	
    	if (!mProgressShowing && mFlipper != null) {
	    	TextView titleText = (TextView) mParent.findViewById(R.id.titlebar_progress_title);
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

    public void addCenterFillButton(Button button) {
    	
    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
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
    	button.setTextAppearance(mParent, android.R.style.TextAppearance_WindowTitle);
    	button.setTextColor(mParent.getResources().getColor(R.color.noteit_header_textcolor));
    	button.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
    	button.setSingleLine(true);
    	button.setEllipsize(TruncateAt.END);
    	button.setBackgroundResource(R.color.app_button_background);
    	root.addView(button);
    }
        
    public void addLeftAlignedButton(ImageButton button, boolean separatorBefore, boolean separatorAfter) {

    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(mButtonSize, mButtonSize);
    	
//    	if (separatorBefore)
//    		root.addView(getSeparator(Gravity.LEFT | Gravity.CENTER_VERTICAL), index++);
    	
    	lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    	lp.leftMargin = separatorBefore == true ? mButtonMargin : 0;
    	lp.rightMargin = separatorAfter == true ? mButtonMargin : 0;
    	button.setLayoutParams(lp);
    	button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
    	button.setBackgroundResource(R.color.app_button_background);
    	root.addView(button, index++);
    	
//    	if (separatorAfter)
//    		root.addView(getSeparator(Gravity.LEFT | Gravity.CENTER_VERTICAL), index++);
    }
    
    public void addRightAlignedButton(ImageButton button, boolean separatorBefore, boolean separatorAfter) {
    	
    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(mButtonSize, mButtonSize);

//    	if (separatorBefore)
//    		root.addView(getSeparator(Gravity.RIGHT | Gravity.CENTER_VERTICAL));

    	lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
    	lp.leftMargin = separatorBefore == true ? mButtonMargin : 0;
    	lp.rightMargin = separatorAfter == true ? mButtonMargin : 0;
    	button.setLayoutParams(lp);
    	button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
    	button.setBackgroundResource(R.color.app_button_background);
    	root.addView(button);
    	
//    	if (separatorAfter) 
//    		root.addView(getSeparator(Gravity.RIGHT | Gravity.CENTER_VERTICAL));
    }
    
    protected ImageView getSeparator(int gravity) {
    	
    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
		LinearLayout.LayoutParams 	separatorLP = new LinearLayout.LayoutParams(
    			2, LayoutParams.FILL_PARENT);
		
    	ImageView imageSep = new ImageView(root.getContext());
    	separatorLP.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
    	imageSep.setLayoutParams(separatorLP);
		Resources.Theme theme = mParent.getTheme();
		TypedValue 		imageID = new TypedValue();
		if (theme.resolveAttribute(R.attr.NI_VerticalSeparator, imageID, true)) {
	    	imageSep.setImageResource(imageID.resourceId);
		}
//    	imageSep.setPadding(0, 1, 0, 1);
    	return imageSep;
    }
    
}
