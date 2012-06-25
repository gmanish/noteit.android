
package com.geekjamboree.noteit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.geekjamboree.noteit.ActionItem;
import com.geekjamboree.noteit.NoteItApplication.ItemAndStats;
import com.geekjamboree.noteit.NoteItApplication.OnAddItemListener;
import com.geekjamboree.noteit.NoteItApplication.OnFetchItemsListener;
import com.geekjamboree.noteit.NoteItApplication.OnItemVoteListener;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;
import com.geekjamboree.noteit.NoteItApplication.ShoppingList;
import com.geekjamboree.noteit.QuickAction;

import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.Item;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ItemListActivity 
		extends ExpandableListActivity 
		implements NoteItApplication.OnFetchItemsListener {
	
	QuickAction 					mQuickAction = null;
	QuickAction						mExpandCollapseQA = null;
	AtomicInteger					mSelectedGroup = new AtomicInteger();
	AtomicInteger					mSelectedChild = new AtomicInteger();
	long							mSelectedItemID = 0;
	boolean							mDisplayExtras = true;
	boolean							mDisplayCategoryExtras = true;
	Integer							mFontSize = 3;
	boolean							mHideDoneItems = false;
	TitleBar						mToolbar;
	Button							mShoppingListButton;
	boolean							mIsItemListFetched = false;
	SharedPreferences				mPrefs;
	String							mCurrencyFormat = new String();
	boolean							mLoadingMore = false;
	float							mPendingTotal = 0f;
	ViewFlipperHack					mLoadMoreFlipper;
	LayoutInflater					mLayoutInflater;
	AlertDialog 					mInstallScanAppDialog = null;
	Item							mTempItemToPassToDialog = null;
	View							mContentView = null;
	static final int ADD_ITEM_REQUEST = 0;	
	
	static final int QA_ID_EDIT 		= 0;
	static final int QA_ID_DELETE		= 1;
	static final int QA_ID_BOUGHT		= 2;
	static final int QA_ID_COPY			= 3;
	static final int QA_ID_MOVE 		= 4;
	static final int QA_EXPAND			= 5;
	static final int QA_COLLAPSE		= 6;
	static final int QA_ID_LIKE			= 7;
	
	static final int ITEM_FONT_LARGE 	= 0;
	static final int ITEM_FONT_MEDIUM	= 1;
	static final int ITEM_FONT_SMALL	= 2;
	
	static final int DIALOG_ADD_ITEM 	= 99;
	static final int DIALOG_EDIT_ITEM	= 100;
	
	static final String SELECTED_GROUP 			= "selGroup";
	static final String SELECTED_CHILD 			= "selChild";
	static final String SELECTED_ITEM_ID		= "selItemID";
	static final String IS_ITEMLIST_FETCHED 	= "IS_ITEM_LIST_FETCHED";
	
	ViewConfiguration 	mVC 						= null;
	int 				mSwipeMinDistance 			= 0;
	int 				mSwipeThresholdVelocity 	= 0;
	int 				mSwipeMaxVDistance 			= 0;
	final int			MAX_SWIPE_DISTANCE			= 50;
	
    protected enum ItemType {
    	PENDING, 
    	DONE, 
    	GROUP,
    	BOLD
    }
    
    protected enum ProductSearchMethod {
    	GOOGLE_SEARCH,
    	SEARCH_UPC,
    	NOTE_IT
    }
    
	public static interface OnChildGestureListener {
		
		public boolean onChildClick(
				ExpandableListView parent, 
				View v, 
				int groupPosition, 
				int childPosition, 
				long id);
		
		public boolean onChildLeftSwipe(
				ExpandableListView parent, 
				View v, 
				int groupPosition, 
				int childPosition, 
				long id);
		
		public boolean onChildRightSwipe(
				ExpandableListView parent, 
				View v, 
				int groupPosition, 
				int childPosition, 
				long id);
	}
	
	protected AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {
		
		public void onScrollStateChanged(
				AbsListView view, 
				int scrollState) {
		}
		
		public void onScroll(
			AbsListView view, 
			int firstVisibleItem,
			int visibleItemCount, 
			int totalItemCount) {
		
			NoteItApplication app = (NoteItApplication) getApplication();
			if (firstVisibleItem + visibleItemCount >= totalItemCount && 
				app != null &&
				!mLoadingMore) {
				
				Log.i("ItemsListView.onScrollListener", 
						"FirstVisible: " + firstVisibleItem + 
						" VisibleItemCount: " + visibleItemCount + 
						" TotalItemsCount:" + totalItemCount);

				ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
				Log.i("ItemsListView.onScrollListener", "App Items: " + app.getItems().size());
				if (totalItemCount - 
					((ExpandableLVRightIndicator)getExpandableListView()).getGroupCount() >= 
					adapter.getTotalChildrenCount()) {
					if (app.isMoreItemsPending()) {
						fetchItems(ItemListActivity.this);
					}
					else
						Log.i("ItemListActivity.onScroll", "NOP: No More Pending");
				} else {
					Log.i("ItemListActivity.onScroll", "NOP: Already Have Sufficient Data");
					//doDisplayItems(app.getItems());
				}
			}
		}
	};
	
	protected SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener = 
			new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences,
					String key) {
				if (key.equals("Display_Price_Quantity") || 
						key.equals("Item_Font_Size")) {
					Log.i("ItemListActivity.onSharedPreferenceChanged", "Display_Price_Quantity preference changed");
					mDisplayExtras = sharedPreferences.getBoolean("Display_Price_Quantity", true);
					mDisplayCategoryExtras = sharedPreferences.getBoolean("Display_Category_Totals", true);
					mFontSize = Integer.valueOf(sharedPreferences.getString("Item_Font_Size", "3"));
			        mHideDoneItems = sharedPreferences.getBoolean(
			        		MainPreferenceActivity.kPref_HideDoneItems, 
			        		MainPreferenceActivity.kPref_HideDoneDefault);
					getExpandableListView().invalidateViews();
				}
				
			}
		};

	/* 
	 * The following was added for detecting fling across an item. There's a problem with the onChildClick
	 * listener in that it is always called whether we detect a fling on not leading to both a fling and
	 * item click being fired. This obviously is not desirable. Hence we do not use the onClickClick 
	 * listener on the listview. Instead we've simulated onChildClick thorough the SimpleGestureListener 
	 * and it seems to work fine. Yippie!
	 */
	protected SimpleOnGestureListener mGestureListener = 
			new SimpleOnGestureListener() {

				@Override
				public boolean onFling(
						MotionEvent e1, 
						MotionEvent e2,
						float velocityX, 
						float velocityY) {

		            try {
		            	Log.i("onFling", "Y Offset: " + Math.abs(e1.getY() - e2.getY()));
		                if (Math.abs(e1.getY() - e2.getY()) > mSwipeMaxVDistance) {
		                	Log.i("onFling", "Too much vertical movement.");
		                    return false;
		                }
		                
		                // right to left swipe
		                if(Math.abs(e1.getX() - e2.getX()) > mSwipeMinDistance 
		                		&& Math.abs(velocityX) > mSwipeThresholdVelocity) {

							ExpandableListView lv = getExpandableListView();
				            int pos = lv.pointToPosition((int)e1.getX(), (int)e1.getY());
				            if (pos != ListView.INVALID_POSITION) {
					            
				            	int 	viewPos = pos - lv.getFirstVisiblePosition(); 
					            View 	view = lv.getChildAt(viewPos);
					            
					            if (view != null) {

					            	long packedPosition = lv.getExpandableListPosition(pos);
				            		// Don't send onFling for groups
					            	if (ExpandableListView.getPackedPositionType(packedPosition) == 
					            			ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					            		
							            if (e1.getX() > e2.getX()) {
							            	mChildClickListener.onChildLeftSwipe(
							            			lv, 
							            			view, 
							            			ExpandableListView.getPackedPositionGroup(packedPosition), 
						            				ExpandableListView.getPackedPositionChild(packedPosition), 
						            				view.getId());
							            	
						                }  else {
							            	mChildClickListener.onChildRightSwipe(
							            			lv, 
							            			view, 
							            			ExpandableListView.getPackedPositionGroup(packedPosition), 
						            				ExpandableListView.getPackedPositionChild(packedPosition), 
						            				view.getId());
						                }
					            	}
					            }
				            }
		                }
		            } catch (Exception e) {
		                // nothing
		            }
		            Log.i("ItemListActivity.SimpleOnGestureListener", "onFling Detected");
					return false;
				}

				@Override
		        public boolean onSingleTapUp(MotionEvent e) {
		            
					ExpandableListView lv = getExpandableListView();
		            int pos = lv.pointToPosition((int)e.getX(), (int)e.getY());
		            if (pos != ListView.INVALID_POSITION) {
			            int 	viewPos = pos - lv.getFirstVisiblePosition(); 
			            View 	view = lv.getChildAt(viewPos);
			            
			            if (view != null) {
			            	long packedPosition = lv.getExpandableListPosition(pos);
			            	if (ExpandableListView.getPackedPositionType(packedPosition) == 
			            			ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			            		
			            		// Don't send itemClick for groups
			            		mChildClickListener.onChildClick(
			            				lv, 
			            				view, 
			            				ExpandableListView.getPackedPositionGroup(packedPosition), 
			            				ExpandableListView.getPackedPositionChild(packedPosition), 
			            				view.getId());
			            	}
			            }
		            }
		            return false;
		        }
			};
	
	protected GestureDetector mGestureDetector = new GestureDetector(mGestureListener);
	
	protected View.OnTouchListener mTouchListener = 
			new View.OnTouchListener() {
				
				public boolean onTouch(View v, MotionEvent event) {
					if (mGestureDetector != null)
						return mGestureDetector.onTouchEvent(event);
					else
						return false;
				}
			};
			
	protected OnChildGestureListener mChildClickListener = new OnChildGestureListener() {
		
		public boolean onChildClick(
				ExpandableListView parent, 
				View v,
				int groupPosition, 
				int childPosition, long id) {

			mQuickAction.show(v);
			mSelectedGroup.set(groupPosition);
			mSelectedChild.set(childPosition);
			mSelectedItemID = ((Item) getExpandableListView().getExpandableListAdapter().getChild(groupPosition, childPosition)).mID;
			return false;
		}
		
		public boolean onChildLeftSwipe(
				ExpandableListView parent, 
				View v, 
				int groupPosition, 
				int childPosition, 
				long id) {

			mSelectedGroup.set(groupPosition);
			mSelectedChild.set(childPosition);
			mSelectedItemID = ((Item) parent.getExpandableListAdapter().getChild(groupPosition, childPosition)).mID;
			doDeleteItem();
			return false;
		}
		
		public boolean onChildRightSwipe(
				ExpandableListView parent, 
				View v, 
				int groupPosition, 
				int childPosition, 
				long id) {
			
			mSelectedGroup.set(groupPosition);
			mSelectedChild.set(childPosition);
			mSelectedItemID = ((Item) parent.getExpandableListAdapter().getChild(groupPosition, childPosition)).mID;
			doToggleMarkDone();
			return false;
		}
	};
	
	/**
	* Interface for shake gesture.
	* 
	*/
	public interface OnShakeListener {
		
		/**
		* Called when shake gesture is detected.
		*/
		void onShake();
	}
	
	/**
	 * Listener that detects shake gesture.
	 */
	public class ShakeEventListener implements SensorEventListener {

//		/** Minimum movement force to consider. */
//		private static final int MIN_FORCE = 10;
//			
//		/**
//		* Minimum times in a shake gesture that the direction of movement needs to
//		* change.
//		*/
//		private static final int MIN_DIRECTION_CHANGE = 3;
//			
//		/** Maximum pause between movements. */
//		private static final int MAX_PAUSE_BETHWEEN_DIRECTION_CHANGE = 200;
//		
//		/** Maximum allowed time for shake gesture. */
//		private static final int MAX_TOTAL_DURATION_OF_SHAKE = 400;
//			
//		/** Time when the gesture started. */
//		private long mFirstDirectionChangeTime = 0;
//			
//		/** Time when the last movement started. */
//		private long mLastDirectionChangeTime;
//			
//		/** How many movements are considered so far. */
//		private int mDirectionChangeCount = 0;
//			
//		/** The last x position. */
//		private float lastX = 0;
//			
//		/** The last y position. */
//		private float lastY = 0;
//			
//		/** The last z position. */
//		private float lastZ = 0;
		
		private long lastUpdate = -1;
//		private float x, y, z;
//		private float last_x, last_y, last_z;
//		private static final int SHAKE_THRESHOLD = 800;
		
		/** OnShakeListener that is called when shake is detected. */
		private OnShakeListener mShakeListener = null;

		public void setOnShakeListener(OnShakeListener listener) {
			mShakeListener = listener;
		}

		private void getAccelerometer(SensorEvent event) {
			float[] values = event.values;
			// Movement
			float x = values[0];
			float y = values[1];
			float z = values[2];

			float accelationSquareRoot = (x * x + y * y + z * z)
					/ (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
			long actualTime = System.currentTimeMillis();
			Log.i("onSensorChanged", "accelationSquareRoot: " + accelationSquareRoot);
			
			if (accelationSquareRoot >= 2) //
			{
				if (actualTime - lastUpdate < 200) {
					return;
				}
				lastUpdate = actualTime;
				mShakeListener.onShake();
			}
		}

		public void onSensorChanged(SensorEvent se) {
	  		
	  		Log.d("onSensorChanged", "X: " + se.values[SensorManager.DATA_X] + " Y: " + se.values[SensorManager.DATA_Y] + " Z: " + se.values[SensorManager.DATA_Z]);
	  		if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
	  		{
	  			getAccelerometer(se);
	  			/*
	  		    long curTime = System.currentTimeMillis();
	  		    // only allow one update every 100ms.
	  		    if ((curTime - lastUpdate) > 100) {
		  			long diffTime = (curTime - lastUpdate);
		  			lastUpdate = curTime;
		  	 
		  			x = se.values[SensorManager.DATA_X];
		  			y = se.values[SensorManager.DATA_Y];
		  			z = se.values[SensorManager.DATA_Z];
		  	 
		  			float speed = Math.abs(x+y+z - last_x - last_y - last_z)
		  	                              / diffTime * 10000;
		  			Log.d("onSensorChanged: ", "Speed: " + speed);
		  			if (speed > SHAKE_THRESHOLD) {
		  			    // yes, this is a shake action! Do something about it!
		  				mShakeListener.onShake();
		  			}
		  			last_x = x;
		  			last_y = y;
		  			last_z = z;
	  		    }*/
	  		}
	  		/*
			// get sensor data
			float x = se.values[SensorManager.DATA_X];
			float y = se.values[SensorManager.DATA_Y];
			float z = se.values[SensorManager.DATA_Z];

		  // calculate movement
			float totalMovement = Math.abs(x + y + z - lastX - lastY - lastZ);
			Log.i("onSensorChanged", "totalMovement:" + totalMovement);
			if (totalMovement > MIN_FORCE) {

				// get time
				long now = System.currentTimeMillis();

				// store first movement time
				if (mFirstDirectionChangeTime == 0) {
					mFirstDirectionChangeTime = now;
					mLastDirectionChangeTime = now;
				}
	
				// check if the last movement was not long ago
				long lastChangeWasAgo = now - mLastDirectionChangeTime;
				Log.i("onSensorChange", "lastChangeWasAgo: " + lastChangeWasAgo);
				if (lastChangeWasAgo < MAX_PAUSE_BETHWEEN_DIRECTION_CHANGE) {
	
					// store movement data
					mLastDirectionChangeTime = now;
					mDirectionChangeCount++;
					
					// store last sensor data 
					lastX = x;
					lastY = y;
					lastZ = z;
					
					Log.i("OnSensorChange", "mDirectionChangeCount: " + mDirectionChangeCount);
	
			        // check how many movements are so far
			        if (mDirectionChangeCount >= MIN_DIRECTION_CHANGE) {
			
			          // check total duration
			          long totalDuration = now - mFirstDirectionChangeTime;
			          if (totalDuration < MAX_TOTAL_DURATION_OF_SHAKE) {
			            mShakeListener.onShake();
			            resetShakeParameters();
			          }
			        }
	
				} else {
					resetShakeParameters();
				}
			}*/
		}

		/**
		 * Resets the shake parameters to their default values.
		 */
//		private void resetShakeParameters() {
//		    mFirstDirectionChangeTime = 0;
//		    mDirectionChangeCount = 0;
//		    mLastDirectionChangeTime = 0;
//		    lastX = 0;
//		    lastY = 0;
//		    lastZ = 0;
//		}
//
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	}
	
//	private SensorManager 				mSensorManager = null;
//	private final ShakeEventListener 	mSensorListener = new ShakeEventListener();
	
	class ShoppingListAdapterWithIcons extends ArrayAdapter<ShoppingList> {

		public ShoppingListAdapterWithIcons(
				Context context, 
				int resource,
				int textViewResourceId, 
				List<ShoppingList> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View 			view = super.getView(position, convertView, parent);
			ShoppingList	item = getItem(position);
			
			TextView 		itemCount = (TextView) view.findViewById(R.id.shoppinglist_itemCount);
			if (itemCount != null && item != null) {
				itemCount.setText(" (" + String.valueOf(item.mItemCount) + ")");
				itemCount.setVisibility(View.VISIBLE);
			}
			
			TextView listName = (TextView) view.findViewById(R.id.shoppinglist_name);
			if (listName != null) {
				if (item.mUserID != ((NoteItApplication) getApplication()).getUserID())
					listName.setCompoundDrawablesWithIntrinsicBounds(
							getResourceIdFromAttribute(R.attr.Spinner_SharedShoppingList), 0, 0, 0);
				else 
					listName.setCompoundDrawablesWithIntrinsicBounds(
							getResourceIdFromAttribute(R.attr.Spinner_ShoppingList_Cart), 0, 0, 0);
			}

			TextView listCount = (TextView) view.findViewById(R.id.shoppinglist_itemCount);
			if (listCount != null) {
				listCount.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
			return view;
		}
	}
		
	public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);

    	final float scale 			= getResources().getDisplayMetrics().density;
    	mVC 						= ViewConfiguration.get(this);
    	mSwipeMinDistance 			= mVC.getScaledTouchSlop();
    	mSwipeThresholdVelocity 	= mVC.getScaledMinimumFlingVelocity();
    	mSwipeMaxVDistance 			= (int)(MAX_SWIPE_DISTANCE * scale + 0.5f);

    	
    	if (savedInstanceState != null) {
    		Log.i("ItemListActivity.onCreate", "Got a valid savedInstanceState");
    		mSelectedGroup.set(savedInstanceState.getInt(SELECTED_GROUP));
    		mSelectedChild.set(savedInstanceState.getInt(SELECTED_CHILD));
    		mSelectedItemID = savedInstanceState.getLong(SELECTED_ITEM_ID);
    		mIsItemListFetched = savedInstanceState.getBoolean(IS_ITEMLIST_FETCHED);
    	}
    	
        NoteItApplication app = (NoteItApplication) getApplication();
        if (app.getShoppingListCount() <= 0) {
        	// Send user back to the ShoppingListActivity where a list can be created
        	Intent intent = new Intent(this, ShoppingListActivity.class);
        	startActivity(intent);
        	finish();
        	return;
        }
        
    	TitleBar.RequestNoTitle(this);
        setContentView(R.layout.itemlists);
        mToolbar = (TitleBar) findViewById(R.id.itemslist_title);
        mContentView = findViewById(R.id.layout_itemslist);
        mToolbar.SetTitle(app.getShoppingList().get(app.getCurrentShoppingListIndex()).mName);
        doSetupToolbarButtons(app.getShoppingList().get(app.getCurrentShoppingListIndex()).mName);
                
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Set up Quick Actions
        ActionItem boughtItem 	= new ActionItem(
					        		QA_ID_BOUGHT, 
					        		getResources().getString(R.string.itemlistqe_bought), 
					        		getResources().getDrawable(R.drawable.tick));
        ActionItem editItem 	= new ActionItem(
									QA_ID_EDIT,
									getResources().getString(R.string.itemlistqe_edit),
									getResources().getDrawable(R.drawable.edit)); 
        ActionItem deleteItem 	= new ActionItem(
									QA_ID_DELETE,
									getResources().getString(R.string.itemlistqe_delete),
									getResources().getDrawable(R.drawable.delete));
        ActionItem copyItem 	= new ActionItem(
									QA_ID_COPY,
									getResources().getString(R.string.itemlistqe_copy),
									getResources().getDrawable(R.drawable.copy));
        ActionItem moveItem 	= new ActionItem(
									QA_ID_MOVE,
									getResources().getString(R.string.itemlistqe_move),
									getResources().getDrawable(R.drawable.move));
        ActionItem likeItem 	= new ActionItem(
        							QA_ID_LIKE,
        							getResources().getString(R.string.itemlistqe_like),
        							getResources().getDrawable(R.drawable.thumbs_up));

        mQuickAction = new QuickAction(this);
		mQuickAction.addActionItem(boughtItem);
		mQuickAction.addActionItem(editItem);
		mQuickAction.addActionItem(deleteItem);
		mQuickAction.addActionItem(copyItem);
		mQuickAction.addActionItem(moveItem);
		mQuickAction.addActionItem(likeItem);
		
		//setup the action item click listener
		mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			
			public void onItemClick(QuickAction quickAction, int pos, int actionId) {

				switch(actionId){
					case QA_ID_BOUGHT:
						doToggleMarkDone();
						break;
					case QA_ID_EDIT:
						doEditItem();
						break;
					case QA_ID_DELETE:
						doDeleteItem();
						break;
					case QA_ID_COPY:
						doCopyItem();
						break;
					case QA_ID_MOVE:
						doMoveItem();
						break;
					case QA_ID_LIKE:
						doLikeItem();
						break;
				}
			}
		});
		
		mQuickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {			
			public void onDismiss() {
			}
		});    
		
		// Sensor
//		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//		if (mSensorManager != null) {
//			mSensorManager.registerListener(
//					mSensorListener, 
//					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
//					SensorManager.SENSOR_DELAY_NORMAL);
//			
//			mSensorListener.setOnShakeListener(new OnShakeListener() {
//				
//				public void onShake() {
//					Toast.makeText(ItemListActivity.this, "Shake", Toast.LENGTH_SHORT);
//				}
//			});
//		}
		
        ItemsExpandableListAdapter adapter = new ItemsExpandableListAdapter(this);
		mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE); 
		getExpandableListView().setTextFilterEnabled(true);
		getExpandableListView().setOnTouchListener(mTouchListener);
		addFooterToListView(true);
		getExpandableListView().setAdapter(adapter);
        
    	// Populating of the list with items is handled in OnScrollListener for the list view
		if (app.getShoppingListCount() > 0 && !mIsItemListFetched) {
			fetchItems(this);
		} else {
			Log.i("ItemListActivity.onCreate", "Skipping fetchItems");
			doDisplayItems(app.getItems());
		}
		
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getInt(LoginActivity.DISPLAY_UNREAD_MESSAGES, 0) > 0) {
	        // Read the messages in inbox
			InboxMessages inboxMessages = new InboxMessages(app, ItemListActivity.this, getExpandableListView());
			if (inboxMessages != null) {
				inboxMessages.doDisplayUnreadMessages();
			}
        }
        
    	doFetchAndDisplayPendingTotal();
    }

    @Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.itemlist_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
    
    public void fetchItems(OnFetchItemsListener listener) {
    	NoteItApplication app = (NoteItApplication) getApplication();
    	mToolbar.showInderminateProgress(getString(R.string.progress_message));
    	mLoadMoreFlipper.showNext();
		mLoadingMore = true;
		app.fetchItems(
			!mPrefs.getBoolean("Delete_Bought_Items", true),
				true,
				listener);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemlist_add:
			doAddItem();
			break;
		case R.id.itemlist_home:
			Intent intent = new Intent(ItemListActivity.this, DashBoardActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			break;
		case R.id.itemlist_email:
			doShare();
			break;
		case R.id.itemlist_alldone:
			doAllDone();
			break;
		case R.id.itemlist_settings:
			startActivity(new Intent(this, MainPreferenceActivity.class));
			break;
		case R.id.itemlist_scan:
			doScanBarcode();
			break;
		case R.id.itemlist_logout:
			doSignOut();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SELECTED_GROUP, mSelectedGroup.get());
		outState.putInt(SELECTED_CHILD, mSelectedChild.get());
		outState.putBoolean(IS_ITEMLIST_FETCHED, mIsItemListFetched);
		Item selItem = null;
		ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
		if (mSelectedGroup.get() < adapter.getGroupCount() && 
			mSelectedChild.get() < adapter.getChildrenCount(mSelectedGroup.get())) {
			selItem = (Item) getExpandableListView().getExpandableListAdapter().getChild(
									mSelectedGroup.get(), mSelectedChild.get());
		}
		if (selItem != null) {
			outState.putLong(SELECTED_ITEM_ID, selItem.mID);
		} else {
			outState.putLong(SELECTED_ITEM_ID, 0);
		}
	}

	@Override
	protected void onPause() {
		SharedPreferences.Editor editor = mPrefs.edit(); 
		editor.putLong("LastUsedShoppingListID", ((NoteItApplication)getApplication()).getCurrentShoppingListID());
		editor.commit();
		mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
		if (mInstallScanAppDialog != null && mInstallScanAppDialog.isShowing())
			mInstallScanAppDialog.dismiss();
//	    mSensorManager.unregisterListener(mSensorListener);
		super.onPause();
	}

	@Override
	protected void onResume() {

		super.onResume();
		mPrefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
		mDisplayExtras = mPrefs.getBoolean("Display_Price_Quantity", true);
		mDisplayCategoryExtras = mPrefs.getBoolean("Display_Category_Totals", true);
		mHideDoneItems = mPrefs.getBoolean("Delete_Bought_Items", true);
        mFontSize = Integer.valueOf(mPrefs.getString("Item_Font_Size", "3"));
		getExpandableListView().invalidateViews();
		mCurrencyFormat = ((NoteItApplication) getApplication()).getCurrencyFormat(false);
		doDisplayPendingTotal();
//		mSensorManager.registerListener(
//				mSensorListener, 
//				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
//				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onPostExecute(long retval, ArrayList<Item> items, String message) {
    	
    	try {
    		mToolbar.hideIndeterminateProgress();
			mLoadMoreFlipper.showPrevious();
	    	if (retval == 0) {
	        	mIsItemListFetched = true;
	        	doDisplayItems(items);
	    	}
	    	else {
	    		CustomToast.makeText(
	    				getApplicationContext(),
	    				mContentView,
	    				"The server seems to be out of its mind. Please try later.").show(true);
	    	}
    	} catch (Exception e) {
    		CustomToast.makeText(
    				this,
    				mContentView,
    				e.getMessage()).show(true);
    	}
		if (mLoadingMore) 
			mLoadingMore = false;
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	Log.i("ItemListActivity.onActivityResult", "requestCode:" + requestCode + " resultCode: " + resultCode);
    	final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
    	if (scanResult != null && scanResult.getFormatName() != null && scanResult.getContents() != null) {
    		// handle scan result
        	final NoteItApplication app = (NoteItApplication) getApplication();
    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
        	app.searchItemByBarcode(
        		NoteItApplication.barcodeFormatFromString(scanResult.getFormatName()), 
        		scanResult.getContents()/*"602527246949"*/, 
        		new NoteItApplication.OnSearchBarcodeListener() {
    			
	    			public void onSearchResults(long retVal, final Item item, String message) {
	    				try {
	    					if (retVal == 0 && item != null) {
			    				mTempItemToPassToDialog = app.new Item(item);
			    				mTempItemToPassToDialog.mBarcodeFormat = NoteItApplication.barcodeFormatFromString(scanResult.getFormatName());
			    				mTempItemToPassToDialog.mBarcode = scanResult.getContents(); //"602527246949";
			    				mTempItemToPassToDialog.mListID = app.getCurrentShoppingListID();
			    				if (mTempItemToPassToDialog.mCategoryID <= 0)
			    					mTempItemToPassToDialog.mCategoryID = 1; // Uncategorized
			    				doAddItem(mTempItemToPassToDialog);
	    					} else if (retVal == 0){
		    					// Not Found, ask if user wants to add
		    					final AlertDialog dialog = new AlertDialog.Builder(ItemListActivity.this).create();
		    					
		    					dialog.setTitle(getResources().getString(R.string.addedit_Title));
		    					dialog.setMessage(getResources().getString(R.string.itemlist_itemnotfound));
		    					dialog.setButton(DialogInterface.BUTTON1, "Yes", new DialogInterface.OnClickListener() {
		    						
		    						public void onClick(DialogInterface dialog, int which) {
		    							dialog.dismiss();
	    			    				mTempItemToPassToDialog = app.new Item();
	    			    				mTempItemToPassToDialog.mBarcodeFormat = NoteItApplication.barcodeFormatFromString(scanResult.getFormatName());
	    			    				mTempItemToPassToDialog.mBarcode = scanResult.getContents(); //"602527246949";
	    			    				showDialog(DIALOG_ADD_ITEM);
		    						}
		    					});
		    			
		    					dialog.setButton(DialogInterface.BUTTON2, "No", new DialogInterface.OnClickListener() {
		    						
		    						public void onClick(DialogInterface dialog, int which) {
		    							dialog.dismiss();
		    						}
		    					});

		    					dialog.show();
		    				} else {
		    		    		CustomToast.makeText(
		    		    				ItemListActivity.this,
		    		    				mContentView,
		    		    				message).show(true);
		    				}
	    				} finally {
	    					mToolbar.hideIndeterminateProgress();
	    				}
	    			}
    		});
        } else if (requestCode == ADD_ITEM_REQUEST && resultCode == RESULT_OK) {
    		
    		// refresh our view
    		ArrayList<Item> addedItems = new ArrayList<Item>();
    		if (addedItems.size() > 0) {
    	
    			// New items were added by the called activity, we need to add them to our view
    			ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)getExpandableListView().getExpandableListAdapter();
    			for (Item item : addedItems) {
    				Category category = ((NoteItApplication) getApplication()).getCategory(item.mCategoryID);
    				adapter.AddItem(item, category);
    			}
				adapter.notifyDataSetChanged();
    		}
    	}
    }
    
    protected void setUpQuickActions() {
	}
    
    protected void doAddItem() {
    	showDialog(DIALOG_ADD_ITEM);
    }
    
    protected void doEditItem() {
    	showDialog(DIALOG_EDIT_ITEM);
    }
    
    @Override
	protected Dialog onCreateDialog(int id) {

    	switch (id) {
    	case DIALOG_ADD_ITEM:
    		Log.i("ItemListActivity.onCreateDailog", "DIALOG_EDIT_ITEM");
        	AddEditItemDialog addDialog = new AddEditItemDialog(
    				this,
    				(NoteItApplication)getApplication(),
    				new AddEditItemDialog.addItemListener() {
    					
    					public void onAddItem(Item item) {
    						doAddItem(item);
    					}
    				});
    		return addDialog;
    		
    	case DIALOG_EDIT_ITEM:
    		Log.i("ItemListActivity.onCreateDailog", "DIALOG_EDIT_ITEM");
    		AddEditItemDialog editDialog = new AddEditItemDialog(
    			this, 
    			(NoteItApplication)getApplication(),
    			new AddEditItemDialog.editItemListener() {
    				
    				public void onEditItem(final Item oldItem, final Item newItem, final int bitMask) {
    					doEditItem(oldItem, newItem, bitMask);
    				}
    			},
    			new AddEditItemDialog.navigateItemsListener() {
    				
    				public long onPreviousItem() {
    					
    					ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
    					if (adapter.getPrevChildPosition(mSelectedGroup, mSelectedChild)) {
    						
    						Item prevItem = (Item) adapter.getChild(mSelectedGroup.get(), mSelectedChild.get());
    						if (prevItem != null) {
    							return mSelectedItemID = prevItem.mID;
    						}
    					}
    					return 0;
    				}
    				
    				public long onNextItem() {
    					
    					ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
    					if (adapter.getNextChildPosition(mSelectedGroup, mSelectedChild)) {
    						
    						Item nextItem = (Item) adapter.getChild(mSelectedGroup.get(), mSelectedChild.get());
    						if (nextItem != null) {
    							return mSelectedItemID = nextItem.mID;
    						}
    					}
    					return 0;
    				}
    			},
    			mSelectedItemID);
    		
    		return editDialog;
    		
    	default:
    		return super.onCreateDialog(id);
    	}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		
		super.onPrepareDialog(id, dialog);
		switch(id) {
		case DIALOG_ADD_ITEM:
			AddEditItemDialog addDialog = (AddEditItemDialog) dialog;
			addDialog.clearDialogFields();
			if (mTempItemToPassToDialog != null) {
				addDialog.setItem(mTempItemToPassToDialog);
			}
			break;
		case DIALOG_EDIT_ITEM:
        	AddEditItemDialog editDialog = (AddEditItemDialog) dialog;
        	editDialog.setItemID(mSelectedItemID);
			break;
		}
	}

	protected void doDeleteItem() {
    	
    	final Item selItem = (Item) getExpandableListView().getExpandableListAdapter().getChild(mSelectedGroup.get(), mSelectedChild.get());
    	if (selItem != null) {
    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
    		((NoteItApplication) getApplication()).deleteItem(selItem.mID, new OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					try {
						if (resultCode == 0) {
							// Remove the item from our adapter
							ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)getExpandableListView().getExpandableListAdapter();
							adapter.DeleteItem(selItem);
							adapter.notifyDataSetChanged();
							doDeductFromPendingTotal(selItem.mUnitPrice * selItem.mQuantity);
							doDisplayPendingTotal();
						} else
	    		    		CustomToast.makeText(
	    		    				ItemListActivity.this,
	    		    				mContentView,
	    		    				message).show(true);
					} finally {
						mToolbar.hideIndeterminateProgress();
					}
				}
			});
    	}
    }

	protected void doAddItem(Item item) {
		
		final NoteItApplication 			app = (NoteItApplication) getApplication();
		final ExpandableListView 			listView = (ExpandableListView)getExpandableListView();
		final ItemsExpandableListAdapter	adapter = (ItemsExpandableListAdapter)listView.getExpandableListAdapter();				
				
		mToolbar.showInderminateProgress(getString(R.string.progress_message));
		app.addItem(item, new OnAddItemListener() {
			
			public void onPostExecute(long resultCode, Item newItem, String message) {
				try {
					if (resultCode == 0){
				    	
	    				Category 	category = app.getCategory(newItem.mCategoryID);
	    				long 		packedPosition = adapter.AddItem(newItem, category);
	    				
	    				if (packedPosition != ExpandableListView.PACKED_POSITION_TYPE_NULL) {
		    				Log.d("ItemListActivity.doAddItem()", "Item added: " + newItem.mName);
							adapter.notifyDataSetChanged();
							doAddToPendingTotal(newItem.mUnitID * newItem.mUnitPrice);
							doDisplayPendingTotal();
							
							// Select the just added child
							int groupPos = ExpandableListView.getPackedPositionGroup(packedPosition);
							int childPos = ExpandableListView.getPackedPositionGroup(packedPosition);
							
							if (groupPos != ExpandableListView.PACKED_POSITION_VALUE_NULL &&
								childPos != ExpandableListView.PACKED_POSITION_VALUE_NULL) {
								getExpandableListView().setSelectedChild(
										groupPos, 
										childPos, 
										true);
							}
	    				} else {
	    					Log.e("doAddItem", "Could not add item to adapter. PACKED_POSITION_TYPE_NULL returned.");
	    				}
					}
					else {
    		    		CustomToast.makeText(
    		    				ItemListActivity.this,
    		    				mContentView,
    		    				message).show(true);
					}
				} finally {
		    		mToolbar.hideIndeterminateProgress();
				}
			}
		});
	}
	
	protected void doEditItem(final Item oldItem, Item newItem, final int bitMask) {

		NoteItApplication app = (NoteItApplication) getApplication();
		mToolbar.showInderminateProgress(getString(R.string.progress_message));
		app.editItem(
				bitMask, 
    			newItem,  
    			new OnAddItemListener() {
					
					public void onPostExecute(long resultCode, Item item, String message) {
						try {
							if (resultCode == 0) {
				    			ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)getExpandableListView().getExpandableListAdapter();
			    				Category category = ((NoteItApplication) getApplication()).getCategory(item.mCategoryID);
			    				if ((bitMask & Item.ITEM_CATEGORYID) > 0 || (bitMask & Item.ITEM_NAME) > 0) {
				    				adapter.DeleteItem(oldItem);
				    				adapter.AddItem(item, category);
				    				doAddToPendingTotal(item.mUnitPrice * item.mQuantity - oldItem.mUnitPrice * oldItem.mQuantity);
				    				doDisplayPendingTotal();
			    				} else {
			    					Item selItem = (Item) getExpandableListView().getExpandableListAdapter().getChild(
			    							mSelectedGroup.get(), 
			    							mSelectedChild.get());
			    					selItem.copyFrom(item);
			    				}
								adapter.notifyDataSetChanged();
							}
							else
		    		    		CustomToast.makeText(
		    		    				ItemListActivity.this,
		    		    				mContentView,
		    		    				message).show(true);
						} finally {
				    		mToolbar.hideIndeterminateProgress();
						}
					}
				});
	}
	
	protected void doCopyItem() {
		
		final ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
		if (mSelectedGroup.get() < adapter.getGroupCount() &&
		    mSelectedGroup.get() >= 0 &&
		    mSelectedChild.get() < adapter.getChildrenCount(mSelectedGroup.get()) &&
		    mSelectedChild.get() >= 0) {
			
	    	final NoteItApplication 	app = (NoteItApplication) getApplication();
	    	ArrayList<ShoppingList> 	shoppingList = app.getShoppingList();
	    	ArrayAdapter<ShoppingList> 	shopListAdapter = new ArrayAdapter<ShoppingList>(
				this, 
				android.R.layout.simple_dropdown_item_1line,
				shoppingList);
	    	AlertDialog shoppingLists = new AlertDialog.Builder(this)
	    		.setTitle(getResources().getString(R.string.itemlist_copytolist))
	    		.setAdapter(shopListAdapter, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {

						final Item selItem = (Item) adapter.getChild(
								mSelectedGroup.get(), 
								mSelectedChild.get());
				    	ShoppingList targetList = app.getShoppingList().get(which);
				    	
						if (selItem != null && 
				    			targetList.mID != selItem.mListID) {
				    		
				    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
				    		app.copyItem(
			    				selItem.mID,
			    				targetList.mID,
			        			new OnMethodExecuteListerner() {
			    					public void onPostExecute(long resultCode, String message) {
			    						try {
				    						if (resultCode == 0) {
				    	    		    		CustomToast.makeText(
				    	    		    				ItemListActivity.this,
				    	    		    				mContentView,
				    	    		    				getResources().getString(R.string.itemlist_copytolistsuccess)).show(true);
				    						}
				    						else
				    	    		    		CustomToast.makeText(
				    	    		    				ItemListActivity.this,
				    	    		    				mContentView,
				    	    		    				message).show(true);
			    						} finally {
			    							mToolbar.hideIndeterminateProgress();
			    						}
			    					}
			    				});
				    	}
					}
				})
				.create();
	    	shoppingLists.show();
		}
	}
	
	protected void doMoveItem() {
		
		final ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter )getExpandableListView().getExpandableListAdapter();
		if (mSelectedGroup.get() < adapter.getGroupCount() &&
		    mSelectedGroup.get() >= 0 &&
		    mSelectedChild.get() < adapter.getChildrenCount(mSelectedGroup.get()) &&
		    mSelectedChild.get() >= 0) {
			
	    	final NoteItApplication 	app = (NoteItApplication) getApplication();
	    	ArrayList<ShoppingList> 	shoppingList = app.getShoppingList();
	    	ArrayAdapter<ShoppingList> 	shopListadapter = new ArrayAdapter<ShoppingList>(
				this, 
				android.R.layout.simple_dropdown_item_1line,
				shoppingList);
	    	AlertDialog shoppingLists = new AlertDialog.Builder(this)
	    		.setTitle(getResources().getString(R.string.itemlist_movetolist))
	    		.setAdapter(shopListadapter, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {

						final Item selItem = (Item) adapter.getChild(
								mSelectedGroup.get(), 
								mSelectedChild.get());
				    	ShoppingList targetList = app.getShoppingList().get(which);
				    	
						if (selItem != null && 
				    			targetList.mID != selItem.mListID) {
				    		
				    		final int	editBitmask = Item.ITEM_LISTID;
				        	final Item	newItem = app.new Item(selItem);
				        	newItem.mListID = targetList.mID;
				    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
				    		app.editItem(
				    				editBitmask, 
				    				newItem, 
				        			new OnAddItemListener() {
										
										public void onPostExecute(long resultCode, Item item, String message) {
				    						try {
					    						if (resultCode == 0) {
					    							adapter.DeleteItem(selItem);
					    							adapter.notifyDataSetChanged();
					    						}
					    						else
					    	    		    		CustomToast.makeText(
					    	    		    				ItemListActivity.this,
					    	    		    				mContentView,
					    	    		    				message).show(true);
				    						} finally {
				    				    		mToolbar.hideIndeterminateProgress();
				    						}
										}
				    				});
				    	}
					}
				})
				.create();
	    	shoppingLists.show();
		}
	}
	
    void doToggleMarkDone() {
    	
    	final Item selItem = (Item) getExpandableListView().getExpandableListAdapter().getChild(mSelectedGroup.get(), mSelectedChild.get());
    	if (selItem != null){
    		
    		if (selItem.mIsPurchased <= 0 && selItem.mIsAskLater > 0) {
    			// Item is being marked done and "Ask Later" is checked
    			doAskForPriceAndSave(selItem);
    		} else {
    			doCommitToggleItemDone(selItem, false, 0);
    		}
    	}
    }
    
    void doLikeItem(){
    	final Item selItem = (Item) getExpandableListView().getExpandableListAdapter().getChild(mSelectedGroup.get(), mSelectedChild.get());
    	if (selItem != null){
	    	
    		final NoteItApplication app = (NoteItApplication) getApplication();
    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
	    	app.setItemMetadata(selItem.mClassID, true, new OnItemVoteListener() {
				
				public void onPostExecute(long retVal, int voteCount, String message) {
					
					try {
						if (retVal != 0) {
	    		    		CustomToast.makeText(
	    		    				ItemListActivity.this,
	    		    				mContentView,
	    		    				message).show(true);
						} else {
							selItem.mLikeCount = voteCount;
							ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)getExpandableListView().getExpandableListAdapter();
							if (adapter != null) {
								adapter.notifyDataSetChanged();
							}
						}
					} finally {
						mToolbar.hideIndeterminateProgress();
					}
				}
			});
    	}
    }
    
    void doDislikeItem(){
    	final Item selItem = (Item) getExpandableListView().getExpandableListAdapter().getChild(mSelectedGroup.get(), mSelectedChild.get());
    	if (selItem != null){
	    	final NoteItApplication app = (NoteItApplication) getApplication();
    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
	    	app.setItemMetadata(selItem.mClassID, false, new OnItemVoteListener() {
				
	    		public void onPostExecute(long retVal, int voteCount, String message) {
	    			
	    			try {
						if (retVal != 0) {
	    		    		CustomToast.makeText(
	    		    				ItemListActivity.this,
	    		    				mContentView,
	    		    				message).show(true);
						} else {
							selItem.mLikeCount = voteCount;
							ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)getExpandableListView().getExpandableListAdapter();
							if (adapter != null) {
								adapter.notifyDataSetChanged();
							}
						}
	    			} finally {
	    	    		mToolbar.hideIndeterminateProgress();
	    			}
				}
			});
    	}
    }
    
    protected void doExpandPending() {
    	ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
    	for (int i = 0; i < adapter.getGroupCount(); i++){
    		if (adapter.getUnpurchasedChildrenCount(i) > 0)
    			getExpandableListView().expandGroup(i);
    	}        	
    }
    
    protected void doExpandAll() {
    	ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
    	for (int i = 0; i < adapter.getGroupCount(); i++){
    		getExpandableListView().expandGroup(i);
    	}        	
    }
    
    protected void doCollapseAll() {
    	ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
    	for (int i = 0; i < adapter.getGroupCount(); i++){
    		getExpandableListView().collapseGroup(i);
    	}        	
    	
    }
    
    protected void doSetupToolbarButtons(String listName) {

    	ImageButton homeButton = mToolbar.addLeftAlignedButton(R.drawable.home, true, true);
    	homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(ItemListActivity.this, DashBoardActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
    	
    	mShoppingListButton = new Button(this);
    	mShoppingListButton.setText(listName);
    	mToolbar.addCenterFillButton(mShoppingListButton);
    	mShoppingListButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doDisplayShoppingLists();
			}
		});
		
    	ImageButton addButton = mToolbar.addRightAlignedButton(R.drawable.add, false, true);
    	addButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doAddItem();
			}
		});
    	
    	ImageButton scanBarcodeButton = mToolbar.addRightAlignedButton(R.drawable.barcode, true, false);
    	scanBarcodeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				doScanBarcode();
			}
		});
    	
    	ImageButton expandCollapse = mToolbar.addRightAlignedButton(R.drawable.expand_collapse, true, true);
    	expandCollapse.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mExpandCollapseQA.show(v);
			}
		});
    	
    	mExpandCollapseQA = new QuickAction(this);
    	ActionItem collapseAll = new ActionItem(
    									QA_COLLAPSE,
    									getString(R.string.itemlist_collapseall),
    									getResources().getDrawable(R.drawable.collapse));
    	ActionItem expandAll = new ActionItem(
	    		QA_EXPAND, 
	    		getString(R.string.itemlist_expandall), 
	    		getResources().getDrawable(R.drawable.expand));
    	mExpandCollapseQA.addActionItem(collapseAll);
    	mExpandCollapseQA.addActionItem(expandAll);
    	mExpandCollapseQA.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			
			public void onItemClick(QuickAction source, int pos, int actionId) {
				
				switch(actionId){
				case QA_EXPAND:
					doExpandAll();
					break;
				case QA_COLLAPSE:
					doCollapseAll();
					break;
				}
			}
		});		
    }
    
    private void addFooterToListView(boolean add) {
    	
    	if (add) {
    		
    		if (mLoadMoreFlipper == null) {
	    		mLoadMoreFlipper = (ViewFlipperHack) mLayoutInflater.inflate(
	    				R.layout.itemlist_moreswitcher, 
	    				null, 
	    				false);	    		
	    	}
    		
			if (mLoadMoreFlipper != null) {
	    		String text = String.format(
	    				getString(R.string.itemlist_loadmorebutton), 
	    				NoteItApplication.getItemBatchSize());
	    		Button moreBtn = (Button) mLoadMoreFlipper.findViewById(R.id.itemlist_morebuttom);
	    		moreBtn.setText(text);
	    		
				// Note: There seems to be a bug in android. If I don't call
				// addFooterView before setting the adapter, the footer view
				// is never added. Go Figure!
				getExpandableListView().addFooterView(mLoadMoreFlipper);
				Button moreButton = (Button) mLoadMoreFlipper.findViewById(R.id.itemlist_morebuttom);
				if (moreButton != null) {
					moreButton.setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View v) {
							fetchItems(ItemListActivity.this);
						}
					});
				}
			}
    	} else {
//    		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//    		if (wm != null) {
//    			wm.removeView(mLoadMoreFlipper);
//    		}
    		mLoadMoreFlipper.stopFlipping();
    		mLoadMoreFlipper.removeAllViews();
    		getExpandableListView().removeFooterView(mLoadMoreFlipper);
    		mLoadMoreFlipper = null;
    	}
    }
    
    protected float doAskForPriceAndSave(final Item item) {

		// inflate the view from resource layout
		final View dialogView = mLayoutInflater.inflate(R.layout.dialog_addshoppinglist, (ViewGroup) findViewById(R.id.dialog_addshoppinglist_root));
		String text = String.format(
				getResources().getString(R.string.addedit_askLaterPrompt), 
				item.mQuantity,
				((NoteItApplication) getApplication()).getUnitFromID(item.mUnitID).mAbbreviation, 
				item.mName);
		
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setView(dialogView)
			.setTitle(getResources().getString(R.string.addedit_asklater_price))
			.setCancelable(false)
			.create();
    	
    	dialog.setMessage(text);
		dialog.setOwnerActivity(this);
		final EditText editPrice = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
		dialog.setButton(
				DialogInterface.BUTTON1, 
				getResources().getString(R.string.Skip), 
				new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				// Even if the user cancels the Price dialog, we still need to toggle the done state
				doCommitToggleItemDone(item, false, 0);
			}
		});
		dialog.setButton(
				DialogInterface.BUTTON2, 
				getResources().getString(R.string.OK), 
				new DialogInterface.OnClickListener() {
		
			public void onClick(DialogInterface dialog, int which) {
				if (editPrice != null) {
					doCommitToggleItemDone(item, true, Float.valueOf(editPrice.getText().toString()));
				} else {
					doCommitToggleItemDone(item, true, 0);
				}
			}
		});
	
		if (editPrice != null) {
			editPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			editPrice.setSelectAllOnFocus(true);
			editPrice.setText(String.valueOf(item.mUnitPrice));
			editPrice.requestFocus();
		}
		dialog.show();
		return 0;
	}
    
	// Send in the item as is without any changes
    protected void doCommitToggleItemDone(final Item item, final boolean resetAskLater, final float newPrice) {
    	
    	final NoteItApplication 	app = (NoteItApplication) getApplication();
    	final Item 					newItem = app.new Item(item);
    	int							editBitmask = Item.ITEM_ISPURCHASED;
    	
    	newItem.mIsPurchased = newItem.mIsPurchased > 0 ? 0 : 1;

    	if (resetAskLater) {
			newItem.mIsAskLater = 0;
			editBitmask = editBitmask | Item.ITEM_ISASKLATER;
    	}
    	
    	if (newPrice > 0) {
    		editBitmask = editBitmask | Item.ITEM_UNITCOST;
    		if (newItem.mQuantity > 0)
    			newItem.mUnitPrice = newPrice / newItem.mQuantity;
    		else
    			newItem.mUnitPrice = newPrice;
    	}
    	
    	mToolbar.showInderminateProgress(getString(R.string.progress_message));
		app.editItem(
				editBitmask, 
    			newItem, 
    			new OnAddItemListener() {
					
					public void onPostExecute(long resultCode, Item editedItem, String message) {
						try {
							if (resultCode == 0) {
								item.copyFrom(editedItem);
								ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter)getExpandableListView().getExpandableListAdapter();
								if (item.mIsPurchased > 0) {
									// Shuffle purchased item to the bottom
									adapter.DeleteItem(item);
									adapter.AddItem(item, app.getCategory(item.mCategoryID));
								}
								if (item.mIsPurchased > 0)
									doDeductFromPendingTotal(item.mUnitPrice * item.mQuantity);
								else
									doAddToPendingTotal(item.mUnitPrice * item.mQuantity);
								doDisplayPendingTotal();
								adapter.notifyDataSetChanged();
							}
							else
    	    		    		CustomToast.makeText(
    	    		    				ItemListActivity.this,
    	    		    				mContentView,
    	    		    				message).show(true);
						} finally {
							mToolbar.hideIndeterminateProgress();
						}
					}
			});
    }
    
    protected void doDisplayShoppingLists() {
    	
    	final NoteItApplication app = (NoteItApplication) getApplication();
    	
    	ArrayList<ShoppingList> shoppingList = app.getShoppingList();
    	ShoppingListAdapterWithIcons adapter = new ShoppingListAdapterWithIcons(
    			this, 
    			R.layout.spinner_shoppinglists_item,
    			R.id.shoppinglist_name,
    			shoppingList);
    	AlertDialog shoppingLists = new AlertDialog.Builder(this)
    		.setTitle(R.string.itemlist_select_shoppinglist)
    		.setAdapter(adapter, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {

					ShoppingList shoppingList = app.getShoppingList(which);
					if (shoppingList != null) {
						mShoppingListButton.setText(shoppingList.mName);
					}
					app.setCurrentShoppingListIndex(which);
					mIsItemListFetched = false;
					ItemsExpandableListAdapter adapter;
					if ((adapter = new ItemsExpandableListAdapter(ItemListActivity.this)) != null) {
						// Note: There seems to be a bug in android. If I don't call
						// addFooterView before setting the adapter, the footer view
						// is never added. Go Figure!
						addFooterToListView(true);
						getExpandableListView().setAdapter(adapter);
					}
					fetchItems(ItemListActivity.this);
					doFetchAndDisplayPendingTotal();
				}
			})
			.create();
    	shoppingLists.show();
    }
    
    protected void doDisplayItems(ArrayList<Item> items) {
		
    	ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
    	for (int index = 0; index < items.size(); index++){
    		Item 	thisItem = items.get(index);
    		if (thisItem.mIsPurchased <= 0 || ((thisItem.mIsPurchased > 0) && !mHideDoneItems)) {
	    		NoteItApplication.Category category = ((NoteItApplication)getApplication()).getCategory(thisItem.mCategoryID);
	    		adapter.AddItem(thisItem, category);
    		}
    	}
		NoteItApplication app = (NoteItApplication) getApplication();
		if (app != null) { 
			if (getExpandableListView().getFooterViewsCount() > 0) {
				if  (!app.isMoreItemsPending()) {
					addFooterToListView(false);
				}
			} else {
				if (app.isMoreItemsPending()){
					addFooterToListView(true);
				}
			}
		}
    	adapter.notifyDataSetChanged();
    	Log.i("NoteItApplication.doDisplayItems", "Notified Adapter of change");
		doExpandPending();
    }
    
    void doFetchAndDisplayPendingTotal() {
    	NoteItApplication app = (NoteItApplication) getApplication();
    	app.getPendingTotal(
    		app.getCurrentShoppingListID(), 
    		new NoteItApplication.OnGetPendingTotalListener() {
			
			public void onPostExecute(long resultCode, float total, String message) {
				if (resultCode == 0) {
					mPendingTotal = total;
					doDisplayPendingTotal();
				}
				else {
					mPendingTotal = 0f;
					doDisplayPendingTotal();
				}
			}
		});
    }
    
    void doAddToPendingTotal(float add) {
    	mPendingTotal += add;
    }
    
    void doDeductFromPendingTotal(float deduct) {
    	mPendingTotal -= deduct;
    }
    
    void doDisplayPendingTotal() {
		final String 	remainingFormat = getResources().getString(R.string.itemlist_remaining);
		LinearLayout 	statusBar = (LinearLayout) findViewById(R.id.bottom_bar);

		if (statusBar != null && mPendingTotal > 0) {
			statusBar.setVisibility(View.VISIBLE);

			float taxes = 0f;
			try {
				taxes = Float.valueOf(mPrefs.getString("taxes", "0"));
			} catch (Exception e) {
			}
			
			float total = mPendingTotal;
			if (taxes > 0) {
				total += (taxes / 100f) * total;
			}
			
			String strRemaining = String.format(remainingFormat, String.format(mCurrencyFormat, total));
			TextView textViewRemaining = (TextView) statusBar.findViewById(R.id.bottom_remaining);
			if (textViewRemaining != null) 
				textViewRemaining.setText(strRemaining);
			
		} else if (statusBar != null && mPendingTotal <= 0) {
			statusBar.setVisibility(View.GONE);
		}
    }
    
    protected void doShare() {
    	
    	String[] 				optionsArray = getResources().getStringArray(R.array.shoplist_share_options);
    	ArrayAdapter<String> 	adapter = new ArrayAdapter<String>(
								    			this, 
								    			android.R.layout.simple_dropdown_item_1line,
								    			optionsArray);
    	AlertDialog shareOptions = new AlertDialog.Builder(this)
    		.setTitle(R.string.sharewith_title)
    		.setAdapter(adapter, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					
					switch (which) {
					case 0:
						doShareWithUser();
						break;
					default:
						doPickApp();
					}
				}
			})
			.create();
    	shareOptions.show();
    }
    
    void doShareWithUser() {
		// inflate the view from resource layout
		LayoutInflater	inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View 		dialogView = inflater.inflate(R.layout.dialog_addshoppinglist, null, false);
		final EditText 	editListName = (EditText) dialogView.findViewById(R.id.dialog_addshoppinglist_editTextName);
		
		AlertDialog dialog = new AlertDialog.Builder(this)
			.setView(dialogView)
			.setTitle(getResources().getString(R.string.sharewithuser_title))
			.create();

		editListName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		dialog.setButton(DialogInterface.BUTTON1, "OK", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				final String emailID = editListName.getText().toString();

				if (!emailID.trim().matches("")) {
					dialog.dismiss();
				
					NoteItApplication app = (NoteItApplication) getApplication();
					app.shareShoppingList(app.getCurrentShoppingListID(), emailID, new OnMethodExecuteListerner() {
						
						public void onPostExecute(long resultCode, String message) {
							
							if (resultCode != 0) {
								CustomToast.makeText(
										ItemListActivity.this,
										getExpandableListView(),
										message).show(true);
							} else {
								CustomToast.makeText(
										ItemListActivity.this, 
										getExpandableListView(), 
										String.format(getResources().getString(R.string.sharewith_succeded), emailID)).show(true);
							}
						}
					});
				}
			}
		});
	
		dialog.setButton(DialogInterface.BUTTON2, "Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Log.e("AddShoppingList", "Cancel");
				dialog.dismiss();
			}
		});

		dialog.show();    
	}
    
    protected void doPickApp() {
    	NoteItApplication	app = (NoteItApplication) getApplication();
    	final Intent 		emailIntent = new Intent(Intent.ACTION_SEND);
    	emailIntent.setType("text/plain");
    	emailIntent.putExtra(
    			Intent.EXTRA_SUBJECT, 
    			app.getShoppingList().get(app.getCurrentShoppingListIndex()).mName);
    	emailIntent.putExtra(Intent.EXTRA_TEXT, formatItemsList());
    	startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.itemlist_emailPrompt)));
    }

    protected void doAllDone() {
    	NoteItApplication app = (NoteItApplication) getApplication();
    	if (app != null) {
    		mToolbar.showInderminateProgress(getString(R.string.progress_message));
    		app.markAllItemsDone(app.getCurrentShoppingListID(), true, new OnMethodExecuteListerner() {
				
				public void onPostExecute(long resultCode, String message) {
					try {
						if (resultCode == 0) {
							ItemsExpandableListAdapter adapter = (ItemsExpandableListAdapter) getExpandableListView().getExpandableListAdapter();
							for (int group = 0; group < adapter.getGroupCount(); group++)
								for (int child = 0; child < adapter.getChildrenCount(group); child++)
								{
									Item item = (Item) adapter.getChild(group, child);
									if (item != null && item.mIsPurchased <= 0) {
										item.mIsPurchased = 1;
									}
								}
							adapter.notifyDataSetChanged();
						} else
	    		    		CustomToast.makeText(
	    		    				ItemListActivity.this,
	    		    				mContentView,
	    		    				message).show(true);
					} finally {
						mToolbar.hideIndeterminateProgress();
					}
				}
			});
    	}
    }
    
    protected String formatItemsList() {
    	NoteItApplication app = (NoteItApplication) getApplication();
    	String str = getResources().getString(R.string.itemlist_emailIntro) + "\n\n";
    	for (Item item : app.getItems()) {
    		str += item.mName;
    		if (item.mQuantity > 0) {
    			str += ", " + item.mQuantity;
    			str += " " + app.getUnitFromID(item.mUnitID);
    		}
    		if (item.mUnitPrice > 0)
    			str += ", " + item.mUnitPrice;
    		str += "\n";
    	}

    	str += "\n\n";
    	str += getResources().getString(R.string.itemlist_emailsig);		
    	return str;
    }
    
    protected void doScanBarcode() {
    	
    	IntentIntegrator integrator = new IntentIntegrator(this);
    	mInstallScanAppDialog = integrator.initiateScan();
    }
    
	// Custom adapter for my shopping items
	public class ItemsExpandableListAdapter extends BaseExpandableListAdapter {

		private ArrayList<Category>				mCategories;

		/* mItems
		  -------------------------------------------
	  	  | Category 0   		| ArrayList<Item>	|
		  ------------------------------------------
		  | Category n 			| ArrayList<Item>	|
		  -------------------------------------------*/
		private ArrayList<ArrayList<Item>>		mItems;
		private Context							mContext = null;
		private String							mUnitPriceFormat;
		
		ItemsExpandableListAdapter(Context context){
			
			mContext = context;
			mCategories = new ArrayList<Category>();
			mItems = new ArrayList<ArrayList<Item>>();
			mUnitPriceFormat = getResources().getString(R.string.itemlist_unitpriceformat);
			for (int i = 0; i < mCategories.size(); i++){
				// Initialize the mItems ArrayList
				mItems.add(new ArrayList<Item>());
			}
		}
		
		public void AddCategory(Category category){
			
			if (!mCategories.contains(category)) {
				assert(category.mName != "");
				mCategories.add(category);
			}
		}
		
		// Returns the packed position for the newly added item
		public long AddItem(Item item, Category category){
			
			if (category != null){
				int index = mCategories.indexOf(category);
				if (index < 0){
					// Category has not been added, add it.
					mCategories.add(category);
					index = mCategories.indexOf(category);
				}
				if (index > mItems.size() - 1){
					mItems.add(new ArrayList<Item>());
				}
				mItems.get(index).add(item);
				return ExpandableListView.getPackedPositionForChild(
						index, 
						mItems.get(index).size() - 1);
			} else 
				return ExpandableListView.PACKED_POSITION_TYPE_NULL;
		}
		
		public void DeleteItem(final Item item) {
			
			if (item != null) {
				Category category = ((NoteItApplication) getApplication()).getCategory(item.mCategoryID);
				if (category != null) {
					int categoryIndex = mCategories.indexOf(category);
					if (categoryIndex >= 0 && categoryIndex < mItems.size()) {
						mItems.get(categoryIndex).remove(item);
						// if this is the last item remove the category as well
						if (mItems.get(categoryIndex).size() == 0) { 
							mItems.remove(categoryIndex);
							mCategories.remove(category);
						}
					}
				}
			}
		}
		
		public Object getChild(int groupPosition, int childPosition) {
			
			if (groupPosition >= 0 && groupPosition < mItems.size() &&
				childPosition >= 0 && childPosition < mItems.get(groupPosition).size())
				return mItems.get(groupPosition).get(childPosition);
			else
				throw new IndexOutOfBoundsException();
		}

		public long getChildId(int groupPosition, int childPosition) {
			
			return childPosition;
		}

		public Object getNextChild(int groupPosition, int childPosition) {
			
			if (childPosition < getChildrenCount(groupPosition) - 1) {
				// There are more next items in the same group
				return getChild(groupPosition, childPosition + 1);
			} else  if (groupPosition < getGroupCount() - 1)
				return getChild(groupPosition + 1, 0);
			
			return null;
		}
		
		public Object getPrevChild(int groupPosition, int childPosition) {
			
			if (childPosition > 0) {
				// There are more next items in the same group
				return getChild(groupPosition, childPosition -1);
			} else if (groupPosition > 0) {
				return getChild(
					groupPosition - 1, 	// The previous group
					getChildrenCount(groupPosition - 1) -1); // Last child
			}
			
			return null;
		}
		
		public boolean getNextChildPosition(
				AtomicInteger groupPosition, //[IN/OUT] 
				AtomicInteger childPosition) //[IN/OUT]
		{
			
			if (childPosition.get() < getChildrenCount(groupPosition.get()) - 1) {

				childPosition.set(childPosition.get() + 1);
				return true;
			} else if (groupPosition.get() < getGroupCount() - 1) {
				
				groupPosition.set(groupPosition.get() + 1);
				childPosition.set(0);
				return true;
			}
			
			return false;
		}

		public boolean getPrevChildPosition(
				AtomicInteger groupPosition, //[IN/OUT] 
				AtomicInteger childPosition) //[IN/OUT]
		{
			
			if (childPosition.get() > 0) {

				childPosition.set(childPosition.get() - 1);
				return true;
			} else if (groupPosition.get() > 0) {
				
				groupPosition.set(groupPosition.get() - 1);
				childPosition.set(getChildrenCount(groupPosition.get()) - 1);
				return true;
			}
			
			return false;
		}

		public int getTotalChildrenCount() {
			int count = 0;
			for (int index = 0; index < mCategories.size(); index++)
				count += mItems.get(index).size();
			return count;
		}
		
		public int getChildrenCount(int groupPosition) {
			
			return mItems.get(groupPosition).size();
		}

		public Object getGroup(int groupPosition) {
			if (groupPosition >=0 && groupPosition < getGroupCount())
				return mCategories.get(groupPosition);
			else 
				return null;
		}

		public int getGroupCount() {
			
			return mCategories.size();
		}

		public long getGroupId(int groupPosition) {
			
			return groupPosition;
		}

		public boolean hasStableIds() {
			
			return true;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			
			return true;
		}

	    public View getView(ViewGroup parent) {
	    	
	    	if (mLayoutInflater != null) {
	    		return mLayoutInflater.inflate(R.layout.listitems_item, parent, false);
	    	} else
	    		return null;
	    }
	    
		/* From Android Documentation: Makes life interesting
		 * 
		 * @param convertView the old view to reuse, if possible. You should check
		 *            that this view is non-null and of an appropriate type before
		 *            using. If it is not possible to convert this view to display
		 *            the correct data, this method can create a new view. It is not
		 *            guaranteed that the convertView will have been previously
		 *            created by
		 *            {@link #getChildView(int, int, boolean, View, ViewGroup)}.
		 */
	    public void setViewParams(View view, int height) {

	    	 ViewGroup.LayoutParams params = new AbsListView.LayoutParams(
		                ViewGroup.LayoutParams.FILL_PARENT, 
		                height);
	        
	        view.setLayoutParams(params);
	        view.setPadding(height + 10, 0, 0, 0);
	    }
	    
	    public View getChildView(
	    		int groupPosition, 
	    		int childPosition,
				boolean isLastChild, 
				View convertView, 
				ViewGroup parent) {

	        Item thisItem = (Item) getChild(groupPosition, childPosition);;
	        View view;
	        
	        if (convertView == null) {
		        view = getView(parent);
	        } else {
	        	view = convertView;
	        }
	        
	        if (view != null) {
	        	LinearLayout			details = (LinearLayout) view.findViewById(R.id.itemList_details);
    	        TextView				textView = (TextView) view.findViewById(R.id.itemlist_name);
    	        TextView				likeCount = (TextView) view.findViewById(R.id.itemlist_likeCount);
    	        TextView 				quantity = (TextView) view.findViewById(R.id.itemlist_quantity);
    	        TextView 				price = (TextView) view.findViewById(R.id.itemlist_price);
    	        TextView				total = (TextView) view.findViewById(R.id.itemlist_Total);
    	        ViewGroup.LayoutParams 	params = (ViewGroup.LayoutParams) parent.getLayoutParams();
    	        
    	        if (params != null) {  
    	            params.width = ViewGroup.LayoutParams.FILL_PARENT;
    	            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
    	        } else {
    	        	params = new AbsListView.LayoutParams(
    		                ViewGroup.LayoutParams.FILL_PARENT, 
    		                ViewGroup.LayoutParams.WRAP_CONTENT);
    	        }	

        		if (textView != null) {

			        if (thisItem.mIsPurchased > 0) {
			        	textView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
			        	textView.setTextAppearance(
			        			mContext, 
			        			NoteItApplication.getPreferredTextAppearance(mContext, mFontSize, ItemType.DONE));
			        }
			        else { 
			        	textView.setTextAppearance(
			        			mContext, 
			        			NoteItApplication.getPreferredTextAppearance(mContext, mFontSize, ItemType.PENDING));
			        	textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			        }
			        
			        textView.setText(thisItem.mName.toString());
			        textView.setBackgroundColor(android.R.color.transparent);
	        	}
	        	
	        	if (mDisplayExtras && quantity != null && thisItem.mQuantity > 0 ) {
	        		
	        		NoteItApplication 	app = (NoteItApplication) getApplication();
	        		String 				unit = app.getUnitFromID(thisItem.mUnitID).mAbbreviation; 
	        		
	        		details.setVisibility(View.VISIBLE);
	        		quantity.setText(String.valueOf(thisItem.mQuantity) + " " + unit); 
	        		quantity.setVisibility(View.VISIBLE);
	        		quantity.setPaintFlags(
	        			thisItem.mIsPurchased > 0 ?
	        					Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG :
	        					textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);	
	        		
	        		if (price != null && thisItem.mUnitPrice > 0){
		        		
	        			String strPrice = String.format(
		        				mUnitPriceFormat, 
		        				String.format(mCurrencyFormat, thisItem.mUnitPrice),
		        				unit);
		        		String strTotal = String.format(mCurrencyFormat, thisItem.mUnitPrice * thisItem.mQuantity);
			        	
		        		price.setPaintFlags(
			        			thisItem.mIsPurchased > 0 ?
	        					Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG :
		        				textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);	
			        	total.setPaintFlags(	        			
			        			thisItem.mIsPurchased > 0 ?
	        					Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG :
		        				textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);	
		        		total.setText(strTotal);
		        		
				        if (thisItem instanceof ItemAndStats) {
				        	ItemAndStats itemStats = (ItemAndStats) thisItem;
			        		int deviationRange = itemStats.getDeviationRange();
			        		price.setText(strPrice + getStatsText(deviationRange));
				        } else {
			        		price.setText(strPrice);
				        }
				        
		        		price.setVisibility(View.VISIBLE);
		        		total.setVisibility(View.VISIBLE);
		        	} else {
		        		price.setVisibility(View.GONE);
		        		total.setVisibility(View.GONE);
		        	}
	        	} else {
	        		details.setVisibility(View.GONE);
	        		quantity.setVisibility(View.GONE);
	        		price.setVisibility(View.GONE);
	        		total.setVisibility(View.GONE);
	        	}
	        	
	        	if (thisItem.mLikeCount > 0) {
	        		likeCount.setVisibility(View.VISIBLE);
	        		likeCount.setText(String.format("%,d", thisItem.mLikeCount));
	        	} else {
	        		likeCount.setVisibility(View.GONE);
	        	}
	        }
	        return view;
		}

	    public View getGroupView(
	    		int groupPosition, 
	    		boolean isExpanded,
				View convertView, 
				ViewGroup parent) {
	    	
	    	ViewGroup viewGroup = null;
	    	if (convertView == null) {
		    	if (mLayoutInflater != null) {
		    		viewGroup = (ViewGroup) mLayoutInflater.inflate(R.layout.listitems_group, parent, false);
		    	}
	    	} else {
	    		viewGroup = (ViewGroup) convertView;
	    	}
	    	
    		if (viewGroup != null) {
		    	TextView  	textView = (TextView) viewGroup.findViewById(R.id.itemslist_categoryName);
		    	TextView  	totals = (TextView) viewGroup.findViewById(R.id.itemslist_categoryTotals);
		    	
		        textView.setText(getGroup(groupPosition).toString());
		        textView.setTextAppearance(
		        	mContext, 
		        	NoteItApplication.getPreferredTextAppearance(
		        			mContext, mFontSize, ItemType.GROUP));

		        if (mDisplayCategoryExtras) {
			    	String 		totalsText;
			    	totalsText = "(" + getUnpurchasedChildrenCount(groupPosition) + ")";
			        totals.setText(totalsText);
			        totals.setTextAppearance(
			        	mContext, 
			        	NoteItApplication.getPreferredTextAppearance(
			        		mContext, mFontSize, ItemType.GROUP));
			        totals.setPadding(0, 0, ((ExpandableLVRightIndicator) getExpandableListView()).getRightMargin(), 0);
			        totals.setVisibility(View.VISIBLE);
		        } else {
			        totals.setVisibility(View.GONE);
		        }
    		}
    		return viewGroup;
		}
	    
	    public int getUnpurchasedChildrenCount(int groupPosition) {
	    	int count = 0;
	    	for (Item item : mItems.get(groupPosition)) {
	    		count += (item.mIsPurchased <= 0 ? 1 : 0);
	    	}
	    	return count;
	    }
	    
	    protected String getStatsText(int deviationType) {
	    	
	    	final String up = mContext.getString(R.string.itemlist_Price_Up);
	    	final String down = mContext.getString(R.string.itemlist_Price_Down); 
	    	if (deviationType == ItemAndStats.kUP_AlarmingDev) {
	    		return " " + up + up + up;	
	    	} else if (deviationType == ItemAndStats.kUp_TwoStandardDev) {
				return " " + up + up;
	    	} else if (deviationType == ItemAndStats.kUp_OneStandardDev) {
	    		return " " + up;
	    	} else if (deviationType == ItemAndStats.kDown_AlarmingDev) {
    			return " " + down + down + down;
    		} else if (deviationType == ItemAndStats.kDown_TwoStandardDev) {
    			return " " + down + down;
    		} else if (deviationType == ItemAndStats.kDown_OneStandardDev) {
    			return " " + down;
    		}
	    	return "";
	    }
	}
	
	protected int getResourceIdFromAttribute(int attribId) {
		Resources.Theme theme = getTheme();
		TypedValue 		resID = new TypedValue();
		theme.resolveAttribute(attribId, resID, false);
		return resID.data;
	}

	public void onSensorChanged(int sensor, float[] values) {
	}

	public void onAccuracyChanged(int sensor, int accuracy) {
	}
	
	private void doSignOut() {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(LoginActivity.DONT_LOGIN, true);
		startActivity(intent);
		finish();
	}
}
