package com.geekjamboree.noteit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;

class DragDropListView extends ListView {

	protected boolean			mIsDown = false;
	protected boolean 			mIsDragging = false;
	protected int				mStartPosition = INVALID_POSITION;
	protected int 				mEndPosition = INVALID_POSITION;
	protected ImageView			mDragView = null;
//	protected int 				mXDragOffset = 0;
	protected int				mYDragOffset = 0;
	protected DragDropListener	mDragDropListener = null;
	protected int				mDragDropIndicatorLeft = 0;
	protected int				mDragDropIndicatorRight = 0;
	
	
	public DragDropListView(
			Context context) {
		super(context);
	}

	public DragDropListView(
			Context context, 
			AttributeSet attrs) {
		super(context, attrs);
	}

	public DragDropListView(
			Context context, 
			AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setDragDropListener(DragDropListener inListener) {
		mDragDropListener = inListener;
	}
	
	public void setDragDropIndicatorBounds(int left, int right) {
		mDragDropIndicatorLeft = left;
		mDragDropIndicatorRight = right;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		if (ev.getAction() == MotionEvent.ACTION_DOWN && x >= mDragDropIndicatorLeft && x <=	 mDragDropIndicatorRight) {
			
			Log.i("CategoryListView.onTouchEvent"	, "X: " + x + " Y: " + y);
			mIsDown = true;
			Log.i("CategoryListView.onTouchEvent", 
					" Dragging: " + mStartPosition + 
					" First Visible: " + getFirstVisiblePosition() + 
					" Last Visible: " + getLastVisiblePosition() + 
					" Count: " + getCount());
			return true;
		} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			
			int position = pointToPosition(x, y);
			if (position != INVALID_POSITION) {
				if (mIsDown && !mIsDragging) {
					int viewPosition = position - getFirstVisiblePosition();
					View item = getChildAt(viewPosition);
					Log.i("CategoryListView.onTouchEvent", "Start Dragging: " + mStartPosition + " Over: " + position);
					if (item != null) {
	//					mXDragOffset = x - item.getLeft();
						mYDragOffset = y - item.getTop();
						mIsDragging = true;
						mStartPosition = position;
						startDrag(viewPosition, (int)ev.getRawY());
						return true;
					}
				} else if (mIsDown && mIsDragging) {
					
					int firstVisible = getFirstVisiblePosition();
					int lastVisible = getLastVisiblePosition();
					Log.i("CategoryListView.onTouchEvent", 
							" Current: " + position +
							" First Visible: " + firstVisible +
							" Last Visible: " + lastVisible);
					
					if (position != INVALID_POSITION && 
						firstVisible != INVALID_POSITION && 
						lastVisible != INVALID_POSITION) {
 						View child = getChildAt(position - firstVisible);
/*						
						if (child != null) {
							// Ensure that this child is properly visible
							requestChildRectangleOnScreen(
									child, 
									new Rect(
										child.getLeft(), 
										child.getTop(), 
										child.getRight(), 
										child.getBottom()), 
									true);
						}
*/						
						drag(x, (int)ev.getRawY(), position);
						if (mDragDropListener != null) {
							mDragDropListener.onDrag(mStartPosition, position);
						}
						
						if (position == firstVisible && firstVisible > 0) {
							Log.i("CategoryListView.onTouchEvent", "Scroll Up to: " + (firstVisible - 1));
							if (child == null)
								setSelection(position - 1);
							else
								setSelectionFromTop(position - 1, child.getTop());
						}
						else if (position == lastVisible && lastVisible < getCount() - 1) {
							Log.i("CategoryListView.onTouchEvent", "Scroll Down to: " + (position + 1));
							if (child == null)
								setSelection(position + 1);
							else 
								setSelectionFromTop(position + 1, child.getTop());
						}
					}
					return true;
				}
			}
			return super.onTouchEvent(ev);
				
		} else if (ev.getAction() == MotionEvent.ACTION_UP) {
			mIsDown = false;
			Log.i("CategoryListView.onTouchEvent", "MotionEvent.ACTION_UP");
			int endPosition = pointToPosition(x, y);
			if (mIsDragging) {
				mIsDragging = false;
				Log.i("CategoryListView.onTouchEvent", "Dropping Item: " + mStartPosition + " @ Position:" + endPosition);
				stopDrag(endPosition);
				return true;
			}
		} else if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
			Log.i("CategoryListView.onTouchEvent", "MotionEvent.ACTION_CANCEL");
			return true;
		} else if (ev.getAction() == MotionEvent.ACTION_OUTSIDE) {
			int position = pointToPosition(x, y);
			Log.i("CategoryListView.onTouchEvent", "MotionEvent.ACTION_OUTSIDE: " + position);
			return true;
		}
		
		return super.onTouchEvent(ev);
	}

	// enable the drag view for dragging
	private void startDrag(int itemIndex, int y) {

		// ListView only creates as many child views as are visible. getChildAt() iterates only
		// only through visible children. So we have to account for that.
		View item = getChildAt(itemIndex);
		if (item != null) {
			item.setDrawingCacheEnabled(true);
			
			// Create a copy of the drawing cache so that it does not get recycled
	        // by the framework when the list tries to clean up memory
        	int origColor = item.getDrawingCacheBackgroundColor();
        	item.setBackgroundColor(0x80808080);
			Bitmap origBitmap = item.getDrawingCache();
			item.setBackgroundColor(origColor);
	        if (origBitmap != null) {
//	        	Bitmap bitmap = origBitmap.copy(origBitmap.getConfig(), true);
//	        	item.setBackgroundColor(origColor);
		        WindowManager.LayoutParams mWindowParams = new WindowManager.LayoutParams();
		        mWindowParams.gravity = Gravity.TOP;
		        mWindowParams.x = 0;
		        mWindowParams.y = y - mYDragOffset;
		        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
		                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
		                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		        mWindowParams.format = PixelFormat.TRANSLUCENT;
		        mWindowParams.windowAnimations = 0;
		        
		        Context context = getContext();
		        ImageView v = new ImageView(context);
		        v.setImageBitmap(origBitmap);      
		
		        WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		        mWindowManager.addView(v, mWindowParams);
		        mDragView = v;
	        }
		}
	}
	
	// move the drag view
	private void drag(int x, int y, int position) {
		if (mDragView != null) {
			WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) mDragView.getLayoutParams();
			layoutParams.x = 0;// x - mXDragOffset;
			layoutParams.y = y - mYDragOffset;
			WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
			mWindowManager.updateViewLayout(mDragView, layoutParams);
			if (mDragDropListener != null)
				mDragDropListener.onDrag(mStartPosition, position);
		}
	}

	// destroy drag view
	private void stopDrag(int position) {
		if (mDragView != null) {
			mDragView.setVisibility(GONE);
			WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
			wm.removeView(mDragView);
			mDragView.setImageDrawable(null);
			mDragView = null;
			if (mDragDropListener != null && 
					position != INVALID_POSITION && 
					mStartPosition != position) {
				mDragDropListener.onDrop(mStartPosition, position);
				invalidateViews();
			}
        }
	}
}
