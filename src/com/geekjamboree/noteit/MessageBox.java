package com.geekjamboree.noteit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

class MessageBox {
	
	static AlertDialog createMessageBox(Context context, String title, String message) {
			
		AlertDialog dialog = new AlertDialog.Builder(context)
			.setTitle(title)
			.setIcon(R.drawable.indicator_input_error)
			.setMessage(message)
			.create();
		dialog.setButton(
				DialogInterface.BUTTON_POSITIVE, 	
				context.getString(R.string.OK),
				(DialogInterface.OnClickListener) null);
		return dialog;
	}
}
