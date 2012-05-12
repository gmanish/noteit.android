package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Message;

import android.content.Context;
import android.view.View;

public class InboxMessages {
	
	NoteItApplication	mApp = null;
	Context				mContext = null;
	View 				mAnchor = null;
	
	public InboxMessages(NoteItApplication app, Context context, View anchor) {
		super();
		mApp = app;
		mContext = context;
		mAnchor = anchor;
	}
	
	public void doDisplayUnreadMessages() {
		
		mApp.fetchInboxMessageHeaders(true, new NoteItApplication.OnFetchMessagesListener() {
			
			public void onPostExecute(
					long retVal, 
					ArrayList<Message> messages,
					String errMessage) {

				if (retVal == 0) {
					doDisplayMessage(messages, 0);
				}
			}
		});
	}
	
	public void doDisplayMessage(final ArrayList<Message> messages, final int index) {
		
		if (index < messages.size()) {
			
			mApp.getMessage(messages.get(index).mMessageId, new NoteItApplication.OnFetchMessagesListener() {
				
				public void onPostExecute(
						long retVal, 
						ArrayList<Message> message,
						String errMessage) {
					
					if (retVal == 0 && message.size() == 1) {
						CustomToast.makeText(mContext, mAnchor, message.get(0).mText, new FloatingPopup.OnDismissListener() {
							
							public void onDismiss() {
								doDisplayMessage(messages, index + 1);
							}
						}).show(true);
					}
				}
			});
		}
	}
}
