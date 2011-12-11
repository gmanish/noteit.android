package com.geekjamboree.noteit;

import android.app.Activity;
import android.text.TextUtils.TruncateAt;
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
public class CustomTitlebarWrapper {

    boolean 	mCustomTitleSupported = false;
	Activity	mParent;
	TextView 	mTitleText;
	
    public CustomTitlebarWrapper(Activity parent) {
    	mParent = parent;
		mCustomTitleSupported = parent.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	}
    
    public void SetTitle(CharSequence charSequence){
        
    	if (mCustomTitleSupported) {
        	mParent.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        }

    	mTitleText = (TextView) mParent.findViewById(R.id.textView_titlebar);
        if (mTitleText != null) {
        	mTitleText.setText(charSequence);
         }
    }

    static final int BUTTON_DIMENSION = 50; //dip
    public void addCenterFillButton(Button button) {
    	
    	final float 				scale = mParent.getResources().getDisplayMetrics().density;
    	final int 					buttonSize = (int) (BUTTON_DIMENSION * scale + 0.5f);
    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, buttonSize);
    	
    	// When we display the center fill button, we don't have the title
    	mTitleText.setVisibility(View.GONE);
    	lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    	lp.weight = 1;
    	button.setLayoutParams(lp);
    	button.setPadding(10, 0, 5, 0);
    	button.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_flyout, 0);
    	button.setTextAppearance(mParent, android.R.style.TextAppearance_WindowTitle);
    	button.setTextColor(mParent.getResources().getColor(R.color.theme_offwhite_header_text));
    	button.setSingleLine(true);
    	button.setEllipsize(TruncateAt.END);
    	button.setBackgroundResource(R.color.app_button_background);
    	root.addView(button);
    }
    
    public void addLeftAlignedButton(ImageButton button, boolean separatorBefore, boolean separatorAfter) {

    	final float 				scale = mParent.getResources().getDisplayMetrics().density;
    	final int 					buttonSize = (int) (BUTTON_DIMENSION * scale + 0.5f);
    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(buttonSize, buttonSize);
    	
    	if (separatorBefore)
    		root.addView(getSeparator(Gravity.LEFT | Gravity.CENTER_VERTICAL), -1);
    	
    	lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    	button.setLayoutParams(lp);
    	button.setPadding(0, 2, 0, 2);
    	button.setBackgroundResource(R.color.app_button_background);
    	root.addView(button);
    	
    	if (separatorAfter)
    		root.addView(getSeparator(Gravity.LEFT | Gravity.CENTER_VERTICAL));
    }
    
    public void addRightAlignedButton(ImageButton button, boolean separatorBefore, boolean separatorAfter) {
    	
    	final float 				scale = mParent.getResources().getDisplayMetrics().density;
    	final int 					buttonSize = (int) (BUTTON_DIMENSION * scale + 0.5f);
    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(buttonSize, buttonSize);

    	if (separatorBefore)
    		root.addView(getSeparator(Gravity.RIGHT | Gravity.CENTER_VERTICAL));

    	lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
    	button.setLayoutParams(lp);
    	button.setPadding(0, 2, 0, 2);
    	button.setBackgroundResource(R.color.app_button_background);
    	root.addView(button);
    	
    	if (separatorAfter) 
    		root.addView(getSeparator(Gravity.RIGHT | Gravity.CENTER_VERTICAL));
    }
    
    protected ImageView getSeparator(int gravity) {
    	
    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
		LinearLayout.LayoutParams 	separatorLP = new LinearLayout.LayoutParams(
    			2, LayoutParams.FILL_PARENT);
		
    	ImageView imageSep = new ImageView(root.getContext());
    	separatorLP.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
    	imageSep.setLayoutParams(separatorLP);
    	imageSep.setImageResource(R.drawable.vertical_separator);
    	imageSep.setPadding(0, 1, 0, 1);
    	
    	return imageSep;
    }
    
}
