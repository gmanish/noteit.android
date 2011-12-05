package com.geekjamboree.noteit;

import android.app.Activity;
import android.view.Gravity;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
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
	
    public CustomTitlebarWrapper(Activity parent) {
    	mParent = parent;
		mCustomTitleSupported = parent.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	}
    
    public void SetTitle(CharSequence charSequence){
        if (mCustomTitleSupported) {
        	mParent.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        }

        final TextView myTitleText = (TextView)mParent.findViewById(R.id.textView_titlebar);
        if ( myTitleText != null ) {
            myTitleText.setText(charSequence);
         }
    }

    public void addRightAlignedButton(ImageButton button, boolean separatorBefore, boolean separatorAfter) {
    	
    	LinearLayout 				root = (LinearLayout) mParent.findViewById(R.id.titlebar_root);
    	LinearLayout.LayoutParams 	lp = new LinearLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    	if (separatorBefore) {
        	
    		LinearLayout.LayoutParams leftSepLP = new LinearLayout.LayoutParams(
        			2, LayoutParams.FILL_PARENT);
        	ImageView imageSep = new ImageView(root.getContext());
        	lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        	imageSep.setLayoutParams(leftSepLP);
        	imageSep.setImageResource(R.drawable.vertical_separator);
        	imageSep.setPadding(0, 2, 0, 2);
    		root.addView(imageSep);
    	}

    	lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
    	button.setLayoutParams(lp);
    	button.setPadding(0, 2, 0, 2);
    	button.setBackgroundResource(R.color.app_button_background);
    	root.addView(button);
    	
    	if (separatorAfter) {
        	
    		LinearLayout.LayoutParams rightSepLP = new LinearLayout.LayoutParams(
        			2, LayoutParams.FILL_PARENT);
        	ImageView imageSep = new ImageView(root.getContext());
        	lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        	imageSep.setLayoutParams(rightSepLP);
        	imageSep.setImageResource(R.drawable.vertical_separator);
        	imageSep.setPadding(0, 2, 0, 2);
    		root.addView(imageSep);
    	}
    }
    
}
