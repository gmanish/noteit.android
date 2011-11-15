package com.geekjamboree.noteit;

import android.app.Activity;
import android.view.Window;
import android.widget.TextView;

/*
 *  A wrapper over customizing the toolbar in an activity. Note that the order of 
 *  contruction is important. A instance should be created before setContentView
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

}
