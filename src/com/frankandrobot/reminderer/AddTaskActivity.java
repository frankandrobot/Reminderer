package com.frankandrobot.reminderer;

import com.frankandrobot.reminderer.Parser.GrammarParser;
import com.frankandrobot.reminderer.Parser.Task;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddTaskActivity extends Activity {
    Task mTask;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.add_task);
	
	Button add = (Button)this.findViewById(R.id.submit);
	add.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		GrammarParser parser = new GrammarParser();
		parser.setAndroidContext(AddTaskActivity.this);
		EditText text = (EditText) findViewById(R.id.add_task);
		String input = text.getText().toString();
		mTask = parser.parse(input);
		String output = mTask.toString();
		TextView log = (TextView) findViewById(R.id.log_cat);
		log.setText(output);
	    }
	    
	});
	
	Button save = (Button)findViewById(R.id.save);
	save.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	    }
	    
	});
	
	Button cancel = (Button)findViewById(R.id.cancel);
	cancel.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		finish();
		
	    }
	    
	});
	
	// stringTester("June 2, 2003<--this should work");
	// stringTester("6/2<--this should work");
	// stringTester("Jun 2, 2003<---this should work");
	// stringTester("Jun 2<---this should work");
	// stringTester("hello June 2<---this should fail");
	// parseTester("hello"); // this should pass
	// parseTester("buy eggs June 2 8pm"); // pass
	// parseTester("buy eggs"); // pass
	// parseTester("milk at July 1 on 7:15p"); // pass
	// parseTester("buy egss repeats daily"); // pass
    }
}
