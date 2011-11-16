package com.geekjamboree.noteit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import com.geekjamboree.noteit.R;

public class MainActivity extends Activity {

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
 
    	super.onCreate(savedInstanceState);
    	CustomTitlebarWrapper toolbar = new CustomTitlebarWrapper(this);
        setContentView(R.layout.main);
        toolbar.SetTitle("Note It!");
        Button next = (Button) findViewById(R.id.buttonLogin);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), LoginActivity.class);
                startActivity(myIntent);
            }

        });
    }
}