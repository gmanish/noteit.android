package com.geekjamboree.noteit;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class NIPreferenceCategory extends PreferenceCategory {

	public NIPreferenceCategory(Context context) {
		super(context);
	}

	public NIPreferenceCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NIPreferenceCategory(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
	    
		// And it's just a TextView!
		View view = super.onCreateView(parent);
		if (view instanceof TextView) {
		    TextView categoryTitle =  (TextView)view;
		    if (categoryTitle != null) {
			    categoryTitle.setBackgroundColor(
			    		getContext().getResources().getColor(
			    				ThemeUtils.getResourceIdFromAttribute(
			    						getContext(), 
			    						R.attr.NI_TitleBackgroundColor)));
			    categoryTitle.setTextAppearance(getContext(), android.R.style.TextAppearance_WindowTitle);
		    }
		}
	    return view;
	}
}
