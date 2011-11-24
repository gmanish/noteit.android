package com.geekjamboree.noteit;

import java.util.ArrayList;

import com.geekjamboree.noteit.NoteItApplication.Item;
import com.geekjamboree.noteit.NoteItApplication.Category;
import com.geekjamboree.noteit.NoteItApplication.OnMethodExecuteListerner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class AddEditItemActivity extends Activity {

	static final int 	UNITS_GENERIC = 0;
	static final int 	UNITS_METRIC = 1;
	static final int	UNITS_IMPERIAL = 2;
	Item 				thisItem;
	
	// Entry mode is true when the user adds items details 
	// and clicks on "Continue" instead of "Done"
	boolean				mEntryMode = false;
	
	// controls
	EditText			mEditName;
	Spinner				spinCategories;
	
    public void onCreate(Bundle savedInstanceState) { 
    	
    	super.onCreate(savedInstanceState);
    	
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.add_edit_item_view);
        toolbar.SetTitle(getResources().getText(R.string.addedit_Title));
        
        // Are we opening in the add or edit mode
        final Intent intent = getIntent();
        boolean add = intent.getBooleanExtra("ADD", true);
        if (!add) {
        	// read details of this item from the back-end
        }
        
        spinCategories = (Spinner) findViewById(R.id.addedit_spinCategories);
        if (spinCategories != null) {
        	populateCategories();
        }
        
        mEditName = (EditText) findViewById(R.id.addedit_editName);
        Button doneBtn = (Button) findViewById(R.id.addedit_btnDone);
        doneBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mEntryMode = false;
				addItem();
			}
		});

        Button continueBtn = (Button) findViewById(R.id.addedit_btnContinue);
        continueBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mEntryMode = true;
				addItem();
			}
		});
    }

    protected void addItem() {
    	Category	category = (Category)spinCategories.getSelectedItem();
    	
    	Item newItem = new Item(
    			((NoteItApplication) getApplication()).getCurrentShoppingList(),
    			category.mID,
    			mEditName.getEditableText().toString());
    	
    	((NoteItApplication) getApplication()).addItem(newItem, new OnMethodExecuteListerner() {
			
			public void onPostExecute(long resultCode, String message) {
				if (resultCode == 0){
					Toast.makeText(AddEditItemActivity.this, "Item has been added", Toast.LENGTH_LONG);
					if (!mEntryMode){
						// We'll dismiss the Activity now
						Intent intent = getIntent();
						intent.putExtra("RESULT", true);
						AddEditItemActivity.this.setResult(RESULT_OK);
						finish();
					}
					else {
						// Clear the controls and continue adding
					}
				}
				else
					Toast.makeText(AddEditItemActivity.this, message, Toast.LENGTH_LONG);
			}
		});
    }
    
    protected void populateCategories() {
    	ArrayList<Category> categories = ((NoteItApplication)getApplication()).getCategories();
        ArrayAdapter<Category> adapter = new ArrayAdapter<Category>(
                this, 
                android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategories.setAdapter(adapter);
        spinCategories.setOnItemSelectedListener(
                new OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(AddEditItemActivity.this, "Spinner1: position=" + position + " id=" + id, Toast.LENGTH_LONG);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        Toast.makeText(AddEditItemActivity.this, "Spinner1 unselected", Toast.LENGTH_LONG);
                    }
                });
    }
}
